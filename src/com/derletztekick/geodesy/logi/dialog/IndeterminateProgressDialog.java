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

package com.derletztekick.geodesy.logi.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;


import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;
import com.derletztekick.geodesy.bundleadjustment.v2.transformation.Transformation;
import com.derletztekick.tools.babel.Babel;

public class IndeterminateProgressDialog extends JDialog implements MessageThread, WindowListener, ActionListener, PropertyChangeListener {
	private JLabel statusLabel = new JLabel(); 
	private JProgressBar progressBar = new JProgressBar();
	private JButton cancelButton = new JButton();
	private static final long serialVersionUID = -858963971427541358L;
	private Object task;
	private Babel babel;
	public IndeterminateProgressDialog(JFrame owner, Babel babel) { 
        super(owner, "", true); 
        this.babel = babel;
        this.init(); 
    }
	
	@Override
	public void setChildTask(Object t) {
		this.task = t;
	}

	public void interrupt() {
		if (this.task instanceof PreAnalysis || this.task instanceof Transformation) {
			if (JOptionPane.showConfirmDialog(this, 
					this.babel.getString(this.getClass().getSimpleName(), "msg.abortBody"),
					this.babel.getString(this.getClass().getSimpleName(), "msg.abortTitle"),
			        JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION) {
				if (this.task instanceof Transformation) {
					((Transformation)this.task).interrupt();
				}
				else if (this.task instanceof PreAnalysis) {
					((PreAnalysis)this.task).interrupt();
				}
			}
		}
	}
		
	public void setStatusMessage(String txt) {
		this.statusLabel.setText(txt);
	}
	
	public void setProgressMessage(String txt) {
		this.progressBar.setString(txt);
	}
	
	private void init(){ 
		int frameWidth = 375, frameHeight = 140;
		
		this.setTitle( this.babel.getString(this.getClass().getSimpleName(), "title"));
        this.progressBar = new JProgressBar(); 
        this.progressBar.setStringPainted(true);
		this.progressBar.setIndeterminate(true);
		this.statusLabel.setText(" ");
		this.progressBar.setString(" ");
		this.cancelButton.setText(this.babel.getString(this.getClass().getSimpleName(), "abort"));
		this.cancelButton.addActionListener(this);
		
		this.setSize(          new Dimension(frameWidth,frameHeight) );
	    this.setPreferredSize( new Dimension(frameWidth,frameHeight) );
	    this.setResizable(false);
	    this.addWindowListener(this);
	    
		JPanel contentPane = (JPanel)getContentPane(); 
	    contentPane.setLayout( new BorderLayout(10, 10) );
	    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50)); 
        
        contentPane.add(statusLabel, BorderLayout.NORTH); 
        contentPane.add(progressBar, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(); 
        buttonPanel.add(this.cancelButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH); 
        pack();
        super.setLocationRelativeTo(this.getParent());
        super.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); 

    }

	@Override
	public void run() {
		super.setVisible(true);
	}
	
	@Override
	public Babel getBabel() {
		return this.babel;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		this.interrupt();
	}

	@Override
	public void windowActivated(WindowEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosed(WindowEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosing(WindowEvent evt) {
		this.interrupt();
	}

	@Override
	public void windowDeactivated(WindowEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowDeiconified(WindowEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
//		System.out.printf( this.getClass().getSimpleName() + "  Property '%s': '%s' -> '%s'%n", e.getPropertyName(), e.getOldValue(), e.getNewValue() );
		if (e.getPropertyName().equals("TRANSFORMATION_BUSY")) {
			this.setStatusMessage(" ");
			this.setProgressMessage(" ");
		}
		else if(e.getPropertyName().equals("TRANSFORMATION_ITERATION_STEP")) {
			this.setStatusMessage(String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "iteration"),  e.getNewValue()));
		}
		else if(e.getPropertyName().equals("TRANSFORMATION_CURRENT_MAX_DX")) {
			this.setProgressMessage(String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "iterationstep"), e.getNewValue()));
		}
		else if(e.getPropertyName().equals("TRANSFORMATION_ADJUST_STOCHASTIC_VALUES")) {
			this.setStatusMessage( this.babel.getString(this.getClass().getSimpleName(), "adjustStochPar") );
		}
		else if(e.getPropertyName().equals("TRANSFORMATION_CHECK_POINT")) {
			this.setProgressMessage( String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "checkPoint"), e.getNewValue()));
		}
		else if(e.getPropertyName().equals("TRANSFORMATION_ESTIMATE_APPROX_VALUES")) {
			this.setStatusMessage( this.babel.getString(this.getClass().getSimpleName(), "approxValues") );
		}
		else if(e.getPropertyName().equals("TRANSFORMATION_REMAINING_SYSTEMS")) {
			this.setProgressMessage( String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "approxValues.remain"), e.getNewValue(), e.getOldValue()));
		}
		else if(e.getPropertyName().equals("TRANSFORMATION_TRANSFORM_PARAMETERS")) {
			this.setStatusMessage( this.babel.getString(this.getClass().getSimpleName(), "transparam") );
			this.setProgressMessage( String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "transparam.systemcount"), (Integer)e.getNewValue()+1, e.getOldValue()));
		}
		else if(e.getPropertyName().equals("PRE_ANALYSIS_ESTIMATE_LOCAL_UNCERTAINTY")) {
			this.setStatusMessage(  this.babel.getString(this.getClass().getSimpleName(), "covar") );
			this.setProgressMessage( String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "covar.local"), e.getNewValue()));
		}
		else if(e.getPropertyName().equals("PRE_ANALYSIS_ESTIMATE_GLOBAL_UNCERTAINTY")) {
			this.setStatusMessage(  this.babel.getString(this.getClass().getSimpleName(), "covar") );
			this.setProgressMessage( String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "covar.global"), e.getNewValue()));
		}
		else if(e.getPropertyName().equals("BUNDLEADJUSTMENT_SAVE_RESULT")) {
			this.setStatusMessage(  this.babel.getString(this.getClass().getSimpleName(), "save") );
			this.setProgressMessage( " " );
		}
		
	}

}