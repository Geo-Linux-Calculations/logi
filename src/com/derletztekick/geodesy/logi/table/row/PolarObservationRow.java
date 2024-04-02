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

package com.derletztekick.geodesy.logi.table.row;

import java.util.Date;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.tools.geodesy.Constant;
import com.derletztekick.tools.geodesy.MathExtension;

public class PolarObservationRow extends DataRow {
	private String endPointId;
	private Date observationTime = null; 
	// speichert die drei Messelemente Strecke, Azimut, Vertikalwinkel
	private Double distAimuthZenithObservations[] = new Double[] {null, null, null}; 
	
	public PolarObservationRow() {}
	
	public PolarObservationRow(int id, String endPointId, double dist3D, double azimuth, double zenith, boolean enabled, Date observationTime) {
		this.setId(id);
		this.endPointId = endPointId;
		this.distAimuthZenithObservations = new Double[] {
				dist3D,
				azimuth,
				zenith
		};
		this.setEnable(enabled);
		this.observationTime = observationTime;
	}
	
	public PolarObservationRow(int id, Point3D startPoint, Point3D endPoint, boolean enabled, Date observationTime) {
		this.setId(id);
		this.endPointId = endPoint.getId();
		double dist3D = startPoint.getDistance3D(endPoint);
		this.distAimuthZenithObservations = new Double[] {
				dist3D,
				MathExtension.MOD(Math.atan2(endPoint.getY()-startPoint.getY(), endPoint.getX()-startPoint.getX())*Constant.RHO_RAD2GRAD, 400.0),
				Math.acos((endPoint.getZ()-startPoint.getZ())/dist3D)*Constant.RHO_RAD2GRAD
		};
		this.setEnable(enabled);
		this.observationTime = observationTime;
	}
	
	
	/**
	 * Liefert die Punktnummer des Endpunktes
	 * @return endPointId
	 */
	public String getEndPointId() {
		return this.endPointId;
	}
	
	/**
	 * Liefert die Raumstrecke zwischen den Punkten
	 * @return dist3D
	 */
	public Double getDistance3D() {
		return this.distAimuthZenithObservations[0];
	}
	
	/**
	 * Liefert das Azimut zwischen den Punkten
	 * @return azimuth
	 */
	public Double getAzimuth() {
		return this.distAimuthZenithObservations[1];
	}
	
	/**
	 * Liefert den Zenitwinkel zwischen den Punkten
	 * @return zenith
	 */
	public Double getZenith() {
		return this.distAimuthZenithObservations[2];
	}
	
	/**
	 * Setzt die Punktnummer des Endpunktes
	 * @param endPointId
	 */
	public void setEndPointId(String endPointId) {
		this.endPointId = endPointId;
	}
	
	/**
	 * Setzt die Raumstrecke zwischen den Punkten, wenn dist3d > 0
	 * @param dist3D
	 */
	public void setDistance3D(Double dist3D) {
		if (dist3D == null || dist3D > Constant.EPS)
			this.distAimuthZenithObservations[0] = dist3D;
	}
	
	/**
	 * Setzt das Azimut zwischen den Punkten, wenn -400 <= az <= 400 
	 * @param azimuth
	 */
	public void setAzimuth(Double azimuth) {
		if (azimuth == null || (azimuth >= -400 && azimuth <= 400))
			this.distAimuthZenithObservations[1] = azimuth;
	}
	
	/**
	 * Setzt den Zenitwinkel zwischen den Punkten, wenn -400 <= z <= 400 
	 * @param zenith
	 */
	public void setZenith(Double zenith) {
		if (zenith == null || (zenith >= -400 && zenith <= 400))
			this.distAimuthZenithObservations[2] = zenith;
	}

	@Override
	public boolean isComplete() {
		for (int i=0; i<this.distAimuthZenithObservations.length; i++) {
			if (this.distAimuthZenithObservations[i] == null)
				return false;
		}
		
		return this.endPointId != null && !this.endPointId.trim().isEmpty();
	}
	
	/**
	 * Liefert den Aufnahmezeitpunkt der Beobachtung
	 * @return obsTime
	 */
	public Date getObservationTime() {
		return this.observationTime;
	}
	
	/**
	 * Setzt den Aufnahmezeitpunkt der Beobachtung
	 * @param date
	 */
	public void setObservationTime(Date date) {
		this.observationTime = date;
	}
}
