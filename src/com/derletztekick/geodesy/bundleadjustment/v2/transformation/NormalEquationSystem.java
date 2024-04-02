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

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

public class NormalEquationSystem {
	private final Matrix N;
	private final Vector n;
	public NormalEquationSystem(Matrix N, Vector n) {
		this.N = N;
		this.n = n;
	}
	  
	/**
	 * Liefert die Normalgleichung 
	 * 
	 * N = A'*P*A  R'
	 *     R       0
	 * @return N
	 */
	public Matrix getNmatrix() {
		return this.N;
	}
	 
	/**
	 * Liefert den n-Vektor
	 * 
	 * n = A'*P*w
	 *     r
	 * 
	 * @return n
	 */
	public Vector getNvector() {
		return this.n;
	}
}
