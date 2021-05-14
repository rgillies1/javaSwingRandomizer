package randomizer.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

import randomizer.RandomPool;
import randomizer.Log;
/**
 * The parent JFrame that all other components reside under
 * @author Raymond Gillies
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame 
				implements Runnable {
	
	private JPanel mainPane;
	protected JTabbedPane tabbedPane;
	private JPanel statusBar;
	private JProgressBar progressBar;
	private JLabel progressLabel;
	private PickerPanel picker;
	private EditorPanel editor;
	/**
	 * Initializes a window and places all components within.
	 * Should be called from the event dispatch thread.
	 * @author Raymond Gillies
	 */
	public MainWindow(RandomPool tablePool, Log log) {
		super("Randomizer");
		
		mainPane = new JPanel();
		statusBar = new JPanel();
		progressBar = new JProgressBar();
		progressLabel = new JLabel();
		tabbedPane = new JTabbedPane(); 
		
		picker = new PickerPanel(tablePool, log);
		editor = new EditorPanel(picker, tablePool, log);
		
		tabbedPane.addTab("Choose tables", picker);
		tabbedPane.addTab("Edit tables", editor);
		tabbedPane.setBorder(BorderFactory.createEmptyBorder());
		
		statusBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		statusBar.add(Box.createRigidArea(new Dimension(0, 15)));
		statusBar.add(progressLabel);
		statusBar.add(progressBar);
		progressBar.setVisible(false);
		
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		
		progressBar.setAlignmentX(RIGHT_ALIGNMENT);
		
		mainPane.add(tabbedPane);
		mainPane.add(statusBar);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				log.closeBuffer();
			}
		});
		
		this.setContentPane(mainPane);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
	}
	
	@Override
	public void run() {
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/***
	 * Shows the progress bar and label in the status bar and sets the progress bar to indeterminate
	 * @author Raymond Gillies
	 */
	protected void startProgressBar() {
		progressLabel.setText("Randomizing...");
		progressBar.setVisible(true);
		progressBar.setIndeterminate(true);
	}
	
	/***
	 * Hides the progress bar and label
	 * @author Raymond Gillies
	 */
	protected void stopProgressBar() {
		progressLabel.setText("");
		progressBar.setIndeterminate(false);
		progressBar.setVisible(false);
	}

}
