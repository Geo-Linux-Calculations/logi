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

import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.derletztekick.geodesy.logi.table.row.DataRow;

public abstract class DataTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1740368391098549791L;
	private int row=0;
	private final String columnNames[];
	protected final URL deleteIconURL = this.getClass().getResource("/com/derletztekick/geodesy/logi/gfx/cross-small.png");
	public DataTableModel(String columnNames[], int row) {
		this.columnNames = columnNames;
		this.row = row;
	}
	
	/**
	 * Liefert den gesamten Datensatz der Zeile
	 * @param rowIndex
	 * @return data
	 */
	public abstract DataRow getRowData(int rowIndex);

	/**
	 * Entfernt alle Eintraege aus der Tabelle,
	 */
	public abstract void removeAll();
	
	/**
	 * Loescht die gesamte Zeile
	 * @param rowIndex
	 */
	public abstract void removeRow(int rowIndex);
	
	/**
	 * Entfernt alle Eintraege aus der Tabelle. Ruft hierzu die Methode
	 * removeAll() auf und loest anschlieﬂend ein TableModelEvent via 
	 * fireTableCleared() aus.
	 */
	public void clearAll() {
		this.removeAll();
		this.fireTableCleared();
	}
	
	/**
	 * Methode, die ein TableModelEvent ausloest wenn alle Daten
	 * aus der Tabelle geloescht werden.
	 */
	public void fireTableCleared() {
		// http://openbook.galileocomputing.de/javainsel/javainsel_19_019.htm
		this.fireTableChanged(new TableModelEvent(this, -1, -1, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
	}
	
	/**
	 * Setzt einen Datensatz in die Tabelle
	 * @param rowIndex
	 * @param data
	 */
	public abstract void setRowData(int rowIndex, DataRow data);
	
	/**
	 * Setzt eine Liste mit Datensaetze in die Tabelle
	 * @param data
	 */
	public abstract void setRowData(List<? extends DataRow> data);
	
	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}
	
	/**
	 * Setzt die Anzahl der dargestellten Zeilen
	 * @param rowIndex
	 */
	public void setRowCount(int rowIndex) {
		this.row = rowIndex;
		super.fireTableStructureChanged();
	}
	
	/**
	 * Liefert die Anzahl der dargestellten Zeilen
	 * @return row
	 */
	@Override
	public int getRowCount() {
		return this.row;
	}
	
	/**
	 * Fuegt eine leere Zeile an die
	 * Tabelle an.
	 */
	public void addRow(){
		this.row++;
		super.fireTableStructureChanged();
	}
	
	/**
	 * Gibt die Spaltenkopfbezeichnung der Spalte <code>columnIndex</code> zurueck
	 * @param columnIndex
	 * @return columnName
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return this.columnNames[columnIndex];
	}
	
	/**
	 * Liefer <code>true</code>, wenn Zelle editierbar ist
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return isCellEditable
	 */
	@Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
    }
	
	/**
	 * Liefert den TableRowSorter vom Modell
	 * @return rowSorter
	 */
	public TableRowSorter<TableModel> getRowSorter(){
		return null;
	}

	public void fireTableCellUpdated(int rowIndex, int columnIndex) {
		if (!this.getColumnClass(columnIndex).equals(ImageIcon.class))
			super.fireTableCellUpdated(rowIndex, columnIndex);
	}
}
