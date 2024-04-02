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
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.PolarPoint3D;
import com.derletztekick.tools.geodesy.Constant;

public class LasertrackerUncertaintyModel extends UncertaintyModel {
	private CorrelationFunction distCovFunc, azimuthCovFunc, zenithCovFunc;
		
	public static final int DISTANCE3D   = 1;
	public static final int AZIMUTHANGLE = 2;
	public static final int ZENITHANGLE  = 3;
	
	public LasertrackerUncertaintyModel() {}
	
	public LasertrackerUncertaintyModel(double[] diagCll) {
		super(diagCll);
	}
	
	public LasertrackerUncertaintyModel(double[] diagCll, CorrelationFunction distCovFunc, CorrelationFunction azimuthCovFunc, CorrelationFunction zenithCovFunc) {
		super(diagCll);
		this.distCovFunc    = distCovFunc;
		this.azimuthCovFunc = azimuthCovFunc;
		this.zenithCovFunc  = zenithCovFunc;
	}
	
	private double getTimeDependentJacobiElement(Point point, int equationIndex, int obsType) {
		double a = 0.0;
		
		if (point instanceof PolarPoint3D) {
			PolarPoint3D polarPoint = (PolarPoint3D)point;
			double s   = polarPoint.getDistance();
			double az  = polarPoint.getAzimuth();
			double v   = polarPoint.getZenith();
			
			if (equationIndex == 0) {
				switch(obsType) {
					case DISTANCE3D:
						a =    Math.cos(az)*Math.sin(v);
					break;
					case AZIMUTHANGLE:
						a = -s*Math.sin(az)*Math.sin(v);
					break;
					case ZENITHANGLE:
						a =  s*Math.cos(az)*Math.cos(v);
					break;
				}
			}
				
			
			else if (equationIndex == 1) {
				switch(obsType) {
					case DISTANCE3D:
						a =   Math.sin(az)*Math.sin(v);
					break;
					case AZIMUTHANGLE:
						a = s*Math.cos(az)*Math.sin(v);
					break;
					case ZENITHANGLE:
						a = s*Math.sin(az)*Math.cos(v);
					break;
				}
			}
			else if (equationIndex == 2) {
				switch(obsType) {
					case DISTANCE3D:
						a = Math.cos(v);
					break;
					case AZIMUTHANGLE:
						a = 0.0;
					break;
					case ZENITHANGLE:
						a = -s*Math.sin(v);
					break;
				}
			}
		}
		return a;
	}
		
	@Override
	public Matrix deriveCovarianceMatrix(PolarPointGroup pointGroup) {
		int nop = pointGroup.size();
		int dim = pointGroup.getDimension();
		int n = dim*nop;
		Matrix Cxx = new UpperSymmPackMatrix(n);
		boolean hasTimeReferencedData = pointGroup.hasTimeReferencedData();
		int numberOfTimeDependentParameters = hasTimeReferencedData?this.numberOfTimeDependentParameters():0;
		hasTimeReferencedData = hasTimeReferencedData && numberOfTimeDependentParameters > 0;
		
		System.out.println(this.getClass().getSimpleName() + ": " + pointGroup.getId()+", Correlation-Function " + hasTimeReferencedData);
		
		if (hasTimeReferencedData) {
			for (int i=1, rowN=dim; i<nop; i++, rowN+=dim) {
				double cllAtDist[] = new double[nop-1];
				double cllAtAzim[] = new double[nop-1];
				double cllAtZeni[] = new double[nop-1];
				Point pointAT = pointGroup.get(i);
				for (int equationIndexAT=0; equationIndexAT<dim; equationIndexAT++) {
					double atDist = getTimeDependentJacobiElement(pointAT, equationIndexAT, DISTANCE3D);
					double atAzim = getTimeDependentJacobiElement(pointAT, equationIndexAT, AZIMUTHANGLE);
					double atZeni = getTimeDependentJacobiElement(pointAT, equationIndexAT, ZENITHANGLE);
					for (int j=1; j<nop; j++) {
						Point pointCll = pointGroup.get(j);
						double dT = 0.001*( ((PolarPoint3D)pointCll).getObservationDate().getTime() - ((PolarPoint3D)pointAT).getObservationDate().getTime() );
						double cllDist = this.hasDistanceCorrelationFunktion() ? this.distCovFunc.getCovariance(dT)    : 0;
						double cllAzim = this.hasAzimuthCorrelationFunktion()  ? this.azimuthCovFunc.getCovariance(dT) : 0;
						double cllZeni = this.hasZenithCorrelationFunktion()   ? this.zenithCovFunc.getCovariance(dT)  : 0;
						cllAtDist[j-1] = cllDist*atDist;
						cllAtAzim[j-1] = cllAzim*atAzim;
						cllAtZeni[j-1] = cllZeni*atZeni;
					}
					
					// Spalte cll*at liegt vor
					for (int j=i, colN=rowN; j<nop; j++, colN+=dim) {
						Point pointA = pointGroup.get(j);
						for (int equationIndexA=0; equationIndexA<dim; equationIndexA++) {			
							double aDist = getTimeDependentJacobiElement(pointA, equationIndexA, DISTANCE3D);
							double aAzim = getTimeDependentJacobiElement(pointA, equationIndexA, AZIMUTHANGLE);
							double aZeni = getTimeDependentJacobiElement(pointA, equationIndexA, ZENITHANGLE);
		
							Cxx.set(rowN+equationIndexAT, colN+equationIndexA, aDist*cllAtDist[j-1] + aAzim*cllAtAzim[j-1] + aZeni*cllAtZeni[j-1] );
						}
					}
				}
			}
		}
		Cxx = Cxx.add(super.deriveCovarianceMatrix(pointGroup));
		return Cxx;
	}
	
	@Override
	protected final double getJacobiElement(Point point, int column, int equationIndex, int pointNumber) {
		double a = 0.0;
		int paramLen = this.getDiagCovar().length;
		int numberOfVariableParameters = paramLen-this.numberOfFixedParameters();

		if (point instanceof PolarPoint3D) {
			PolarPoint3D polarPoint = (PolarPoint3D)point;
			double s   = polarPoint.getDistance();
			double az  = polarPoint.getAzimuth();
			double v   = polarPoint.getZenith();

			if (column-this.numberOfFixedParameters() >= 0) {
				if (column >= this.numberOfFixedParameters() + pointNumber * numberOfVariableParameters && column < this.numberOfFixedParameters() + (pointNumber+1) * numberOfVariableParameters)
					column = this.numberOfFixedParameters()+(column-this.numberOfFixedParameters())%(paramLen-this.numberOfFixedParameters());
				else
					return a;
			}
			
			if (equationIndex == 0) {
				switch (column) {
					case 0:	
						a = 1.0;
					break;
					case 1:
						a = 0;
					break;
					case 2:
						a = 0;
					break;
					case 3:
						a = s*Math.cos(az)*Math.sin(v);
					break;
					case 4:
						a = Math.cos(az)*Math.sin(v);
					break;
					case 5:
						a = -s*Math.sin(az)*Math.cos(az)*Math.sin(v);
					break;
					case 6:
						a = -s*Math.sin(az)*Math.cos(2.0*az)*Math.sin(v);
					break;
					case 7:
						a = -s*Math.sin(az)*Math.sin(az)*Math.sin(v);
					break;
					case 8:
						a = -s*Math.sin(az)*Math.sin(2.0*az)*Math.sin(v);
					break;
					case 9:
						a = s*Math.cos(az)*Math.cos(v);
					break;
					case 10:
						a = s*Math.cos(az)*Math.cos(v)*Math.cos(v);
					break;
					case 11:
						a = s*Math.cos(az)*Math.cos(v)*Math.cos(2.0*v);
					break;
					case 12:
						a = s*Math.cos(az)*Math.cos(v)*Math.sin(v);
					break;
					case 13:
						a = s*Math.cos(az)*Math.cos(v)*Math.sin(2.0*v);
					break;
					case 14:
						a = s*Math.sin(az)*Math.cos(v);
					break;
					case 15:
						a = -s*Math.sin(az);
					break;
					case 16:
						a = -Math.cos(az)*(-1.0+Math.sin(v));
					break;
					case 17:
						a = -Math.sin(az);
					break;
					case 18:
						a = -Math.cos(az)*Math.cos(v);
					break;
					case 19:
						a = s*Math.sin(v)*Math.cos(az);
					break;
					case 20:
						a = Math.sin(v)*Math.cos(az);
					break;
					case 21:
						a = -s*Math.sin(az)*Math.sin(v);
					break;
					case 22:
						a = s*Math.cos(az)*Math.cos(v);
					break;
					case 23:
						a = -Math.sin(az)*Math.sin(v);
					break;
					case 24:
						a = Math.cos(az)*Math.cos(v);
					break;
				}
			} else if (equationIndex == 1) {
				switch (column) {
					case 0:
						a = 0;
					break;
					case 1:
						a = 1.0;
					break;
					case 2:
						a = 0;
					break;
					case 3:
						a = s*Math.sin(az)*Math.sin(v);
					break;
					case 4:
						a = Math.sin(az)*Math.sin(v);
					break;
					case 5:
						a = s*Math.cos(az)*Math.cos(az)*Math.sin(v);
					break;
					case 6:
						a = s*Math.cos(az)*Math.cos(2.0*az)*Math.sin(v);
					break;
					case 7:
						a = s*Math.cos(az)*Math.sin(az)*Math.sin(v);
					break;
					case 8:
						a = s*Math.cos(az)*Math.sin(2.0*az)*Math.sin(v);
					break;
					case 9:
						a = s*Math.sin(az)*Math.cos(v);
					break;
					case 10:
						a = s*Math.sin(az)*Math.cos(v)*Math.cos(v);
					break;
					case 11:
						a = s*Math.sin(az)*Math.cos(v)*Math.cos(2.0*v);
					break;
					case 12:
						a = s*Math.sin(az)*Math.cos(v)*Math.sin(v);
					break;
					case 13:
						a = s*Math.sin(az)*Math.cos(v)*Math.sin(2.0*v);
					break;
					case 14:
						a = -s*Math.cos(az)*Math.cos(v);
					break;
					case 15:
						a = s*Math.cos(az);
					break;
					case 16:
						a = -Math.sin(az)*(-1.0+Math.sin(v));
					break;
					case 17:
						a = Math.cos(az);
					break;
					case 18:
						a = -Math.sin(az)*Math.cos(v);
					break;
					case 19:
						a = s*Math.sin(az)*Math.sin(v);
					break;
					case 20:
						a = Math.sin(az)*Math.sin(v);
					break;
					case 21:
						a = s*Math.cos(az)*Math.sin(v);
					break;
					case 22:
						a = s*Math.sin(az)*Math.cos(v);
					break;
					case 23:
						a = Math.cos(az)*Math.sin(v);
					break;
					case 24:
						a = Math.sin(az)*Math.cos(v);
					break;
				}
			} else if (equationIndex == 2) {
				switch (column) {
					case 0:
						a = 0;
					break;
					case 1:
						a = 0;
					break;
					case 2:
						a = 1.0;
					break;
					case 3:
						a = s*Math.cos(v);
					break;
					case 4:
						a = Math.cos(v);
					break;
					case 5:
						a = 0;
					break;
					case 6:
						a = 0;
					break;
					case 7:
						a = 0;
					break;
					case 8:
						a = 0;
					break;
					case 9:
						a = -s*Math.sin(v);
					break;
					case 10:
						a = -0.5*s*Math.sin(2.0*v);
					break;
					case 11:
						a = -s*Math.sin(v)*Math.cos(2.0*v);
					break;
					case 12:
						a = -s*Math.sin(v)*Math.sin(v);
					break;
					case 13:
						a = -s*Math.sin(v)*Math.sin(2.0*v);
					break;
					case 14:
						a = 0;
					break;
					case 15:
						a = 0;
					break;
					case 16:
						a = -Math.cos(v);
					break;
					case 17:
						a = 0;
					break;
					case 18:
						a = Math.sin(v);
					break;
					case 19:
						a = s*Math.cos(v);
					break;
					case 20:
						a = Math.cos(v);
					break;
					case 21:
						a = 0;
					break;
					case 22:
						a = -s*Math.sin(v);
					break;
					case 23:
						a = 0;
					break;
					case 24:
						a = -Math.sin(v);
					break;
				}
			}
		}
		else {
			if (equationIndex == 0 && column == 0) 
				a = 1;
			else if (equationIndex == 1 && column == 1)
				a = 1;
			else if (equationIndex == 2 && column == 2)
				a = 1;
		}
		return a;
	}

	@Override
	public int numberOfFixedParameters() {
		return 19;
	}
	
	@Override
	public double[] getDefaultSigmas() {
		double sigma[] = new double[] { 
				0.00010,   // x0
				0.00010,   // y0
				0.00010,   // z0
				0.0000005, // mInst
				0.0000050, // aInst
				0.000075*Constant.RHO_GRAD2RAD, // aA1
				0.000075*Constant.RHO_GRAD2RAD, // aA2
				0.000075*Constant.RHO_GRAD2RAD, // bA1
				0.000075*Constant.RHO_GRAD2RAD, // bA2
				0.000075*Constant.RHO_GRAD2RAD, // aE0
				0.000075*Constant.RHO_GRAD2RAD, // aE1
				0.000075*Constant.RHO_GRAD2RAD, // aE2
				0.000075*Constant.RHO_GRAD2RAD, // bE1
				0.000075*Constant.RHO_GRAD2RAD, // bE2
				0.000075*Constant.RHO_GRAD2RAD, // alpha Kippachsfehler
				0.000075*Constant.RHO_GRAD2RAD, // gamma Zielachsfehler
				0.00000050,    // ex
				0.00000050,    // by0
				0.00000050,    // bz0
				
				0.000001,   // mRef
				0.000025,   // aRef
				0.000150*Constant.RHO_GRAD2RAD, // azRef - Additiver Anteil des Az Fehlers
				0.000150*Constant.RHO_GRAD2RAD, // vRef - Additiver Anteil des V Fehlers
				0.000015,   // anzAz
				0.000015    // anzV
		};

		return sigma;
	}
	
	public int getIndexByAbbreviation(String abbr) {
		if (abbr.equalsIgnoreCase("X0"))
			return 0;
		else if (abbr.equalsIgnoreCase("Y0"))
			return 1;
		else if (abbr.equalsIgnoreCase("Z0"))
			return 2;
		else if (abbr.equalsIgnoreCase("mInst"))
			return 3;
		else if (abbr.equalsIgnoreCase("addInst"))
			return 4;
		else if (abbr.equalsIgnoreCase("aA1"))
			return 5;					
		else if (abbr.equalsIgnoreCase("aA2"))	
			return 6;
		else if (abbr.equalsIgnoreCase("bA1"))
			return 7;
		else if (abbr.equalsIgnoreCase("bA2"))
			return 8;
		else if (abbr.equalsIgnoreCase("aE0"))
			return 9;
		else if (abbr.equalsIgnoreCase("aE1"))
			return 10;
		else if (abbr.equalsIgnoreCase("aE2"))
			return 11;
		else if (abbr.equalsIgnoreCase("bE1"))
			return 12;
		else if (abbr.equalsIgnoreCase("bE2"))
			return 13;
		else if (abbr.equalsIgnoreCase("alpha"))
			return 14;
		else if (abbr.equalsIgnoreCase("gamma"))
			return 15;
		else if (abbr.equalsIgnoreCase("exz"))	
			return 16;
		else if (abbr.equalsIgnoreCase("by"))	
			return 17;
		else if (abbr.equalsIgnoreCase("bz"))	
			return 18;
		else if (abbr.equalsIgnoreCase("mRef"))
			return 19;					
		else if (abbr.equalsIgnoreCase("addRef"))
			return 20;
		else if (abbr.equalsIgnoreCase("az"))	
			return 21;
		else if (abbr.equalsIgnoreCase("v"))	
			return 22;
		else if (abbr.equalsIgnoreCase("azCent"))
			return 23;
		else if (abbr.equalsIgnoreCase("vCent"))
			return 24;
		return -1;
	}

	@Override
	public boolean isAngle(int i) {
		return (i >= 5 && i <= 15) || (i >= 21 && i <= 22);
	}
	
	/**
	 * Liefert die Opere-Dreiecksmatrix, sodass gilt C = R'R --> R = L' --> C = LL'
	 * @param pointGroup
	 * @param a
	 * @param b
	 * @param c
	 * @return R
	 */
	double[][] upperCholeskyDecomposition(PolarPointGroup pointGroup, int obsType) {
		int n = pointGroup.size() - 1;
		//double R[][] = new double[n][n];
		double R[][] = new double[n][];
		for (int j = 0; j < n; j++) {
			R[j] = new double[n-j];
			double d = 0.0;
			for (int k = 0; k < j; k++) {
				//double s = A[k][j];
				// erste Punkt ist der Standpunkt, daher +1, um nur polare Elemente zu erwischen
				// Umrechung in SEC
				double dT = 0.001*( ((PolarPoint3D)pointGroup.get(k+1)).getObservationDate().getTime() - ((PolarPoint3D)pointGroup.get(j+1)).getObservationDate().getTime() );
				
				double cov = 0.0;
				if (obsType == DISTANCE3D && this.hasDistanceCorrelationFunktion()) 
					cov = this.distCovFunc.getCovariance(dT);
				if (obsType == AZIMUTHANGLE && this.hasAzimuthCorrelationFunktion()) 
					cov = this.azimuthCovFunc.getCovariance(dT);
				if (obsType == ZENITHANGLE && this.hasZenithCorrelationFunktion()) 
					cov = this.zenithCovFunc.getCovariance(dT);

				double s = cov;
				for (int i = 0; i < k; i++) {
					s = s - R[i][k-i]*R[i][j-i];
	            }
				R[k][j-k] = s = s/R[k][k-k];
				d = d + s*s;
			}
			//d = A[j][j] - d;
			double dT = 0.0;
			double cov = 0.0;
			if (obsType == DISTANCE3D && this.hasDistanceCorrelationFunktion()) 
				cov = this.distCovFunc.getCovariance(dT);
			if (obsType == AZIMUTHANGLE && this.hasAzimuthCorrelationFunktion()) 
				cov = this.azimuthCovFunc.getCovariance(dT);
			if (obsType == ZENITHANGLE && this.hasZenithCorrelationFunktion()) 
				cov = this.zenithCovFunc.getCovariance(dT);
			
			d = cov - d;
			R[j][j-j] = Math.sqrt(Math.max(d,0.0));
		}	
		return R;
	}

	public int numberOfTimeDependentParameters() {
		int n=0;
		if (this.hasDistanceCorrelationFunktion())
			n++;
		if (this.hasAzimuthCorrelationFunktion())
			n++;
		if (this.hasZenithCorrelationFunktion())
			n++;
		return n;
	}
	
	public boolean hasDistanceCorrelationFunktion() {
		return this.distCovFunc != null;
	}
	
	public boolean hasAzimuthCorrelationFunktion() {
		return this.azimuthCovFunc != null;
	}
	
	public boolean hasZenithCorrelationFunktion() {
		return this.zenithCovFunc != null;
	}
	
	public CorrelationFunction getCorrelationFunction(int type) {
		switch(type) {
			case DISTANCE3D:
				return this.distCovFunc;
			case AZIMUTHANGLE:
				return this.azimuthCovFunc;
			case ZENITHANGLE:
				return this.zenithCovFunc;	
		}
		return null;
	}
	
//	public static void main(String args[]) throws Exception {
//		LasertrackerUncertaintyModel ut = new LasertrackerUncertaintyModel();
//		
//		CorrelationFunction distCovFunc    = null;
//		CorrelationFunction azimuthCovFunc = null;
//		CorrelationFunction zenithCovFunc  = null;
//
//		distCovFunc    = new ExponentialCosineCorrelationFunction(0.002*0.002,                                                 -1, 0.455596718347454, 0.000123942004019267, 0.000264721734175281);
//		azimuthCovFunc = new ExponentialCosineCorrelationFunction(0.00015*Constant.RHO_GRAD2RAD*0.00015*Constant.RHO_GRAD2RAD, -1, 0.348845275850707, 0.000119519711131458, 0.000382066728933325);
//		zenithCovFunc  = new ExponentialCosineCorrelationFunction(0.00015*Constant.RHO_GRAD2RAD*0.00015*Constant.RHO_GRAD2RAD, -1, 0.673632337215673, 0.000358598447079722, 0.000800223384590601);
//
//		double diagCoVar[] = new double[ut.getDefaultSigmas().length];
//		for (int i=0; i<ut.getDefaultSigmas().length; i++) 
//			diagCoVar[i] = ut.getDefaultSigmas()[i]*ut.getDefaultSigmas()[i];
//		
//		LasertrackerUncertaintyModel ut2 = new LasertrackerUncertaintyModel(diagCoVar, distCovFunc, azimuthCovFunc, zenithCovFunc);
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
