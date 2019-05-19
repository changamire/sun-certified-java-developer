package suncertify.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import suncertify.db.Contractor;
import suncertify.server.Services;
import suncertify.db.RecordNotFoundException;

/**
 * This class forms the controller between the data store (the model) and the
 * client window (the view).
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public class GUIController {

    /**
     * Internal reference to the interface to the data store.
     */
    private Services services;


    /**
     * Constructor for the controller object.
     *
     *@param  services  the data store interface.
     */
    public GUIController(Services services) {
        this.services = services;
    }


    /**
     * Returns the list of contractors in the data store formatted as a
     * <code>ContractorTableModel</code> .
     *
     *@return                  the contractor table model.
     *@exception  IOException  thrown if there is a problem
     *      accessing the database file
     */
    public ContractorTableModel getContractors() throws IOException {
        ContractorTableModel out = new ContractorTableModel();
        List contractorArray = new ArrayList();
        contractorArray = (ArrayList) services.getContractors();
        Iterator it = contractorArray.iterator();
        while (it.hasNext()) {
            Contractor contractor = (Contractor) it.next();
            if (!contractor.getDeleted()) {
                out.addContractorRecord(contractor);
            }
        }
        return out;
    }


    /**
     * Searches for contractors that match the specified name and location
     * criteria, returning the results formatted as a table model.
     *
     *@param  name              the name search string.
     *@param  location          the location search string.
     *@return                   the matching contractors formatted as a table
     *      model.
     *@exception  IOException   thrown if there is a problem
     *      accessing the database file
     */
    public ContractorTableModel find(String name,
            String location) throws IOException {
        ContractorTableModel out = new ContractorTableModel();
        List records = (ArrayList) services.find(name, location);
        Iterator it = records.iterator();
        while (it.hasNext()) {
            out.addContractorRecord((Contractor) it.next());
        }
        return out;
    }


    /**
     * Books a specific contractor in the database.
     *
     *@param  id                            the identifier of the contractor to
     *      book.
     *@param  customerID                    the CSR against which to book the 
     *      booking.
     *@return                               whether the booking was successful.
     *@exception  RecordNotFoundException   thrown if the record is not found
     *@exception  SecurityException         thrown if record is locked by
     *      another user
     *@exception  IOException               thrown if there is a problem
     *      accessing the database file
     */
    public boolean book(int id, long customerID) throws
            IOException, RecordNotFoundException, SecurityException {
        return services.book(id, customerID);
    }

    /**
     * Deletes a contractor from the database.
     *
     *@param  id                            the identifier of the contractor to
     *      delete
     *@exception  IOException               thrown if there is a problem
     *      accessing the database file
     *@exception  RecordNotFoundException   thrown if the record is not found
     *@exception  SecurityException         thrown if record is locked by
     *      another user
     */
    public void delete(int id) throws
            IOException, RecordNotFoundException, SecurityException {
        services.deleteContractor(id);
    }
}
