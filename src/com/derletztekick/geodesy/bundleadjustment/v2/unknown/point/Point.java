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

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.UnknownParameter;
import com.derletztekick.tools.geodesy.Constant;


/**
 * Allg. Punkteigenschaften. Klasse ist
 * <em>abstract</em>, da eine Instanz dieser
 * Klasse keinen Sinn macht.
 * @author Michael Loesler <derletztekick.com>
 *
 */

public abstract class Point extends UnknownParameter {
    // Punktnummer
	private String id = new String();
    // Zeile in Designmatrix; -1 entspricht nicht gesetzt	
	private int rowInJacobiMatrix = -1;
	private final static double ZERO = Math.sqrt(Constant.EPS);
	protected final double coordinates0[] = new double[this.getDimension()];
	private double coordinates[] = new double[this.getDimension()],
				   redundancy[]  = new double[this.getDimension()],
	               sigma[]       = new double[this.getDimension()],
	               nabla[]       = new double[this.getDimension()],
	               Tprio =  0.0,
    			   Tpost =  0.0,
    			   omega =  0.0;
	
	private boolean isOutlier = false;

	/**
	 * Erzeugt einen Punkt mit einer Punktnummer
	 * @param id Punktnummer
	 * @throws IllegalArgumentException
	 */
	public Point(String id) throws IllegalArgumentException {
		if (id == null || id.trim().length()==0)
			throw new IllegalArgumentException(this.getClass()+" Punktnummer ungueltig!");
		this.id = id.trim();
		for (int i=0; i<this.getDimension(); i++) 
			this.sigma[i] = -1.0;
	}
	/**
	 * Liefert die eindeutige Nummer (id)
	 * des betrefflichen Punktes
	 * @return str
	 */
	public String getId() {
		return this.id;
	}
		
	/**
	 * Gibt die (erste) Zeilenposition
	 * in der Designmatrix A zurueck 
	 * @return row Zeile in Matrix
	 */
	public int getRowInJacobiMatrix() {
		return this.rowInJacobiMatrix;
	}
	
	/**
	 * Legt die Zeile, in der der Punkt
	 * in der JacobiMatrix steht, fest.
	 * @param row Zeile in Matrix
	 */
	public void setRowInJacobiMatrix(int row) {
		this.rowInJacobiMatrix = row;
	}
		
	/**
	 * Liefert die Dimension des Punktes 1,2 bzw. 3
	 * @return dimension
	 */
	public abstract int getDimension();

	/**
	 * setzt Standardabweichung der X-Komponente 
	 * 
	 * @param std
	 */
	public void setStdX(double std) {
		this.sigma[0] = (std>0)?std:-1.0;
	}

	/**
	 * setzt Standardabweichung der Y-Komponente 
	 * 
	 * @param std
	 */
	public void setStdY(double std) {
		this.sigma[1] = (std>0)?std:-1.0;
	}

	/**
	 * setzt Standardabweichung der Z-Komponente 
	 * 
	 * @param std
	 */
	public void setStdZ(double std) {
		this.sigma[this.getDimension()-1] = (std>0)?std:-1.0;
	}
	
	/**
	 * Standardabweichung der X-Komponente 
	 * 
	 * @return std
	 */
	public double getStdX() {
		return this.sigma[0];
	}

	/**
	 * Standardabweichung der Y-Komponente 
	 * 
	 * @return std
	 */
	public double getStdY() {
		return this.sigma[1];
	}

	/**
	 * Standardabweichung der Z-Komponente
	 * 
	 * @return std
	 */
	public double getStdZ(){
		return this.sigma[this.getDimension()-1];
	}
		
	/**
	 * Setzt X-Koordinate des Punktes 
	 * 
	 * @param x X-Wert
	 */
	public void setX(double x) {
		this.coordinates[0] = x;
	}

	
	/**
	 * Setzt Y-Koordinate des Punktes 
	 * 
	 * @param y Y-Wert
	 */
	public void setY(double y) {
		this.coordinates[1] = y;
	}
	
	/**
	 * Setzt Z-Koordinate des Punktes 
	 * @param z Z-Wert
	 */
	public void setZ(double z) {
		this.coordinates[this.getDimension()-1] = z;
	}
	
	/**
	 * X-Koordinate vom Punkt 
	 * 
	 * @return x X-Wert
	 */
	public double getX() {
		return this.coordinates[0];
	}
	
	/**
	 * Y-Koordinate vom Punkt 
	 * 
	 * @return y Y-Wert
	 */
	public double getY() {
		return this.coordinates[1];
	}
	
	/**
	 * Z-Koordinate vom Punkt 
	 * 
	 * @return z Z-Wert
	 */
	public double getZ() {
		return this.coordinates[this.getDimension()-1];
	}
	
	/**
	 * Liefert urspruenglichen Koordinatenwert von X
	 * @return X<sub>0</sub>
	 */
	public double getX0() {
		return this.coordinates0[0];
	}
	
	/**
	 * Liefert urspruenglichen Koordinatenwert von Y
	 * @return Y<sub>0</sub>
	 */
	public double getY0() {
		return this.coordinates0[1];
	}
	
	/**
	 * Liefert urspruenglichen Koordinatenwert von Z
	 * @return Z<sub>0</sub>
	 */
	public double getZ0() {
		return this.coordinates0[this.getDimension()-1];
	}
	
	/**
	 * Setzt den Redundanzanteil des Punktes
	 * @param r
	 */
	public void setRedundancy(double[] r) {
		if (r.length == this.getDimension())
			this.redundancy = r;
	}	
	
	/**
	 * Setzt den geschaetztn groben Fehler des Punktes
	 * @param nabla
	 */
	public void setNabla(double[] nabla) {
		if (nabla.length == this.getDimension())
			this.nabla = nabla;
	}
	
	/**
	 * Redundanzanteil des gesamten Punktes
	 * @return r
	 */
	public double getRedundancy() {
		double r = 0.0;
		for (int i=0; i<this.redundancy.length; i++)
			r += this.redundancy[i];
		return r;
	}
	
	/**
	 * Liefert den Redundanzanteil r<sub>X</sub>
	 * @return r<sub>X</sub>
	 */
	public double getRedundancyX() {
		return this.redundancy[0];
	}
	
	/**
	 * Liefert den Redundanzanteil r<sub>Y</sub>
	 * @return r<sub>Y</sub>
	 */
	public double getRedundancyY() {
		return this.redundancy[1];
	}
	
	/**
	 * Liefert den Redundanzanteil r<sub>Z</sub>
	 * @return r<sub>Z</sub>
	 */
	public double getRedundancyZ() {
		return this.redundancy[this.getDimension()-1];
	}
	
	/**
	 * Liefert Nabla n<sub>X</sub>
	 * @return n<sub>X</sub>
	 */
	public double getNablaX() {
		return this.nabla[0];
	}
	
	/**
	 * Liefert Nabla n<sub>Y</sub>
	 * @return n<sub>Y</sub>
	 */
	public double getNablaY() {
		return this.nabla[1];
	}
	
	/**
	 * Liefert Nabla n<sub>Z</sub>
	 * @return n<sub>Z</sub>
	 */
	public double getNablaZ() {
		return this.nabla[this.getDimension()-1];
	}
	
	/**
	 * Setzt die Testgroesse a-priori
	 * 
	 * @param Tprio
	 */
	public void setTprio(double Tprio) {
		this.Tprio = Tprio<Point.ZERO?0.0:Tprio;
	}
	
	/**
	 * Setzt die Testgroesse a-posteriori
	 * 
	 * @param Tpost
	 */
	public void setTpost(double Tpost) {
		this.Tpost = Tpost<Point.ZERO?0.0:Tpost;
	}	
	
	
	/**
	 * Liefert die Testgroesse a-priori
	 * 
	 * @return Tprio
	 */
	public double getTprio() {
		return this.Tprio<Point.ZERO?0.0:this.Tprio;
	}
	
	/**
	 * Liefert die Testgroesse a-posteriori
	 * 
	 * @return Tpost
	 */
	public double getTpost() {
		return this.Tpost<Point.ZERO?0.0:this.Tpost;
	}	
	
	/**
	 * Legt fest, ob der Punkt mgl.weise 
	 * ein Ausreisser ist, weil die Nullhypothese 
	 * verworfen wurde  
	 * 
	 * @param isOutlier
	 */
	public void isOutlier(boolean isOutlier) {
		this.isOutlier = isOutlier;
	}	
	
	/**
	 * Liefert <code>true</code>, wenn die Nullhypothese der betreffliche 
	 * Punkt beim statistischen Test T<sub>prio</sub> <strong>oder</strong>
	 * T<sub>post</sub> verworfen wurde. 
	 * 
	 * @return isOutlier
	 */
	public boolean isOutlier() {
		return this.isOutlier;
	}
	
	/**
	 * Setzt die gewichtete Quadratsumme der
	 * Verbesserungen der Beobachung
	 * 
	 * @return v<sup>T</sup>Pv
	 */
	public void setOmega(double omega) {
		this.omega = omega;
	}
	
	/**
	 * Liefert die gewichtete Quadratsumme der
	 * Verbesserungen der Beobachung
	 * 
	 * @return v<sup>T</sup>Pv
	 */
	public double getOmega() {
		return this.omega;
	}

	
	/**
	 * Liefert 3D-Abstand zum 3D Punkten p
	 * @param p 3D Punkt
	 * @return distance3D
	 * @throws IllegalArgumentException
	 */
	public double getDistance3D(Point p) {
		if (this.getDimension() + p.getDimension() < 6)
			throw new IllegalArgumentException("Raumstrecke nur zwischen 3D Punkten bestimmbar " +
					this.id +" " + this.getDimension() + "D und " + 
					p.getId() + " " + p.getDimension() + "D"); 

	    return Math.sqrt( Math.pow(this.getX()-p.getX(),2)
	                    + Math.pow(this.getY()-p.getY(),2)
	                    + Math.pow(this.getZ()-p.getZ(),2));
	}

	/**
	 * Liefert 2D-Abstand zwischen zwei 2D/3D Punkten
	 * @param p
	 * @return distance2D
	 * @throws IllegalArgumentException
	 */
	public double getDistance2D(Point p){
		if (this.getDimension() == 1 || p.getDimension() == 1) 
			throw new IllegalArgumentException("Horizontalstrecke nicht mit 1D Punkte(n) bestimmbar " +
					this.id +" " + this.getDimension() + "D und " + 
					p.getId() + " " + p.getDimension() + "D");
		return Math.hypot(this.getX()-p.getX(), this.getY()-p.getY());
	}
	
	@Override
	public int getParameterTyp() {
		if (this.getDimension() == 1)
			return UnknownParameter.TYPE_POINT1D;
		else if (this.getDimension() == 2)
			return UnknownParameter.TYPE_POINT2D;
		return UnknownParameter.TYPE_POINT3D;
	}
}