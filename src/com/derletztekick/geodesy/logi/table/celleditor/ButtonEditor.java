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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;


// http://www.esus.com/javaindex/j2se/jdk1.2/javaxswing/editableatomiccontrols/jtable/jtablebuttoncell.html
public class ButtonEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 5560634091485685386L;
	protected JButton button = new JButton();
	private ImageIcon icon;
	private boolean isPushed;
	 
	public ButtonEditor() {
		super(new JCheckBox());
		this.button.setOpaque(false);
		this.button.setContentAreaFilled(false);
		this.button.setBorder(null);
		this.button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
	}
	 
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (isSelected) {
			this.button.setForeground(table.getSelectionForeground());
			this.button.setBackground(table.getSelectionBackground());
	    } else{
	    	this.button.setForeground(table.getForeground());
	    	this.button.setBackground(table.getBackground());
	    }
		if (value instanceof ImageIcon) {
			this.icon = (ImageIcon)value;
			this.button.setIcon(this.icon);
		}

		this.isPushed = true;
		return this.button;
	}
	 
	public Object getCellEditorValue() {
//	    if (this.isPushed)  {
//	    	System.out.println(this.getClass().getSimpleName() + " Geklickt: " + this.icon);
//	    }
	    this.isPushed = false;
	    return this.icon;
	  }
	   
	public boolean stopCellEditing() {
		this.isPushed = false;
		return this.isPushed; //super.stopCellEditing();
	}
	 
	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}
}