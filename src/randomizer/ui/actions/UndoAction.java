package randomizer.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Represents a basic undo action
 * @author Raymond Gillies
 */
@SuppressWarnings("serial")
public class UndoAction extends AbstractAction {

	UndoManager manager;
	RedoAction redoAction;
	
	public UndoAction(UndoManager manager) {
		super("Undo");
		this.manager = manager;
	}
	
	public void setRedoAction(RedoAction redoAction) {
		this.redoAction = redoAction;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			manager.undo();
		} catch(CannotUndoException e1) {
			System.out.println("Undo failed. " + e1);
		}
		updateUndoState();
		redoAction.updateRedoState();
	}
	
	public void updateUndoState() {
		if(manager.canUndo()) {
			this.setEnabled(true);
		} else {
			this.setEnabled(false);
		}
	}
}
