package com.clockTower.main;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.clockTower.CT.CTMemory;
import com.clockTower.CT.CTUI;

public class Window extends Canvas implements MouseListener{

	private static final long serialVersionUID = 1985691236150140601L;
	public JFrame frame;
	
	public static int headerSizeY = 0;	//The size of the bar at the top of the window. We need to compensate for this when checking mouse position
	public static int blackBarSize = 0;
	
	public static boolean MouseHeld = false;
	
	boolean fullScreen = false;
	
	public Window(int width, int height, String title, Game game) {
		
		frame = new JFrame(title);
		
		if (fullScreen) {
			width = Game.WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
			height = Game.HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
			frame.setUndecorated(true);
			frame.setVisible(true);
		}
		
		blackBarSize = Math.round((Game.WIDTH - (Game.DEFAULT_ASPECT_RATIO * Game.HEIGHT)) / 2);
		
		frame.getContentPane().setMaximumSize(new Dimension(width, height));
		frame.getContentPane().setMinimumSize(new Dimension(width, height));
		frame.getContentPane().setPreferredSize(new Dimension(width, height));
		frame.pack();
		
		headerSizeY = frame.getSize().height - Game.HEIGHT;
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		frame.add(game);
		frame.setVisible(true);
		
		game.addMouseListener(this);
		
		frame.addWindowListener(new WindowAdapter()
	        {
	            @Override
	            public void windowClosing(WindowEvent e)
	            {
	                e.getWindow().dispose();
	                
	                if (Game.client != null && Game.client.active) {
	                	Game.client.CloseConnection();
	                }
	                
	                if(Game.server != null && Game.server.running) {
		            	Game.server.stopServer();
		            }
	            }
	        });
		
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		//Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank");
		//frame.getContentPane().setCursor(blankCursor);
		
		game.start();
	}
	
	@Override
    public void mouseClicked(MouseEvent e) {
		loader.click();
		if(CTUI.displayingAnyMessage) {
			CTUI.skipToEnd = true;
			}
		MouseHeld = false;
    }
 
    @Override
    public void mouseEntered(MouseEvent e) {
     
    }
 
    @Override
    public void mouseExited(MouseEvent e) {
       
    }
    
    
    @Override
    public void mousePressed(MouseEvent e) {
    
    	if (!CTMemory.userControl) {
			 return;
		 }
    	
    	Point p = e.getPoint();

    	MouseHeld = true;
    	
	    //go through backwards so that things closer to the screen get higher priority
			 
		//sprites in rooms
    }
 
    @Override
    public void mouseReleased(MouseEvent e) {
    	MouseHeld = false;
    	loader.releaseClick();
    }
}