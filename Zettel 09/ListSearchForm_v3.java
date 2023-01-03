package org.mhisoft.wallet.view;

import org.mhisoft.wallet.model.WalletItem;
import org.mhisoft.wallet.model.WalletModel;
import org.mhisoft.wallet.service.ServiceRegistry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;


public class ListSearchForm extends JDialog {

    private JPanel contentPane;
    private JTextField textFieldSearchField;
    private JButton buttonSearch;
    private JList<WalletItem> resultsJList;
    private JButton buttonGoTo;
    private JButton buttonCancel;
    private JLabel labelSearchResults;
    private JScrollPane scrollPaneSearchResults;
    private WalletItem selectedItem;
    private final WalletModel walletModel;
    private final DefaultListModel<WalletItem> defaultListModel;
    private final ItemDetailView itemDetailView;

    private final TreeExploreView treeExploreView;


    //Außerdem Änderung in WalletForm, beim Filter Button hinzufügen
    public ListSearchForm() {
        setContentPane(contentPane);
        setModal(true);

        //Get References to other classes
        //Get WalletModel
        walletModel = ServiceRegistry.instance.getWalletModel();
        //Get ItemDetailView
        itemDetailView = new ItemDetailView(walletModel, ServiceRegistry.instance.getWalletForm());

        treeExploreView = ServiceRegistry.instance.getWalletForm().getTreeExploreView();

        //Set up objects
        defaultListModel = new DefaultListModel<>();
        buttonSearch.setEnabled(false);
        buttonGoTo.setEnabled(false);

        filterListOfElements("");
        showFilteredElements();

        //Add textField Search Functionality
        createTextFieldSearchEvents();

        //Add Search Button Functionality
        createButtonSearchEvents();

        //Add GoTo Button Functionality
        createButtonGoToEvents();

        //Add Cancel Functionality
        createCancelMethods();

    }


    //Show the selected item in detail view
    private void createButtonGoToEvents() {
        buttonGoTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Display Details in WalletForm
                itemDetailView.displayWalletItemDetails(selectedItem);
                //set selected item in WalletModel
                walletModel.setCurrentItem(selectedItem);
                //set selected item in the tree view
                treeExploreView.setSelectionToCurrentNode();

                //Close the list Search form
                onCancel();
            }
        });
    }


    //filter the items based on search input and show them in the scroll panel
    private void createButtonSearchEvents() {
        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //if the textField isn't empty, start searching
                if (!textFieldSearchField.getText().matches("")) {
                    String filter = textFieldSearchField.getText();
                    filterListOfElements(filter);
                    showFilteredElements();
                }
            }
        });
    }

    //add Key Listeners to enable or disable the search button
    private void createTextFieldSearchEvents() {
        //Set Button based on Text input

        textFieldSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setSearchButton();
                resultsJList.clearSelection();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setSearchButton();
                resultsJList.clearSelection();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setSearchButton();
                resultsJList.clearSelection();
            }

            private void setSearchButton(){
                buttonSearch.setEnabled(!textFieldSearchField.getText().matches(""));
            }
        });
    }

    //Add items that match the filter to the default list model
    private void filterListOfElements(String filter){
        defaultListModel.clear();
        walletModel.getItemsFlatList().forEach(item -> {
            if (filter == null || item.isMatch(filter)) {
                defaultListModel.addElement(item);
            }
        });
    }


    //create a new JList based on DefaultListModel and add it to the scroll panel
    private void showFilteredElements() {
        //Set up the JList
        resultsJList = new JList<>(defaultListModel);
        resultsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Show JList in the scrollPane
        scrollPaneSearchResults.setColumnHeaderView(labelSearchResults);
        scrollPaneSearchResults.setViewportView(resultsJList);

        //Get Selection Model to add a SelectionListened
        ListSelectionModel listSelectionModel = resultsJList.getSelectionModel();

        addSelectionModelListeners(listSelectionModel);

    }

    private void addSelectionModelListeners(ListSelectionModel listSelectionModel) {
        listSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectedItem = resultsJList.getSelectedValue();
                buttonGoTo.setEnabled(selectedItem != null);

            }
        });
    }


    //Add different ways to cancel the filter function
    private void createCancelMethods() {

        //call onCancel() when cancel button is clicked
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        ListSearchForm dialog = new ListSearchForm();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
