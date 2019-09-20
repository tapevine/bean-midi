import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BeanMidiFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Get display size
	GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	int width = gd.getDisplayMode().getWidth();
	int height = gd.getDisplayMode().getHeight();

	private MidiFrame mf;
	private JSlider tempoSlider;
	private int iBPM;
	private JLabel tempoTxtLbl;

	public BeanMidiFrame() {
		// Set window preferences
		setSize(800, 600);
		setTitle("Bean MIDI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(new ImageIcon(getClass().getResource(
				"/resources/icon.png"), "Icon").getImage());

		// Menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(createOptionsMenu());
		menuBar.add(createHelpMenu());

		createPanel();
	}

	private JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.add(createFileNewMidiItem());
		return menu;
	}

	private JMenuItem createFileNewMidiItem() {
		JMenuItem menuItem = new JMenuItem("MIDI Item");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				String measures = JOptionPane
						.showInputDialog("Enter number of measures:");
				int iMeasures = 0;

				try {
					iMeasures = Integer.parseInt(measures);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null,
							"Measures must be numeric "
									+ "and equal to at least one");
				}

				if (iMeasures > 0) {

					mf = new MidiFrame(width, height, iMeasures * 1280, iBPM);
					mf.setVisible(true);
				}
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	private JMenu createEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		return menu;
	}

	private JMenu createOptionsMenu() {
		JMenu menu = new JMenu("Options");
		menu.setMnemonic(KeyEvent.VK_O);
		menu.add(createOptionsPrefItem());
		return menu;
	}

	private JMenuItem createOptionsPrefItem() {
		JMenuItem menuItem = new JMenuItem("Preferences");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.CTRL_MASK));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				JFrame frame = new Preferences();
				frame.setVisible(true);
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}
	
	private JMenu createHelpMenu() {
		JMenu menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menu.add(createHelpAboutItem());
		return menu;
	}
	
	private JMenuItem createHelpAboutItem() {
		JMenuItem menuItem = new JMenuItem("About");
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				JFrame frame = new About();
				frame.setVisible(true);
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	private JPanel createNorth() {

		class TempoListener implements ChangeListener {

			public void stateChanged(ChangeEvent event) {

				changeTempo();
			}

		}

		iBPM = 120;

		TempoListener listener = new TempoListener();

		// Labels
		JLabel tempoLbl = new JLabel("Tempo:");
		tempoTxtLbl = new JLabel(iBPM + " BPM");

		// Slider
		tempoSlider = new JSlider(2, 300, 120);
		tempoSlider.addChangeListener(listener);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 3));
		panel.add(tempoLbl);
		panel.add(tempoSlider);
		panel.add(tempoTxtLbl);
		return panel;
	}

	private void createPanel() {

		JPanel northPanel = createNorth();

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));

		panel.setBorder(new EtchedBorder());
		panel.add(northPanel);
		add(panel, BorderLayout.NORTH);
	}

	private void changeTempo() {
		iBPM = tempoSlider.getValue();
		tempoTxtLbl.setText(iBPM + " BPM");
	}
}