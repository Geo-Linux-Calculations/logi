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

package com.derletztekick.geodesy.logi.combobox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public abstract class NumberComboBox extends JComboBox implements ActionListener {
	private static final long serialVersionUID = -1793216893308012761L;

	public NumberComboBox(Object[] obj) {	
		super(obj);
		this.setEditable(true);
		this.addActionListener(this);
	}
	
	@Override
	public void addFocusListener(FocusListener listener ) {
		super.addFocusListener(listener);
		super.getEditor().getEditorComponent().addFocusListener( listener );
	}
	
	public abstract double getValue();
	
	public abstract void setText(String txt);
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		ComboBoxEditor editor = null;
		if (evt.getSource() == this) {
			JComboBox box = (JComboBox)evt.getSource();
			editor = box.getEditor(); 
		}
		else if(evt.getSource() instanceof ComboBoxEditor ) {
			editor = (ComboBoxEditor)evt.getSource(); 
		}
		
		if (editor != null) {
			JTextField text = (JTextField)editor.getEditorComponent();
			setText( text.getText() );
		}
	}
	
}
