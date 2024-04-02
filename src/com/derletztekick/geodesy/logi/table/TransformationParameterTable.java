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

import com.derletztekick.geodesy.logi.table.cellrenderer.ColoredTableCellRenderer;
import com.derletztekick.geodesy.logi.table.cellrenderer.FractionCellRenderer;
import com.derletztekick.geodesy.logi.table.cellrenderer.NumberCellRenderer;
import com.derletztekick.geodesy.logi.table.model.TransformationParameterTableModel;

public class TransformationParameterTable extends JTable {
	private static final long serialVersionUID = 8610294504181349236L;
	private final TransformationParameterTableModel tableModel;
	
	private final String name;
	
	public TransformationParameterTable(String name, TransformationParameterTableModel tableModel) {
		this.name = name;
		this.tableModel = tableModel;
		super.setModel(this.tableModel);
        super.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ); 
        super.setRowSelectionAllowed( true ); 
        
        this.setDefaultRenderer(Object.class,  new ColoredTableCellRenderer());
		this.setDefaultRenderer(Boolean.class, new ColoredTableCellRenderer());
		this.setDefaultRenderer(Number.class,  new NumberCellRenderer());
		this.setDefaultRenderer(Double.class,  new FractionCellRenderer(-1, 8, FractionCellRenderer.RIGHT));
		this.getTableHeader().setReorderingAllowed(false);		
	}
	
	public void setTableRowHeader(JScrollPane pane) {
		this.tableModel.setTableRowHeader( new TableRowHeader(this, pane));
	}
	
	public TransformationParameterTableModel getModel() {
		return this.tableModel;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
