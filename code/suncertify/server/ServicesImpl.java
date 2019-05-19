package suncertify.server;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import suncertify.db.RecordNotFoundException;
import suncertify.db.DuplicateKeyException;
import suncertify.db.Data;
import suncertify.db.Contractor;

/**
 *  An implementation of the <code>Services</code> interface, allowing clients
 *  to access a local database.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public final class ServicesImpl implements Services {

    /**
     * A reference to the data access class.
     */
    private Data databaseAccess;

    /**
     * A reference to the single instance of this class.
     */
    private static ServicesImpl ref;


    /**
     *  Private constructor to enforce singleton.
     *
     *@param  databaseFile  the data file.
     */
    private ServicesImpl(File databaseFile) {
        databaseAccess = Data.getInstance(databaseFile.getPath());
    }


    /**
     *  Gets the list of contractors from the database.
     *
     *@return                  the list of contractors in the database.
     *@exception  IOException  thrown if there is a problem
     *      accessing the database file
     */
    public List getContractors() throws IOException {
        return databaseAccess.getContractorList();
    }


    /**
     *  Books a contractor record for a client.
     *
     *@param  id                           the identifier of the contractor
     *      record to book.
     *@param  customerID                   the id of the customer against whom
     *      to book this contractor.
     *@return                              true if this contractor was booked,
     *      else false.
     *@exception  IOException              thrown if there is a problem
     *      accessing the database file
     *@exception  RecordNotFoundException  thrown if the contractor is not
     *      found to book
     *@exception  SecurityException        thrown if record is locked by
     *      another user
     */
    public boolean book(int id, long customerID) throws IOException,
            RecordNotFoundException, SecurityException {
        /**
         * Read record to see if the contractor we are trying to
         * book is already booked by someone else, or is deleted.
         */
        String owner = databaseAccess.readRecord((long)id)[2].trim();
        boolean deleted = databaseAccess.readRecord((long)id)[6].equals("true");
        if (owner.length() > 0) {
            return false;
        } else if (deleted) {
            throw new RecordNotFoundException("This contractor has been "
                        + "deleted");
        }
 
        /* Lock the record and get the cookie. */
        long cookie = databaseAccess.lockRecord(id);
        /* Update the record's owner field with the CSR. */
        try {
            databaseAccess.updateRecord(id, new String[]{""
                    + customerID}, cookie);
        } catch (RecordNotFoundException rnf) {
            throw rnf;
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException();
        } finally {
            /* Unlock the record with the cookie. */
            databaseAccess.unlock(id, cookie);
        }
        return true;
    }


    /**
     *  Unbooks a contractor record for a client.
     *
     *@param  id                           the identifier of the contractor
     *      record to unbook.
     *@return                              true if this contractor was unbooked,
     *      else false.
     *@exception  IOException              thrown if there is a problem
     *      accessing the database file
     *@exception  RecordNotFoundException  thrown if the contractor is not
     *      found to unbook
     *@exception  SecurityException        thrown if record is locked by
     *      another user
     */
    public boolean unBook(int id) throws IOException,
            RecordNotFoundException, SecurityException {
        /**
         * Read record to see if the contractor we are trying to
         * unbook is already unbooked or deleted.
         */
        String owner = databaseAccess.readRecord((long)id)[2].trim();
        boolean deleted = databaseAccess.readRecord((long)id)[6].equals("true");
        if (owner.length() == 0) {
            return false;
        } else if (deleted) {
            throw new RecordNotFoundException("This contractor has been "
                        + "deleted");
        }
 
        /* Lock the record and get the cookie. */
        long cookie = databaseAccess.lockRecord(id);
        /* Update the record's owner field with to a blank string. */
        try {
            databaseAccess.updateRecord(id, new String[]{"        "}, cookie);
        } catch (RecordNotFoundException rnf) {
            throw rnf;
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            throw new IOException();
        } finally {
            /* Unlock the record with the cookie. */
            databaseAccess.unlock(id, cookie);
        }
        return true;
    }
    
    
    /**
     *  Finds contractors that match the specified search criteria.
     *
     *@param  name             the name to search for
     *@param  location         the location to search for
     *@return                  the list of contractors which match the
     *       search criteria
     *@exception  IOException  thrown if there is a problem accessing the
     *      database file
     */
    public List find(String name, String location) throws IOException {
        /* This list will hold successful results of the search. */
        List foundRecords = new ArrayList();
        long[] recordNumbers = null;

        String[] criteria = new String[3];
        criteria[0] = "0";
        criteria[1] = name.toLowerCase();
        criteria[2] = location.toLowerCase();

        recordNumbers = databaseAccess.findByCriteria(criteria);
        Iterator iter = databaseAccess.getContractorList().iterator();
        Contractor contractor = null; 
        
        while (iter.hasNext()) {
            contractor = (Contractor) iter.next();
            for (int i = 0; i < recordNumbers.length; i++) {
                if (contractor.getRecordNo() == recordNumbers[i]) {
                    foundRecords.add(contractor);
                }
            }
        }
        return foundRecords;
    }


    /**
     *  Deletes a contractor from the database.
     *
     *@param  id                           the identifier of the contractor to
     *      delete
     *@exception  IOException              thrown if there is a problem
     *      accessing the database file
     *@exception  RecordNotFoundException  thrown if the contractor is not
     *      found to delete
     *@exception  SecurityException        thrown if record is locked by
     *      another user
     */
    public void deleteContractor(int id)
             throws IOException, RecordNotFoundException, SecurityException {
        /**
         * Look through records to see if the contractor we are trying to
         * delete is already deleted.
         */
        Contractor contractor = null;
        Iterator iter = databaseAccess.getContractorList().iterator();
        while (iter.hasNext()) {
            contractor = (Contractor) iter.next();
            if (contractor.getRecordNo() == id
                    && contractor.getDeleted()) {
                throw new RecordNotFoundException("This contractor has been"
                        + "deleted");
            }
        }

        /* Lock the record and get the cookie. */
        long cookie = databaseAccess.lockRecord(id);
        try {
            databaseAccess.deleteRecord(id, cookie);
        } catch (RecordNotFoundException rnf) {
            throw rnf;
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            throw new IOException();
        } finally {
            /* Unlock the record with the cookie. */
            databaseAccess.unlock(id, cookie);
        }
    }


    /**
     *  Adds a contractor to the database.
     *
     *@param  data             an array containing the contractor record data
     *      to add
     *@exception  IOException  thrown if there is a problem accessing the
     *      database file
     */
    public void addContractor(String[] data) throws IOException {
        try {
            databaseAccess.createRecord(data);
        } catch (DuplicateKeyException dke) {
            throw new IOException();
        }
    }

    
    /**
     *  Gets the single instance of this class.
     *
     *@param  databaseFile  the data file the server is to use.
     *@return               the singleton instance of the services
     *  implementation.
     */
    public static synchronized ServicesImpl getInstance(File databaseFile) {
        if (ref == null) {
            ref = new ServicesImpl(databaseFile);
        }
        return ref;
    }


    /**
     * Returns a clone of this object. Not supported for a singleton
     * object.
     *
     * @throws CloneNotSupportedException   thrown if an attempt is made to
     *      to clone the object.
     * @return                                  a clone of the object.
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
