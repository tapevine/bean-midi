import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class About extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public About() {
		setSize(400, 250);
		setTitle("Bean MIDI: About");
		setIconImage(new ImageIcon(getClass().getResource(
				"/resources/icon.png"), "Icon").getImage());
		createNorthPanel();
		createCenterPanel();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}

	public void createNorthPanel() {
		JPanel panel = new JPanel();
		ImageIcon beanIcon = new ImageIcon(getClass().getResource(
				"/resources/icon.png"), "BeanMidi");

		JLabel aboutIcon = new JLabel(beanIcon);

		panel.add(aboutIcon);
		add(panel, BorderLayout.NORTH);
	}
	
	public void createCenterPanel() {
		JPanel panel = new JPanel();


		
		StyleContext context = new StyleContext();
		StyledDocument document = new DefaultStyledDocument(context);
		Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
		
		StyleConstants.setFontSize(style, 14);
		StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
		StyleConstants.setSpaceAbove(style, 1);
		StyleConstants.setSpaceBelow(style, 1);
		
	    SimpleAttributeSet attributes = new SimpleAttributeSet();
	    attributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

		try {
			document.insertString(document.getLength(), "Author: Ryan Olejnik", attributes);
			document.insertString(document.getLength(), System.lineSeparator(), attributes);
			document.insertString(document.getLength(), "Company: Tapevine", attributes);
			document.insertString(document.getLength(), System.lineSeparator(), attributes);
			document.insertString(document.getLength(), "Version: 0.01", style);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JTextPane aboutTxt = new JTextPane(document);
		aboutTxt.setPreferredSize(new Dimension(250, 100));
		aboutTxt.setEditable(false);
		aboutTxt.setOpaque(false);

		panel.add(aboutTxt);
		add(panel, BorderLayout.CENTER);
	}

}
