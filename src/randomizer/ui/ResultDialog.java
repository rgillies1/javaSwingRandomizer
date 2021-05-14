package randomizer.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import randomizer.Log;

/**
 * A modal dialog that displays the results of randomizing to the end user.
 * @author Raymond Gillies
 */
@SuppressWarnings("serial")
class ResultDialog extends JDialog {
	
	private final int DIALOG_WIDTH = 400;
	private final int DIALOG_HEIGHT = 300;
	
	private String[] result;
	
	private PickerPanel from;
	
	private JPanel resultPanel;
	private JPanel buttonPanel;
	private JLabel repeatLabel;
	private JList<String> resultList;
	private JLabel doneText;
	private JButton yesButton;
	private JButton noButton;
	private JButton copySelectedButton;
	private JButton copyAllButton;
	private JScrollPane resultScrollpane;
	
	private Log log;
	
	public ResultDialog(PickerPanel from, Window to, String[] result) {
		super(to, "Randomizer Results", JDialog.ModalityType.DOCUMENT_MODAL);
		this.result = result;
		this.from = from;
		makeFrame();
		this.setLocationRelativeTo(to);
		this.pack();
	}
	
	/**
	 * Creates the main JFrame of this JDialog and sets it as the content pane
	 */
	private void makeFrame() {
		resultPanel = new JPanel();
		buttonPanel = new JPanel();
		repeatLabel = new JLabel("Would you like to repeat using the same settings?");
		doneText = new JLabel("The Randomizer produced the following output:");
		yesButton = new JButton("Yes");
		noButton = new JButton("No");
		copySelectedButton = new JButton("Copy Selected");
		copyAllButton = new JButton("Copy All");
		
		resultList = new JList<String>(result);
		resultList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		resultList.setLayoutOrientation(JList.VERTICAL);
		DefaultListCellRenderer renderer = (DefaultListCellRenderer)resultList.getCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		resultScrollpane = new JScrollPane(resultList);

		yesButton.addActionListener(e -> {
			this.dispose();
			from.pickerGoAction();
		});
		
		noButton.addActionListener(e -> {
			this.dispose();
		});
		
		copySelectedButton.addActionListener(e -> {
			try {
				String str = String.join("\n", resultList.getSelectedValuesList());
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection toCopy = new StringSelection(str);
				c.setContents(toCopy, null);
			} catch (Error err) {
				JOptionPane.showMessageDialog(this, "An error occured.\n" + err);
			}
		});
		copySelectedButton.setEnabled(false);
		
		copyAllButton.addActionListener(e -> {
			try {
				String str = String.join("\n", result);
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection toCopy = new StringSelection(str);
				c.setContents(toCopy, null);
			} catch (Error err) {
				JOptionPane.showMessageDialog(this, "An error occured.\n" + err);
				log.writeToLog(err.toString());
			}
		});
		
		resultList.addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()) {
				if(resultList.getSelectedIndex() == -1) {
					copySelectedButton.setEnabled(false);
				} else {
					copySelectedButton.setEnabled(true);
				}
			}
		});
		
		this.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		resultScrollpane.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		
		buttonPanel.add(yesButton);
		buttonPanel.add(noButton);
		buttonPanel.add(copySelectedButton);
		buttonPanel.add(copyAllButton);
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		resultPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		repeatLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		buttonPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		doneText.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		
		resultPanel.add(doneText);
		resultPanel.add(resultScrollpane);
		resultPanel.add(repeatLabel);
		resultPanel.add(buttonPanel);
		this.setContentPane(resultPanel);
	}
}
