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

package com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter;

import java.util.ArrayList;
import java.util.List;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;

public abstract class TransformationParameterSet {
	public final static int FIXED_LENGTH = 0,
							FIXED_TRANSLATION_X = 1,
							FIXED_TRANSLATION_Y = 2,
							FIXED_TRANSLATION_Z = 3,
							FIXED_ROTATION_X = 4,
							FIXED_ROTATION_Y = 5,
							FIXED_ROTATION_Z = 6,
							FIXED_SCALE = 7;
	private List<Integer> restrictions = new ArrayList<Integer>(8);
	private Point srcCenterPoint, trgCenterPoint;
	/**
	 * Fuegt Restriktionen zum Modell hinzu
	 * @param type
	 */
	public void setRestriction(int type) {
		if (!this.restrictions.contains(type))
			this.restrictions.add(type);
	}
	
	/**
	 * Fuegt Restriktionen zum Modell hinzu
	 * @param type
	 */
	public final int getRestriction(int index) {
		return this.restrictions.get(index);
	}
	
	/**
	 * Setzt den (genaeherten) Schwerpunkt des Quellsystems
	 * @param point
	 */
	public void setSourceSystemCenterPoint(Point point) {
		this.srcCenterPoint = point;
	}
	
	/**
	 * Setzt den (genaeherten) Schwerpunkt des Zielsystems
	 * @param point
	 */
	public void setTargetSystemCenterPoint(Point point) {
		this.trgCenterPoint = point;
	}
	
	/**
	 * Liefert den (genaeherten) Schwerpunkt des Quellsystems
	 * @return point
	 */
	public Point getSourceSystemCenterPoint() {
		return this.srcCenterPoint;
	}
	
	/**
	 * Liefert den (genaeherten) Schwerpunkt des Zielsystems
	 * @return point
	 */
	public Point getTargetSystemCenterPoint() {
		return this.trgCenterPoint;
	}
	
	/**
	 * Liefert <code>true</code>, wenn Restriktion vorhanden
	 * @param type
	 * @return isRestricted
	 */
	public boolean isRestricted(int type) {
		return this.restrictions.contains(type);
	}
	
	/**
	 * Anzahl der Restriktionen
	 * @return nor
	 */
	public int numberOfRestrictions() {
		return this.restrictions.size();
	}
	
	/**
	 * Liefert die Anzahl aller Transformationsparameter
	 * @return nop
	 */
	public int numberOfTransformationParameters() {
		return this.getTransformationParameters() == null?0:this.getTransformationParameters().length;
	}
	
	/**
	 * Liefert die Anzahl aller zusaetzlichen Transformationsparameter
	 * @return noap
	 */
	public int numberOfAdditionalTransformationParameters() {
		return this.getAdditionalTransformationParameters() == null?0:this.getAdditionalTransformationParameters().length;
	}
	
	/**
	 * Liefert <code>true</code>, wenn es Zusatzparameter gibt
	 * @return hasAdditionalParameters
	 */
	public boolean hasAdditionalParameters() {
		return this.getAdditionalTransformationParameters() != null && this.getAdditionalTransformationParameters().length > 0;
	}
	
	/**
	 * Liefert alle Transformationsparameter in einem Array
	 * @return transformationParameters
	 */
	public abstract TransformationParameter[] getAdditionalTransformationParameters();
	
	/**
	 * Liefert den Transformationsparameter anhand seiner Kennung
	 * @param paramType
	 * @return transformationParameter
	 */
	public abstract TransformationParameter getAdditionalTransformationParameter(int paramType);

	/**
	 * Liefert alle Transformationsparameter der AGL in einem Array
	 * @return transformationParameters
	 */
	public abstract TransformationParameter[] getTransformationParameters();
	
	/**
	 * Liefert den Transformationsparameter anhand seiner Kennung
	 * @param paramType
	 * @return transformationParameter
	 */
	public abstract TransformationParameter getTransformationParameter(int paramType);
	
	/**
	 * Transformiert einen Punkt anhand des Parametersatzes ins Zielsystem
	 * @param point
	 * @return point
	 */
	public abstract Point transform(Point point);
	
	/**
	 * Transformiert einen Punkt anhand des Parametersatzes ins Startsystem
	 * @param point
	 * @return point
	 */
	public abstract Point inverseTransform(Point point);
	
}