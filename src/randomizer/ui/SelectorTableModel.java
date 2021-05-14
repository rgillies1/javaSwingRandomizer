package randomizer.ui;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import randomizer.RandomPool;
import randomizer.RandomTable;


/**
 * A custom TableModel that is used by the JTable in PickerPanel. 
 * @author Raymond Gillies
 */
public class SelectorTableModel extends AbstractTableModel {
	private final int MAX_OUTPUTS_PER_TABLE = 1000000;
	
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {"Table", "# of outputs (Max 1000000)", "Allow repeats?"};
	private ArrayList<Object[]> data = new ArrayList<>();
	private RandomPool tablePool;
	private PickerPanel parentPanel;
	private ArrayList<RandomTable> tables;
	
	private int rowCount;
	
	SelectorTableModel(PickerPanel parentPanel, RandomPool tablePool, ArrayList<RandomTable> tables) {
		rowCount = 0;
		SelectorTableModel thisModel = this;
		this.parentPanel = parentPanel;
		this.tablePool = tablePool;
		this.tables = tables;
		// Listener that validates input whenever data is entered
		this.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int row = e.getFirstRow();
				int column = e.getColumn();
				Object newData = thisModel.getValueAt(row, column);
				if(column == 0) { // Do nothing if the current selected table is not in the RandomPool (i.e. "Select a table...")
					String asString = (String)newData;
					if(!tablePool.has(asString)) return;
				} else if(column == 1) { // If the entered amount of outputs is too large, inform the user and set it to the largest accepted value
					Integer asInt = (Integer)newData;
					String tableName = (String)thisModel.getValueAt(row, column-1);
					if(!tablePool.has(tableName)) {
						newData = Integer.valueOf(0);
						data.get(row)[column] = newData;
						return;
					}
					Boolean canRepeat = (Boolean)thisModel.getValueAt(row, column+1);
					ArrayList<String> table = tablePool.getTable(tableName);
					
					if(!canRepeat && asInt > table.size()) {
						String errorMessage = "The value that you have entered is greater than the number of items in the table. "
								+ "If you would like to have more outputs than number of items in a table, you must allow repeats.";
						JOptionPane.showMessageDialog(parentPanel, errorMessage, "Invalid input!", JOptionPane.ERROR_MESSAGE);
						newData = Integer.valueOf(table.size());
					} else if(asInt > MAX_OUTPUTS_PER_TABLE) {
						String errorMessage = "The value that you have entered is greater than the allowed number of outputs per table. "
								+ "If you would like to have more outputs than the allowed amount per table, add a new table.";
						JOptionPane.showMessageDialog(parentPanel, errorMessage, "Invalid input!", JOptionPane.ERROR_MESSAGE);
						newData = Integer.valueOf(MAX_OUTPUTS_PER_TABLE);
					}
				} else if(column == 2) { // When toggling allowRepeats, if numOutputs is invalid, change the value;
					Boolean asBool = (Boolean)newData;
					Integer numOutputs = (Integer)thisModel.getValueAt(row, column-1);
					String tableName = (String)thisModel.getValueAt(row, column-2);
					if(!tablePool.has(tableName)) {
						numOutputs = Integer.valueOf(0);
						data.get(row)[column-1] = numOutputs;
						data.get(row)[column] = newData;
						fireTableCellUpdated(row, column-1);
						return;
					}
					ArrayList<String> table = tablePool.getTable(tableName);
					if(!asBool.booleanValue() && numOutputs > table.size()) {
						numOutputs = Integer.valueOf(table.size());
						data.get(row)[column-1] = numOutputs;
						fireTableCellUpdated(row, column-1);
					}
				}

				data.get(row)[column] = newData;
				thisModel.updateSelection();
				parentPanel.updateMainLabel();
				parentPanel.updateRandomizeButton();
			}
		});
		this.addRow();
		
	}
	
	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex)[columnIndex];
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		if(!parentPanel.isUnified()) return true;
		else if(row == 0 || col == 0) return true;
		else return false;
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		data.get(row)[col] = value;
		fireTableCellUpdated(row, col);
	}
	
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	}
	/**
	 * Adds a row to the JTable
	 */
	public void addRow() {
		data.add(new Object[] {"Select a table...", Integer.valueOf(0), Boolean.valueOf(false)});
		rowCount++;
	}
	/**
	 * Removes a row from the JTable
	 */
	public void removeRow() {
		data.remove(data.size() - 1);
		rowCount--;
	}
	/**
	 * Updates the current settings for selected tables in the tables ArrayList
	 */
	public void updateSelection() {
		tables.clear();
		for(Object[] obj : data) {
			String tableName = (String)obj[0];
			if(!tablePool.has(tableName)) continue;
			ArrayList<String> tableInPool = tablePool.getTable(tableName);
			int numOutputs = (Integer)obj[1];
			boolean canRepeat = (Boolean)obj[2];
			tables.add(new RandomTable(tableInPool, tableName, numOutputs, canRepeat));
		}
	}
	/**
	 * Calculates the total number of outputs in the tables ArrayList
	 * @return the total number of outputs
	 */
	public int getTotalOutputs() {
		int totalOutputs = 0;
		for(Object[] obj : data) {
			Integer asInt = (Integer)obj[1];
			totalOutputs += asInt;
		}
		return totalOutputs;
	}
}
