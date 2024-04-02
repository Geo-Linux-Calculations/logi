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
import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.PolarPoint3D;
import com.derletztekick.tools.geodesy.distribution.Distribution;

public class LasertrackerUncertaintyModelMCS extends LasertrackerUncertaintyModel {
	private int distTypes[];
	private int samples = 5000;
	public LasertrackerUncertaintyModelMCS() {
		this(5000);
	}
	
	public LasertrackerUncertaintyModelMCS(int samples) {
		super();
		this.samples = samples;
		this.distTypes = this.getDefaultDistributionTypes();
	}
	
	public LasertrackerUncertaintyModelMCS(double[] diagCll, int[] distTypes) {
		this(5000, diagCll, distTypes);
	}
	
	public LasertrackerUncertaintyModelMCS(int samples, double[] diagCll, int[] distTypes) {
		this(samples, diagCll, distTypes, null, null, null);
	}
	
	public LasertrackerUncertaintyModelMCS(double[] diagCll, int[] distTypes, CorrelationFunction distCovFunc, CorrelationFunction azimuthCovFunc, CorrelationFunction zenithCovFunc) {
		this(5000, diagCll, distTypes, distCovFunc, azimuthCovFunc, zenithCovFunc);
	}
	
	public LasertrackerUncertaintyModelMCS(int samples, double[] diagCll, int[] distTypes, CorrelationFunction distCovFunc, CorrelationFunction azimuthCovFunc, CorrelationFunction zenithCovFunc) {
		super(diagCll, distCovFunc, azimuthCovFunc, zenithCovFunc);
		this.samples = samples;
		this.distTypes = distTypes;
	}
	
	private int[] getDefaultDistributionTypes() {
		int n = this.getDiagCovar().length;
		int distTypes[] = new int[n];
		for (int i=0; i<n; i++)
			distTypes[i] = PreAnalysis.DISTRIBUTION_NORMAL;
		return distTypes;
	}
	
	/**
	 * Bestimmt die Kovarianzmatrix der polaren Beobachtungen mittels Monte-Carlo-Methode.
	 * Die Simulation wird <code>samples</code>-mal durchgefueht und beruecksichtigt die
	 * gewaehlte Wahrscheinlichkeitsverteilung. Die Erwartungswerte der umgeformten kartesischen 
	 * Koordinaten werden korrigiert und berücksichtigen Terme hoeherer Ordnung.
	 * 
	 * @return Cxx
	 */
	public Matrix deriveCovarianceMatrix(PolarPointGroup pointGroup) {
		int dim = pointGroup.getDimension();
		double diagCll[] = this.getDiagCovar(); 
		int paramLen = diagCll.length;
		int nop = pointGroup.size();
		int cxxSize = dim*nop;
		
		boolean hasTimeReferencedData = pointGroup.hasTimeReferencedData();
		int numberOfTimeDependentParameters = hasTimeReferencedData?this.numberOfTimeDependentParameters():0;
		hasTimeReferencedData = hasTimeReferencedData && numberOfTimeDependentParameters > 0;
		// Bestimme Zeitabhaengige Faktoren der CoVars fuer polare Elemente
		double[][] upperCholDist3d = null, upperCholAzimuth = null, upperCholZenith = null;
		
		System.out.println(this.getClass().getSimpleName() + ": " + pointGroup.getId()+", Correlation-Function " + hasTimeReferencedData);
		
		if (hasTimeReferencedData) {
			if (this.hasDistanceCorrelationFunktion())
				upperCholDist3d  = this.upperCholeskyDecomposition(pointGroup, LasertrackerUncertaintyModel.DISTANCE3D);
			if (this.hasAzimuthCorrelationFunktion())
				upperCholAzimuth = this.upperCholeskyDecomposition(pointGroup, LasertrackerUncertaintyModel.AZIMUTHANGLE);
			if (this.hasZenithCorrelationFunktion())
				upperCholZenith  = this.upperCholeskyDecomposition(pointGroup, LasertrackerUncertaintyModel.ZENITHANGLE);
		}

		double randomPoints[][] = new double[this.samples][cxxSize];
		double expectedValues[] = new double[cxxSize];
		for (int i=0; i<this.samples; i++) {
			// system. Einfluesse
			double instrumentDependentParameters[] = new double[this.numberOfFixedParameters()];
			for (int j=0; j<this.numberOfFixedParameters(); j++)
				instrumentDependentParameters[j] = this.random(j);

			// zeit. Einfluesse
			double timeDependentDistance[] = null, timeDependentZenith[] = null, timeDependentAzimuth[] = null;
			if (hasTimeReferencedData) {
				double randomDistance[] = null, randomZenith[] = null, randomAzimuth[] = null;
				if (this.hasDistanceCorrelationFunktion()) {
					timeDependentDistance = new double[nop];
					randomDistance        = new double[nop-1];
				}
				if (this.hasAzimuthCorrelationFunktion()) {
					timeDependentAzimuth  = new double[nop];
					randomAzimuth         = new double[nop-1];
				}
				if (this.hasZenithCorrelationFunktion()) {
					timeDependentZenith   = new double[nop];
					randomZenith          = new double[nop-1];
				}
				// Erzeuge Standardnormalverteilte Zufallszahlen
				for (int j=0; j<nop-1; j++) {
					if (this.hasDistanceCorrelationFunktion()) 
						randomDistance[j] = Distribution.randNormal(0.0, 1.0);
					if (this.hasAzimuthCorrelationFunktion())
						randomAzimuth[j]  = Distribution.randNormal(0.0, 1.0);
					if (this.hasZenithCorrelationFunktion())
						randomZenith[j]   = Distribution.randNormal(0.0, 1.0);
				}			
				
				for (int j=0; j<nop-1; j++) {
					for (int k=0; k<=j; k++) {
						if (this.hasDistanceCorrelationFunktion()) 
							timeDependentDistance[j+1] += upperCholDist3d[k][j-k]  * randomDistance[k];
						if (this.hasAzimuthCorrelationFunktion())
							timeDependentAzimuth[j+1]  += upperCholAzimuth[k][j-k] * randomAzimuth[k];
						if (this.hasZenithCorrelationFunktion())
							timeDependentZenith[j+1]   += upperCholZenith[k][j-k]  * randomZenith[k];
					}
				}
				randomDistance = randomAzimuth = randomZenith = null;
			}
			// Jeden Punkt
			for (int k=0; k<nop; k++) {
				Point point = pointGroup.get(k);
				// punktbezogene Einfluesse
				double pointDependentParameters[] = new double[paramLen-this.numberOfFixedParameters()];
				for (int j=this.numberOfFixedParameters(), l=0; j<paramLen; j++, l++) {
					pointDependentParameters[l] = this.random(j);
				}
				
				// zeitliche Einfluesse
				double timeDependentParameters[] = new double[3];
				if (hasTimeReferencedData) {
					if (this.hasDistanceCorrelationFunktion()) 
						timeDependentParameters[0] = timeDependentDistance[k];
					if (this.hasAzimuthCorrelationFunktion())
						timeDependentParameters[1] = timeDependentAzimuth[k];
					if (this.hasZenithCorrelationFunktion())
						timeDependentParameters[2] = timeDependentZenith[k];
				}
				
				// Bestimme verrauschte Punkte
				double randErrorPointXYZ[] = this.getRandomErrorPointXYZ(point, instrumentDependentParameters, pointDependentParameters, timeDependentParameters);
				
				// Speichere Punkte der Simulation und bestimme Mittelwert
				for (int j=0; j<dim; j++) {
					randomPoints[i][k*dim+j] = randErrorPointXYZ[j];
					expectedValues[k*dim+j] += 1.0/this.samples*randErrorPointXYZ[j];
				}
			}
		}
		
		// Bestimme die CoVar-Matrix
		Matrix Cxx = new UpperSymmPackMatrix(cxxSize);
		for (int i=0; i<cxxSize; i++) {
			for (int j=i; j<cxxSize; j++) {
				double cxx = 0;
				// Bestimme v{i}Tv{j}
				for (int s=0; s<this.samples; s++) {
					cxx += (randomPoints[s][i] - expectedValues[i]) * (randomPoints[s][j] - expectedValues[j]);
				}
				cxx /= this.samples;
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
	
	private double[] getRandomErrorPointXYZ(Point point, double instrumentDependentParameters[], double pointDependentParameters[], double timeDependentParameters[]) {
		if (point instanceof PolarPoint3D) {
			PolarPoint3D polarPoint = (PolarPoint3D)point;
			double dist3D  = polarPoint.getDistance();
			double azimuth = polarPoint.getAzimuth();
			double zenith  = polarPoint.getZenith();
			
			double x0 = instrumentDependentParameters[0];
			double y0 = instrumentDependentParameters[1];
			double z0 = instrumentDependentParameters[2];
			double mInst = instrumentDependentParameters[3];
			double aInst = instrumentDependentParameters[4];
			
			double aA1 = instrumentDependentParameters[5];
			double aA2 = instrumentDependentParameters[6];
			double bA1 = instrumentDependentParameters[7];
			double bA2 = instrumentDependentParameters[8];
			double aE0 = instrumentDependentParameters[9];
			double aE1 = instrumentDependentParameters[10];
			double aE2 = instrumentDependentParameters[11];
			double bE1 = instrumentDependentParameters[12];
			double bE2 = instrumentDependentParameters[13];
			
			double alpha = instrumentDependentParameters[14];
			double gamma = instrumentDependentParameters[15];
			
			double ex  = instrumentDependentParameters[16];
			double by0 = instrumentDependentParameters[17];
			double bz0 = instrumentDependentParameters[18];
			
			double mRef = pointDependentParameters[0];
			double aRef = pointDependentParameters[1];
			
			double azRef = pointDependentParameters[2];
			double vRef  = pointDependentParameters[3];
			
			double anzAz = pointDependentParameters[4];
			double anzV  = pointDependentParameters[5];
			
			double dist3DTimeRef = timeDependentParameters[0];
			double azimutTimeRef = timeDependentParameters[1];
			double zenithTimeRef = timeDependentParameters[2];
			
//			double x = x0 + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*ex + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(-ex*Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0)) - Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(by0+alpha*bz0 - alpha*(-ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0))) + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef) * (Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma) - Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(gamma - alpha*(Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma)));
//			double y = y0 + Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*ex + Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(-ex*Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0)) + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(by0+alpha*bz0 - alpha*(-ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0))) + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef) * (Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma) + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + anzAz/dist3D)*(gamma - alpha*(Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma)));
//			double z = z0 + alpha*(by0+alpha*bz0) - ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*(bz0-alpha*by0)                                                                                    + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef) * (alpha*gamma + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + anzV/dist3D)*alpha*gamma);
			
			double x = x0 + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*ex + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(-ex*Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0)) - Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(by0+alpha*bz0 - alpha*(-ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0))) + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef+dist3DTimeRef) * (Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma) - Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(gamma - alpha*(Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma)));
			double y = y0 + Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*ex + Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(-ex*Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0)) + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(by0+alpha*bz0 - alpha*(-ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0))) + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef+dist3DTimeRef) * (Math.sin((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma) + Math.cos((azimuth+aA1*Math.cos(azimuth) + bA1*Math.sin(azimuth) + aA2*Math.cos(2*azimuth) + bA2*Math.sin(2*azimuth)) + azRef + azimutTimeRef + anzAz/dist3D)*(gamma - alpha*(Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma)));
			double z = z0 + alpha*(by0+alpha*bz0) - ex*Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) + Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*(bz0-alpha*by0)                                                                                    + ((1.0+mRef)*(1.0+mInst)*dist3D+aInst+aRef+dist3DTimeRef) * (alpha*gamma + Math.cos((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D) - Math.sin((zenith + aE0 + aE1*Math.cos(zenith) + bE1*Math.sin(zenith) + aE2*Math.cos(2*zenith) + bE2*Math.sin(2*zenith)) + vRef + zenithTimeRef + anzV/dist3D)*alpha*gamma);

			return new double [] {x,y,z};
		}
		else {
			double x = instrumentDependentParameters[0];
			double y = instrumentDependentParameters[1];
			double z = instrumentDependentParameters[2];

			return new double [] {x,y,z};
		}
	}
		
	private double random(int i) {
		double sigma = Math.sqrt(this.getDiagCovar()[i]);
		double rand  = 0.0;
		switch (this.distTypes[i]) {
			case PreAnalysis.DISTRIBUTION_TRIANGULAR:
				rand = Distribution.randTriangular(0.0, sigma);
			break;
			case PreAnalysis.DISTRIBUTION_UNIFORM:
				rand = Distribution.randUniform(0.0, sigma);
			break;
			default:
				rand = Distribution.randNormal(0.0, sigma);
			break;
		}
		return rand;
	}
	
//	public static void main(String args[]) throws Exception {
//		LasertrackerUncertaintyModelMCS ut = new LasertrackerUncertaintyModelMCS();
//		
//		CorrelationFunction distCovFunc    = null;
//		CorrelationFunction azimuthCovFunc = null;
//		CorrelationFunction zenithCovFunc  = null;
//
//		distCovFunc    = new ExponentialCosineCorrelationFunction(0.002*0.002,                                                 -1, 0.455596718347454, 0.000123942004019267, 0.000264721734175281);
//		azimuthCovFunc = new ExponentialCosineCorrelationFunction(0.00015*Constant.RHO_GRAD2RAD*0.00015*Constant.RHO_GRAD2RAD, -1, 0.348845275850707, 0.000119519711131458, 0.000382066728933325);
//		zenithCovFunc  = new ExponentialCosineCorrelationFunction(0.00015*Constant.RHO_GRAD2RAD*0.00015*Constant.RHO_GRAD2RAD, -1, 0.673632337215673, 0.000358598447079722, 0.000800223384590601);
//		
//		int distTypes[] = ut.getDefaultDistributionTypes();
//		double diagCoVar[] = new double[ut.getDefaultSigmas().length];
//		for (int i=0; i<ut.getDefaultSigmas().length; i++) 
//			diagCoVar[i] = ut.getDefaultSigmas()[i]*ut.getDefaultSigmas()[i];
//		
//		LasertrackerUncertaintyModelMCS ut2 = new LasertrackerUncertaintyModelMCS(10000, diagCoVar, distTypes, distCovFunc, azimuthCovFunc, zenithCovFunc);
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
