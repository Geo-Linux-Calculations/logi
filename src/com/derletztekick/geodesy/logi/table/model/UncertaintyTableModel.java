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
import com.derletztekick.geodesy.logi.table.row.DistributionOption;
import com.derletztekick.geodesy.logi.table.row.UncertaintyRow;

public class UncertaintyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -3791293791899622947L;

	private final String columnNames[];
	private Map<Integer, DistributionOption> distributions;
	private TableRowHeader tableRowHeader;
	protected UncertaintyRow tableData[];
	
	public UncertaintyTableModel(String columnNames[], Map<Integer, DistributionOption> distributions, UncertaintyRow uncertaintyRows[]) {
		this.columnNames   = columnNames;
		this.distributions = distributions;
		this.tableData     = uncertaintyRows;
	}
	
	public UncertaintyRow[] getUncertaintyRows() {
		return this.tableData;
	}
	
	public void setUncertaintyRows(UncertaintyRow uncertaintyRows[]) {
		this.tableData = uncertaintyRows;
		this.fireTableDataChanged();
	}
	
	@Override
	public int getColumnCount() {
		// (Name,) Value, Dist.
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
		
		if (columnIndex == 0) {
			double d = Double.parseDouble(value.toString());
			if (d > 0)
				this.tableData[rowIndex].setValue( d );
		}
		else if (columnIndex == 1 && value instanceof DistributionOption) {
			this.tableData[rowIndex].setDistribution( ((DistributionOption)value).getType() );
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
			return this.tableData[rowIndex].getValue();
		else if (columnIndex == 1)
			return this.distributions.get(this.tableData[rowIndex].getDistribution());
		return null;
	}
	
	public Map<Integer, DistributionOption> getDistributions() {
		return this.distributions;
	}
	
	public int getDistributionCount() {
		return this.distributions.size();
	}
		
	@Override
	public String getColumnName(int columnIndex) {
		return this.columnNames[columnIndex];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return Double.class;
			default:
				return String.class;
		}
	}
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
    }
	
	public void setTableRowHeader(TableRowHeader tableRowHeader) {
		this.tableRowHeader = tableRowHeader;
		for (int i=0; i<this.tableData.length; i++) {
			this.tableRowHeader.setTableRowHead(i, this.tableData[i].getName());
		}
	}

}
