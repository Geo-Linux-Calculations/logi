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

package com.derletztekick.geodesy.logi.sql;

public class BequeathOption {
	public static final int TRANSFORMATION_PARAMETERS = 1,
							UNCERTAINTIES = 2,
							TRANSFORMATION_PARAMETERS_AND_UNCERTAINTIES = 12;
	private String name;
	private int type;
	
	public BequeathOption(String name, int type) {
		this.name = name;
		this.type = type;
	}
	
	/**
	 * Liefert den Namen der Option
	 * @return name
	 */
	public String toString() {
		return this.name;
	}
	
	/**
	 * Liefert den Typ der Option
	 * @return typ
	 */
	public int getType() {
		return this.type;
	}
}
