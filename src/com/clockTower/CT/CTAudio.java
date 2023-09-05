package com.clockTower.CT;
import java.io.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.clockTower.main.Game;

public class CTAudio {

	public class CTClip{
		public String path;
		public Clip clip;
		public AudioInputStream ais = null;
		
		public void play() {
			
			try
	        {
	            File f = new File(path);
	            ais = AudioSystem.getAudioInputStream(f);
	            clip = AudioSystem.getClip();
	            
	            //TODO: For non-looping audio clips, they need to close themselves after they are complete
	            System.out.println("TODO: For non-looping audio clips, they need to close themselves after they are complete");

	            clip.open(ais);
	            clip.start();
	            
	            if (loop) {
	            	clip.loop(Clip.LOOP_CONTINUOUSLY);
	            	}
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
		}
		
		public void stop() {
			clip.stop();
			clip.close();
			try {
				ais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			CTMemory.currentAudioClips.remove(this);
		}
	}
	
	public CTClip myClip;
	public boolean panStereo;
	public boolean loop;
	public float panAmount = 0;

	public void Init(String _path, boolean _loop, boolean _panStereo){
			myClip = new CTClip();
			myClip.path = new File(Game.GAME_ROOT_DIR,_path).getAbsolutePath();
			loop = _loop;
			panStereo = _panStereo;
			/*
			if (panStereo){
				if (transform.position.x < Camera.main.transform.position.x){
					//TODO: left panned
				} else {
					//TODO: right panned
					}
				}*/
			
			CTMemory.currentAudioClips.add(myClip);
			myClip.play();
		}
	


	}