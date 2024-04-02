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

package com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.UnknownParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;

public class TransformationParameterSet3D extends TransformationParameterSet {
	private TransformationParameter[] transformationParameters;
	private TransformationParameter[] additionalTransformationParameters;
	
	public TransformationParameterSet3D() {
		this.setRestriction(TransformationParameterSet.FIXED_LENGTH);
		this.init();
	}
	
	private void init() {
		this.transformationParameters = new TransformationParameter[] {
				new TransformationParameter(UnknownParameter.TYPE_TRANSLATION_X, 0.0),
				new TransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y, 0.0),
				new TransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z, 0.0),
				
				new TransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0, 1.0),
				new TransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1, 0.0),
				new TransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2, 0.0),
				new TransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3, 0.0),
				
				new TransformationParameter(UnknownParameter.TYPE_SCALE,         1.0)
		};
		
		this.additionalTransformationParameters = new TransformationParameter[] {
				new TransformationParameter(UnknownParameter.TYPE_ROTATION_X, 0.0),
				new TransformationParameter(UnknownParameter.TYPE_ROTATION_Y, 0.0),
				new TransformationParameter(UnknownParameter.TYPE_ROTATION_Z, 0.0)
		};
	}
	
	@Override
	public Point getSourceSystemCenterPoint() {
		if (super.getSourceSystemCenterPoint() == null)
			super.setSourceSystemCenterPoint( new Point3D(this.getClass().getSimpleName(),0,0,0) );
		return super.getSourceSystemCenterPoint();
	}
	
	@Override
	public Point getTargetSystemCenterPoint() {
		if (super.getTargetSystemCenterPoint() == null)
			super.setTargetSystemCenterPoint( new Point3D(this.getClass().getSimpleName(),0,0,0) );
		return super.getTargetSystemCenterPoint();
	}
		
	@Override
	public TransformationParameter[] getTransformationParameters() {
		return this.transformationParameters;
	}

	@Override
	public TransformationParameter getTransformationParameter(int paramType) {
		TransformationParameter transformationParameter = null;
		switch (paramType) {
			case UnknownParameter.TYPE_TRANSLATION_X:
				transformationParameter = this.transformationParameters[0];
			break;
			case UnknownParameter.TYPE_TRANSLATION_Y:
				transformationParameter = this.transformationParameters[1];
			break;
			case UnknownParameter.TYPE_TRANSLATION_Z:
				transformationParameter = this.transformationParameters[2];
			break;
			case UnknownParameter.TYPE_QUATERNION_Q0:
				transformationParameter = this.transformationParameters[3];
			break;
			case UnknownParameter.TYPE_QUATERNION_Q1:
				transformationParameter = this.transformationParameters[4];
			break;
			case UnknownParameter.TYPE_QUATERNION_Q2:
				transformationParameter = this.transformationParameters[5];
			break;
			case UnknownParameter.TYPE_QUATERNION_Q3:
				transformationParameter = this.transformationParameters[6];
			break;
			case UnknownParameter.TYPE_SCALE:
				transformationParameter = this.transformationParameters[7];
			break;
		}
		return transformationParameter;
	}
	
	@Override
	public Point inverseTransform(Point point) {		
		double XP = point.getX();
		double YP = point.getY();
		double ZP = point.getZ();

		// Transformationsparameter
	    double x0 = this.transformationParameters[0].getValue();
	    double y0 = this.transformationParameters[1].getValue();
	    double z0 = this.transformationParameters[2].getValue();

	    double q0 = this.transformationParameters[3].getValue();
	    double q1 = this.transformationParameters[4].getValue();
	    double q2 = this.transformationParameters[5].getValue();
	    double q3 = this.transformationParameters[6].getValue();
	    
	    double m  = this.transformationParameters[7].getValue();
		
	    double xP = 1.0/m * ((2.0*q0*q0-1.0+2.0*q1*q1)*(XP-x0) + (2.0*(q1*q2+q0*q3))*(YP-y0) + (2.0*(q1*q3-q0*q2))*(ZP-z0));
	    double yP = 1.0/m * ((2.0*(q1*q2-q0*q3))*(XP-x0) + (2.0*q0*q0-1.0+2.0*q2*q2)*(YP-y0) + (2.0*(q2*q3+q0*q1))*(ZP-z0));
	    double zP = 1.0/m * ((2.0*(q1*q3+q0*q2))*(XP-x0) + (2.0*(q2*q3-q0*q1))*(YP-y0) + (2.0*q0*q0-1.0+2.0*q3*q3)*(ZP-z0));

		return new Point3D(point.getId(), xP, yP, zP);
	}
	
	@Override
	public Point transform(Point point) {		
		double xP = point.getX();
		double yP = point.getY();
		double zP = point.getZ();

		// Transformationsparameter
	    double x0 = this.transformationParameters[0].getValue();
	    double y0 = this.transformationParameters[1].getValue();
	    double z0 = this.transformationParameters[2].getValue();

	    double q0 = this.transformationParameters[3].getValue();
	    double q1 = this.transformationParameters[4].getValue();
	    double q2 = this.transformationParameters[5].getValue();
	    double q3 = this.transformationParameters[6].getValue();
	    
	    double m  = this.transformationParameters[7].getValue();
		
		double XP = (x0 + m*((2.0*q0*q0-1.0+2.0*q1*q1)*xP + (2.0*(q1*q2-q0*q3))*yP + (2.0*(q1*q3+q0*q2))*zP));
		double YP = (y0 + m*((2.0*(q1*q2+q0*q3))*xP + (2.0*q0*q0-1.0+2.0*q2*q2)*yP + (2.0*(q2*q3-q0*q1))*zP));
		double ZP = (z0 + m*((2.0*(q1*q3-q0*q2))*xP + (2.0*(q2*q3+q0*q1))*yP + (2.0*q0*q0-1.0+2.0*q3*q3)*zP));

		return new Point3D(point.getId(), XP, YP, ZP);
	}
	
	@Override
	public String toString() {
		return 	"x0 = " + this.transformationParameters[0].getValue() +"\n"+
				"y0 = " + this.transformationParameters[1].getValue() +"\n"+
				"z0 = " + this.transformationParameters[2].getValue() +"\n"+
				"q0 = " + this.transformationParameters[3].getValue() +"\n"+
				"q1 = " + this.transformationParameters[4].getValue() +"\n"+
				"q2 = " + this.transformationParameters[5].getValue() +"\n"+
				"q3 = " + this.transformationParameters[6].getValue() +"\n"+
				" m = " + this.transformationParameters[7].getValue() +"\n"+
				"rx = " + this.additionalTransformationParameters[0].getValue() +"\n"+
				"ry = " + this.additionalTransformationParameters[1].getValue() +"\n"+
				"rz = " + this.additionalTransformationParameters[2].getValue();
	}
	
	@Override
	public TransformationParameter[] getAdditionalTransformationParameters() {
		return this.additionalTransformationParameters;
	}
	
	@Override
	public TransformationParameter getAdditionalTransformationParameter(int paramType) {
		TransformationParameter transformationParameter = null;
		switch (paramType) {
			case UnknownParameter.TYPE_ROTATION_X:
				transformationParameter = this.additionalTransformationParameters[0];
			break;
			case UnknownParameter.TYPE_ROTATION_Y:
				transformationParameter = this.additionalTransformationParameters[1];
			break;
			case UnknownParameter.TYPE_ROTATION_Z:
				transformationParameter = this.additionalTransformationParameters[2];
			break;
		}
		return transformationParameter;
	}
}
