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

package com.derletztekick.geodesy.logi.toolbar;

import com.derletztekick.geodesy.logi.sql.DataManager;
import com.derletztekick.tools.babel.Babel;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
//http://download.oracle.com/javase/tutorial/uiswing/components/toolbar.html
public class NavigationToolBar extends JToolBar {
	private static final long serialVersionUID = 2295462245384869280L;
	private final DataManager dataManager;
	public final static String NEW_PROJECT            = "NEW_PROJECT",
							   OPEN_PROJECT           = "OPEN_PROJECT",
							   COPY_PROJECT           = "COPY_PROJECT",
							   IMPORT_FILE            = "IMPORT_FILE",
							   EXPORT_REPORT          = "EXPORT_REPORT",
							   SAVE_SETTINGS          = "SAVE_SETTINGS",
							   ADD_LOCAL_SYSTEM       = "ADD_LOCAL_SYSTEM",
							   REMOVE_LOCAL_SYSTEM    = "REMOVE_LOCAL_SYSTEM",
							   TRUNCATE_SYSTEM        = "TRUNCATE_SYSTEM",
							   CHECK_UPDATES          = "CHECK_UPDATES",
							   ADJUST                 = "ADJUST";
	public NavigationToolBar(DataManager dataManager) {
		this.dataManager = dataManager;
		this.init();
	}
	
	public void init() {
		super.setOrientation(JToolBar.HORIZONTAL);
		super.setName("Werkzeugleiste");

		super.setBorderPainted(true);
		super.setRollover(true);
		super.setFloatable(true);
		
		Babel babel = this.dataManager.getBabel();
		
		this.add(
				this.makeNavigationButton("database--plus", NavigationToolBar.NEW_PROJECT, babel.getString(this.getClass().getSimpleName(), "new.title"))
		);
		

		this.add(
				this.makeNavigationButton("database--arrow", NavigationToolBar.OPEN_PROJECT, babel.getString(this.getClass().getSimpleName(), "open.title"))
		);
		
		this.add(
				this.makeNavigationButton("databases", NavigationToolBar.COPY_PROJECT, babel.getString(this.getClass().getSimpleName(), "copy.title"))
		);
		
		super.addSeparator();
		
		this.add(
				this.makeNavigationButton("document-import", NavigationToolBar.IMPORT_FILE, babel.getString(this.getClass().getSimpleName(), "import.title"))
		);
		
		this.add(
				this.makeNavigationButton("application-task--disk", NavigationToolBar.SAVE_SETTINGS, babel.getString(this.getClass().getSimpleName(), "save.title"))
		);
		
		this.add(
				this.makeNavigationButton("report--pencil", NavigationToolBar.EXPORT_REPORT, babel.getString(this.getClass().getSimpleName(), "report.title"))
		);
		
		super.addSeparator();

		this.add(                     
				this.makeNavigationButton("table--eraser", NavigationToolBar.TRUNCATE_SYSTEM, babel.getString(this.getClass().getSimpleName(), "truncate.title"))
		);
		
		super.addSeparator();
		
		this.add(
				this.makeNavigationButton("map--plus", NavigationToolBar.ADD_LOCAL_SYSTEM, babel.getString(this.getClass().getSimpleName(), "add_local.title"))
		);
				
		this.add(
				this.makeNavigationButton("map--minus", NavigationToolBar.REMOVE_LOCAL_SYSTEM, babel.getString(this.getClass().getSimpleName(), "del_local.title"))
		);
		
		
		super.addSeparator();
		
		this.add(
				this.makeNavigationButton("omega", NavigationToolBar.ADJUST, babel.getString(this.getClass().getSimpleName(), "adjust.title"))
		);
		
		super.addSeparator();
		
		this.add(
				this.makeNavigationButton("arrow-circle-double", NavigationToolBar.CHECK_UPDATES, babel.getString(this.getClass().getSimpleName(), "update.title"))
		);

	}
	
	private JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText) {
		String imgLocation = "/com/derletztekick/geodesy/logi/gfx/" + imageName + ".png";
		URL imageURL = this.getClass().getResource(imgLocation);
		String altText = actionCommand;

		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this.dataManager);
		if (imageURL != null) {               
			button.setIcon(new ImageIcon(imageURL, altText));
		} else {
			button.setText(altText);
			System.err.println("Resource not found: " + imgLocation);
		}

		return button;
	}
}
