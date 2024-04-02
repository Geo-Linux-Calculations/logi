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

package com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.correlation;

import com.derletztekick.tools.geodesy.Constant;

public abstract class CorrelationFunction {
	public final static int EXP_COS_CORR_FUN  = 100;
	public final static int GAUSSIAN_CORR_FUN = 200;
	private double param[];
	private double sigma, maxDT;
	public CorrelationFunction(double sigma, double maxDT, double param[]) {
		this.sigma = sigma;
		this.param = param;
		this.maxDT = maxDT;
	}
	
	public int numberOfFunctionParameters() {
		return this.param.length;
	}
	
	public double getFunctionParameter(int i) {
		return this.param[i];
	}
	
	public double getVarianceFactor() {
		return this.sigma*this.sigma;
	}
	
	public double getMaximalTimeDifference() {
		return this.maxDT <= 0 ? Double.MAX_VALUE : this.maxDT;
	}
	
	public double getCovariance(double dT) {
		dT = Math.abs(dT);
		if (dT > this.getMaximalTimeDifference())
			return 0;
		if (dT < Constant.EPS)
			return this.getVarianceFactor();
		return this.getCovarianceValue(dT);
	}
	
	abstract double getCovarianceValue(double dT);
	public abstract int getType();
	
	public String toString() {
		return this.getClass().getSimpleName()+": type = " +this.getType() + ", sigma = " + this.sigma + ", Param = " + java.util.Arrays.toString(this.param);
	}
}
