import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class Algorythms extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String sRootNote;
	private String sScale;
	private static int[] iScale;
	private Scales scales = new Scales();

	public Algorythms() {
		super((Window) null);
		setModal(true);
		setSize(400, 250);
		setTitle("Auracle: Algorythms");
		createPanel();
		setIconImage(new ImageIcon(getClass().getResource(
				"/resources/icon.png"), "Icon").getImage());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		sRootNote = "A";
		sScale = "Major";

	}

	private void createPanel() {

		class enterAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				setScale();
			}
		}

		class escapeAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				close();
			}
		}

		Action enterAction = new enterAction();
		Action escapeAction = new escapeAction();

		JPanel panel = new JPanel();
		// Labels
		JLabel labelRoot = new JLabel("Root:");
		JLabel labelScale = new JLabel("Scale:");

		// Combo boxes
		final JComboBox<String> rootNoteCombo = new JComboBox<String>();
		rootNoteCombo.addItem("A");
		rootNoteCombo.addItem("A#");
		rootNoteCombo.addItem("B");
		rootNoteCombo.addItem("C");
		rootNoteCombo.addItem("C#");
		rootNoteCombo.addItem("D");
		rootNoteCombo.addItem("D#");
		rootNoteCombo.addItem("E");
		rootNoteCombo.addItem("F");
		rootNoteCombo.addItem("F#");
		rootNoteCombo.addItem("G");
		rootNoteCombo.addItem("G#");

		class RootNoteListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				sRootNote = (String) rootNoteCombo.getSelectedItem();
			}
		}
		ActionListener rootNoteListener = new RootNoteListener();
		rootNoteCombo.addActionListener(rootNoteListener);

		final JComboBox<String> scaleCombo = new JComboBox<String>();
		scaleCombo.addItem("Major");
		scaleCombo.addItem("Minor");
		scaleCombo.addItem("Harmonic Minor");
		scaleCombo.addItem("Melodic Minor");

		class ScaleListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				sScale = (String) scaleCombo.getSelectedItem();
			}
		}
		ActionListener scaleListener = new ScaleListener();
		rootNoteCombo.addActionListener(scaleListener);

		JButton okButton = new JButton(enterAction);
		okButton.setText("Ok");

		JButton cancelButton = new JButton(escapeAction);
		cancelButton.setText("Cancel");

		// Keybinds for enter key
		okButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
				"enterPressed");
		okButton.getActionMap().put("enterPressed", enterAction);

		cancelButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
				"escapePressed");
		cancelButton.getActionMap().put("escapePressed", escapeAction);

		// Keybinds for escape key
		rootNoteCombo.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),
				"escapePressed");
		rootNoteCombo.getActionMap().put("escapePressed", escapeAction);

		scaleCombo.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),
				"escapePressed");
		scaleCombo.getActionMap().put("escapePressed", escapeAction);

		okButton.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),
				"escapePressed");
		okButton.getActionMap().put("escapePressed", escapeAction);

		cancelButton.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),
				"escapePressed");
		cancelButton.getActionMap().put("escapePressed", escapeAction);

		panel.add(labelRoot);
		panel.add(rootNoteCombo);
		panel.add(labelScale);
		panel.add(scaleCombo);
		panel.add(okButton);
		panel.add(cancelButton);

		add(panel);
	}

	private void setScale() {
		String sScaleFinal = sRootNote + " " + sScale;

		if (sScale == "Major") {
			iScale = scales.majorScale(sScaleFinal);
		} else if (sScale == "Minor") {
			iScale = scales.minorScale(sScaleFinal);
		} else if (sScale == "Harmonic Minor") {
			iScale = scales.harmonicMinorScale(sScaleFinal);
		} else if (sScale == "Melodic Minor") {
			iScale = scales.melodicMinorScale(sScaleFinal);
		}

		close();
	}

	public int[] getScale() {
		return iScale;
	}

	private void close() {
		this.dispose();
	}

}
