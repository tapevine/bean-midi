import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class Preferences extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BeanMidiSequencer as = new BeanMidiSequencer();
	private String sInDev, sOutDev;

	public Preferences() {
		setSize(400, 250);
		setTitle("Bean MIDI: Preferences");
		setIconImage(new ImageIcon(getClass().getResource(
				"/resources/icon.png"), "Icon").getImage());
		createPanel();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void createPanel() {

		JPanel panel = new JPanel();

		panel.setBorder(new TitledBorder("MIDI Devices"));
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		JLabel labelInDev = new JLabel("Input:");
		c.gridx = 0;
		c.gridy = 0;
		panel.add(labelInDev, c);

		// Get an array of MIDI devices from the sequencer
		ArrayList<String> aInDev = new ArrayList<String>();
		aInDev = as.getInDevices();

		// Create combo box of MIDI in devices
		final JComboBox<String> inDevCombo = new JComboBox<String>();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 1.0;

		for (int x = 0; x < aInDev.size(); x++) {
			inDevCombo.addItem(aInDev.get(x));
		}

		// TODO: Ugly code change this in the future
		// Gets last selected device
		if (BeanMidiSequencer.inDev != null) {
			inDevCombo.setSelectedItem(BeanMidiSequencer.inDev.getDeviceInfo()
					.getName());
		}

		class InDevListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				sInDev = (String) inDevCombo.getSelectedItem();
			}
		}
		ActionListener inDevListener = new InDevListener();
		inDevCombo.addActionListener(inDevListener);

		panel.add(inDevCombo, c);

		JLabel labelOutDev = new JLabel("Output:");
		c.gridx = 0;
		c.gridy = 4;
		panel.add(labelOutDev, c);

		// Get an array of MIDI out devices from the sequencer
		ArrayList<String> aOutDev = new ArrayList<String>();
		aOutDev = as.getOutDevices();

		// Create combo box of MIDI out devices
		final JComboBox<String> outDevCombo = new JComboBox<String>();
		c.gridx = 1;
		c.gridy = 4;

		for (int x = 0; x < aOutDev.size(); x++) {
			outDevCombo.addItem(aOutDev.get(x));
		}

		// TODO: Ugly code change this in the future
		// Gets last selected device
		if (BeanMidiSequencer.outDev != null) {
			outDevCombo.setSelectedItem(BeanMidiSequencer.outDev.getDeviceInfo()
					.getName());
		}

		class OutDevListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				sOutDev = (String) outDevCombo.getSelectedItem();
			}
		}
		ActionListener outDevListener = new OutDevListener();
		outDevCombo.addActionListener(outDevListener);

		panel.add(outDevCombo, c);

		JButton okButton = new JButton("Ok");
		c.gridx = 2;
		c.gridy = 6;

		class OkButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				setDevices();
				close();
			}
		}
		ActionListener okListener = new OkButtonListener();
		okButton.addActionListener(okListener);

		panel.add(okButton, c);

		add(panel);

	}

	private void setDevices() {

		as.setInDevice(sInDev);
		as.setOutDevice(sOutDev);

	}

	private void close() {
		this.dispose();
	}
}
