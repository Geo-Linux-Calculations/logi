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

import com.derletztekick.geodesy.logi.sql.DataManager;

public class CoVarFileReader extends RowFileReader {
	//private double coVar[][] = null;
	private int row = 0, size = -1;

	public CoVarFileReader(int groupId, DataManager dataManager, File srcFile, int size) {
		super(groupId, dataManager, srcFile);
		this.initMatrix(size);
	}
	
	private void initMatrix(int size) {
		if (size > 0)
			//this.coVar = new double[size][];
			this.size = size;
	}

	@Override
	public void parse(String line) {
		String columns[] = line.trim().split("[\\s;]+");
		if (this.size <= 0)
			this.initMatrix(columns.length);
		
		if (columns.length >= this.size-this.row && this.size > this.row) {
			double rowData[] = this.doubleArray(columns);
			if (rowData == null)
				return;
			
			// nur obere Dreiecksmatrix speichern
			// Einlesen von hinten, da unklar, ob in der Datei die gedsamte oder 
			// nur die obere Dreiecksmatrix enthalten war
			DataManager dataManager = this.getDataManager();
			dataManager.saveGlobalCovarianceMatrixApriori(this.row, this.row, rowData);
//			this.coVar[this.row] = new double[this.coVar.length-this.row];
//			for (int i=0; i<this.coVar.length-this.row; i++) {
//				this.coVar[this.row][this.coVar.length-this.row-1-i] = rowData[rowData.length-1-i];
//			}
			this.row++;
		}
	}
	
	private double[] doubleArray(String strArr[]) {
		if (strArr.length >= this.size-this.row) {
			double d[] = new double[this.size-this.row];
			for (int i=0; i<this.size-this.row; i++) {
				try {
					d[this.size-this.row-1-i] = Double.parseDouble(strArr[strArr.length-1-i].trim());
				} 
				catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					return null;
				}
			}
			return d;
		}
		return null;
	}

	@Override
	public int getRowCounter() {
		return this.row;
	}

//	private double[] doubleArray(String strArr[]) {
//		double d[] = new double[strArr.length];
//		for (int i=0; i<d.length; i++) {
//			try {
//				d[i] = Double.parseDouble(strArr[i].trim());
//			} 
//			catch (NumberFormatException nfe) {
//				nfe.printStackTrace();
//				return null;
//			}
//		}
//		return d;
//	}
}
