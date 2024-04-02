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

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Ein Zellrenderer, der jede zweite Zeile in der Tabelle f&auml;rbt
 * @author Michael Loesler <derletztekick.com>
 *
 */
public class ColoredTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 9009395832806330397L;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) { 
		JComponent comp = null;
		boolean enabled = false;

		if (table.getColumnClass(column) == Boolean.class) {
			enabled = value == null?false:((Boolean) value).booleanValue();
			comp = new JCheckBox();
			((JCheckBox)comp).setSelected(enabled);
			((JCheckBox)comp).setHorizontalAlignment(SwingConstants.CENTER);
		}
		else if (table.getColumnClass(column) == ImageIcon.class) {
			comp = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof ImageIcon) {
				((JLabel)comp).setIcon((ImageIcon)value);
				((JLabel)comp).setText("");
				((JLabel)comp).setHorizontalAlignment(SwingConstants.CENTER);
				((JLabel)comp).setVerticalAlignment(SwingConstants.CENTER);
			}
			else {
				((JLabel)comp).setIcon(null);
				((JLabel)comp).setText("");
			}
		}
		else {
			comp = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		if (row%2 == 1) {
			comp.setForeground(table.getForeground());
			comp.setBackground(new Color(245,245,245)); 
		} else {
			comp.setForeground(table.getForeground());
			comp.setBackground(table.getBackground());
		}		
		if(isSelected) { 
			comp.setBackground(table.getSelectionBackground()); 
			comp.setForeground(table.getSelectionForeground()); 
		}
			
		return comp; 
	}
	

}
