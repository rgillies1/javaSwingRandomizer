package randomizer;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import randomizer.ui.MainWindow;

/**
 * The main executable for the Randomizer program. Sets the look and feel, attempts to generate a log,
 * then launches the main window
 * @author Raymond Gillies
 *
 */
public class Driver {
	
	public static void main(String[] args) {
		
		// Generate log
		Log log;
		try {
			log = new Log();
		} catch(IOException e) {
			return;
		}
		log.writeToLog("Log created");
		
		RandomPool tables = new RandomPool(log);
		
		tables.load();
		
		// Set look and feel to system default
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			if(log.isEnabled()) e.printStackTrace(log.getWriter());
		}
		
		// Genereate main window
		MainWindow ui = new MainWindow(tables, log);
		
		log.writeToLog("Tables found:");
		for(String str : tables.getTableNames()) {
			log.writeToLog(str);
		}
		
		// Display main window on EVT
		SwingUtilities.invokeLater(ui);

		log.writeToLog("End of driver reached");
		
		tables.save();
		
	}
}