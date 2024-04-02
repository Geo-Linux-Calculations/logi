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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.logi.sql.DataManager;
import com.derletztekick.geodesy.logi.table.row.PolarObservationRow;
import com.derletztekick.geodesy.logi.treemenu.node.LocalSystemNode;
import com.derletztekick.tools.geodesy.Constant;
import com.derletztekick.tools.geodesy.MathExtension;

public class ObservationFileReader extends RowFileReader {
	private final boolean isXYZ;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	private Map<String, Point3D> points;
	private Map<String, PolarObservationRow> polarObservations;
	private Map<String, Integer> pointCounter;
	private String startPointId;
	private int counter = 0;
	private Map<String, Date> obsDates;
	public ObservationFileReader(LocalSystemNode node, DataManager dataManager, File sf, boolean isXYZ) {
		super(node.getId(), dataManager, sf);
		this.isXYZ = isXYZ;
		this.points   = new LinkedHashMap<String, Point3D>();
		this.obsDates = new LinkedHashMap<String, Date>();
		this.startPointId = node.getStartPointId();
		this.pointCounter = new LinkedHashMap<String, Integer>();
		this.polarObservations = new LinkedHashMap<String, PolarObservationRow>();
	}

	@Override
	public void parse(String line) {
		String columns[] = line.trim().split("[\\s;]+");

		if (columns.length >= 4) {
			String endPointId = columns[0];
			try {
				double value1 = Double.parseDouble(columns[1]);
				double value2 = Double.parseDouble(columns[2]);
				double value3 = Double.parseDouble(columns[3]);
				Date obsDate = null;
				if (columns.length >= 6) {
					obsDate = this.dateFormatter.parse(String.format(Locale.ENGLISH, "%s %s", columns[4], columns[5]));
					this.obsDates.put(endPointId, obsDate);
				}
				
				if (this.isXYZ) {
					if (this.points.containsKey(endPointId)) {
						Point3D point3d = this.points.get(endPointId);
						point3d.setX(point3d.getX() + value1);
						point3d.setY(point3d.getY() + value2);
						point3d.setZ(point3d.getZ() + value3);
						this.pointCounter.put(endPointId, this.pointCounter.get(endPointId)+1);
					}
					else {
						Point3D point3d = new Point3D(endPointId, value1, value2, value3);
						this.points.put(endPointId, point3d);
						this.pointCounter.put(endPointId, 1);
					}
				}			
				// Strecke muss groesser Null sein und Winkel zw. +/-400, sonst abbrechen
				else if (value1 > Constant.EPS && !this.startPointId.equals(endPointId) && value3 >= -400 && value3 <= 400 && value2 >= -400 && value2 <= 400) {
					value2 = MathExtension.MOD(value2, 400.0);
					value3 = MathExtension.MOD(value3, 400.0);

					// reduziere auf Lage I
					if (value3 > 200.0) {
						value3 = 400.0 - value3;
						value2 = MathExtension.MOD(value2 + 200.0, 400.0);
					}
					
					if (this.polarObservations.containsKey(endPointId)) {
						PolarObservationRow polarObservationRow = this.polarObservations.get(endPointId);
						// double dist3D, double azimuth, double zenith
						polarObservationRow.setDistance3D(polarObservationRow.getDistance3D() + value1);
						polarObservationRow.setAzimuth(polarObservationRow.getAzimuth()       + value2);
						polarObservationRow.setZenith(polarObservationRow.getZenith()         + value3);
						
						if (obsDate != null) {
							if (polarObservationRow.getObservationTime() == null)
								polarObservationRow.setObservationTime(obsDate);
							else
								polarObservationRow.setObservationTime(new Date(polarObservationRow.getObservationTime().getTime() + obsDate.getTime()));
						}
						this.pointCounter.put(endPointId, this.pointCounter.get(endPointId)+1);
					}
					else {
						this.polarObservations.put(endPointId, new PolarObservationRow(-1, endPointId, value1, value2, value3, true, obsDate));
						this.pointCounter.put(endPointId, 1);
					}							
				}	
			} catch (Exception e) {}
		}		
	}
	
	@Override
	public boolean readSourceFile() {
		this.counter = 0;
		boolean isRead = super.readSourceFile();
		if (this.isXYZ && isRead) {		
			DataManager dataManager = this.getDataManager();
			int id = this.getGroupId();
			Point3D startPoint = this.points.containsKey(this.startPointId) ? this.points.get(this.startPointId) : new Point3D(this.startPointId, 0, 0, 0);
			
			Integer cnt = this.pointCounter.get(this.startPointId);
			if (cnt != null && cnt > 0) {
				startPoint.setX(startPoint.getX()/cnt);
				startPoint.setY(startPoint.getY()/cnt);
				startPoint.setZ(startPoint.getZ()/cnt);
			}
			
			for (Point3D endPoint : this.points.values()) {
				cnt = this.pointCounter.get(endPoint.getId());
				if(!startPoint.getId().equals(endPoint.getId()) && startPoint.getDistance3D(endPoint) > Constant.EPS) {
					endPoint.setX(endPoint.getX()/cnt);
					endPoint.setY(endPoint.getY()/cnt);
					endPoint.setZ(endPoint.getZ()/cnt);

					// Fuege Punkt hinzu
					if (dataManager.saveTableRow(id, new PolarObservationRow(-1, startPoint, endPoint, true, this.obsDates.get(endPoint.getId()))))
						this.counter++;
				}
			}
		}
		else if (!this.isXYZ && isRead) {
			DataManager dataManager = this.getDataManager();
			int id = this.getGroupId();
			for (PolarObservationRow polarObs : this.polarObservations.values()) {
				String endPointId = polarObs.getEndPointId();
				if (this.pointCounter.containsKey(endPointId)) {
					Integer cnt = this.pointCounter.get(endPointId);
					if (cnt != null && cnt > 0) {
						double dist3d  = polarObs.getDistance3D() / cnt;
						double azimuth = polarObs.getAzimuth()    / cnt;
						double zenith  = polarObs.getZenith()     / cnt;
						Date obsDate = polarObs.getObservationTime();
						if (obsDate != null)
							obsDate = new Date(obsDate.getTime() / cnt);
						
						// Fuege Polarpunkt hinzu
						if (dataManager.saveTableRow(id, new PolarObservationRow(-1, endPointId, dist3d, azimuth, zenith, true, obsDate)))
							this.counter++;		
					}
				}
			}
		}
		return isRead;
	}
	
	@Override
	public int getRowCounter() {
		return this.counter;
	}

}
