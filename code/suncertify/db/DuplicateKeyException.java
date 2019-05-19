package suncertify.db;

/**
 *  Holds an exception thrown when an attempt is made to insert a duplicate
 *  key into the database.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public class DuplicateKeyException extends Throwable {
    static final long serialVersionUID = 1L;
	
    /* Description of the exception. */
    private String message;
    
    /**
     *  Constructor for the DuplicateKeyException object.
     */
    public DuplicateKeyException() {
    }


    /**
     *  Constructor for the DuplicateKeyException object.
     *
     *@param  message  the message associated with the exception.
     */
    public DuplicateKeyException(String message) {
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
