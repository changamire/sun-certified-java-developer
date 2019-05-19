package suncertify.server;

import java.io.IOException;
import java.util.List;
import suncertify.db.RecordNotFoundException;

/**
 *  This interface is the primary API through which clients access a local
 *  database server.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public interface Services {

    /**
     *  Books a contractor in the database.
     *
     *@param  id                           the identifier of the contractor to
     *      book
     *@param  customerID                   the CSR under which to book the
     *      contractor
     *@return                              true if the contractor was booked,
     *      else false
     *@exception  RecordNotFoundException  thrown if the contractor was not
     *      found
     *@exception  IOException              thrown if there is a problem
     *      accessing the database file
     *@exception  SecurityException        thrown if the record is locked by
     *      another user
     */
    public boolean book(int id, long customerID) throws IOException,
            RecordNotFoundException, SecurityException;

    /**
     *  Unbooks a contractor in the database.
     *
     *@param  id                           the identifier of the contractor to
     *      unbook
     *@return                              true if the contractor was unbooked,
     *      else false
     *@exception  RecordNotFoundException  thrown if the contractor was not
     *      found
     *@exception  IOException              thrown if there is a problem
     *      accessing the database file
     *@exception  SecurityException        thrown if the record is locked by
     *      another user
     */
    public boolean unBook(int id) throws IOException,
            RecordNotFoundException, SecurityException;
    
    /**
     *  Finds contractors that match the specified search criteria.
     *
     *@param  name             the name to search for
     *@param  location         the location to search for
     *@return                  the list of contractors which match the search
     *      criteria
     *@exception  IOException  thrown if there is a problem accessing the
     *      database file
     */
    public List find(String name, String location) throws IOException;


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
             throws IOException, RecordNotFoundException, SecurityException;


    /**
     *  Adds a contractor to the database.
     *
     *@param  data             an array containing the contractor record data
     *      to add
     *@exception  IOException  thrown if there is a problem accessing the
     *      database file
     */
    public void addContractor(String[] data) throws IOException;



    /**
     *  Gets the list of contractors.
     *
     *@return                  the list of contractors in the database
     *@exception  IOException  thrown if there is a problem accessing the
     *      database file
     */
    public List getContractors() throws IOException;
}
