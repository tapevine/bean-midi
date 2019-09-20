import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MidiFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	

	private MidiPane midiPane;
	private KeyPane keyPane;
	private JSplitPane splitPane;
	private MidiTransport midiTransport;
	private Toolbar toolbar;
	
	// Sequencer
	BeanMidiSequencer bms = new BeanMidiSequencer();
	
	// Key and Midi scrollers
	private KeyScroller keyScroller;
	private MidiScroller midiScroller;



	// Take globals
	private int takeLength;
	private int takeBPM;
	private int takeChannel;

	// Store drawn notes
	private Vector<MidiNote> notes;

	// Store cut or copied notes
	private Vector<MidiNote> notesBuf;

	// Store mouse x position
	private double mosPosX;

	// Relative dragging of notes
	private double firstNoteX;
	private double firstNoteY;
	
	// Grid
	private double gridSize;
	private double noteSize;
	
	// Classes to sync midi scroller to keyscroller
	class DownBarChangeListener implements ChangeListener {
	    public void stateChanged(ChangeEvent e) {
	      JScrollBar srcBar = midiScroller.getHorizontalScrollBar();
	      JScrollBar targetBar = keyScroller.getHorizontalScrollBar();
	      targetBar.setValue((int)(srcBar.getValue()));
	    }
	  }
	
	class UpBarChangeListener implements ChangeListener {
	    public void stateChanged(ChangeEvent e) {
	      JScrollBar srcBar = midiScroller.getVerticalScrollBar();
	      JScrollBar targetBar = keyScroller.getVerticalScrollBar();
	      targetBar.setValue((int)(srcBar.getValue()));
	    }}
	

	public MidiFrame(int width, int height, int takelength, int bpm) {

		takeLength = takelength;
		takeBPM = bpm;
		keyPane = new KeyPane();
		midiPane = new MidiPane();
		midiScroller = new MidiScroller(midiPane);
		keyScroller = new KeyScroller(keyPane);
		
		System.out.println(width + " " + height);
		

		// Listen for vertical scroll event
		midiScroller.getVerticalScrollBar().getModel().addChangeListener(new UpBarChangeListener());
		
		// Add scrollers to splitpane
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, keyScroller, midiScroller);
		splitPane.setDividerSize(0);

		toolbar = new Toolbar();
		midiTransport = new MidiTransport();

		setTitle("Bean MIDI: Editor");
		setLayout(new BorderLayout(2, 2));
		// setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//setIconImage(new ImageIcon(getClass().getResource(
				//"/resources/Auracle.png"), "Icon").getImage());

		// Menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(createToolsMenu());

		// Load frame
		add(toolbar, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		add(midiTransport, BorderLayout.PAGE_END);
		pack();
	}

	// File menu
	private JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.add(createFileNewItem());
		menu.add(saveMidiFile());
		return menu;
	}

	// New MIDI take
	private JMenuItem createFileNewItem() {
		JMenuItem menuItem = new JMenuItem("New");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.removeNotes();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	// Save MIDI file
	private JMenuItem saveMidiFile() {
		JMenuItem menuItem = new JMenuItem("Save");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.saveMidiFile();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	// Edit menu
	private JMenu createEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menu.add(createEditSelectAllItem());
		menu.add(createClearSelectedItem());
		menu.add(createCopySelectedItem());
		menu.add(createCutSelectedItem());
		menu.add(createPasteSelectedItem());
		return menu;
	}

	// Select all notes
	private JMenuItem createEditSelectAllItem() {
		JMenuItem menuItem = new JMenuItem("Select all");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.selectAll();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	// Clear selected notes
	private JMenuItem createClearSelectedItem() {
		JMenuItem menuItem = new JMenuItem("Delete");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.removeSelected();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	private JMenuItem createCopySelectedItem() {
		JMenuItem menuItem = new JMenuItem("Copy");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.copySelected();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	private JMenuItem createCutSelectedItem() {
		JMenuItem menuItem = new JMenuItem("Cut");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.cutSelected();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	private JMenuItem createPasteSelectedItem() {
		JMenuItem menuItem = new JMenuItem("Paste");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.pasteSelected();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	// File menu
	private JMenu createToolsMenu() {
		JMenu menu = new JMenu("Tools");
		menu.setMnemonic(KeyEvent.VK_T);
		menu.add(createToolsAlgorythmsMenu());
		return menu;
	}
	
	private JMenu createToolsAlgorythmsMenu() {
		JMenu menu = new JMenu("Algorythms");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.add(createAlgorythmsNewItem());
		menu.add(createAlgorythmsRepeatItem());
		return menu;
	}

	// New algorythm
	private JMenuItem createAlgorythmsNewItem() {
		JMenuItem menuItem = new JMenuItem("New");
		menuItem.setMnemonic(KeyEvent.VK_N);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.algorythms();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}
	
	// Repeat algorythms
	private JMenuItem createAlgorythmsRepeatItem() {
		JMenuItem menuItem = new JMenuItem("Repeat");
		menuItem.setMnemonic(KeyEvent.VK_R);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		class MenuItemListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				midiPane.algorythms();
			}
		}
		ActionListener listener = new MenuItemListener();
		menuItem.addActionListener(listener);
		return menuItem;
	}

	// Toolbar methods
	public void setChannel(String channel) {
		takeChannel = (Integer.parseInt(channel) - 1);
	}

	// Transport methods
	public void updateGrid() {
		midiPane.revalidate();
		midiPane.repaint();
	}

	public double getGridSize() {
		return gridSize;
	}

	public void setGridSize(double gs) {
		gridSize = gs;
	}

	public double getNoteSize() {
		return noteSize;
	}

	public void setNoteSize(double ns) {
		noteSize = ns;
	}

	// Sequencer methods
	public void play() {
		bms.play(notes, takeBPM, takeChannel);
	}

	public void pause() {
		bms.pause();
	}

	public void stop() {
		bms.stop();
	}
	
	class KeyPane extends JPanel implements MouseListener, MouseMotionListener {

		private static final long serialVersionUID = 1L;
		
		// Key position and octave position counter
		private int iKeyPos = 0;
		private int iOctPos = 0;

		public KeyPane() {

			// Set size of the MIDI pane
			setPreferredSize(new Dimension(40, 1280));
		}
			
		public BufferedImage drawBackground() {

			// Keyboard background
			Graphics2D g2 = null;

			BufferedImage image = (BufferedImage) createImage(
					(int) getGridSize(), 1280);
			g2 = image.createGraphics();

			for (int i = 127; i >= 0; i--) {
				// Checks to see which keys are sharp or flat
				switch (iKeyPos) {
				case 1:
				case 4:
				case 6:
				case 9:
					g2.setColor(Color.black);
					g2.setStroke(new BasicStroke(2));
					Rectangle2D rect = new Rectangle2D.Double(0, iOctPos,
							getGridSize(), 10);
					g2.draw(rect);
					g2.setPaint(Color.gray);
					g2.fill(rect);
					break;
				case 11:
					g2.setColor(Color.black);
					g2.setStroke(new BasicStroke(2));
					Rectangle2D rect2 = new Rectangle2D.Double(0, iOctPos,
							getGridSize(), 10);
					g2.draw(rect2);
					g2.setPaint(Color.gray);
					g2.fill(rect2);
					iKeyPos = -1;
					break;
				default:
					g2.setColor(Color.black);
					g2.setStroke(new BasicStroke(2));
					Rectangle2D rect3 = new Rectangle2D.Double(0, iOctPos,
							getGridSize(), 10);
					g2.draw(rect3);
					g2.setPaint(Color.white);
					g2.fill(rect3);
					break;
				}
				iKeyPos++;
				iOctPos += 10;
			}

			// Reset counters for repaint
			iKeyPos = 0;
			iOctPos = 0;

			g2.dispose();

			return image;
		}

		public void paintComponent(Graphics g) {

			Graphics2D g2 = (Graphics2D) g;
			
			int iOct = 9;

			// Draw background
			g2.drawImage(drawBackground(), 0, 0, takeLength, 1280, null);
			
			for (int y = 79; y <= 1280; y += 120)
			{
				g2.drawString("C" + iOct, 15, y);
				iOct -= 1;
			}

			}


		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}


	class MidiPane extends JPanel implements MouseListener, MouseMotionListener {

		private static final long serialVersionUID = 1L;

		// Key position and octave position counter
		private int iKeyPos = 0;
		private int iOctPos = 0;

		// Coordinates to store where note was added
		private int noteX = 0;
		private int noteY = 0;

		// Temporary comparison variables
		private MidiNote origNote;

		// MIDI cursor
		private Line2D midiCursor;

		// Cursor hit
		private boolean cursorHit = false;

		// Selection rectangle
		private Rectangle2D selectRect;
		private int selectRectStartX;
		private int selectRectStartY;

		// Font
		private Font velocityFont = new Font("Default", Font.PLAIN, 10);

		public MidiPane() {

			// Set size of the MIDI pane
			setPreferredSize(new Dimension(takeLength, 1280));

			// Vector to store notes
			notes = new Vector<MidiNote>();

			// Set initial position of MIDI cursor
			midiCursor = new Line2D.Double(0, 0, 0, 1280);

			// Handle mouse events
			addMouseListener(this);
			addMouseMotionListener(this);

			// MidiPane keybinds
			InputMap im = this.getInputMap();
			ActionMap am = this.getActionMap();

			// Toolbox keybinds

			// Note increase
			Action action = new TransportAction("noteIncrease");
			Object key = action.getValue(Action.NAME);
			KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0);
			im.put(keyStroke, key);
			am.put(key, action);

			action = new TransportAction("noteIncrease");
			key = action.getValue(Action.NAME);
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
					InputEvent.SHIFT_DOWN_MASK);
			im.put(keyStroke, key);
			am.put(key, action);

			// Note decrease
			action = new TransportAction("noteDecrease");
			key = action.getValue(Action.NAME);
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0);
			im.put(keyStroke, key);
			am.put(key, action);

			action = new TransportAction("noteDecrease");
			key = action.getValue(Action.NAME);
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
					InputEvent.SHIFT_DOWN_MASK);
			im.put(keyStroke, key);
			am.put(key, action);

			// Grid increase
			action = new TransportAction("gridIncrease");
			key = action.getValue(Action.NAME);
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0);
			im.put(keyStroke, key);
			am.put(key, action);

			// Grid decrease
			action = new TransportAction("gridDecrease");
			key = action.getValue(Action.NAME);
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0);
			im.put(keyStroke, key);
			am.put(key, action);

			// Play button
			action = new TransportAction("playButton");
			key = action.getValue(Action.NAME);
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
			im.put(keyStroke, key);
			am.put(key, action);

			setFocusable(true);

		}

		// Creates random notes
		public void algorythms() {

			Algorythms algo = new Algorythms();
			algo.setVisible(true);

			if (algo.getScale() != null) {
				int[] iScale = algo.getScale();

				int iNoteNo;
				int iRatio;
				int iExp = 0;
				int iVel;
				int iXPos = 0;
				double dSize;

				Random rNoteNo = new Random();
				Random rRatio = new Random();
				Random rVel = new Random();

				// Remove current notes
				// TODO Add option to merge
				removeNotes();

				while (iXPos < takeLength) {
					iNoteNo = rNoteNo.nextInt(iScale.length);

					iRatio = rRatio.nextInt(100);
					
					if (iRatio >= 0 && iRatio <= 4)
					{
						iExp = 0;
					}
					else if (iRatio >= 5 && iRatio <= 14) 
					{
						iExp = 1;
					}
					else if (iRatio >= 15 && iRatio <= 29)
					{
						iExp = 2;
					}
					else if (iRatio >= 30 && iRatio <= 49)
					{
						iExp = 3;
					}
					else if (iRatio >= 50 && iRatio <= 69)
					{
						iExp = 4;
					}
					else if (iRatio >= 70 && iRatio <= 84)
					{
						iExp = 5;
					}
					else if (iRatio >= 85 && iRatio <= 94)
					{
						iExp = 6;
					}
					else
					{
						iExp = 7;
					}
					
					dSize = Math.pow(2, iExp) * 10;

					iVel = rVel.nextInt(127) + 1;

					MidiNote note = new MidiNote(iXPos,
							(1270 - (iScale[iNoteNo] * 10)), dSize, 8, iVel);
					notes.add(note);

					iXPos += dSize;
				}

				repaint();
				revalidate();
			}

		}

		public BufferedImage drawBackground() {

			// Keyboard background
			Graphics2D g2 = null;

			BufferedImage image = (BufferedImage) createImage(
					(int) getGridSize(), 1280);
			g2 = image.createGraphics();

			for (int i = 127; i >= 0; i--) {
				// Checks to see which keys are sharp or flat
				switch (iKeyPos) {
				case 1:
				case 4:
				case 6:
				case 9:
					g2.setColor(Color.black);
					g2.setStroke(new BasicStroke(2));
					Rectangle2D rect = new Rectangle2D.Double(0, iOctPos,
							getGridSize(), 10);
					g2.draw(rect);
					g2.setPaint(Color.gray);
					g2.fill(rect);
					break;
				case 11:
					g2.setColor(Color.black);
					g2.setStroke(new BasicStroke(2));
					Rectangle2D rect2 = new Rectangle2D.Double(0, iOctPos,
							getGridSize(), 10);
					g2.draw(rect2);
					g2.setPaint(Color.gray);
					g2.fill(rect2);
					iKeyPos = -1;
					break;
				default:
					g2.setColor(Color.black);
					g2.setStroke(new BasicStroke(2));
					Rectangle2D rect3 = new Rectangle2D.Double(0, iOctPos,
							getGridSize(), 10);
					g2.draw(rect3);
					g2.setPaint(Color.white);
					g2.fill(rect3);
					break;
				}
				iKeyPos++;
				iOctPos += 10;
			}

			// Reset counters for repaint
			iKeyPos = 0;
			iOctPos = 0;

			g2.dispose();

			return image;
		}

		public void paintComponent(Graphics g) {

			Graphics2D g2 = (Graphics2D) g;

			// Draw background
			g2.drawImage(drawBackground(), 0, 0, takeLength, 1280, null);

			// Draw grid lines
			for (int x = (int) getGridSize(); x < takeLength; x += (int) getGridSize()) {
				g2.drawLine(x, 0, x, 1280);
			}

			// Draw MIDI cursor
			g2.setColor(Color.red);
			g2.draw(midiCursor);

			// Draw selection rectangle
			if (selectRect != null) {
				g2.setColor(new Color(20, 20, 20, 50));
				g2.fill(selectRect);
				g2.setColor(Color.white);
				g2.draw(selectRect);
			}

			// Paint added notes
			MidiNote note;
			for (int y = 0; y < notes.size(); y++) {
				note = notes.elementAt(y);
				g2.setColor(note.getColor());
				g2.fill(note);
				g2.setColor(Color.black);
				g2.draw(note);
				g2.setFont(velocityFont);
				g2.drawString(note.VelocityString(), (int) (note.getX() + 1),
						(int) (note.getY() + 8));
			}
		}

		// Mouse select
		public void select(double x, double y, double w, double h) {

			// Clear selected notes first
			clearSelected();

			MidiNote rect;
			for (int z = 0; z < notes.size(); z++) {
				rect = notes.elementAt(z);

				if (rect.getX() >= x && rect.getY() >= y
						&& rect.getX() < (w + x) && rect.getY() < (h + y)) {
					rect.setSelected(true);
				}
			}
		}

		// Selects all notes
		public void selectAll() {

			boolean allSelected;
			int selectCnt = 0;

			for (int x = 0; x < notes.size(); x++) {
				if (notes.elementAt(x).isSelected()) {
					selectCnt++;
				}
			}

			if (selectCnt == notes.size()) {
				allSelected = true;
			} else {
				allSelected = false;
			}

			if (!allSelected) {
				for (int x = 0; x < notes.size(); x++) {
					notes.elementAt(x).setSelected(true);
				}
			} else {
				for (int x = 0; x < notes.size(); x++) {
					notes.elementAt(x).setSelected(false);
				}
			}
			revalidate();
			repaint();
		}

		// Clears selected notes
		public void clearSelected() {
			for (int x = 0; x < notes.size(); x++) {
				notes.elementAt(x).setSelected(false);
			}
		}

		// Remove selected notes
		public void removeSelected() {
			for (int x = 0; x < notes.size(); x++) {
				if (notes.elementAt(x).isSelected() == true) {
					notes.remove(notes.elementAt(x));
					x--;
				}
			}
			this.revalidate();
			this.repaint();
		}

		// Copy selected notes
		public void copySelected() {
			notesBuf = new Vector<MidiNote>();

			for (int x = 0; x < notes.size(); x++) {
				if (notes.elementAt(x).isSelected() == true) {
					notesBuf.add(notes.elementAt(x));
				}
			}
		}

		// Cut selected notes
		public void cutSelected() {
			notesBuf = new Vector<MidiNote>();

			for (int x = 0; x < notes.size(); x++) {
				if (notes.elementAt(x).isSelected() == true) {
					notesBuf.add(notes.elementAt(x));
					notes.remove(notes.elementAt(x));
					x--;
				}
			}
			this.revalidate();
			this.repaint();
		}

		// Paste selected notes
		public void pasteSelected() {
			if (notesBuf.size() > 0) {

				// Unselect previous notes whether copying or cutting
				for (int x = 0; x < notes.size(); x++) {
					notes.elementAt(x).setSelected(false);
				}

				// Retains original x-axis spacing
				for (int x = 0; x < notesBuf.size(); x++) {
					MidiNote note;
					note = (MidiNote) notesBuf.elementAt(x).clone();

					if (note.getX() > notesBuf.elementAt(0).getX()) {
						note.x = mosPosX
								+ (note.getX() - notesBuf.elementAt(0).getX());
					} else {
						note.x = mosPosX;
					}
					note.setLength(note.getWidth() + note.getX());

					// Check to see if notes are within take
					// add if they are
					if (note.getX() < takeLength) {
						note.setSelected(true);

						// Protect against duplicate notes, then add
						if (!notes.contains(note)) {
							notes.add(note);
						}

					}
				}

				this.revalidate();
				this.repaint();
			}

		}

		// Remove all notes
		public void removeNotes() {
			notes.clear();
			this.revalidate();
			this.repaint();
		}

		// Save MIDI file
		public void saveMidiFile() {

			// File to be saved
			File selectedFile = null;

			// Create save dialog object
			JFileChooser chooser = new JFileChooser();

			// Apply MIDI file filters
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"MIDI File", "mid", "midi");
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);

			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				// Check to make sure file is saved with proper extension
				if (!(chooser.getSelectedFile().getAbsolutePath()
						.endsWith(".mid") || chooser.getSelectedFile()
						.getAbsolutePath().endsWith(".midi"))) {
					selectedFile = new File(chooser.getSelectedFile() + ".mid");
				}

				// Ask sequencer to save
				bms.save(selectedFile, notes, takeChannel, takeBPM);
			}
		}

		public Line2D getMidiCursor() {
			return midiCursor;
		}

		public void setMidiCursor(double x1, double y1, double x2, double y2) {
			Line2D newCursorPos = new Line2D.Double(x1, y1, x2, y2);
			this.midiCursor = newCursorPos;
		}

		public void mouseClicked(MouseEvent e) {

			// Current mouse position
			double curX = e.getX();
			int curY = e.getY() / 10;

			// Gridlock
			noteY = curY * 10;

			// Store
			mosPosX = (int) (e.getX() / getGridSize());
			mosPosX = (int) (mosPosX * getGridSize());

			MidiNote rect = null;

			// Find note on single click
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				// Select
				for (int x = 0; x < notes.size(); x++) {
					rect = notes.elementAt(x);
					if (curX >= rect.getX() && curX <= rect.getLength()
							&& noteY == (int) rect.getY()) {

						// Cursor inside note
						cursorHit = true;
					}
				}

				// Select note
				if (cursorHit == true) {
					for (int x = 0; x < notes.size(); x++) {
						rect = notes.elementAt(x);
						if (curX >= rect.getX() && curX <= rect.getLength()
								&& noteY == (int) rect.getY()) {
							if (rect.isSelected() == false) {
								rect.setSelected(true);
							} else {
								rect.setSelected(false);
							}
						}
					}

					// Reset cursor
					cursorHit = false;
				} else {
					for (int x = 0; x < notes.size(); x++) {
						notes.elementAt(x).setSelected(false);
					}

					// Set MIDI cursor position
					double midiCurNewX = (int) (e.getX() / getGridSize());
					int midiCurX = (int) (midiCurNewX * getGridSize());
					setMidiCursor(midiCurX, 0, midiCurX, 1280);
				}
				revalidate();
				repaint();
			}

			// Find note on double click
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {

				boolean noteExists = false;

				// Remove if note exists
				for (int x = 0; x < notes.size(); x++) {
					rect = notes.elementAt(x);
					if (curX >= rect.getX() && curX <= rect.getLength()
							&& noteY == (int) rect.getY()) {
						notes.remove(x);
						noteExists = true;
					}
				}

				// Add if note doesn't exist
				if (noteExists == false) {
					curX = (int) (e.getX() / getGridSize());
					noteX = (int) (curX * getGridSize());
					rect = new MidiNote(noteX, noteY, getNoteSize(), 8, 127);
					notes.addElement(rect);
				}
			}

			// Sort to make sure note is stored relative to its x and y position
			Collections.sort(notes);

			this.repaint();

		}

		public void mouseDragged(MouseEvent e) {
			// Current mouse position
			int curX = (int) (e.getX() / getGridSize());
			int curY = e.getY() / 10;

			// Gridlock
			noteX = (int) (curX * getGridSize());
			noteY = curY * 10;

			// Selection counter
			int iCnt = 0;

			if (origNote != null && !e.isControlDown()) {
				if (noteX >= 0 && noteX < takeLength && noteY < 1280
						&& noteY >= 0) {

					// Move notes relative to first note
					for (int x = 0; x < notes.size(); x++) {
						MidiNote note = notes.elementAt(x);
						if (note.isSelected()) {
							iCnt += 1;

							if (iCnt == 1) {
								note.setX(noteX);
								note.setY(noteY);
								note.setLength(note.getWidth() + note.getX());
							} else {
								// TODO have farthest note hit a "wall"
								if (note.getX() < takeLength) {
									note.setX(noteX + note.getXDif());
									note.setLength(note.getWidth()
											+ note.getX());
								} else {
									notes.remove(note);
								}

								note.setY(noteY - note.getYDif());

								// Verify notes do not go outside of editor
								if (note.getY() < 0) {
									note.setY(0);
								} else if (note.getY() > 1270) {
									note.setY(1270);
								}
							}
						}
					}
				}

				// Scroll on drag
				Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
				scrollRectToVisible(r);
			} else if (origNote != null && e.isControlDown()) {
				// Change velocity
				// NOTE: Add code for mouse position relative to boundaries
				if (e.getY() > origNote.getY()) {
					if (origNote.getVelocity() > 1) {
						origNote.setVelocity(origNote.getVelocity() - 1);
					}
				} else if (e.getY() < origNote.getY()) {
					if (origNote.getVelocity() < 127) {
						origNote.setVelocity(origNote.getVelocity() + 1);
					}
				}

				if (origNote.isSelected()) {
					origNote.setSelected(true);
				}
				// Change velocity of all selected notes
				for (int x = 0; x < notes.size(); x++) {
					if (notes.elementAt(x).isSelected()
							&& notes.elementAt(x) != origNote) {
						if (e.getY() > notes.elementAt(x).getY()) {
							if (notes.elementAt(x).getVelocity() > 1) {
								notes.elementAt(x).setVelocity(
										notes.elementAt(x).getVelocity() - 1);
							}
						} else if (e.getY() < notes.elementAt(x).getY()) {
							if (notes.elementAt(x).getVelocity() < 127) {
								notes.elementAt(x).setVelocity(
										notes.elementAt(x).getVelocity() + 1);
							}
						}
						if (notes.elementAt(x).isSelected()) {
							notes.elementAt(x).setSelected(true);
						}
					}
				}
			} else {
				// Position selection rectangle
				if (e.getX() > selectRectStartX && e.getY() > selectRectStartY) {
					selectRect = new Rectangle2D.Double(selectRectStartX,
							selectRectStartY, e.getX() - selectRectStartX,
							e.getY() - selectRectStartY);
				} else if (e.getX() < selectRectStartX
						&& e.getY() < selectRectStartY) {
					selectRect = new Rectangle2D.Double(e.getX(), e.getY(),
							selectRectStartX - e.getX(), selectRectStartY
									- e.getY());
				} else if (e.getX() < selectRectStartX
						&& e.getY() > selectRectStartY) {
					selectRect = new Rectangle2D.Double(e.getX(),
							selectRectStartY, selectRectStartX - e.getX(),
							e.getY() - selectRectStartY);
				} else if (e.getX() > selectRectStartX
						&& e.getY() < selectRectStartY) {
					selectRect = new Rectangle2D.Double(selectRectStartX,
							e.getY(), e.getX() - selectRectStartX,
							selectRectStartY - e.getY());
				}

				// Scroll on drag
				Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
				scrollRectToVisible(r);
			}

			this.revalidate();
			this.repaint();

		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			// Reset note
			origNote = null;

			// Note to check
			MidiNote note;

			// Current cursor position
			double curX = e.getX();
			int curY = e.getY() / 10;

			// Gridlock
			noteY = curY * 10;

			// Create copy of the note
			for (int x = 0; x < notes.size(); x++) {
				note = notes.elementAt(x);
				if (curX >= note.getX() && curX <= note.getLength()
						&& noteY == (int) note.getY()) {
					origNote = note;
					break;
				} else {
					origNote = null;
				}
			}

			int selCnt = 0;

			// Set the x & y difference of selected notes
			for (int x = 0; x < notes.size(); x++) {
				if (notes.elementAt(x).isSelected()) {
					selCnt += 1;
					if (selCnt == 1) {
						firstNoteX = notes.elementAt(x).getX();
						firstNoteY = notes.elementAt(x).getY();
					} else {
						notes.elementAt(x).setXDif(
								notes.elementAt(x).getX() - firstNoteX);
						notes.elementAt(x).setYDif(
								firstNoteY - notes.elementAt(x).getY());
					}
				}
			}

			// Selection rectangle starting positions
			selectRectStartX = (int) ((int) (e.getX() / getGridSize()) * getGridSize());
			selectRectStartY = (int) ((e.getY() / 10) * 10) - 1;

		}

		public void mouseReleased(MouseEvent e) {

			// Select notes
			if (selectRect != null) {
				select(selectRect.getX(), selectRect.getY(),
						selectRect.getWidth(), selectRect.getHeight());
			}

			// Remove selection
			selectRect = null;

			this.repaint();

		}

		public void mouseMoved(MouseEvent e) {
		}

		class MidiPaneAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public MidiPaneAction(String name) {
				putValue(Action.NAME, name);
			}

			public void actionPerformed(ActionEvent e) {
				if (getValue(Action.NAME) == "clearSelected") {
					removeSelected();
				}

			}
		}

		class TransportAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public TransportAction(String name) {
				putValue(Action.NAME, name);
			}

			public void actionPerformed(ActionEvent e) {
				// Increase note size
				if (getValue(Action.NAME) == "noteIncrease") {
					if (midiTransport.comboNoteSize.getSelectedIndex() < (midiTransport.comboNoteSize
							.getItemCount() - 1)) {
						midiTransport.comboNoteSize
								.setSelectedIndex(midiTransport.comboNoteSize
										.getSelectedIndex() + 1);
					}
				}
				// Decrease note size
				if (getValue(Action.NAME) == "noteDecrease") {
					if (midiTransport.comboNoteSize.getSelectedIndex() > 0) {
						midiTransport.comboNoteSize
								.setSelectedIndex(midiTransport.comboNoteSize
										.getSelectedIndex() - 1);
					}
				}

				// Increase grid size
				if (getValue(Action.NAME) == "gridIncrease") {
					if (midiTransport.comboGridSize.getSelectedIndex() < (midiTransport.comboGridSize
							.getItemCount() - 1)) {
						midiTransport.comboGridSize
								.setSelectedIndex(midiTransport.comboGridSize
										.getSelectedIndex() + 1);
					}
				}

				// Decrease grid size
				if (getValue(Action.NAME) == "gridDecrease") {
					if (midiTransport.comboGridSize.getSelectedIndex() > 0) {
						midiTransport.comboGridSize
								.setSelectedIndex(midiTransport.comboGridSize
										.getSelectedIndex() - 1);
					}
				}

				if (getValue(Action.NAME) == "playButton") {
					play();
				}
			}
		}
	}

	class Toolbar extends JPanel implements ItemListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JComboBox<String> channelCombo;
		private JLabel channelLbl;

		public Toolbar() {

			// Channel label
			channelLbl = new JLabel("Channel: ");
			// Channel combo
			channelCombo = new JComboBox<String>();

			// Channel listener
			channelCombo.addItemListener(this);

			for (int i = 1; i < 17; i++) {
				channelCombo.addItem(Integer.toString(i));
			}

			// Set default channel
			channelCombo.setSelectedItem("1");
			channelCombo.setFocusable(false);

			add(channelLbl);
			add(channelCombo);
		}

		@Override
		public void itemStateChanged(ItemEvent item) {
			setChannel(item.getItem().toString());
		}
	}

	class MidiScroller extends JScrollPane {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MidiScroller(Component midiPane) {
			super(midiPane);
			// Set size of scroll window
			setPreferredSize(new Dimension(800, 600));

			// Set how fast to scroll
			getHorizontalScrollBar().setUnitIncrement(800);
			getVerticalScrollBar().setUnitIncrement(120);
			

		}


	}
	

	
	class KeyScroller extends JScrollPane {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public KeyScroller(Component keyPane) {
			super(keyPane);
			// Set size of scroll window
			setPreferredSize(new Dimension(40, 1280));

			// Set how fast to scroll
			getHorizontalScrollBar().setUnitIncrement(800);
			getVerticalScrollBar().setUnitIncrement(120);
			
			// TODO: Find a way to remove scrollbar in key scroller but keep layout
//			setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		}
	}

	class MidiTransport extends JPanel implements ItemListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private JComboBox<String> comboGridSize;
		private JComboBox<String> comboNoteSize;

		public MidiTransport() {

			// Transport labels
			JLabel labelGrid = new JLabel("Grid: ");
			JLabel labelNote = new JLabel("Note: ");

			// Transport buttons
			ImageIcon playIcon = new ImageIcon(getClass().getResource(
					"/resources/play.png"), "Play");
			ImageIcon pauseIcon = new ImageIcon(getClass().getResource(
					"/resources/pause.png"), "Pause");
			ImageIcon stopIcon = new ImageIcon(getClass().getResource(
					"/resources/stop.png"), "Stop");

			// Play button
			JButton playButton = new JButton(playIcon);

			class PlayListener implements ActionListener {
				public void actionPerformed(ActionEvent event) {
					play();
				}
			}
			ActionListener playListener = new PlayListener();
			playButton.addActionListener(playListener);

			// Pause button
			JButton pauseButton = new JButton(pauseIcon);

			class PauseListener implements ActionListener {
				public void actionPerformed(ActionEvent event) {
					pause();
				}
			}
			ActionListener pauseListener = new PauseListener();
			pauseButton.addActionListener(pauseListener);

			// Stop button
			JButton stopButton = new JButton(stopIcon);

			class StopListener implements ActionListener {
				public void actionPerformed(ActionEvent event) {
					stop();
				}
			}
			ActionListener stopListener = new StopListener();
			stopButton.addActionListener(stopListener);

			// Keep focus on grid and note selection
			playButton.setFocusable(false);
			pauseButton.setFocusable(false);
			stopButton.setFocusable(false);

			// Grid combo
			comboGridSize = new JComboBox<String>();
			comboGridSize.addItem("1/128");
			comboGridSize.addItem("1/64");
			comboGridSize.addItem("1/32");
			comboGridSize.addItem("1/16");
			comboGridSize.addItem("1/8");
			comboGridSize.addItem("1/4");
			comboGridSize.addItem("1/2");
			comboGridSize.addItem("1");

			// Grid size listener
			comboGridSize.addItemListener(this);

			// Set default grid size
			comboGridSize.setSelectedItem("1/32");

			// Do not allow focus on combo grid (for keybinds)
			comboGridSize.setFocusable(false);

			// Note combo
			comboNoteSize = new JComboBox<String>();
			comboNoteSize.addItem("1/128");
			comboNoteSize.addItem("1/64");
			comboNoteSize.addItem("1/32");
			comboNoteSize.addItem("1/16");
			comboNoteSize.addItem("1/8");
			comboNoteSize.addItem("1/4");
			comboNoteSize.addItem("1/2");
			comboNoteSize.addItem("1");

			// Note size listener
			comboNoteSize.addItemListener(this);

			// Sets default note size
			comboNoteSize.setSelectedItem("1/32");

			// Do not allow focus on combo note (for keybinds)
			comboNoteSize.setFocusable(false);

			// Add controls
			add(playButton);
			add(pauseButton);
			add(stopButton);
			add(labelGrid);
			add(comboGridSize);
			add(labelNote);
			add(comboNoteSize);
		}

		public void gridSize(String gS) {
			switch (gS) {
			case "1/128":
				setGridSize(10);
				break;
			case "1/64":
				setGridSize(20);
				break;
			case "1/32":
				setGridSize(40);
				break;
			case "1/16":
				setGridSize(80);
				break;
			case "1/8":
				setGridSize(160);
				break;
			case "1/4":
				setGridSize(320);
				break;
			case "1/2":
				setGridSize(640);
				break;
			case "1":
				setGridSize(1280);
				break;
			}

			// Call to repaint the grid with new snap size
			updateGrid();
		}

		public void noteSize(String nS) {
			switch (nS) {
			case "1/128":
				setNoteSize(10);
				break;
			case "1/64":
				setNoteSize(20);
				break;
			case "1/32":
				setNoteSize(40);
				break;
			case "1/16":
				setNoteSize(80);
				break;
			case "1/8":
				setNoteSize(160);
				break;
			case "1/4":
				setNoteSize(320);
				break;
			case "1/2":
				setNoteSize(640);
				break;
			case "1":
				setNoteSize(1280);
				break;
			}
		}

		public void itemStateChanged(ItemEvent item) {
			if (item.getSource() == comboGridSize) {
				gridSize(item.getItem().toString());
			} else if (item.getSource() == comboNoteSize) {
				noteSize(item.getItem().toString());
			}
		}
	}

}