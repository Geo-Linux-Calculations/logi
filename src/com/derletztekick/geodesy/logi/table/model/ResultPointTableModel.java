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

import java.util.List;

import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.derletztekick.geodesy.logi.table.comparator.AbsoluteValueComparator;
import com.derletztekick.geodesy.logi.table.row.DataRow;
import com.derletztekick.geodesy.logi.table.row.PointRow;

public class ResultPointTableModel extends RawPointTableModel {
	private static final long serialVersionUID = 6412211754566359420L;
	private TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(this);
	public ResultPointTableModel(String columnNames[], int row) {
		super(columnNames, row, null);
		this.setColumnComparator();
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {}
		
	/**
	 * Liefert den Wert der Tabelle
	 * @param rowIndex
	 * @param colIndex
	 * @return value
	 */
	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		if (colIndex<0)
			return null;
		PointRow point = this.tableData.get( rowIndex );
		if(point == null)
			return null;
		int curCol = 0;
		if (colIndex == curCol++)
			return point.getPointId() == null || point.getPointId().trim().isEmpty()?null:point.getPointId();
		else if (colIndex == curCol++)
			return point.getX();
		else if (colIndex == curCol++)
			return point.getY();
		else if (colIndex == curCol++)
			return point.getZ();
		else if (colIndex == curCol++)
			return point.getStdX();
		else if (colIndex == curCol++)
			return point.getStdY();
		else if (colIndex == curCol++)
			return point.getStdZ();
		else if (colIndex == curCol++)
			return point.getErrorX();
		else if (colIndex == curCol++)
			return point.getErrorY();
		else if (colIndex == curCol++)
			return point.getErrorZ();
		else if (colIndex == curCol++)
			return point.getRedundanceX();
		else if (colIndex == curCol++)
			return point.getRedundanceY();
		else if (colIndex == curCol++)
			return point.getRedundanceZ();
		else if (colIndex == curCol++)
			return point.getNablaX();
		else if (colIndex == curCol++)
			return point.getNablaY();
		else if (colIndex == curCol++)
			return point.getNablaZ();
		else if (colIndex == curCol++)
			return point.getOmega();
		else if (colIndex == curCol++)
			return point.getTprio();
		else if (colIndex == curCol++)
			return point.getTpost();
		else if (colIndex == curCol++)
			return point.isOutlier();

		return null;
    }
	
	@Override
	public TableRowSorter<TableModel> getRowSorter() {
		return this.rowSorter;
	}
	
	/**
	 * Fuegt den AbsoluteValueComparator
	 * jeder nummerischen Spalte hinzu
	 *
	 */
	private void setColumnComparator() {
		// Die Brechstange, da sich beim Wechsel der Tabellen
		// der Sorter nicht mehr den AbsoluteValueComparator nutzt!?
		this.rowSorter.addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent evt) {
				for (int i=4; i<getColumnCount(); i++) {
					rowSorter.setComparator( i, new AbsoluteValueComparator() );
				}
			}
		});
//		for (int i=0; i<getColumnCount(); i++) {
//			rowSorter.setComparator( i, new AbsoluteValueComparator() );
//		}
	}
	
	@Override
	public void setRowData(List<? extends DataRow> points) {
		super.setRowData(points);
		// Korrigiere zusaetzliche Zeile
		this.setRowCount(this.getRowCount() - 1);
	}
	
	/**
	 * Liefert die Klasse der Spalte
	 * @param columnIndex
	 * @return columnClass
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// Ausreißer
		if (this.getColumnCount() > 7 && columnIndex == this.getColumnCount()-1)
			return Boolean.class;
		// Punktnummer
		else if (columnIndex == 0)
			return String.class;
		return Double.class;
	}
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
    }
}
