package com.clockTower.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.io.File;
import com.clockTower.CT.CTUI;
import com.clockTower.online.Client;
import com.clockTower.online.Server;


public class Game extends Canvas implements Runnable{
		
	private static final long serialVersionUID = 5193470106202939266L;
		
		public static float ASPECT_RATIO = 40f/27f;
		public static int HEIGHT = 432, WIDTH = Math.round(HEIGHT * (ASPECT_RATIO));	//default height is 432
		
		public static final float DEFAULT_ASPECT_RATIO = 40f/27f;
		
		public static final int DEFAULT_WIDTH = 640;
		public static final int DEFAULT_HEIGHT = 432;
		
		public static int FRAME_RATE = 60;
		public static long totalFramesElapsed = 0;
		
		public static String GAME_ROOT_DIR;
		
		private Thread thread;
		private boolean running = false;

		private final Camera camera;
		private final KeyInput keyInput;
		
		public static Window window;
		
		public static Server server; //used if we are a server or a client, as we still have to access the other client objects via the """"server"""", even if we are a client
		public static Client client; //used if we are a client
		public static boolean isServer; //used if we are ACTUALLY the server
		
		BufferStrategy bs;
		public static Graphics g;
		
		public static Game gameWindow;
		public static Color backgroundColor = Color.black;
		public static Color fadeColor = backgroundColor;
		
		public static final RenderingHints RH_ANTIALIASING = new RenderingHints(
	             RenderingHints.KEY_ANTIALIASING,
	             RenderingHints.VALUE_ANTIALIAS_ON);
		
		public static final RenderingHints RH_BILINEAR = new RenderingHints(
	             RenderingHints.KEY_INTERPOLATION,
	             RenderingHints.VALUE_INTERPOLATION_BILINEAR);


		
		public static Font textFont;


		public Game() {
			
			camera = new Camera();

			window = new Window(WIDTH, HEIGHT, "Clock Tower: The First Fear (Java)", this);	

			keyInput = new KeyInput(); 
			this.addKeyListener(keyInput);
			
			LoadFonts();

			gameWindow = this;
			setBackground(Color.black);

			bs = this.getBufferStrategy();
			
			if (bs == null) {
				this.createBufferStrategy(3);
				bs = this.getBufferStrategy();
				g = bs.getDrawGraphics();
				
				Font currentFont = g.getFont();
				Font newFont = currentFont.deriveFont(19.5f * (HEIGHT/(float)DEFAULT_HEIGHT));
				g.setFont(newFont);
				
				//g.setFont(textFont);
			}

			GAME_ROOT_DIR = new File(System.getProperty("user.dir"),"game").getAbsolutePath();
			loader.Start();	
		}
		
		public synchronized void start() {
			thread = new Thread(this);
			thread.start();
			running = true;
		}
		
		public synchronized void stop() {
			try {
				thread.join();
				running = false;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			this.requestFocus();
			long lastTime = System.nanoTime();
			double amountOfTicks = FRAME_RATE;
			double ns = 1000000000 / amountOfTicks;
			double delta = 0;
			long timer = System.currentTimeMillis();
			int frames = 0;
			while (running) {
				long now = System.nanoTime();
				delta += (now - lastTime) / ns;
				lastTime = now;
				while (delta >= 1) {
					tick();
					delta--;
				}
				
				if (running)
					render();
				frames++;
				totalFramesElapsed++;
				
				if(System.currentTimeMillis() - timer > 1000) {
					timer += 1000;
					System.out.println("FPS: "+ frames);
					frames = 0;
				}
			}
			stop();
		}
		
		private void tick() {
			
			if (client != null && client.active && totalFramesElapsed % 60 == 0) {
				client.heartbeat();
			}
			
			keyInput.tick();
			Camera.tick();
			loader.tick();
			CTUI.tick();
		}
		
		private void render() {
			
			if (g == null) {
				return;
			}
			
			if (bs != null) {
				
				//g.setColor(backgroundColor);
				//g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);		
				
				loader.render(g, camera);
				CTUI.render(g, camera);
				
				//g.dispose();
				bs.show();
				}
		}
		
		public static void main (String[] args) {
			
			new Game();
		}

		public static int GetAdjustedDimension(int originalDimension) {	//adjust a dimension of the BufferedImage to reflect the current screen size (originally they were designed for the DS screen size, which is what this calculation is based on)
			return Math.round(originalDimension * ((float)Game.HEIGHT / 540f)); //always base it on the height, even if we're using it to modify a width (this way, we can still change the aspect ratio to widescreen)
		}
		
		public static float GetAdjustedDimension(float originalDimension) {	//adjust a dimension of the BufferedImage to reflect the current screen size (originally they were designed for the DS screen size, which is what this calculation is based on)
			return originalDimension * ((float)Game.HEIGHT / 540f); //always base it on the height, even if we're using it to modify a width (this way, we can still change the aspect ratio to widescreen)
		}		
		
		public void LoadFonts() {
			
			/*
	        try (InputStream stream = Game.class.getResourceAsStream("textFont.ttf")) {
	            textFont = Font.createFont(Font.TRUETYPE_FONT, stream);
	        	Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
				attributes.put(TextAttribute.TRACKING, 0.005f);
				attributes.put(TextAttribute.SIZE, (40f * (Game.HEIGHT/720f)));
				textFont = textFont.deriveFont(attributes);
	        } catch (FontFormatException | IOException e) {
				e.printStackTrace();
			}
			*/
		}
}