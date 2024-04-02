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

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import com.derletztekick.geodesy.logi.table.row.DataRow;
import com.derletztekick.geodesy.logi.table.row.PolarObservationRow;

public class PolarObservationTableModel extends DataTableModel {
	private static final long serialVersionUID = 6333252876087648561L;
	private Map<Integer, PolarObservationRow> tableData = new Hashtable<Integer, PolarObservationRow>();
	private Set<String> pointIds = new HashSet<String>();
	
	public PolarObservationTableModel(String columnNames[], int row) {
		super(columnNames, row);
	}
	
	public void setObservation(int rowIndex, PolarObservationRow obs) {
		if (this.pointIds.contains(obs.getEndPointId()))
			return;
		
		this.tableData.remove( rowIndex );
		if (obs != null)
			this.tableData.put(rowIndex, obs);
		if (rowIndex == this.getRowCount()-1)
			this.addRow();
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

		if ((value==null || value.toString().trim().isEmpty()) && columnIndex < this.getColumnCount()-2)
			return;

		PolarObservationRow obs = null;
		
		// Unnoetiges SQL-Update unterbinden
		Object oldValue = this.getValueAt(rowIndex, columnIndex);

		if (!this.tableData.containsKey(rowIndex)) {
			obs = new PolarObservationRow();
			this.tableData.put(rowIndex, obs);
		}
		else 
			obs = this.tableData.get(rowIndex);
		int curCol = 0;

		// enabled
		if (columnIndex == curCol++)
			obs.setEnable( value==null?false:Boolean.parseBoolean(value.toString()) );
		// Endpunktid
		else if (columnIndex == curCol++ && value != null && !value.toString().trim().isEmpty() && (obs.getEndPointId() == null || !obs.getEndPointId().equals(value))) {
			if (this.pointIds.contains(value.toString().trim())) {
				System.err.println(this.getClass().getSimpleName() + " Fehler, Punktnummer in Tabelle schon vorhanden! " + value.toString() );
				return;
			}
			// Entferne die alte Punktnummer, bevor sie ueberschrieben wird
			this.pointIds.remove(obs.getEndPointId());
			obs.setEndPointId( value==null?null:value.toString().trim() );
			this.pointIds.add( value.toString().trim() );
		}
		// Strecke
		else if (columnIndex == curCol++)
			obs.setDistance3D( value==null?null:Double.parseDouble(value.toString()) );
		// Azimut
		else if (columnIndex == curCol++)
			obs.setAzimuth( value==null?null:Double.parseDouble(value.toString()) );
		// Zenit
		else if (columnIndex == curCol++)
			obs.setZenith( value==null?null:Double.parseDouble(value.toString()) );
		// Datum/Zeit
		else if (columnIndex == curCol++) {
			obs.setObservationTime( value==null?null:(Date)value );
		}
		
		if (columnIndex != 0 && value != null && !obs.isEnabled()) {
			obs.setEnable(true);
			this.fireTableCellUpdated(rowIndex, 0);
		}
		// Unnoetiges SQL-Update unterbinden
		value = this.getValueAt(rowIndex, columnIndex);
		if ( (value == null && value != oldValue) || (value != null && !value.equals(oldValue))) {
			this.fireTableCellUpdated(rowIndex, columnIndex);
		}

		//if (this.getGroupTreeNode() != null && this.getColumnCount()>1 && this.isCellEditable(rowIndex, 1) && this.getRowSorter().convertRowIndexToView(rowIndex) == this.getRowCount()-1)
		if (this.getColumnCount()>1 && this.isCellEditable(rowIndex, 1) && rowIndex == this.getRowCount()-1)
			super.addRow();
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
		PolarObservationRow obs = this.tableData.get( rowIndex );
		if(obs == null)
			return null;
		int curCol = 0;
		if (colIndex == curCol++)
			return obs.isEnabled();
		else if (colIndex == curCol++)
			return obs.getEndPointId() == null || obs.getEndPointId().trim().isEmpty()?null:obs.getEndPointId();
		else if (colIndex == curCol++)
			return obs.getDistance3D();
		else if (colIndex == curCol++)
			return obs.getAzimuth();
		else if (colIndex == curCol++)
			return obs.getZenith();
		else if (colIndex == curCol++)
			return obs.getObservationTime();
		else if (colIndex == curCol++)
			return new ImageIcon(this.deleteIconURL,"x");
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
		// Punktnummer des Zielpunktes
		else if (columnIndex == 1)
			return String.class;
		// Datum
		else if (columnIndex == this.getColumnCount()-2)
			return Date.class;
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
	
	@Override
	public void setRowData(int rowIndex, DataRow data) {
		if (data instanceof PolarObservationRow) {
			this.setObservation(rowIndex, (PolarObservationRow) data);
		}
	}
	
	@Override
	public void setRowData(List<? extends DataRow> obs) {
		int rowIndex = Math.max(this.getRowCount()-1,0);
		int row = 0;
		for (int i=0; i<obs.size(); i++) {
			if (obs.get(i) instanceof PolarObservationRow) {
				PolarObservationRow o = (PolarObservationRow)obs.get(i);
				if (this.pointIds.contains(o.getEndPointId()))
					continue;
				this.tableData.put(rowIndex+row, o);
				this.pointIds.add(o.getEndPointId());
				row++;
			}
		}
		this.setRowCount(rowIndex + row + 1);
	}
	
	@Override
	public void removeAll() {
		this.pointIds.clear();
		this.tableData.clear();
		this.setRowCount(1);
	}
	
	@Override
	public void removeRow(int rowIndex) {
		PolarObservationRow obs = this.tableData.get(rowIndex);
		if (obs != null) {
			super.fireTableRowsDeleted(rowIndex, rowIndex);
//			this.pointIds.remove(obs.getEndPointId());
//			this.tableData.remove(rowIndex);
			
		}
	}
}
