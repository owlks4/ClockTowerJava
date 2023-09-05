package com.clockTower.Utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.clockTower.fileUtilities.MidiStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;


    public class MIDIPlayer
    {
        public static Sequencer sequencer;
        public static int vol;
        
        public static void Play(MidiStream midi, boolean loop)
        {
            if (sequencer != null){
                sequencer.stop();
                sequencer.close();
                sequencer = null;
            }
            
            if (sequencer == null){
            	try {
					LoadSequencer();
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
                }

            InputStream is = new ByteArrayInputStream(midi.filebytes);
            
            try {
				sequencer.setSequence(is);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}

            if (loop) {
            	 sequencer.setLoopCount(-1);	
            }
            else {
            	sequencer.setLoopCount(0);
            }
            
            sequencer.stop();

            sequencer.start();
        }
        
        public static void LoadSequencer() throws MidiUnavailableException {
        	
           	sequencer = MidiSystem.getSequencer();
            sequencer.open();
        	setVol(vol);
        }
        
        public static void setVol(int _vol) {
        	
        	vol = _vol;
        	
        	if (sequencer instanceof Synthesizer) {
                Synthesizer synthesizer = (Synthesizer) sequencer;
                MidiChannel[] channels = synthesizer.getChannels();

                // gain is a value between 0 and 1 (loudest)
                for (int i = 0; i < channels.length; i++) {
                  channels[i].controlChange(7, vol);
                }
              }
        	
        }
    }