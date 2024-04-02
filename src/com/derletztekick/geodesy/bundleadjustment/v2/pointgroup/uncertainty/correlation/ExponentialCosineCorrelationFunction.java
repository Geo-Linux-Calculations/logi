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

public class ExponentialCosineCorrelationFunction extends CorrelationFunction {
	public ExponentialCosineCorrelationFunction(double sigma, double maxDT, double a, double b, double c) {
		super(sigma, maxDT, new double[] {a,b,c});
	}

	@Override
	double getCovarianceValue(double dT) {
		double a  = this.getFunctionParameter(0);
		double b  = this.getFunctionParameter(1);
		double c  = this.getFunctionParameter(2);
		double s2 = this.getVarianceFactor();
		return s2 * a * Math.exp(-b * Math.abs(dT)) * Math.cos(c * Math.abs(dT));
	}

	@Override
	public int getType() {
		return CorrelationFunction.EXP_COS_CORR_FUN;
	}
}
