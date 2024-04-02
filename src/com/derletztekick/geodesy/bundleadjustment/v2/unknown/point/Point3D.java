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
import java.util.Locale;
import java.util.Scanner;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;

/**
* Eigenschaften eines 3D-Punktes (XYZ)
* @author Michael Loesler <derletztekick.com>
*
*/
public class Point3D extends Point{
	
	/**
	 * minimaler 3D-Punktconstructor - Varianz wird auf -1.0 gesetzt
	 * @param id	Punktnummer
	 * @param x		X-Wert
	 * @param y		Y-Wert
	 * @param z		Z-Wert
	 */
	public Point3D(String id, double x, double y, double z) {
		super(id);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		
		this.coordinates0[0] = x;
		this.coordinates0[1] = y;
		this.coordinates0[2] = z;
	}
		
	@Override
	public final int getDimension() {
		return 3; 
	}
	
	/**
	 * Scan-Methode um Punkt aus Zeichenkette zu extrahieren,
	 * nutzt dabei das interne (default) Locale-Object
	 * 
	 * @see com.derletztekick.geodesy.point.Point3D#scan(String, Locale)
	 * @param str Zeichenkette
	 * @return p Point
	 */
	
	public static Point3D scan(String str) {
		return Point3D.scan(str, Locale.ENGLISH);
	}
	
	/**
	 * <code>PointId  X,  Y,  Z,  &sigma;<sub>X</sub>,  &sigma;<sub>Y</sub>,  &sigma;<sub>Z</sub><br>
	 * PointId  X,  Y,  Z,  &sigma;<sub>X,Y</sub>,  &sigma;<sub>Z</sub><br> 
	 * PointId  X,  Y,  Z,  &sigma;<sub>X,Y,Z</sub><br>
	 * PointId  X,  Y,  Z</code>
	 * 
	 */
	public static Point3D scan(String str, Locale locale) {
		Scanner scanner = new Scanner( str.trim() ).useLocale( locale );
		String pointId = new String();
		double x = 0.0,
			   y = 0.0,
			   z = 0.0;
		
		// Startpunktnummer
		if (!scanner.hasNext())
			return null;
		pointId = scanner.next();
		
		// Y-Wert 		
		if (!scanner.hasNextDouble())
			return null;
		y = scanner.nextDouble();
		
		// X-Wert 		
		if (!scanner.hasNextDouble())
			return null;
		x = scanner.nextDouble();
		
		// Z-Wert 		
		if (!scanner.hasNextDouble())
			return null;
		z = scanner.nextDouble();
				
		return new Point3D(pointId, x, y, z);
	}	

	/**
	*   Liefert Punktspezifische Zeichenkette
	* 	@return str
	*/
	@Override
	public String toString() {
		return new String(this.getClass().getSimpleName() + " " + this.getId() + ": " + this.getX() + "/" + this.getY() + "/" + this.getZ());
	}

	/**
	*   Prueft, ob Punktobjekt identisch ist
	*   @param obj ein beliebiger Punkt
	* 	@return isEquals
	*/
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final Point3D p3d = (Point3D) obj;

		if (!this.getId().equals(p3d.getId()))
			return false;

		return true;
	}

	/**
	 * Setzt Koordinaten, die um die Verzerrung zum Erwartungswert korrigiert sind.
	 * Überschreibt a-priori und a-posteriori Werte, sodass das Setzen vor der 
	 * Ausgleichun zu erfolgen hat!
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void applyBiasCorrectedValues(double x, double y, double z) {
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		
		this.coordinates0[0] = x;
		this.coordinates0[1] = y;
		this.coordinates0[2] = z;
	}
}
