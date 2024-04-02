  /**********************************************************************
  * Copyright (C) by Michael Loesler, http//derletztekick.com            *
  *                                                                      *
  * This program is free software; you can redistribute it and/or modify *
  * it under the terms of the GNU General Public License as published by *
  * the Free Software Foundation; either version 3 of the License, or    *
  * (at your option) any later version.                                  *
  *                                                                      *
  * This program is distributed in the hope that it will be useful,      *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of       *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
  * GNU General Public License for more details.                         *
  *                                                                      *
  * You should have received a copy of the GNU General Public License    *
  * along with this program; if not, see <http://www.gnu.org/licenses/>  *
  * or write to the                                                      *
  * Free Software Foundation, Inc.,                                      *
  * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
  *                                                                      *
   **********************************************************************/

package com.derletztekick.geodesy.logi;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.derletztekick.geodesy.logi.sql.DataManager;


public class Logi extends JFrame {
	private static final long serialVersionUID = 7223537199070909984L;
	private final DataManager dataManager;
	private final String frameTitle;
	public static double VERSION = 1.0;
	public static int BUILD = 20160530;
	public static String ICON = "/com/derletztekick/geodesy/logi/gfx/logi.png";
	public Logi(String title, String icon, int frameWidth, int frameHeight, String[] args) {
		// Frame-Initialisierung
		this.frameTitle = title;
		this.dataManager = new DataManager(this);
//		this.dataManager = new DataManager(java.util.Locale.ENGLISH, this);
		super.setTitle(this.frameTitle + " - http://derletztekick.com");
		
	    this.addWindowListener(this.dataManager);
	    super.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    //super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    
	    try {
	    	icon = icon==null||icon.trim().isEmpty()?Logi.ICON:icon;
	    	this.setIconImage(new ImageIcon(getClass().getResource(icon)).getImage());
	    }
	    catch ( Exception e ) { 
	    	//e.printStackTrace();
	    };
	    
	    this.setSize(          new Dimension(frameWidth,frameHeight) );
	    this.setMinimumSize(   new Dimension((int)(0.70*frameWidth),(int)(0.50*frameHeight)) );
	    this.setPreferredSize( new Dimension(frameWidth,frameHeight) );
	    this.setLocationRelativeTo(null);
	    final Container contentPane = getContentPane();
	    contentPane.setLayout( new BorderLayout(15, 15) );
	    
	    contentPane.add(dataManager.getToolBar(), BorderLayout.NORTH);
	    contentPane.add(dataManager.getDataSplitPane(), BorderLayout.CENTER);
	    contentPane.add(new JLabel("<html><p style=\"margin: 0 0 7px 10px; color: #9f9f9f;\">Logi (BundleAdjustment) \u2015 The <em>OpenSource</em> Similarity Transformation Program for Polar Observations</p></html>"), BorderLayout.SOUTH);
	    
	    // Zeichen Frame
	    this.pack();
	    this.setResizable(true);
	    this.setVisible(true);
	    
	}
	
	@Override
	public void setTitle(String subTitle) {
		super.setTitle(this.frameTitle + " - " + subTitle + " - http://derletztekick.com");
	}
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	int frameWidth  = 775;
	    	    int frameHeight = 650;
	    	    String frameTitle = "Logi - Local Observations to Global Integration v" + Logi.VERSION + "." + Logi.BUILD +" alpha";
	        	new Logi(frameTitle, null, frameWidth, frameHeight, args);
	        }
	    });
	}

}
