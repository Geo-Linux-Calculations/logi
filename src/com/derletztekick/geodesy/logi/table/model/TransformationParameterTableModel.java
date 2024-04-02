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

import javax.swing.table.AbstractTableModel;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet;
import com.derletztekick.geodesy.logi.table.TableRowHeader;

public class TransformationParameterTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -2013165525669983439L;
	private TableRowHeader tableRowHeader;
	private int row = 0;
	private TransformationParameterSet trafoParamSet;
	private final String columnHeader[], rowHeader[];
	public TransformationParameterTableModel(String columnHeader[], String rowHeader[], TransformationParameterSet trafoParamSet) {
		this.trafoParamSet = trafoParamSet;
		this.columnHeader = columnHeader;
		this.rowHeader = rowHeader;
		this.row = this.trafoParamSet.numberOfTransformationParameters() + this.trafoParamSet.numberOfAdditionalTransformationParameters();
	}
	
	/**
	 * Setzt ein neues Trafo-Set
	 * @param trafoParamSet
	 */
	public void setTransformationParameterSet(TransformationParameterSet trafoParamSet) {
		this.trafoParamSet = trafoParamSet;
		this.fireTableDataChanged();
	}
	
	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return this.row;
	}

	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		if (rowIndex < this.trafoParamSet.numberOfTransformationParameters()) {
			TransformationParameter param[] = trafoParamSet.getTransformationParameters();
			if (colIndex == 0)
				return param[rowIndex].getValue();
			else if (colIndex == 1)
				return param[rowIndex].getStd();
			else if (colIndex == 2)
				return param[rowIndex].isSignificant();
		}
		else if (rowIndex < this.trafoParamSet.numberOfTransformationParameters()+this.trafoParamSet.numberOfAdditionalTransformationParameters()) {
			TransformationParameter param[] = trafoParamSet.getAdditionalTransformationParameters();
			if (colIndex == 0)
				return param[rowIndex-this.trafoParamSet.numberOfTransformationParameters()].getValue();
			else if (colIndex == 1)
				return param[rowIndex-this.trafoParamSet.numberOfTransformationParameters()].getStd();
			else if (colIndex == 2)
				return param[rowIndex-this.trafoParamSet.numberOfTransformationParameters()].isSignificant();
		}
		return null;
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		return this.columnHeader[columnIndex];
	}
	
	/**
	 * Liefert die Klasse der Spalte
	 * @param columnIndex
	 * @return columnClass
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// Datensatz signifikant
		if (columnIndex == this.getColumnCount()-1)
			return Boolean.class;
		return Double.class;
	}
	
	public void setTableRowHeader(TableRowHeader tableRowHeader) {
		this.tableRowHeader = tableRowHeader;
		for (int i=0; i<this.rowHeader.length; i++) {
			this.tableRowHeader.setTableRowHead(i, this.rowHeader[i]);
		}
	}

}
