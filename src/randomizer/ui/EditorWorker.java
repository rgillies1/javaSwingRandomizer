package randomizer.ui;

import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import randomizer.Log;
import randomizer.RandomPool;


/**
 * A class that handles most actions related to the EditorPanel
 * @author Raymond Gillies
 */
class EditorWorker extends SwingWorker<Void, Void> {
	
	/***
	 * An enumeration that contains all possible jobs that EditorWorker can perform.
	 * @author Raymond Gillies
	 */
	protected enum JobType { DONE, CLEAR, DELETE, SELECT }
	
	JobType job;
	EditorPanel editorInstance;
	PickerPanel pickerInstance;
	RandomPool pool;
	Log log;
	
	EditorWorker(EditorPanel editorInstance, PickerPanel pickerInstance, JobType job, RandomPool pool, Log log) {
		this.editorInstance = editorInstance;
		this.pickerInstance = pickerInstance;
		this.job = job;
		this.pool = pool;
		this.log = log;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		switch(job) {
		case CLEAR:
			editorClearAction();
			break;
		case DELETE:
			editorDeleteAction();
			break;
		case DONE:
			editorDoneAction();
			break;
		case SELECT:
			editorSelectorAction();
			break;
		default:
			throw new IllegalArgumentException();
		}
		return null;
	}
	
	@Override
	protected void done() {
		log.writeToLog("EditorWorker has finished! (" + job + ")");
		try {
			get();
		} catch (InterruptedException | ExecutionException e) {
			if(log.isEnabled()) e.printStackTrace(log.getWriter());
		}
	}
	
	/***
	 * Attempts to save the currently displayed editor text as a table. If the either text field is blank, the
	 * user is notified vid JOptionPane and this method returns. If a table with the exact same name already exists,
	 * the user is prompted to confirm whether or not they'd like to overwrite it. After the table is saved, the
	 * current list of tables is also saved, the text is cleared, and any relevant components are updated.
	 * @author Raymond Gillies
	 */
	private void editorDoneAction() {
		int overwrite = -1;
		
		log.writeToLog("saving table");
		// Actions
		String tableName = editorInstance.getSelectedTableName();
		String tableText = editorInstance.getSelectedTableText();
		// Ask user to fill out title field or table
		if(tableName == null || tableName.isBlank()) {
			JOptionPane.showMessageDialog(null, "Please enter a title for the table.", "Notification", JOptionPane.WARNING_MESSAGE);
			return;
		} else if(tableText.isBlank()) {
			JOptionPane.showMessageDialog(null, "Please provide at least one entry.", "Notification", JOptionPane.WARNING_MESSAGE);
			return;
		}
		// Ask user if they want to overwrite the table given
		if(pool.has(tableName)) {
			overwrite = JOptionPane.showConfirmDialog(editorInstance, "Are you sure you want to overwrite the table '"
					+ tableName + "'?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(overwrite == JOptionPane.NO_OPTION) return; // If "no" is selected, don't do anything else.
		}
		// Save table
		pool.newTableFromString(tableText, tableName);
		pool.save();
		if(overwrite != JOptionPane.YES_OPTION) editorInstance.addToComboBox(tableName);
		editorInstance.clearFields();
		pickerInstance.updateMainLabel();
		pickerInstance.updateRandomizeButton();
		pickerInstance.updateSelectors();
	}
	
	/***
	 * Prompt the user via JOptionPane to confirm that they'd like to clear all displayed text.
	 * If the user selects 'no', nothing happens. If the user selects 'yes', all editable
	 * text fields are cleared.
	 * @author Raymond Gillies
	 */
	private void editorClearAction() {
		log.writeToLog("clearing text");
		int clear = JOptionPane.showConfirmDialog(editorInstance, 
				"Are you sure you want to clear all current text?", 
				"Confirmation", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
		if(clear == JOptionPane.YES_OPTION) {
			editorInstance.clearFields();
		}
		editorInstance.updateDeleteButton();
		editorInstance.updateClearButton();
	}
	
	/***
	 * Prompt the user via JOptionPane to confirm that they'd like to delete the currently displayed table.
	 * If the user selects 'no', nothing happens. If the user selects 'yes', the table is removed from the
	 * list of tables. The relevant components are then updated.
	 */
	private void editorDeleteAction() {
		log.writeToLog("Deleting table");
		
		int delete = JOptionPane.showConfirmDialog(editorInstance, 
				"Are you sure you want to delete '"
				+ editorInstance.getSelectedTableName() + "'?", 
				"Confirmation", 
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if(delete == JOptionPane.YES_OPTION) {
			pool.deleteTable(editorInstance.getSelectedTableName());
			editorInstance.removeFromComboBox(editorInstance.getSelectedTableIndex());
			pool.save();
			editorInstance.clearFields();
			pickerInstance.updateMainLabel();
			pickerInstance.updateRandomizeButton();
			pickerInstance.updateSelectors();
			log.writeToLog("Table deleted");
		}
	}
	/***
	 * Checks to see if the current contents of the editor's combo box are a title of an existing table.
	 * If it is, the contents of that table are displayed in the text area.
	 */
	private void editorSelectorAction() {
		// Component Validaton
		if(log.isEnabled()) {
			log.writeToLog("Selector worker");
			log.writeToLog("editor: " + editorInstance);
		}
		if(editorInstance.getSelectedTableIndex() != -1) {
			if(log.isEnabled()) {
				log.writeToLog("Item is selected");
				log.writeToLog("Has?: " + pool.has(editorInstance.getSelectedTableName()));
			}
			StringBuilder resultBuilder = new StringBuilder();
			for(String str : pool.getTable(editorInstance.getSelectedTableName())) {
				resultBuilder.append(str);
				resultBuilder.append('\n');
			}
			editorInstance.setTextboxText(resultBuilder.toString());
		}
		editorInstance.updateDeleteButton();
		editorInstance.updateClearButton();
	}
	
}
