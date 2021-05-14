package randomizer.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

/**
 * Represents a basic redo action
 * @author Raymond Gillies
 */
@SuppressWarnings("serial")
public class RedoAction extends AbstractAction {

	UndoManager manager;
	UndoAction undoAction;
	
	public RedoAction(UndoManager manager) {
		super("Redo");
		this.manager = manager;
	}
	
	public void setUndoAction(UndoAction undoAction) {
		this.undoAction = undoAction;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			manager.redo();
		} catch(CannotRedoException e1) {
			System.out.println("Redo failed. " + e1);
		}
		updateRedoState();
		undoAction.updateUndoState();
	}
	
	public void updateRedoState() {
		if(manager.canRedo()) {
			this.setEnabled(true);
		} else {
			this.setEnabled(false);
		}
	}

}
