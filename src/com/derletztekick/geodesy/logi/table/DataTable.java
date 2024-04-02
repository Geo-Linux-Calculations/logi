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

package com.derletztekick.geodesy.logi.table;

import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.derletztekick.geodesy.logi.table.celleditor.ButtonEditor;

import com.derletztekick.geodesy.logi.table.celleditor.DateTimeEditor;
import com.derletztekick.geodesy.logi.table.cellrenderer.ColoredTableCellRenderer;
import com.derletztekick.geodesy.logi.table.cellrenderer.DateCellRenderer;
import com.derletztekick.geodesy.logi.table.cellrenderer.FractionCellRenderer;
import com.derletztekick.geodesy.logi.table.cellrenderer.NumberCellRenderer;
import com.derletztekick.geodesy.logi.table.model.DataTableModel;
import com.derletztekick.geodesy.logi.table.row.DataRow;


public class DataTable extends JTable {

	private FractionCellRenderer cellRenderer = new FractionCellRenderer(-1, 6, FractionCellRenderer.RIGHT);
	private static final long serialVersionUID = 8343447601622074672L;
	private final DataTableModel tableModel;
	private final String name;
	
	public DataTable(String name, DataTableModel tableModel) {
		this.name = name;
		this.tableModel = tableModel;
		super.setModel(this.tableModel);
		super.setRowSorter( this.tableModel.getRowSorter() );
		
        super.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ); 
        super.setRowSelectionAllowed( true );

        this.setDefaultEditor(ImageIcon.class,    new ButtonEditor());
		this.setDefaultRenderer(ImageIcon.class,  new ColoredTableCellRenderer());
		this.setDefaultRenderer(Date.class,       new DateCellRenderer());
		this.setDefaultRenderer(Object.class,     new ColoredTableCellRenderer());
		this.setDefaultRenderer(Boolean.class,    new ColoredTableCellRenderer());
		this.setDefaultRenderer(Number.class,     new NumberCellRenderer());
		this.setDefaultRenderer(Double.class,     this.cellRenderer);
		
		// this.setDefaultEditor(Date.class, new DateEditor());
		this.setDefaultEditor(Date.class, new DateTimeEditor());
		 
		this.getTableHeader().setReorderingAllowed(false);
	}
	
	public DataRow getRowData(int rowIndex) {
		return this.tableModel.getRowData(rowIndex);
	}
	
	public void setRowData(int rowIndex, DataRow data) {
		this.tableModel.setRowData(rowIndex, data);
	}
	
	public void setRowData(List<? extends DataRow> data) {
		this.tableModel.setRowData(data);
	}
	
	public DataTableModel getModel() {
		return this.tableModel;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
