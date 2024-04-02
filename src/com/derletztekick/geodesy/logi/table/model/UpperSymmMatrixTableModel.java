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

package com.derletztekick.geodesy.logi.table.model;

import java.util.Hashtable;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public class UpperSymmMatrixTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -368343669811970160L;
	private Map<String, Double> tableData = new Hashtable<String, Double>();
	private int size = 0;
	private int dim = 3;
	private final double defaulValue = 2.5E-7; // Defaultgenauigkeit 0.0005m
	public UpperSymmMatrixTableModel(int dim, int size) {
		this.size = size*dim;
		this.dim = dim;
	}

	@Override
	public int getColumnCount() {
		return this.size;
	}

	@Override
	public int getRowCount() {
		return this.size;
	}
	
	public void setSize(int size) {
		if (size < 0)
			this.size = 0;
		else
			this.size = size*this.dim;
		
		for (int i=0; i<this.size; i++) {
			String key = "[" + i + ", " + i + "]";
			this.tableData.put(key, this.defaulValue); 
		}
		
		super.fireTableStructureChanged();

	}
	
	/**
	 * Fuegt leere Zeilen an die
	 * Tabelle an.
	 */
	public void addRow(){
		for (int i=0; i<this.dim; i++) {
			String key = "[" + (this.size+i) + ", " + (this.size+i) + "]";
			this.tableData.put(key, this.defaulValue);
			super.fireTableCellUpdated(this.size+i, this.size+i);
		}
		this.size += this.dim;
		super.fireTableStructureChanged();
	}
	
	public int getIncrement() {
		return this.dim;
	}
	
	public void setMatrix(Matrix M) {
		int rows = Math.min(M.numColumns(), this.size);
		for (int rowIndex=0; rowIndex<rows; rowIndex++) {
			for (int colIndex=rowIndex; colIndex<rows; colIndex++) {
				String key = "[" + rowIndex + ", " + colIndex + "]";
				this.tableData.put(key, M.get(rowIndex, colIndex));
			}
		}
	}
	
	public Matrix getMatrix() {
		Matrix C = new UpperSymmPackMatrix(this.size);
		for (int rowIndex=0; rowIndex<this.size; rowIndex++) {
			for (int colIndex=rowIndex; colIndex<this.size; colIndex++) {
				Object value = this.getValueAt(rowIndex, colIndex);
				double d = 0;
				if (value != null && (value instanceof Double))
					d = (Double)value;

				C.set(rowIndex, colIndex, d);
			}
		}
		return C;
	}

	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		int min = Math.min(rowIndex, colIndex);
		int max = Math.max(rowIndex, colIndex);
		String key = "[" + min + ", " + max + "]";
		return this.tableData.get(key);
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int colIndex) {
		int min = Math.min(rowIndex, colIndex);
		int max = Math.max(rowIndex, colIndex);
		String key = "[" + min + ", " + max + "]";
		
		if (value == null || !(value instanceof Double))
			this.tableData.remove( key );
		else {
			double d = (Double)value;
			this.tableData.put(key, d);
			if (rowIndex >= colIndex)
				super.fireTableCellUpdated(min, max);
			else
				super.fireTableCellUpdated(max, min);	
		}
	}
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
    }
	
	/**
	 * Liefert die Klasse der Spalte
	 * @param columnIndex
	 * @return columnClass
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Double.class;
	}
	
	/**
	 * Gibt die Spaltenkopfbezeichnung der Spalte <code>columnIndex</code> zurueck
	 * @param columnIndex
	 * @return columnName
	 */
	@Override
	public String getColumnName(int columnIndex) {
		int index = columnIndex/this.dim + 1;
	
		if (columnIndex % this.dim == 0)
			return "<html>X<small>" + index + "</small></html>";
		else if ((columnIndex-1) % this.dim == 0)
			return "<html>Y<small>" + index + "</small></html>";
		else if ((columnIndex-2) % this.dim == 0)
			return "<html>Z<small>" + index + "</small></html>";
		
		return null;
	}
	
	public void removeAll() {
		this.tableData.clear();
		this.size = 0;
	}
	
}
