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

package com.derletztekick.geodesy.logi.table.row;

public abstract class DataRow {
	private int id = -1;
	private boolean enabled = true;
	
	/**
	 * Liefert die ID des Datensatzes. Besitzt der Datensatz noch keine ID
	 * wird -1 zurueckgeliefert.
	 * @return id
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Liefert <code>true</code>, wenn der Datensatz
	 * aktiv ist und verwendet werden soll
	 * @return enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
	
	/**
	 * Legt fest, ob der Datensatz
	 * aktiv ist und verwendet werden soll
	 * @param enabled
	 */
	public void setEnable(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Liefert die ID des Datensatzes
	 * @return id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + " " + this.getId();
	}
	
	/**
	 * Liefert <code>true</code>, wenn der Datensatz vollstaendig ist
	 * @return isComplete
	 */
	public abstract boolean isComplete();
}
