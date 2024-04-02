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

package com.derletztekick.geodesy.logi.io;

import java.io.File;

import com.derletztekick.geodesy.logi.sql.DataManager;
import com.derletztekick.tools.io.LockFileReader;

public abstract class RowFileReader extends LockFileReader {
	private final DataManager dataManager;
	private final int groupId;
	public RowFileReader(int groupId, DataManager dataManager, File sf) {
		super(sf);
		this.ignoreLinesWhichStartWith("#");
		this.groupId = groupId;
		this.dataManager = dataManager;
	}
	
	public RowFileReader(int groupId, DataManager dataManager, String s) {
		super(s);
		this.groupId = groupId;
		this.dataManager = dataManager;
	}
	
	/**
	 * Liefert die ID der Gruppe, in die die Daten eingefügt werden sollen
	 * @return id
	 */
	protected final int getGroupId() {
		return this.groupId;
	}
	
	/**
	 * Liefert den Datenmanager
	 * @return Datenmanager
	 */
	protected final DataManager getDataManager() {
		return this.dataManager;
	}
	
	/**
	 * Liefert die Anzahl der gelesenen Zeilen
	 * @return number of lines
	 */
	public abstract int getRowCounter();
}
