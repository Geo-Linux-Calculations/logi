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

package com.derletztekick.geodesy.logi.table.celleditor;

import java.awt.Color;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class DateEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 3919519518988550875L;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	private Date value = null;
	private final static Border RED = new LineBorder(Color.RED);
	private final static Border BLACK = new LineBorder(Color.BLACK);
	
	// http://www.cs.rit.edu/usr/local/pub/swm/jdoc1.6.0_19/src-html/javax/swing/JTable.NumberEditor.html#line.5433))
	public DateEditor() {
		super(new JTextField());
	}
	
	@Override
	public boolean stopCellEditing() {
		String str = (String)super.getCellEditorValue();
		if (str.isEmpty()) {
			this.value = null;
			super.stopCellEditing();
		}
		try {
			this.value = this.dateFormatter.parse(str.trim());
		}
		catch (Exception e) {
			((JComponent)getComponent()).setBorder(RED);
			return false;
		}
		return super.stopCellEditing();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		//this.value = (Double) value;
		if (value != null && value instanceof Date)
			value = this.dateFormatter.format(value);
		this.value = null;
		((JComponent)getComponent()).setBorder(BLACK);
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	@Override
	public Object getCellEditorValue() {
		return this.value;
	}
}
