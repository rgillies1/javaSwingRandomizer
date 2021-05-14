package randomizer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import randomizer.Randomizer;
import randomizer.RandomPool;
import randomizer.RandomTable;
import randomizer.Log;
// TODO: fix column greying out when turning off unification

/**
 * Class that represents the JTabbedPane tab that allows the user to select which tables
 * they'd like to get output from, among other related settings.
 * @author Raymond Gillies
 */
@SuppressWarnings("serial")
class PickerPanel extends JPanel {

	private final int ROW_HEIGHT = 30;

	
	private boolean unified;
	private RandomPool tablePool;
	
	private JTable selectorTable;
	private SelectorTableModel tableModel;
	private ArrayList<RandomTable> tables;
	
	// Table selector
	private JPanel pickerPane;
	private JPanel buttonPane;
	private JLabel mainLabel;
	private JButton addButton;
	private JButton removeButton;
	private JButton goButton;
	private JCheckBox oneTableCheckBox;
	private JScrollPane selectorScrollpane;
	
	private Log log;
	
	PickerPanel(RandomPool tablePool, Log log) {
		this.unified = false;
		this.log = log;
		this.tablePool = tablePool;
		tables = new ArrayList<>();
		makeSelectorPane();
	}

	/**
	 * Creates the panel that will be displayed in the "choose tables" tab.
	 * @return the panel to be displayed in the tab
	 */
	private void makeSelectorPane() {
		tableModel = new SelectorTableModel(this, tablePool, tables);
		selectorTable = new JTable(tableModel) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				Component comp = super.prepareRenderer(renderer, row, col);
				SelectorTableModel model = (SelectorTableModel)selectorTable.getModel();
				if(unified) {
					if(!model.isCellEditable(row, col)) {
						comp.setBackground(Color.gray);
					} else {
						comp.setBackground(Color.white);
						comp.setForeground(Color.black);
					}
				}
				return comp;
			}
		};
		pickerPane = new JPanel();
		mainLabel = new JLabel();
		addButton = new JButton("Add another table");
		selectorScrollpane = new JScrollPane(selectorTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		buttonPane = new JPanel();
		removeButton = new JButton("Remove a table");
		goButton = new JButton("Randomize");
		oneTableCheckBox = new JCheckBox("Combine tables");
		
		selectorTable.setFillsViewportHeight(true);
		selectorTable.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor(makeNewComboBox()));
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)selectorTable.getTableHeader().getDefaultRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		selectorTable.setRowHeight(ROW_HEIGHT);
		selectorTable.revalidate();
		
		goButton.addActionListener(e -> {
			pickerGoAction();
		});
		goButton.setEnabled(false);
		
		addButton.addActionListener(e -> {
			pickerAddAction();
		});
		
		removeButton.addActionListener(e -> {
			pickerRemoveAction();
		});
		
		oneTableCheckBox.addActionListener(e -> {
			oneTableSelectorAction();
		});
		
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				updateMainLabel();
				updateRandomizeButton();
				repaint();
			}
			@Override
			public void focusLost(FocusEvent e) {
				updateMainLabel();
				updateRandomizeButton();
				repaint();
			}
		});
		
		removeButton.setEnabled(false);
		
		mainLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		updateMainLabel();
		updateRandomizeButton();
		pickerPane.setLayout(new BoxLayout(pickerPane, BoxLayout.Y_AXIS));
		buttonPane.add(goButton);
		buttonPane.add(addButton);
		buttonPane.add(removeButton);
		buttonPane.add(oneTableCheckBox);
		buttonPane.setMinimumSize(new Dimension(500, 20));
		buttonPane.setPreferredSize(new Dimension(700, 30));
		buttonPane.setMaximumSize(new Dimension(800, 30));
		selectorScrollpane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		selectorScrollpane.setMinimumSize(new Dimension(20, 20));
		selectorScrollpane.setPreferredSize(new Dimension(450, 450));
		selectorScrollpane.setBorder(BorderFactory.createEmptyBorder());
		oneTableCheckBox.setToolTipText("If this is enabled, the randomizer will combine all tables into one large one, and pulling using the settings indicated on the first table.");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		this.add(mainLabel);
		this.add(selectorScrollpane);
		this.add(buttonPane);
		this.revalidate();
		this.repaint();
		
	}
	
	/***
	 * Gets the current settings from the active instance of PickerPanel and uses them to
	 * launch a new thread to call the randomize() function, then displays the result in a
	 * new ResultDialog.
	 * @author Raymond Gillies
	 * @see Randomizer
	 */
	protected void pickerGoAction() {
		Window to = (Window)this.getTopLevelAncestor();
		PickerPanel from = this;
		((MainWindow)to).startProgressBar();
		new SwingWorker<Void, Void>() {
			String[] result;
			@Override
			protected Void doInBackground() throws Exception {
				Randomizer rand = new Randomizer();
				result = rand.randomize(tables, unified);
				return null;
			}
			
			@Override
			public void done() {
				ResultDialog dialog = new ResultDialog(from, to, result);
				((MainWindow)to).stopProgressBar();
				dialog.setVisible(true);
			}
		}.execute();
	}
	
	/***
	 * Creates a new Selector object and initializes it based on the settings in this Worker's
	 * instance of PickerPanel. Then adds the Selector object to the PickerPanel.
	 * @author Raymond Gillies
	 * @see Selector
	 */
	protected void pickerAddAction() {
		log.writeToLog("Adding table");
		tableModel.addRow();
		this.updateRemoveButton();
		this.revalidate();
		this.repaint();
		selectorTable.revalidate();
		selectorTable.repaint();
	}
	
	/***
	 * Removes the most recently added Selector object from this Worker's instance of
	 * picker panel. If there is only one Selector left after one is removed, it disables
	 * the Remove button in the PickerPanel.
	 * @author Raymond Gillies
	 */
	protected void pickerRemoveAction() {
		log.writeToLog("Removing table");
		TableCellEditor cellEditor = selectorTable.getCellEditor();
		if(cellEditor != null) cellEditor.stopCellEditing();
		tableModel.removeRow();
		this.updateRemoveButton();
		selectorTable.revalidate();
		selectorTable.repaint();
	}
	
	/***
	 * Enables and builds the 'unified table', in which all selected tables are
	 * compiled into one larger one to pull from.
	 * @author Raymond Gillies
	 */
	protected void oneTableSelectorAction() {
		unified = unified ? false : true;
		selectorTable.revalidate();
		selectorTable.repaint();
	}
	/**
	 * Updates the label at the top of the panel. The label changes depending on whether or not the RandomPool is empty.
	 */
	protected void updateMainLabel() {
		if(tablePool.isEmpty()) mainLabel.setText("There are currently no tables to pull from. Please enter a table in the 'Edit tables' tab.");
		else mainLabel.setText("Select the table(s) you wish to use.");
	}
	/**
	 * Enables or disables the Randomize button if the current settings are invalid.
	 */
	protected void updateRandomizeButton() {
		if(tableModel.getTotalOutputs() == 0) {
			goButton.setEnabled(false);
			goButton.setToolTipText("<html>Generates a random output with the given settings.<br />(NOTE: You do not have any tables to pull from.)</html>");
		}
		else {
			goButton.setEnabled(true);
			goButton.setToolTipText("Generates a random output with the given settings.");
		}
	}
	/**
	 * @return true if the Combine tables checkbox is checked, false otherwise.
	 */
	protected boolean isUnified() {
		return unified;
	}
	/**
	 * Disables the remove button if there is only one row in the table. Enables it otherwise.
	 */
	protected void updateRemoveButton() {
		if(tableModel.getRowCount() > 1) removeButton.setEnabled(true);
		else removeButton.setEnabled(false);
	}
	/**
	 * Updates the combo boxes in the table with the current state of the RandomPool
	 */
	protected void updateSelectors() {
		selectorTable.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor(makeNewComboBox()));
		
		for(int row = 0; row < tableModel.getRowCount(); row++) {
			String tableAtRow = (String)tableModel.getValueAt(row, 0);
			if(!tablePool.has(tableAtRow)) {
				tableModel.setValueAt("Select a table...", row, 0);
				tableModel.setValueAt(Integer.valueOf(0), row, 1);
			}
		}
	}
	/**
	 * Creates a new combo box with the current contents of the RandomPool
	 * @return a JComboBox
	 */
	private JComboBox<String> makeNewComboBox() {
		String[] list = tablePool.getTableNames();
		JComboBox<String> toMake = new JComboBox<>(list);
		return toMake;
	}
	
	/**
	 * Cell editor class that cancels cell editing if a combo box in the table is clicked away from
	 * @author Raymond Gillies
	 */
	class ComboBoxCellEditor extends DefaultCellEditor {
		ComboBoxCellEditor(JComboBox<?> combo) {
			super(combo);
			combo.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					cancelCellEditing();
				}
				
			});
		}
	}
	
	
}
