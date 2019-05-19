package suncertify.server;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import suncertify.db.RecordNotFoundException;

/**
 *  An implementation of the <code>RemoteServices</code> interface,allowing
 *  clients to access a database server on a remote host, through RMI.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public final class RemoteServicesImpl extends UnicastRemoteObject
         implements RemoteServices {

    /**
     * Guarantee a consistent serialVersionUID value across different
     * java compiler implementations.
     */
    static final long serialVersionUID = 1L;

    /**
     *  A reference to the services implementation.
     */
    private Services services;

    /**
     *  A reference to the single instance of this class.
     */
    private static RemoteServicesImpl ref;


    /**
     *  Constructor for the RemoteServicesImpl object.
     *
     *@param  databaseFile               the database file
     *@exception  RemoteException        thrown if an exception occurs in the
     *      <code>RemoteServicesImpl</code> class
     *@exception  MalformedURLException  thrown if the URL is badly formed
     */
    private RemoteServicesImpl(File databaseFile) throws MalformedURLException, 
            RemoteException {
        services = ServicesImpl.getInstance(databaseFile);
        try {
            /* Bind the server object to the RMI registry. */
            Naming.rebind("BSServices", this);
        } catch (RemoteException re) {
            throw re;
        } catch (MalformedURLException mue) {
            throw mue;
        }
    }


    /**
     *  Gets the list of contractors.
     *
     *@return                      the list of contractors in the database
     *@exception  RemoteException  thrown if an exception occurs in the
     *      <code>RemoteServicesImpl</code> class
     *@exception  IOException      thrown if there is a problem accessing the
     *      database file
     */
    public List getContractors() throws IOException, RemoteException {
        return services.getContractors();
    }


    /**
     *  Books a contractor in the database.
     *
     *@param  id                           the identifier of the contractor to
     *      book
     *@param  customerID                   the CSR under which to book the
     *      contractor
     *@return                              true if the contractor was booked,
     *      else false
     *@exception  RemoteException          thrown if an exception occurs in
     *      the <code>RemoteServicesImpl</code> class
     *@exception  RecordNotFoundException  thrown if the contractor was not
     *      found
     *@exception  IOException              thrown if there is a problem
     *      accessing the database file
     *@exception  SecurityException        thrown if record is locked by
     *      another user
     */
    public boolean book(int id, long customerID)
             throws IOException, RecordNotFoundException, RemoteException, 
             SecurityException {
        return services.book(id, customerID);
    }

    
    /**
     *  Unbooks a contractor in the database.
     *
     *@param  id                           the identifier of the contractor to
     *      unbook
     *@return                              true if the contractor was unbooked,
     *      else false
     *@exception  RemoteException          thrown if an exception occurs in
     *      the <code>RemoteServicesImpl</code> class
     *@exception  RecordNotFoundException  thrown if the contractor was not
     *      found
     *@exception  IOException              thrown if there is a problem
     *      accessing the database file
     *@exception  SecurityException        thrown if record is locked by
     *      another user
     */
    public boolean unBook(int id)
             throws IOException, RecordNotFoundException, RemoteException, 
             SecurityException {
        return services.unBook(id);
    }
    
    
    /**
     *  Finds contractors that match the specified search criteria.
     *
     *@param  name                 the name to search for
     *@param  location             the location to search for
     *@return                      the list of contractors which match the
     *      search criteria
     *@exception  RemoteException  thrown if an exception occurs in the
     *      <code>RemoteServicesImpl</code> class
     *@exception  IOException      thrown if there is a problem accessing the 
     *      database file
     */
    public List find(String name, String location) throws IOException, 
            RemoteException {
        return services.find(name, location);
    }


    /**
     *  Deletes a contractor from the database.
     *
     *@param  id                            the identifier of the contractor to
     *      delete
     *@exception  IOException               thrown if there is a problem
     *      accessing the database file
     *@exception  RecordNotFoundException   thrown if the contractor is not
     *      found to delete
     *@exception  SecurityException         thrown if record is locked by
     *      another user
     *@exception  RemoteException           thrown if an exception occurs in the
     *      <code>RemoteServicesImpl</code> class
     */
    public void deleteContractor(int id)
             throws IOException, RecordNotFoundException, RemoteException, SecurityException {
        services.deleteContractor(id);
    }


    /**
     *  Adds a contractor to the database.
     *
     *@param  data                 an array containing the contractor record
     *      data to add
     *@exception  RemoteException  thrown if an exception occurs in the
     *      <code>RemoteServicesImpl</code> class
     *@exception  IOException      thrown if there is a problem accessing the
     *      database file
     */
    public void addContractor(String[] data) throws IOException, RemoteException {
        services.addContractor(data);
    }


    /**
     *  Gets the single instance of the remote server.
     *
     *@param  databaseFile               the database file
     *@return                            the remote server object
     *@exception  RemoteException        thrown if an exception occurs in the
     *      <code>RemoteServicesImpl</code> class
     *@exception  MalformedURLException  thrown if the URL is badly formed
     */
    public static synchronized RemoteServicesImpl getInstance(File databaseFile)
             throws MalformedURLException, RemoteException {
        if (ref == null) {
            try {
                ref = new RemoteServicesImpl(databaseFile);
            } catch (RemoteException re) {
                throw re;
            } catch (MalformedURLException mue) {
                throw mue;
            }
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
