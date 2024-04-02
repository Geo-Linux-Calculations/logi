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

package com.derletztekick.geodesy.logi.table.comparator;

import java.util.Comparator;

/**
 * AbsoluteValueComparator vergleicht die Betraege zweier Zahlen; ansonsten wird die String-Methode compareToIgnoreCase() genutzt
 * @author Michael Loesler <derletztekick.com>
 *
 */
public class AbsoluteValueComparator implements Comparator<Object>{

	@Override
    public int compare( Object o1, Object o2 ) {
		if (o1 instanceof Number && o2 instanceof Number) {
			if( Math.abs(((Number)o1).doubleValue()) < Math.abs(((Number)o2).doubleValue()) )
				return -1;
			else if( Math.abs(((Number)o1).doubleValue()) > Math.abs(((Number)o2).doubleValue()) )
				return  1;
			else
				return  0;
		}
		else if (o1 instanceof Boolean && o2 instanceof Boolean) {
			if( ((Boolean)o1).booleanValue()  && ((Boolean)o2).booleanValue() )
				return  0;
			else if( ((Boolean)o1).booleanValue() )
				return -1;
			else
				return  1;
		}
		else
			return o1.toString().compareToIgnoreCase(o2.toString());
    }
}
