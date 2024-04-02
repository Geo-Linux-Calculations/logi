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

package com.derletztekick.geodesy.bundleadjustment.v2.transformation.sql;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.GlobalPointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.PointGroup;
import com.derletztekick.geodesy.bundleadjustment.v2.transformation.Transformation;
import com.derletztekick.geodesy.bundleadjustment.v2.transformation.Transformation3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.GlobalPoint3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.point.Point3D;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet3D;

import com.derletztekick.tools.sql.DataBase;
import com.derletztekick.tools.sql.HSQLDB;

public class BundleAdjustmentProjectDatabase implements Runnable {
	public final static double DATABASE_VERSION = 1.07;
	final private int dim = 3;
	private boolean isFreeNetAdjustment = true,
					exportCoVar2WorkingDirectory = false,
					applyVarianceFactorAposteriori = true;
	private final DataBase projectDB;
	private double alpha = 0.1, beta = 80.0;
	private int maxIteration = 20;
	private Transformation transformation = null;
	private PropertyChangeSupport changes = new PropertyChangeSupport( this );
	
	public BundleAdjustmentProjectDatabase(DataBase db) {
		this.projectDB = db;
	}
	
	public Transformation getBundleTransformation() {
		this.transformation = null;
		boolean isConnected = this.projectDB.isOpen();
		try {
			if (!isConnected)
				this.projectDB.open();
			
			this.initGlobalSettings();
			GlobalPointGroup globalPointGroup = this.getGlobalPointGroup(this.isFreeNetAdjustment);
			List<PointGroup> localPointGroups = this.getLocalPointGroups();
		
			if (this.dim == 3 && globalPointGroup != null && localPointGroups != null && localPointGroups.size() > 0) {
				this.transformation = new Transformation3D();
				this.transformation.setFreeNetAdjustment(this.isFreeNetAdjustment);
				this.transformation.setMaxIteration(this.maxIteration);
				this.transformation.setProbabilityValue(this.alpha);
				this.transformation.setTestPowerValue(this.beta);
				this.transformation.applyVarianceFactorAposteriori(this.applyVarianceFactorAposteriori); 
					
				for (PropertyChangeListener l : this.changes.getPropertyChangeListeners())
					this.transformation.addPropertyChangeListener(l);
					
				if (!transformation.initializeSystems(localPointGroups, globalPointGroup))
					this.transformation = null;
			}
			
		} 
		catch (NullPointerException ex) {
			ex.printStackTrace();
			this.transformation = null;
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			this.transformation = null;
	    } 
		catch (ClassNotFoundException ex) {
	    	ex.printStackTrace();
	    	this.transformation = null;
	    }
		finally {
			if (!isConnected && this.projectDB != null)
				this.projectDB.close();
	    }

		return this.transformation;
	}
	
	/**
	 * Speichert die Ausgleichungsergebnisse
	 * 
	 * @return isSaved
	 */
	public boolean saveResults() {
		if (this.transformation == null)
			return false;
		this.changes.firePropertyChange( "BUNDLEADJUSTMENT_SAVE_RESULT", false, true );
		try {
			// Leere alle Ergebnistabellen
			this.truncateResultTables();
			
			// Speichere das globale System
			this.saveGlobalPointGroup(this.transformation.getTargetPointGroup());
			
			// Speichere alle (verwendeten) lokalen Systeme
			List<PointGroup> srcPointGroupt = this.transformation.getSourcePointGroups();
			for (PointGroup pointGroup : srcPointGroupt)
				this.saveLocalPointGroup(pointGroup);
			
			// Speichere globale Ausgleichungsergebnisse (Freiheitsgrad, Testgroessen, usw.)
			this.saveLeastSquareResult();
			
		} catch (SQLException e) {
			this.changes.firePropertyChange( "BUNDLEADJUSTMENT_DATA_SAVED_FAILED", null, e );
			e.printStackTrace();
			return false;
		}
		
		if (this.exportCoVar2WorkingDirectory && this.projectDB instanceof HSQLDB) {
			System.out.println(this.getClass().getSimpleName()+" Exportiere Varianz-Kovarianz-Matrix...");
			File coVarMatrixFile = new File(((HSQLDB)this.projectDB).getDataBaseBaseFileName() + ".cxx");
			File coVarInfoFile   = new File(((HSQLDB)this.projectDB).getDataBaseBaseFileName() + ".info");
			try {
				this.transformation.exportCovarianceMatrixToFile(coVarMatrixFile);
				this.transformation.exportCovarianceMatrixInfoToFile(coVarInfoFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	
	private void truncateResultTables() throws SQLException {
		String sqls[] = new String[] {
				"DELETE FROM \"LocalPointApost\"",
				"DELETE FROM \"GlobalPointApost\"",
				"DELETE FROM \"TransformationParameter\""
		};

		for (String sql : sqls) {
			PreparedStatement statement = this.projectDB.getPreparedStatement(sql);
			statement.execute();
		}
	}
	
	private void saveLeastSquareResult() throws SQLException {
		String sql = "UPDATE \"LeastSquareResult\" SET " +
					 "\"date\" = ?, \"t_global\" = ?, \"k_global\" = ?, \"k_prio\" = ?, \"k_post\" = ?, " +
					 "\"omega\" = ?, \"degree_of_freedom\" = ? " +
					 "WHERE \"id\" = 1";
		PreparedStatement statement = this.projectDB.getPreparedStatement(sql);
		statement.setString(1, String.valueOf(new Timestamp(System.currentTimeMillis())));
		statement.setDouble(2, this.transformation.getTglobal());
		statement.setDouble(3, this.transformation.getObservationTestValues().getKprioGlobal()<1?1.0:this.transformation.getObservationTestValues().getKprioGlobal());
		statement.setDouble(4, this.transformation.getObservationTestValues().getKprioAB(this.dim));
		statement.setDouble(5, this.transformation.getObservationTestValues().getKpostAB(this.dim));
		statement.setDouble(6, this.transformation.getOmega()/this.transformation.getVarianceFactorApriori());
		statement.setInt(7, this.transformation.degreeOfFreedom());
		
		statement.executeUpdate();
	}
	
	private void saveGlobalPointGroup(PointGroup pointGroup) throws SQLException {
		String sqlGroup = "UPDATE \"PointGroup\" SET " +
			"\"fixed_tx\" = ?, \"fixed_ty\" = ?, \"fixed_tz\" = ?, \"fixed_m\" = ?, " +
			"\"fixed_rx\" = ?, \"fixed_ry\" = ?, \"fixed_rz\" = ? " +
			"WHERE \"id\" = ?";
				
		TransformationParameterSet trafoSet = pointGroup.getTransformationParameterSet();
		PreparedStatement statement = this.projectDB.getPreparedStatement(sqlGroup);
		statement.setBoolean(1, trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X));
		statement.setBoolean(2, trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y));
		statement.setBoolean(3, trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z));
		statement.setBoolean(4, trafoSet.isRestricted(TransformationParameterSet.FIXED_SCALE));
		statement.setBoolean(5, trafoSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_X));
		statement.setBoolean(6, trafoSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Y));
		statement.setBoolean(7, trafoSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Z));
		statement.setInt(8, pointGroup.getId());
		
		// Update der Gruppe
		if (statement.executeUpdate()>0) {
			for (int i=0; i<pointGroup.size(); i++) {
				Point point = pointGroup.get(i);
				this.savePoint(true, pointGroup.getId(), point);
			}
		}	
	}
	
	private void saveLocalPointGroup(PointGroup pointGroup) throws SQLException {
		String sqlGroup = "INSERT INTO \"TransformationParameter\" (" +
				"\"tx\", \"ty\", \"tz\", \"m\", " +
				"\"q0\", \"q1\", \"q2\", \"q3\", " +
				"\"rx\", \"ry\", \"rz\", " +
				"\"sigma_tx\", \"sigma_ty\", \"sigma_tz\", \"sigma_m\", " +
				"\"sigma_q0\", \"sigma_q1\", \"sigma_q2\", \"sigma_q3\", " +
				"\"sigma_rx\", \"sigma_ry\", \"sigma_rz\", " +
				"\"significant_tx\", \"significant_ty\", \"significant_tz\", \"significant_m\", " +
				"\"significant_q0\", \"significant_q1\", \"significant_q2\", \"significant_q3\", " +
				"\"significant_rx\", \"significant_ry\", \"significant_rz\", \"id\") " +
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		int dim = pointGroup.getDimension();
		if (dim != 3)
			return;
		
		// Speichere ermittelte Trafoparameter der Gruppe
		TransformationParameterSet trafoSet = pointGroup.getTransformationParameterSet();

		PreparedStatement statement = this.projectDB.getPreparedStatement(sqlGroup);
		statement.setDouble( 1, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_X).getValue());
		statement.setDouble( 2, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_Y).getValue());
		statement.setDouble( 3, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_Z).getValue());
		statement.setDouble( 4, trafoSet.getTransformationParameter(TransformationParameter.TYPE_SCALE).getValue());
		statement.setDouble( 5, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q0).getValue());
		statement.setDouble( 6, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q1).getValue());
		statement.setDouble( 7, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q2).getValue());
		statement.setDouble( 8, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q3).getValue());
		
		statement.setDouble( 9, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_X).getValue());
		statement.setDouble(10, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_Y).getValue());
		statement.setDouble(11, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_Z).getValue());
		
		
		statement.setDouble(12, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_X).getStd());
		statement.setDouble(13, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_Y).getStd());
		statement.setDouble(14, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_Z).getStd());
		statement.setDouble(15, trafoSet.getTransformationParameter(TransformationParameter.TYPE_SCALE).getStd());
		statement.setDouble(16, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q0).getStd());
		statement.setDouble(17, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q1).getStd());
		statement.setDouble(18, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q2).getStd());
		statement.setDouble(19, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q3).getStd());
		
		statement.setDouble(20, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_X).getStd());
		statement.setDouble(21, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_Y).getStd());
		statement.setDouble(22, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_Z).getStd());
		
		statement.setBoolean(23, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_X).isSignificant());
		statement.setBoolean(24, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_Y).isSignificant());
		statement.setBoolean(25, trafoSet.getTransformationParameter(TransformationParameter.TYPE_TRANSLATION_Z).isSignificant());
		statement.setBoolean(26, trafoSet.getTransformationParameter(TransformationParameter.TYPE_SCALE).isSignificant());
		statement.setBoolean(27, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q0).isSignificant());
		statement.setBoolean(28, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q1).isSignificant());
		statement.setBoolean(29, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q2).isSignificant());
		statement.setBoolean(30, trafoSet.getTransformationParameter(TransformationParameter.TYPE_QUATERNION_Q3).isSignificant());
		
		statement.setBoolean(31, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_X).isSignificant());
		statement.setBoolean(32, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_Y).isSignificant());
		statement.setBoolean(33, trafoSet.getAdditionalTransformationParameter(TransformationParameter.TYPE_ROTATION_Z).isSignificant());
		
		statement.setInt(34, pointGroup.getId());
		
		// Update der Gruppe
		if (statement.executeUpdate()>0) {
			for (int i=0; i<pointGroup.size(); i++) {
				Point point = pointGroup.get(i);
				this.savePoint(false, pointGroup.getId(), point);
			}
		}	
	}
	
	private void savePoint(boolean isGlobalPoint, int groupId, Point point) throws SQLException{
		String tableName = isGlobalPoint?"GlobalPointApost":"LocalPointApost";

		String sqlPoint = "INSERT INTO \"" + tableName + "\" (" +
				(isGlobalPoint?"\"point_id\",":"\"id\",") +
				"\"x\",\"y\",\"z\"," +
				"\"sigma_x\",\"sigma_y\",\"sigma_z\"," +
				"\"nabla_x\",\"nabla_y\",\"nabla_z\"," +
				"\"redundance_x\",\"redundance_y\",\"redundance_z\"," +
				"\"omega\",\"t_prio\",\"t_post\",\"outlier\"" +
				(isGlobalPoint?",\"covar_index\"":"") +
				") VALUES (" +
				(isGlobalPoint?"?,":"(SELECT \"id\" FROM \"PointApriori\" WHERE \"point_id\" = ? AND \"group_id\" = ?), ") +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " + (isGlobalPoint?",?":"") + ")";
		
		PreparedStatement statement = this.projectDB.getPreparedStatement(sqlPoint);
		int index = 1;
		statement.setString(index++, point.getId());
		if (!isGlobalPoint)
			statement.setInt(index++, groupId);
		
		statement.setDouble(index++, this.dim != 1?point.getX():0.0);
		statement.setDouble(index++, this.dim != 1?point.getY():0.0);
		statement.setDouble(index++, this.dim != 2?point.getZ():0.0);
		
		statement.setDouble(index++, this.dim != 1?point.getStdX():0.0);
		statement.setDouble(index++, this.dim != 1?point.getStdY():0.0);
		statement.setDouble(index++, this.dim != 2?point.getStdZ():0.0);
		
		statement.setDouble(index++, this.dim != 1?point.getNablaX():0.0);
		statement.setDouble(index++, this.dim != 1?point.getNablaY():0.0);
		statement.setDouble(index++, this.dim != 2?point.getNablaZ():0.0);
					
		statement.setDouble(index++, this.dim != 1?point.getRedundancyX():0.0);
		statement.setDouble(index++, this.dim != 1?point.getRedundancyY():0.0);
		statement.setDouble(index++, this.dim != 2?point.getRedundancyZ():0.0);
			
		statement.setDouble(index++, point.getOmega());
		statement.setDouble(index++, point.getTprio());
		statement.setDouble(index++, point.getTpost());
							
		statement.setBoolean(index++, point.isOutlier());
		
		if (isGlobalPoint)
			statement.setInt(index++, point.getColInJacobiMatrix());
		
		statement.executeUpdate();
	}

	
	private void initGlobalSettings() throws SQLException {
		String sql = "SELECT \"freenet\", \"iteration\", \"alpha\", \"beta\", \"export_covar\", \"appy_sigma_apost\" FROM \"GeneralSetting\" WHERE \"id\" = 1";

		PreparedStatement statement = this.projectDB.getPreparedStatement(sql);
		
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			this.isFreeNetAdjustment = groupSet.getBoolean("freenet");
			this.maxIteration = groupSet.getInt("iteration");
			this.alpha = groupSet.getDouble("alpha");
			this.beta  = groupSet.getDouble("beta");
			this.exportCoVar2WorkingDirectory = groupSet.getBoolean("export_covar");
			this.applyVarianceFactorAposteriori = groupSet.getBoolean("appy_sigma_apost");
		}
	}
	
	private Matrix getCovarianceMatrix(int groupId) throws SQLException {
		Matrix Cll = null;
		String sqlMax   = "SELECT MAX(\"row\") AS \"max_row\", MAX(\"column\") AS \"max_column\" FROM  \"CovarianceMatrix\" WHERE \"id\" = ?";
		String sqlCovar = "SELECT \"row\", \"column\", \"value\" FROM \"CovarianceMatrix\" WHERE \"id\" = ?";

		int size = -1;
		// ermittle Dimension der symm. Matrix
		PreparedStatement statement = this.projectDB.getPreparedStatement(sqlMax);
		statement.setInt(1, groupId);
		ResultSet rs = statement.executeQuery();
		if (!rs.wasNull() && rs.next()) {
			size = Math.max(rs.getInt("max_row"), rs.getInt("max_column")) + 1;
			statement = this.projectDB.getPreparedStatement(sqlCovar);
			statement.setInt(1, groupId);
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
	
	private List<PointGroup> getLocalPointGroups() throws SQLException {
		
		String sqlGroup = "SELECT \"id\", \"fixed_tx\", \"fixed_ty\", \"fixed_tz\", \"fixed_m\", \"fixed_rx\", \"fixed_ry\", \"fixed_rz\" FROM \"PointGroup\" WHERE \"enable\" = TRUE AND \"target_system\" = FALSE ORDER BY \"id\" ASC";
		String sqlPoint = "SELECT \"point_id\", \"x0\", \"y0\", \"z0\" FROM \"PointApriori\" WHERE \"group_id\" = ? AND \"enable\" = TRUE ORDER BY \"id\" ASC";

		List<PointGroup> groups = new ArrayList<PointGroup>();
		PreparedStatement statementGroup = this.projectDB.getPreparedStatement(sqlGroup);
		PreparedStatement statementPoint = this.projectDB.getPreparedStatement(sqlPoint);
		
		ResultSet groupSet = statementGroup.executeQuery();
		if (!groupSet.wasNull()) {
			while (groupSet.next()) {
				TransformationParameterSet trafoParamSet = null;
				int groupId = groupSet.getInt("id");
				
				if (this.dim == 3) {
					trafoParamSet = new TransformationParameterSet3D();
					if (groupSet.getBoolean("fixed_tx"))
						trafoParamSet.setRestriction( TransformationParameterSet.FIXED_TRANSLATION_X );
					if (groupSet.getBoolean("fixed_ty"))
						trafoParamSet.setRestriction( TransformationParameterSet.FIXED_TRANSLATION_Y );
					if (groupSet.getBoolean("fixed_tz"))
						trafoParamSet.setRestriction( TransformationParameterSet.FIXED_TRANSLATION_Z );
					if (groupSet.getBoolean("fixed_m"))
						trafoParamSet.setRestriction( TransformationParameterSet.FIXED_SCALE );
					if (groupSet.getBoolean("fixed_rx"))
						trafoParamSet.setRestriction( TransformationParameterSet.FIXED_ROTATION_X );
					if (groupSet.getBoolean("fixed_ry"))
						trafoParamSet.setRestriction( TransformationParameterSet.FIXED_ROTATION_Y );
					if (groupSet.getBoolean("fixed_rz"))
						trafoParamSet.setRestriction( TransformationParameterSet.FIXED_ROTATION_Z );
				}
				
				PointGroup group = new PointGroup(groupId);
				//group.setCovarianceMatrix( MathExtension.getDeserializedMatrix( groupSet.getString("covar") ) );
				group.setCovarianceMatrix( this.getCovarianceMatrix(groupId) );
				group.setTransformationParameterSet(trafoParamSet);
				statementPoint.setInt(1, groupId);
				ResultSet pointsSet = statementPoint.executeQuery();
				if (!pointsSet.wasNull()) {
					while (pointsSet.next()) {
						Point point = null;
						if (this.dim == 3) {
							point = new Point3D(
									pointsSet.getString("point_id"), 
									pointsSet.getDouble("x0"),
									pointsSet.getDouble("y0"),
									pointsSet.getDouble("z0")
							);
						}
						
						// Fuege Punkt der Gruppe hinzu
						if (point != null) {
							group.add(point);
						}
					}
				}
				// Speichere die Gruppe
				if (group.size() > 0) {
					groups.add(group);
				}
			}
		}			

	    return groups;		
	}
	
	/**
	 * Liefert die globale Punktgruppe
	 * @return global
	 * @throws SQLException 
	 */
	private GlobalPointGroup getGlobalPointGroup(boolean isFreenet) throws SQLException {
		String sqlGroup = "SELECT \"id\" FROM \"PointGroup\" WHERE \"enable\" = TRUE AND \"target_system\" = TRUE ORDER BY \"id\" ASC";
		String sqlPoint = "SELECT \"point_id\", \"x0\", \"y0\", \"z0\" FROM \"PointApriori\" WHERE \"group_id\" = ? AND \"enable\" = TRUE ORDER BY \"id\" ASC";

		GlobalPointGroup group = null;
		PreparedStatement statementGroup = this.projectDB.getPreparedStatement(sqlGroup);
		PreparedStatement statementPoint = this.projectDB.getPreparedStatement(sqlPoint);

		ResultSet groupSet = statementGroup.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			int groupId = groupSet.getInt("id");
			TransformationParameterSet trafoParamSet = null;
			
			if (this.dim == 3) {
				trafoParamSet = new TransformationParameterSet3D();
			}
			
			group = new GlobalPointGroup(groupId);
			//group.setCovarianceMatrix( MathExtension.getDeserializedMatrix( groupSet.getString("covar") ) );
			if (!isFreenet)
				group.setCovarianceMatrix( this.getCovarianceMatrix(groupId) );
			group.setTransformationParameterSet(trafoParamSet);
			statementPoint.setInt(1, groupId);
			ResultSet pointsSet = statementPoint.executeQuery();
			if (!pointsSet.wasNull()) {
				while (pointsSet.next()) {
					Point point = null;
					if (this.dim == 3) {
						point = new GlobalPoint3D (
								pointsSet.getString("point_id"), 
								pointsSet.getDouble("x0"),
								pointsSet.getDouble("y0"),
								pointsSet.getDouble("z0")
						);
					}
						
					// Fuege Punkt der Gruppe hinzu
					if (point != null) {
						group.add(point);
					}
				}
			}
		}
	    return group;		
	}
	
	@Override
	public void run() {
		if (this.transformation == null) {
			this.changes.firePropertyChange( "NOT_INITIALIZE", false, true );
			this.changes.firePropertyChange( "ABORTET", false, true );
			return;
		}
		this.saveResults();
	}
	
	public void addPropertyChangeListener( PropertyChangeListener l ) {
		this.changes.addPropertyChangeListener( l );
	}

	public void removePropertyChangeListener( PropertyChangeListener l ) {
		this.changes.removePropertyChangeListener( l );
	}
	
}
