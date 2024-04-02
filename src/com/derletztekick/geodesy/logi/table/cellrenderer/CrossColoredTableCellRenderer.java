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

package com.derletztekick.geodesy.logi.table.cellrenderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class CrossColoredTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -6939631094344281289L;
	private final int increment;
	public CrossColoredTableCellRenderer(int increment) {
		this.increment = increment;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) { 
		
		int drow = row/this.increment; 
		int dcol = col/this.increment;
		
		JComponent comp = null;
		boolean enabled = false;
		if (table.getColumnClass(col) == Boolean.class) {
			enabled = value == null?false:((Boolean) value).booleanValue();
			comp = new JCheckBox();
			((JCheckBox)comp).setSelected(enabled);
			((JCheckBox)comp).setHorizontalAlignment(SwingConstants.CENTER);
		}
		else {
			comp = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		}
		

		if (row == col) {
			
			comp.setForeground(table.getForeground());
			comp.setBackground(new Color(225,225,225));
			
		}
		
		else if (drow%2 == 1 && dcol%2 == 1 && drow==dcol) {
			comp.setForeground(table.getForeground());
			comp.setBackground(new Color(235,235,235));
			
		}
		
		else if (drow%2 == 1) {
			comp.setForeground(table.getForeground());
			comp.setBackground(new Color(250,250,250));
			
		}
		
		else if (dcol%2 == 1) {
			comp.setForeground(table.getForeground());
			comp.setBackground(new Color(250,250,250));
			
		}
		
		else {
			setForeground(table.getForeground());
			comp.setBackground(table.getBackground());
		}
		
		if(isSelected) { 
			comp.setBackground(table.getSelectionBackground()); 
			comp.setForeground(table.getSelectionForeground()); 
		}
			
		return comp; 
	}
	
	@Override
	protected void setValue(final Object value) {
		if (value != null) {
			if (value instanceof String) {
				this.setHorizontalAlignment(SwingConstants.CENTER);
			}
			else if (value != null && value instanceof Number) {
				this.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			this.setText( value.toString() );
		} 
		else {
			this.setText( (value == null) ? "" : value.toString() );
		}
	}
	
}
