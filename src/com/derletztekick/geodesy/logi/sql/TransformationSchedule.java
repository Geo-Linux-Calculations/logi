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

package com.derletztekick.geodesy.logi.sql;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;
import com.derletztekick.geodesy.bundleadjustment.v2.transformation.Transformation;
import com.derletztekick.geodesy.bundleadjustment.v2.transformation.sql.BundleAdjustmentProjectDatabase;
import com.derletztekick.geodesy.logi.dialog.IndeterminateProgressDialog;
import com.derletztekick.tools.sql.DataBase;

public class TransformationSchedule extends Thread {
	private IndeterminateProgressDialog progressDialog;
	private PreAnalysis preAnalysis;
	private BundleAdjustmentProjectDatabase bundleAdjustmentProjectDatabase;
	private PropertyChangeSupport changes = new PropertyChangeSupport( this );
	
	TransformationSchedule(IndeterminateProgressDialog progressDialog, DataBase db) {
		this.progressDialog = progressDialog;
		this.preAnalysis = new PreAnalysis(db);
		this.bundleAdjustmentProjectDatabase = new BundleAdjustmentProjectDatabase(db);
		
		this.preAnalysis.addPropertyChangeListener(this.progressDialog);
		this.bundleAdjustmentProjectDatabase.addPropertyChangeListener(this.progressDialog);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l){
		this.changes.addPropertyChangeListener(l);
		this.progressDialog.addPropertyChangeListener(l);
		this.preAnalysis.addPropertyChangeListener(l);
		this.bundleAdjustmentProjectDatabase.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener( PropertyChangeListener l ) {
		this.changes.removePropertyChangeListener( l );
		this.progressDialog.removePropertyChangeListener(l);
		this.preAnalysis.removePropertyChangeListener(l);
		this.bundleAdjustmentProjectDatabase.removePropertyChangeListener(l);
	}
	
	@Override
	public void run() {
		try {
			this.progressDialog.setChildTask(this.preAnalysis);

			if (!this.preAnalysis.analyse()) {
				System.err.println(this.getClass().getSimpleName()+ " Fehler, pre-Analyse gescheitert!");
				return;
			}

			Transformation bundleTransformation = this.bundleAdjustmentProjectDatabase.getBundleTransformation();
			
			if (bundleTransformation == null) {
				System.err.println(this.getClass().getSimpleName()+ " Fehler, Transformationsobjekt konnte nicht erstellt werden!");
				return;
			}
			this.progressDialog.setChildTask(bundleTransformation);
			
			if (!bundleTransformation.estimate()) {
				System.err.println(this.getClass().getSimpleName()+ " Fehler, Transformation gescheitert!");
				return;
			}
			
			if (!this.bundleAdjustmentProjectDatabase.saveResults()) {
				System.err.println(this.getClass().getSimpleName()+ " Fehler, Daten konnten nicht gespeichert werden!");
			}
		} 
		finally {
			this.progressDialog.setVisible(false);
			this.progressDialog.dispose();
			this.changes.firePropertyChange( "SCHEDULE_FINISHED", false, true );
		}
	}
}
