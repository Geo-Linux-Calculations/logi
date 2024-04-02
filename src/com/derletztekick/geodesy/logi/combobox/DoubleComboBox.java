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

import java.util.Locale;
import javax.swing.JTextField;

public class DoubleComboBox extends NumberComboBox {
	private static final long serialVersionUID = -3846588510973083020L;
	
	private final double defaultValue;
	private double value;
	private Double values[];
	private JTextField textField;
	
	public DoubleComboBox(Double values[], double defaultValue) {
		super(values);
		this.value = defaultValue;
		this.values = values;
		this.defaultValue = defaultValue;
		this.textField = (JTextField)this.getEditor().getEditorComponent();
		this.setText(String.valueOf(this.value));
	}
	
	public double getValue() {
		return this.value;
	}
	
	public void setText(String txt) {
		double oldValue = this.getValue();
		final String format = "%.1f";
		try {
			this.value = Double.parseDouble( txt );
		} catch(Exception e) { }
		
		if (this.value < this.values[0].doubleValue() || this.value > this.values[this.values.length-1].doubleValue() )
			this.value = this.defaultValue;
		
		this.textField.setText( String.format(Locale.ENGLISH, format, this.value) );
		this.setSelectedItem(this.value);
		this.firePropertyChange("value", oldValue, this.value);
	}	
}
