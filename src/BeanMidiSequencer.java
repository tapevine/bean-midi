import javax.sound.midi.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class BeanMidiSequencer {

	// 128th, 64th, 32nd, 16th, 8th, 4th, 2nd, whole, 2 bars, 3 bars, 4 bars
	int[] tickTimes = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};

	// Create a MIDI file to use
	private File midiFile = null;
	private Vector<MidiNote> notes;

	// MIDI devices for realtime playback and recording
	static MidiDevice inDev;
	static MidiDevice outDev;

	// Reserve the sequencer
	private static Sequencer sequencer;
	
	// Play position for realtime MIDI cursor
	private long lPlayPos;
	
	// Pause position
	private long lPausePos;
	
	// Boolean to see if playing
	public boolean isPlaying;

	public ArrayList<String> getInDevices()
	{
		// Show list of devices in main window
		MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
		ArrayList<String> aInDevices = new ArrayList<String>();


		if (aInfos.length > 0)
		{
			for (int x = 0; x < aInfos.length; x++)
			{
				try {
					MidiDevice	device = MidiSystem.getMidiDevice(aInfos[x]);
					if (device.getMaxTransmitters() != 0)
					{
						aInDevices.add(aInfos[x].getName());
					}
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}

			}
		}

		if (aInDevices.size() == 0)
		{
			aInDevices.add("No device");
		}

		return aInDevices;
	}

	public ArrayList<String> getOutDevices()
	{
		// Show list of devices in main window
		MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
		ArrayList<String> aOutDevices = new ArrayList<String>();


		if (aInfos.length > 0)
		{
			for (int x = 0; x < aInfos.length; x++)
			{
				try {
					MidiDevice	device = MidiSystem.getMidiDevice(aInfos[x]);
					if (device.getMaxReceivers() != 0)
					{
						aOutDevices.add(aInfos[x].getName());
					}
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}

			}
		}

		if (aOutDevices.size() == 0)
		{
			aOutDevices.add("No device");
		}

		return aOutDevices;
	}

	public void setInDevice(String sDevName)
	{

		// Close any open devices
		if (inDev != null && inDev.isOpen() == true)
		{
			inDev.close();
		}

		MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
		for (int x = 0; x < aInfos.length; x++)
		{
			if (aInfos[x].getName().equals(sDevName))
			{
				try
				{
					inDev = MidiSystem.getMidiDevice(aInfos[x]);
				}
				catch (MidiUnavailableException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void setOutDevice(String sDevName)
	{

		// Close any open devices
		if (outDev != null && outDev.isOpen() == true)
		{
			outDev.close();
		}

		MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
		for (int x = 0; x < aInfos.length; x++)
		{
			if (aInfos[x].getName().equals(sDevName))
			{
				try
				{
					outDev = MidiSystem.getMidiDevice(aInfos[x]);
				}
				catch (MidiUnavailableException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void setDefaultInDevice()
	{
		try
		{
			ArrayList<String> aInDev = new ArrayList<String>();
			aInDev = getInDevices();
			setInDevice(aInDev.get(0));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void setDefaultOutDevice()
	{
		try
		{
			ArrayList<String> aOutDev = new ArrayList<String>();
			aOutDev = getOutDevices();
			setOutDevice(aOutDev.get(0));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void save(File mf, Vector<MidiNote> n, int channel, int bpm)
	{
		midiFile = mf;
		notes = n;

		// Create a new sequence
		Sequence sequence = null;

		try
		{
			// PPQ 32 = 128th
			sequence = new Sequence(Sequence.PPQ, 32);
		}
		catch (InvalidMidiDataException ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}


		// Create a MIDI track
		Track track = sequence.createTrack();


		// Declare important sequence variables
		long tickPos;
		int noteLen, note;
		
		// TODO Midi meta message code
		track.add(tempoEvent(bpm, 0));
		

		for (int x = 0; x < notes.size(); x++)
		{	
			note = (int) (127 - (notes.elementAt(x).getY() / 10));
			tickPos = (int) (notes.elementAt(x).getX() / 10);
			noteLen = (int) (notes.elementAt(x).getWidth() / 10);

			track.add(noteOn(note, channel, notes.elementAt(x).getVelocity(), tickPos));
			track.add(noteOff(note, channel, tickPos + noteLen));
		}

		try
		{
			MidiSystem.write(sequence, 1, midiFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void play(Vector<MidiNote> n, int bpm, int channel)
	{
		notes = n;

		// Create a new sequence
		Sequence sequence = null;

		try
		{
			// PPQ 32 = 128th
			sequence = new Sequence(Sequence.PPQ, 24);
		}
		catch (InvalidMidiDataException ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}


		// Create a MIDI track
		Track track = sequence.createTrack();


		// Declare important sequence variables
		long tickPos;
		int noteLen, note;

		for (int x = 0; x < notes.size(); x++)
		{	
			note = (int) (127 - (notes.elementAt(x).getY() / 10));
			tickPos = (int) (notes.elementAt(x).getX() / 10);
			noteLen = (int) (notes.elementAt(x).getWidth() / 10);

			track.add(noteOn(note, channel, notes.elementAt(x).getVelocity(), tickPos));
			track.add(noteOff(note, channel, tickPos + noteLen));
		}


		// Open device (will try to open a default one if one wasn't selected)
		try {

			if (outDev == null)
			{
				setDefaultOutDevice();
			}

			if (outDev.isOpen() == false)
			{
				outDev.open();
			}

		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}

		// Get and open the sequencer if it isn't
		try{
			if (sequencer == null)
			{
				sequencer = MidiSystem.getSequencer();
				sequencer.open();
			}
		}
		catch (MidiUnavailableException e)
		{
			e.printStackTrace();
		}


		// Give the sequence
		try
		{

			sequencer.setSequence(sequence);
			sequencer.setTempoInBPM(bpm);
		}
		catch (InvalidMidiDataException e)
		{
			e.printStackTrace();
		}

		// Hookup sequencer to devices and start it
		try
		{
			Receiver	midiReceiver = outDev.getReceiver();
			Transmitter	midiTransmitter = sequencer.getTransmitter();
			midiTransmitter.setReceiver(midiReceiver);
			//			sequencer.setSlaveSyncMode(SyncMode.MIDI_SYNC);
			
			// Resume paused position
			if (lPausePos > 0)
			{
				sequencer.setTickPosition(lPausePos);
				lPausePos = 0;
			}
			
			sequencer.start();
			
////			while (sequencer.isRunning())
////			{
////				lPlayPos = sequencer.getTickPosition();
////				isPlaying = true;
////			}
//
//			// Reset play position
//			lPlayPos = 0;
//			
//			// Sequencer has stopped
//			isPlaying = false;


			midiTransmitter.close();
			midiReceiver.close();
		}
		catch (MidiUnavailableException e)
		{
			e.printStackTrace();
		}

	}

	public void pause()
	{
		lPausePos = sequencer.getTickPosition();
		sequencer.stop();
	}

	public void stop()
	{
		sequencer.stop();
	}
	
	public long playPos()
	{
		return lPlayPos;
	}

	private static MidiEvent noteOn(int note, int channel, int velocity, long tick)
	{
		return noteEvent(ShortMessage.NOTE_ON,
				note,
				channel,
				velocity,
				tick);
	}



	private static MidiEvent noteOff(int note, int channel, long tick)
	{
		return noteEvent(ShortMessage.NOTE_OFF,
				note,
				channel,
				0,
				tick);
	}

	private static MidiEvent noteEvent(int command, int note, int channel, int velocity, 
			long lTick)
	{

		ShortMessage	message = new ShortMessage();

		try
		{
			message.setMessage(command,
					channel,
					note,
					velocity);
		}
		catch (InvalidMidiDataException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		MidiEvent	event = new MidiEvent(message,
				lTick);
		return event;
	}
	

	// TODO Set tempo for midi file
	private static MidiEvent tempoEvent(int iBPM, long lTick)
	{
		MetaMessage message = new MetaMessage();
		
		
		
		int i = 127;
		byte b = (byte) i;
		
		byte[] data = { b };
		
		
		
		//javax.xml.bind.DatatypeConverter.parseHexBinary("C8");
		
		try {
			message.setMessage(81, data, data.length);

		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MidiEvent event = new MidiEvent(message, lTick);
		return event;
	}



}
