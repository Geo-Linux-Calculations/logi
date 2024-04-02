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

package com.derletztekick.geodesy.bundleadjustment.v2.unknown;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PointGroup;

public abstract class UnknownParameter {
	public final static int TYPE_SCALE         = 1,
    						TYPE_TRANSLATION_X = 2,
    						TYPE_TRANSLATION_Y = 3,	                        
    						TYPE_TRANSLATION_Z = 4,
    						TYPE_QUATERNION_Q0 = 5,
    						TYPE_QUATERNION_Q1 = 6,
    						TYPE_QUATERNION_Q2 = 7,	                        
    						TYPE_QUATERNION_Q3 = 8,
    						TYPE_POINT1D       = 9,
    						TYPE_POINT2D      = 10,
    						TYPE_POINT3D      = 11,
    						TYPE_ROTATION_X   = 12,
    						TYPE_ROTATION_Y   = 13,
    						TYPE_ROTATION_Z   = 14;
	private PointGroup group = null;

    // Spalte in Designmatrix; -1 entspricht nicht gesetzt 
	private int colInJacobiMatrix = -1;	
	
	/**
	 * Liefert den Parametertyp
	 * @return typ
	 */
	public abstract int getParameterTyp();
	
	/**
	 * Gibt die Spaltenposition
	 * in der Designmatrix A zurueck 
	 * @return col
	 */
	public int getColInJacobiMatrix() {
		return this.colInJacobiMatrix;
	}
	
	/**
	 * Legt die Spalte, in der die Beobachtung 
	 * in der JacobiMatrix steht, fest.
	 * @param col
	 */
	public void setColInJacobiMatrix(int col) {
		this.colInJacobiMatrix = col;
	}
	
	/**
	 * Setzt die Punktgruppe, zuder der Parameter gehoert
	 * @param group
	 */
	public void setPointGroup(PointGroup group) {
		this.group = group;
	}
	
	/**
	 * Liefert die Punktgruppe, zuder der Parameter gehoert
	 * @return group
	 */
	public PointGroup getPointGroup() {
		return this.group;
	}
}
