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

package com.derletztekick.geodesy.bundleadjustment.v2.pointgroup;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.GlobalPoint3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.GlobalPoint;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;

public class GlobalPointGroup extends PointGroup {

	public GlobalPointGroup(int id) {
		super(id);
	}
	
	@Override
	public boolean add(Point point) {
		if (point instanceof GlobalPoint)
			return super.add(point);
		else if (point.getDimension() == 3)
			return super.add(new GlobalPoint3D((Point3D)point));
		return false;
	}

}
