package suncertify.db;

/**
 *  An interface implemented by classes that provide access to the contractor
 *  data store.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public interface DBAccess {

    /**
     *  Reads a record from the file storing the list of contractors.
     *
     *@param  recNo                     the identifier of the record to read.
     *@return                           an array holding the record fields.
     *@throws  RecordNotFoundException  thrown if the record is not found.
     */
    public String[] readRecord(long recNo)
             throws RecordNotFoundException;


    /**
     *  Modifies the fields of a record. The new value for field n appears in
     *  data[n].
     *
     *@param  recNo                     the identifier of the record to
     *      modify.
     *@param  data                      an array containing the modified
     *      record fields.
     *@param  lockCookie                the cookie with which the record was
     *      locked.
     *@throws  RecordNotFoundException  thrown if the record is not found in the
     *      database.
     *@throws  SecurityException        thrown if the record is locked with
     *      a cookie other than lockCookie.
     */
    public void updateRecord(long recNo, String[] data, long lockCookie)
             throws RecordNotFoundException, SecurityException;


    /**
     *  Deletes a record, making the record number and associated disk storage
     *  available for reuse.
     *
     *@param  recNo                     the identifier of the record to delete.
     *@param  lockCookie                the cookie with which the record
     *      was locked.
     *@throws  RecordNotFoundException  thrown if the record is not found in the
     *      database.
     *@throws  SecurityException        thrown if the record is locked with a
     *      cookie other than lockCookie.
     */
    public void deleteRecord(long recNo, long lockCookie)
             throws RecordNotFoundException, SecurityException;


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
    public long[] findByCriteria(String[] criteria);


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
             throws DuplicateKeyException;


    /**
     *  Locks a record so that it can only be updated or deleted by this
     *  client. Returned value is a cookie that must be used when the record
     *  is unlocked, updated, or deleted. If the specified record is already
     *  locked by a different client, the current thread gives up the CPU and
     *  consumes no CPU cycles until the record is unlocked.
     *
     *@param  recNo                     the identifier of the record to lock.
     *@return                           a cookie value that must be specified
     *      when record is unlocked.
     *@throws  RecordNotFoundException  thrown if the record is not found in the
     *      database.
     */
    public long lockRecord(long recNo)
             throws RecordNotFoundException;


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
             throws SecurityException;
}
