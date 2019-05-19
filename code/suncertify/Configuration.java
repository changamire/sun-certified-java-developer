package suncertify;

/**
 * This class represents the persistent application configuration
 * in an object-oriented manner. The configuration key/vale pairs
 * of hostname and database file path are stored as string variables
 * within the class.
 *
 *@author Gregory Biegel
 *@version 1.0
 */
public class Configuration {
    /**
     * The path of the configured database file.
     */
    private String databaseFile;
    /**
     * The configured hostname of the database server.
     */
    private String hostname;

    /**
     * Class constructor specifying the database file and hostname.
     *
     * @param databaseFile  the configured database file.
     * @param hostname      the configured hostname.
     */
    public Configuration(String databaseFile, String hostname) {
        this.databaseFile = databaseFile;
        this.hostname = hostname;
    }

    /**
     * Gets the value of the configured database file.
     *
     * @return  the path to the database file configured to be used.
     */
    public String getDatabaseFile() {
        return databaseFile;
    }

    /**
     * Gets the value of the hostname on which the database server
     * is configured to run.
     *
     * @return  the hostname of the configured database server.
     */
    public String getHostname() {
        return hostname;
    }
}
