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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import com.derletztekick.geodesy.logi.table.row.DataRow;
import com.derletztekick.geodesy.logi.table.row.PointRow;


public class RawPointTableModel extends DataTableModel {
	private static final long serialVersionUID = -9002371735502419183L;
	protected Map<Integer, PointRow> tableData = new Hashtable<Integer, PointRow>();
	private Set<String> pointIds = new HashSet<String>();
	private UpperSymmMatrixTableModel upperSymmMatrixTableModel;

	public RawPointTableModel(String columnNames[], int row, UpperSymmMatrixTableModel upperSymmMatrixTableModel) {
		super(columnNames, row);
		this.upperSymmMatrixTableModel = upperSymmMatrixTableModel;
	}
	
	public void addRow() {
		super.addRow();
		if (this.upperSymmMatrixTableModel != null)
			this.upperSymmMatrixTableModel.addRow();
	}
	
	/**
	 * Setzt einen Wert an die uebergebene Position in der Tabelle
	 * @param value
	 * @param rowIndex
	 * @param colIndex
	 */
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		// Loesche Datensatz
		if (columnIndex == this.getColumnCount()-1) {
			this.removeRow(rowIndex);
			return;
		}

		PointRow point = null;

		if (value==null || value.toString().trim().isEmpty())
			return;
		
		// Unnoetiges SQL-Update unterbinden
		Object oldValue = this.getValueAt(rowIndex, columnIndex);

		if (!this.tableData.containsKey(rowIndex)) {
			point = new PointRow();
			this.tableData.put(rowIndex, point);
		}
		else 
			point = this.tableData.get(rowIndex);
				
		int curCol = 0;
		// enabled
		if (columnIndex == curCol++)
			point.setEnable( value==null?false:Boolean.parseBoolean(value.toString()) );
		// Punkt-ID
		else if (columnIndex == curCol++) {
			if (this.pointIds.contains(value.toString())) {
				System.err.println(this.getClass().getSimpleName() + " Fehler, Punktnummer in Tabelle schon vorhanden! " + value.toString() );
				return;
			}
			// Entferne die alte Punktnummer, bevor sie ueberschrieben wird
			this.pointIds.remove(point.getPointId());
			point.setPointId( value.toString() );
			this.pointIds.add( value.toString() );
		}
		// X
		else if (columnIndex == curCol++)
			point.setX( value==null?null:Double.parseDouble(value.toString()) );
		// Y
		else if (columnIndex == curCol++)
			point.setY( value==null?null:Double.parseDouble(value.toString()) );
		// Z
		else if (columnIndex == curCol++)
			point.setZ( value==null?null:Double.parseDouble(value.toString()) );
		
		if (columnIndex != 0 && value != null && !point.isEnabled()) {
			point.setEnable(true);
			this.fireTableCellUpdated(rowIndex, 0);
		}
		// Unnoetiges SQL-Update unterbinden
		value = this.getValueAt(rowIndex, columnIndex);
		if ( (value == null && value != oldValue) || (value != null && !value.equals(oldValue))) {
			this.fireTableCellUpdated(rowIndex, columnIndex);
		}

		//if (this.getGroupTreeNode() != null && this.getColumnCount()>1 && this.isCellEditable(rowIndex, 1) && this.getRowSorter().convertRowIndexToView(rowIndex) == this.getRowCount()-1)
		if (this.getColumnCount()>1 && this.isCellEditable(rowIndex, 1) && rowIndex == this.getRowCount()-1)
			this.addRow();
	}
		
	public void setRowCount(int rows) {
		super.setRowCount(rows);
		// Lade nur die tatsaechliche Anzahl an Zeilen/Spalten
		if (this.upperSymmMatrixTableModel != null)
			this.upperSymmMatrixTableModel.setSize(rows-1);
	}
	
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
			return point.isEnabled();
		else if (colIndex == curCol++)
			return point.getPointId() == null || point.getPointId().trim().isEmpty()?null:point.getPointId();
		else if (colIndex == curCol++)
			return point.getX();
		else if (colIndex == curCol++)
			return point.getY();
		else if (colIndex == curCol++)
			return point.getZ();
		else if (colIndex == curCol++)
			return new ImageIcon(this.deleteIconURL, "x");
		
		return null;
    }
	
	/**
	 * Liefert die Klasse der Spalte
	 * @param columnIndex
	 * @return columnClass
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// Datensatz aktiv
		if (columnIndex == 0)
			return Boolean.class;
		// Punktnummer
		else if (columnIndex == 1)
			return String.class;
		// Loeschicon
		else if (columnIndex == this.getColumnCount()-1)
			return ImageIcon.class;
		return Double.class;
	}
	
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
    }
	
	@Override
	public DataRow getRowData(int rowIndex) {
		return this.tableData.get(rowIndex);
	}
	
	/**
	 * Setzt einen Punkt in die angebene Zeile
	 * @param rowIndex
	 * @param point
	 */
	public void setPoint(int rowIndex, PointRow point) {
		if (this.pointIds.contains(point.getPointId()))
			return;

		this.tableData.remove( rowIndex );
		if (point != null)
			this.tableData.put(rowIndex, point);
		if (rowIndex == this.getRowCount()-1)
			this.addRow();
	}
	
	@Override
	public void setRowData(int rowIndex, DataRow data) {
		if (data instanceof PointRow) {
			this.setPoint(rowIndex, (PointRow) data);
		}
	}
	
	@Override
	public void setRowData(List<? extends DataRow> points) {
		int rowIndex = Math.max(this.getRowCount()-1,0);
		int row = 0;
		for (int i=0; i<points.size(); i++) {
			if (points.get(i) instanceof PointRow) {
				PointRow point = (PointRow)points.get(i);
				if (this.pointIds.contains(point.getPointId()))
					continue;
				this.tableData.put(rowIndex+row, point);
				this.pointIds.add(point.getPointId());
				row++;
			}
		}
		this.setRowCount(rowIndex + row + 1);
	}
	
	@Override
	public void removeAll() {
		this.pointIds.clear();
		this.tableData.clear();
		if (this.upperSymmMatrixTableModel != null)
			this.upperSymmMatrixTableModel.removeAll();
		this.setRowCount(1);
	}

	@Override
	public void removeRow(int rowIndex) {
		PointRow point = this.tableData.get(rowIndex);
		if (point != null) {
			super.fireTableRowsDeleted(rowIndex, rowIndex);
//			this.pointIds.remove(point.getPointId());
//			this.tableData.remove(rowIndex);
		}
	}
}
