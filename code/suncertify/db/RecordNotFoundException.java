package suncertify.db;

/**
 *  Holds an exception thrown when a record may not be found in the database.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public class RecordNotFoundException extends Exception {
    static final long serialVersionUID = 1L;

    private String message;


    /**
     *  Constructor for the RecordNotFoundException object.
     */
    public RecordNotFoundException() {
    }


    /**
     *  Constructor for the RecordNotFoundException object.
     *
     *@param  message  the message associated with the exception.
     */
    public RecordNotFoundException(String message) {
        this.message = message;
    }


    /**
     *  Returns the informational message associated with the exception.
     *
     *@return    the message associated with the exception.
     */
    public String getMessage() {
        return message;
    }

}
