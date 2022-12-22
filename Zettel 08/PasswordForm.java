/*
 *
 *  * Copyright (c) 2014- MHISoft LLC and/or its affiliates. All rights reserved.
 *  * Licensed to MHISoft LLC under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. MHISoft LLC licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.mhisoft.wallet.view;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.*;
import javax.swing.text.*;

import org.mhisoft.wallet.SystemSettings;
import org.mhisoft.wallet.action.ActionResult;
import org.mhisoft.wallet.action.CreateWalletAction;
import org.mhisoft.wallet.action.LoadWalletAction;
import org.mhisoft.wallet.action.VerifyPasswordAction;
import org.mhisoft.wallet.model.PassCombinationEncryptionAdaptor;
import org.mhisoft.wallet.model.PassCombinationVO;
import org.mhisoft.wallet.model.PasswordValidator;
import org.mhisoft.wallet.model.WalletModel;
import org.mhisoft.wallet.service.BeanType;
import org.mhisoft.wallet.service.ServiceRegistry;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
@SuppressWarnings("rawtypes")
public class PasswordForm implements ActionListener {
	private JPanel mainPanel;
	private JPasswordField fldPassword;
	private JButton btnCancel;
	private JButton btnOk;
	private JLabel labelPassword;
	private JLabel labelSafeCombination;
	private JLabel labelInst1;
	private JLabel labelInst2;
	private JLabel labelInst3;
	private JLabel labelMsg;
	private JButton button1;

	private JTextField textField1;
	private JTextField textField2;
	private JTextField textField3;

	private MyDocumentFilter docFilter1;
	private MyDocumentFilter docFilter2;
	private MyDocumentFilter docFilter3;

	JDialog dialog;

	String title;

	WalletForm walletForm;

	List<Component> componentsList = new ArrayList<>();


	PasswordValidator passwordValidator = ServiceRegistry.instance.getService(BeanType.singleton, PasswordValidator.class);

	transient Item spinner1Item, spinner2Item, spinner3Item;



	public PasswordForm(String title) {
		passwordValidator = new PasswordValidator();
		this.title = title;
		init();

	}


	class Item {
		Integer value;
		String text;

		public Item(Integer value, String text) {
			this.value = value;
			this.text = text;
		}

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return getText();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Item item = (Item) o;
			return Objects.equals(value, item.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}


	/* place custom component creation code here*/
	private void createUIComponents() {
		//Set up the textFields

		JTextField[] textFields = {textField1,textField2,textField3};
		//Loop over every textField to add Listeners
		for(JTextField textField : textFields){
			//Setting up the visuals of the component
			textField.setFont(new Font("SansSerif",Font.BOLD, 20));
			textField.setHorizontalAlignment(JTextField.CENTER);

			//Adding Focus- and MouseWheelListeners to the textField
			textField.addFocusListener(new MyFocusAdapter(textField));
			textField.addMouseWheelListener(new MyMouseWheelListener(textField));

			//write stars in every textField
			textField.setText("*");
		}

		//Get the textField documents
		PlainDocument document1 = (PlainDocument) textField1.getDocument();
		PlainDocument document2 = (PlainDocument) textField2.getDocument();
		PlainDocument document3 = (PlainDocument) textField3.getDocument();

		//Initialize the document filters
		docFilter1 = new MyDocumentFilter(textField1);
		docFilter2 = new MyDocumentFilter(textField2);
		docFilter3 = new MyDocumentFilter(textField3);

		//Set the document filters
		document1.setDocumentFilter(docFilter1);
		document2.setDocumentFilter(docFilter2);
		document3.setDocumentFilter(docFilter3);

		//Initialize textFields with value 0
		spinner1Item = new Item(0, "0");
		spinner2Item = new Item(0, "0");
		spinner3Item = new Item(0, "0");
	}

	//never used
	private class IndexedFocusTraversalPolicy extends
			FocusTraversalPolicy {

		private ArrayList<Component> components =
				new ArrayList<Component>();

		public void addIndexedComponent(Component component) {
			components.add(component);
		}

		@Override
		public Component getComponentAfter(Container aContainer,
				Component aComponent) {
			int atIndex = components.indexOf(aComponent);
			int nextIndex = (atIndex + 1) % components.size();
			return components.get(nextIndex);
		}

		@Override
		public Component getComponentBefore(Container aContainer,
				Component aComponent) {
			int atIndex = components.indexOf(aComponent);
			int nextIndex = (atIndex + components.size() - 1) %
					components.size();
			return components.get(nextIndex);
		}

		@Override
		public Component getFirstComponent(Container aContainer) {
			return components.get(0);
		}

		@Override
		public Component getLastComponent(Container aContainer) {
			return components.get(components.size());
		}

		@Override
		public Component getDefaultComponent(Container aContainer) {
			return textField1;
		}
	}


	public interface Callback {
		void setResult(ActionResult result);

	}

	public static abstract class PasswordFormActionListener implements ActionListener {
		private Callback callback;

		public PasswordFormActionListener(Callback callback) {
			this.callback = callback;
		}

	}

	public static class PasswordFormCancelActionListener extends PasswordFormActionListener {
		PasswordForm passwordForm;

		public PasswordFormCancelActionListener(Callback callback, PasswordForm passwordForm) {
			super(callback);
			this.passwordForm = passwordForm;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			passwordForm.exitPasswordForm();
		}
	}


	public PasswordFormCancelActionListener defaultCancelListener = new PasswordFormCancelActionListener(null, this);

	/**
	 * @param walletForm
	 * @param okListener optional action listener. if not provided, the one in this class will be used.
	 */
	public void showPasswordForm(WalletForm walletForm, PasswordFormActionListener okListener, PasswordFormActionListener cancelListener) {
		this.walletForm = walletForm;
		dialog = new JDialog(walletForm.frame, this.title != null ? this.title : "Please enter password", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().add(mainPanel);
		dialog.setPreferredSize(new Dimension(800, 400));

		dialog.getRootPane().setDefaultButton(btnOk);


		dialog.pack();


		// Put client property
		fldPassword.putClientProperty("JPasswordField.cutCopyAllowed", true);
		if (title == null)
			labelMsg.setText("Creating New Wallet");
		else {
			labelMsg.setText(title);
		}


		if (okListener != null)
			btnOk.addActionListener(okListener);
		else {
			btnOk.addActionListener(this);
		}

		if (cancelListener != null)
			btnCancel.addActionListener(cancelListener);
		else {
			btnCancel.addActionListener(defaultCancelListener);
		}


		dialog.setLocationRelativeTo(walletForm.frame);
		dialog.setVisible(true);


		textField1.requestFocusInWindow();

	}


	public void exitPasswordForm() {
		dialog.dispose();
	}


	public String getUserInputPass() {
		return new String(fldPassword.getPassword());
	}


	public String getCombinationDisplay() {
		return spinner1Item.getValue() + "-" + spinner2Item.getValue() + "-" + spinner3Item.getValue();
	}


	private void init() {
		createUIComponents();
	}

	private static class MyDocumentFilter extends DocumentFilter{
		JTextField textField;
		String regex;

		public MyDocumentFilter(JTextField textField){
			this.textField = textField;
			setRegexAllowStar();
		}

		public void setRegexOnlyDigits(){
			regex = "[0-9]";
		}

		public void setRegexAllowStar(){
			regex = "[*]";
		}

		@Override
		public void insertString(FilterBypass fb, int offs, String newString, AttributeSet a) throws BadLocationException {
			textField.setCaretPosition(0);
			if (newString.matches(regex)) {
				remove(fb, offs, fb.getDocument().getLength());
				super.insertString(fb, offs, newString, a);
				textField.setCaretPosition(0);
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String newString, AttributeSet attrs) throws BadLocationException {
			insertString(fb, offset, newString,attrs);
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			super.remove(fb, offset, length);
		}

	}


	//Handles the change of values when scrolling the mouseWheel
	private static class MyMouseWheelListener implements MouseWheelListener{
		JTextField textField;
		public MyMouseWheelListener(JTextField textField){
			this.textField = textField;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if(textField.getText().equals("*")){
				return;
			}
			int textFieldValue = Integer.parseInt(textField.getText());
			int safeNumber = 0;
			int rotationValue = e.getWheelRotation();
			//use % operator (mod 10) to stay in the bounds of 0-9
			if(rotationValue > 0){
				safeNumber = (((textFieldValue - 1) % 10) + 10) % 10;
			} else if(rotationValue < 0){
				safeNumber = ((textFieldValue + 1) % 10);
			}
			textField.setText(Integer.toString(safeNumber));
		}
	}

	//set the user entered pass and combination to the PassCombinationVO
	public PassCombinationVO getUserEnteredPassForVerification() {
		Integer safeValue1, safeValue2 , safeValue3;


		if (!SystemSettings.isDevMode) {
			 safeValue1 = spinner1Item.getValue();
			 safeValue2 = spinner2Item.getValue();
			 safeValue3 = spinner3Item.getValue();

			if (safeValue1.equals(safeValue2) && safeValue2.equals(safeValue3)) {
				DialogUtils.getInstance().info("Cant' use the same numbers for the safe combination.");
				return null;
			}

			if (!passwordValidator.validate( String.valueOf(fldPassword.getPassword()))) {
				DialogUtils.getInstance().info("Please use a password following the above rules.");
				return null;
			}
		}
		else {
			//dev mode
			safeValue1 =1;
			safeValue2 = 2;
			safeValue3 = 3;
		}


		PassCombinationVO passVO = new PassCombinationEncryptionAdaptor();
		WalletModel model = ServiceRegistry.instance.getWalletModel();
		//set the raw data only, do not add logic here. or later we can't get the raw pass
		passVO.setCombination(safeValue1.toString(), safeValue2.toString(), safeValue3.toString());
		if (SystemSettings.isDevMode) {
			passVO.setPass("Test123!");
		} else {
			passVO.setPass(String.valueOf(fldPassword.getPassword()));
		}

		model.setPassVO(passVO);
		return model.getUserEnteredPassForVerification();

	}

	/**
	 * Default ok button listener
	 *
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		boolean createHash = ServiceRegistry.instance.getWalletModel().getPassHash() == null;
		PassCombinationVO passVO = getUserEnteredPassForVerification();

		if (passVO == null) {
			//user input is not good. try again.
		} else {
			if (createHash) {
				//user password is no good, did not pass validation.
				CreateWalletAction createWalletAction = ServiceRegistry.instance.getService(BeanType.prototype, CreateWalletAction.class);
				createWalletAction.execute(passVO, this);
			} else {
				VerifyPasswordAction verifyPasswordAction = ServiceRegistry.instance.getService(BeanType.prototype, VerifyPasswordAction.class);
				ActionResult result = verifyPasswordAction.execute(passVO,
						ServiceRegistry.instance.getWalletModel().getPassHash(),
						ServiceRegistry.instance.getWalletModel().getCombinationHash()
				);
				if (result.isSuccess()) {
					//close the password form
					exitPasswordForm();

					//load the wallet
					LoadWalletAction loadWalletAction = ServiceRegistry.instance.getService(BeanType.prototype, LoadWalletAction.class);
					loadWalletAction.execute(passVO);
				}
			}
		}
	}

	/**
	 * class for capture fcocus related actions on the safe combination controls
	 * in order to mask the control.
	 */
	class MyFocusAdapter extends FocusAdapter {
		JTextField ctl;
		public MyFocusAdapter(JTextField ctl) {
			this.ctl = ctl;
		}

		public void focusLost(FocusEvent e) {
			Item ctlValue = new Item(Integer.parseInt(ctl.getText()), ctl.getText());

			//Save the current textField value in the spinner
			if (textField1 == ctl) {
				spinner1Item = ctlValue;
				docFilter1.setRegexAllowStar();
				textField1.setText("*");
			} else if (textField2 == ctl) {
				spinner2Item = ctlValue;
				docFilter2.setRegexAllowStar();
				textField2.setText("*");
			} else if (textField3 == ctl) {
				spinner3Item = ctlValue;
				docFilter3.setRegexAllowStar();
				textField3.setText("*");
			}

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					ctl.setText("*"); //position zero is reserved with "*"
				}
			});
		}

		/*
		The focus listener events
		on focus, we need to reselect the value saved in the spinner1 to 3. 
		*/
		public void focusGained(FocusEvent e) {

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					//Set the textFields text to the value of the spinner
					if (textField1 == ctl && spinner1Item != null) {
						docFilter1.setRegexOnlyDigits();
						textField1.setText(spinner1Item.getText());
					} else if (textField2 == ctl && spinner2Item != null) {
						docFilter2.setRegexOnlyDigits();
						textField2.setText(spinner2Item.getText());
					} else if (textField3 == ctl && spinner3Item != null) {
						docFilter3.setRegexOnlyDigits();
						textField3.setText(spinner3Item.getText());
					}
				}
			});
		}

	}

}
