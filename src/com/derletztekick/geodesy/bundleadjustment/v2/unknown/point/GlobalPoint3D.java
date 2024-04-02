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

package com.derletztekick.geodesy.bundleadjustment.v2.unknown.point;

import java.util.ArrayList;
import java.util.List;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PointGroup;


public class GlobalPoint3D extends Point3D implements GlobalPoint {
	private List<PointGroup> pointGroupArrayList = new ArrayList<PointGroup>();
	public GlobalPoint3D(String id, double x, double y, double z) {
		super(id, x, y, z);
	}
	
	public GlobalPoint3D(Point3D point) {
		super(point.getId(), point.getX0(), point.getY0(), point.getZ0());
		this.setX(point.getX());
		this.setY(point.getY());
		this.setZ(point.getZ());
	}
	
	public boolean addLocalPointGroup(PointGroup group) {
		if (group.getDimension() != this.getDimension() || !group.contains(this.getId()) || this.pointGroupArrayList.contains(group))
			return false;
		return this.pointGroupArrayList.add(group);
	}
	
	public int numberOfLocalPointGroups() {
		return this.pointGroupArrayList.size();
	}
	
	public PointGroup getLocalPointGroup(int index) {
		return this.pointGroupArrayList.get(index);
	}
	
	@Override
	public void setColInJacobiMatrix(int col) {
		for (PointGroup localPointGroup : this.pointGroupArrayList) {
			Point localPoint = localPointGroup.get(this.getId());
			localPoint.setColInJacobiMatrix(col);
		}
		super.setColInJacobiMatrix(col);
	}

}
