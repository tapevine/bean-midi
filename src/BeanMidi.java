public class BeanMidi
{
	
	
	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void createAndShowGUI()
	{
		BeanMidiFrame bmf = new BeanMidiFrame();
		bmf.setVisible(true);
	}
	

}

