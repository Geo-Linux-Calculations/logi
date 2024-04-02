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

public class TransformationParameter  extends UnknownParameter {
	private final int type;
	private double value = 0, sigma = 0;
	private final double value0;
	private boolean significant = false;
	public TransformationParameter(int type, double value) {
		this.type   = type;
		this.value  = value;
		this.value0 = value;
	}

	@Override
	public int getParameterTyp() {
		return this.type;
	}
	
	/**
	 * Liefert den Wert des Transformationsparameters
	 * @return value
	 */
	public double getValue() {
		return this.value;
	}
	
	/**
	 * Setzt den Wert des Transformationsparameters
	 * @param value
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * Liefert den Wert des Transformationsparameters bei seiner Initialisierung
	 * @return value0
	 */
	public final double getInitialisationValue() {
		return this.value0;
	}

	/**
	 * Liefert den Wert der Standardabweichung des Transformationsparameters
	 * @return sigma
	 */
	public double getStd() {
		return this.sigma;
	}
	
	/**
	 * Setzt den Wert der Standardabweichung des Transformationsparameters
	 * @param sigma
	 */
	public void setStd(double sigma) {
		this.sigma = sigma;
	}
	
	/**
	 * Legt fest, ob Parameter signifikant ist
	 * @param significant
	 */
	public void setSignificant(boolean significant) {
		this.significant = significant;
	}
	
	/**
	 * Gibt Auskunft, ob Parameter signifikant ist
	 * @return significant
	 */
	public boolean isSignificant() {
		return this.significant;
	}
	
}
