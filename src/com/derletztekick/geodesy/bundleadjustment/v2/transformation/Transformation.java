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

package com.derletztekick.geodesy.bundleadjustment.v2.transformation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.UpperSymmBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.GlobalPointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.UnknownParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.GlobalPoint;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.GlobalPoint3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet;
import com.derletztekick.geodesy.bundleadjustment.v2.util.DiagonalSymmetricBlockMatrix;
import com.derletztekick.tools.geodesy.Constant;
import com.derletztekick.tools.geodesy.MathExtension;
import com.derletztekick.tools.geodesy.ObservationTestValues;

public abstract class Transformation implements Runnable {
	private boolean isFreeNet     = true;
	private boolean isInterrupted = false;
	
	private List<PointGroup> srcPointGroups = new ArrayList<PointGroup>();
	private PointGroup trgPointGroup, datumPointGroup = new PointGroup(0);
	private DiagonalSymmetricBlockMatrix P = null;
	private List<UnknownParameter> unknownParameters = new ArrayList<UnknownParameter>();
	private ObservationTestValues observationTestValues = null;
	
	private int numberOfUnknownParameters = 0;
	private int numberOfObservations = 0;
	private int numberOfRestrictions = 0;
	private double degreeOfFreedom = 0;
	private int maxIteration = 10000;
	
	private double maxDx = Double.MIN_VALUE;
	private double omega = 0.0;
	private double sigma2aprio = 1.0/100000.0;
	private static double SQRT_EPS = Math.sqrt(Constant.EPS);
	private double alpha = 0.1;
	private double beta  = 80.0;
	private Matrix Qxx = null;
	private boolean applyVarianceFactorAposteriori = true;
	
	private PropertyChangeSupport changes = new PropertyChangeSupport( this );
	
	public Transformation() { }
	
	public boolean initializeSystems(List<PointGroup> srcPointGroups, GlobalPointGroup trgPointGroup) {
		if (trgPointGroup.size() < this.numberOfRequiredHomologousPointsPerSubsystem()) {
			this.changes.firePropertyChange( "TRANSFORMATION_NOT_ENOUGH_GLOBAL_POINTS", -1, trgPointGroup.size() );
			return false;
		}
		if (srcPointGroups.size() < 1) {
			this.changes.firePropertyChange( "TRANSFORMATION_NOT_ENOUGH_SOURCE_SYSTEMS", -1, srcPointGroups.size() );
			return false;
		}

		this.changes.firePropertyChange( "TRANSFORMATION_ESTIMATE_APPROX_VALUES", true, false );
		
		this.trgPointGroup = trgPointGroup;
		
		for (int i=0; i<this.trgPointGroup.size(); i++) { 
			Point datumPoint = this.trgPointGroup.get(i);
			this.datumPointGroup.add(datumPoint);
		}
		this.datumPointGroup.setTransformationParameterSet(trgPointGroup.getTransformationParameterSet());
		if (!this.isFreeNet)
			this.datumPointGroup.setCovarianceMatrix(trgPointGroup.getCovarianceMatrix());
		// Bestimme die Subsysteme, die sich ineinander transformieren lassen
		// Ermittle Naeherungswerte fuer die fehlenden Punkte im Zielsystem und
		// fuer die Transformationsparameter
		int numberOfIdenticalPoints = this.numberOfRequiredHomologousPointsPerSubsystem();
		
		// Erzeuge eine Kopie der Systeme und fuege das gewaehlte Zielsystem am Ende hinzu
		List<PointGroup> tmpGroups = new ArrayList<PointGroup>(srcPointGroups.size());
		for (PointGroup srcGroup : srcPointGroups) {
			PointGroup destGroup = new PointGroup(srcGroup.getId());
			for (int i=0; i<srcGroup.size(); i++) 
				destGroup.add(srcGroup.get(i));
			
			destGroup.setTransformationParameterSet(srcGroup.getTransformationParameterSet());
			tmpGroups.add(destGroup);
		}
		PointGroup destGroup = new PointGroup(trgPointGroup.getId());
		for (int i=0; i<trgPointGroup.size(); i++) 
			destGroup.add(trgPointGroup.get(i));

		destGroup.setTransformationParameterSet(trgPointGroup.getTransformationParameterSet());
		tmpGroups.add(destGroup);
		destGroup=null;
		
		// Permutiere ueber alle Systeme
		while (numberOfIdenticalPoints >= this.numberOfRequiredHomologousPointsPerSubsystem() && tmpGroups.size() > 0) {
			if (this.isInterrupted) {
		    	System.err.println( "Berechnung vom Anwender abgebrochen!" );
		    	this.isInterrupted = false;
		    	this.changes.firePropertyChange( "TRANSFORMATION_INTERRUPT", false, true );
		    	return false;
		    }
			
			numberOfIdenticalPoints = 0;
		
			PointGroup maxSrcPointGroupA = null;
			PointGroup maxSrcPointGroupB = null;
			Set<String> maxPointIds = new LinkedHashSet<String>();
			for (int i=0; i<tmpGroups.size()-1; i++) {
				PointGroup groupA = tmpGroups.get(i);
				for (int j=i+1; j<tmpGroups.size(); j++) {
					PointGroup groupB = tmpGroups.get(j);
					Set<String> pointIds = this.identicalPointIds(groupA, groupB);
					if (pointIds.size() > maxPointIds.size()) {
						maxPointIds = pointIds;
						maxSrcPointGroupA = groupA;
						maxSrcPointGroupB = groupB;
					}
				}
			}
		
			if (maxSrcPointGroupA != null && maxSrcPointGroupB != null && maxPointIds.size() >= this.numberOfRequiredHomologousPointsPerSubsystem()) {
				this.getApproximatedValues(maxPointIds, maxSrcPointGroupA, maxSrcPointGroupB, maxSrcPointGroupB.getTransformationParameterSet());
				maxSrcPointGroupA = this.addOutageLocalPoints2GlobalSystem(maxSrcPointGroupB, maxSrcPointGroupA);
				tmpGroups.remove(maxSrcPointGroupB);
			}
			numberOfIdenticalPoints = maxPointIds.size();
			this.changes.firePropertyChange( "TRANSFORMATION_REMAINING_SYSTEMS", srcPointGroups.size(), tmpGroups.size() );
		}
		
		// Pruefe, ob alles ineinander ueberfuehrt werden konnte.
		if (tmpGroups.size() != 1) {
			this.changes.firePropertyChange( "TRANSFORMATION_NO_UNIQUE_SYSTEM_FOUND", -1, tmpGroups.size() );
			return false;
		}
		
		// Transformiere alle Punkte ins Zielsystem (als Naeherung fuer AGL)
		this.getApproximatedValues(null, this.trgPointGroup, tmpGroups.get(0), tmpGroups.get(0).getTransformationParameterSet());
		this.addOutageLocalPoints2GlobalSystem(tmpGroups.get(0), this.trgPointGroup);
		
		tmpGroups = null;
		
		
		// Bestimme Transformationsparameter, um das GLOBALE System ins LOKALE zu ueberfuehren
		for (PointGroup pointGroup : srcPointGroups) {
			TransformationParameterSet trafoSet = this.getApproximatedValues(null, this.trgPointGroup, pointGroup, pointGroup.getTransformationParameterSet());
			pointGroup.setTransformationParameterSet(trafoSet);
			this.addOutageLocalPoints2GlobalSystem(pointGroup);
			this.srcPointGroups.add(pointGroup);
		}
		
		
//		// Bestimme die Subsysteme, die sich ineinander transformieren lassen
//		// Ermittle Naeherungswerte fuer die fehlenden Punkte im Zielsystem und
//		// fuer die Transformationsparameter
//		int numberOfIdenticalPoints = this.numberOfRequiredHomologousPointsPerSubsystem();
//		while (numberOfIdenticalPoints >= this.numberOfRequiredHomologousPointsPerSubsystem() && srcPointGroups.size() > 0) {
//			numberOfIdenticalPoints = 0;
//			
//			// Ermittle die Punktgruppe mit den meisten Passpunkten
//			PointGroup maxSrcPointGroup = null;
//			Set<String> maxPointIds = new LinkedHashSet<String>();
//			for (PointGroup srcPointGroup : srcPointGroups) {
//				Set<String> pointIds = this.identicalPointIds(this.trgPointGroup, srcPointGroup);
//				if (pointIds.size() > maxPointIds.size()) {
//					maxPointIds = pointIds;
//					maxSrcPointGroup = srcPointGroup;
//				}
//			}
//			// Bestimme genaeherte Transformationsparameter und ueberführe alle Punkte ins globale System, die dort noch nicht vorhanden sind
//			if (maxSrcPointGroup != null && maxPointIds.size() >= this.numberOfRequiredHomologousPointsPerSubsystem()) {
//				TransformationParameterSet trafoSet = this.getApproximatedValues(maxPointIds, this.trgPointGroup, maxSrcPointGroup, maxSrcPointGroup.getTransformationParameterSet());
//				maxSrcPointGroup.setTransformationParameterSet(trafoSet);
//				this.addOutageLocalPoints2GlobalSystem(maxSrcPointGroup);
//				this.srcPointGroups.add(maxSrcPointGroup);
//				srcPointGroups.remove(maxSrcPointGroup);
//			}
//			
//			numberOfIdenticalPoints = maxPointIds.size();
//		}
		
		// Pruefe die Anzahl der verbliebenen Subsysteme
		if (this.srcPointGroups.size() < 1) {
			this.changes.firePropertyChange( "TRANSFORMATION_NOT_ENOUGH_SOURCE_SYSTEMS", -1, this.srcPointGroups.size() );
			return false;
		}
		
		// Bestimme Schwerpunkt fuer Zielsystem
		Point trgCenterPoint = this.trgPointGroup.getCenterPoint();
		
		// Setze die Indizes fuer das NGL - Zielsystem
		int dim = this.getDimension();
		int col = 0;
		int row = 0;

		if (this.isFreeNet) {
			this.P = new DiagonalSymmetricBlockMatrix(this.sigma2aprio, this.srcPointGroups.size());
		}
		else {
			this.P = new DiagonalSymmetricBlockMatrix(this.sigma2aprio, this.srcPointGroups.size()+1);
			try {
				this.P.invAdd(this.datumPointGroup.getCovarianceMatrix());
			} catch (Exception e) {
				e.printStackTrace();
				this.changes.firePropertyChange( "TRANSFORMATION_GLOBAL_SINGULAR_COVAR", false, true );
				return false;
			}
		}
			
		for (int i=0; i<this.trgPointGroup.size(); i++, col+=dim) { 
			Point point = this.trgPointGroup.get(i);
			point.setColInJacobiMatrix(col);
			// Nur fuer die "Original" Zielsystempunkte 
			if (!this.isFreeNet && i<this.datumPointGroup.size()) {
				point.setRowInJacobiMatrix(row);
				this.numberOfObservations += dim;
				row+=dim;
			}
			this.numberOfUnknownParameters += dim;
			this.unknownParameters.add(point);
		}
		
		// Setze die Indizes fuer das NGL - Parameter und Startsystem
		for (PointGroup pointGroup : this.srcPointGroups) {
			// Nutze Schwerpunktreduzierte Daten; fuege Schwerpunkte dem Subsystemen hinzu, sofern diese
			// nicht durch eine Restriktion (keine Translation) unterbunden wird
			// Da die Transformationsparameter in der Richtung: GLOBAL -> LOKAL bestimmt werden,
			// ist das GLOBALE System das Startsystem!
			// Anpassung der Naeherungswerte der Translation
			TransformationParameterSet trafoSet = pointGroup.getTransformationParameterSet();			
			if (dim == 3 && !trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X) && !trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y) && !trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z)) {
				Point localCenterPoint  = pointGroup.getCenterPoint();
				Point globalCenterPoint = trafoSet.transform(trgCenterPoint);
						
				// Bestimme reduzierte Translation aus Schwerpunkten
				double tx = globalCenterPoint.getX() - localCenterPoint.getX();
				double ty = globalCenterPoint.getY() - localCenterPoint.getY();
				double tz = globalCenterPoint.getZ() - localCenterPoint.getZ();
				
				trafoSet.setSourceSystemCenterPoint( trgCenterPoint );
				trafoSet.setTargetSystemCenterPoint( localCenterPoint );
				trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_X).setValue(tx);
				trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_Y).setValue(ty);
				trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_Z).setValue(tz);
			}

			TransformationParameter trafoParam[] = trafoSet.getTransformationParameters();
			this.numberOfRestrictions += trafoSet.numberOfRestrictions();
			for (int i=0; i<trafoParam.length; i++) {
				trafoParam[i].setColInJacobiMatrix(col++);
				this.unknownParameters.add(trafoParam[i]);
				this.numberOfUnknownParameters++;
			}

			for (int i=0; i<pointGroup.size(); i++, row+=dim) { 
				Point point = pointGroup.get(i);
				point.setRowInJacobiMatrix(row);
				this.numberOfObservations += dim;
			}
			
			try {
				this.P.invAdd(pointGroup.getCovarianceMatrix());
			} catch (Exception e) {
				e.printStackTrace();
				this.changes.firePropertyChange( "TRANSFORMATION_LOCAL_SINGULAR_COVAR", false, true );
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Ermoeglicht nach der AGL eine Transformation der Parameter um bspw. die 
	 * Schwerpunktreduktion rueckgaengig zu machen oder um aus Hilfsgroessen
	 * die eigentlichen Parameter abzuleiten
	 * 
	 * @param trafoParam
	 * @param Cxx
	 */
	protected abstract void transformParameterSet(TransformationParameterSet trafoParam, Matrix Cxx);
	
	/**
	 * Bestimmt die Naeherungswerte fuer eine Trafo; die Transformationsparameter
	 * ueberfuehren die Punkte im GLOBALEN-System ins LOKALE-System!!!
	 * @param srcSystem
	 * @param trgSystem
	 * @param transformationParameters
	 * @return transformationParameters
	 */
	protected abstract TransformationParameterSet getApproximatedValues(Set<String> identicalPointIds, PointGroup srcSystem, PointGroup trgSystem, TransformationParameterSet transformationParameters);
	
	/**
	 * Transformiert die fehlenden Punkte eines lokalen Systems ins zweite System
	 * @param srcLocalSystem
	 * @param destLocalSystem
	 * @return destLocalSystem
	 */
	private PointGroup addOutageLocalPoints2GlobalSystem(PointGroup srcLocalSystem, PointGroup destLocalSystem) {
		TransformationParameterSet trafoSet = srcLocalSystem.getTransformationParameterSet();
		for (int i=0; i<srcLocalSystem.size(); i++) {
			Point localPoint = srcLocalSystem.get(i);
			Point globalPoint = destLocalSystem.get(localPoint.getId());
			if (globalPoint == null) {
				globalPoint = trafoSet.inverseTransform(localPoint);
				destLocalSystem.add(globalPoint);
			}
		}
		return destLocalSystem;
	}
	
	/**
	 * Transformiert die fehlenden Punkte eines lokalen Systems ins Globale,
	 * haengt dabei eine Referenz des lokalen Systems an den globalen Punkt
	 * @param localSystem
	 */
	private void addOutageLocalPoints2GlobalSystem(PointGroup localSystem) {
		TransformationParameterSet trafoSet = localSystem.getTransformationParameterSet();
		for (int i=0; i<localSystem.size(); i++) {
			Point localPoint = localSystem.get(i);
			Point globalPoint = this.trgPointGroup.get(localPoint.getId());
			if (globalPoint == null) {
				globalPoint = new GlobalPoint3D((Point3D)trafoSet.inverseTransform(localPoint));
				this.trgPointGroup.add(globalPoint);
			}
			((GlobalPoint)globalPoint).addLocalPointGroup(localSystem);
		}
	}
	
	/**
	 * Loese Ausgleichung und bestimme Punkte im globalen System
	 * Statusmedlungen werden per PropertyChangeEvent angezeigt
	 * 
	 * @return isEstimated
	 */
	public boolean estimate() {
		// http://openbook.galileocomputing.de/javainsel/javainsel_10_003.htm#mj6548e11cd688937faa1e8dc03ac1ebcb
		this.changes.firePropertyChange( "TRANSFORMATION_BUSY", false, true );
		int maxruns = this.maxIteration;
		int runs = maxruns-1;
		boolean isEstimated = false, estimateCompleteModel = false, isConverge = true;
		
		if (maxruns == 0)
			estimateCompleteModel = isEstimated = true;
				
		try {
			
			// Prüfe, ob die eingefuehrten Datumspunkte min. eine Beobachtung in einem Subsystem haben
			for (int i=0; i<this.datumPointGroup.size(); i++) {
				Point datumPoint = this.datumPointGroup.get(i);
				String pointId = datumPoint.getId(); 
				boolean hasObservation = false;
				for (PointGroup localPointGroup : this.srcPointGroups) {
					hasObservation = localPointGroup.contains(pointId);
					if (hasObservation)
						break;
				}
				if (!hasObservation) {
					this.changes.firePropertyChange( "TRANSFORMATION_NOT_ENOUGH_OBSERVATIONS", null, pointId);
					return false;
				}
			}
			
			do {
			
				if (this.isInterrupted) {
			    	System.err.println( "Berechnung vom Anwender abgebrochen!" );
			    	this.isInterrupted = false;
			    	this.changes.firePropertyChange( "TRANSFORMATION_INTERRUPT", false, true );
			    	return false;
			    }
				
				this.changes.firePropertyChange( "TRANSFORMATION_ITERATION_STEP", maxruns, maxruns-runs );
				
				this.maxDx = Double.MIN_VALUE;
				
				estimateCompleteModel = isEstimated;
								
		    	try {
		    		// Bestimme Normalgleichungssystem
					NormalEquationSystem NES = this.getSystemOfEquations();
					Matrix N  = NES.getNmatrix();
					Vector n  = NES.getNvector();
					Vector dx = new DenseVector(n.size()); 
					
		    		if (estimateCompleteModel) {
		    			Matrix I = MathExtension.identity(N.numColumns());
			    		Matrix Qxx = new DenseMatrix(N.numColumns(), N.numColumns());
			    		N.solve(I, Qxx);
			    		Qxx.mult(n, dx);
			    		// Erzeuge eine Kopie von Qxx OHNE Bedingungen der NGL
			    		this.Qxx = new UpperSymmPackMatrix(this.numberOfUnknownParameters);
			    		for (int i=0; i<this.numberOfUnknownParameters; i++) {
			    			for (int j=i; j<this.numberOfUnknownParameters; j++) {
			    				this.Qxx.set(i, j, Qxx.get(i, j));
			    			}
			    		}
			    	}
		    		else
		    			N.solve(n, dx);
		    		
		    		this.updateUnknownParameters(dx, estimateCompleteModel);
			    }
			    catch (MatrixSingularException mse) {
			    	mse.printStackTrace();
			    	System.err.println( "Gleichungssystem ist singulär, Lösung nicht möglich!" );
			    	this.changes.firePropertyChange( "TRANSFORMATION_SINGULAR_MATRIX", false, true );
			    	return false;
			    }
			    catch (Exception e) {
			    	e.printStackTrace();
			    	this.changes.firePropertyChange( "TRANSFORMATION_FATAL_ERROR", false, true );
			    	return false;
			    }
			    
			    this.changes.firePropertyChange( "TRANSFORMATION_CURRENT_MAX_DX", Double.MAX_VALUE, this.maxDx );
				
			    if (this.maxDx <= Transformation.SQRT_EPS) {
					isEstimated = true;
			    	runs--;
			    }
			    else if (runs-- <= 1) {
			    	if (estimateCompleteModel) {
			    		System.err.println(this.getClass().getSimpleName() + " Berechnung abgebrochen, da die maximale Iterationsanzahl von " + maxruns + " erreicht wurde.\r\n" + 
			    							"Der maximale Zuschlag der letzten Iteration betrug max(dx)= " + this.maxDx + " (EPS = " + Transformation.SQRT_EPS + ").");
			    		isConverge = false;
			    	}
			    	isEstimated = true;
			    }
				System.out.println("MAX(|dX|) = " + this.maxDx );
				
				
			} 
			while (!estimateCompleteModel);
		}
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			this.changes.firePropertyChange( "TRANSFORMATION_OUT_OF_MEMORY", false, true );
			return false;
		}

		// Bestimme stochastische Parameter
		this.changes.firePropertyChange( "TRANSFORMATION_ADJUST_STOCHASTIC_VALUES", Double.MAX_VALUE, this.maxDx );
		boolean hasStochasticParameters = this.calculateStochasticParameters();

		// Bestimme CoVar --> Cxx = s0*Qxx
		System.out.println("sigma2apost = vTPv/f = " + this.getVarianceFactorAposteriori()/this.sigma2aprio);
		this.Qxx.scale(this.getVarianceFactorAposteriori());
		this.transformTransformationParameters();
		
//		// DEBUG AUSGABEN
//		for (int i=0; i<this.trgPointGroup.size(); i++) {
//			System.out.println(this.trgPointGroup.get(i)+"  "+trgPointGroup.get(i).getStdX()+"  "+trgPointGroup.get(i).getStdY()+"  "+trgPointGroup.get(i).getStdZ());
//		}
//
//		if (!this.isFreeNet) {
//			double r = this.getGroupRedundance(this.datumPointGroup);
//			double vTPv = this.getGroupOmega(this.datumPointGroup);
//			System.out.println("sig02 = " + (vTPv/r)+"   "+vTPv+"   "+r);
//		}
//		System.out.println();
//		
//		for (PointGroup localPointGroup : this.srcPointGroups) {
//			System.out.println(localPointGroup.getTransformationParameterSet());
//			for (int i=0; i<localPointGroup.size(); i++) {
//				System.out.println(localPointGroup.get(i)+"  "+localPointGroup.get(i).getStdX()+"  "+localPointGroup.get(i).getStdY()+"  "+localPointGroup.get(i).getStdZ());
//			}
//			double r = this.getGroupRedundance(localPointGroup);
//			double vTPv = this.getGroupOmega(localPointGroup);
//			System.out.println("sig02 = " + (vTPv/r)+"   "+vTPv+"   "+r);
//			System.out.println();
//		}
//		// DEBUG AUSGABEN		

		// Diese Meldungen liefern nicht false, sodass der User ggf. die Daten noch zu sehen bekommt.
		if (!isConverge) {
			System.err.println(this.getClass().getSimpleName() + " Fehler beim Bestimmen der Stochastischen Parameter");
			this.changes.firePropertyChange( "TRANSFORMATION_ITERATION_LIMIT_REACHED", Transformation.SQRT_EPS, this.maxDx );
		}
		if (!hasStochasticParameters) {
			int theoretic_dof = this.numberOfObservations-this.numberOfUnknownParameters+this.numberOfRestrictions+this.numberOfDatumConditions(this.datumPointGroup.getTransformationParameterSet());
			this.changes.firePropertyChange( "TRANSFORMATION_DEGREE_OF_FREEDOM_INACCURATE", this.degreeOfFreedom, theoretic_dof );
		}
		this.changes.firePropertyChange( "TRANSFORMATION_ERROR_FREE_ESTIMATION", false, true );
		return true;
	}
		
	private boolean calculateStochasticParameters() {
		int dim = this.getDimension();
		double dof = 0.0;
		int theoretic_dof = this.degreeOfFreedom();
		double sigma2apost = this.sigma2aprio;
		if (theoretic_dof > 0 && this.omega > Constant.EPS && this.applyVarianceFactorAposteriori)
			sigma2apost = this.omega/theoretic_dof;
		// Abstimmung des Tests auf 3D-Beobachtungen
		this.observationTestValues = null;
		try {
			this.observationTestValues = new ObservationTestValues(this.alpha, this.beta, dim, theoretic_dof);
		}
		catch (Exception e) {
			this.observationTestValues = new ObservationTestValues(this.alpha, this.beta, theoretic_dof);
		}
		final double kPrioPoint = this.observationTestValues.getKprioAB(dim);
		final double kPostPoint = this.applyVarianceFactorAposteriori ? this.observationTestValues.getKpostAB(dim) : kPrioPoint;
//		final double kPrioParam = this.observationTestValues.getKprioAB(1);
		final double kParam = this.applyVarianceFactorAposteriori ? this.observationTestValues.getKpostAB(1) : this.observationTestValues.getKprioAB(1);
		for (int lpAT=0; lpAT<this.srcPointGroups.size(); lpAT++) {
			if (this.isInterrupted) {
		    	System.err.println( "Berechnung vom Anwender abgebrochen!" );
		    	this.isInterrupted = false;
		    	this.changes.firePropertyChange( "TRANSFORMATION_INTERRUPT", false, true );
		    	return false;
		    }
			PointGroup localPointGroupAT = this.srcPointGroups.get(lpAT);
			TransformationParameterSet trafoSetAT = localPointGroupAT.getTransformationParameterSet();
			TransformationParameter[] trafoParamAT = trafoSetAT.getTransformationParameters();
			int localSystemSize = localPointGroupAT.size()*dim;
			// Matrix R = new DenseMatrix(Cll.numRows(), Cll.numColumns());
			// Setze Standardabweichung der Trafo-Parameter
			for (int i=0; i<trafoParamAT.length; i++) {
				int colN = trafoParamAT[i].getColInJacobiMatrix();
				double qxx = Math.abs(this.Qxx.get(colN, colN));
				if (Math.sqrt(qxx) > Transformation.SQRT_EPS) {
					double d = trafoParamAT[i].getValue() - trafoParamAT[i].getInitialisationValue();
					double tPrio = d*d/this.sigma2aprio/qxx;
					double tPost = d*d/sigma2apost/qxx;
					double t = this.applyVarianceFactorAposteriori ? tPost : tPrio;
					
					trafoParamAT[i].setSignificant(t > kParam);
				}
				trafoParamAT[i].setStd(Math.sqrt( sigma2apost*qxx ) );
				
			}
			Matrix Pnn = new UpperSymmBandMatrix(localSystemSize, dim-1);
			Vector Pv = new DenseVector(localSystemSize);

			for (int i=0; i<localPointGroupAT.size(); i++) {
				Point localPointAT  = localPointGroupAT.get(i);
				Point globalPointAT = this.trgPointGroup.get(localPointAT.getId());
				int colNPointAT = globalPointAT.getColInJacobiMatrix();
				int colAPointAT = localPointAT.getRowInJacobiMatrix();			
				this.changes.firePropertyChange( "TRANSFORMATION_CHECK_POINT", null, localPointAT.getId() );
				
				double redundancy[] = new double[dim];
				// Standardabweichung des lokalen Puktes
				double sigma[] = new double[dim];

				for (int cAT=0; cAT<dim; cAT++) {
					// Zeile der Redundanzmatrix R
					double redundanceRow[] = new double[localSystemSize];	

					// getJacobiElement(TransformationParameterSet trafoParam, Point globalPoint, int parameterType, int equationIndex)
					// getConditionElement(TransformationParameterSet trafoParam, int coordIndex, int equationIndex)
					// double aT[] = new double[this.numberOfUnknownParameters];
					double qxxaT[] = new double[this.numberOfUnknownParameters];

					for (int rowN=0; rowN<this.numberOfUnknownParameters; rowN++) {

						// Ableitungen nach dem Punkt im GLOBALEN-System
						for (int coordIndex=0; coordIndex<dim; coordIndex++) {
							double a   = this.getConditionElement(trafoSetAT, coordIndex, cAT);
							double qxx = this.Qxx.get(rowN, colNPointAT+coordIndex);
							//aT[colNPointAT+coordIndex] = a;
							qxxaT[rowN] += qxx*a;
						}
						
						// Ableitungen nach den Trafo-Parametern
						for (int j=0; j<trafoParamAT.length; j++) {
							int colN = trafoParamAT[j].getColInJacobiMatrix();
							double a = this.getJacobiElement(trafoSetAT, globalPointAT, trafoParamAT[j].getParameterTyp(), cAT);
							double qxx = this.Qxx.get(rowN, colN);
							//aT[colN] = a;
							qxxaT[rowN] += qxx*a;
						}
					}

					// Es steht eine Spalte aus Qxx*AT zur Verfuegung
					// Bestimme Q_ll = A*Qxx*AT
					double aqxxaT[] = new double[this.numberOfObservations];
					//for (PointGroup localPointGroupA : this.srcPointGroups) {
					for (int lpA=0; lpA<this.srcPointGroups.size(); lpA++) {
						PointGroup localPointGroupA = this.srcPointGroups.get(lpA);
						TransformationParameterSet trafoSetA = localPointGroupA.getTransformationParameterSet();
						TransformationParameter[] trafoParamA = trafoSetA.getTransformationParameters();
						for (int k=0; k<localPointGroupA.size(); k++) {
							Point localPointA  = localPointGroupA.get(k);
							Point globalPointA = this.trgPointGroup.get(localPointA.getId());
							int rowA = localPointA.getRowInJacobiMatrix();
							int colNPointA = globalPointA.getColInJacobiMatrix();
							for (int rA=0; rA<dim; rA++) {
								
								// Ableitungen nach dem Punkt im GLOBALEN-System
								for (int coordIndex=0; coordIndex<dim; coordIndex++) {
									double a = getConditionElement(trafoSetA, coordIndex, rA);
									aqxxaT[rowA+rA] += a*qxxaT[colNPointA+coordIndex];
								}
								
								// Ableitungen nach den Trafo-Parametern
								for (int j=0; j<trafoParamA.length; j++) {
									int colN = trafoParamA[j].getColInJacobiMatrix();
									double a = this.getJacobiElement(trafoSetA, globalPointA, trafoParamA[j].getParameterTyp(), rA);
									aqxxaT[rowA+rA] += a*qxxaT[colN];
								}
								
							}
						}
					}
				
//					// Bestimme Pv
//					for (int j=0; j<localPointGroupAT.size(); j++) {
//						Point pointC = localPointGroupAT.get(j);
//						int colP = pointC.getRowInJacobiMatrix();
//						double v[] = new double[dim];
//						v[0] = pointC.getX0() - pointC.getX();
//						v[1] = pointC.getY0() - pointC.getY();
//						v[2] = pointC.getZ0() - pointC.getZ();
//						double pv = 0;	
//						for (int cP=0; cP<dim; cP++) {	
//							double p = this.P.get(colP+cP,colAPointAT+cAT);
//							pv += p*v[cP];
//							
//						}
//						Pv.set(i*dim+cAT, Pv.get(i*dim+cAT) + pv);
//					}
					
					// Eine Zeile von Q_ll steht zur Verfuegung 
					// Bestimme Zeile der Redundanzmatrix R
					// Und die gewichteten Verbesserungen P*v
					for (int j=0; j<localPointGroupAT.size(); j++) {
						Point pointC = localPointGroupAT.get(j);
						int colP = pointC.getRowInJacobiMatrix();
						
						// Bestimme gewichtete Verbesserungen P*v
						double v[] = new double[dim];
						if (dim != 1) {
							v[0] = pointC.getX() - pointC.getX0();
							v[1] = pointC.getY() - pointC.getY0();
						}
						if (dim != 2)
							v[dim-1] = pointC.getZ() - pointC.getZ0();
						
						double pv = 0;	
						
						for (int cP=0; cP<dim; cP++) {
							double red = 0;
							double p = this.P.get(colP+cP,colAPointAT+cAT);
							pv += p*v[cP];
							
							for (int k=0; k<localPointGroupAT.size(); k++) {
								Point pointR = localPointGroupAT.get(k);
								int rowP = pointR.getRowInJacobiMatrix();
								for (int rP=0; rP<dim; rP++) {
									p = this.P.get(rowP+rP,colP+cP);
									red += aqxxaT[rowP+rP] * p;
								}
							}
							if (i*dim+cAT == j*dim+cP) {
								red = 1.0-red;
								redundancy[cAT] = Math.max(0.0, Math.min(1.0, Math.abs(red)));
								dof += red;
							}
							else
								red = -red;
							redundanceRow[j*dim+cP] = red;
							// R.set(i*dim+cAT, j*dim+cP, red);
						}
						Pv.set(i*dim+cAT, Pv.get(i*dim+cAT) + pv);
					}
					// Speichere Standardabweichungen des loaklen Punktes a-post
					sigma[cAT] = Math.sqrt(sigma2apost * Math.abs(aqxxaT[colAPointAT+cAT]));
					
					// Bestimme Redundanzanteile (nur HAUPTDIAGONALE)
//					double red = 0;
//					for (int j=0; j<localPointGroupAT.size(); j++) {
//						Point point = localPointGroupAT.get(j);
//						int colP = point.getRowInJacobiMatrix();
//						for (int cP=0; cP<dim; cP++) {
//							double p = this.P.get(colP+cP,colAPointAT+cAT);
//							// bestimme q_ll*p
//							red += aqxxaT[colP+cP] * p;
//						}
//					}
//					redundancy[cAT] = Math.abs(1.0-red);
//					dof += (1.0-red);


					// Eine Zeile der Redundanzmatrix steht zur Verfuegung
					// Bestimme Matrixprodukt Pnn = P*Qvv*P fuer Ausreissertest
					for (int j=0; j<localPointGroupAT.size(); j++) {
						Point pointR = localPointGroupAT.get(j);
						int rowP = pointR.getRowInJacobiMatrix();
			
						for (int cR=0; cR<dim; cR++) {
							double r = redundanceRow[j*dim+cR];
							for (int rP=0; rP<dim; rP++) {
								double p = this.P.get(rowP+rP, colAPointAT+cAT);
								Pnn.set(j*dim+rP, j*dim+cR, Pnn.get(j*dim+rP, j*dim+cR) + p*r);
							}
						}
					}
				}
				// Speichere Standardabweichungen des lokalen Punktes a-post und dessen Redundanzanteile
				if (dim != 1) {
					localPointAT.setStdX( sigma[0] );
					localPointAT.setStdY( sigma[1] );
				}
				if (dim != 2) {
					localPointAT.setStdZ( sigma[dim-1] );
				}				
				localPointAT.setRedundancy(redundancy);				
			}
			
			// Fuehre Ausreissertest durch
			for (int i=0; i<localPointGroupAT.size(); i++) {
				Point localPointAT = localPointGroupAT.get(i);
				if (localPointAT.getRedundancy() > Transformation.SQRT_EPS) {
					try {
						Matrix subQnn = new UpperSymmPackMatrix(dim);
						Vector subPv = new DenseVector(dim);
						Vector nabla = new DenseVector(dim);
						for (int r=0; r<dim; r++) {
							subPv.set(r, Pv.get(i*dim+r));
							for (int c=r; c<dim; c++) {
								subQnn.set(r,c, Pnn.get(i*dim+r, i*dim+c));	
							}
						}
						subQnn = MathExtension.pinv(subQnn, -1);
						subQnn.mult(subPv, nabla);
						
						double NablaQnnNabla = subPv.dot(nabla);
						double tPrio = Math.abs(NablaQnnNabla/dim/this.sigma2aprio);
						double tPost = tPrio;
						
						if (this.applyVarianceFactorAposteriori && theoretic_dof-dim > 0 && (this.omega - NablaQnnNabla) > Transformation.SQRT_EPS) {
							double sigma2apostPoint = (this.omega - NablaQnnNabla)/(theoretic_dof-dim);
							tPost = Math.abs(NablaQnnNabla/dim/sigma2apostPoint);
						}
						
						localPointAT.setNabla(Matrices.getArray(nabla.scale(-1.0)));
						localPointAT.setTprio(tPrio);
						localPointAT.setTpost(tPost);
						localPointAT.isOutlier(tPrio > kPrioPoint || tPost > kPostPoint);						
						
					} catch (NotConvergedException e) {
						e.printStackTrace();
					}					
				}
			}
		}

		// Bestimme Redundanzanteile der stoch. Punkte im GLOBALEN System
		// Hierbei gilt Qxx == Q_ll fuer GLOBALE-Punkte
		if (!this.isFreeNet) {
			int globalSystemSize = this.datumPointGroup.size()*dim;
			Matrix Pnn = new DenseMatrix(globalSystemSize, globalSystemSize);
			Vector Pv = new DenseVector(globalSystemSize);
			for (int i=0; i<this.datumPointGroup.size(); i++) {
				double redundancy[] = new double[dim];
				Point globalPointRN = this.datumPointGroup.get(i);
				int rowN = globalPointRN.getColInJacobiMatrix();
				
//				// Bestimme Redundanzanteile (Hauptdiagonale)		
//				int colPP = globalPointRN.getRowInJacobiMatrix();
//				int colNN = globalPointRN.getColInJacobiMatrix();
//				for (int cN=0; cN<dim; cN++) {
//					double red = 0;
//					for (int j=0; j<this.datumPointGroup.size(); j++) {
//						
//						Point globalPointR = this.datumPointGroup.get(j);
//						int rowNN = globalPointR.getColInJacobiMatrix();
//						int rowP = globalPointR.getRowInJacobiMatrix();
//						
//						for (int rN=0; rN<dim; rN++) {
//							double qll = this.Qxx.get(colNN+cN, rowNN+rN);
//							double p   = this.P.get(rowP+rN, colPP+cN);
//							red += qll*p;
//							
//							
//						}
//					}
//					redundancy[cN] = Math.abs(1.0-red);
//					dof += 1.0-red;
//				}
				
				for (int rN=0; rN<dim; rN++) {
					double redundanceRow[] = new double[globalSystemSize];
					for (int j=0; j<this.datumPointGroup.size(); j++) {
						Point globalPointCP = this.datumPointGroup.get(j);
						int colP = globalPointCP.getRowInJacobiMatrix();
						
						// Bestimme gewichtete Verbesserungen P*v
						double v[] = new double[dim];
						if (dim != 1) {
							v[0] = globalPointCP.getX() - globalPointCP.getX0();
							v[1] = globalPointCP.getY() - globalPointCP.getY0();
						}
						if (dim != 2)
							v[dim-1] = globalPointCP.getZ() - globalPointCP.getZ0();
						double pv = 0;	

						for (int cP=0; cP<dim; cP++) {
							double red = 0;
							double p = this.P.get(colP+cP,rowN+rN);
							pv += p*v[cP];
							for (int k=0; k<this.datumPointGroup.size(); k++) {
								Point globalPointCN = this.datumPointGroup.get(k);
								int colN = globalPointCN.getColInJacobiMatrix();
								for (int cN=0; cN<dim; cN++) {
									double qxx = this.Qxx.get(rowN+rN, colN+cN);
									p = this.P.get(colN+cN, colP+cP);
									red += qxx*p;
								}
							}
							if (i*dim+rN == j*dim+cP) {
								red = 1.0 - red;
								redundancy[rN] = Math.max(0.0, Math.min(1.0, Math.abs(red)));
								dof += red;
							}
							redundanceRow[j*dim+cP] = red;
							//R.set(i*dim+rN, j*dim+cP, red);
						}
						Pv.set(i*dim+rN, Pv.get(i*dim+rN) + pv);
					}
				
					// Eine Zeile der Redundanzmatrix steht zur Verfuegung
					// Bestimme Matrixprodukt Pnn = P*Qvv*P = P*R fuer Ausreissertest
					for (int j=0; j<this.datumPointGroup.size(); j++) {
						Point pointR = this.datumPointGroup.get(j);
						int rowP = pointR.getRowInJacobiMatrix();
			
						for (int cR=0; cR<dim; cR++) {
							double r = redundanceRow[j*dim+cR];
							for (int rP=0; rP<dim; rP++) {
								double p = this.P.get(rowP+rP, rowN+rN);
								Pnn.set(j*dim+rP, j*dim+cR, Pnn.get(j*dim+rP, j*dim+cR) + p*r);
							}
						}
					}
				}
				// Weise Redundanzanteile dem Punkt zu
				globalPointRN.setRedundancy(redundancy);	
			}
			
			// Fuehre Ausreissertest durch
			for (int i=0; i<this.datumPointGroup.size(); i++) {
				Point globalPoint = this.datumPointGroup.get(i);
				
				if (globalPoint.getRedundancy() > Transformation.SQRT_EPS) {
					try {
						Matrix subQnn = new UpperSymmPackMatrix(dim);
						Vector subPv = new DenseVector(dim);
						Vector nabla = new DenseVector(dim);
						for (int r=0; r<dim; r++) {
							subPv.set(r, Pv.get(i*dim+r));
							for (int c=r; c<dim; c++) {
								subQnn.set(r,c, Pnn.get(i*dim+r, i*dim+c));	
							}
						}
						subQnn = MathExtension.pinv(subQnn, -1);
						subQnn.mult(subPv, nabla);
						
						double NablaQnnNabla = subPv.dot(nabla);
						
						
						double tPrio = Math.abs(NablaQnnNabla/dim/this.sigma2aprio);
						double tPost = tPrio;
						
						if (this.applyVarianceFactorAposteriori && theoretic_dof-dim > 0 && (this.omega - NablaQnnNabla) > Transformation.SQRT_EPS) {
							double sigma2apostPoint = (this.omega - NablaQnnNabla)/(theoretic_dof-dim);
							tPost = Math.abs(NablaQnnNabla/dim/sigma2apostPoint);
						}
						
						globalPoint.setNabla(Matrices.getArray(nabla.scale(-1.0)));
						globalPoint.setTprio(tPrio);
						globalPoint.setTpost(tPost);
						globalPoint.isOutlier(tPrio > kPrioPoint || tPost > kPostPoint);						
						
					} catch (NotConvergedException e) {
						e.printStackTrace();
					}					
				}
			}
		}

		// Weise die Standardabweichungen den GLOBALEN Punkten zu
		for (int i=0; i<this.trgPointGroup.size(); i++) {
			Point globalPointC = this.trgPointGroup.get(i);
			int colN = globalPointC.getColInJacobiMatrix();
			
			if (dim != 1) {			
				globalPointC.setStdX( Math.sqrt( sigma2apost * Math.abs(this.Qxx.get(colN, colN)) ) );
				globalPointC.setStdY( Math.sqrt( sigma2apost * Math.abs(this.Qxx.get(colN+1, colN+1)) ) );
				
			}
			if (dim != 2) {
				globalPointC.setStdZ( Math.sqrt( sigma2apost * Math.abs(this.Qxx.get(colN+dim-1, colN+dim-1)) ) );
			}
		}
		
		this.degreeOfFreedom = dof;
		double limit = 0.1*theoretic_dof>5.0?5.0:0.1*theoretic_dof;
		// Ab hier steht sigma2apost zur Verfügung, da der Freiheitsgrad und die Verbessungsquadratsumme bekannt sind.		
		return Math.abs(Math.rint(this.degreeOfFreedom) - theoretic_dof) <= Math.ceil(limit);
	}
	
	private void transformTransformationParameters() {
		for (int i=0; i<this.srcPointGroups.size(); i++) {
			PointGroup pointGroup = this.srcPointGroups.get(i);
			this.changes.firePropertyChange( "TRANSFORMATION_TRANSFORM_PARAMETERS", this.srcPointGroups.size(), i );
			TransformationParameterSet trafoParam = pointGroup.getTransformationParameterSet();
			this.transformParameterSet(trafoParam, this.Qxx);
		}
	}
	
	private void updateUnknownParameters(Vector dx, boolean completeModel) {
		double vTPv = 0.0;
		int dim = this.getDimension();

		// Update der Punkte im Zielsystem
		for (int i=0; i<this.trgPointGroup.size(); i++) {
			Point point = this.trgPointGroup.get(i);

			int col = point.getColInJacobiMatrix();
			if (dim != 1) {
				double value = dx.get(col++); 
				this.maxDx = Math.max(this.maxDx, Math.abs(value));
				point.setX( point.getX() + value );
				value = dx.get(col++);	
				this.maxDx = Math.max(this.maxDx, Math.abs(value));
				point.setY( point.getY() + value );
			}
			if (dim != 2) {
				double value = dx.get(col++); 
				this.maxDx = Math.max(this.maxDx, Math.abs(value));
				point.setZ( point.getZ() + value );
			}

		}

		// Update der Trafo-Parameter und der lokalen Punkte
		for (PointGroup localPointGroup : this.srcPointGroups) {
			TransformationParameterSet trafoParamSet = localPointGroup.getTransformationParameterSet();
			TransformationParameter[] trafoParams = trafoParamSet.getTransformationParameters();
			for (int j=0; j<trafoParams.length; j++) {
				TransformationParameter trafoParam = trafoParams[j];
				int col = trafoParam.getColInJacobiMatrix();
				double value = dx.get(col);
				//this.maxDx = Math.max(this.maxDx, Math.abs(value));
				trafoParam.setValue(trafoParam.getValue() + value);	
			}
			
			if (completeModel) {
				for (int i=0; i<localPointGroup.size(); i++) {
					Point localPoint  = localPointGroup.get(i);
					Point globalPoint = this.trgPointGroup.get(localPoint.getId());
					
					if (dim != 1) {
						double vx = this.getContradiction(trafoParamSet, globalPoint, 0, localPoint);
						double vy = this.getContradiction(trafoParamSet, globalPoint, 1, localPoint);
						
						localPoint.setX( localPoint.getX0() + vx );
						localPoint.setY( localPoint.getY0() + vy );
					}
					if (dim != 2) {
						double vz = this.getContradiction(trafoParamSet, globalPoint, dim-1, localPoint);
						
						localPoint.setZ( localPoint.getZ0() + vz );
					}
				}
			}
		}
		
		if (completeModel) {
			for (PointGroup localPointGroup : this.srcPointGroups) {
				vTPv += this.getGroupOmega(localPointGroup);
			}

			if (!this.isFreeNet) {
				vTPv += this.getGroupOmega(this.datumPointGroup);
			}
			
			this.omega = vTPv;
		}
	}
		
	/**
	 * Liefert den Redundanzanteil einer Punktgruppe
	 * @param pointGroup
	 * @return rGroup
	 */
	protected double getGroupRedundance(PointGroup pointGroup) {
		double rGroup=0;
		for (int i=0; i<pointGroup.size(); i++) {
			Point p = pointGroup.get(i);
			rGroup += p.getRedundancy();
		}
		return rGroup;	
	}
	
	/**
	 * Bestimmt die Verbesserungsquadratsumme der Punktgruppe &Omega;<sub>Group</sub> = [v<sup>T</sup>Pv]<sub>Group</sub>
	 * @param pointGroup
	 * @return omega
	 */
	protected double getGroupOmega(PointGroup pointGroup) {
		int dim = this.getDimension();
		double vTPv = 0.0;
		
		for (int i=0; i<pointGroup.size(); i++) {
			Point pointR = pointGroup.get(i);
			int rowA = pointR.getRowInJacobiMatrix();
			double omegaPointR = 0.0;
			Vector vR = new DenseVector(dim);
			if (dim != 1) {
				vR.set(0, pointR.getX0() - pointR.getX());
				vR.set(1, pointR.getY0() - pointR.getY());
			}
			if (dim != 2) {
				vR.set(dim-1, pointR.getZ0() - pointR.getZ());
			}
			
			for (int rA=0; rA<dim; rA++) {
				double vP = 0.0;
				for (int j=0; j<pointGroup.size(); j++) {
					Point pointC = pointGroup.get(j);
					int colP = pointC.getRowInJacobiMatrix();
					
					if (dim != 1) {
						double px = this.P.get(rowA+rA, colP++);
						double py = this.P.get(rowA+rA, colP++);
						
						double vx = pointC.getX0() - pointC.getX();
						double vy = pointC.getY0() - pointC.getY();

						vP += px*vx + py*vy;
					}
					if (dim != 2) {
						double pz = this.P.get(rowA+rA, colP);
						double vz = pointC.getZ0() - pointC.getZ();
						
						vP += pz*vz;
					}
				}
				omegaPointR += vR.get(rA) * vP;
			}
			pointR.setOmega(omegaPointR/this.sigma2aprio);
			vTPv += omegaPointR;
		}

		return vTPv;
	}
		
	/**
	 * Aufstellung des Normalgleichungssystems ohne zwischenspeicherung der Jacobi-Matrix
	 * Bedingungen werden dem System am Ende hinzugefuegt, sodass NES bereits geraendert ist.
	 * 
	 * Modell entspricht einem GAUSS-MARKOV-MODELL. Unsicherheiten im Zielsystem werden hierbei
	 * durch direkte Beobachtungen beruecksichtigt - vgl. die Arbeiten von Koch. 
	 * 
	 * <pre>
	 * |N  R| * |x|  =  |n| 
	 * |R' 0|   |k|     |r|
	 * </pre>
	 * 
	 * @return NES
	 */
	private NormalEquationSystem getSystemOfEquations() {
		int numberOfDatumConditions = 0;
		if (this.isFreeNet) {
			TransformationParameterSet trafoParamDatumSet = this.datumPointGroup.getTransformationParameterSet();
			for (PointGroup localPointGroup : this.srcPointGroups) {
				TransformationParameterSet trafoParamSet = localPointGroup.getTransformationParameterSet();
				// Reduziere die notwendigen Datumsparameter fuer die freie AGL
				for (int i=0; i<trafoParamSet.numberOfRestrictions(); i++) {
					trafoParamDatumSet.setRestriction(trafoParamSet.getRestriction(i));
				}
			}
			numberOfDatumConditions = this.numberOfDatumConditions(trafoParamDatumSet);
		}

		int sizeN = numberOfDatumConditions;
		sizeN += this.numberOfUnknownParameters+this.numberOfRestrictions;
		Matrix N = new UpperSymmPackMatrix(sizeN);
		Vector n = new DenseVector(sizeN);
		int dim = this.getDimension();
		
		for (UnknownParameter unknownParameter1 : this.unknownParameters) {
			int paramType1 = unknownParameter1.getParameterTyp();
			int colN = unknownParameter1.getColInJacobiMatrix();

			
			int paramDim1 = paramType1 == UnknownParameter.TYPE_POINT3D?3:1;
			
			for (int cN=0; cN<paramDim1; cN++) {
				double pa[] = new double[this.numberOfObservations];
				// Unbekannter ist ein Punkt
				if (paramType1 == UnknownParameter.TYPE_POINT3D && unknownParameter1 instanceof GlobalPoint) {
					// zu bestimmender Punkt im globalen System
					GlobalPoint3D globalPoint = (GlobalPoint3D)unknownParameter1;

					for (int g=0; g<globalPoint.numberOfLocalPointGroups(); g++) {
						PointGroup group = globalPoint.getLocalPointGroup(g);
						// Idetische Punkt im lokalen System
						Point localPoint = group.get(globalPoint.getId());
						// rowP + r --> Entspricht der Zeile  in der A-Matrix, die gegenwaertig bestimmt wird
						// colP + c --> Entspricht der Spalte in der P-Matrix, die gegenwaertig bestimmt wird
						int colP = localPoint.getRowInJacobiMatrix();
						TransformationParameterSet trafoParam = group.getTransformationParameterSet();
						
						// Druchlaufe nur die Subsysteme, in denen der Punkt auch enthalten ist.
						for (int t=0; t<globalPoint.numberOfLocalPointGroups(); t++) {
							PointGroup groupR = globalPoint.getLocalPointGroup(t);
							for (int k=0; k<groupR.size(); k++) {
								Point pointR = groupR.get(k);
								int rowP = pointR.getRowInJacobiMatrix();
								for (int rP=0; rP<dim; rP++) {
									for (int c=0; c<dim; c++) {
										double a = this.getConditionElement(trafoParam,  cN, c);
										double p = this.P.get(rowP+rP, colP+c);
										pa[rowP+rP] += a*p;
									}
								}
							}
						}
						
//						for (int r=0; r<this.numberOfObservations; r++) {
//							for (int c=0; c<dim; c++) {
//								double a = this.getConditionElement(trafoParam,  cN, c);
//								double p = this.P.get(r, colP+c);
//								pa[r] += a*p;
//							}
//						}
						
					}
				}
				// Unbekannter ist ein Trafo-Parameter
				else {
					PointGroup group = unknownParameter1.getPointGroup();
					TransformationParameterSet trafoParam = group.getTransformationParameterSet();

					for (int i=0; i<group.size(); i++) {
						Point point1 = group.get(i);
						// rowP + r --> Entspricht der Zeile in der A-Matrix, die gegenwartig bestimmt wird
						int rowP = point1.getRowInJacobiMatrix();

						for (int j=0; j<group.size(); j++) {
							Point point2 = group.get(j);
							Point globalPoint = this.trgPointGroup.get(point2.getId());
							// colP + c --> Entspricht der Spalte in der P-Matrix, die gegenwartig bestimmt wird
							int colP = point2.getRowInJacobiMatrix();
							
							for (int r=0; r<dim; r++) {		
								for (int c=0; c<dim; c++) {
									// 	Ableitung am Punkt P2 fuer unbeaknnten Parameter param
									double a = this.getJacobiElement(trafoParam, globalPoint, paramType1, c);
									double p = this.P.get(rowP+r, colP+c);
									pa[rowP+r] += a*p;
								}
							}
							
						}
					}
				}
				//System.out.println(this.numberOfUnknownParameters+"  "+pa.length+"  "+java.util.Arrays.toString(pa));
				// Hier steht eine Spalte von PA zur verfuegung
				// n = AT*Pw
				
//				double aTpw = 0;
//				for (UnknownParameter unknownParameter2 : this.unknownParameters) {
//					int paramType2 = unknownParameter2.getParameterTyp();
//					
//					if (paramType2 == UnknownParameter.TYPE_POINT3D && unknownParameter2 instanceof GlobalPoint) {
//						// zu bestimmender Punkt im globalen System
//						GlobalPoint3D globalPoint = (GlobalPoint3D)unknownParameter2;
//						for (int g=0; g<globalPoint.numberOfLocalPointGroups(); g++) {
//							PointGroup localGroup = globalPoint.getLocalPointGroup(g);
//							TransformationParameterSet trafoParam = localGroup.getTransformationParameterSet();
//							Point srcPoint = localGroup.get(globalPoint.getId());
//							int rowW = srcPoint.getRowInJacobiMatrix();
//							for (int r=0; r<dim; r++) {
//								double w = this.getContradiction(trafoParam, globalPoint, r, srcPoint);
//								aTpw += pa[rowW+r]*w;
//							}
//						}
//					}
//				}
//				n.set(colN+cN, aTpw);

				
				double aTpw = 0;
				// N = AT*PA
				for (UnknownParameter unknownParameter2 : this.unknownParameters) {
					int paramType2 = unknownParameter2.getParameterTyp();
					int rowN = unknownParameter2.getColInJacobiMatrix();
					int paramDim2 = paramType2 == UnknownParameter.TYPE_POINT3D?3:1;
					for (int rN=0; rN<paramDim2; rN++) {
						double aTpa = 0.0;
						// Unbekannter ist ein Punkt
						if (paramType2 == UnknownParameter.TYPE_POINT3D && unknownParameter2 instanceof GlobalPoint) {
							// zu bestimmender Punkt im globalen System
							GlobalPoint3D globalPoint = (GlobalPoint3D)unknownParameter2;
							for (int g=0; g<globalPoint.numberOfLocalPointGroups(); g++) {
								PointGroup group = globalPoint.getLocalPointGroup(g);
								// Idetische Punkt im lokalen System
								Point localPoint = group.get(globalPoint.getId());
								// colP + c --> Entspricht der Spalte in der AT-Matrix, die gegenwartig bestimmt wird
								int colP = localPoint.getRowInJacobiMatrix();
								TransformationParameterSet trafoParam = group.getTransformationParameterSet();

								for (int c=0; c<dim; c++) {
									double aT = this.getConditionElement(trafoParam, rN, c);
									aTpa += aT*pa[colP+c];
									
									if (rN == 0) {
										double w = -this.getContradiction(trafoParam, globalPoint, c, localPoint);
										aTpw += pa[colP+c]*w;
									}
									
								}
								
							}
						}
						
						// Unbekannter ist ein Trafo-Parameter
						else {
							PointGroup group = unknownParameter2.getPointGroup();
							TransformationParameterSet trafoParam = group.getTransformationParameterSet();

							for (int i=0; i<group.size(); i++) {
								Point point1 = group.get(i);
								Point globalPoint = this.trgPointGroup.get(point1.getId());
								// colP + c --> Entspricht der Zeile in der AT-Matrix, die gegenwartig bestimmt wird
								int colP = point1.getRowInJacobiMatrix();
								
								for (int c=0; c<dim; c++) {
									double aT = this.getJacobiElement(trafoParam, globalPoint, paramType2, c);
									aTpa += aT*pa[colP+c];
								}
							}
						}
						// Setze N
						N.set(rowN+rN, colN+cN, aTpa);
					}

				}
				n.set(colN+cN, aTpw);
			}
			
		}
		
		if (!this.isFreeNet) {
			// addiere inverse CoVar der Anschlusspunkte auf N
			for (int i=0; i<this.datumPointGroup.size(); i++) {
				Point point1 = this.datumPointGroup.get(i);
				int rowP = point1.getRowInJacobiMatrix();
				int colN = point1.getColInJacobiMatrix();

				if (colN < 0 || rowP < 0)
					continue;

				for (int d1=0; d1<dim; d1++) {

					for (int j=0; j<this.datumPointGroup.size(); j++) {
						Point point2 = this.datumPointGroup.get(j);
						int colP = point2.getRowInJacobiMatrix();
						int rowN = point2.getColInJacobiMatrix();

						if (rowN < 0 || colP < 0)
							continue;

						double w[] = new double[dim];
						if (dim != 1) {
							w[0] = point2.getX0() - point2.getX();
							w[1] = point2.getY0() - point2.getY();
						}

						if (dim != 2) {
							w[dim-1] = point2.getZ0() - point2.getZ();
						}

						for (int d2=0; d2<dim; d2++) {
							n.set(colN+d1, n.get(colN+d1) + w[d2]*this.P.get(rowP+d1, colP+d2));
							N.set(rowN+d2, colN+d1, N.get(rowN+d2, colN+d1) + this.P.get(rowP+d1, colP+d2));
						}
					}
				}				
			}
		}
		
		// Fuege Restriktionen hinzu
		int rowN = this.numberOfUnknownParameters;
		for (PointGroup localPointGroup : this.srcPointGroups) {
			TransformationParameterSet trafoParamSet = localPointGroup.getTransformationParameterSet();
			TransformationParameter[] trafoParams = trafoParamSet.getTransformationParameters();
			for (int i=0; i<trafoParamSet.numberOfRestrictions(); i++, rowN++) {
				for (int j=0; j<trafoParams.length; j++) {
					int colN = trafoParams[j].getColInJacobiMatrix();
					//N.set(rowN, colN, this.getRestrictionElement(trafoParamSet, j, i));
					N.set(colN, rowN, this.getRestrictionElement(trafoParamSet, j, i));
				}
				n.set(rowN, -this.getRestrictionContradiction(trafoParamSet, i));	
			}
		}
		
		// Fuege Datumsbedingungen hinzu
		if (this.isFreeNet) {
			this.addDatumConditions(this.datumPointGroup, N);
		}
		
//		System.out.println();
//		MathExtension.print(N);
//
//		System.out.println();
//		MathExtension.print(n);
		
		return new NormalEquationSystem(N, n);
	}
	
	/**
	 * Liefert die Testgroesse fuer den Globaltest
	 * @return TGlobal
	 */
	public double getTglobal() {
		return this.getVarianceFactorAposteriori()/this.sigma2aprio;
	}
	
	/**
	 * Liefert eine Liste mit allen Punktnummern, die nsowohl im Start- als auch im Zielsystem vorhanden sind
	 * @param group1
	 * @param group2
	 * @return identicalPointIds
	 */
	private Set<String> identicalPointIds(PointGroup group1, PointGroup group2) {
		Set<String> pointIds = new LinkedHashSet<String>();
		for (int i=0; i<group1.size(); i++) {
			String pointId = group1.get(i).getId();
			for (int j=0; j<group2.size(); j++) {
				if (group2.get(j).getId().equals(pointId)) {
					pointIds.add(pointId);
					break;
				}
			}
		}
		return pointIds;
	}
	
	/**
	 * Liefert <code>true</code>, wenn eine freie
	 * Netzausgleichung durchgefuehrt wird
	 * @return isFreeNetAdjustment
	 */
	public boolean isFreeNetAdjustment() {
		return this.isFreeNet;
	}
	
	/**
	 * Legt fest, ob eine freie
	 * Netzausgleichung durchgefuehrt werden soll
	 * @param freeNetAdjustment
	 */
	public void setFreeNetAdjustment(boolean freeNetAdjustment) {
		this.isFreeNet = freeNetAdjustment;
	}
	
	/**
	 * Liefert den Freiheitsgrad der Ausgleichung
	 * f = n-u+d+r=spur(R)
	 * @return degreeOfFreedom
	 */
	public int degreeOfFreedom() {
		return this.numberOfObservations-this.numberOfUnknownParameters+this.numberOfRestrictions+this.numberOfDatumConditions(this.datumPointGroup.getTransformationParameterSet());
	}
	
	/**
	 * Liefert die Quadratsumme der Verbesserungen &Omega; = v<sup>T</sup>Pv
	 * @return omega
	 */
	public double getOmega() {
		return this.omega;
	}

	/**
	 * Liefert den Varianzfaktor a-post, 
	 * wenn &Omega; = v<sup>T</sup>Pv <= 0 oder f = n-u+d+r <= 0 sind,
	 * wird der Varianzfaktor a-priori ausgegeben! 
	 * @return sigma2apost
	 */
	public double getVarianceFactorAposteriori() {
		int dof = this.degreeOfFreedom();
		if (dof <= 0 || this.omega <= Constant.EPS || !this.applyVarianceFactorAposteriori)
			return this.sigma2aprio;
		return this.omega/(double)dof;
	}
	
	/**
	 * Liefert true, wenn der Schätzwert fuer den Varianzfaktor verwendet werden soll
	 * @return applyVarianceFactorAposteriori
	 */
	public boolean applyVarianceFactorAposteriori() {
		return this.applyVarianceFactorAposteriori;
	}
	
	/**
	 * Legt fest, ob  der Schätzwert fuer den Varianzfaktor verwendet werden soll
	 * @param applyVarianceFactorAposteriori
	 */
	public void applyVarianceFactorAposteriori(boolean applyVarianceFactorAposteriori) {
		this.applyVarianceFactorAposteriori = applyVarianceFactorAposteriori;
	}
	
	/**
	 * Liefert den Varianzfaktor a-priori, 
	 * @return sigma2apost
	 */
	public double getVarianceFactorApriori() {
		return this.sigma2aprio;
	}
	
	/**
	 * Liefert die Testgrößen nach der AGL
	 * @return testValues
	 */
	public ObservationTestValues getObservationTestValues() {
		return this.observationTestValues;
	}
	
	/**
	 * Liefert eine Liste mit allen Punktgruppen, die in der
	 * Ausgleichung beruecksichtigt wurden
	 * @return pointGroups
	 */
	public List<PointGroup> getSourcePointGroups() {
		return this.srcPointGroups;
	}
	
	/**
	 * Liefert das vollständige Zielsystem
	 * @return targetSystem
	 */
	public PointGroup getTargetPointGroup() {
		return this.trgPointGroup;
	}
	
	/**
	 * Setzt Macht des Tests &beta; [%] (60% &lt; &beta; &lt; 100%)
	 * @param newBeta Macht des Tests &beta; [%]
	 */
	public void setTestPowerValue(double newBeta) {
		if (newBeta > 60 && newBeta < 100)
			this.beta = newBeta;	
	}	
	
	/**
	 * Setzt die Irrtumswahrscheinlichkeit &alpha; [%] (0% &lt; &alpha; &lt; 30%)
	 * @param newAlpha neue Irrtumswahrscheinlichkeit &alpha; [%]
	 */
	public void setProbabilityValue(double newAlpha) {
		if (newAlpha > 0 && newAlpha < 30)
			this.alpha = newAlpha;	
	}
	
	/**
	 * Legt die max. Anzahl an Iterationen fest (0 &lt; i &lt; 10000)
	 * @param newMaxIteration Iterationsanzhal
	 */
	public void setMaxIteration(int newMaxIteration) {
		if (newMaxIteration >= 0 && newMaxIteration <= 10000 )
			this.maxIteration = newMaxIteration;
	}
	
	/**
	 * Anzahl der Unbekannten pro Subsystem
	 * @return n
	 */
	public abstract int numberOfUnknownsPerSubsystem();
	
	/**
	 * Anzahl der notwendigen homologen Punkte
	 * @return n
	 */
	public abstract int numberOfRequiredHomologousPointsPerSubsystem();
	
	/**
	 * Dimension der Punkte, die in der Buendelausgleichung zulaessig sind
	 * @return dim
	 */
	public abstract int getDimension();
	
	/**
	 * Liefert die Ableitungen der Transformationsgleichungen an der Stelle des Transformationsparameters
	 * @param trafoParam
	 * @param point
	 * @param parameterType
	 * @param equationIndex
	 * @return a
	 */
	public abstract double getJacobiElement(TransformationParameterSet trafoParam, Point point, int parameterType, int equationIndex);
	
	/**
	 * Liefert die Ableitungen der Transformationsgleichungen an der Stelle eines Punktes
	 * @param trafoParam
	 * @param coordIndex
	 * @param equationIndex
	 * @return b
	 */
	public abstract double getConditionElement(TransformationParameterSet trafoParam, int coordIndex, int equationIndex);
	
	/**
	 * Liefert den Widerspruch einer Gleichung
	 * @param trafoParam
	 * @param srcPoint
	 * @param equationIndex
	 * @param trgPoint
	 * @return w
	 */
	public abstract double getContradiction(TransformationParameterSet trafoParam, Point srcPoint, int equationIndex, Point trgPoint);
	
	/**
	 * Liefert die Gleichung des Widerspruchs
	 * @param trafoParam
	 * @param parameterIndex
	 * @param equationIndex
	 * @return R
	 */
	public abstract double getRestrictionElement(TransformationParameterSet trafoParam, int parameterIndex, int equationIndex);
	
	/**
	 * Liefert den Widerspruch der Bedingungsgleichung
	 * @param trafoParam
	 * @param equationIndex
	 * @return r
	 */
	public abstract double getRestrictionContradiction(TransformationParameterSet trafoParam, int equationIndex);
	
	/**
	 * Liefert die Anzahl der Datumsbedingungen bei freier AGL
	 * @param trafoParamSet
	 * @return d
	 */
	public abstract int numberOfDatumConditions(TransformationParameterSet trafoParamSet);
	
	/**
	 * Fuege der Matrix M die fehlenden Datumsbedingungen d hinzu
	 * @param datumPointGroup
	 * @param M
	 * @return M
	 */
	public abstract Matrix addDatumConditions(PointGroup datumPointGroup, Matrix M);
	
	/**
	 * Liefert die Jacobi-Matrix
	 * @return A
	 * @deprecated Nur fuer Debugausgaben
	 */
	Matrix getJacobiMatrix() {
		Matrix A = new DenseMatrix(this.numberOfObservations, this.numberOfUnknownParameters);
		int dim = this.getDimension();
		if (!this.isFreeNet) {
			for (int i=0; i<this.datumPointGroup.size(); i++) {
				Point point = this.datumPointGroup.get(i);
				int colN = point.getColInJacobiMatrix();
				for (int c=0; c<dim; c++) {
					A.set(colN+c, colN+c, 1.0);
				}
			}
		}
		for (PointGroup localPointGroupA : this.srcPointGroups) {
			TransformationParameterSet trafoSetA = localPointGroupA.getTransformationParameterSet();
			TransformationParameter[] trafoParamA = trafoSetA.getTransformationParameters();
			for (int k=0; k<localPointGroupA.size(); k++) {
				Point localPointA  = localPointGroupA.get(k);
				Point globalPointA = this.trgPointGroup.get(localPointA.getId());
				int rowA = localPointA.getRowInJacobiMatrix();
				int colNPointA = globalPointA.getColInJacobiMatrix();
				for (int rA=0; rA<dim; rA++) {
					// Ableitungen nach den Trafo-Parametern
					for (int j=0; j<trafoParamA.length; j++) {
						int colN = trafoParamA[j].getColInJacobiMatrix();
						double a = this.getJacobiElement(trafoSetA, globalPointA, trafoParamA[j].getParameterTyp(), rA);
						A.set(rowA+rA, colN, a);
					}
					
					// Ableitungen nach dem Punkt im GLOBALEN-System
					for (int coordIndex=0; coordIndex<dim; coordIndex++) {
						double a = getConditionElement(trafoSetA, coordIndex, rA);
						A.set(rowA+rA, colNPointA+coordIndex, a);
					}
					
				}
			}
		}
		return A;
	}
	
	/**
	 * Fuer DEBUG - Exportiert Matrizen A, P, B, v
	 * @return A
	 * @deprecated
	 */
	boolean exportMatricesToFile(File f) {
		Matrix A = this.getJacobiMatrix();
		int dim = this.getDimension();

		boolean isComplete = false;
    	PrintWriter pwM = null;
    	PrintWriter pwI = null;
    	PrintWriter pwP = null;
    	PrintWriter pwB = null;
    	PrintWriter pwE = null;
    	PrintWriter pwQ = null;
    	try {
    		Vector v = new DenseVector(this.numberOfObservations);
    		for (PointGroup localPointGroupA : this.srcPointGroups) {
    			for (int k=0; k<localPointGroupA.size(); k++) {
    				Point point = localPointGroupA.get(k);
    				int rowA = point.getRowInJacobiMatrix();
    				
    				if (dim != 1) {
    					v.set(rowA++, point.getX0() - point.getX());
    					v.set(rowA++, point.getY0() - point.getY());
    	    		}
    	    		if (dim != 2) {
    	    			v.set(rowA++, point.getZ0() - point.getZ());
    	    		}
    			}
    		}

    		double sigma2apost = this.getVarianceFactorAposteriori();
    		double scale = this.sigma2aprio / sigma2apost;
    		pwQ = new PrintWriter(new BufferedWriter(new FileWriter( new File(f + "_cofactor.txt") )));
    		for (int i=0; i<this.Qxx.numRows(); i++) {
    			for (int j=0; j<this.Qxx.numColumns(); j++) {
    				pwQ.printf(Locale.ENGLISH, "%+35.18f  ",  scale * this.Qxx.get(i,j));
    			}
    			pwQ.println();
    		}
    		pwQ.flush();
    		
    		pwE = new PrintWriter(new BufferedWriter(new FileWriter( new File(f + "_residuals.txt") )));
    		for (int i=0; i<v.size(); i++) {
    			pwE.printf(Locale.ENGLISH, "%+35.18f%n", v.get(i));
    		}
    		pwE.flush();
    		
    		pwP = new PrintWriter(new BufferedWriter(new FileWriter( new File(f + "_weight.txt") )));
    		for (int i=0; i<this.P.numRows(); i++) {
    			for (int j=0; j<this.P.numColumns(); j++) {
    				pwP.printf(Locale.ENGLISH, "%+35.18f  ", this.P.get(i,j));
    			}
    			pwP.println();
    		}
    		pwP.flush();
    		
    		pwM = new PrintWriter(new BufferedWriter(new FileWriter( new File(f + "_jacobi.txt") )));
    		for (int i=0; i<A.numRows(); i++) {
    			for (int j=0; j<A.numColumns(); j++) {
    				pwM.printf(Locale.ENGLISH, "%+35.18f  ", A.get(i,j));
    			}
    			pwM.println();
    		}
    		pwM.flush();
    		
    		    		
    		pwB = new PrintWriter(new BufferedWriter(new FileWriter( new File(f + "_condition.txt") )));
    		NormalEquationSystem nes = this.getSystemOfEquations();
    		Matrix N = nes.getNmatrix();
    		
    		for (int i=this.numberOfUnknownParameters; i<N.numRows(); i++) {
    			for (int j=0; j<this.numberOfUnknownParameters; j++) {
    				pwB.printf(Locale.ENGLISH, "%+35.18f  ", N.get(i,j));
    			}
    			pwB.println();
    		}
    		pwB.flush();
    		
    		
    		
    		pwI = new PrintWriter(new BufferedWriter(new FileWriter( new File(f + "_jacobi_info.txt") )));
    		String format = "%5s\t%15s\t%35s\t%5s\t%10d%n";

    		for (PointGroup localPointGroupA : this.srcPointGroups) {
    			Point startPoint = localPointGroupA.get(0);
    			int gid = localPointGroupA.getId();
    			for (int k=0; k<localPointGroupA.size(); k++) {
    				Point endPoint = localPointGroupA.get(k);
    				int rowA = endPoint.getRowInJacobiMatrix();
    				if (dim != 1) {
    					pwI.printf(Locale.ENGLISH, format, gid, startPoint.getId(), endPoint.getId(), 'X', rowA++);
    					pwI.printf(Locale.ENGLISH, format, gid, startPoint.getId(), endPoint.getId(), 'Y', rowA++);
    	    		}
    	    		if (dim != 2) {
    	    			pwI.printf(Locale.ENGLISH, format, gid, startPoint.getId(), endPoint.getId(), 'Z', rowA++);
    	    		}
    			}
    		}
    		pwI.flush();
    		isComplete = true;
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	finally {
    		if (pwI != null)
    			pwI.close();
    		if (pwM != null)
    			pwM.close();
    		if (pwP != null)
    			pwP.close();
    		if (pwB != null)
    			pwB.close();
    		if (pwE != null)
    			pwE.close();
    		if (pwQ != null)
    			pwQ.close();
    	}
 
    	return isComplete;
	}
	
	/**
	 * Schreibt punktbezogene Informationen zur CoVar raus
	 * @param f
	 * @return isWritten
	 */
	public boolean exportCovarianceMatrixInfoToFile(File f) {
		// noch keine Loesung vorhanden
		if (this.Qxx == null)
    		return false;
		
		boolean isComplete = false;
    	PrintWriter pw = null;
    	try {
    		pw = new PrintWriter(new BufferedWriter(new FileWriter( f )));
    						//Pkt,Type(XYZ),Coord,Row in NGL
    		String format = "%25s\t%5s\t%35.15f\t%10d%n";   		
    		
    		for (UnknownParameter parameter : this.unknownParameters) {
    			if (!(parameter instanceof Point))
    				continue;
    			
    			Point point = (Point)parameter;
    			int colInJacobi = point.getColInJacobiMatrix();
    			int dim = point.getDimension();
    			if (colInJacobi < 0)
    				continue;
    			
    			if (dim != 1) {
    				pw.printf(Locale.ENGLISH, format, point.getId(), 'X', point.getX(), colInJacobi++);
    				pw.printf(Locale.ENGLISH, format, point.getId(), 'Y', point.getY(), colInJacobi++);
    			}
    			if (dim != 2) {
    				pw.printf(Locale.ENGLISH, format, point.getId(), 'Z', point.getZ(), colInJacobi++);
    			}
				
    		}
    		isComplete = true;
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	finally {
    		if (pw != null) {
    			pw.close();
    		}
    	}
 
    	return isComplete;
	}
	
	/**
	 * Schreibt die CoVar raus
	 * @param f
	 * @return isWritten
	 */
	public boolean exportCovarianceMatrixToFile(File f) {
		// noch keine Loesung vorhanden
		if (this.Qxx == null)
			return false;
    	boolean isComplete = false, DEBUG = false;
    	PrintWriter pw = null;
    	int size = this.Qxx.numRows();
    	try {
    		pw = new PrintWriter(new BufferedWriter(new FileWriter( f )));

    		for (int i=0; i<size; i++) {
    			for (int j=0; j<size; j++) {
    				pw.printf(Locale.ENGLISH, "%+35.18f  ", this.Qxx.get(i,j));
    			}
    			pw.println();
    		}
    		isComplete = true;
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	finally {
    		if (pw != null) {
    			pw.close();
    		}
    	}
    	
    	if (isComplete && DEBUG) 
    		isComplete = isComplete && exportMatricesToFile(f);

    	return isComplete;
    }
	
	public void addPropertyChangeListener( PropertyChangeListener l ) {
		this.changes.addPropertyChangeListener( l );
	}

	public void removePropertyChangeListener( PropertyChangeListener l ) {
		this.changes.removePropertyChangeListener( l );
	}
	
	/**
	 * Bricht Iteration an der naechst moeglichen Stelle ab
	 */
	public void interrupt() {
		this.isInterrupted = true;
	}
	
	@Override
	public void run() {
		this.estimate();
	}
}
