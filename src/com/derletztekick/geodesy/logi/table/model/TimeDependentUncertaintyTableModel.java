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

import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.derletztekick.geodesy.logi.table.TableRowHeader;
import com.derletztekick.geodesy.logi.table.row.CorrelationFunctionOption;
import com.derletztekick.geodesy.logi.table.row.TimeDependentUncertaintyRow;

public class TimeDependentUncertaintyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -3791293791899622947L;

	private final String columnNames[], rowNames[];
	private TableRowHeader tableRowHeader;
	private TimeDependentUncertaintyRow tableData[];
	private Map<Integer, CorrelationFunctionOption> corrFuncName;
	public TimeDependentUncertaintyTableModel(String columnNames[], String rowNames[], Map<Integer, CorrelationFunctionOption> corrFuncName,TimeDependentUncertaintyRow distRow, TimeDependentUncertaintyRow azimuthRow, TimeDependentUncertaintyRow zenitRow) {
		this.columnNames = columnNames;
		this.rowNames    = rowNames;
		this.tableData   = new TimeDependentUncertaintyRow[] {
				distRow, azimuthRow, zenitRow
		};
		this.corrFuncName = corrFuncName;
	}
	
	public TimeDependentUncertaintyRow[] getTimeDependentUncertaintyRows() {
		return this.tableData;
	}
	
	public void setUncertaintyRows(TimeDependentUncertaintyRow distRow, TimeDependentUncertaintyRow azimuthRow, TimeDependentUncertaintyRow zenitRow) {
		this.tableData     = new TimeDependentUncertaintyRow[] {
				distRow, azimuthRow, zenitRow
		};
		this.fireTableDataChanged();
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return this.tableData.length;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if (value == null || value.toString().trim().isEmpty())
			return;
		
		// Unnoetiges SQL-Update unterbinden
		Object oldValue = this.getValueAt(rowIndex, columnIndex);
		
		if (columnIndex == 0 && value instanceof CorrelationFunctionOption) {
			int type = ((CorrelationFunctionOption)value).getType();
			if (this.corrFuncName.containsKey(type))
				this.tableData[rowIndex].setCorrelationFunctionType(type);
		}
		else if (columnIndex > 0) {
			double d = Double.parseDouble(value.toString());
			if (columnIndex == 1) 
				this.tableData[rowIndex].setUncertainty(Math.abs(d));
			else if (columnIndex == 2) 
				this.tableData[rowIndex].setCoefA(Math.abs(d));
			else if (columnIndex == 3) 
				this.tableData[rowIndex].setCoefB(Math.abs(d));
			else if (columnIndex == 4) 
				this.tableData[rowIndex].setCoefC(Math.abs(d));
			else if (columnIndex == 5) 
				this.tableData[rowIndex].setMaximalTimeDifference(Math.abs(d));
		}
		
		// Unnoetiges SQL-Update unterbinden
		value = this.getValueAt(rowIndex, columnIndex);
		if ( (value == null && value != oldValue) || (value != null && !value.equals(oldValue))) {
			this.fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) 
			return this.corrFuncName.get(this.tableData[rowIndex].getCorrelationFunctionType());
		else if (columnIndex == 1) 
			return this.tableData[rowIndex].getUncertainty();
		else if (columnIndex == 2) 
			return this.tableData[rowIndex].getCoefA();
		else if (columnIndex == 3) 
			return this.tableData[rowIndex].getCoefB();
		else if (columnIndex == 4) 
			return this.tableData[rowIndex].getCoefC();
		else if (columnIndex == 5) 
			return this.tableData[rowIndex].getMaximalTimeDifference();
		return null;
	}
		
	@Override
	public String getColumnName(int columnIndex) {
		return this.columnNames[columnIndex];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return String.class;
			default:
				return Double.class;
		}
	}
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
    }
	
	public void setTableRowHeader(TableRowHeader tableRowHeader) {
		this.tableRowHeader = tableRowHeader;
		for (int i=0; i<this.tableData.length; i++) {
			this.tableRowHeader.setTableRowHead(i, this.rowNames[i]);
		}
	}
	
	public Map<Integer, CorrelationFunctionOption> getCorrelationFunction() {
		return this.corrFuncName;
	}
}
