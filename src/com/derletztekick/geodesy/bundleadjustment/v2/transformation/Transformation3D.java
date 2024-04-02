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

import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
import no.uib.cipr.matrix.Vector;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.UnknownParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet;
import com.derletztekick.tools.geodesy.MathExtension;

public class Transformation3D extends Transformation {

	public Transformation3D () {
		super();
	}
	
	protected TransformationParameterSet getApproximatedValues(Set<String> identicalPointIds, PointGroup srcSystem, PointGroup trgSystem, TransformationParameterSet transformationParameters) {
		double tx=0, ty=0, tz=0, q0=1, q1=0, q2=0, q3=0, m=1;
		
		PointGroup sourceSystem = new PointGroup(srcSystem.getId());
		PointGroup targetSystem = new PointGroup(trgSystem.getId());
		
		// Bestimme homologe Punkte
		if (identicalPointIds == null) {
			for (int i=0; i<trgSystem.size(); i++) {
				Point trgPoint = trgSystem.get(i);
				for (int j=0; j<srcSystem.size(); j++) {
					Point srcPoint = srcSystem.get(j);
					if (srcPoint.getId().equals(trgPoint.getId())) {
						sourceSystem.add(srcPoint);
						targetSystem.add(trgPoint);
						break;
					}
				}
			}
		}
		else {
			for (String pointId : identicalPointIds) {
				Point trgPoint = trgSystem.get(pointId);
				Point srcPoint = srcSystem.get(pointId);
				if (trgPoint != null && srcPoint != null) {
					sourceSystem.add(srcPoint);
					targetSystem.add(trgPoint);
				}
			}
		}

		
		// Anzahl der homologen Punkte und der Trafo-Dimension
		int noh = targetSystem.size();
		int dim = this.getDimension();
		
		// Schwerpunkt
		Point cS = sourceSystem.getCenterPoint();
		Point cT = targetSystem.getCenterPoint();
		
		Matrix R = MathExtension.identity(dim);
		
		if (transformationParameters.isRestricted(TransformationParameterSet.FIXED_ROTATION_X) && transformationParameters.isRestricted(TransformationParameterSet.FIXED_ROTATION_Y) && transformationParameters.isRestricted(TransformationParameterSet.FIXED_ROTATION_Z)) {
			q0 = 1.0;
			q1 = q2 = q3 = 0.0;
		}
		else {
			// Naeherung fuer q (Fallback) - vgl. Gielsdorf
			q0 = q1 = q2 = q3 = 0.5;
			// Bestimme Rotationsquaternion - vgl. Loesler
			DenseMatrix S = new DenseMatrix(3,3);
						
			for (int k=0; k<noh; k++) {
				Point pS = sourceSystem.get(k);
				Point pT = targetSystem.get(pS.getId());
				for (int i=0; i<dim; i++) {
					for (int j=0; j<dim; j++) {
						double a=0, b=0;
						
						if (i==0)
							a = pS.getX() - cS.getX(); 
						else if (i==1)
							a = pS.getY() - cS.getY();
						else 
							a = pS.getZ() - cS.getZ();
						
						if (j==0)
							b = pT.getX() - cT.getX(); 
						else if (j==1)
							b = pT.getY() - cT.getY();
						else 
							b = pT.getZ() - cT.getZ();
						
						S.set(i,j, S.get(i,j) + a * b);
					}
				}			
			}
			DenseMatrix N = new DenseMatrix(4,4);
			N.set(0,0, S.get(0,0)+S.get(1,1)+S.get(2,2));
			N.set(0,1, S.get(1,2)-S.get(2,1));
			N.set(0,2, S.get(2,0)-S.get(0,2));
			N.set(0,3, S.get(0,1)-S.get(1,0));
			
			N.set(1,0, S.get(1,2)-S.get(2,1));
			N.set(1,1, S.get(0,0)-S.get(1,1)-S.get(2,2));
			N.set(1,2, S.get(0,1)+S.get(1,0));
			N.set(1,3, S.get(2,0)+S.get(0,2));
			
			N.set(2,0, S.get(2,0)-S.get(0,2));
			N.set(2,1, S.get(0,1)+S.get(1,0));
			N.set(2,2,-S.get(0,0)+S.get(1,1)-S.get(2,2));
			N.set(2,3, S.get(1,2)+S.get(2,1));
			
			N.set(3,0, S.get(0,1)-S.get(1,0));
			N.set(3,1, S.get(2,0)+S.get(0,2));
			N.set(3,2, S.get(1,2)+S.get(2,1));
			N.set(3,3,-S.get(0,0)-S.get(1,1)+S.get(2,2));
			
			EVD evd = new EVD(4);
			try {
				evd.factor(N);
				
				if (evd.hasRightEigenvectors()) {
					Matrix eigVec = evd.getRightEigenvectors();
					double eigVal[] = evd.getRealEigenvalues();
					
					int indexMaxEigVal = 0;
					double maxEigVal = eigVal[indexMaxEigVal];
					for (int i=indexMaxEigVal+1; i<eigVal.length; i++) {
						if (maxEigVal < eigVal[i]) {
							maxEigVal = eigVal[i];
							indexMaxEigVal = i;
						}
					}
					// Setze berechnetes Quaternion ein
					q0 = eigVec.get(0, indexMaxEigVal);
					q1 = eigVec.get(1, indexMaxEigVal);
					q2 = eigVec.get(2, indexMaxEigVal);
					q3 = eigVec.get(3, indexMaxEigVal);
				}
				
			} catch (NotConvergedException e) {
				e.printStackTrace();
			}	
		}
		
		R = new DenseMatrix( new double[][]{
				{2.0*q0*q0-1.0+2.0*q1*q1,  2.0*(q1*q2-q0*q3),  2.0*(q1*q3+q0*q2)},
				{2.0*(q1*q2+q0*q3),  2.0*q0*q0-1.0+2.0*q2*q2,  2.0*(q2*q3-q0*q1)},
				{2.0*(q1*q3-q0*q2),  2.0*(q2*q3+q0*q1),  2.0*q0*q0-1.0+2.0*q3*q3}
		});

		// Bestimmung des Maßstabs
		if (transformationParameters.isRestricted(TransformationParameterSet.FIXED_SCALE)) {
			m = 1.0;
		}
		else {
			double m1 = 0;
			double m2 = 0;
			
			for (int i=0; i<noh; i++) {
				Point pS = sourceSystem.get(i);
				Point pT = targetSystem.get(pS.getId());
				
				Vector vS = new DenseVector(new double [] {
						pS.getX() - cS.getX(),
						pS.getY() - cS.getY(),
						pS.getZ() - cS.getZ()
				});
				
				Vector vT = new DenseVector(new double [] {
						pT.getX() - cT.getX(),
						pT.getY() - cT.getY(),
						pT.getZ() - cT.getZ()
				});
				Vector RvS = new DenseVector(3);
				R.mult(vS, RvS);
				
				m1 += vT.dot(RvS);
				m2 += vS.dot(vS);
			}
			
			m = m2!=0?m1/m2:1.0;
		}
		
		// Bestimme Translationsvektor, da Schwerpunktreduziert weitergerechnet wird, unnoetig!
		Vector vS = new DenseVector(new double [] {	cS.getX(), cS.getY(), cS.getZ()	});
		
		Vector RvS = new DenseVector(3);
		R.mult(vS, RvS);
		
		if (!transformationParameters.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X)) 
			tx = cT.getX() - m*RvS.get(0);

		if (!transformationParameters.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y)) 
			ty = cT.getY() - m*RvS.get(1);
		
		if (!transformationParameters.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z)) 
			tz = cT.getZ() - m*RvS.get(2);
		
		// Setzte neue Trafo-Parameter
		transformationParameters.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_X).setValue(tx);
		transformationParameters.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y).setValue(ty);
		transformationParameters.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z).setValue(tz);
		
		transformationParameters.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).setValue(q0);
		transformationParameters.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).setValue(q1);
		transformationParameters.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).setValue(q2);
		transformationParameters.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).setValue(q3);
		
		transformationParameters.getTransformationParameter(UnknownParameter.TYPE_SCALE).setValue(m);

		return transformationParameters;
	}
	
	@Override
	public double getJacobiElement(TransformationParameterSet trafoParam, Point point, int parameterType, int equationIndex) {
		double elmA = 0;

		//Schwerpunktreduktion
		Point srcCenterPoint = trafoParam.getSourceSystemCenterPoint();
		double xP = point.getX() - srcCenterPoint.getX();
		double yP = point.getY() - srcCenterPoint.getY();
		double zP = point.getZ() - srcCenterPoint.getZ();
	    
		// Transformationsparameter
		double q0 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).getValue();
		double q1 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).getValue();
		double q2 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).getValue();
		double q3 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).getValue();
		
		double m  = trafoParam.getTransformationParameter(UnknownParameter.TYPE_SCALE).getValue();
		
	    if (equationIndex == 0) {
			switch(parameterType) {
				// X0
				case UnknownParameter.TYPE_TRANSLATION_X:
					elmA = 1.0;
				break;
				// Y0
				case UnknownParameter.TYPE_TRANSLATION_Y:
					elmA = 0.0;
				break;
				// Z0
				case UnknownParameter.TYPE_TRANSLATION_Z:
					elmA = 0.0;
				break;
				// q0
				case UnknownParameter.TYPE_QUATERNION_Q0:
					elmA = 2.0*m*(2.0*q0*xP-q3*yP+q2*zP);
				break;
				// q1
				case UnknownParameter.TYPE_QUATERNION_Q1:
					elmA = 2.0*m*(2.0*q1*xP+q2*yP+q3*zP);
				break;
				// q2
				case UnknownParameter.TYPE_QUATERNION_Q2:
					elmA = 2.0*m*(q1*yP+q0*zP);
				break;
				// q3
				case UnknownParameter.TYPE_QUATERNION_Q3:
					elmA = 2.0*m*(q1*zP-q0*yP);
				break;
				// m
				case UnknownParameter.TYPE_SCALE:
					elmA = (2.0*q0*q0-1.0+2.0*q1*q1)*xP+2.0*(q1*q2-q0*q3)*yP+2.0*(q1*q3+q0*q2)*zP;
				break;
			}	    	
	    }
	    else if (equationIndex == 1) {
			switch(parameterType) {
				// X0
				case UnknownParameter.TYPE_TRANSLATION_X:
					elmA = 0.0;
				break;
				// Y0
				case UnknownParameter.TYPE_TRANSLATION_Y:
					elmA = 1.0;
				break;
				// Z0
				case UnknownParameter.TYPE_TRANSLATION_Z:
					elmA = 0.0;
				break;
				// q0
				case UnknownParameter.TYPE_QUATERNION_Q0:
					elmA = 2.0*m*(q3*xP+2.0*q0*yP-q1*zP);
				break;
				// q1
				case UnknownParameter.TYPE_QUATERNION_Q1:
					elmA = 2.0*m*(q2*xP-q0*zP);
				break;
				// q2
				case UnknownParameter.TYPE_QUATERNION_Q2:
					elmA = 2.0*m*(q1*xP+2.0*q2*yP+q3*zP);
				break;
				// q3
				case UnknownParameter.TYPE_QUATERNION_Q3:
					elmA = 2.0*m*(q0*xP+q2*zP);
				break;
				// m
				case UnknownParameter.TYPE_SCALE:
					elmA = 2.0*(q1*q2+q0*q3)*xP+(2.0*q0*q0-1.0+2.0*q2*q2)*yP+2.0*(q2*q3-q0*q1)*zP;
				break;
			}	    	
	    }
	    else if (equationIndex == 2) {
			switch(parameterType) {
				// X0
				case UnknownParameter.TYPE_TRANSLATION_X:
					elmA = 0.0;
				break;
				// Y0
				case UnknownParameter.TYPE_TRANSLATION_Y:
					elmA = 0.0;
				break;
				// Z0
				case UnknownParameter.TYPE_TRANSLATION_Z:
					elmA = 1.0;
				break;
				// q0
				case UnknownParameter.TYPE_QUATERNION_Q0:
					elmA = 2.0*m*(q1*yP-q2*xP+2.0*q0*zP);
				break;
				// q1
				case UnknownParameter.TYPE_QUATERNION_Q1:
					elmA = 2.0*m*(q3*xP+q0*yP);
				break;
				// q2
				case UnknownParameter.TYPE_QUATERNION_Q2:
					elmA = 2.0*m*(q3*yP-q0*xP);
				break;
				// q3
				case UnknownParameter.TYPE_QUATERNION_Q3:
					elmA = 2.0*m*(q1*xP+q2*yP+2.0*q3*zP);
				break;
				// m
				case UnknownParameter.TYPE_SCALE:
					elmA = 2.0*(q1*q3-q0*q2)*xP+2.0*(q2*q3+q0*q1)*yP+(2.0*q0*q0-1.0+2.0*q3*q3)*zP;
				break;
			}	    	
	    }

		return elmA;
	}
	
	@Override
	public double getConditionElement(TransformationParameterSet trafoParam, int coordIndex, int equationIndex) {
		// Transformationsparameter
		double q0 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).getValue();
		double q1 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).getValue();
		double q2 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).getValue();
		double q3 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).getValue();
		
		double m  = trafoParam.getTransformationParameter(UnknownParameter.TYPE_SCALE).getValue();
	    
	    double elmB = 0.0;
	    
	    if (equationIndex == 0) {
			switch (coordIndex) {
				// xS-Wert
				case 0:
					elmB = m*(2.0*q0*q0-1.0+2.0*q1*q1);
				break;
				// yS-Wert
				case 1:
					elmB = m*(2.0*(q1*q2-q0*q3));
				break;
				// zS-Wert
				case 2:
					elmB = m*(2.0*(q1*q3+q0*q2));
				break;
			}
		}
		else if (equationIndex == 1) {
			switch (coordIndex) {
				// xS-Wert
				case 0:
					elmB = m*(2.0*(q1*q2+q0*q3));
				break;
				// yS-Wert
				case 1:
					elmB = m*(2.0*q0*q0-1.0+2.0*q2*q2);
				break;
				// zS-Wert
				case 2:
					elmB = m*(2.0*(q2*q3-q0*q1));
				break;
			}
		}
		else if (equationIndex == 2) {
			switch (coordIndex) {
				// xS-Wert
				case 0:
					elmB = m*(2.0*(q1*q3-q0*q2));
				break;
				// yS-Wert
				case 1:
					elmB = m*(2.0*(q2*q3+q0*q1));
				break;
				// zS-Wert
				case 2:
					elmB = m*(2.0*q0*q0-1.0+2.0*q3*q3);
				break;
			}
		}	    
		return elmB;
	}
	
	@Override
	public double getContradiction(TransformationParameterSet trafoParam, Point srcPoint, int equationIndex, Point trgPoint) {
		//Schwerpunktreduktion
		Point srcCenterPoint = trafoParam.getSourceSystemCenterPoint();
		Point trgCenterPoint = trafoParam.getTargetSystemCenterPoint();

		double xP = srcPoint.getX() - srcCenterPoint.getX();
		double yP = srcPoint.getY() - srcCenterPoint.getY();
		double zP = srcPoint.getZ() - srcCenterPoint.getZ();
		
		double XP = trgPoint.getX() - trgCenterPoint.getX();
		double YP = trgPoint.getY() - trgCenterPoint.getY();
		double ZP = trgPoint.getZ() - trgCenterPoint.getZ();
	    
		// Transformationsparameter
		double tx = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_X).getValue();
		double ty = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y).getValue();
		double tz = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z).getValue();
		
		double q0 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).getValue();
		double q1 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).getValue();
		double q2 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).getValue();
		double q3 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).getValue();
		
		double m  = trafoParam.getTransformationParameter(UnknownParameter.TYPE_SCALE).getValue();

		if (equationIndex == 0) 
			return (tx + m*((2.0*q0*q0-1.0+2.0*q1*q1)*xP + (2.0*(q1*q2-q0*q3))*yP + (2.0*(q1*q3+q0*q2))*zP)) - XP;
		else if (equationIndex == 1) 
			return (ty + m*((2.0*(q1*q2+q0*q3))*xP + (2.0*q0*q0-1.0+2.0*q2*q2)*yP + (2.0*(q2*q3-q0*q1))*zP)) - YP;
		return (tz + m*((2.0*(q1*q3-q0*q2))*xP + (2.0*(q2*q3+q0*q1))*yP + (2.0*q0*q0-1.0+2.0*q3*q3)*zP)) - ZP;
	}
	
	@Override
	public double getRestrictionElement(TransformationParameterSet trafoParam, int parameterIndex, int equationIndex) {
		double elmR = 0.0;
				
		// Transformationsparameter
		double q0 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).getValue();
		double q1 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).getValue();
		double q2 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).getValue();
		double q3 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).getValue();
			    	    
	    if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_LENGTH) {
	    	switch(parameterIndex) {
				// q0
				case 3:
					elmR = 2.0*q0;
				break;
				// q1
				case 4:
					elmR = 2.0*q1;
				break;
				// q2
				case 5:
					elmR = 2.0*q2;
				break;
				// q3
				case 6:
					elmR = 2.0*q3;
				break;
	    	}
	    }
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_TRANSLATION_X) {
	    	switch(parameterIndex) {
				// x0
				case 0:
					elmR = 1.0;
				break;				
	    	}
	    }
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_TRANSLATION_Y) {
	    	switch(parameterIndex) {
				// y0
				case 1:
					elmR = 1.0;
				break;
	    	}
	    }
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_TRANSLATION_Z) {
	    	switch(parameterIndex) {
				// z0
				case 2:
					elmR = 1.0;
				break;
	    	}
	    }
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_ROTATION_X) {
	    	switch(parameterIndex) {
				// q0
				case 3:
					elmR = -q1;
				break;
				// q1
				case 4:
					elmR = -q0;
				break;
				// q2
				case 5:
					elmR = q3;
				break;
				// q3
				case 6:
					elmR = q2;
				break;
	    	}
	    }
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_ROTATION_Y) {
	    	switch(parameterIndex) {
				// q0
				case 3:
					elmR = q2;
				break;
				// q1
				case 4:
					elmR = q3;
				break;
				// q2
				case 5:
					elmR = q0;
				break;
				// q3
				case 6:
					elmR = q1;
				break;
	    	}
	    }	
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_ROTATION_Z) {
	    	switch(parameterIndex) {
				// q0
				case 3:
					elmR = -q3;
				break;
				// q1
				case 4:
					elmR =  q2;
				break;
				// q2
				case 5:
					elmR =  q1;
				break;
				// q3
				case 6:
					elmR = -q0;
				break;
	    	}
	    }
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_SCALE) {
	    	switch(parameterIndex) {
				// mx
				case 7:
					elmR = 1.0;
				break;
				
	    	}
	    }
		return elmR;
	}
	
	@Override
	public double getRestrictionContradiction(TransformationParameterSet trafoParam, int equationIndex) {
   
		// Transformationsparameter
		double tx = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_X).getValue();
		double ty = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y).getValue();
		double tz = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z).getValue();
		
		double q0 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).getValue();
		double q1 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).getValue();
		double q2 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).getValue();
		double q3 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).getValue();
		
		double m  = trafoParam.getTransformationParameter(UnknownParameter.TYPE_SCALE).getValue();

	    if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_LENGTH)
	    	return (q0*q0 + q1*q1 + q2*q2 + q3*q3) - 1.0;
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_TRANSLATION_X) 
	    	return tx; 
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_TRANSLATION_Y) 
	    	return ty; 
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_TRANSLATION_Z) 
	    	return tz; 
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_ROTATION_X) 
	    	return q2*q3-q0*q1;	
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_ROTATION_Y) 
	    	return q1*q3+q0*q2;	
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_ROTATION_Z) 
	    	return q1*q2-q0*q3;
	    else if (trafoParam.getRestriction(equationIndex) == TransformationParameterSet.FIXED_SCALE) 
	    	return m - 1.0;
	    	    
	    return 0;
	}
	
	@Override
	public Matrix addDatumConditions(PointGroup datumPointGroup, Matrix M) {
		Point centerPoint = datumPointGroup.getCenterPoint();
		TransformationParameterSet trafoParamSet = datumPointGroup.getTransformationParameterSet();
		int numDatum = this.numberOfDatumConditions(trafoParamSet);

		int row = M.numRows() - numDatum;
		double norm[] = new double[numDatum];
		
		for (int j=0; j<datumPointGroup.size(); j++) {
			Point point = datumPointGroup.get(j);
			// Reduktion auf den Schwerpunkt der Datumspunkte
			double x = point.getX() - centerPoint.getX();
			double y = point.getY() - centerPoint.getY();
			double z = point.getZ() - centerPoint.getZ();
			
			int colNorm = 0;
			
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X))
				norm[colNorm++]++;
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y))
				norm[colNorm++]++;
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z))
				norm[colNorm++]++;
			
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_SCALE))
				norm[colNorm++] += x*x + y*y + z*z;
			
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_X))
				norm[colNorm++] += z*z + y*y;
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Y))
				norm[colNorm++] += x*x + z*z;
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Z))
				norm[colNorm++] += x*x + y*y;
		}
		
		for (int i=0; i<norm.length; i++) {
			norm[i] = Math.sqrt(norm[i]);
		}
		
		for (int j=0; j<datumPointGroup.size(); j++) {
			Point point = datumPointGroup.get(j);
			int col = point.getColInJacobiMatrix();
			// Reduktion auf den Schwerpunkt der Datumspunkte
			double x = point.getX() - centerPoint.getX();
			double y = point.getY() - centerPoint.getY();
			double z = point.getZ() - centerPoint.getZ();
			
			int rowM = row;
			int colNorm = 0;
			
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X))
				M.set(col, rowM++, 1.0/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
			
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_SCALE))
				M.set(col, rowM++,   x/norm[colNorm++]);
			      
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_X))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Y))
				M.set(col, rowM++,   z/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Z))
				M.set(col, rowM++,  -y/norm[colNorm++]);
			   
			col++;
			rowM = row;
			colNorm = 0;
			
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y))
				M.set(col, rowM++, 1.0/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
				      
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_SCALE))
				M.set(col, rowM++,   y/norm[colNorm++]);
			                     
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_X))
				M.set(col, rowM++,  -z/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Y))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Z))
				M.set(col, rowM++,   x/norm[colNorm++]);
			      
			col++;
			rowM = row;
			colNorm = 0;
			
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z))
				M.set(col, rowM++, 1.0/norm[colNorm++]);
				       
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_SCALE))
				M.set(col, rowM++,   z/norm[colNorm++]);
			                     
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_X))
				M.set(col, rowM++,   y/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Y))
				M.set(col, rowM++,  -x/norm[colNorm++]);
			if (!trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Z))
				M.set(col, rowM++, 0.0/norm[colNorm++]);
		
		}
		return M;
	}

	
	@Override
	public final int numberOfUnknownsPerSubsystem() {
		return 8;
	}
	
	@Override
	public final int getDimension() {
		return 3;
	}
	
	@Override
	public final int numberOfRequiredHomologousPointsPerSubsystem() {
		return 3;
	}
	
	@Override
	public final int numberOfDatumConditions(TransformationParameterSet trafoParamSet) {
		if (!this.isFreeNetAdjustment())
			return 0;
		
		int numDatum = 7;
		// Reduziere bereits feste Parameter
		numDatum = trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X)?numDatum-1:numDatum;
		numDatum = trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y)?numDatum-1:numDatum;
		numDatum = trafoParamSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z)?numDatum-1:numDatum;
		
		numDatum = trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_X)?numDatum-1:numDatum;
		numDatum = trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Y)?numDatum-1:numDatum;
		numDatum = trafoParamSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Z)?numDatum-1:numDatum;
		
		numDatum = trafoParamSet.isRestricted(TransformationParameterSet.FIXED_SCALE)?numDatum-1:numDatum;
		return numDatum;
	}

	@Override
	protected void transformParameterSet(TransformationParameterSet trafoParam, Matrix Cxx) {
		//Schwerpunktreduktion
		Point srcCenterPoint = trafoParam.getSourceSystemCenterPoint();
		Point trgCenterPoint = trafoParam.getTargetSystemCenterPoint();
		
		// Transformationsparameter
		double tx = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_X).getValue();
		double ty = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y).getValue();
		double tz = trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z).getValue();
		
		double q0 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).getValue();
		double q1 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).getValue();
		double q2 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).getValue();
		double q3 = trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).getValue();
		
		double m  = trafoParam.getTransformationParameter(UnknownParameter.TYPE_SCALE).getValue();

		// Schwerpunkt Startsystem
		double xP = -srcCenterPoint.getX();
		double yP = -srcCenterPoint.getY();
		double zP = -srcCenterPoint.getZ();

		// Schwerpunkt Zielsystem
		double XP = trgCenterPoint.getX();
		double YP = trgCenterPoint.getY();
		double ZP = trgCenterPoint.getZ();

		// Umkehrung der Schwerpunktreduktion
		tx = tx + XP + m*((2.0*q0*q0-1.0+2.0*q1*q1)*xP + (2.0*(q1*q2-q0*q3))*yP + (2.0*(q1*q3+q0*q2))*zP);
		ty = ty + YP + m*((2.0*(q1*q2+q0*q3))*xP + (2.0*q0*q0-1.0+2.0*q2*q2)*yP + (2.0*(q2*q3-q0*q1))*zP);
		tz = tz + ZP + m*((2.0*(q1*q3-q0*q2))*xP + (2.0*(q2*q3+q0*q1))*yP + (2.0*q0*q0-1.0+2.0*q3*q3)*zP);
		
		trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_X).setValue(tx);
		trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y).setValue(ty);
		trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z).setValue(tz);
		
		// Bestimmung der Unsicherheiten der abgeleiteten Parameter
		int nou = this.numberOfUnknownsPerSubsystem();
		int dim = this.getDimension();
		
		// Bestimme Covar der Trafoparameter
		Matrix Cpp = new UpperSymmPackMatrix(nou);
		TransformationParameter trafoParams[] = trafoParam.getTransformationParameters();
		for (int i=0; i<nou; i++) {
			int row = trafoParams[i].getColInJacobiMatrix();
			for (int j=i; j<nou; j++) {
				int col = trafoParams[j].getColInJacobiMatrix();
				Cpp.set(i, j, Cxx.get(row, col));
			}
		}

		Point point = new Point3D(this.getClass().getSimpleName(), xP, yP, zP);
		Matrix A = Matrices.identity(nou);

		for (int i=0; i<dim; i++) {
		        for (int j=dim; j<nou; j++) {
		                A.set(i,j, this.getJacobiElement(trafoParam, point, j, i));
		        }
		}

		Matrix CxxAT = new DenseMatrix(nou,nou);

		Cpp.transBmult(A, CxxAT);
		A.mult(CxxAT, Cpp);
		
		// Speichere Standardabweichungen und ggf. ob Parameter nun signifikant ist
		final double kPostParam = this.getObservationTestValues().getKpostAB(1);
		for (int i=0; i<nou; i++) {
			double cpp = Math.abs(Cpp.get(i,i));
			double d = trafoParams[i].getValue() - trafoParams[i].getInitialisationValue();
			double tPost = d*d/cpp;
			trafoParams[i].setSignificant(tPost > kPostParam);
			trafoParams[i].setStd(Math.sqrt(cpp));
		}
		
		// Setze "neuen" Schwerpunkt
		trafoParam.setSourceSystemCenterPoint(new Point3D(this.getClass().getSimpleName(),0,0,0));
		trafoParam.setTargetSystemCenterPoint(new Point3D(this.getClass().getSimpleName(),0,0,0));
		
		// Pruefe, ob Zusatzparameter existieren und ermittle diese
		if (trafoParam.hasAdditionalParameters()) {
			// Elemente der Rotationsmatrix
			double r13 = 2.0*(q1*q3+q0*q2);
			double r11 = 2.0*q0*q0-1.0+2.0*q1*q1;
			double r12 = 2.0*(q1*q2-q0*q3);
			double r23 = 2.0*(q2*q3-q0*q1);
			double r33 = 2.0*q0*q0-1.0+2.0*q3*q3;
			
			// Drehwinkel der Rotationsmatrizen
			double rx = MathExtension.MOD( Math.atan2(r23, r33), 2.0*Math.PI );
			double ry = MathExtension.MOD( Math.asin(-r13)     , 2.0*Math.PI );
			double rz = MathExtension.MOD( Math.atan2(r12, r11), 2.0*Math.PI );
						
			A = new DenseMatrix(3,nou);
			// rx
			A.set(0, 3, -2.0*(q1*r33+2.0*r23*q0)/(r33*r33+r23*r23) );
			A.set(0, 4, -2.0*q0*r33/(r33*r33+r23*r23) );
			A.set(0, 5,  2.0*q3*r33/(r33*r33+r23*r23) );
			A.set(0, 6,  2.0*(q2*r33-2.0*r23*q3)/(r33*r33+r23*r23) );
			
			// ry
			A.set(1, 3, -2.0*q2/Math.sqrt(1.0-(r13*r13)) );
			A.set(1, 4, -2.0*q3/Math.sqrt(1.0-(r13*r13)) );
			A.set(1, 5, -2.0*q0/Math.sqrt(1.0-(r13*r13)) );
			A.set(1, 6, -2.0*q1/Math.sqrt(1.0-(r13*r13)) );
			
			// rz
			A.set(2, 3, -2.0*(q3*r11+2.0*r12*q0)/(r11*r11+r12*r12) );
			A.set(2, 4,  2.0*(q2*r11-2.0*r12*q1)/(r11*r11+r12*r12) );
			A.set(2, 5,  2.0*q1*r11/(r11*r11+r12*r12) );
			A.set(2, 6, -2.0*q0*r11/(r11*r11+r12*r12) );
			
			Matrix AQxx = new DenseMatrix(3, nou);
			A.mult(Cpp, AQxx);
			Matrix AQxxAT = new UpperSymmPackMatrix(3);
			AQxx.transBmult(A, AQxxAT);

			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_X).setValue(rx);
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_Y).setValue(ry);
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_Z).setValue(rz);
			
			TransformationParameter addTrafoParams[] = trafoParam.getAdditionalTransformationParameters();
			for (int i=0; i<addTrafoParams.length; i++) {
				double value = Math.min(addTrafoParams[i].getValue(), 2.0*Math.PI-addTrafoParams[i].getValue());
				double cpp = Math.abs(AQxxAT.get(i,i));
				double d = value - addTrafoParams[i].getInitialisationValue();
				double tPost = d*d/cpp;
				addTrafoParams[i].setSignificant(tPost > kPostParam);
				addTrafoParams[i].setStd(Math.sqrt(cpp));
			}
		}
		
	}
}
