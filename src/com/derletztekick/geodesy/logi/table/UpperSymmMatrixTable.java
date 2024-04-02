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

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import com.derletztekick.geodesy.logi.table.cellrenderer.CrossColoredTableCellRenderer;
import com.derletztekick.geodesy.logi.table.model.UpperSymmMatrixTableModel;

public class UpperSymmMatrixTable extends JTable {
	private static final long serialVersionUID = -7649761019906642726L;
	private final UpperSymmMatrixTableModel tableModel;
	
	private final String name;
	
	public UpperSymmMatrixTable(String name, UpperSymmMatrixTableModel tableModel) {
		this.name = name;
		this.tableModel = tableModel;
		super.setModel(this.tableModel);
        super.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ); 
        super.setRowSelectionAllowed( true ); 
        
        this.setDefaultRenderer(Object.class, new CrossColoredTableCellRenderer(this.tableModel.getIncrement()));
        this.setDefaultRenderer(Double.class, new CrossColoredTableCellRenderer(this.tableModel.getIncrement()));
        
		this.getTableHeader().setReorderingAllowed(false);
		
		TableColumn tc = this.getColumnModel().getColumn(0);
		tc.setMaxWidth(tc.getPreferredWidth());
		tc.setMinWidth(tc.getPreferredWidth());
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
	}
	
	public void setTableRowHeader(JScrollPane pane) {
		new TableRowHeader(this, pane, 120, true);
		//this.tableModel.setTableRowHeader( new TableRowHeader(this, pane));
	}
	
	public UpperSymmMatrixTableModel getModel() {
		return this.tableModel;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
