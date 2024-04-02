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

public class PointRow extends DataRow {
	private String pointId;
	
	// speichert die drei Koordinaten X, Y, Z
	private Double coordinates[] = new Double[] {null, null, null}; 
	private double redundance[] = new double[] {0.0, 0.0, 0.0}; 
	private double nabla[] = new double[] {0.0, 0.0, 0.0}; 
	private double error[] = new double[] {0.0, 0.0, 0.0}; 
	private Double sigma[] = new Double[] {null, null, null};
	private double testValues[] = new double[] {0.0, 0.0};
	private double omega = 0;
	private boolean outlier = false;
	
	public PointRow() {}
	
	public PointRow(int id, String pointId, double x, double y, double z, boolean enabled) {
		this.setId(id);
		this.pointId = pointId;
		this.coordinates = new Double[] {
				x,
				y,
				z
		};
		this.setEnable(enabled);
	}
	
	/**
	 * Liefert die Punktnummer 
	 * @return pointId
	 */
	public String getPointId() {
		return this.pointId;
	}
	
	/**
	 * Liefert die X-Koordinate
	 * @return x
	 */
	public Double getX() {
		return this.coordinates[0];
	}
	
	/**
	 * Liefert die Y-Koordinate
	 * @return y
	 */
	public Double getY() {
		return this.coordinates[1];
	}
	
	/**
	 * Liefert die Z-Koordinate
	 * @return z
	 */
	public Double getZ() {
		return this.coordinates[2];
	}
	
	/**
	 * Liefert die Standardabweichung der X-Koordinate
	 * @return x
	 */
	public Double getStdX() {
		return this.sigma[0];
	}
	
	/**
	 * Liefert die Standardabweichung der Y-Koordinate
	 * @return y
	 */
	public Double getStdY() {
		return this.sigma[1];
	}
	
	/**
	 * Liefert die Standardabweichung der Z-Koordinate
	 * @return z
	 */
	public Double getStdZ() {
		return this.sigma[2];
	}
	
	/**
	 * Liefert vTPv
	 * @return omega
	 */
	public double getOmega() {
		return this.omega;
	}
	
	/**
	 * Liefert die Redundanz der X-Koordinate
	 * @return rx
	 */
	public Double getRedundanceX() {
		return this.redundance[0];
	}
	
	/**
	 * Liefert die Redundanz der Y-Koordinate
	 * @return ry
	 */
	public Double getRedundanceY() {
		return this.redundance[1];
	}
	
	/**
	 * Liefert die Redundanz der Z-Koordinate
	 * @return rz
	 */
	public Double getRedundanceZ() {
		return this.redundance[2];
	}
	
	/**
	 * Liefert Nabla der X-Koordinate
	 * @return nablaX
	 */
	public Double getNablaX() {
		return this.nabla[0];
	}
	
	/**
	 * Liefert Nabla der Y-Koordinate
	 * @return nablaY
	 */
	public Double getNablaY() {
		return this.nabla[1];
	}
	
	/**
	 * Liefert Nabla der Z-Koordinate
	 * @return nablaZ
	 */
	public Double getNablaZ() {
		return this.nabla[2];
	}
	
	/**
	 * Liefert die Verbesserung von der  X-Koordinate
	 * @return vx
	 */
	public Double getErrorX() {
		return this.error[0];
	}
	
	/**
	 * Liefert die Verbesserung von der  Y-Koordinate
	 * @return vy
	 */
	public Double getErrorY() {
		return this.error[1];
	}
	
	/**
	 * Liefert die Verbesserung von der Z-Koordinate
	 * @return vz
	 */
	public Double getErrorZ() {
		return this.error[2];
	}
	
	/**
	 * Liefert die Testgroesse Tprio
	 * @return t
	 */
	public double getTprio() {
		return this.testValues[0];
	}
	
	/**
	 * Liefert die Testgroesse Tpost
	 * @return t
	 */
	public double getTpost() {
		return this.testValues[1];
	}
	
	/**
	 * Liefert <code>true</code>, wenn der Punkt ein Ausreiﬂer ist 
	 * @return outlier
	 */
	public boolean isOutlier() {
		return this.outlier;
	}
	
	/**
	 * Setzt die Punktnummer
	 * @param pointId
	 */
	public void setPointId(String pointId) {
		this.pointId = pointId;
	}
		
	/**
	 * Setzt die X-Koordinate
	 * @param x
	 */
	public void setX(Double x) {
		this.coordinates[0] = x;
	}
	
	/**
	 * Setzt die Y-Koordinate
	 * @param y
	 */
	public void setY(Double y) {
		this.coordinates[1] = y;
	}
	
	/**
	 * Setzt die Z-Koordinate
	 * @param z
	 */
	public void setZ(Double z) {
		this.coordinates[2] = z;
	}
	
	/**
	 * Setzt die Standardabweichung der X-Koordinate
	 * @param sigmaX
	 */
	public void setStdX(Double sigmaX) {
		this.sigma[0] = sigmaX;
	}
	
	/**
	 * Setzt die Standardabweichung der Y-Koordinate
	 * @param sigmaY
	 */
	public void setStdY(Double sigmaY) {
		this.sigma[1] = sigmaY;
	}
	
	/**
	 * Setzt die Standardabweichung der Z-Koordinate
	 * @param sigmaZ
	 */
	public void setStdZ(Double sigmaZ) {
		this.sigma[2] = sigmaZ;
	}
	
	/**
	 * Setzt die Redundanz der X-Koordinate
	 * @param rx
	 */
	public void setRedundanceX(double rx) {
		this.redundance[0] = rx;
	}
	
	/**
	 * Setzt die Redundanz der Y-Koordinate
	 * @param ry
	 */
	public void setRedundanceY(double ry) {
		this.redundance[1] = ry;
	}
	
	/**
	 * Setzt die Redundanz der Z-Koordinate
	 * @param rz
	 */
	public void setRedundanceZ(double rz) {
		this.redundance[2] = rz;
	}
	
	/**
	 * Setzt die Verbesserung der X-Koordinate
	 * @param vx
	 */
	public void setErrorX(double vx) {
		this.error[0] = vx;
	}
	
	/**
	 * Setzt die Verbesserung der Y-Koordinate
	 * @param vy
	 */
	public void setErrorY(double vy) {
		this.error[1] = vy;
	}
	
	/**
	 * Setzt die Verbesserung der Z-Koordinate
	 * @param vz
	 */
	public void setErrorZ(double vz) {
		this.error[2] = vz;
	}
	
	/**
	 * Setzt Nabla der X-Koordinate
	 * @param nx
	 */
	public void setNablaX(double nz) {
		this.nabla[0] = nz;
	}
	
	/**
	 * Setzt Nabla der Y-Koordinate
	 * @param ny
	 */
	public void setNablaY(double ny) {
		this.nabla[1] = ny;
	}
	
	/**
	 * Setzt Nabla der Z-Koordinate
	 * @param nz
	 */
	public void setNablaZ(double nz) {
		this.nabla[2] = nz;
	}
	
	/**
	 * Legt fest, ob der Punkt ein Ausreiﬂer ist 
	 * @param outlier
	 */
	public void setOutlier(boolean outlier) {
		this.outlier = outlier;
	}
	
	/**
	 * Setzt die Testgroesse Tprio
	 * @param t
	 */
	public void setTprio(double t) {
		this.testValues[0] = t;
	}
	
	/**
	 * Setzt die Testgroesse Tpost
	 * @param t
	 */
	public void setTpost(double t) {
		this.testValues[1] = t;
	}
	
	/**
	 * Setzt vTPv
	 * @param omega
	 */
	public void setOmega(double omega) {
		this.omega = omega;
	}
	
	@Override
	public boolean isComplete() {
		for (int i=0; i<this.coordinates.length; i++) {
			if (this.coordinates[i] == null)
				return false;
		}
		
		return this.pointId != null && !this.pointId.trim().isEmpty();
	}

}