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

import java.util.Date;

public class PolarPoint3D extends Point3D {
	private final double distance,
						 azimuth,
						 zenith;
	private Date obsDate = null;
	
	public PolarPoint3D(String id, double distance, double azimuth, double zenith) {
		this(id, distance, azimuth, zenith, null);
	}
	
	public PolarPoint3D(String id, double distance, double azimuth, double zenith, Date obsDate) {
		super(id, Math.cos(azimuth)*distance*Math.sin(zenith), 
				  Math.sin(azimuth)*distance*Math.sin(zenith),
				  distance*Math.cos(zenith));
		this.distance = distance;
		this.azimuth  = azimuth;
		this.zenith   = zenith;
		this.obsDate  = obsDate;
	}
	
	public double getDistance() {
		return this.distance;
	}
	
	public double getAzimuth() {
		return this.azimuth;
	}
	
	public double getZenith() {
		return this.zenith;
	}
	
	public void setObservationDate(Date date) {
		this.obsDate = date;
	}
	
	public Date getObservationDate() {
		return obsDate;
	}
}
