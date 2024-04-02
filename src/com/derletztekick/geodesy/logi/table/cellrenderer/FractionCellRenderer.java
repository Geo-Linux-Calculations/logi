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

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Klasse zur Regelung der Darstelung von Double-Werten
 * @author Michael
 *
 */
public class FractionCellRenderer extends NumberCellRenderer {

	private static final long serialVersionUID = 8383824568799743786L;

	final private int integer;
	final private int minFraction, maxFraction;
	final private NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);
	
	public FractionCellRenderer(final int integer, final int fraction) {
		this(integer, fraction, NumberCellRenderer.RIGHT);
	}
	
	public FractionCellRenderer(int integer, int fraction, int align) {
		super(align);
		this.integer  = integer; 
		this.minFraction = fraction; 
		this.maxFraction = fraction; 
	}
	
	public FractionCellRenderer(int integer, int minFraction, int maxFraction, int align) {
		super(align);
		this.integer  = integer;   
		this.minFraction = minFraction;  
		this.maxFraction = maxFraction;
	}
	
	@Override
	protected void setValue(final Object value) {
		super.setValue(value);
		if (value != null && value instanceof Number) {
			this.formatter.setGroupingUsed(false);
			if (this.integer > 0)
				this.formatter.setMaximumIntegerDigits(this.integer);
			this.formatter.setMaximumFractionDigits(this.maxFraction);
			this.formatter.setMinimumFractionDigits(this.minFraction);
			this.setText(this.formatter.format(((Number) value).doubleValue()));
		} else {
			this.setText( (value == null) ? "" : value.toString() );
		}
	}
	
	/**
	 * Liefert die min Anzahl der Nachkommastellen
	 * @return fraction
	 */
	public int getMinimumFraction() {
		return this.minFraction;
	}
	
	/**
	 * Liefert die max Anzahl der Nachkommastellen
	 * @return fraction
	 */
	public int getMaximumFraction() {
		return this.maxFraction;
	}
	
	/**
	 * Liefert die Anzahl der Vorkommastellen
	 * @return integer
	 */
	public int getMaximumInteger() {
		return this.integer;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FractionCellRenderer other = (FractionCellRenderer) obj;
		if (align != other.align)
			return false;
		if (formatter == null) {
			if (other.formatter != null)
				return false;
		} else if (!formatter.equals(other.formatter))
			return false;
		if (minFraction != other.minFraction)
			return false;
		if (maxFraction != other.maxFraction)
			return false;
		if (integer != other.integer)
			return false;
		return true;
	}
}
