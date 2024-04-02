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

package com.derletztekick.geodesy.bundleadjustment.v2.util;

import java.util.ArrayList;
import java.util.List;

import com.derletztekick.tools.geodesy.MathExtension;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.UnitUpperTriangBandMatrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;
public class DiagonalSymmetricBlockMatrix {
	private int row = 0;
	private List<Matrix> diagonalMatrixElements = null;
	private double sigma2apriori = 0.000001;

	public DiagonalSymmetricBlockMatrix(double sigma2apriori, int diag) {
		this.diagonalMatrixElements = new ArrayList<Matrix>(diag);
		this.sigma2apriori = sigma2apriori;
	}
	
	/**
	 * Anzahl der Zeilen der gesamten Matrix
	 * @return rows
	 */
	public int numRows() {
		return this.row;
	}
	
	/**
	 * Anzahl der Spalten der gesamten Marix
	 * @return cols
	 */
	public int numColumns() {
		return this.row;
	}
	
	/**
	 * Fuegt eine Sub-Matrix hinzu
	 * 
	 * @param matrix
	 * @throws IllegalArgumentException
	 */
	public void add(Matrix matrix) throws IllegalArgumentException {
		if (matrix instanceof UnitUpperTriangBandMatrix || matrix instanceof UpperSymmPackMatrix) {
			this.row += matrix.numRows();
			this.diagonalMatrixElements.add(matrix);
		}
		else {
			throw new IllegalArgumentException(this.getClass().getSimpleName()+" Fehler, Matrixtyp wird nicht unterstützt");
		}	
	}
		
	/**
	 * Fuegt eine Sub-Matrix hinzu. Bestimmt zunaechst die Inverse der Matrix
	 * 
	 * @param matrix
	 * @throws IllegalArgumentException
	 * @throws MatrixSingularException
	 */
	public void invAdd(Matrix matrix) throws IllegalArgumentException, MatrixSingularException {
		if (matrix instanceof UnitUpperTriangBandMatrix) {
			this.add(MathExtension.identity(matrix.numColumns()));
		}
		else if (matrix instanceof UpperSymmPackMatrix) {
			Matrix I = MathExtension.identity(matrix.numColumns());
			Matrix P = new DenseMatrix(matrix.numColumns(), matrix.numColumns());
			matrix.solve(I, P);
			this.add(new UpperSymmPackMatrix(P));
		}
		else {
			throw new IllegalArgumentException(this.getClass().getSimpleName()+" Fehler, Matrixtyp wird nicht unterstützt");
		}	
	}
	
	/**
	 * Fuegt eine Sub-Matrix hinzu. Bestimmt zunaechst die Pseudoinverse der Matrix
	 * 
	 * @param matrix
	 * @throws IllegalArgumentException
	 * @throws MatrixSingularException
	 */
	public void pinvAdd(Matrix matrix) throws IllegalArgumentException, NotConvergedException {
		if (matrix instanceof UnitUpperTriangBandMatrix) {
			this.add(MathExtension.identity(matrix.numColumns()));
		}
		else if (matrix instanceof UpperSymmPackMatrix) {
			this.add(new UpperSymmPackMatrix(MathExtension.pinv(matrix)));
		}
		else {
			throw new IllegalArgumentException(this.getClass().getSimpleName()+" Fehler, Matrixtyp wird nicht unterstützt");
		}	
	}
		
	/**
	 * Liefert das Element der Matrix an der Stelle row/column. Sollte es auf ein
	 * Nebendiagonalblockelement fallen, wird 0 (Null) ausgegeben
	 * 
	 * @param row
	 * @param column
	 * @return element
	 */
	public double get(int row, int column) {
		int len = 0;
		Matrix blockMatrix = this.diagonalMatrixElements.get(len);
		int indexOfMatrix = 0;
		for (int i=0; i<this.diagonalMatrixElements.size(); i++) {
			if (len <= column) {
				indexOfMatrix = i;
				blockMatrix = this.diagonalMatrixElements.get(indexOfMatrix);
				len += blockMatrix.numColumns();
			}
			else
				break;
		}
		
		int rowIndex = row    - len + blockMatrix.numRows();
		int colIndex = column - len + blockMatrix.numColumns();
		
		if (rowIndex >=0 && colIndex >=0 && rowIndex < blockMatrix.numRows() && colIndex < blockMatrix.numColumns()) {
			return blockMatrix.get(rowIndex, colIndex)*this.sigma2apriori;
		}
			
		return 0.0;
	}
	
	/**
	 * Liefert eine Sub-matrix
	 * 
	 * @param i
	 * @return submatrix
	 */
	public Matrix getSubMatrix(int i) {
		return this.diagonalMatrixElements.get(i);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<this.row; i++){
			for (int j=0; j<this.row; j++) {
				sb.append(this.get(i, j)).append("  ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
