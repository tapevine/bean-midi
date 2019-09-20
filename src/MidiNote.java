import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class MidiNote extends Rectangle2D.Double implements Comparable<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int velocity;
	private Color color;
	private double length;
	private double xDif;
	private double yDif;
	private boolean isSelected;
	
    public MidiNote(double x, double y, double width, double height, int velocity){

    	super(x, y, width, height);
        this.setVelocity(velocity);
        this.length = x + width;
        
		if (velocity >= 96 && velocity <= 127)
		{
			this.setColor(Color.red);
		}
		else if (velocity >= 62 && velocity <= 95)
		{
			this.setColor(Color.green);
		}
		else if (velocity >= 32 && velocity <= 61)
		{
			this.setColor(Color.orange);
		}
		else
		{
			this.setColor(Color.yellow);
		}
    }


	public int getVelocity() {
		return velocity;
	}


	public void setVelocity(int velocity) {
		this.velocity = velocity;
		
		if (velocity >= 96 && velocity <= 127)
		{
			this.setColor(Color.red);
		}
		else if (velocity >= 62 && velocity <= 95)
		{
			this.setColor(Color.green);
		}
		else if (velocity >= 32 && velocity <= 61)
		{
			this.setColor(Color.orange);
		}
		else
		{
			this.setColor(Color.yellow);
		}
	}
	
	public String VelocityString(){
		return Integer.toString(velocity);
	}


	public Color getColor() {
		return color;
	}


	public void setColor(Color color) {
		this.color = color;
	}


	public double getLength() {
		return length;
	}


	public void setLength(double length) {
		this.length = length;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}


	public boolean isSelected() {
		return isSelected;
	}


	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		if (isSelected == true) {
			this.setColor(Color.magenta);
		} else {
			if (this.velocity >= 96 && this.velocity <= 127)
			{
				this.setColor(Color.red);
			}
			else if (this.velocity >= 62 && this.velocity <= 95)
			{
				this.setColor(Color.green);
			}
			else if (this.velocity >= 32 && this.velocity <= 61)
			{
				this.setColor(Color.orange);
			}
			else
			{
				this.setColor(Color.yellow);
			}
		}
	}
	
	public void setXDif(double diff)
	{
		this.xDif = diff;
	}
	
	public double getXDif()
	{
		return this.xDif;
	}
	
	public void setYDif(double diff)
	{
		this.yDif = diff;
	}
	
	public double getYDif()
	{
		return this.yDif;
	}

	public int compareTo(Object otherObj) {
		// Used for sorting
		MidiNote otherNote = (MidiNote) otherObj;
		if (x < otherNote.x) return -1;
		if (x == otherNote.x) return 0;
		return 1;
	}

}