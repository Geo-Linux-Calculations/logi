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

import java.awt.Component;
import java.util.EventObject;
import java.util.Locale;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import com.derletztekick.geodesy.logi.table.cellrenderer.ColoredTableCellRenderer;
import com.derletztekick.geodesy.logi.table.cellrenderer.FractionCellRenderer;
import com.derletztekick.geodesy.logi.table.cellrenderer.NumberCellRenderer;
import com.derletztekick.geodesy.logi.table.model.TimeDependentUncertaintyTableModel;
import com.derletztekick.geodesy.logi.table.row.CorrelationFunctionOption;

public class TimeDependentUncertaintyTable extends JTable {

	private static final long serialVersionUID = 8231062097457444561L;
	private FractionCellRenderer cellRenderer = new FractionCellRenderer(-1, 4, 8, FractionCellRenderer.RIGHT);
	private final TimeDependentUncertaintyTableModel tableModel;
	
	public TimeDependentUncertaintyTable(TimeDependentUncertaintyTableModel tableModel) {
		this.tableModel = tableModel;
		super.setModel(this.tableModel);
        super.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ); 
        super.setRowSelectionAllowed( true ); 
        
		super.setDefaultRenderer(Object.class,  new ColoredTableCellRenderer());
		super.setDefaultRenderer(Number.class,  new NumberCellRenderer());
		super.setDefaultRenderer(Double.class,  this.cellRenderer);
		super.getTableHeader().setReorderingAllowed(false);
		
		//super.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// http://download.oracle.com/javase/tutorial/uiswing/components/table.html#combobox
		super.setRowHeight(super.getRowHeight() + 5);
		this.setCorrelationFunctionColumn(
				super.getColumnModel().getColumn(0)
		);
	}
	
	public boolean editCellAt(int rowIndex, int columnIndex, EventObject eo) {
		boolean isEdited = super.editCellAt(rowIndex, columnIndex, eo);
		this.formatCell(rowIndex, columnIndex);
		return isEdited;
	}
	
	@Override
	public boolean editCellAt(int rowIndex, int columnIndex) {
		boolean isEdited = super.editCellAt(rowIndex, columnIndex);
		this.formatCell(rowIndex, columnIndex);
		return isEdited;
	}
	
	private void formatCell(int rowIndex, int columnIndex) {
		final Component editor = this.getEditorComponent();
		if(this.getCellEditor() != null && editor instanceof JTextField){
			editor.requestFocusInWindow();
			JTextField text = (JTextField)this.getEditorComponent();
			if (this.getColumnClass(columnIndex) == Double.class && !text.getText().isEmpty()) {
				String format = "%" + this.cellRenderer.getMaximumInteger() + "." + this.cellRenderer.getMaximumFraction() + "f";
				try {
					String formatedText = String.format(Locale.ENGLISH, format, Double.parseDouble( text.getText() ));						
					text.setText( String.format(Locale.ENGLISH, format, Double.parseDouble( formatedText )) );
				} catch(Exception e) {}
			}
			text.selectAll();
		}
	}
	
	public void setTableRowHeader(JScrollPane pane) {
		this.tableModel.setTableRowHeader( new TableRowHeader(this, pane, 85));
	}
	
	public void setCorrelationFunctionColumn(TableColumn column) {
		JComboBox comboBox = new JComboBox();
		Map<Integer, CorrelationFunctionOption> corrFunctions = this.tableModel.getCorrelationFunction();
		for (CorrelationFunctionOption corrFunc : corrFunctions.values() ) {
			comboBox.addItem( corrFunc );
		}
		column.setCellEditor(new DefaultCellEditor(comboBox));
	}
}