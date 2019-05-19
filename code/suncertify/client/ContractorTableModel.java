package suncertify.client;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import suncertify.db.Contractor;

/**
 * The custom table model used by the <code>GUI</code> class.
 *
 *@author     Gregory Biegel
 *@version    1.0
 *@see        suncertify.client.GUI
 */
public class ContractorTableModel extends AbstractTableModel {
    /**
     * Guarantee a consistent serialVersionUID value across different
     * java compiler implementations.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A constant array containing the column names.
     */
    private static final String[] headerNames = {"Number",
            "Name",
            "Location",
            "Specialities",
            "Size",
            "Rate",
            "Owner"
            };

    /**
     * A list of contractors held in the table model.
     */
    private List contractorRecords = new ArrayList();


    /**
     * Sets the value at a location in the table model.
     *
     *@param  obj     the new value.
     *@param  row     the row coordinate.
     *@param  column  the column coordinate.
     */
    public void setValueAt(Object obj, int row, int column) {
        Object[] temp = (Object[]) this.contractorRecords.get(row);
        temp[column] = obj;
    }


    /**
     * Gets the number of columns in the table model.
     *
     *@return    the number of columns in this table model.
     */
    public int getColumnCount() {
        return headerNames.length;
    }


    /**
     * Gets the name of a particular column.
     *
     *@param  column  the column to get the name of.
     *@return         the column name.
     */
    public String getColumnName(int column) {
        return headerNames[column];
    }


    /**
     * Get the value at a location in the table model.
     *
     *@param  row     the row coordinate.
     *@param  column  the column coordinate.
     *@return         the value at the specified location in the table.
     */
    public Object getValueAt(int row, int column) {
        Object[] temp = (Object[]) this.contractorRecords.get(row);
        return temp[column];
    }


    /**
     * Gets the number of rows in the table model.
     *
     *@return    the number of rows in the table.
     */
    public int getRowCount() {
        return this.contractorRecords.size();
    }


    /**
     * Gets whether a particular cell is editable.
     *
     *@param  row     the row coordinate.
     *@param  column  the column coordinate.
     *@return         whether the cell is editable.
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }


    /**
     * Adds a contractor record to the table model.
     *
     *@param  number        the contractor record number.
     *@param  name          the contractor name.
     *@param  location      the contractor location.
     *@param  specialities  the contractor's specialities.
     *@param  size          the contractor size.
     *@param  rate          the contractor rate.
     *@param  owner         the CSR owning this contractor record.
     */
    public void addContractorRecord(long number, String name, String location,
            String specialities, int size, String rate, String owner) {
        String[] temp = {"" + number, name, location, specialities, ""
                + size, rate, owner};
        this.contractorRecords.add(temp);
    }


    /**
     * Adds a contractor record to the table model.
     *
     *@param  contractor  the contractor to add.
     */
    public void addContractorRecord(Contractor contractor) {
        String[] temp = {"" + contractor.getRecordNo(), contractor.getName(),
                contractor.getLocation(), contractor.getSpecialities(),
                "" + contractor.getSize(), contractor.getRate(), ""
                + contractor.getOwner()};
        this.contractorRecords.add(temp);

    }
}
