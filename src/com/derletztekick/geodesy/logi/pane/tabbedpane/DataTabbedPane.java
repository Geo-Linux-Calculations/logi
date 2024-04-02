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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.derletztekick.geodesy.logi.settingpanel.SettingPanel;
import com.derletztekick.geodesy.logi.table.DataTable;

public abstract class DataTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = -1943329121753006627L;
	private List<DataTable> tableList = new ArrayList<DataTable>(3);
	private SettingPanel settingPanel;
	public DataTabbedPane(SettingPanel settingPanel) {
		super(JTabbedPane.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
		this.settingPanel = settingPanel;
		super.add(this.settingPanel.toString(), new JScrollPane(this.settingPanel));
	}
	
	public void add(DataTable table) {
		this.tableList.add(table);
		super.add(table.toString(), new JScrollPane(table));
		super.repaint();
	}
	
	/**
	 * Liefert das Panel, auf den die Einstellungen fuer
	 * die Gruppe gesetzt werden.
	 * 
	 * @return settingPanel
	 */
	public SettingPanel getSettingPanel() {
		return this.settingPanel;
	}
	
	/**
	 * Tasucht das SettingPanel aus
	 * @param settingPanel
	 */
	public void setSettingPanel(SettingPanel settingPanel) {
		this.settingPanel = settingPanel;
		this.setComponentAt(0,  new JScrollPane(this.settingPanel));
		this.validate();
	}
	
	/**
	 * Liefert die Datentabelle zurueck
	 * @return table
	 */
	public DataTable getTable(int i) {
		return this.tableList.get(i);
	}
	
	/**
	 * Liefert die Anzahl der Datentabellen zurueck
	 * @return count
	 */
	public int getTableCount() {
		return this.tableList.size();
	}
	
	
	/**
	 * Entfernt den Inhalt in den Tabellen
	 */
	public void clearTables() {
		for (DataTable dataTable : this.tableList ) {
			dataTable.getModel().clearAll();
		}
	}

}
