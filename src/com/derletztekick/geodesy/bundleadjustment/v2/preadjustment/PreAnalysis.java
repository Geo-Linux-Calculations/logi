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

package com.derletztekick.geodesy.bundleadjustment.v2.preadjustment;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PolarPointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.LasertrackerUncertaintyModel;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.LasertrackerUncertaintyModelMCS;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.LasertrackerUncertaintyModelUT;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.UncertaintyModel;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.correlation.CorrelationFunction;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.correlation.ExponentialCosineCorrelationFunction;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.correlation.GaussianlCosineCorrelationFunction;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.PolarPoint3D;
import com.derletztekick.tools.sql.DataBase;
import com.derletztekick.tools.geodesy.Constant;
import com.derletztekick.tools.geodesy.MathExtension;

public class PreAnalysis implements Runnable {
	private boolean isInterrupted = false;
	private final DataBase db;
	private int uncertaintyModelType    = PreAnalysis.UNCERTAINTY_MODEL_UNSCENTED_TRANSFORMATION;
	private int moneCarloSamples        = PreAnalysis.MONTE_CARLO_SAMPLES;
	public final static int DISTRIBUTION_NORMAL     = 1,
							DISTRIBUTION_UNIFORM    = 2,
							DISTRIBUTION_TRIANGULAR = 3,
							UNCERTAINTY_MODEL_PROPAGATION_OF_UNCERTAINTY = 10,
							UNCERTAINTY_MODEL_MONTE_CARLO_SIMULATION     = 20,
							UNCERTAINTY_MODEL_UNSCENTED_TRANSFORMATION   = 30,
							MONTE_CARLO_SAMPLES = 5000;
	private PropertyChangeSupport changes = new PropertyChangeSupport( this );
	
	public PreAnalysis(DataBase db) {
		this.db = db;
	}
	
	/**
	 * Vorprozessierung der Daten; bestimmt aus Polarelementen lokale Koordinaten
	 * und leitet die Varianz-Kovarianz-Matrix ab
	 * 
	 * @return isAnalysed
	 */
	public boolean analyse() {
		boolean isConnected = false;
		try {
			isConnected = this.db.isOpen();
			if (!isConnected)
				this.db.open();
				
			this.removeLocalSystemPointsApriori();
			this.identifyUncertaintyModelName();
			
			this.deriveGlobalCovarianceMatrix();
			this.deriveLocalCovarianceMatrices();
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			this.changes.firePropertyChange( "PRE_ANALYSIS_FAILD_NO_DATABASE", null, e );
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			this.changes.firePropertyChange( "PRE_ANALYSIS_FAILD_COULD_NOT_CONNECT_TO_DATABASE", null, e );
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			this.changes.firePropertyChange( "PRE_ANALYSIS_FAILD_SQL_ERROR", null, e );
			return false;
		} finally {
			if (!isConnected && this.db != null)
				this.db.close();
		}
		if (this.isInterrupted) {
			this.isInterrupted = false;
			this.changes.firePropertyChange( "TRANSFORMATION_INTERRUPT", false, true );
			return false;
		}
		else {
			this.changes.firePropertyChange( "PRE_ANALYSIS_SUCCESSFULLY_FINISHED", false, true );
			return true;
		}
	}
	
	/**
	 * Entfernt alle Koordinaten aus der Tabelle PointApriori, die <strong>NICHT</strong> 
	 * zur globalen Gruppe gehoeren.
	 * 
	 * @throws SQLException
	 */
	private void removeLocalSystemPointsApriori() throws SQLException {
		String sqlGroup = "SELECT \"id\" FROM \"PointGroup\" WHERE \"target_system\" = TRUE";
		String sqlPoint = "DELETE FROM \"PointApriori\" WHERE \"group_id\" != ?";
			
		PreparedStatement statement = this.db.getPreparedStatement(sqlGroup);
		ResultSet groupSet = statement.executeQuery();
		// Bestimme ID der globalen Gruppe
		if (!groupSet.wasNull() && groupSet.next()) {
			int groupId = groupSet.getInt("id");
			// Loesche alle Punkte ausser dem globalen System
			statement = this.db.getPreparedStatement(sqlPoint);
			statement.setInt(1, groupId);
			statement.execute();
		}
	}
	
	/**
	 * Ermittelt den Modellnamen zur Ermittlung der Messunsicherheiten fuer
	 * die lokalen (polaren) Subsysteme.
	 * 
	 * @throws SQLException
	 */
	private void identifyUncertaintyModelName() throws SQLException {
		String sqlGroup = "SELECT \"stochastic_model\", \"monte_carlo_samples\" FROM \"GeneralSetting\" WHERE \"id\" = 1";
		PreparedStatement statement = this.db.getPreparedStatement(sqlGroup);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			this.uncertaintyModelType = groupSet.getInt("stochastic_model");
			this.moneCarloSamples     = groupSet.getInt("monte_carlo_samples");
		}
	}
	
	private CorrelationFunction getCorrelationFunctionOfPointGroup(int groupId, int observationType) throws SQLException {
		String sqlGroup = "SELECT \"corr_fun\",\"sigma\",\"max_dt\",\"coef_a\",\"coef_b\",\"coef_c\" FROM \"CorrelationFunctionParameter\" WHERE \"group_id\" = ? AND \"obs_type\" = ?";
		PreparedStatement statement = this.db.getPreparedStatement(sqlGroup);
		statement.setInt(1, groupId);
		statement.setInt(2, observationType);
		ResultSet groupSet = statement.executeQuery();
		double scale = observationType == LasertrackerUncertaintyModel.DISTANCE3D ? 1.0 : Constant.RHO_GRAD2RAD; 
		if (!groupSet.wasNull() && groupSet.next()) {
			int corrFunType = groupSet.getInt("corr_fun");
			
			double sigma = groupSet.getDouble("sigma")  *  scale;
			double maxDT = groupSet.getDouble("max_dt");
			double a     = groupSet.getDouble("coef_a");
			double b     = groupSet.getDouble("coef_b");
			double c     = groupSet.getDouble("coef_c");
			
			if (corrFunType == CorrelationFunction.EXP_COS_CORR_FUN)
				return new ExponentialCosineCorrelationFunction(sigma, maxDT, a, b, c);
			else if(corrFunType == CorrelationFunction.GAUSSIAN_CORR_FUN)
				return new GaussianlCosineCorrelationFunction(sigma, maxDT, a, b, c);
		}
		return null;
	}
	
	/**
	 * Bestimmt die globale Kovarianzmatrix der <em>aktiven</em> Punkte im globalen System.
	 * 
	 * @throws SQLException
	 */
	private void deriveGlobalCovarianceMatrix() throws SQLException {
		String sqlFreenet = "SELECT \"freenet\" FROM \"GeneralSetting\" WHERE \"id\" = 1";
		String sqlGroup   = "SELECT \"id\", \"sigma2aprio\" FROM \"PointGroup\" WHERE \"target_system\" = TRUE";
		String sqlPoint   = "SELECT \"enable\" FROM \"PointApriori\" WHERE \"group_id\" = ? ORDER BY \"id\" ASC";
		
		// Freie Netzausgleichung erfordert keine Covar im Globalen System
		PreparedStatement statement = this.db.getPreparedStatement(sqlFreenet);
		ResultSet result = statement.executeQuery();
		if (!result.wasNull() && result.next() && result.getBoolean("freenet")) {
			return;
		}
		
		
		Set<Integer> indices = new LinkedHashSet<Integer>();
	
		statement = this.db.getPreparedStatement(sqlGroup);
		result = statement.executeQuery();
		// Bestimme ID der globalen Gruppe
		if (!result.wasNull() && result.next()) {
			int groupId = result.getInt("id");
			this.changes.firePropertyChange( "PRE_ANALYSIS_ESTIMATE_GLOBAL_UNCERTAINTY", false, true );
			statement = this.db.getPreparedStatement(sqlPoint);
			statement.setInt(1, groupId); // Id der Gruppe
			double sigma2aprio = result.getDouble("sigma2aprio"); // Bestimme den Varianzfaktor a-priori, mit dem die CoVar skaliert wird
			result = statement.executeQuery();
			// Bestimme Punkte der globalen Gruppe und ermittle Indizes
			// zur Extraktion der zugehoerigen CoVar
			if (!result.wasNull()) {
				int rowPoint = 0;
				while(result.next()) {
					boolean enable = result.getBoolean("enable");
					// Speichere die Indizes der Punkte, die aktiv sind
					if (enable) {
						indices.add(rowPoint);
						indices.add(rowPoint+1);
						indices.add(rowPoint+2);
					}
					rowPoint+=3;
				}
				// Ermittle vollständige CoVar
				Matrix Cllcomplete = this.getGlobalCovarianceMatrix();

				// Bestimme die reduzierte CoVar aus aktiven Punkten
				int size = indices.size();
				Matrix Cll = null;

				if (Cllcomplete == null || Cllcomplete.numColumns() < size) {
					Cll = new UpperSymmPackMatrix(MathExtension.identity(size));
					System.err.println(this.getClass().getSimpleName()+" Kovarianzmatrix a-priori des globalen Systems nicht vorhanden, nutze Einheitsmatrix. " + Cllcomplete);
				}
				else {
					Cll = new UpperSymmPackMatrix(size);
					int r=0, c=0;
					for ( Integer row : indices ) {
						r = 0;
						for ( Integer col : indices ) {
							Cll.set(r, c, Cllcomplete.get(row, col));
							r++;
						}
						c++;
					}
				}
				// Erweitere um sigma2aprio
				if (sigma2aprio != 1.0)
					Cll.scale(sigma2aprio);
				// Speichere CoVar zur Gruppe
				this.saveCovarianceMatrix(groupId, Cll);
			}
		}
	}
	
	/**
	 * Bestimmt die globale Kovarianzmatrix der <em>aktiven</em> Punkte in den lokalen Systemen.
	 * Ermittelt dabei die Matrix nachdem Varianz-Kovarianz-Fortpflanzungsgesetz anhand des
	 * gewaehlten Schemas, welches mit <code>identifyUncertaintyModelName()</code> ermittelt wurde.
	 * 
	 * @see identifyUncertaintyModelName()
	 * @throws SQLException
	 */
	private void deriveLocalCovarianceMatrices() throws SQLException {
		String sqlGroup = "SELECT \"id\", \"point_id\", \"sigma2aprio\" FROM \"PointGroup\" WHERE \"target_system\" = FALSE AND \"enable\" = TRUE ORDER BY \"id\" ASC";
		PreparedStatement statement = this.db.getPreparedStatement(sqlGroup);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull()) {
			while(groupSet.next()) {
				// Bestimme ID der Gruppe
				int groupId = groupSet.getInt("id");
				// Bestimme Standpunktnummer
				String startPointId = groupSet.getString("point_id");
				// Bestimme den Varianzfaktor a-priori, mit dem die CoVar skaliert wird
				double sigma2aprio = groupSet.getDouble("sigma2aprio");
				// Bestimme die Unsicherheiten der Gruppe fuer Polarelemente
				double sigmas[] = this.getUncertaintyValues(groupId);
				// Bestimme die Wahrscheinlichkeitsfunktionen der Parameter
				int distributionTypes[] = this.getUncertaintyDistributions(groupId);
				
				this.changes.firePropertyChange( "PRE_ANALYSIS_ESTIMATE_LOCAL_UNCERTAINTY", null, startPointId );
				
				// Erzeuge Varianzen
				double diagCll[] = null;
				if (sigmas != null) {
					diagCll = new double[sigmas.length];
					for (int i=0; i<diagCll.length; i++) 
						diagCll[i] = sigmas[i]*sigmas[i];
				}
				
				// Erstelle die Korrelationsfunktion der Beobachtungstypen
				CorrelationFunction distanceCorrelationFunction = this.getCorrelationFunctionOfPointGroup(groupId, LasertrackerUncertaintyModel.DISTANCE3D);
				CorrelationFunction azimuthCorrelationFunction  = this.getCorrelationFunctionOfPointGroup(groupId, LasertrackerUncertaintyModel.AZIMUTHANGLE);
				CorrelationFunction zenithCorrelationFunction   = this.getCorrelationFunctionOfPointGroup(groupId, LasertrackerUncertaintyModel.ZENITHANGLE);

				// Erstelle eine Gruppe
				UncertaintyModel uncertaintyModel = null;
				
				if (this.uncertaintyModelType == PreAnalysis.UNCERTAINTY_MODEL_MONTE_CARLO_SIMULATION)
					uncertaintyModel = new LasertrackerUncertaintyModelMCS(this.moneCarloSamples, diagCll, distributionTypes, distanceCorrelationFunction, azimuthCorrelationFunction, zenithCorrelationFunction);
				else if (this.uncertaintyModelType == PreAnalysis.UNCERTAINTY_MODEL_PROPAGATION_OF_UNCERTAINTY)
					uncertaintyModel = new LasertrackerUncertaintyModel(diagCll, distanceCorrelationFunction, azimuthCorrelationFunction, zenithCorrelationFunction);
				else //if (this.uncertaintyModelType == PreAnalysis.UNCERTAINTY_MODEL_UNSCENTED_TRANSFORMATION)
					uncertaintyModel = new LasertrackerUncertaintyModelUT(diagCll, distanceCorrelationFunction, azimuthCorrelationFunction, zenithCorrelationFunction);

				PolarPointGroup pointGroup = new PolarPointGroup(groupId, startPointId, uncertaintyModel);
				// Hole alle aktiven Punkte dieser Gruppe
				List<PolarPoint3D> polarPoints = this.getPolarPoints(groupId);
				for ( PolarPoint3D point : polarPoints ) {
					pointGroup.add(point);
				}

				// Bestimme Kovarianzmatrix dieser Gruppe
				pointGroup.deriveCovarianceMatrix();
				// Hole die abgeleitete CoVar
				Matrix Cll = pointGroup.getCovarianceMatrix();
				// Erweitere um sigma2aprio
				if (sigma2aprio != 1.0)
					Cll.scale(sigma2aprio);
				
//				System.out.println(this.getClass().getSimpleName());
//				MathExtension.print(Cll);
//				System.out.println();
				
				
				this.saveCovarianceMatrix(groupId, Cll);
				// Speichere kart. Punkte
				this.savePoints(pointGroup);
				
				if (this.isInterrupted) {
					return;
				}
			}
		}
	}
	
	/**
	 * Speichert die kart. Punkte einer Gruppe. Alle Punkte werden als <em>aktiv</em> gesetzt.
	 * 
	 * @param pointGroup
	 * @throws SQLException
	 */
	private void savePoints(PointGroup pointGroup) throws SQLException {
		String sql = "INSERT INTO \"PointApriori\" (\"group_id\",\"point_id\",\"x0\",\"y0\",\"z0\",\"enable\") VALUES (?,?,?,?,?,?)";
		PreparedStatement statement = null;
		for (int i=0; i<pointGroup.size(); i++) {
			Point point = pointGroup.get(i);
			statement = this.db.getPreparedStatement(sql);
			statement.setInt(1, pointGroup.getId()); // id der Gruppe
			statement.setString(2, point.getId()); // Punktnummer
			statement.setDouble(3, point.getX());  // X-Wert
			statement.setDouble(4, point.getY());  // Y-Wert
			statement.setDouble(5, point.getZ());  // Z-Wert
			statement.setBoolean(6, true);         // Aktiv
			statement.executeUpdate();
		}	
	}
	
	/**
	 * Holt die globale Kovarianzmatrix aus den GlobalSettings. Diese Matrix enthaelt noch
	 * alle Punkte des globalen Systems unabhaengig davon, ob der Punkt aktiviert wurde.
	 * 
	 * @return Cll<sub>complete</sub>
	 * @throws SQLException
	 */
	private Matrix getGlobalCovarianceMatrix() throws SQLException {
		Matrix Cll = null;
		String sqlMax   = "SELECT MAX(\"row\") AS \"max_row\", MAX(\"column\") AS \"max_column\" FROM  \"CovarianceMatrix\" WHERE \"id\" = -1";
		String sqlCovar = "SELECT \"row\", \"column\", \"value\" FROM \"CovarianceMatrix\" WHERE \"id\" = -1";

		int size = -1;
		// ermittle Dimension der symm. Matrix
		PreparedStatement statement = this.db.getPreparedStatement(sqlMax);
		ResultSet rs = statement.executeQuery();
		if (!rs.wasNull() && rs.next()) {
			size = Math.max(rs.getInt("max_row"), rs.getInt("max_column"));
			if (size <= 0)
				return null;
			size += 1;
			statement = this.db.getPreparedStatement(sqlCovar);
			rs = statement.executeQuery();
			if (size > 0 && !rs.wasNull()) {
				Cll = new UpperSymmPackMatrix(size);
				while (rs.next()) {
					int row = rs.getInt("row");
					int col = rs.getInt("column");
					double value = rs.getDouble("value");
					Cll.set(row, col, value);
					Cll.set(col, row, value);
				}
			}
		}
		return Cll;
	}
	
//	/**
//	 * Holt die globale Kovarianzmatrix aus den GlobalSettings. Diese Matrix enthaelt noch
//	 * alle Punkte des globalen Systems unabhaengig davon, ob der Punkt aktiviert wurde.
//	 * 
//	 * @return Cll<sub>complete</sub>
//	 * @throws SQLException
//	 */
//	private Matrix getGlobalCovarianceMatrix() throws SQLException {
//		String sql = "SELECT \"covar\" FROM \"GeneralSetting\" WHERE \"id\" = 1";
//		PreparedStatement statement = this.db.getPreparedStatement(sql);
//		ResultSet groupSet = statement.executeQuery();
//		Matrix Cll = null;
//		if (!groupSet.wasNull() && groupSet.next()) {
//			String serialMatrix = groupSet.getString("covar");
//			Cll = MathExtension.getDeserializedMatrix(serialMatrix);
//		}
//		return Cll;
//	}
	
	/**
	 * Speichert die Kovarianzmarix einer Gruppe bei den Gruppeneinstellungen (GlobalGroup).
	 * Diese Matrix sollte die Dimension der aktiven Punkte haben. Eine Reduktion sollte 
	 * somit bereits erfolgt sein!
	 * 
	 * @param groupId
	 * @param Cll
	 * @throws SQLException
	 */
//	private void saveCovarianceMatrix(int groupId, Matrix Cll) throws SQLException {
//		String sql = "UPDATE \"PointGroup\" SET \"covar\" = ? WHERE \"id\" = ?";
//		PreparedStatement statement = this.db.getPreparedStatement(sql);
//		String serialMatrix = MathExtension.getSerializedMatrix(Cll);
//		statement.setString(1, serialMatrix==null?"":serialMatrix);
//		statement.setInt(2, groupId);
//		statement.executeUpdate();
//	}
	
	public void saveCovarianceMatrix(int groupId, Matrix Cll) {
		try {
			this.deleteCovarianceMatrix(groupId);
			
			String sqlMatrixInsert = "INSERT INTO \"CovarianceMatrix\" (\"id\", \"row\", \"column\", \"value\") VALUES (?,?,?,?)";
//			String sqlMatrixDelete = "DELETE FROM \"CovarianceMatrix\" WHERE \"id\" = ? AND \"row\" = ? AND \"column\" = ?";
			PreparedStatement statementInsert = this.db.getPreparedStatement(sqlMatrixInsert);
//			PreparedStatement statementDelete = this.db.getPreparedStatement(sqlMatrixDelete);
			
			for (int row=0; row<Cll.numRows(); row++) {
				for (int column=row; column<Cll.numColumns(); column++) {					
//					// Loesche zunachst betreffliche Datensaetze
//					statementDelete.setInt(1, groupId);
//					statementDelete.setInt(2, row);
//					statementDelete.setInt(3, column);
//					statementDelete.execute();

					// Fuege neue Datensaetze ein
					statementInsert.setInt(1, groupId);
					statementInsert.setInt(2, row);
					statementInsert.setInt(3, column);
					statementInsert.setDouble(4, Cll.get(row, column));
					statementInsert.execute();
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Loescht die Covar. Dies muss sequenziell erfolgen, das die gefahr eines OOM-Fehlers besteht beim Loeschen der gesamten Matrix.
	 * Mit 1000000 Werten pro Loeschvorgang scheint er aber leben zu koennen.
	 * 
	 * @param matrixId
	 */
	private void deleteCovarianceMatrix(int groupId) {
		String sqlCount = "SELECT MAX(\"row\") AS \"max_row\", MAX(\"column\") AS \"max_col\" FROM \"CovarianceMatrix\" WHERE \"id\" = ?";
		String sqlDel   = "DELETE FROM \"CovarianceMatrix\" WHERE \"id\" = ? AND \"row\" <= ?";
		try {
			PreparedStatement statement = this.db.getPreparedStatement(sqlCount);
			statement.setInt(1, groupId);
			ResultSet group = statement.executeQuery();

			if (!group.wasNull() && group.next()) {
				int maxRow = group.getInt("max_row") + 1;
				int maxCol = group.getInt("max_col") + 1;
				
				int max = Math.max(maxRow,maxCol);
				int delta = 1000000/max;
				delta++;
				
				statement = this.db.getPreparedStatement(sqlDel);
				statement.setInt(1, groupId);
				for (int row=0; row<=max; row+=delta) {
					statement.setInt(2, row);
					statement.execute();
				}
				statement.setInt(2, max);
				statement.execute();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Liefert die polaren Punkte einer bestimmten Gruppe.
	 * 
	 * @param groupId
	 * @return
	 * @throws SQLException
	 */
	private List<PolarPoint3D> getPolarPoints(int groupId) throws SQLException {
		List<PolarPoint3D> polarPoints = new ArrayList<PolarPoint3D>();
		String sqlPoint = "SELECT \"point_id\", \"distance\", \"azimuth\", \"zenith\", \"date\" FROM \"PolarObservation\" WHERE \"group_id\" = ? AND \"enable\" = TRUE ORDER BY \"id\" ASC";
		PreparedStatement statement = this.db.getPreparedStatement(sqlPoint);
		statement.setInt(1, groupId);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull()) {
			while(groupSet.next()) {
				String pointId  = groupSet.getString("point_id");
				double distance = groupSet.getDouble("distance");
				double azimuth  = groupSet.getDouble("azimuth")*Constant.RHO_GRAD2RAD;
				double zenith   = groupSet.getDouble("zenith")*Constant.RHO_GRAD2RAD;
				Timestamp ts    = groupSet.getTimestamp("date");
				Date obsDate    = null;
				if (!groupSet.wasNull()) 
					obsDate = new Date(ts.getTime());
				PolarPoint3D polarPoint = new PolarPoint3D(pointId, distance, azimuth, zenith, obsDate);
				if (polarPoint != null)
					polarPoints.add(polarPoint);
			}
		}
		return polarPoints;
	}
	
	/**
	 * Liefert ein Array mit den a-priori Werten der Varianzmatrix, die zur Ableitung
	 * der Messunsicherheiten verwendet werden.
	 * 
	 * @param groupId
	 * @return uncertaintyValues
	 * @throws SQLException
	 */
	private double[] getUncertaintyValues(int groupId) throws SQLException {
		double sigmas[] = null;
		
		String sqlStochModel = "SELECT \"sigma_x_instrument\",\"sigma_y_instrument\",\"sigma_z_instrument\",\"sigma_scale_instrument\",\"sigma_add_instrument\",\"sigma_fourier_aa1_instrument\",\"sigma_fourier_aa2_instrument\",\"sigma_fourier_ba1_instrument\",\"sigma_fourier_ba2_instrument\",\"sigma_fourier_ae0_instrument\",\"sigma_fourier_ae1_instrument\",\"sigma_fourier_ae2_instrument\",\"sigma_fourier_be1_instrument\",\"sigma_fourier_be2_instrument\",\"sigma_alpha_instrument\",\"sigma_gamma_instrument\",\"sigma_ex_instrument\",\"sigma_by_instrument\",\"sigma_bz_instrument\",\"sigma_scale_target\",\"sigma_add_target\",\"sigma_azimuth_target\",\"sigma_zenith_target\",\"sigma_centring_azimuth_target\",\"sigma_centring_zenith_target\" FROM \"LasertrackerUncertaintyParameter\" WHERE \"id\" = ?";
		
		PreparedStatement statement = this.db.getPreparedStatement(sqlStochModel);
		statement.setInt(1, groupId);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			sigmas = new double[] { 
					groupSet.getDouble("sigma_x_instrument"), 
					groupSet.getDouble("sigma_y_instrument"), 
					groupSet.getDouble("sigma_z_instrument"),
					groupSet.getDouble("sigma_scale_instrument"), 
					groupSet.getDouble("sigma_add_instrument"), 
					groupSet.getDouble("sigma_fourier_aa1_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_fourier_aa2_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_fourier_ba1_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_fourier_ba2_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_fourier_ae0_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_fourier_ae1_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_fourier_ae2_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_fourier_be1_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_fourier_be2_instrument")*Constant.RHO_GRAD2RAD,
					groupSet.getDouble("sigma_alpha_instrument")*Constant.RHO_GRAD2RAD, 
					groupSet.getDouble("sigma_gamma_instrument")*Constant.RHO_GRAD2RAD,
					groupSet.getDouble("sigma_ex_instrument"), 
					groupSet.getDouble("sigma_by_instrument"), 
					groupSet.getDouble("sigma_bz_instrument"), 
					groupSet.getDouble("sigma_scale_target"), 
					groupSet.getDouble("sigma_add_target"),
					groupSet.getDouble("sigma_azimuth_target")*Constant.RHO_GRAD2RAD,
					groupSet.getDouble("sigma_zenith_target")*Constant.RHO_GRAD2RAD,
					groupSet.getDouble("sigma_centring_azimuth_target"), 
					groupSet.getDouble("sigma_centring_zenith_target")
			};
		}
		
		return sigmas;
	}
	
	/**
	 * Liefert ein Array mit Wahrscheinlichkeitsfunktionen der Parameter
	 * 
	 * @param groupId
	 * @return distributionTyps
	 * @throws SQLException
	 */
	private int[] getUncertaintyDistributions(int groupId) throws SQLException {
		int distTypes[] = null;
		
		String sqlDistModel = "SELECT \"distribution_x_instrument\",\"distribution_y_instrument\",\"distribution_z_instrument\",\"distribution_scale_instrument\",\"distribution_add_instrument\",\"distribution_fourier_aa1_instrument\",\"distribution_fourier_aa2_instrument\",\"distribution_fourier_ba1_instrument\",\"distribution_fourier_ba2_instrument\",\"distribution_fourier_ae0_instrument\",\"distribution_fourier_ae1_instrument\",\"distribution_fourier_ae2_instrument\",\"distribution_fourier_be1_instrument\",\"distribution_fourier_be2_instrument\",\"distribution_alpha_instrument\",\"distribution_gamma_instrument\",\"distribution_ex_instrument\",\"distribution_by_instrument\",\"distribution_bz_instrument\",\"distribution_scale_target\",\"distribution_add_target\",\"distribution_azimuth_target\",\"distribution_zenith_target\",\"distribution_centring_azimuth_target\",\"distribution_centring_zenith_target\" FROM \"DistributionUncertaintyParameter\" WHERE \"id\" = ?";
		
		PreparedStatement statement = this.db.getPreparedStatement(sqlDistModel);
		statement.setInt(1, groupId);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			distTypes = new int[] { 
					groupSet.getInt("distribution_x_instrument"), 
					groupSet.getInt("distribution_y_instrument"), 
					groupSet.getInt("distribution_z_instrument"),
					groupSet.getInt("distribution_scale_instrument"), 
					groupSet.getInt("distribution_add_instrument"), 
					groupSet.getInt("distribution_fourier_aa1_instrument"), 
					groupSet.getInt("distribution_fourier_aa2_instrument"), 
					groupSet.getInt("distribution_fourier_ba1_instrument"), 
					groupSet.getInt("distribution_fourier_ba2_instrument"), 
					groupSet.getInt("distribution_fourier_ae0_instrument"), 
					groupSet.getInt("distribution_fourier_ae1_instrument"), 
					groupSet.getInt("distribution_fourier_ae2_instrument"), 
					groupSet.getInt("distribution_fourier_be1_instrument"), 
					groupSet.getInt("distribution_fourier_be2_instrument"),
					groupSet.getInt("distribution_alpha_instrument"), 
					groupSet.getInt("distribution_gamma_instrument"),
					groupSet.getInt("distribution_ex_instrument"), 
					groupSet.getInt("distribution_by_instrument"), 
					groupSet.getInt("distribution_bz_instrument"), 
					groupSet.getInt("distribution_scale_target"), 
					groupSet.getInt("distribution_add_target"),
					groupSet.getInt("distribution_azimuth_target"),
					groupSet.getInt("distribution_zenith_target"),
					groupSet.getInt("distribution_centring_azimuth_target"), 
					groupSet.getInt("distribution_centring_zenith_target")
			};
		}
		
		return distTypes;
	}
		
	public void addPropertyChangeListener( PropertyChangeListener l ) {
		this.changes.addPropertyChangeListener( l );
	}

	public void removePropertyChangeListener( PropertyChangeListener l ) {
		this.changes.removePropertyChangeListener( l );
	}
	
	/**
	 * Bricht Iteration an der naechst moeglichen Stelle ab
	 */
	public void interrupt() {
		this.isInterrupted = true;
	}

	@Override
	public void run() {
		this.analyse();
	}
}
