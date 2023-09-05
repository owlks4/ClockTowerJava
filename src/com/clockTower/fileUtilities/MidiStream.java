package com.clockTower.fileUtilities;

import com.clockTower.Utility.MIDIPlayer;
import com.clockTower.Utility.Utility;
import org.apache.commons.io.FileUtils;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;


public class MidiStream   //The MDS format used by Clock Tower
{
    public byte[] filebytes;

    public Hashtable<Short, Byte> LengthsOfMIDIEvents = new Hashtable<Short, Byte>();
    //these are the lengths of the midi events when written to standard midi files

    public MidiStream() {
        LengthsOfMIDIEvents.put((short) 0x80, (byte) 3);
        LengthsOfMIDIEvents.put((short) 0x90, (byte) 3);
        LengthsOfMIDIEvents.put((short) 0xA0, (byte) 3);
        LengthsOfMIDIEvents.put((short) 0xB0, (byte) 3);
        LengthsOfMIDIEvents.put((short) 0xC0, (byte) 2);
        LengthsOfMIDIEvents.put((short) 0xD0, (byte) 2);
        LengthsOfMIDIEvents.put((short) 0xE0, (byte) 3);
        //and F0 is a special case, but is unlikely to be encountered here
    }


    public class MDSBlock {
        public int absoluteTickOffset;
        public int blockSize;
        public LinkedList<MDSEvent> mdsEvents = new LinkedList<MDSEvent>();
    }

    public class MDSEvent {
        public int deltaTicks;
        public int streamID;
        public byte[] eventValue;
    }

    public void SetVolume(int i) {
        if (MIDIPlayer.sequencer == null) {
            try {
                MIDIPlayer.LoadSequencer();
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        MIDIPlayer.setVol(i);
    }

    public void Play(boolean loop) {

        if (filebytes == null || filebytes.length == 0) {
            JOptionPane.showMessageDialog(null, "You need to load a MDS file before you can convert and play it!");
        }

        MIDIPlayer.Play(this, loop);
    }

    public void Stop() {
        MIDIPlayer.sequencer.stop();
    }

    public void LoadFromPath(String path) {
        File f = new File(path);

        if (f.exists()) {
            try {
                filebytes = FileUtils.readFileToByteArray(new File(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            filebytes = MDS_to_MIDI();
        } else {
            JOptionPane.showMessageDialog(null, "The MDS file " + path + " was not found.");
        }
    }


    private byte[] MDS_to_MIDI() {

        if (filebytes == null || filebytes.length == 0) {
            JOptionPane.showMessageDialog(null, "Cannot convert the MDS file to MIDI; there is no MDS file loaded.");
        }

        LinkedList<Byte> output = new LinkedList<Byte>();

        String magic = "" + (char) filebytes[0] + (char) filebytes[1] + (char) filebytes[2] + (char) filebytes[3];

        if (magic == "MThd") { //this is just a normal midi... return it as-is.
            return filebytes;
        }

        int pos = 0x14;

        int TimeFormat = BitConverter.ToInt32(filebytes, pos);
        pos += 4;

        int MaxBuffer = BitConverter.ToInt32(filebytes, pos);
        pos += 4;

        int FormatFlags = BitConverter.ToInt32(filebytes, pos);
        pos += 4;

        pos += 4; //skip "data" header

        int dataLength = BitConverter.ToInt32(filebytes, pos);
        pos += 4;

        int numBlocks = BitConverter.ToInt32(filebytes, pos);
        pos += 4;

        LinkedList<MDSBlock> blocks = new LinkedList<MDSBlock>();

        int numTracks = 0;

        for (int i = 0; i < numBlocks; i++) {
            MDSBlock newBlock = new MDSBlock();

            newBlock.absoluteTickOffset = BitConverter.ToInt32(filebytes, pos);
            pos += 4;

            newBlock.blockSize = BitConverter.ToInt32(filebytes, pos);
            pos += 4;

            int startPoint = pos;
            newBlock.mdsEvents = new LinkedList<MDSEvent>();

            while (pos < startPoint + newBlock.blockSize) {
                MDSEvent newEvent = new MDSEvent();

                newEvent.deltaTicks = BitConverter.ToInt32(filebytes, pos);
                pos += 4;

                newEvent.streamID = BitConverter.ToInt32(filebytes, pos);
                pos += 4;

                if (newEvent.streamID > numTracks) {
                    numTracks = newEvent.streamID;
                }

                newEvent.eventValue = new byte[]{(byte) (filebytes[pos] & 0xFF), filebytes[pos + 1], filebytes[pos + 2], filebytes[pos + 3]};
                pos += 4;

                newBlock.mdsEvents.add(newEvent);
            }

            blocks.add(newBlock);
        }

        numTracks++; //because it will have undercounted by one

        //Now convert that data and write to a MIDI

        output.add((byte) 'M');
        output.add((byte) 'T');
        output.add((byte) 'h');
        output.add((byte) 'd');

        Utility.AddBigEndianUIntToList(output, 6);

        Utility.AddBigEndianUShortToList(output, 1); //format. 1 means multitrack

        Utility.AddBigEndianUShortToList(output, (numTracks & 0xFFFF)); //number of tracks

        Utility.AddBigEndianUShortToList(output, (TimeFormat & 0xFFFF)); //time format


        for (int i = 0; i < numTracks; i++) {
            output.add((byte) 'M');
            output.add((byte) 'T');
            output.add((byte) 'r');
            output.add((byte) 'k');

            int placeholderTrackSizePos = output.size();
            Utility.AddBigEndianUIntToList(output, 0xEEEEEEEE); //placeholder for track size

            int trackSize = 0;

            for (MDSBlock block : blocks) {
                for (MDSEvent ev : block.mdsEvents) {
                    for (byte b : GetAsVariableLengthQuantity(ev.deltaTicks)) {
                        output.add(b);
                        trackSize++;
                    }

                    for (byte b : TranslateMDSEventToMIDI(ev.eventValue)) {
                        output.add(b);
                        trackSize++;
                    }
                }
            }
            output.add((byte) 0x00);
            output.add((byte) 0xFF);   //these three bytes denote the end of a track
            output.add((byte) 0x2F);
            output.add((byte) 0x00);
            trackSize += 4;

            byte[] trackSizeAsBytes = BitConverter.GetBytes(trackSize);
            output.set(placeholderTrackSizePos, trackSizeAsBytes[3]);
            output.set(placeholderTrackSizePos + 1, trackSizeAsBytes[2]);
            output.set(placeholderTrackSizePos + 2, trackSizeAsBytes[1]);
            output.set(placeholderTrackSizePos + 3, trackSizeAsBytes[0]);
        }

        return ConvertListToByteArray(output);
    }

    public byte[] ConvertListToByteArray(LinkedList<Byte> input) {

        byte[] output = new byte[input.size()];

        for (int i = 0; i < output.length; i++) {
            output[i] = input.get(i);
        }

        return output;
    }


    public byte[] TranslateMDSEventToMIDI(byte[] MDSEvent) {

        byte[] output = new byte[0];

        switch (MDSEvent[3]) {
            case 0x00: //MIDI short message
                if (LengthsOfMIDIEvents.get((short) (MDSEvent[0] & 0xF0)) == 2) {  //if it's one of the events that uses a shorter entry in standard midi
                    output = new byte[2];
                    output[0] = MDSEvent[0];
                    output[1] = MDSEvent[1];
                } else {   //otherwise give it three bytes
                    output = new byte[3];
                    output[0] = MDSEvent[0];
                    output[1] = MDSEvent[1];
                    output[2] = MDSEvent[2];
                }
                return output;
            case 0x01: //Tempo change
                output = new byte[6];
                output[0] = (byte) 0xFF;
                output[1] = 0x51;
                output[2] = 0x03;
                output[3] = MDSEvent[2];
                output[4] = MDSEvent[1];
                output[5] = MDSEvent[0];
                return output;
            default:
                JOptionPane.showMessageDialog(null, "Unknown MDS event type: " + MDSEvent[3]);
                return output;
        }
    }

    public byte[] GetAsVariableLengthQuantity(int input) {
        byte lowByte = (byte) (input & 0x7F);

        int highThreeBytes = (input << 1) & 0xFFFFFF00;            //lowest byte has its highest bit clear; all the other bytes have it set. So we need to move the rest of the bits around so that they are all preserved

        byte secondLowestByte = (byte) (((highThreeBytes & 0x0000FF00) >>> 8) | 0x80);

        int highestTwoBytes = (highThreeBytes << 1) & 0xFFFF0000;

        byte secondHighestByte = (byte) (((highestTwoBytes & 0x00FF0000) >>> 16) | 0x80);

        int TopByte = (highestTwoBytes << 1) & 0xFF000000;

        byte highestByte = (byte) (((TopByte & 0xFF000000) >>> 24) | 0x80);

        if (highestByte != 0x80) {
            return new byte[]{highestByte, secondHighestByte, secondLowestByte, lowByte};
        } else if (secondHighestByte != 0x80) {
            return new byte[]{secondHighestByte, secondLowestByte, lowByte};
        } else if (secondLowestByte != 0x80) {
            return new byte[]{secondLowestByte, lowByte};
        } else {
            return new byte[]{lowByte};
        }
    }
}