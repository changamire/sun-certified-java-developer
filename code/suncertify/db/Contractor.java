package suncertify.db;

import java.io.Serializable;

/**
 *  A contractor object represents a contractor object in the database file.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public class Contractor implements Serializable {
	
    /**
     * Guarantee a consistent serialVersionUID value across different
     * java compiler implementations.
     */
    static final long serialVersionUID = 1L;
    
    /**
     *  The unique identifier of the contractor.
     */
    private long recordNo;

    /**
     *  A flag indicating whether the contractor has been deleted.
     */
    private boolean deleted;

    /**
     *  The name of the contractor.
     */
    private String name;

    /**
     *  The location of the contractor.
     */
    private String location;

    /**
     *  The specialities of the contractor.
     */
    private String specialities;

    /**
     *  The size (no of employees) of the contractor.
     */
    private int size;

    /**
     *  The contractors rate.
     */
    private String rate;

    /**
     *  The id of the customer currently owning this contractor record.
     */
    private String owner;


    /**
     *  Constructor for the contractor object.
     *
     *@param  recordNo      the contractor record number.
     *@param  deleted       flag indicating whether this record has been
     *      deleted.
     *@param  name          the contractor name.
     *@param  location      the contractor location.
     *@param  specialities  the contractor's specialities.
     *@param  size          the contractor size.
     *@param  rate          the contractor rate.
     *@param  owner         the CSR owning this contractor record.
     */
    public Contractor(long recordNo, String deleted, String name, String location,
            String specialities, int size, String rate, String owner) {
        this.recordNo = recordNo;
        if (deleted.equals("1")) {
            this.deleted = true;
        }
        else {
            this.deleted = false;
        }
        this.name = name;
        this.location = location;
        this.specialities = specialities;
        this.size = size;
        this.rate = rate;
        this.owner = owner;
    }


    /**
     *  Sets the owner of this contractor.
     *
     *@param  newOwner  the new owner of this contractor.
     */
    public void setOwner(String newOwner) {
        this.owner = newOwner;
    }


    /**
     *  Sets the deleted flag of this contractor.
     *
     *@param  deleted  the new value of the deleted flag for this contractor.
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }


    /**
     *  Gets the contractor record number.
     *
     *@return   the contractor record number.
     */
    public long getRecordNo() {
        return recordNo;
    }


    /**
     *  Gets whether this contractor record is marked as deleted.
     *
     *@return    the deleted flag.
     */
    public boolean getDeleted() {
        return this.deleted;
    }


    /**
     *  Gets the contractor name.
     *
     *@return    the contractor name.
     */
    public String getName() {
        return this.name;
    }


    /**
     *  Gets the contractor location.
     *
     *@return    the contractor location.
     */
    public String getLocation() {
        return this.location;
    }


    /**
     *  Gets the contractor specialities.
     *
     *@return    the contractor specialities.
     */
    public String getSpecialities() {
        return this.specialities;
    }


    /**
     *  Gets the number of people employed by the contractor.
     *
     *@return    the contractor size.
     */
    public int getSize() {
        return this.size;
    }


    /**
     *  Gets the hourly rate of the contractor.
     *
     *@return    the contractor rate.
     */
    public String getRate() {
        return this.rate;
    }


    /**
     *  Gets the owner of this contractor.
     *
     *@return    the owner of this contractor.
     */
    public String getOwner() {
        return this.owner;
    }


    /**
     *  Converts the contractor to a string representation.
     *
     *@return    a string representation of the contractor object.
     */
    public String toString() {
        return name + location + specialities + size + rate + owner;
    }

}
