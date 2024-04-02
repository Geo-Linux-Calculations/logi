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

package com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PolarPointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.correlation.CorrelationFunction;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.PolarPoint3D;

public class LasertrackerUncertaintyModelUT extends LasertrackerUncertaintyModel {
	
	private class SigmaPointSet {
		private int n;
		private double sigmaPointSet[][];
		private int columnIndex[];
		private double scale;
		public SigmaPointSet(int n, double scale) {
			this.n = n;
			this.scale = scale;
			this.sigmaPointSet = new double[n][];
			this.columnIndex   = new int[n];
		}
		
		public void setValue(int row, int column, double v) {
			this.setValue(row, column, new double[] {v});
		}
		
		public void setValue(int row, int column, double v[]) {
			this.columnIndex[row] = column;
			this.sigmaPointSet[row] = new double[v.length];
			System.arraycopy(v, 0, this.sigmaPointSet[row], 0, v.length);
		}
		
		public double getValue(int row, int column) {
			// Hauptdiagonale
			if (row == column && this.sigmaPointSet[row] != null) {
				return this.scale*this.sigmaPointSet[row][0];
			}
			else if (row - this.n == column && row < 2*this.n && this.sigmaPointSet[row-this.n] != null)
				return -this.scale*this.sigmaPointSet[row-this.n][0];
			
			// Nebendiagonale
			else if (row < this.n && this.sigmaPointSet[row] != null) {
				int startColumn = this.columnIndex[row];
				int columnLength = this.sigmaPointSet[row].length;
				if (column >= startColumn && column < startColumn + columnLength)
					return this.scale*this.sigmaPointSet[row][column - startColumn];
			}
			else if (row - this.n >= 0 && row < 2*this.n && this.sigmaPointSet[row - this.n] != null) {
				int startColumn = this.columnIndex[row - this.n];
				int columnLength = this.sigmaPointSet[row - this.n].length;
				if (column >= startColumn && column < startColumn + columnLength)
					return -this.scale*this.sigmaPointSet[row - this.n][column - startColumn];
			}
			
			// Ansonsten
			return 0.0;
		}

		public String toString() {
			String str = new String();
			for (int i=0; i<2*this.n+1; i++) {
				for (int j=0; j<this.n; j++) {
					str += String.format("%+3.8f   ", this.getValue(i, j));
				}
				str += "\r\n";
			}
			return str;
		}
		
	}

	public LasertrackerUncertaintyModelUT() {
		super();
	}
	
	public LasertrackerUncertaintyModelUT(double[] diagCll) {
		super(diagCll);
	}
	
	public LasertrackerUncertaintyModelUT(double[] diagCll, CorrelationFunction distCovFunc, CorrelationFunction azimuthCovFunc, CorrelationFunction zenithCovFunc) {
		super(diagCll, distCovFunc, azimuthCovFunc, zenithCovFunc);
	}
	
	/**
	 * Bestimmt die Kovarianzmatrix der polaren Beobachtungen nach dem Verfahren Unscented Transformation.
	 * Die Erwartungswerte der umgeformten kartesischen Koordinaten werden korrigiert und berücksichtigen
	 * Terme hoeherer Ordnung.
	 * 
	 * @return Cxx
	 */
	@Override
	public Matrix deriveCovarianceMatrix(PolarPointGroup pointGroup) {
		boolean hasTimeReferencedData = pointGroup.hasTimeReferencedData();
		int dim = pointGroup.getDimension();
		double diagCll[] = this.getDiagCovar(); 
		int paramLen = diagCll.length;
		int nop = pointGroup.size();
		int cxxSize = dim*nop;
		int numberOfVariableParameters = paramLen-this.numberOfFixedParameters();
		int numberOfTimeDependentParameters = hasTimeReferencedData?this.numberOfTimeDependentParameters():0;
		hasTimeReferencedData = hasTimeReferencedData && numberOfTimeDependentParameters > 0;
		
		// Anzahl der Sigma-Punkte
		int n = (numberOfTimeDependentParameters + numberOfVariableParameters) * (nop - 1)  + this.numberOfFixedParameters();
		int k = 3 - n;
		double sqrtNK = Math.sqrt(n+k);

		SigmaPointSet sigmaPolarUncertaintyPoints = new SigmaPointSet(n, sqrtNK);

		// Feste und zufaellige Anteile
		for (int i=0; i<numberOfVariableParameters * (nop-1) + this.numberOfFixedParameters(); i++) {		
			int column = i;
			if (column >= this.numberOfFixedParameters() && column < numberOfVariableParameters * (nop-1) + this.numberOfFixedParameters())
				column = this.numberOfFixedParameters()+(column-this.numberOfFixedParameters())%(paramLen-this.numberOfFixedParameters()); 
			sigmaPolarUncertaintyPoints.setValue(i, i, Math.sqrt(diagCll[column]));
		}
		
		System.out.println(this.getClass().getSimpleName() + ": " + pointGroup.getId()+", Correlation-Function " + hasTimeReferencedData);
		
		if (hasTimeReferencedData) {	
			int idxTimeDep = numberOfVariableParameters * (nop-1) + this.numberOfFixedParameters();
			if (this.hasDistanceCorrelationFunktion()) {
				double upperCholDist3d[][] = this.upperCholeskyDecomposition(pointGroup, LasertrackerUncertaintyModel.DISTANCE3D);
				for (int l=0, i=idxTimeDep; i<idxTimeDep + upperCholDist3d.length; i++, l++) 
					sigmaPolarUncertaintyPoints.setValue(i, i, upperCholDist3d[l]);
				
				idxTimeDep += upperCholDist3d.length;
			}
			if (this.hasAzimuthCorrelationFunktion()) {
				double upperCholAzimuth[][] = this.upperCholeskyDecomposition(pointGroup, LasertrackerUncertaintyModel.AZIMUTHANGLE);
				for (int l=0, i=idxTimeDep; i<idxTimeDep + upperCholAzimuth.length; i++, l++) 
					sigmaPolarUncertaintyPoints.setValue(i, i, upperCholAzimuth[l]);
				
				idxTimeDep += upperCholAzimuth.length;
			}
			if (this.hasZenithCorrelationFunktion()) {
				double upperCholZenith[][] = this.upperCholeskyDecomposition(pointGroup, LasertrackerUncertaintyModel.ZENITHANGLE);
				for (int l=0, i=idxTimeDep; i<idxTimeDep + upperCholZenith.length; i++, l++) 
					sigmaPolarUncertaintyPoints.setValue(i, i, upperCholZenith[l]);
			}
		}

	    double weights[] = new double[] {
	    		1.0/(2.0*(n+k)),
	    		(double)k/(double)(n+k)
	    };
	    
	    // Skalierung fuer Indexkorrektur
	    int sIdxDist = 0, sIdxAzimuth = 1, sIdxZenith = 2;
	    if (!this.hasDistanceCorrelationFunktion()) {
	    	sIdxDist--;
	    	sIdxAzimuth--;
	    	sIdxZenith--;
	    }
	    if (!this.hasAzimuthCorrelationFunktion()) {
	    	sIdxAzimuth--;
	    	sIdxZenith--;
	    }
	    if (!this.hasZenithCorrelationFunktion()) {
	    	sIdxZenith--;
	    }
	    
	    
	    double sigmaCartesianPoints[][] = new double[2*n+1][nop*dim];
	    double expectedValues[] = new double[nop*dim];
	    for (int i=0; i<2*n+1; i++) {
	    	double weight = weights[i < 2*n ? 0 : 1];
	    	
	    	// Instrument abhaengig
			double x0    = sigmaPolarUncertaintyPoints.getValue(i,0);
			double y0    = sigmaPolarUncertaintyPoints.getValue(i,1);
			double z0    = sigmaPolarUncertaintyPoints.getValue(i,2);
			double mInst = sigmaPolarUncertaintyPoints.getValue(i,3);
			double aInst = sigmaPolarUncertaintyPoints.getValue(i,4);
			
			double aA1 = sigmaPolarUncertaintyPoints.getValue(i,5);
			double aA2 = sigmaPolarUncertaintyPoints.getValue(i,6);
			double bA1 = sigmaPolarUncertaintyPoints.getValue(i,7);
			double bA2 = sigmaPolarUncertaintyPoints.getValue(i,8);
			double aE0 = sigmaPolarUncertaintyPoints.getValue(i,9);
			double aE1 = sigmaPolarUncertaintyPoints.getValue(i,10);
			double aE2 = sigmaPolarUncertaintyPoints.getValue(i,11);
			double bE1 = sigmaPolarUncertaintyPoints.getValue(i,12);
			double bE2 = sigmaPolarUncertaintyPoints.getValue(i,13);
			
			double alpha = sigmaPolarUncertaintyPoints.getValue(i,14);
			double gamma = sigmaPolarUncertaintyPoints.getValue(i,15);
			
			double ex  = sigmaPolarUncertaintyPoints.getValue(i,16);
			double by0 = sigmaPolarUncertaintyPoints.getValue(i,17);
			double bz0 = sigmaPolarUncertaintyPoints.getValue(i,18);
			
			// Punkt abhaengig
			for (int j=0; j<nop; j++) {
				Point point = pointGroup.get(j);
				if (j > 0 && point instanceof PolarPoint3D) {
					PolarPoint3D polarPoint = (PolarPoint3D)point;
					int column = this.numberOfFixedParameters() + (j-1) * numberOfVariableParameters;
					
					double dist3D  = polarPoint.getDistance();
					double azimuth = polarPoint.getAzimuth();
					double zenith  = polarPoint.getZenith();
					
					double mRef = sigmaPolarUncertaintyPoints.getValue(i,column + 0);
					double aRef = sigmaPolarUncertaintyPoints.getValue(i,column + 1);
					
					double azRef = sigmaPolarUncertaintyPoints.getValue(i,column + 2);
					double vRef  = sigmaPolarUncertaintyPoints.getValue(i,column + 3);
					
					double anzAz = sigmaPolarUncertaintyPoints.getValue(i,column + 4);
					double anzV  = sigmaPolarUncertaintyPoints.getValue(i,column + 5);
					
					
					// Zeitabhaengig
					double dist3DTimeRef = 0, azimutTimeRef = 0, zenithTimeRef = 0;
					if (hasTimeReferencedData) {
						column = this.numberOfFixedParameters() + (nop - 1) * numberOfVariableParameters;
						// j - 1, da der erste Punkt der Standpunkt ist und kein Polarpunkt == Indexkorrektur
						if (this.hasDistanceCorrelationFunktion() && sIdxDist >= 0)
							dist3DTimeRef = sigmaPolarUncertaintyPoints.getValue(i,column + j - 1 + sIdxDist    * (nop-1));
						if (this.hasAzimuthCorrelationFunktion() && sIdxAzimuth >= 0)
							azimutTimeRef = sigmaPolarUncertaintyPoints.getValue(i,column + j - 1 + sIdxAzimuth * (nop-1));
						if (this.hasZenithCorrelationFunktion() && sIdxZenith >= 0)
							zenithTimeRef = sigmaPolarUncertaintyPoints.getValue(i,column + j - 1 + sIdxZenith  * (nop-1));
					}
									
//					double x = x0 + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*ex + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(-ex*Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0)) - Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(by0+alpha*bz0 - alpha*(-ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0))) + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef) * (Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma) - Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(gamma - alpha*(Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma)));
//					double y = y0 + Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*ex + Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(-ex*Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0)) + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(by0+alpha*bz0 - alpha*(-ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0))) + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef) * (Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma) + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(gamma - alpha*(Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma)));
//					double z = z0 + alpha*(by0+alpha*bz0) - ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0)                                                                                    + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef) * (alpha*gamma + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma);
					
					double x = x0 + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*ex + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(-ex*Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0)) - Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(by0+alpha*bz0 - alpha*(-ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0))) + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef+dist3DTimeRef) * (Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma) - Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(gamma - alpha*(Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma)));
					double y = y0 + Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*ex + Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(-ex*Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0)) + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(by0+alpha*bz0 - alpha*(-ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0))) + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef+dist3DTimeRef) * (Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma) + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(gamma - alpha*(Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma)));
					double z = z0 + alpha*(by0+alpha*bz0) - ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0)                                                                                    + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef+dist3DTimeRef) * (alpha*gamma + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma);

					sigmaCartesianPoints[i][j*dim]   = x;
					sigmaCartesianPoints[i][j*dim+1] = y;
					sigmaCartesianPoints[i][j*dim+2] = z;
		
					expectedValues[j*dim]   += weight*x;
					expectedValues[j*dim+1] += weight*y;
					expectedValues[j*dim+2] += weight*z;
				}
				// j == 0 --> Standpunkt
				else {
					sigmaCartesianPoints[i][j*dim]   = x0;
					sigmaCartesianPoints[i][j*dim+1] = y0;
					sigmaCartesianPoints[i][j*dim+2] = z0;
					
					expectedValues[j*dim]   += weight*x0;
					expectedValues[j*dim+1] += weight*y0;
					expectedValues[j*dim+2] += weight*z0;
				}
			}
	    }
	    
	    // Bestimme Verbesserungen
	    for (int i=0; i<2*n+1; i++) {
	    	for (int j=0; j<nop; j++) {
	    		for (int d=0; d<dim; d++) {
	    			sigmaCartesianPoints[i][j*dim+d] -= expectedValues[j*dim+d];
	    		}
	    	}
	    }  
	    
	    // Bestimme die CoVar-Matrix
	    Matrix Cxx = new UpperSymmPackMatrix(cxxSize);
	    for (int i=0; i<cxxSize; i++) {
	    	for (int j=i; j<cxxSize; j++) {
	    		double cxx = 0;
	    		// Bestimme v{i}TPv{j}
	    		for (int s=0; s<2*n+1; s++) {
	    			double weight = weights[s < 2*n ? 0 : 1];
	    			cxx += sigmaCartesianPoints[s][i] * weight * sigmaCartesianPoints[s][j];
	    		}
	    		Cxx.set(i,j,cxx);
	    	}
	    }
	    
	    // Korrigiere Erwartungswert
	    for (int j=0; j<nop; j++) {
			Point point = pointGroup.get(j);
			if (point instanceof Point3D) {
				((Point3D)point).applyBiasCorrectedValues(
						expectedValues[j*dim], expectedValues[j*dim+1], expectedValues[j*dim+2]
				);
			}
	    }
		return Cxx;
	}
	
//	public static void main(String args[]) throws Exception {
//		LasertrackerUncertaintyModelUT ut = new LasertrackerUncertaintyModelUT();
//		double diagCoVar[] = new double[ut.getDefaultSigmas().length];
//		for (int i=0; i<ut.getDefaultSigmas().length; i++) 
//			diagCoVar[i] = ut.getDefaultSigmas()[i]*ut.getDefaultSigmas()[i];
//		
//		CorrelationFunction distCovFunc    = null;
//		CorrelationFunction azimuthCovFunc = null;
//		CorrelationFunction zenithCovFunc  = null;
//
//		distCovFunc    = new ExponentialCosineCorrelationFunction(0.002*0.002,                                                 -1, 0.455596718347454, 0.000123942004019267, 0.000264721734175281);
//		azimuthCovFunc = new ExponentialCosineCorrelationFunction(0.00015*Constant.RHO_GRAD2RAD*0.00015*Constant.RHO_GRAD2RAD, -1, 0.348845275850707, 0.000119519711131458, 0.000382066728933325);
//		zenithCovFunc  = new ExponentialCosineCorrelationFunction(0.00015*Constant.RHO_GRAD2RAD*0.00015*Constant.RHO_GRAD2RAD, -1, 0.673632337215673, 0.000358598447079722, 0.000800223384590601);
//		
//		LasertrackerUncertaintyModelUT ut2 = new LasertrackerUncertaintyModelUT(diagCoVar, distCovFunc, azimuthCovFunc, zenithCovFunc);
//		PolarPointGroup pointGroup = new PolarPointGroup(1, "1", ut2);
//		pointGroup.add(new PolarPoint3D("2", 100,  15.*Constant.RHO_GRAD2RAD,  89.*Constant.RHO_GRAD2RAD));
//		pointGroup.add(new PolarPoint3D("3", 200,  45.*Constant.RHO_GRAD2RAD,  70.*Constant.RHO_GRAD2RAD));
////		pointGroup.add(new PolarPoint3D("4", 150,  75.*Constant.RHO_GRAD2RAD, 125.*Constant.RHO_GRAD2RAD));
////		pointGroup.add(new PolarPoint3D("5",  50, 145.*Constant.RHO_GRAD2RAD, 105.*Constant.RHO_GRAD2RAD));
//		
//		
//		SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );
//		for (int i=0; i<pointGroup.size(); i++) {
//			pointGroup.get(i).setRowInJacobiMatrix(i);
//			if (i > 0)
//				((PolarPoint3D)pointGroup.get(i)).setObservationDate(df.parse( "2014-10-18 19:0"+i+":06.7" ));
//		}
//		pointGroup.deriveCovarianceMatrix();
//	}
}
