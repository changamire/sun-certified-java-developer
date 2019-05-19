package suncertify;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.AbstractAction;
import javax.swing.UIManager;

import suncertify.server.Services;
import suncertify.server.ServicesImpl;
import suncertify.server.RemoteServicesImpl;
import suncertify.client.GUI;

/**
 *  This is the Main class for the application. A single commandline flag
 *  indicates whether to run the application as a server, a remote client, or
 *  as a local server and client.<br>
 *  Usage java -jar <path> suncertify.Main [<mode>] where mode may be 'server',
 *  indicating that the application be run in server mode, 'alone', indicating
 *  standalone mode, or left out, indicating network client mode.
 *
 *@author   Gregory Biegel
 *@version  1.0
 */
public class Main {

    /**
     * A reference to the interface to the data store.
     */
    private Services services;
    /**
     * A constant representing the key for host configuration key/value pair.
     */
    private final static String HOST_ENTRY = "Host : ";
    /**
     * A constant representing the key for path configuration key/value pair.
     */
    private final static String PATH_ENTRY = "Path : ";
    /**
     * A constant representing name of the configuration file.
     */
    private final static String CONFIG_FILE_NAME = "suncertify.properties";
    /**
     * A constant representing local server mode.
     */
    private final static String SERVER_MODE_LOCAL = "local";
    /**
     * A constant representing remote server mode.
     */
    private final static String SERVER_MODE_REMOTE = "remote";
    /**
     * A constant representing the port on which the server will run.
     */
    private final static int SERVER_PORT = 1099;


    /**
     * Gets the hostname on which the remote database server resides.
     * This method instantiates a graphical component that allows the
     * user to input the hostname or I.P. address of the host on which
     * the database server resides.
     *
     * @see HostnameInput
     */
    private void getHostname() {
        new HostnameInput();
    }


    /**
     *  Gets the database file from the user via a file chooser component.
     *
     *@return   the database file selected by the user.
     */
    private File getDatabaseFile() {
        File selectedFile = null;
        JFileChooser dbFileChooser = new JFileChooser();
        dbFileChooser.setDialogTitle("Select Database File");
        if (dbFileChooser.showOpenDialog(new JFrame())
                == JFileChooser.APPROVE_OPTION) {
            selectedFile = dbFileChooser.getSelectedFile();
            /* Get the current configuration information. */
            Configuration currentConfig = readConfigurationFile();
            /* Persist the new configuration information. */
            this.writeConfigurationFile(new Configuration(
                    selectedFile.getAbsolutePath(),
                        currentConfig.getHostname()));
        } else {
            System.exit(1);
        }
        return selectedFile;
    }


    /**
     *  Starts the database server, either as a local server, or as an RMI
     *  server that accepts connections from remote clients over a network.
     *
     *@param  mode          the type of server to start. The server may
     *      be started in local mode, or remote mode.
     *@param  databaseFile  the file containing the database records.
     *@return               true if the server was started, else false.
     */
    private boolean startDatabaseServer(String mode, File databaseFile) {
        /* Starts a local (non-networked) server. */
        if (mode.equalsIgnoreCase(SERVER_MODE_LOCAL)) {
            services = ServicesImpl.getInstance(databaseFile);
            return true; 
        }
        /* Starts a server that accepts connections from remote clients. */
        else if (mode.equalsIgnoreCase(SERVER_MODE_REMOTE)) {
            try {
                /* Create an instance of the RMI registry. */
                LocateRegistry.createRegistry(SERVER_PORT);
                services = RemoteServicesImpl.getInstance(databaseFile);
            } catch (RemoteException re) {
                System.err.println("Problem starting database server");
                return false;
            } catch (MalformedURLException mue) {
                System.err.println("Please ensure you have entered a valid "
                        + "hostname or IP address");
                return false;
            }
        }
        return true;
    }


    /**
     *  Starts a network client.
     *
     *@param  hostname  the hostname of the server for this client to use.
     */
    private void startNetworkClient(String hostname) {
        String serverName = "rmi://" + hostname + ":" + SERVER_PORT
            + "/BSServices";
        try {
            services = (Services)(Naming.lookup(serverName));
            new GUI(services);
        } catch (RemoteException e) {
            System.err.println("Problem with remote database server");
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (MalformedURLException mue) {
            System.err.println("Please ensure you have entered a valid "
                    + "hostname or IP address");
            System.exit(1);
        } catch (NotBoundException nbe) {
            System.err.println("Server is not bound in RMI registry");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Problem with remote database server");
            System.exit(1);
        }
    }

    
    /**
     * Reads the configuration information from the configuration file
     * in the working directory.
     *
     *@return   an object representing the configuration information in
     *      the <code>suncertify.properties</code> file.
     */
    private Configuration readConfigurationFile() {
        String databaseFile = null;
        String hostname = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(CONFIG_FILE_NAME));
        } catch (FileNotFoundException fnf) {
            System.err.println("Error opening configuration file");
            System.exit(1);
        }
        try {
            String line = "";
            /* Read in and parse the configuration file. */
            while (line != null) {
                line = br.readLine();
                if (line != null && line.indexOf(PATH_ENTRY) != -1) {
                    databaseFile = line.substring(line.indexOf(PATH_ENTRY)
                            + PATH_ENTRY.length(), line.length());
                } else if (line != null && line.indexOf(HOST_ENTRY) != -1) {
                    hostname = line.substring(line.indexOf(HOST_ENTRY)
                            + HOST_ENTRY.length(), line.length());
                }
            }
        } catch (IOException ioe) {
            System.err.println("Error reading configuration file");
            System.exit(1);
        }
        return new Configuration(databaseFile, hostname);
    }


    /**
     * Writes configuration information to the <code>suncertify.properties
     * </code> file in the current working directory.
     *
     * @param configuration object representing the configuration information.
     */
    private void writeConfigurationFile(Configuration configuration) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(CONFIG_FILE_NAME),
                    false));
            String databaseFile = configuration.getDatabaseFile();
            String hostname = configuration.getHostname();
            if (databaseFile != null) {
                bw.write(PATH_ENTRY + configuration.getDatabaseFile() + "\n");
            } else {
                bw.write(PATH_ENTRY + "\n");
            }
            if (hostname != null) {
                bw.write(HOST_ENTRY + configuration.getHostname() + "\n");
            } else {
                bw.write(HOST_ENTRY + "\n");
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException fnf) {
            System.err.println("Error opening configuration file");
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("Error writing to configuration file");
            System.exit(1);
        }
    }

    /**
     * The main entry point to the application.
     *
     *@param args  the array of runtime arguments.
     */
    public static void main(String[] args) {
        Main main = new Main();
        boolean configFilePresent = false;

        /* Set the look and feel to the system look and feel. */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Problem setting the look and feel to system "
                    + "look and feel");
        }

        /* Try and find an existing configuration file. */
        File configFile = new File(CONFIG_FILE_NAME);
        if (configFile.exists()) {
            configFilePresent = true;
        }
        /* Try to open suncertify.properties file. */
        try {
            new FileInputStream(new File(CONFIG_FILE_NAME));
        } catch (FileNotFoundException fnfe) {
            configFilePresent = false;
        }
        /* There is no existing configuration file, create one. */
        if (!configFilePresent) {
            try {
                configFile.createNewFile();
            } catch (IOException ioe) {
                System.err.println("Error creating configuration file");
                System.exit(1);
            }
        }

        /* Only one runtime argument may be used. */
        if (args.length > 1) {
            System.err.println("Usage: java -jar suncertify.Main [server]");
            System.exit(1);
        }
        else if (args.length == 1) {
            String mode = args[0];
            //Start the server only
            if (mode.equals("server")) {
                //Get location of Database file
                File selectedFile = null;
                if (configFilePresent) {
                    Configuration configuration = main.readConfigurationFile();
                    String databaseFile = configuration.getDatabaseFile();
                    if (databaseFile != null && databaseFile.length() > 1) {
                        int response = JOptionPane.showConfirmDialog(null,
                                "The server is currently configured "
                                + "to use the file " + databaseFile
                                + ", would you like to continue using "
                                + "this file?",
                                "Configuration detected",
                                JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.YES_OPTION) {
                            selectedFile = new File(databaseFile);
                        } else {
                            selectedFile = main.getDatabaseFile();
                        }
                    } else {
                        selectedFile = main.getDatabaseFile();
                    }
                }
                else {
                    selectedFile = main.getDatabaseFile();
                }
                if (main.startDatabaseServer("remote", selectedFile)) {
                    System.out.println("------------------------------");
                    System.out.println("Server running on port " + SERVER_PORT
                            + "...");
                    System.out.println("------------------------------");
                } else {
                    System.err.println("Please ensure the RMI registry "
                            + "is running on port " + SERVER_PORT);
                    System.exit(1);
                }
            }
            else if (mode.equals("alone")) {
                /*Start server and client gui - server must be local. */
                File selectedFile = null;
                if (configFilePresent) {
                    Configuration configuration = main.readConfigurationFile();
                    String databaseFile = configuration.getDatabaseFile();
                    if (databaseFile != null) {
                        int response = JOptionPane.showConfirmDialog(null,
                                "The server is currently configured "
                                + "to use the file " + databaseFile
                                + ", would you like to continue using "
                                + "this file?",
                                "Configuration detected",
                                JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.YES_OPTION) {
                            selectedFile = new File(databaseFile);
                        } else {
                            selectedFile = main.getDatabaseFile();
                        }
                    } else {
                        selectedFile = main.getDatabaseFile();
                    }
                } else {
                    selectedFile = main.getDatabaseFile();
                }
                if (main.startDatabaseServer("local", selectedFile)) {
                    System.out.println("Server running locally, on host");
                    new GUI(main.services);
                }
                else {
                    System.err.println("Problem starting the server");
                    System.exit(1);
                }
            }
        }
        else {
            /* Run network client and gui. */
            if (configFilePresent) {
                Configuration configuration = main.readConfigurationFile();
                String hostname = configuration.getHostname();
                if (hostname != null && hostname.length() > 1) {
                    int response = JOptionPane.showConfirmDialog(null,
                            "The server is currently configured "
                            + "to be on host " + hostname
                            + ", would you like to continue using this host?",
                            "Configuration detected",
                            JOptionPane.YES_NO_OPTION);
                    if (response  == JOptionPane.YES_OPTION) {
                        main.startNetworkClient(hostname);
                    } else {
                        main.getHostname();
                    }
                } else {
                    main.getHostname();
                }
            }
        }
    }


    /**
     *  This class represents a window presented to the user to input the
     *  address of the host of the database server when the application is
     *  running as a network client.
     *
     *@author     Gregory Biegel
     *@version    1.0
     */
    class HostnameInput extends JFrame {
        /**
         * Guarantee a consistent serialVersionUID value across different
         * java compiler implementations.
         */
        static final long serialVersionUID = 1L;
        /**
         * Base panel for the window.
         */
        private JPanel basePanel;
        /**
         * Label naming the hostname input text field.
         */
        private JLabel inputLabel;
        /**
         * Text field for freeform input of hostname.
         */
        private JTextField hostTextField;
        /**
         * Confirmation button.
         */
        private JButton okayButton;
        /**
         * Action associated to confirmation button.
         */
        private AbstractAction okayAction;

        /**
         *  Constructor for the hostname input object.
         */
        public HostnameInput() {
            super("Enter hostname");
            basePanel = new JPanel();
            inputLabel = new JLabel("Enter server hostname ");
            hostTextField = new JTextField("127.0.0.1");

            okayAction =
                new AbstractAction("O.K.") {
                    static final long serialVersionUID = 1L;
                    public void actionPerformed(ActionEvent e) {
                        Configuration currentConfig = readConfigurationFile();
                        writeConfigurationFile(new Configuration(
                                currentConfig.getDatabaseFile(),
                                hostTextField.getText()));
                        startNetworkClient(hostTextField.getText());
                        dispose();
                    }
                };

            okayButton = new JButton(okayAction);
            basePanel.add(inputLabel);
            basePanel.add(hostTextField);
            basePanel.add(okayButton);
            getContentPane().add(basePanel);
            pack();
            /* Position in center of screen. */
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (int) ((d.getWidth() - this.getWidth()) / 2);
            int y = (int) ((d.getHeight() - this.getHeight()) / 2);
            setLocation(x, y);
            setVisible(true);
        }
    }
}
