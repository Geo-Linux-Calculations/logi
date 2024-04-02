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

package com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PolarPointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;

public abstract class UncertaintyModel {
	private double diagCll[];

	public UncertaintyModel() {
		this.diagCll = this.getDefaultDiagCovar();
	}
	
	public UncertaintyModel(double diagCll[]) {
		this.diagCll = diagCll==null?this.getDefaultDiagCovar():diagCll;
	}
	
	/**
	 * Liefert den Parameterindex anhand seiner Abkuerzung
	 * @param abbr
	 * @return index
	 */
	public abstract int getIndexByAbbreviation(String abbr);
	
	/**
	 * Liefert einen Satz an (Default) Standardabweichungen
	 * @return sigmas
	 */
	public abstract double[] getDefaultSigmas();
		
	/**
	 * Liefert die Hauptdiagonale des a-priori 
	 * Varianz-Matrix
	 * @return diagCll
	 */
	public double[] getDiagCovar() {
		return this.diagCll;
	}

	/**
	 * Liefert <code>true</code>, wenn der Wert ein Winkel ist.
	 * @return isAngleValue
	 */
	public abstract boolean isAngle(int i);
	
	/**
	 * Liefert die Anzahl der festen (Instrument-bezogenen) Parameter
	 * @return numberOfFixedParameters
	 */
	public abstract int numberOfFixedParameters();

	/**
	 * Bestimmt das Element in der Jacobi-Matrix A, welches zur Bestimmung der
	 * Messunsicherheiten verwendet wird: A*Cll*A<sup>T</sup>
	 * 
	 * @param point
	 * @param column
	 * @param equationIndex
	 * @param pointNumber
	 * @return aij
	 */
	protected abstract double getJacobiElement(Point point, int column, int equationIndex, int pointNumber);

	/**
	 * Liefert die Hauptdiagonale der Default-Matrix Cll
	 * @return Cll
	 */
	private double[] getDefaultDiagCovar() {
		double diagCll[] = this.getDefaultSigmas();
		for (int i=0; i<diagCll.length; i++)
			diagCll[i] *= diagCll[i];
		return diagCll;
	}
	
	/**
	 * Bestimmt die Kovarianzmatrix der polaren Beobachtungen Cxx = A*Cll*A<sup>T</sup>
	 * 
	 * @return Cxx
	 */
	public Matrix deriveCovarianceMatrix(PolarPointGroup pointGroup) {
		int dim = pointGroup.getDimension();
		int paramLen = this.diagCll.length;
		
		int n = dim*pointGroup.size();
		Matrix Cxx = new UpperSymmPackMatrix(n);
		
		int columnsInJacobiMatrix = this.numberOfFixedParameters() + (paramLen-this.numberOfFixedParameters())*pointGroup.size();
		
		for (int i=0, rowN=0; i<pointGroup.size(); i++, rowN+=dim) {
			Point pointA = pointGroup.get(i);
			for (int equationIndexA=0; equationIndexA<dim; equationIndexA++) {
				//for (int j=0, colN=0; j<pointGroup.size(); j++, colN+=dim) {
				for (int j=i, colN=rowN; j<pointGroup.size(); j++, colN+=dim) {
					Point pointAT = pointGroup.get(j);
					for (int equationIndexAT=0; equationIndexAT<dim; equationIndexAT++) {
						double acllaT = 0;

						for (int column=0; column<columnsInJacobiMatrix; column++) {
							int col = column;
							if (column-this.numberOfFixedParameters() >= 0)
								col = this.numberOfFixedParameters()+(column-this.numberOfFixedParameters())%(paramLen-this.numberOfFixedParameters());
							
							double a   = this.getJacobiElement(pointA,  column, equationIndexA,  i);
							double aT  = this.getJacobiElement(pointAT, column, equationIndexAT, j);
							double cll = this.diagCll[col];
							acllaT += a*cll*aT;
						}

						Cxx.set(rowN+equationIndexA, colN+equationIndexAT, acllaT);
					}
				}
			}
		}
		return Cxx;
	}
	
	/**
	 * Erzeugt die Formmatrix F fuer Fehlerfortpflanzung --> Cxx = F*Cll*F<sup>T</sup>
	 * 
	 * @deprecated - nur zum Debuggen
	 * @return F
	 */
	public Matrix getJacobiMatrix(PolarPointGroup pointGroup) {
		int dim = pointGroup.getDimension();
		int paramLen = this.diagCll.length;
		
		int rowsInJacobiMatrix = dim*pointGroup.size();
		
		int columnsInJacobiMatrix = this.numberOfFixedParameters() + (paramLen-this.numberOfFixedParameters())*pointGroup.size();
		Matrix A = new DenseMatrix(rowsInJacobiMatrix, columnsInJacobiMatrix);
		
		
		for (int i=0, rowN=0; i<pointGroup.size(); i++, rowN+=dim) {
			Point pointA = pointGroup.get(i);
			for (int equationIndexA=0; equationIndexA<dim; equationIndexA++) {
				for (int column=0; column<columnsInJacobiMatrix; column++) {
					double a = this.getJacobiElement(pointA,  column, equationIndexA,  i);
					A.set(rowN+equationIndexA, column, a);
				}
			}
		}
		return A;
	}
}
