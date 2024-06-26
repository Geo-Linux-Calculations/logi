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

import com.derletztekick.tools.io.filter.HTMLFileFilter;

public class HTMLTemplateFileFilter extends HTMLFileFilter {
	String name = super.getDescription();
	
	public HTMLTemplateFileFilter(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getDescription() {
		String desc = this.name.toLowerCase().endsWith(".tmpl")?this.name.substring(0, this.name.lastIndexOf('.')):this.name;
		return desc + " (*" + this.getDefaultExtension() + ")";
	}
}
