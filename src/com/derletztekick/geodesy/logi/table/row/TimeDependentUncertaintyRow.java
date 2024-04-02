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

package com.derletztekick.geodesy.logi.table.row;

public class TimeDependentUncertaintyRow {
	private double a,b,c,maxDT,sigma;
	private int funcType;
	private final int obsType;
	public TimeDependentUncertaintyRow(int funcType, int obsType, double sigma, double maxDT, double a, double b, double c) {
		this.funcType = funcType;
		this.obsType  = obsType;
		this.sigma    = sigma;
		this.maxDT    = maxDT;
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	/**
	 * Liefert den Parameter A
	 * @return a
	 */
	public double getCoefA() {
		return this.a;
	}
	
	/**
	 * Liefert den Parameter B
	 * @return b
	 */
	public double getCoefB() {
		return this.b;
	}
	
	/**
	 * Liefert den Parameter C
	 * @return c
	 */
	public double getCoefC() {
		return this.c;
	}
	
	/**
	 * Maximale Zeitdifferenz
	 * @return maxDT
	 */
	public double getMaximalTimeDifference() {
		return this.maxDT;
	}
	
	/**
	 * Unsicherheit der Funktion
	 * @return maxDT
	 */
	public double getUncertainty() {
		return this.sigma;
	}
	
	/**
	 * Liefert die Korrelationsfunktion
	 * @return funcType
	 */
	public int getCorrelationFunctionType() {
		return this.funcType;
	}
	
	/**
	 * Liefert den Beobachtungstyp
	 * @return funcType
	 */
	public int getObservationType() {
		return this.obsType;
	}
	
	
	/**
	 * Set den Parameter A
	 * @param a
	 */
	public void setCoefA(double a) {
		this.a = a;
	}
	
	/**
	 * Set den Parameter B
	 * @param b
	 */
	public void setCoefB(double b) {
		this.b = b;
	}
	
	/**
	 * Set den Parameter C
	 * @param c
	 */
	public void setCoefC(double c) {
		this.c = c;
	}
	
	/**
	 * Maximale Zeitdifferenz
	 * @param maxDT
	 */
	public void setMaximalTimeDifference(double maxDT) {
		this.maxDT = maxDT;
	}
	
	/**
	 * Unsicherheit der Funktion
	 * @param s
	 */
	public void setUncertainty(double s) {
		this.sigma = s;
	}
	
	/**
	 * Setzt die Korrelationsfunktion
	 * @param funcType
	 */
	public void setCorrelationFunctionType(int funcType) {
		this.funcType = funcType;
	}
	
}
