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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.UncertaintyModel;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.PolarPoint3D;

public class PolarPointGroup extends PointGroup {
	private UncertaintyModel uncertaintyModel;
	public PolarPointGroup(int id, String stationId, UncertaintyModel uncertaintyModel) {
		super(id);
		this.uncertaintyModel = uncertaintyModel;
		// Fuege als ersten Punkt in der Liste den Standpunkt hinzu
		super.add(new Point3D(stationId, 0,0,0));
	}
	
	@Override
	public boolean add(Point point) {
		if (point instanceof PolarPoint3D)
			return super.add(point);
		return false;
	}
	
	public void deriveCovarianceMatrix() {
		Matrix Cxx = this.uncertaintyModel.deriveCovarianceMatrix(this);
		super.setCovarianceMatrix(Cxx);
	}
	
	public boolean hasTimeReferencedData() {
		Set<Long> dates = new HashSet<Long>(this.size());
		for (int i=1; i<this.size(); i++) {
			Date obsDate = ((PolarPoint3D)this.get(i)).getObservationDate();
			if (obsDate == null)
				return false;
			
			long date = obsDate.getTime();
			if (dates.contains(date))
				return false;
			
			dates.add(date);
		}
		return true;
	}
}
