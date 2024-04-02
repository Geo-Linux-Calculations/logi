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
import java.util.LinkedHashMap;
import java.util.Map;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.logi.sql.DataManager;
import com.derletztekick.geodesy.logi.table.row.PointRow;

public class PointFileReader extends RowFileReader {
	private int counter = 0;
	private Map<String, Point3D> points;
	private Map<String, Integer> pointCounter;
	
	public PointFileReader(int groupId, DataManager dataManager, File sf) {
		super(groupId, dataManager, sf);
		this.points   = new LinkedHashMap<String, Point3D>();
		this.pointCounter = new LinkedHashMap<String, Integer>();
	}

	@Override
	public void parse(String line) {
		String columns[] = line.trim().split("[\\s;]+");

		if (columns.length >= 4) {
			String pointId = columns[0];
			try {
				double x = Double.parseDouble(columns[1]);
				double y = Double.parseDouble(columns[2]);
				double z = Double.parseDouble(columns[3]);
				
				
				if (this.points.containsKey(pointId)) {
					Point3D point3d = this.points.get(pointId);
					point3d.setX(point3d.getX() + x);
					point3d.setY(point3d.getY() + y);
					point3d.setZ(point3d.getZ() + z);
					this.pointCounter.put(pointId, this.pointCounter.get(pointId)+1);
				}
				else {
					Point3D point3d = new Point3D(pointId, x, y, z);
					this.points.put(pointId, point3d);
					this.pointCounter.put(pointId, 1);
				}
				
			} catch (NumberFormatException nfe) {}
		}		
	}

	@Override
	public boolean readSourceFile() {
		this.counter = 0;
		boolean isRead = super.readSourceFile();
		
		if (isRead) {
			DataManager dataManager = this.getDataManager();
			int id = this.getGroupId();

			for (Point3D point : this.points.values()) {
				String pointId = point.getId();
				Integer cnt = this.pointCounter.get(pointId);
				if (cnt != null && cnt > 0) {
					double x = point.getX()/cnt;
					double y = point.getY()/cnt;
					double z = point.getZ()/cnt;

					if (dataManager.saveTableRow(id, new PointRow(-1, pointId, x, y, z, true)))
						this.counter++;
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
