package suncertify.db;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;
import java.util.Map;

/**
 *  An implementation of the <code>DBAccess</code> interface providing access
 *  to a database of contractor records. This class is implemented as a
 *  singleton, so there is only ever one instance of the class in the
 *  application.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public final class Data implements DBAccess {

    /**
     *  A reference to the file in which the contractor data is stored.
     */
    private RandomAccessFile dataFile;

    /**
     *  Magic cookie value at start of data file header.
     */
    private int cookie;

    /**
     *  The length of a single record in the file.
     */
    private int recordLength;

    /**
     *  The number of fields in a record.
     */
    private short noOfFields;

    /**
     *  The names of the fields in a record.
     */
    private String[] fieldNames;

    /**
     *  The lengths of the fields in a record.
     */
    private int[] fieldLengths;

    /**
     *  A byte array containing a record read from the file.
     */
    private byte[] record;

    /**
     *  A list of <code>Contractor</code> objects.
     */
    private List contractors = new ArrayList();

    /**
     *  A map of contractors that have been locked for access.
     */
    private Map lockedContractors = new HashMap();

    /**
     *  The current record being processed.
     */
    private long recordNo = 0;

    /**
     *  The length in bytes of the file header information.
     */
    private long schemaLength;

    /**
     *  The path of the database file.
     */
    private String databaseFilePath;

    /**
     *  The single instance of this class.
     */
    private static Data ref;

    /**
     * The magic cookie value identifying this file as a data file.
     */
    private static final int MAGIC_COOKIE = 513;


    /**
     *  Private constructor to enforce singleton.
     *
     *@param  filePath  the path of the database file.
     */
    private Data(String filePath) {
        this.databaseFilePath = filePath;
        this.readFile(databaseFilePath);
    }


    /**
     *  Get the in-memory cache list of contractors.
     *
     *@return    The list of contractor objects.
     */
    public List getContractorList() {
        return contractors;
    }


    /**
     *  Reads a record from the file storing the list of contractors. This
     *  method extracts contractor data from the in-memory list, and places
     *  it in a String array that is returned to the client.
     *
     *@param  recNo                     the identifier of the record to read.
     *@return                           an array holding the record fields.
     *@throws  RecordNotFoundException  thrown if the record is not found.
     */
    public String[] readRecord(long recNo) throws RecordNotFoundException {
        String[] readRecord = new String[7];
        /* Read it from the contractor list and insert into array. */
        int contractorRecord = (int)recNo;
        if (contractorRecord > contractors.size() || contractorRecord < 0) {
            throw new RecordNotFoundException("Could not find the contractor");
        }
        Contractor c = (Contractor)contractors.get((int)recNo);
        readRecord[0] = c.getName();
        readRecord[1] = c.getLocation();
        readRecord[2] = c.getOwner();
        readRecord[3] = c.getSpecialities();
        readRecord[4] = c.getRate();
        readRecord[5] = ""+c.getSize();
        readRecord[6] = ""+c.getDeleted();
        return readRecord;
    }


    /**
     *  Modifies the fields of a record. The new value for field n appears in
     *  data[n]. This method allows the contractor owner to be updated as
     *  necessary when a record is booked / unbooked.
     *
     *@param  recNo                     the identifier of the record to
     *      modify.
     *@param  data                      an array containing the modified
     *      record fields.
     *@param  lockCookie                the cookie with which the record was
     *      locked.
     *@throws  RecordNotFoundException  thrown if the record is not found in the
     *      database.
     *@throws  SecurityException        thrown if the record is locked with a 
     *      cookie other than lockCookie.
     */
    public void updateRecord(long recNo, String[] data, long lockCookie)
             throws RecordNotFoundException, SecurityException {
        long cookie = ((Long)lockedContractors.get(
                new Long(recNo))).longValue();
        /* Ensure this record has been locked by the client. */
        if (cookie == lockCookie) {
            synchronized(contractors) {
                Contractor contractor = (Contractor)contractors.get((int)recNo);
                String previousOwner = contractor.getOwner();
                contractor.setOwner(data[0]);
                try {
                    dataFile = new RandomAccessFile(databaseFilePath, "rw");
                    dataFile.seek((schemaLength
                           + ((recNo) * (recordLength + 1)))
                            + (recordLength - 7));
                    dataFile.write(data[0].getBytes());
                    dataFile.close();
                } catch (Exception e) {
                    contractor.setOwner(previousOwner);
                    throw new RecordNotFoundException(
                            "Could not find the record");
                }
             }
        } else {
            throw new SecurityException(
                    "Attempted to update record with wrong cookie");
        }
    }


    /**
     *  Deletes a record, making the record number and associated disk storage
     *  available for reuse.
     *
     *@param recNo                 the identifier of the record to delete.
     *@param lockCookie            the cookie with which the record was locked.
     *@throws SecurityException    if the record is locked with a cookie 
     *      other than lockCookie.
     *@throws RecordNotFoundException   if the record is not found in the 
     *      database.  
     */
    public void deleteRecord(long recNo, long lockCookie)
             throws RecordNotFoundException, SecurityException {
        long cookie = ((Long) lockedContractors.get(
                new Long(recNo))).longValue();
        /* Ensure this record has been locked by the client. */
        if (cookie == lockCookie) {
            synchronized(contractors) {
                Contractor contractor = (Contractor)contractors.get((int)recNo);
                contractor.setDeleted(true);
                try {
                    dataFile = new RandomAccessFile(databaseFilePath, "rw");
                    dataFile.seek((schemaLength
                            + ((recNo) * (recordLength + 1))));
                    dataFile.write("1".getBytes());
                    dataFile.close();
                } catch (IOException e) {
                    throw new RecordNotFoundException(
                            "Error updating database file : " +e.getMessage());
                }
            }
        } else {
            throw new SecurityException(
                    "Attempted to delete record with wrong cookie");
        }
    }



    /**
     *  Returns an array of record numbers that match the specified criteria.
     *  Field n in the database file is described by criteria[n]. A null value
     *  in criteria[n] matches any field value. A non-null value in
     *  criteria[n] matches any field value that begins with criteria[n]. (For
     *  example, "Fred" matches "Fred" or "Freddy".)
     *
     *@param  criteria  the array of criteria to search by.
     *@return           an array of record numbers that match the specified
     *      criteria.
     */
    public long[] findByCriteria(String[] criteria) {
        long[] results;
        ArrayList values = new ArrayList();
        Pattern namePattern = null;
        Pattern locationPattern = null;
        /** 
         * Compile the regular expression - the term must occur at 
         *  the start of the line. 
         */
        namePattern = Pattern.compile("^" + criteria[1]);
        locationPattern = Pattern.compile("^" + criteria[2]);

        Iterator it = contractors.iterator();
        Contractor contractor = null;
        String contractorName = null;
        String contractorLocation = null;

        /* Iterate though each Contractor in the system and look for matches. */
        while (it.hasNext()) {
            contractor = (Contractor) it.next();
            contractorName =
                contractor.toString().substring(0, 32).toLowerCase();
            contractorLocation =
                contractor.toString().substring(32, 96).toLowerCase();
            Matcher nameMatcher = namePattern.matcher(contractorName);
            Matcher locationMatcher =
                locationPattern.matcher(contractorLocation);
            if (nameMatcher.find() && locationMatcher.find()
                    && !contractor.getDeleted()) {
                values.add(contractor);
            }
        }
        /* Create results array of correct size and populate it. */
        results = new long[values.size()];
        for (int i = 0; i < values.size(); i++) {
            results[i] = ((Contractor) values.get(i)).getRecordNo();
        }
        return results;
    }


    /**
     *  Creates a new record in the database (possibly reusing a deleted
     *  entry). Inserts the given data, and returns the record number of the
     *  new record.
     *
     *@param  data                    the new record to insert.
     *@return                         the identifier of the record inserted.
     *@throws  DuplicateKeyException  thrown if a record already exists with 
     *      this key.
     */
    public long createRecord(String[] data)
             throws DuplicateKeyException {
        int recNo = 0;
        long pointer;
        try {
            dataFile = new RandomAccessFile(databaseFilePath, "rw");
            dataFile.seek(schemaLength);
            while (dataFile.getFilePointer() < dataFile.length()) {
                pointer = dataFile.getFilePointer();
                dataFile.read(record);
                String recStr = new String(record, "US-ASCII");
                /* We can re-use a previously deleted record. */
                if (recStr.substring(0, 1).equals("1")) {
                    dataFile.seek(pointer);
                    dataFile.write(" ".getBytes());
                    dataFile.write(padFieldValue(data[0], 32).getBytes());
                    dataFile.write(padFieldValue(data[1], 64).getBytes());
                    dataFile.write(padFieldValue(data[2], 64).getBytes());
                    dataFile.write(padFieldValue(data[3], 6).getBytes());
                    dataFile.write(padFieldValue(data[4], 8).getBytes());
                    dataFile.write(padFieldValue(data[5], 8).getBytes());
                    dataFile.close();
                    return new Long(recNo).longValue();
                }
                recNo++;
            }
            /* No deleted record to re-use, so create a new one. */
            dataFile.write(" ".getBytes());
            dataFile.write(padFieldValue(data[0], 32).getBytes());
            dataFile.write(padFieldValue(data[1], 64).getBytes());
            dataFile.write(padFieldValue(data[2], 64).getBytes());
            dataFile.write(padFieldValue(data[3], 6).getBytes());
            dataFile.write(padFieldValue(data[4], 8).getBytes());
            dataFile.write(padFieldValue(data[5], 8).getBytes());
            dataFile.close();
            return new Long(recNo).longValue();
        } catch (IOException e) {
            throw new DuplicateKeyException("Error updating record : " +
                    e.getMessage());
        }

    }



    /**
     *  Any potential users of the locking API should invoke
     *  lock/process/unlock as a sequence within the context of a single
     *  method call which guarantees it will happen within a single thread of
     *  execution.
     *
     *@param  recNo                        the identifier of the record to lock.
     *@return                              the cookie the record was
     *      locked with.
     *@exception  RecordNotFoundException  thrown if the record was not found
     *      in the database.
     */
    public long lockRecord(long recNo)
             throws RecordNotFoundException {
        synchronized (lockedContractors) {
            while (lockedContractors.containsKey(new Long(recNo))) {
                try {
                    lockedContractors.wait();
                } catch (InterruptedException e) {}
            }
            long cookie = (long) (Math.random() * Long.MAX_VALUE);
            lockedContractors.put(new Long(recNo), new Long(cookie));
            return cookie;
        }
    }


    /**
     *  Releases the lock on a record. Cookie must be the cookie returned when
     *  the record was locked.
     *
     *@param  recNo               the identifier of the record to unlock.
     *@param  cookie              the cookie this record was locked with.
     *@throws  SecurityException  thrown if the record is locked with a cookie 
     *      other than lockCookie.
     */
    public void unlock(long recNo, long cookie)
             throws SecurityException {
        synchronized (lockedContractors) {
            Long key = new Long(recNo);
            if (!lockedContractors.containsKey(key)) {
                throw new SecurityException("Error unlocking contractor record " 
                        + "- record was locked by a different client"); 
            }
            Long lockCookie = (Long) lockedContractors.get(key);
            if (lockCookie.longValue() == cookie) {
                lockedContractors.remove(key);
                lockedContractors.notifyAll();
            } else {
                throw new SecurityException("Error unlocking the contractor "
                        + "record");
            }
        }
    }


    /**
     *  Pads the value to be inserted into a field to the length of the field,
     *  with spaces. If the value is too long for the field, the value is
     *  shortened to the length of the field.
     *
     *@param  value        the value to be inserted to the field.
     *@param  fieldLength  the requisite length of the field.
     *@return              the value padded to the length of the field.
     */
    private String padFieldValue(String value, int fieldLength) {
        /* If the string is too long, reduce its length. */
        if (value.length() > fieldLength) {
            return value.substring(0, fieldLength + 1);
        }
        else {
            /* Pad string with space characters up to correct length. */
            int padding = fieldLength - value.length();
            for (int i = 0; i < padding; i++) {
                value += " ";
            }
            return value;
        }
    }


    /**
     *  Reads the database file, parsing the header and record structure. A
     *  <code>Contractor</code> object is created to represent each record,
     *  and placed in the list of contractors.
     *
     *@param  filePath  the path of the database file.
     */
    private synchronized void readFile(String filePath) {
        try {
            /* Open the file for read-only access. */
            dataFile = new RandomAccessFile(filePath, "r");
            /* Read in the header information. */
            cookie = dataFile.readInt();
            if (cookie != MAGIC_COOKIE) {
                System.err.println("Error reading database file\n"
                        + "Please ensure it is a valid database file");
                System.exit(1);
            }
            recordLength = dataFile.readInt();
            noOfFields = dataFile.readShort();
            fieldNames = new String[noOfFields];
            fieldLengths = new int[noOfFields];
            record = new byte[recordLength + 1];
            /* Schema description variables. */
            short fieldNameLength;
            String fieldName;
            short fieldLength;
            /* Read in the schema description. */
            for (int i = 0; i < noOfFields; i++) {
                fieldNameLength = dataFile.readShort();
                fieldName = "";
                for (int j = 0; j < fieldNameLength; j++) {
                    fieldName += (char) (dataFile.readByte());
                }
                fieldNames[i] = fieldName;
                fieldLength = dataFile.readShort();
                fieldLengths[i] = fieldLength;
            }
            /* File pointer is now at end of schema. */
            schemaLength = dataFile.getFilePointer();
            /* Read in the record data. */
            while (dataFile.getFilePointer() < dataFile.length()) {
                dataFile.read(record);
                String recStr = new String(record, "US-ASCII");
                /* Create a new contractor object for each record. */
                Contractor contractor = new Contractor(
                        recordNo,
                        recStr.substring(0, 1),
                        recStr.substring(1, 33),
                        recStr.substring(33, 97),
                        recStr.substring(97, 161),
                        Integer.parseInt((recStr.substring(161, 
                                167)).replaceAll("\\s+$", "")),
                        recStr.substring(167, 175),
                        recStr.substring(175, 183)
                        );
                contractors.add(contractor);
                recordNo++;
            }
            /* Close the file. */
            dataFile.close();
        } catch (IOException ioe) {
            System.out.println("Error reading the database file");
        }
    }


    /**
     *  Returns the single Data instance.
     *
     *@param  path  the path of the database file.
     *@return       the Instance value
     */
    public static synchronized Data getInstance(String path) {
        if (ref == null) {
            ref = new Data(path);
        }
        return ref;
    }


    /**
     * Returns a clone of this object. Not supported for a singleton
     * object.
     *
     * @exception CloneNotSupportedException    thrown if an attempt is made to
     *      to clone the object.
     * @return                                  a clone of the object.
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
