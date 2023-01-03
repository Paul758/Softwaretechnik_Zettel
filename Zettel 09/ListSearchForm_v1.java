package org.mhisoft.wallet.view;

import org.mhisoft.wallet.WalletMain;
import org.mhisoft.wallet.model.ItemType;
import org.mhisoft.wallet.model.WalletItem;
import org.mhisoft.wallet.model.WalletModel;
import org.mhisoft.wallet.service.ServiceRegistry;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ListSearchForm extends JDialog {
    private JPanel contentPane;
    private JTextField textFieldSearchField;
    private JButton buttonSearch;
    private JList listResultList;
    private JButton buttonGoTo;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel labelSearchResults;
    private JScrollPane scrollPaneSearchResults;

    private WalletItem searchValue;

    private List<WalletItem> itemsFlatList;
    private List<String> walletItemsNameList;

    private WalletModel walletModel;

    private DefaultListModel<WalletItem> listModel;

    private ItemDetailView itemDetailView;

    public ListSearchForm() {
        setContentPane(contentPane);
        setModal(true);

        //Get WalletModel
        walletModel = ServiceRegistry.instance.getWalletModel();

        //Get ItemDetailView
        itemDetailView = new ItemDetailView(walletModel, ServiceRegistry.instance.getWalletForm());

        //Get items from WalletModel
        itemsFlatList = walletModel.getItemsFlatList();

        //Add Items to a string list, needed for search results
        /*for(WalletItem item : itemsFlatList){
            walletItemsNameList.add(item.getName());
        }*/

        //filter items

        //Set up List and Scroll panel
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("a");
        stringList.add("ab");
        stringList.add("abc");
        stringList.add("abcd");
        stringList.add("abcde");
        stringList.add("abcdef");

        //Create some WalletItems
        WalletItem test = new WalletItem(ItemType.category,"Bank");
        WalletItem test2 = new WalletItem(ItemType.item,"Uni");
        WalletItem test3 = new WalletItem(ItemType.item,"Daten");


        listModel = new DefaultListModel<>();
       // listModel.addElement(test);
        //listModel.addElement(test2);
       // listModel.addElement(test3);

       /* for(String s: stringList){
            listModel.addElement(s);
        }*/
        /*listResultList = new JList<>(listModel);
        listResultList.setVisibleRowCount(3);


        scrollPaneSearchResults.setColumnHeaderView(labelSearchResults);
        listResultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPaneSearchResults.setViewportView(listResultList);*/





        //Set Button based on Text input
        textFieldSearchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                setSearchButton();
            }

            @Override
            public void keyPressed(KeyEvent e) {
               setSearchButton();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                setSearchButton();
            }

            private void setSearchButton(){
                buttonSearch.setEnabled(!textFieldSearchField.getText().matches(""));
            }
        });


        //Add Search button Functionality
        textFieldSearchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textFieldSearchField.getText().matches("")){
                    buttonSearch.setEnabled(false);
                } else {
                    buttonSearch.setEnabled(true);
                }
            }
        });



        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textFieldSearchField.getText().matches("")){
                    return;
                } else {
                    searchElements(textFieldSearchField.getText());
                    showElements();
                }
            }
        });

        //Add GoTo Functionality
        buttonGoTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemDetailView.displayWalletItemDetails(searchValue);
                onCancel();
            }
        });


        //Add Cancel Functionality
        createCancelMethods();

    }



    private void searchElements(String searchString){
        listModel.clear();
        walletModel.getItemsFlatList().forEach(item -> {
            if (searchString==null || item.isMatch(searchString)) {
                listModel.addElement(item);
            }
        });
    }

    private void showElements() {
        listResultList = new JList<>(listModel);
        listResultList.setVisibleRowCount(3);


        scrollPaneSearchResults.setColumnHeaderView(labelSearchResults);
        listResultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPaneSearchResults.setViewportView(listResultList);

        ListSelectionModel listSelectionModel = listResultList.getSelectionModel();


        listSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                searchValue = (WalletItem) listResultList.getSelectedValue();
                System.out.println(searchValue);
            }
        });

    }

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
