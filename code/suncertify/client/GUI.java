package suncertify.client;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JLabel;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import suncertify.server.Services;
import suncertify.db.RecordNotFoundException;

/**
 *  The main client application window for the Bodgitt & Scarper contractor
 *  booking system.
 *
 *@author     Gregory Biegel
 *@version    1.0
 */
public class GUI extends JFrame {
    /**
     * Guarantee a consistent serialVersionUID value across different
     * java compiler implementations.
     */
    static final long serialVersionUID = 1L;
    /**
     *  The <code>JTable</code> displaying the contractor records in the
     *  window.
     */
    private JTable mainTable = new JTable();

    /**
     *  The internal reference to the currently displayed contractor data.
     */
    private ContractorTableModel tableData;

    /**
     *  A reference to the controller with which the view communicates.
     */
    private GUIController controller;

    /**
     *  The text field containg the location search string.
     */
    private JTextField searchLocationTextField = new JTextField(10);

    /**
     *  The text field containing the name search string.
     */
    private JTextField searchNameTextField = new JTextField(10);

    /**
     *  The text field containing the CSR under which to book a specific
     *  contractor.
     */
    private JTextField csrTextField = new JTextField();

    /**
     *  The previous name search string.
     */
    private String previousNameSearchString = "";

    /**
     *  The previous location search string.
     */
    private String previousLocationSearchString = "";


    /**
     *  Constructor for the main user interface window.
     *
     *@param  services  a reference to the server acess interface.
     */
    public GUI(Services services) {
        controller = new GUIController(services);
        /* Window menu bar. */
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(
                new AbstractAction("Quit") {
                    static final long serialVersionUID = 1L;
                    public void actionPerformed(ActionEvent e) {
                        System.exit(-1);
                    }
                }
        );

        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);
        /* The table of contractor data is contained within a scroll pane. */
        JScrollPane tableScroll = new JScrollPane(mainTable);
        tableScroll.setSize(500, 250);
        this.getContentPane().add(tableScroll, BorderLayout.CENTER);
        try {
            tableData = controller.getContractors();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(GUI.this,
                    new java.lang.Object[]{
                    "Problem connecting to database server"
                    },
                    "Server error", JOptionPane.ERROR_MESSAGE);
        }
        setupTable();
        /* The panel holding user controls (i.e. the book button). */
        JPanel controlPanel = new JPanel(new BorderLayout());
        /* The panel containing the search controls. */
        JPanel searchPanel = new JPanel();

        JLabel searchNameLabel = new JLabel("Name");
        searchNameTextField.setToolTipText(
                "Enter information about a contractor name to search for here");
        searchPanel.add(searchNameLabel);
        searchPanel.add(searchNameTextField);

        JLabel searchLocationLabel = new JLabel("Location");
        searchLocationTextField.setToolTipText(
                "Enter information about contractor location to search "
                + "for here");
        searchPanel.add(searchLocationLabel);
        searchPanel.add(searchLocationTextField);

        /* The button used to start a search for a contractor. */
        JButton searchButton = new JButton("Search");
        searchButton.setMnemonic(KeyEvent.VK_S);
        searchButton.setToolTipText(
                "Search contractors with supplied name and location "
                + "parameters");
        searchButton.addActionListener(new SearchContractor());
        searchPanel.add(searchButton);
        JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel csrLabel = new JLabel("Customer ID: ");
        csrTextField.setColumns(8);
        csrTextField.setToolTipText(
                "Enter CSR to book contractor under here");
        bookPanel.add(csrLabel);
        bookPanel.add(csrTextField);

        /* The button used to book a contractor. */
        JButton bookContractorButton = new JButton("Book contractor");
        bookContractorButton.setMnemonic(KeyEvent.VK_B);
        bookContractorButton.setToolTipText("Book the selected contractor");
        bookContractorButton.addActionListener(new BookContractor());
        bookPanel.add(bookContractorButton);

        /* The button used to delete a contractor.. */
        JButton deleteContractorButton = new JButton("Delete contractor");
        deleteContractorButton.addActionListener(new DeleteContractor());
        deleteContractorButton.setToolTipText(
                "Delete selected contractor"
                );
        bookPanel.add(deleteContractorButton);

        controlPanel.add(searchPanel, BorderLayout.NORTH);
        controlPanel.add(bookPanel, BorderLayout.SOUTH);

        this.getContentPane().add(controlPanel, BorderLayout.SOUTH);
        this.pack();
        this.setSize(600, 400);
        this.setTitle("Bodgitt and Scarper LLC. Contractor Booking System");
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((d.getWidth() - this.getWidth()) / 2);
        int y = (int) ((d.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
        this.setVisible(true);
        this.setVisible(true);
    }


    /**
     *  Uses the <code>tableData</code> member to refresh the contents of the
     *  <code>mainTable</code>. The method will attempt to preserve all
     *  previous selections and contents displayed.
     */
    private void setupTable() {
        String prevSelected = "";
        /* Preserve the previous selection. */
        int index = mainTable.getSelectedRow();
        if (index >= 0) {
            prevSelected = (String) mainTable.getValueAt(index, 0);
        }

        /* Reset the table data. */
        this.mainTable.setModel(this.tableData);

        /* Reselect the previous item if it still exists. */
        for (int i = 0; i < this.mainTable.getRowCount(); i++) {
            String selectedContractor = (String) mainTable.getValueAt(i, 0);
            if (selectedContractor.equals(prevSelected)) {
                this.mainTable.setRowSelectionInterval(i, i);
                break;
            }
        }
    }


    /**
     *  Refreshes the table model by getting the in-memory cache of contractor
     *  records from the server.
     */
    private void refreshTableModel() {
        try {
            tableData = controller.getContractors();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(GUI.this,
                    new java.lang.Object[]{
                    "Problem connecting to database server"
                    },
                    "Server error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
        }
    }


    /**
     *  This class represents the action taken when the search button is
     *  pressed.
     *
     *@author     Gregory Biegel
     *@version    1.0
     */
    private class SearchContractor implements ActionListener {
        /**
         *  The search action, invoked by the user pressing the 'Search'
         *  button on the user interface.
         *
         *@param e  the event generated by the user pressing the search
         *          button.
         */
        public void actionPerformed(ActionEvent e) {
            previousNameSearchString = searchNameTextField.getText();
            previousLocationSearchString = searchLocationTextField.getText();
            searchNameTextField.setText("");
            searchLocationTextField.setText("");
            try {
                tableData = controller.find(previousNameSearchString,
                        previousLocationSearchString);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Problem connecting to database server"
                        },
                        "Server error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            setupTable();
        }
    }



    /**
     *  This class represents the action taken when the book button is
     *  pressed.
     *
     *@author     Gregory Biegel
     *@version    1.0
     */
    private class DeleteContractor implements ActionListener {

        /**
         *  The delete action, invoked by the user pressing the 'Delete'
         *  button on the user interface.
         *
         *@param e  the event generated by the user pressing the delete
         *          button.
         */
        public void actionPerformed(ActionEvent e) {
            int editingRow = mainTable.getSelectedRow();
            if (editingRow == -1) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Please select a contractor to delete"
                        },
                        "Delete contractor error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Object o = mainTable.getModel().getValueAt(editingRow, 0);
            int recordNo = new Integer("" + o).intValue();

            try {
                controller.delete(recordNo);
            } catch (RecordNotFoundException rnf) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Contractor record could not be found"
                        },
                        "Delete contractor error", JOptionPane.ERROR_MESSAGE);
            } catch (SecurityException se) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Contractor record locked by somebody else"
                        },
                        "Delete contractor error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Problem connecting to database server"
                        },
                        "Server error", JOptionPane.ERROR_MESSAGE);
            } finally {
                refreshTableModel();
                setupTable();
            }
        }
    }


    /**
     *  This class represents the action taken when the book button is
     *  pressed.
     *
     *@author     Gregory Biegel
     *@version    1.0
     */
    private class BookContractor implements ActionListener {
        /**
         *  The book contractor action, invoked by the user pressing the
         *  'Book' button on the user interface.
         *
         *@param  e  the event generated by the user pressing the book button.
         */
        public void actionPerformed(ActionEvent e) {
            int editingRow = mainTable.getSelectedRow();
            if (editingRow == -1) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Please select a contractor to book"
                        },
                        "Book contractor error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Object o = mainTable.getModel().getValueAt(editingRow, 0);
            int recordNo = new Integer("" + o).intValue();

            long customerID = 0;
            if (csrTextField.getText().length() == 8) {
                try {
                    customerID = (new Long(csrTextField.getText())).longValue();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(GUI.this,
                            new java.lang.Object[]{
                            "8-digit CSR no. should have no letters"
                            },
                            "Book contractor error", JOptionPane.ERROR_MESSAGE);
                    csrTextField.setText("");
                    csrTextField.requestFocus();
                    return;
                }

            }
            else {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Please enter an 8-digit CSR no"
                        },
                        "Book contractor error", JOptionPane.ERROR_MESSAGE);
                csrTextField.setText("");
                return;
            }
            try {
                boolean b = controller.book(recordNo, customerID);
                if (!b) {
                    JOptionPane.showMessageDialog(GUI.this,
                            new java.lang.Object[]{
                            "Contractor already booked"
                            },
                            "Book contractor error", JOptionPane.ERROR_MESSAGE); 
                }
            } catch (RecordNotFoundException rnf) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Contractor record could not be found"
                        },
                        "Book contractor error", JOptionPane.ERROR_MESSAGE);
            } catch (SecurityException se) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Contractor record locked by somebody else"
                        },
                        "Book contractor error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(GUI.this,
                        new java.lang.Object[]{
                        "Problem connecting to database server"
                        },
                        "Server error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
            } finally {
                refreshTableModel();
                setupTable();
            }
        }
    }
}
