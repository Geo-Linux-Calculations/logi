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

package com.derletztekick.geodesy.logi.pane.tabbedpane;

import javax.swing.JScrollPane;

import com.derletztekick.geodesy.logi.settingpanel.GlobalSettingPanel;
import com.derletztekick.geodesy.logi.table.DataTable;
import com.derletztekick.geodesy.logi.table.UpperSymmMatrixTable;

public class GlobalTabbedPane extends DataTabbedPane {
	private static final long serialVersionUID = 2122577494723940805L;
	private UpperSymmMatrixTable upperSymmMatrixTable;
	public GlobalTabbedPane(GlobalSettingPanel globalSettingPanel, DataTable dataTables[], UpperSymmMatrixTable upperSymmMatrixTable) {
		super(globalSettingPanel);
		this.upperSymmMatrixTable = upperSymmMatrixTable;
		if (dataTables != null) {
			for (int i=0; i<dataTables.length; i++) {
				if (i==1 && this.upperSymmMatrixTable != null) {
					JScrollPane scrollPane = new JScrollPane(this.upperSymmMatrixTable);
					super.add(this.upperSymmMatrixTable.toString(), scrollPane);
					this.upperSymmMatrixTable.setTableRowHeader(scrollPane);
				}
					
				super.add(dataTables[i]);
			}
		}
	}
	
	public UpperSymmMatrixTable getUpperSymmMatrixTable() {
		return this.upperSymmMatrixTable;
	}
	
	public void setSettingPanel(GlobalSettingPanel globalSettingPanel) {
		super.setSettingPanel(globalSettingPanel);
	}
}
