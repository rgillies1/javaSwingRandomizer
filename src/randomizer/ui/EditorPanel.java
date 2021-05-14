package randomizer.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;
import javax.swing.text.JTextComponent;

import randomizer.Log;
import randomizer.RandomPool;
import randomizer.ui.actions.RedoAction;
import randomizer.ui.actions.UndoAction;

/**
 * Class that represents the JTabbedPane tab that allows the user to edit the currently existing tables,
 * or create a new one.
 * @author Raymond Gillies
 */
@SuppressWarnings("serial")
class EditorPanel extends JPanel {

	// Table 
	private JPanel titlePane;
	private JPanel buttonPane;
	private JPanel editingButtonPane;
	private JLabel titleLabel;
	private JLabel mainLabel;
	private JButton doneButton;
	private JButton clearButton;
	private JButton deleteButton;
	private JButton undoButton; 
	private JButton redoButton;
	private JTextArea textbox;
	private JScrollPane scrollpane;
	private JComboBox<String> tableSelector;
	private JTextComponent tableSelectorEditorComponent;
	
	private UndoManager undoManager;
	private UndoAction undoAction;
	private RedoAction redoAction;
	
	private ImageIcon undoIcon;
	private ImageIcon redoIcon;
	
	private RandomPool tablePool;
	
	private PickerPanel picker;
	private Log log;

	// Constructor
	EditorPanel(PickerPanel picker, RandomPool tablePool, Log log) {
		this.picker = picker;
		this.log = log;
		this.tablePool = tablePool;
		makeEditorPane();
	}
	
	/**
	 * Creates the panel that will be displayed in the "edit tables" tab.
	 * @return the panel to be displayed in the tab
	 */
	private void makeEditorPane() {
		
		// Components
		titlePane = new JPanel();
		buttonPane = new JPanel();
		titleLabel = new JLabel("Enter a new table name or select an existing one from the list:");
		mainLabel = new JLabel("Input your desired table below:");
		doneButton = new JButton("Done");
		clearButton = new JButton("Clear");
		deleteButton = new JButton("Delete");
		textbox = new JTextArea();
		scrollpane = new JScrollPane(textbox);
		tableSelector = new JComboBox<>();
		tableSelectorEditorComponent = (JTextComponent)tableSelector.getEditor().getEditorComponent();
		
		// Undo / Redo actions
		undoManager = new UndoManager();
		undoAction = new UndoAction(undoManager);
		redoAction = new RedoAction(undoManager);
		undoAction.setRedoAction(redoAction);
		redoAction.setUndoAction(undoAction);
		
		// Undo / Redo buttons
		editingButtonPane = new JPanel();
		undoButton = new JButton();
		undoButton.setAction(undoAction);
		redoButton = new JButton();
		redoButton.setAction(redoAction);
		
		// Undo / Redo icons
		URL undoURL = getClass().getResource("images/undo16x16.gif");
		undoIcon = new ImageIcon(undoURL);
		URL redoURL =  getClass().getResource("images/redo16x16.gif");
		redoIcon = new ImageIcon(redoURL);
		
		// Text labels
		mainLabel.setAlignmentX(CENTER_ALIGNMENT);
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);
		textbox.setEditable(true);
		
		updateComboBox();
		
		// Event handling
		// Done button - based on current settings, gets a random output and diplays a ResultDialog.
		doneButton.addActionListener(e -> {
			new EditorWorker(this, picker, EditorWorker.JobType.DONE, tablePool, log).execute();
		});
		
		// Clear button - promts the user to confirm; if yes, clears the textarea.
		clearButton.addActionListener(e -> {
			new EditorWorker(this, picker, EditorWorker.JobType.CLEAR, tablePool, log).execute();
		});
		
		// Delete button - prompts the user to confirm; if yes, deletes the current table.
		deleteButton.addActionListener(e -> {
			new EditorWorker(this, picker, EditorWorker.JobType.DELETE, tablePool, log).execute();
		});
		
		// Table combo box - when a table name is entered / selected, worker thread searches for a table with that name
		// and displays it if it exists.
		tableSelector.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				new EditorWorker(this, picker, EditorWorker.JobType.SELECT, tablePool, log).execute();
		});
		
		// Undo / Redo support for the combo box
		tableSelectorEditorComponent.getDocument().addUndoableEditListener(e -> {
			undoManager.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		});
		
		// Popup menu specification - if a user right-clicks on the combo box, it requests focus and displays the popup
		tableSelectorEditorComponent.addMouseListener(new PopupListener(tableSelector));
		
		// Undo / Redo support for textarea
		textbox.getDocument().addUndoableEditListener(e -> {
			undoManager.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		});
		
		// Popup menu specification - if a user right-clicks on the textarea, it requests focus and displays the popup
		textbox.addMouseListener(new PopupListener(textbox));
		
		textbox.addKeyListener(new ClearListener());
		tableSelectorEditorComponent.addKeyListener(new ClearListener());
		
		// Text editor buttons
		undoButton.setIcon(undoIcon);
		undoButton.setText(null);
		undoButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl Z"), "undo-from-button");
		undoButton.getActionMap().put("undo-from-button", undoAction);
		undoButton.setToolTipText("Undo");
		
		redoButton.setIcon(redoIcon);
		redoButton.setText(null);
		redoButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl Y"), "redo-from-button");
		redoButton.getActionMap().put("redo-from-button", redoAction);
		redoButton.setToolTipText("Redo");
		
		// Panels
		tableSelector.setEditable(true);
		tableSelector.setSelectedIndex(-1);
		scrollpane.setPreferredSize(new Dimension(400, 400));
		scrollpane.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		scrollpane.setMinimumSize(new Dimension(20, 20));
		scrollpane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		titlePane.setPreferredSize(new Dimension(50, 45));
		titlePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
		titlePane.setLayout(new BoxLayout(titlePane, BoxLayout.Y_AXIS));
		titlePane.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		titlePane.add(titleLabel);
		titlePane.add(tableSelector);
		editingButtonPane.add(undoButton);
		editingButtonPane.add(redoButton);
		editingButtonPane.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
		buttonPane.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		buttonPane.setMaximumSize(new Dimension(800, 50));
		buttonPane.add(doneButton);
		buttonPane.add(deleteButton);
		clearButton.setEnabled(false);
		updateDeleteButton();
		
		// EditorPanel
		this.setPreferredSize(new Dimension(500, 500));
		this.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		
		// GridBagLayout settings
		this.setLayout(new GridBagLayout());
		this.add(titlePane, new GridBagConstraints(
				1, 		// x placement
				1, 		// y placement
				3, 		// column width
				1, 		// row height
				1.0,	// x weight
				0.0, 	// y weight
				GridBagConstraints.CENTER, // anchor 
				GridBagConstraints.HORIZONTAL,  // fill
				new Insets(0,0,0,0), // insets
				0,		// internal padding x 
				0		// internal padding y
				));
		this.add(mainLabel, new GridBagConstraints(
				1, 		// x placement
				2, 		// y placement
				3, 		// column width
				1, 		// row height
				1.0,	// x weight
				0.0, 	// y weight
				GridBagConstraints.CENTER, // anchor 
				GridBagConstraints.VERTICAL,  // fill
				new Insets(0,0,0,0), // insets
				0,		// internal padding x 
				0		// internal padding y
				));
		this.add(editingButtonPane, new GridBagConstraints(
				1, 		// x placement
				3, 		// y placement
				1, 		// column width
				1, 		// row height
				0.0,	// x weight
				0.0, 	// y weight
				GridBagConstraints.LINE_START, // anchor 
				GridBagConstraints.NONE,  // fill
				new Insets(0,0,0,0), // insets
				0,		// internal padding x 
				0		// internal padding y
				));
		this.add(clearButton, new GridBagConstraints(
				3, 		// x placement
				3, 		// y placement
				1, 		// column width
				1, 		// row height
				0.0,	// x weight
				0.0, 	// y weight
				GridBagConstraints.LINE_END, // anchor 
				GridBagConstraints.NONE,  // fill
				new Insets(0,0,0,0), // insets
				0,		// internal padding x 
				0		// internal padding y
				));
		this.add(scrollpane, new GridBagConstraints(
				1, 		// x placement
				4, 		// y placement
				3, 		// column width
				1, 		// row height
				1.0,	// x weight
				1.0, 	// y weight
				GridBagConstraints.CENTER, // anchor 
				GridBagConstraints.BOTH,  // fill
				new Insets(0,0,0,0), // insets
				0,		// internal padding x 
				0		// internal padding y
				));
		this.add(buttonPane, new GridBagConstraints(
				1, 		// x placement
				5, 		// y placement
				3, 		// column width
				1, 		// row height
				0.0,	// x weight
				0.0, 	// y weight
				GridBagConstraints.CENTER, // anchor 
				GridBagConstraints.BOTH,  // fill
				new Insets(0,0,0,0), // insets
				0,		// internal padding x 
				0		// internal padding y
				));
		
		this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		undoManager.discardAllEdits();
		undoAction.updateUndoState();
		redoAction.updateRedoState();
	}
	
	/**
	 * @return the name of the table currently being displayed (the combo box text)
	 */
	protected String getSelectedTableName() {
		return (String)tableSelector.getSelectedItem();
	}
	/**
	 * @return the text of the table currently being displayed (the textbox text)
	 */
	protected String getSelectedTableText() {
		return textbox.getText();
	}
	/**
	 * @return the index of the table currently being displayed
	 */
	protected int getSelectedTableIndex() {
		return tableSelector.getSelectedIndex();
	}
	/**
	 * Adds the passed string to the combo box so it can be selected
	 * @param toAdd the String object to add
	 */
	protected void addToComboBox(String toAdd) {
		tableSelector.addItem(toAdd);
	}
	/**
	 * Removes the entry of the combo box at the passed index
	 * @param toRemove index of the entry to be removed
	 */
	protected void removeFromComboBox(int toRemove) {
		tableSelector.removeItemAt(toRemove);
	}
	/**
	 * Clears the textbox and combo box of any current text
	 */
	protected void clearFields() {
		textbox.setText(null);
		tableSelector.setSelectedIndex(-1);
	}
	/**
	 * Removes all the entries in the combo box and populates it with the current RandomPool table names
	 */
	protected void updateComboBox() {
		tableSelector.removeAllItems();
		for(String s : tablePool.getTableNames()) {
			tableSelector.addItem(s);
		}
	}
	
	/**
	 * Sets the text in the textbox to the passed string.
	 * @param newText texet to be displayed
	 */
	protected void setTextboxText(String newText) {
		textbox.setText(newText);
	}
	/**
	 * If the table currently displayed is an already existing table, enable the delete button. Otherwise, disable it.
	 */
	protected void updateDeleteButton() {
		if(tableSelector.getSelectedIndex() == -1) deleteButton.setEnabled(false);
		else deleteButton.setEnabled(true);
	}
	/**
	 * If there is text currently in either text field, enable the clear button. Otherwise, disable it.
	 */
	protected void updateClearButton() {
		if(textbox.getText().isEmpty() && tableSelectorEditorComponent.getText().isEmpty())
			clearButton.setEnabled(false);
		else clearButton.setEnabled(true);
	}
	
	/***
	 * A subclass of MouseListener that displays a popup menu when the component passed in the constructor
	 * is right-clicked.
	 * @author Raymond Gillies
	 */
	private class PopupListener implements MouseListener {
		
		JComponent component;
		
		PopupListener(JComponent component) {
			this.component = component;
		}
		@Override
		public void mousePressed(MouseEvent e) {
			if(e.isPopupTrigger()) {
				component.requestFocusInWindow();
				setPopup();
				component.getComponentPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.isPopupTrigger()) {
				component.requestFocusInWindow();
				setPopup();
				component.getComponentPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			}
		}
		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		/**
		 * When this method is called, it creates a popup menu and displays it at the location where the
		 * calling MousePressed event happened.
		 * @author Raymond Gillies
		 */
		@SuppressWarnings("unchecked")
		private void setPopup() {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem copy;
			JMenuItem paste;
			JMenuItem cut;
			JMenuItem undo;
			JMenuItem redo;
			if(component instanceof JTextArea) {
				copy = new JMenuItem(component.getActionMap().get("copy-to-clipboard"));
				paste = new JMenuItem(component.getActionMap().get("paste-from-clipboard"));
				cut = new JMenuItem(component.getActionMap().get("cut-to-clipboard"));
				undo = new JMenuItem(undoAction);
				undo.setIcon(undoIcon);
				redo = new JMenuItem(redoAction);
				redo.setIcon(redoIcon);
				component.setComponentPopupMenu(popup);
			} else if(component instanceof JComboBox) {
				JComboBox<String> combo = (JComboBox<String>)component;
				JTextField comboText = (JTextField)combo.getEditor().getEditorComponent();
				copy = new JMenuItem(comboText.getActionMap().get("copy-to-clipboard"));
				paste = new JMenuItem(comboText.getActionMap().get("paste-from-clipboard"));
				cut = new JMenuItem(comboText.getActionMap().get("cut-to-clipboard"));
				undo = new JMenuItem(undoAction);
				undo.setIcon(undoIcon);
				redo = new JMenuItem(redoAction);
				redo.setIcon(redoIcon);
				combo.setComponentPopupMenu(popup);
			} else {
				copy = new JMenuItem();
				paste = new JMenuItem();
				cut = new JMenuItem();
				undo = new JMenuItem();
				redo = new JMenuItem();
			}
			copy.setText("Copy");
			paste.setText("Paste");
			cut.setText("Cut");
			undo.setText("Undo");
			redo.setText("Redo");
			popup.add(copy);
			popup.add(paste);
			popup.add(cut);
			popup.add(undo);
			popup.add(redo);
		}
	}
	
	/**
	 * A KeyListener class that checks whether the text area is empty and enables the clear
	 * button if so.
	 * @author Raymond Gillies
	 */
	private class ClearListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {
			updateClearButton();
		}
	}
}
