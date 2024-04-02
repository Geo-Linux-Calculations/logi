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

import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;

public class UncertaintyRow {
	private final String name;
	private double value;
	private int distType = PreAnalysis.DISTRIBUTION_NORMAL;
	
	public UncertaintyRow(String name, double value, int distType) {
		this.name     = name;
		this.value    = value;
		this.distType = distType;
	}
	
	/**
	 * Liefert den num. Wert der Unsicherheit
	 * @return value
	 */
	public double getValue() {
		return this.value;
	}
	
	/**
	 * Setzt den num. Wert der Unsicherheit
	 * @param value
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * Liefert die Verteilung, die fuer diese Unsicherheit gilt
	 * @return distType
	 */
	public int getDistribution() {
		return this.distType;
	}
	
	/**
	 * Setzt die Verteilung, die fuer diese Unsicherheit gilt
	 * @param distType
	 */
	public void setDistribution(int distType) {
		this.distType = distType;
	}
	
	/**
	 * Liefert den Namen des Unsicherheitsparameters
	 * @return name
	 */
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + this.name + ": " + this.value;
	}
}
