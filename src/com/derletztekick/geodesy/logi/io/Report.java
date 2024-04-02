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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.Map.Entry;

import HTML.Template;

import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;
import com.derletztekick.tools.sql.DataBase;

public class Report extends Template{
	private final DataBase db;
	
	public Report(DataBase dataBase, Hashtable<String, Object> tempParameter) throws FileNotFoundException, IllegalArgumentException, IllegalStateException, IOException {
		super(tempParameter);
		this.db = dataBase;
	}

	public boolean toFile(File f) {
		PrintWriter pw = null;
		boolean isConnected = this.db.isOpen();
		boolean isComplete = true;
		try {
			if (!isConnected)
				this.db.open();
			
			this.addGeneralData();
			this.setParam("globalCoordinates", this.getPointsAposteriori(-1, true));
			this.setParam("globalGroup", this.getGroups(true));
			this.setParam("localGroups", this.getGroups(false));
			
			String reportContent = this.output();
			String copyrightFooter = "<p id=\"logi_copyright\">Logi (BundleAdjustment) \u2015 The <em>OpenSource</em> Similarity Transformation Program for Polar Observations &ndash; &copy; Michael L&ouml;sler &ndash; <a href=\"http://derletztekick.com\" title=\"OpenSource Software Development\">derletztekick.com</a></p>";
			if (reportContent.contains("</body>")) {
				String regex = "</body>";
				reportContent = reportContent.replaceAll(regex, copyrightFooter+"\n\r</body>");
			}
			else
				reportContent += copyrightFooter;
			
			pw = new PrintWriter(new BufferedWriter( new OutputStreamWriter(new FileOutputStream( f ), "UTF-8") ));
			pw.println( reportContent );
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			isComplete = false;		
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			isComplete = false;
		} catch (SQLException e) {
			e.printStackTrace();
			isComplete = false;
		} catch (IOException e) {
			e.printStackTrace();
			isComplete = false;
		}
		finally {
			if (!isConnected)
				this.db.close();
	    	if (pw != null) {
				pw.close();
			}
		}
		return isComplete;
	}
	
	private void addGeneralData() throws SQLException {
		String sql = "SELECT  \"name\",\"description\",\"operator\",\"logi_version\",\"logi_build\",\"date\",\"alpha\",\"beta\",\"freenet\",\"t_global\",\"k_global\",\"k_prio\",\"k_post\",\"omega\",\"degree_of_freedom\",\"stochastic_model\" FROM \"GeneralSetting\" JOIN \"LeastSquareResult\" ON \"GeneralSetting\".\"id\" = \"LeastSquareResult\".\"id\" WHERE \"LeastSquareResult\".\"id\" = 1";
		PreparedStatement statement = this.db.getPreparedStatement(sql);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			ResultSetMetaData rsmd = groupSet.getMetaData();
			int cnt = rsmd.getColumnCount();
			for(int i = 1; i <= cnt; i++) {
				String key = rsmd.getColumnLabel(i);
				int type = rsmd.getColumnType(i);

				if(type == Types.CHAR || type==Types.VARCHAR) {
					this.setParam(key, groupSet.getString(i));
				}
				else if(type == Types.INTEGER) {
					this.setParam(key, groupSet.getInt(i) );
				}
				else if(type == Types.BOOLEAN) {
					this.setParam(key, groupSet.getBoolean(i)?1:0 );
				}
				else if(type == Types.DOUBLE) {
					double d = groupSet.getDouble(key);
					if (Double.isInfinite(d))
						this.setParam(key, "&infin;");
					else
						this.setParam(key, String.format( Locale.ENGLISH, "%.3f", d ) );
				}
			}
			this.setParam("logi_version", String.format( Locale.ENGLISH, "%.1f", groupSet.getDouble("logi_version")) );
			this.setParam("alpha", String.format( Locale.ENGLISH, "%.1f", groupSet.getDouble("alpha")) );
			this.setParam("beta", String.format( Locale.ENGLISH, "%.1f", groupSet.getDouble("beta")) );
			this.setParam("global_test_rejected", Math.round(groupSet.getDouble("t_global") * 1000.0) / 1000.0 > groupSet.getDouble("k_global")?1:0);
			this.setParam("stochastic_model_mcs", groupSet.getInt("stochastic_model") == PreAnalysis.UNCERTAINTY_MODEL_MONTE_CARLO_SIMULATION );
			this.setParam("stochastic_model_ut",  groupSet.getInt("stochastic_model") == PreAnalysis.UNCERTAINTY_MODEL_UNSCENTED_TRANSFORMATION );
			this.setParam("stochastic_model_pou", groupSet.getInt("stochastic_model") == PreAnalysis.UNCERTAINTY_MODEL_PROPAGATION_OF_UNCERTAINTY );
			
			try {
				Date d = (new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" )).parse( groupSet.getString("date") );
				this.setParam("date_YYYY", (new SimpleDateFormat( "yyyy" )).format(d) );
				this.setParam("date_MM",   (new SimpleDateFormat( "MM" )).format(d) );
				this.setParam("date_DD",   (new SimpleDateFormat( "dd" )).format(d) );
				this.setParam("date_hh",   (new SimpleDateFormat( "HH" )).format(d) );
				this.setParam("date_mm",   (new SimpleDateFormat( "mm" )).format(d) );
				this.setParam("date_ss",   (new SimpleDateFormat( "ss" )).format(d) );
				this.setParam("date",      (new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" )).format(d) );
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Vector<Hashtable<String, Object>> getGroups(boolean isGlobalGroup) throws SQLException {
		String sql = "SELECT \"id\", \"name\", \"point_id\", \"fixed_tx\", \"fixed_ty\", \"fixed_tz\", \"fixed_rx\", \"fixed_ry\", \"fixed_rz\", \"fixed_m\" FROM \"PointGroup\" WHERE \"target_system\" = ? AND \"enable\" = TRUE";
		Vector<Hashtable<String, Object>> groups = new Vector<Hashtable<String, Object>>();
		PreparedStatement statement = this.db.getPreparedStatement(sql);
		statement.setBoolean(1, isGlobalGroup);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull()) {
			ResultSetMetaData rsmd = groupSet.getMetaData();
			while(groupSet.next()) {
				int cnt = rsmd.getColumnCount();
				Hashtable<String, Object> group = new Hashtable<String, Object>();
				int groupId = groupSet.getInt("id");
				
				for(int i = 1; i <= cnt; i++) {
					String key = rsmd.getColumnLabel(i);
					int type = rsmd.getColumnType(i);

					if(type == Types.CHAR || type==Types.VARCHAR) {
						group.put(key, groupSet.getString(i));
					}
					else if(type == Types.INTEGER) {
						group.put(key, groupSet.getInt(i) );
					}
					else if(type == Types.DOUBLE) {
						group.put(key, String.format( Locale.ENGLISH, "%.6f", groupSet.getDouble(i)) );
					}
					else if(type == Types.BOOLEAN) {
						group.put(key, groupSet.getBoolean(i)?1:0 );
					}
				}
				Vector<Hashtable<String, Object>> points = this.getPointsAposteriori(groupId, isGlobalGroup);
				Hashtable<String, Object> trafoParam = isGlobalGroup?null:this.getTransformationParameter(groupId);
				Hashtable<String, Object> groupVCE = this.getVarianceComponentEstimation(groupId, isGlobalGroup);	
				//Vector<Hashtable<String, Object>> polarObservation = isGlobalGroup?null:this.getPolarObservationsApriori(groupId);
				if (points != null && points.size() > 0) {
					group.put("points", points);
					for ( Entry<String, Object> vce : groupVCE.entrySet() ) {
						group.put(vce.getKey(), vce.getValue());
					}
					if (!isGlobalGroup) {
						group.put("transformationParameter", trafoParam != null && trafoParam.size() > 0?1:0);
						for ( Entry<String, Object> element : trafoParam.entrySet() ) {
							group.put(element.getKey(), element.getValue());
							group.put("has_stochastic_parameters", 1);
						}
						//group.put("polarObservation", polarObservation);
					}
					else if (isGlobalGroup && !this.getParam("freenet").toString().equalsIgnoreCase("1")) {
						group.put("has_stochastic_parameters", 1);
					}
					groups.add(group);
				}
			}
		}
		return groups;
	}
	
	private Hashtable<String, Object> getVarianceComponentEstimation(int groupId, boolean isGlobalGroup) throws SQLException {
		Hashtable<String, Object> groupParameters = new Hashtable<String, Object>();
		String sql = "";
		if (!isGlobalGroup)
			sql = "SELECT SUM(\"redundance_x\" + \"redundance_y\" + \"redundance_z\") AS \"group_redundance\", SUM(\"omega\") AS \"group_omega\" FROM \"LocalPointApost\" JOIN \"PointApriori\" ON \"LocalPointApost\".\"id\" = \"PointApriori\".\"id\" AND \"PointApriori\".\"group_id\" = ? AND \"PointApriori\".\"enable\" = TRUE";
		else {
			sql = "SELECT SUM(\"redundance_x\" + \"redundance_y\" + \"redundance_z\") AS \"group_redundance\", SUM(\"omega\") AS \"group_omega\" FROM \"GlobalPointApost\" JOIN \"PointApriori\" ON \"GlobalPointApost\".\"point_id\" = \"PointApriori\".\"point_id\" AND \"PointApriori\".\"group_id\" = ? AND \"PointApriori\".\"enable\" = TRUE";
		}
		
		PreparedStatement statement = this.db.getPreparedStatement(sql);
		statement.setInt(1, groupId);
		
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			double groupRedundance  = groupSet.getDouble("group_redundance");
			double groupOmega       = groupSet.getDouble("group_omega");
			double groupSigma2apost = groupRedundance>0?groupOmega/groupRedundance:0.0;
			
			groupParameters.put("group_redundance",  String.format( Locale.ENGLISH, "%.6f", groupRedundance));
			groupParameters.put("group_omega",       String.format( Locale.ENGLISH, "%.6f", groupOmega));
			groupParameters.put("group_sigma2apost", String.format( Locale.ENGLISH, "%.6f", groupSigma2apost));		
		}
		return groupParameters;
	}
	
	private Hashtable<String, Object> getTransformationParameter(int groupId) throws SQLException {
		String sqlTrafo = "SELECT \"tx\", \"ty\", \"tz\", \"m\", \"q0\", \"q1\", \"q2\", \"q3\", \"rx\", \"ry\", \"rz\", \"sigma_tx\", \"sigma_ty\", \"sigma_tz\", \"sigma_m\", \"sigma_q0\", \"sigma_q1\", \"sigma_q2\", \"sigma_q3\", \"sigma_rx\", \"sigma_ry\", \"sigma_rz\", \"significant_tx\", \"significant_ty\", \"significant_tz\", \"significant_m\", \"significant_q0\", \"significant_q1\", \"significant_q2\", \"significant_q3\", \"significant_rx\", \"significant_ry\", \"significant_rz\" FROM \"TransformationParameter\" WHERE \"id\" = ?";
		Hashtable<String, Object> parameters = new Hashtable<String, Object>();
		
		PreparedStatement statement = this.db.getPreparedStatement(sqlTrafo);
		statement.setInt(1, groupId);
		
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			ResultSetMetaData rsmd = groupSet.getMetaData();
			int cnt = rsmd.getColumnCount();

			for(int i = 1; i <= cnt; i++) {
				String key = rsmd.getColumnLabel(i);
				int type = rsmd.getColumnType(i);

				if(type == Types.CHAR || type==Types.VARCHAR) {
					parameters.put(key, groupSet.getString(i));
				}
				else if(type == Types.INTEGER) {
					parameters.put(key, groupSet.getInt(i) );
				}
				else if(type == Types.DOUBLE) {
					parameters.put(key, String.format( Locale.ENGLISH, "%.6f", groupSet.getDouble(i)) );
				}
				else if(type == Types.BOOLEAN) {
					parameters.put(key, groupSet.getBoolean(i)?1:0 );
				}
			}			
		}
		return parameters;
	}
	
//	private Vector<Hashtable<String, Object>> getPolarObservationsApriori(int groupId) throws SQLException {
//		String sqlPolar = "SELECT \"point_id\", \"distance\", \"azimuth\", \"zenith\" FROM \"PolarObservation\" WHERE \"group_id\" = ? ORDER BY \"id\" ASC";
//		Vector<Hashtable<String, Object>> observations = new Vector<Hashtable<String, Object>>();
//		
//		PreparedStatement statement = this.db.getPreparedStatement(sqlPolar);
//		statement.setInt(1, groupId);
//		
//		ResultSet groupSet = statement.executeQuery();
//		if (!groupSet.wasNull()) {
//			ResultSetMetaData rsmd = groupSet.getMetaData();
//			while(groupSet.next()) {
//				int cnt = rsmd.getColumnCount();
//				Hashtable<String, Object> h = new Hashtable<String, Object>();
//				for(int i = 1; i <= cnt; i++) {
//					String key = rsmd.getColumnLabel(i);
//					int type = rsmd.getColumnType(i);
//
//					if(type == Types.CHAR || type==Types.VARCHAR) {
//						h.put(key, groupSet.getString(i));
//					}
//					else if(type == Types.INTEGER) {
//						h.put(key, groupSet.getInt(i) );
//					}
//					else if(type == Types.DOUBLE) {
//						h.put(key, String.format( Locale.ENGLISH, "%.6f", groupSet.getDouble(i)) );
//					}
//					else if(type == Types.BOOLEAN) {
//						h.put(key, groupSet.getBoolean(i)?1:0 );
//					}
//				}
//				observations.addElement(h);
//			}
//		}
//		return observations;
//	}
	
	private Vector<Hashtable<String, Object>> getPointsAposteriori(int groupId, boolean isGlobalGroup) throws SQLException {
		Vector<Hashtable<String, Object>> points = new Vector<Hashtable<String, Object>>();
		String sql = "";
		if (!isGlobalGroup)
			sql = "SELECT \"point_id\",\"x0\",\"y0\",\"z0\",\"x\",\"y\",\"z\",(\"x\"-\"x0\") AS \"vx\",(\"y\"-\"y0\") AS \"vy\",(\"z\"-\"z0\") AS \"vz\",\"sigma_x\",\"sigma_y\",\"sigma_z\",\"nabla_x\",\"nabla_y\",\"nabla_z\",\"redundance_x\",\"redundance_y\",\"redundance_z\",\"omega\",\"t_prio\",\"t_post\",\"outlier\" FROM \"LocalPointApost\" JOIN \"PointApriori\" ON \"LocalPointApost\".\"id\" = \"PointApriori\".\"id\" AND \"PointApriori\".\"group_id\" = ? AND \"PointApriori\".\"enable\" = TRUE ORDER BY \"LocalPointApost\".\"id\" ASC";
		else {
			sql = "SELECT \"point_id\",\"x0\",\"y0\",\"z0\",\"x\",\"y\",\"z\",(\"x\"-\"x0\") AS \"vx\",(\"y\"-\"y0\") AS \"vy\",(\"z\"-\"z0\") AS \"vz\",\"sigma_x\",\"sigma_y\",\"sigma_z\",\"nabla_x\",\"nabla_y\",\"nabla_z\",\"redundance_x\",\"redundance_y\",\"redundance_z\",\"omega\",\"t_prio\",\"t_post\",\"outlier\" FROM \"GlobalPointApost\" JOIN \"PointApriori\" ON \"GlobalPointApost\".\"point_id\" = \"PointApriori\".\"point_id\" AND \"PointApriori\".\"group_id\" = ? AND \"PointApriori\".\"enable\" = TRUE ORDER BY \"PointApriori\".\"id\" ASC";
			if (groupId<0) {
				sql = "SELECT \"id\",\"point_id\", \"x\", \"y\", \"z\",\"sigma_x\",\"sigma_y\",\"sigma_z\" FROM \"GlobalPointApost\" WHERE \"id\" > ? ORDER BY \"id\" ASC";
			}
		}
			
		PreparedStatement statement = this.db.getPreparedStatement(sql);
		statement.setInt(1, groupId);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull()) {
			ResultSetMetaData rsmd = groupSet.getMetaData();
			while(groupSet.next()) {
				int cnt = rsmd.getColumnCount();
				Hashtable<String, Object> h = new Hashtable<String, Object>();

				for(int i = 1; i <= cnt; i++) {
					String key = rsmd.getColumnLabel(i);
					int type = rsmd.getColumnType(i);

					if(type == Types.CHAR || type==Types.VARCHAR) {
						h.put(key, groupSet.getString(i));
					}
					else if(type == Types.INTEGER) {
						h.put(key, groupSet.getInt(i) );
					}
					else if(type == Types.DOUBLE) {
						h.put(key, String.format( Locale.ENGLISH, "%.6f", groupSet.getDouble(i)) );
					}
					else if(type == Types.BOOLEAN) {
						h.put(key, groupSet.getBoolean(i)?1:0 );
					}
				}
				points.addElement(h);
			}
		}
		return points;
	}
}
