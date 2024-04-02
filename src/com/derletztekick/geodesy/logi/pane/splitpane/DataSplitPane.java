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

package com.derletztekick.geodesy.logi.pane.splitpane;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.derletztekick.geodesy.logi.treemenu.DataTreeMenu;

public class DataSplitPane extends JSplitPane {
	private static final long serialVersionUID = -5794858701970276109L;
	private final DataTreeMenu treeMenu;
	public DataSplitPane(DataTreeMenu treeMenu) {
		this.treeMenu = treeMenu;
		super.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		super.setContinuousLayout(true);
		this.setLeftComponent(this.treeMenu);
		this.setRightComponent((JComponent)this.treeMenu.getRootObject());
		super.getLeftComponent().setMinimumSize(new Dimension(200, 100));
		super.setOneTouchExpandable(true);
		super.setDividerLocation(200);
		super.setResizeWeight(0.25);
	}
	
	@Override
	public void setLeftComponent(Component comp) {
		super.setLeftComponent(new JScrollPane(comp));
	}
}
