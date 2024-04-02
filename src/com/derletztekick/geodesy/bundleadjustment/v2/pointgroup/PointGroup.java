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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.Matrix;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet;
import com.derletztekick.tools.geodesy.MathExtension;


/**
 * Gruppierung von Punkten gleichen Typs (=Dimension)
 * @author Michael Loesler <derletztekick.com>
 *
 */
public class PointGroup {
	private int id; 
	private int dimension = -1; // Keine Dimension, wenn -1
	private Matrix CoVar = null;
	private Map<String,Point> pointHashMap = new LinkedHashMap<String,Point>();
	private List<Point> pointArrayList = new ArrayList<Point>();
	private TransformationParameterSet transformationParameterSet;
	
	/**
	 * Konstruktor fuer Punktgruppe
	 * @param id GruppenId
	 */
	public PointGroup(int id) {
		this.id = id;
	}
	
	/**
	 * Liefert die Punktgruppenid
	 * @return id
	 */
	public final int getId() {
		return this.id;
	}
	
	/**
	 * Fuegt einen Punkt der Kollektion hinzu
	 * @param  point Punkt
	 * @return isAdded
	 */
	public boolean add(Point point) {
		//String pointId = ((Point)point).getId();
		String pointId = point.getId();
		int pointDim = point.getDimension();
		
		if (this.dimension<0)
			this.dimension = pointDim;
		
		if (this.dimension != pointDim || this.pointHashMap.containsKey( pointId ))
			return false;
		
		this.pointHashMap.put( pointId, point );
		this.pointArrayList.add( point );
		point.setPointGroup(this);
		
		return true;
	}
	
	/**
	 * Gibt Punkt an der Stelle <code>index</code>
	 * aus der Kollektion zurueck
	 * @param index Index
	 * @return Point
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Point get( int index ) throws ArrayIndexOutOfBoundsException{
		return this.pointArrayList.get( index );
	}
	
	/**
	 * Gibt Punkt mit der Punktnummer <code>pointId</code>
	 * aus der Kollektion zurueck
	 * @param pointId Punktnummer
	 * @return Point
	 */
	public Point get( String pointId ) {
		return this.pointHashMap.get( pointId );
	}
	
	/**
	 * Gibt die Anzahl der Punkte zurueck
	 * @return size
	 */
	public int size() {
		return this.pointArrayList.size();
	}
	
	/**
	 * Liefert die Dimension der Punkte
	 * aus der Gruppe zurueck, enthaelt die 
	 * Gruppe keine Punkte, wird -1 geliefert
	 * @return dimension
	 */
	public int getDimension() {
		return this.dimension;
	}

	
	/**
	 * Liefert Klassenspezifischen String
	 * @return str
	 */
	@Override
	public String toString() {
		return new String(this.getClass() + " " + this.id + " Points in Group: " + this.size());
	}


	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PointGroup pointGroup = (PointGroup) obj;
		if (id != pointGroup.id)
			return false;
		if (pointArrayList == null) {
			if (pointGroup.pointArrayList != null)
				return false;
		} else if (!pointArrayList.equals(pointGroup.pointArrayList))
			return false;
		if (pointHashMap == null) {
			if (pointGroup.pointHashMap != null)
				return false;
		} else if (!pointHashMap.equals(pointGroup.pointHashMap))
			return false;
		return true;
	}
	
	/**
	 * Berechnet den Schwerpunkt einer Punktgruppe
	 * @return centerPoint Schwerpunkt
	 */
	public Point getCenterPoint() {
		double x = 0.0, 
		       y = 0.0, 
		       z = 0.0;
		
		for (int i=0; i<this.size(); i++) {
			if (this.getDimension() > 1) {
				x += this.get(i).getX();
				y += this.get(i).getY();
			}
			if (this.getDimension() != 2) {
				z += this.get(i).getZ();
			}
		}
		
//		if (this.getDimension() == 1)
//			return new Point1D("c", z/this.size());
//		
//		else if (this.getDimension() == 2)
//			return new Point2D("c", x/this.size(), y/this.size());
//		
//		else 
		if (this.getDimension() == 3)
			return new Point3D(this.getClass().getSimpleName(), x/this.size(), y/this.size(), z/this.size());
		
		return null;
	}
	
	/**
	 * Liefert <ocde>true</code>, wenn ein Punkt mit der Punktnummer bereits existiert. 
	 * Die Koordinaten werden nicht verglichen!
	 * @param point
	 * @return contains
	 */
	public boolean contains(Point point) {
		return this.pointHashMap.containsKey(point.getId());
	}
	
	/**
	 * Liefert <ocde>true</code>, wenn ein Punkt mit der Punktnummer bereits existiert. 
	 * @param pointId
	 * @return contains
	 */
	public boolean contains(String pointId) {
		return this.pointHashMap.containsKey(pointId);
	}
	
	/**
	 * Liefert die Kovarianzmatrix der Punktgruppe
	 * @return CoVar
	 */
	public Matrix getCovarianceMatrix() {
		int len = this.size()*this.getDimension();

		if (this.CoVar == null || !this.CoVar.isSquare() || this.CoVar.numColumns() != len) 
			return MathExtension.identity(len);

		return this.CoVar;
	}
	
	/**
	 * Setzt die Kovarianzmatrix der Punktgruppe
	 * @param CoVar
	 */
	public void setCovarianceMatrix(Matrix CoVar) {
		this.CoVar = CoVar;
	}
	
	/**
	 * Setzt die Transformationsparameter der Punktgruppe
	 * @param trafoSet
	 */
	public void setTransformationParameterSet(TransformationParameterSet trafoSet) {
		this.transformationParameterSet = trafoSet;
		TransformationParameter trafoParam[] = this.transformationParameterSet.getTransformationParameters();
		for (int i=0; i<trafoParam.length; i++)
			trafoParam[i].setPointGroup(this);
	}
	
	/**
	 * Liefert die Transformationsparameter der Punktgruppe
	 * @return trafoSet
	 */
	public TransformationParameterSet getTransformationParameterSet() {
		return this.transformationParameterSet;
	}
	
}
