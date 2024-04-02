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

package com.derletztekick.geodesy.logi.treemenu.node;

import com.derletztekick.geodesy.logi.pane.tabbedpane.LocalTabbedPane;

public class LocalSystemNode extends SystemNode {
	private static final long serialVersionUID = 8892664713997828129L;
	private String startPointId;
	public LocalSystemNode(int id, String name, String startPointId, LocalTabbedPane localTabbedPane) {
		super(id, name, localTabbedPane);
		this.startPointId = startPointId;
	}
	
	public String getStartPointId() {
		return this.startPointId;
	}
	
	public void setUserObject(Object o) {
		if (o == null || o.toString().trim().isEmpty())
			return;
		super.setUserObject(o);
	}
	
}
