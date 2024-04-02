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

package com.derletztekick.geodesy.logi.sql;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.LasertrackerUncertaintyModel;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.UncertaintyModel;
import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;
import com.derletztekick.geodesy.bundleadjustment.v2.transformation.sql.BundleAdjustmentProjectDatabase;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.UnknownParameter;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet;
import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet3D;
import com.derletztekick.geodesy.logi.Logi;
import com.derletztekick.geodesy.logi.dialog.IndeterminateProgressDialog;
import com.derletztekick.geodesy.logi.io.CoVarFileReader;
import com.derletztekick.geodesy.logi.io.HTMLTemplateFileFilter;
import com.derletztekick.geodesy.logi.io.ObservationFileReader;
import com.derletztekick.geodesy.logi.io.PointFileReader;
import com.derletztekick.geodesy.logi.io.Report;
import com.derletztekick.geodesy.logi.io.RowFileReader;
import com.derletztekick.geodesy.logi.io.XMLFileReader;
import com.derletztekick.geodesy.logi.pane.splitpane.DataSplitPane;
import com.derletztekick.geodesy.logi.pane.tabbedpane.DataTabbedPane;
import com.derletztekick.geodesy.logi.pane.tabbedpane.GlobalTabbedPane;
import com.derletztekick.geodesy.logi.pane.tabbedpane.LocalTabbedPane;
import com.derletztekick.geodesy.logi.settingpanel.GlobalSettingPanel;
import com.derletztekick.geodesy.logi.settingpanel.LocalSettingPanel;
import com.derletztekick.geodesy.logi.settingpanel.SettingPanel;
import com.derletztekick.geodesy.logi.table.DataTable;
import com.derletztekick.geodesy.logi.table.model.DataTableModel;
import com.derletztekick.geodesy.logi.table.model.PolarObservationTableModel;
import com.derletztekick.geodesy.logi.table.model.RawPointTableModel;
import com.derletztekick.geodesy.logi.table.model.ResultPointTableModel;
import com.derletztekick.geodesy.logi.table.model.TimeDependentUncertaintyTableModel;
import com.derletztekick.geodesy.logi.table.model.TransformationParameterTableModel;
import com.derletztekick.geodesy.logi.table.model.UncertaintyTableModel;
import com.derletztekick.geodesy.logi.table.model.UpperSymmMatrixTableModel;
import com.derletztekick.geodesy.logi.table.row.DataRow;
import com.derletztekick.geodesy.logi.table.row.PointRow;
import com.derletztekick.geodesy.logi.table.row.PolarObservationRow;
import com.derletztekick.geodesy.logi.table.row.TimeDependentUncertaintyRow;
import com.derletztekick.geodesy.logi.table.row.UncertaintyRow;
import com.derletztekick.geodesy.logi.toolbar.NavigationToolBar;
import com.derletztekick.geodesy.logi.treemenu.DataTreeMenu;
import com.derletztekick.geodesy.logi.treemenu.node.GlobalSystemNode;
import com.derletztekick.geodesy.logi.treemenu.node.LocalSystemNode;
import com.derletztekick.geodesy.logi.treemenu.node.SystemNode;
import com.derletztekick.tools.sql.DataBase;
import com.derletztekick.tools.sql.HSQLDB;
import com.derletztekick.tools.babel.Babel;
import com.derletztekick.tools.geodesy.Constant;
import com.derletztekick.tools.geodesy.MathExtension;
import com.derletztekick.tools.io.FileChooser;
import com.derletztekick.tools.io.LockFileReader;
import com.derletztekick.tools.io.UpdateChecker;
import com.derletztekick.tools.io.filter.CoVarFileFilter;
import com.derletztekick.tools.io.filter.DefaultFileFilter;
import com.derletztekick.tools.io.filter.HSQLFileFilter;
import com.derletztekick.tools.io.filter.TMPLFileFilter;
import com.derletztekick.tools.io.filter.TXTFileFilter;
import com.derletztekick.tools.io.filter.XMLFileFilter;
import com.derletztekick.tools.io.filter.XYZFileFilter;

public class DataManager implements PropertyChangeListener, TreeSelectionListener, TreeModelListener, TableModelListener, ActionListener, FocusListener, WindowListener { 
	private JFrame owner;
	private DataBase db = null;
	private FileChooser fileChooser; 
	private Babel babel;
	private DataTreeMenu dataTreeMenu;
	private DataSplitPane dataSplitPane;
	private NavigationToolBar navigationToolBar;
	public final String TEMPLATE_PATH = "template" + File.separator + "Logi";
	public DataManager() {
		this(Locale.getDefault());
	}

	public DataManager(JFrame owner) {
		this(Locale.getDefault(), owner);
	}

	public DataManager(Locale locale) {
		this(locale, null);
	}
	
	public DataManager(Locale locale, JFrame owner) {
		this.owner = owner;
		Locale.setDefault(locale);
		this.babel = new Babel("logi", locale);
		this.fileChooser = new FileChooser(this.owner);
		this.fileChooser.setLocale(locale);
		
		this.dataTreeMenu = new DataTreeMenu(this);
		this.dataSplitPane = new DataSplitPane(this.dataTreeMenu);
		this.navigationToolBar = new NavigationToolBar(this);
		
		this.dataTreeMenu.addTreeSelectionListener(this);
	}
	
	private void copyDataBase(boolean newDB) throws ClassNotFoundException, SQLException {
		if (!newDB && this.db == null) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseTitle"),
					JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		
		String srcFiles[] = new String[] { "data", "properties", "script", "backup" };
		String regex = "(.+?)(\\.)(backup$|data$|properties$|script$)";		
		File selectedFile = this.openSingleFileDialog(new HSQLFileFilter(), JFileChooser.SAVE_DIALOG);
		if (selectedFile == null || !selectedFile.toString().matches(regex)) 
			return;

		String dbName = selectedFile.getAbsolutePath().replaceAll(regex, "$1");
		boolean isComplete = true;
		
		// Dateien duerfen (ueber)schrieben werden
		String oldDBName = (!newDB && this.db instanceof HSQLDB)?((HSQLDB)this.db).getDataBaseBaseFileName():null;
		this.closeDataBase();
		
		for (int i=0; i<srcFiles.length; i++) {
			// Teste, ob Datei vorhenden (nach Umstellung auf Version 2.2.1 von HSQLDB)
			if (!new File("db" + File.separator + srcFiles[i]).exists() || oldDBName != null && !new File(oldDBName + "." + srcFiles[i]).exists())
				continue;
			
			// erzeuge ein neues Projekt (kopiere dummy Datenbank)
			if (newDB)
				isComplete = this.copyFile("db" + File.separator + srcFiles[i], dbName + "." + srcFiles[i]);
			// kopiere bestehendes Projekt (Endungen muessen klein geschrieben sein!)
			else if (oldDBName != null)
				isComplete = oldDBName.equals(dbName) || this.copyFile(oldDBName + "." + srcFiles[i], dbName + "." + srcFiles[i]);
			else
				isComplete = false;
			
			if (!isComplete)
				break;
		}
		
		if (isComplete) {
			this.setHSQLDB( dbName );
			if (this.checkDataBaseVersion(newDB))
				this.initTreeMenu();
			else {
				this.db.close();
			}
		}
		else {
			JOptionPane.showMessageDialog(this.owner,
					this.babel.getString(this.getClass().getSimpleName(), "err.createDBBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.createDBTitle"),
				    JOptionPane.ERROR_MESSAGE
			);
			this.db.close();
		}
	}	
	
	private void openDataBase() throws ClassNotFoundException, SQLException {
		final String regex = "(.+?)(\\.)(backup$|data$|properties$|script$)";
		File selectedFile = this.openSingleFileDialog(new HSQLFileFilter(), JFileChooser.OPEN_DIALOG);

		if(selectedFile != null && selectedFile.toString().matches(regex) ) {
			this.closeDataBase();
			this.setHSQLDB( selectedFile.getAbsolutePath().replaceAll(regex, "$1") );
			if (this.checkDataBaseVersion(false))
				this.initTreeMenu();
			else
				this.db.close();
		}
	}
	
	private void openExistingProject() {
		try {
			this.openDataBase();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
	}
	
	private void createNewProject() {
		try {
			this.copyDataBase(true);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
	}
	
	private void copyProject() {
		try {
			this.copyDataBase(false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void initTreeMenu() throws SQLException {
		if (!this.hasDataBase())
			return;
		this.dataTreeMenu.initGroupNodes();
		boolean hasGlobalSystem = false;
		String sqlGroup = "SELECT \"id\", \"name\", \"point_id\", \"target_system\" FROM \"PointGroup\" ORDER BY \"target_system\", \"id\" ASC";

		PreparedStatement statementGroup = this.db.getPreparedStatement(sqlGroup);
		
		ResultSet groupSet = statementGroup.executeQuery();
		if (!groupSet.wasNull()) {
			while (groupSet.next()) {
				int id = groupSet.getInt("id");
				String name = groupSet.getString("name"); 
				String startPointId = groupSet.getString("point_id"); 
				boolean isTargetSystem = groupSet.getBoolean("target_system");
				if (isTargetSystem) {
					hasGlobalSystem = true;
					this.dataTreeMenu.setGlobalSystemId(id);
					this.dataTreeMenu.setGlobalSystemName(name);
				}
				else {
					this.dataTreeMenu.addLocalSystemNode(id, name, startPointId, false);
				}
			}
		}
		
		if (!hasGlobalSystem)
			this.addSystem(this.babel.getString("DataTreeMenu", "root"), true);
		
		this.dataTreeMenu.setSelectionRow(0);
		this.dataTreeMenu.expandRow(0);
	}
	
	public DataTreeMenu getDataTreeMenu() {
		return this.dataTreeMenu;
	}
	
	public DataSplitPane getDataSplitPane() {
		return this.dataSplitPane;
	}
	
	public NavigationToolBar getToolBar() {
		return this.navigationToolBar;
	}
	
	public void setHSQLDB(String uri) throws ClassNotFoundException, SQLException {
		if (uri == null || uri.trim().isEmpty())
			return;
		if (this.db != null)
			this.db.close();
		this.db = new HSQLDB(uri);
		this.db.open();
		if (this.db.isOpen() && this.owner != null) {
			this.owner.setTitle( new File(uri).getName() );
		}

	}
	
	public boolean hasDataBase() {
		return this.db != null;
	}
	
	/**
	 * Lade die globalen Einstellungen
	 * @param settingsPane
	 */
	private void loadGlobalSettings(GlobalSettingPanel settingsPane) {
		try { 
			String sqlSetting = "SELECT \"name\",\"description\",\"operator\",\"alpha\",\"beta\",\"iteration\",\"monte_carlo_samples\",\"freenet\", \"stochastic_model\", \"export_covar\", \"appy_sigma_apost\" FROM \"GeneralSetting\" WHERE \"id\"=1";
			PreparedStatement statement = this.db.getPreparedStatement(sqlSetting);
			ResultSet groupSet = statement.executeQuery();
			if (!groupSet.wasNull() && groupSet.next()) {
				String projectDetails[] = new String[] { 
						groupSet.getString("name"),          // Projekt
						groupSet.getString("description"),   // Beschreibung
						groupSet.getString("operator")       // Name
				};
				int iteration = groupSet.getInt("iteration");
				double alpha  = groupSet.getDouble("alpha");
				double beta   = groupSet.getDouble("beta");
				boolean freenet = groupSet.getBoolean("freenet");
				boolean exportCovar = groupSet.getBoolean("export_covar");
				boolean applySigmaApost = groupSet.getBoolean("appy_sigma_apost");
				int uncertaintyModelType = groupSet.getInt("stochastic_model");
				int monteCarloSamples    = groupSet.getInt("monte_carlo_samples");
				settingsPane.reload(projectDetails, iteration, monteCarloSamples, alpha, beta, freenet, uncertaintyModelType, exportCovar, applySigmaApost);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Laed die lokalen Einstellungen (Gruppe aktiv und Unsicherheitsbudget)
	 * @param groupId
	 * @param settingsPane
	 */
	private void loadLocalSettings(int groupId, LocalSettingPanel settingsPane) {
		try {
			boolean enable = true;
			double sigma2apriori = 1.0;
			// Abfrage der a-priori Std. fuer Fehlerfortpflanzung aus DB
			UncertaintyRow uncertaintyRows[] = null;
			TimeDependentUncertaintyRow timeDependentUncertaintyRows[] = null;
			boolean fixedTrafoParamerers[] = null;
			String sqlGroup = "SELECT \"enable\", \"fixed_tx\", \"fixed_ty\", \"fixed_tz\", \"fixed_rx\", \"fixed_ry\", \"fixed_rz\", \"fixed_m\", \"sigma2aprio\" FROM \"PointGroup\" WHERE \"id\" = ?";
			PreparedStatement statement = this.db.getPreparedStatement(sqlGroup);
			statement.setInt(1, groupId);
			ResultSet groupSet = statement.executeQuery();
			if (!groupSet.wasNull() && groupSet.next()) {
				enable = groupSet.getBoolean("enable");
				sigma2apriori = groupSet.getDouble("sigma2aprio");
				fixedTrafoParamerers = new boolean[] {
						groupSet.getBoolean("fixed_tx"),
						groupSet.getBoolean("fixed_ty"),
						groupSet.getBoolean("fixed_tz"),
						
						groupSet.getBoolean("fixed_rx"),
						groupSet.getBoolean("fixed_ry"),
						groupSet.getBoolean("fixed_rz"),
						
						groupSet.getBoolean("fixed_m")
				};
			}

			
			int obsTypes[] = new int[] {LasertrackerUncertaintyModel.DISTANCE3D, LasertrackerUncertaintyModel.AZIMUTHANGLE, LasertrackerUncertaintyModel.ZENITHANGLE};
			String sqlCorrModel = "SELECT \"corr_fun\",\"sigma\",\"max_dt\",\"coef_a\",\"coef_b\",\"coef_c\" FROM \"CorrelationFunctionParameter\" WHERE \"group_id\" = ? AND \"obs_type\" = ?";
			timeDependentUncertaintyRows = new TimeDependentUncertaintyRow[obsTypes.length];
			statement = this.db.getPreparedStatement(sqlCorrModel);
			statement.setInt(1, groupId);
			for (int k=0; k<obsTypes.length; k++) {
				int obsType = obsTypes[k];
				statement.setInt(2, obsType);
				
				groupSet = statement.executeQuery();
				if (!groupSet.wasNull() && groupSet.next()) {
					int corrFuncType = groupSet.getInt("corr_fun");
					double sigma = Math.abs(groupSet.getDouble("sigma"));
					double maxDT = groupSet.getDouble("max_dt");
					double a = Math.abs(groupSet.getDouble("coef_a"));
					double b = Math.abs(groupSet.getDouble("coef_b"));
					double c = Math.abs(groupSet.getDouble("coef_c"));
					
					TimeDependentUncertaintyRow row = new TimeDependentUncertaintyRow(corrFuncType, obsType, sigma, maxDT, a, b, c);
					timeDependentUncertaintyRows[k] = row;
				}
			}

			String sqlStochModel = "SELECT \"sigma_x_instrument\",\"sigma_y_instrument\",\"sigma_z_instrument\",\"sigma_scale_instrument\",\"sigma_add_instrument\",\"sigma_fourier_aa1_instrument\",\"sigma_fourier_aa2_instrument\",\"sigma_fourier_ba1_instrument\",\"sigma_fourier_ba2_instrument\",\"sigma_fourier_ae0_instrument\",\"sigma_fourier_ae1_instrument\",\"sigma_fourier_ae2_instrument\",\"sigma_fourier_be1_instrument\",\"sigma_fourier_be2_instrument\",\"sigma_alpha_instrument\",\"sigma_gamma_instrument\",\"sigma_ex_instrument\",\"sigma_by_instrument\",\"sigma_bz_instrument\",\"sigma_scale_target\",\"sigma_add_target\",\"sigma_azimuth_target\",\"sigma_zenith_target\",\"sigma_centring_azimuth_target\",\"sigma_centring_zenith_target\",\"distribution_x_instrument\",\"distribution_y_instrument\",\"distribution_z_instrument\",\"distribution_scale_instrument\",\"distribution_add_instrument\",\"distribution_fourier_aa1_instrument\",\"distribution_fourier_aa2_instrument\",\"distribution_fourier_ba1_instrument\",\"distribution_fourier_ba2_instrument\",\"distribution_fourier_ae0_instrument\",\"distribution_fourier_ae1_instrument\",\"distribution_fourier_ae2_instrument\",\"distribution_fourier_be1_instrument\",\"distribution_fourier_be2_instrument\",\"distribution_alpha_instrument\",\"distribution_gamma_instrument\",\"distribution_ex_instrument\",\"distribution_by_instrument\",\"distribution_bz_instrument\",\"distribution_scale_target\",\"distribution_add_target\",\"distribution_azimuth_target\",\"distribution_zenith_target\",\"distribution_centring_azimuth_target\",\"distribution_centring_zenith_target\" FROM \"LasertrackerUncertaintyParameter\" INNER JOIN \"DistributionUncertaintyParameter\" ON \"LasertrackerUncertaintyParameter\".\"id\" = \"DistributionUncertaintyParameter\".\"id\" WHERE \"LasertrackerUncertaintyParameter\".\"id\" = ?";
			
			statement = this.db.getPreparedStatement(sqlStochModel);
			statement.setInt(1, groupId);
			groupSet = statement.executeQuery();
			if (!groupSet.wasNull() && groupSet.next()) {
				
				uncertaintyRows = new UncertaintyRow[] {
						new UncertaintyRow("", groupSet.getDouble("sigma_x_instrument"),              groupSet.getInt("distribution_x_instrument")),
						new UncertaintyRow("", groupSet.getDouble("sigma_y_instrument"),              groupSet.getInt("distribution_y_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_z_instrument"),              groupSet.getInt("distribution_z_instrument")),

						new UncertaintyRow("", groupSet.getDouble("sigma_scale_instrument"),          groupSet.getInt("distribution_scale_instrument")),
						new UncertaintyRow("", groupSet.getDouble("sigma_add_instrument"),            groupSet.getInt("distribution_add_instrument")),

						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_aa1_instrument"),    groupSet.getInt("distribution_fourier_aa1_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_aa2_instrument"),    groupSet.getInt("distribution_fourier_aa2_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_ba1_instrument"),    groupSet.getInt("distribution_fourier_ba1_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_ba2_instrument"),    groupSet.getInt("distribution_fourier_ba2_instrument")), 

						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_ae0_instrument"),    groupSet.getInt("distribution_fourier_ae0_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_ae1_instrument"),    groupSet.getInt("distribution_fourier_ae1_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_ae2_instrument"),    groupSet.getInt("distribution_fourier_ae2_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_be1_instrument"),    groupSet.getInt("distribution_fourier_be1_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_fourier_be2_instrument"),    groupSet.getInt("distribution_fourier_be2_instrument")),

						new UncertaintyRow("", groupSet.getDouble("sigma_alpha_instrument"),          groupSet.getInt("distribution_alpha_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_gamma_instrument"),          groupSet.getInt("distribution_gamma_instrument")),

						new UncertaintyRow("", groupSet.getDouble("sigma_ex_instrument"),             groupSet.getInt("distribution_ex_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_by_instrument"),             groupSet.getInt("distribution_by_instrument")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_bz_instrument"),             groupSet.getInt("distribution_bz_instrument")), 

						new UncertaintyRow("", groupSet.getDouble("sigma_scale_target"),              groupSet.getInt("distribution_scale_target")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_add_target"),                groupSet.getInt("distribution_add_target")),

						new UncertaintyRow("", groupSet.getDouble("sigma_azimuth_target"),            groupSet.getInt("distribution_azimuth_target")), 
						new UncertaintyRow("", groupSet.getDouble("sigma_zenith_target"),             groupSet.getInt("distribution_zenith_target")),

						new UncertaintyRow("", groupSet.getDouble("sigma_centring_azimuth_target"),   groupSet.getInt("distribution_centring_azimuth_target")),
						new UncertaintyRow("", groupSet.getDouble("sigma_centring_zenith_target"),    groupSet.getInt("distribution_centring_zenith_target"))
				};
			}
			settingsPane.reload(enable, sigma2apriori, uncertaintyRows, timeDependentUncertaintyRows, fixedTrafoParamerers);
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadTransformationParameterSet(int id, TransformationParameterTableModel model) throws SQLException {
		String sqlTrafo = "SELECT \"tx\", \"ty\", \"tz\", \"m\", \"q0\", \"q1\", \"q2\", \"q3\", \"rx\", \"ry\", \"rz\", \"sigma_tx\", \"sigma_ty\", \"sigma_tz\", \"sigma_m\", \"sigma_q0\", \"sigma_q1\", \"sigma_q2\", \"sigma_q3\", \"sigma_rx\", \"sigma_ry\", \"sigma_rz\", \"significant_tx\", \"significant_ty\", \"significant_tz\", \"significant_m\", \"significant_q0\", \"significant_q1\", \"significant_q2\", \"significant_q3\", \"significant_rx\", \"significant_ry\", \"significant_rz\" FROM \"TransformationParameter\" WHERE \"id\" = ?";
		PreparedStatement statement = this.db.getPreparedStatement(sqlTrafo);
		TransformationParameterSet trafoParam = new TransformationParameterSet3D();
		statement.setInt(1, id);
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull() && groupSet.next()) {
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_X).setValue(groupSet.getDouble("tx"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y).setValue(groupSet.getDouble("ty"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z).setValue(groupSet.getDouble("tz"));
			
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).setValue(groupSet.getDouble("q0"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).setValue(groupSet.getDouble("q1"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).setValue(groupSet.getDouble("q2"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).setValue(groupSet.getDouble("q3"));
			
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_SCALE).setValue(groupSet.getDouble("m"));
			
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_X).setValue(groupSet.getDouble("rx"));
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_Y).setValue(groupSet.getDouble("ry"));
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_Z).setValue(groupSet.getDouble("rz"));
			
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_X).setStd(groupSet.getDouble("sigma_tx"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y).setStd(groupSet.getDouble("sigma_ty"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z).setStd(groupSet.getDouble("sigma_tz"));
			
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).setStd(groupSet.getDouble("sigma_q0"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).setStd(groupSet.getDouble("sigma_q1"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).setStd(groupSet.getDouble("sigma_q2"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).setStd(groupSet.getDouble("sigma_q3"));
			
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_SCALE).setStd(groupSet.getDouble("sigma_m"));
			
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_X).setStd(groupSet.getDouble("sigma_rx"));
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_Y).setStd(groupSet.getDouble("sigma_ry"));
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_Z).setStd(groupSet.getDouble("sigma_rz"));
			
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_X).setSignificant(groupSet.getBoolean("significant_tx"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Y).setSignificant(groupSet.getBoolean("significant_ty"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_TRANSLATION_Z).setSignificant(groupSet.getBoolean("significant_tz"));
			
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q0).setSignificant(groupSet.getBoolean("significant_q0"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q1).setSignificant(groupSet.getBoolean("significant_q1"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q2).setSignificant(groupSet.getBoolean("significant_q2"));
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_QUATERNION_Q3).setSignificant(groupSet.getBoolean("significant_q3"));
			
			trafoParam.getTransformationParameter(UnknownParameter.TYPE_SCALE).setSignificant(groupSet.getBoolean("significant_m"));
			
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_X).setSignificant(groupSet.getBoolean("significant_rx"));
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_Y).setSignificant(groupSet.getBoolean("significant_ry"));
			trafoParam.getAdditionalTransformationParameter(UnknownParameter.TYPE_ROTATION_Z).setSignificant(groupSet.getBoolean("significant_rz"));
		}
		model.setTransformationParameterSet(trafoParam);
	}
	
	/**
	 * Laedt die a-priori Kovarianzmatrix des Zielsystems Cll
	 * @param model
	 * @throws SQLException 
	 */
	private void loadGlobalCovarianceMatrixApriori(UpperSymmMatrixTableModel model) throws SQLException {
		Matrix Cll = null;
		String sqlMax   = "SELECT MAX(\"row\") AS \"max_row\", MAX(\"column\") AS \"max_column\" FROM  \"CovarianceMatrix\" WHERE \"id\" = -1";
		String sqlCovar = "SELECT \"row\", \"column\", \"value\" FROM \"CovarianceMatrix\" WHERE \"id\" = -1";
		
		int size = -1;
		// ermittle Dimension der symm. Matrix
		PreparedStatement statement = this.db.getPreparedStatement(sqlMax);
		ResultSet rs = statement.executeQuery();
		if (!rs.wasNull() && rs.next()) {
			size = Math.max(rs.getInt("max_row"), rs.getInt("max_column"));
			if (size > 0) {
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
		}
	
		if (Cll != null)
			model.setMatrix(Cll);
		else
			this.saveGlobalCovarianceMatrixApriori(model.getMatrix());
		
//		String sql = "SELECT \"covar\" FROM \"GeneralSetting\" WHERE \"id\"=1";
//		PreparedStatement statement = this.db.getPreparedStatement(sql);
//		ResultSet groupSet = statement.executeQuery();
//		if (!groupSet.wasNull() && groupSet.next()) {
//			Matrix Cxx = MathExtension.getDeserializedMatrix(groupSet.getString("covar"));
//			if (Cxx != null)
//				model.setMatrix(Cxx);
//		}
	}
	
	/**
	 * Laedt die Daten eines Systems (Tabellen und Einstellungen)
	 * @param groupId
	 * @param dataTabbedPane
	 */
	private void loadSystem(int groupId, DataTabbedPane dataTabbedPane)  {
		try {
			// Befülle Einstellungen
			if (dataTabbedPane.getSettingPanel() instanceof GlobalSettingPanel) {
				this.loadGlobalSettings((GlobalSettingPanel)dataTabbedPane.getSettingPanel());
			}
			else if (dataTabbedPane.getSettingPanel() instanceof LocalSettingPanel)
				this.loadLocalSettings(groupId, (LocalSettingPanel)dataTabbedPane.getSettingPanel());
			
			// Entferne den Inhalt in der Tabelle
			dataTabbedPane.clearTables();
			
			// Lade ein lokales System
			if (dataTabbedPane instanceof LocalTabbedPane) {
				
				// Abfrage der polaren Beobachtungen
				List<PolarObservationRow> polarObservations = this.getPolarObservation(groupId);
				
				// Abfrage der ausgeglichenen lokalen Punkte
				List<PointRow> localPointsApost = this.getPointsAposteriori(groupId, false);
				
				// Abfrage der ausgeglichenen globalen Punkte
				List<PointRow> globalPointsApost = this.getPointsAposteriori(groupId, true);
				
				// Schreibe Daten in Rohdatentabelle
				DataTable table = dataTabbedPane.getTable(0);
				if (table.getModel() instanceof PolarObservationTableModel) {
					table.setRowData(polarObservations);
				}
				
				// Schreibe Daten in Ergebnistabelle (lokal)
				table = dataTabbedPane.getTable(1);
				if (table.getModel() instanceof ResultPointTableModel) {
					table.setRowData(localPointsApost);
				}
				
				// Schreibe Daten in Ergebnistabelle (global)
				table = dataTabbedPane.getTable(2);
				if (table.getModel() instanceof ResultPointTableModel) {
					table.setRowData(globalPointsApost);
				}
				
				// Lade Trafo-Param
				this.loadTransformationParameterSet(groupId, ((LocalTabbedPane)dataTabbedPane).getTransformationParameterTable().getModel() );
			}
			else if (dataTabbedPane instanceof GlobalTabbedPane) {
				// Abfrage der Punkte...
				List<PointRow> localPoints = this.getPointsApriori(groupId);
				
				// Abfrage der ausgeglichenen globalen Punkte die zur Datumsbildung verwendet wurden
				List<PointRow> globalPointsApost = this.getPointsAposteriori(groupId, true);
				
				// Abfrage ALLER ausgeglichenen globalen Punkte
				List<PointRow> completeGlobalPointsApost = this.getPointsAposteriori(-1, true);
				
				// Schreibe Daten in Rohdatentabelle
				DataTable table = dataTabbedPane.getTable(0);
				if (table.getModel() instanceof RawPointTableModel) {
					table.setRowData(localPoints);
				}
				
				// Schreibe Daten in Ergebnistabelle
				table = dataTabbedPane.getTable(1);
				if (table.getModel() instanceof ResultPointTableModel) {
					table.setRowData(globalPointsApost);
				}
				
				// Schreibe Daten in Ergebnistabelle
				table = dataTabbedPane.getTable(2);
				if (table.getModel() instanceof ResultPointTableModel) {
					table.setRowData(completeGlobalPointsApost);
				}
				
				// Lade Matrix Cll a-priori
				this.loadGlobalCovarianceMatrixApriori( ((GlobalTabbedPane)dataTabbedPane).getUpperSymmMatrixTable().getModel() );
			}
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private List<PointRow> getPointsApriori(int groupId) throws SQLException {
		String sqlPointAprio = "SELECT \"id\", \"point_id\", \"x0\", \"y0\", \"z0\", \"enable\" FROM \"PointApriori\" WHERE \"group_id\" = ? ORDER BY \"id\" ASC";
		List<PointRow> localPoints = new ArrayList<PointRow>();
		
		PreparedStatement statement = this.db.getPreparedStatement(sqlPointAprio);
		statement.setInt(1, groupId);
		
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull()) {
			while(groupSet.next()) {
				int id          = groupSet.getInt("id");
				String pointId  = groupSet.getString("point_id");
				double x0       = groupSet.getDouble("x0");
				double y0       = groupSet.getDouble("y0");
				double z0       = groupSet.getDouble("z0");
				boolean enable  = groupSet.getBoolean("enable"); 
				
				PointRow point = new PointRow(id, pointId, x0, y0, z0, enable);
				localPoints.add(point);
			}
		}
		return localPoints;
	}
	
	private List<PolarObservationRow> getPolarObservation(int groupId) throws SQLException {
		String sqlPolar = "SELECT \"id\", \"point_id\", \"distance\", \"azimuth\", \"zenith\", \"enable\", \"date\" FROM \"PolarObservation\" WHERE \"group_id\" = ? ORDER BY \"id\" ASC";
		List<PolarObservationRow> polarObservations = new ArrayList<PolarObservationRow>();
		
		PreparedStatement statement = this.db.getPreparedStatement(sqlPolar);
		statement.setInt(1, groupId);
		
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull()) {
			while(groupSet.next()) {
				int id          = groupSet.getInt("id");
				String pointId  = groupSet.getString("point_id");
				double distance = groupSet.getDouble("distance");
				double azimuth  = groupSet.getDouble("azimuth");
				double zenith   = groupSet.getDouble("zenith");
				boolean enable  = groupSet.getBoolean("enable"); 
				Timestamp tiSt  = groupSet.getTimestamp("date");
				Date obsDate = null;
			    if (!groupSet.wasNull())
			    	obsDate = new Date(tiSt.getTime());
				
				PolarObservationRow obs = new PolarObservationRow(id, pointId, distance, azimuth, zenith, enable, obsDate);
				polarObservations.add(obs);
			}
		}
		return polarObservations;
	}
	
	private List<PointRow> getPointsAposteriori(int groupId, boolean isGlobal) throws SQLException {
		String sqlLocalPointApost  = "SELECT \"id\",\"point_id\", \"x\", \"y\", \"z\",\"sigma_x\",\"sigma_y\",\"sigma_z\",\"nabla_x\",\"nabla_y\",\"nabla_z\",(\"x\"-\"x0\") AS \"vx\",(\"y\"-\"y0\") AS \"vy\",(\"z\"-\"z0\") AS \"vz\",\"redundance_x\",\"redundance_y\",\"redundance_z\",\"omega\",\"t_prio\",\"t_post\",\"outlier\" FROM \"LocalPointApost\"  JOIN \"PointApriori\" ON \"LocalPointApost\".\"id\" = \"PointApriori\".\"id\" AND \"PointApriori\".\"group_id\" = ? AND \"PointApriori\".\"enable\" = TRUE ORDER BY \"LocalPointApost\".\"id\" ASC";
		String sqlGlobalPointApost = "SELECT \"id\",\"point_id\", \"x\", \"y\", \"z\",\"sigma_x\",\"sigma_y\",\"sigma_z\",\"nabla_x\",\"nabla_y\",\"nabla_z\",(\"x\"-\"x0\") AS \"vx\",(\"y\"-\"y0\") AS \"vy\",(\"z\"-\"z0\") AS \"vz\",\"redundance_x\",\"redundance_y\",\"redundance_z\",\"omega\",\"t_prio\",\"t_post\",\"outlier\" FROM \"GlobalPointApost\" JOIN \"PointApriori\" ON \"GlobalPointApost\".\"point_id\" = \"PointApriori\".\"point_id\" AND \"PointApriori\".\"group_id\" = ? AND \"PointApriori\".\"enable\" = TRUE ORDER BY \"PointApriori\".\"id\" ASC";
		if (isGlobal && groupId<0) {
			sqlGlobalPointApost = "SELECT \"id\",\"point_id\", \"x\", \"y\", \"z\",\"sigma_x\",\"sigma_y\",\"sigma_z\",\"nabla_x\",\"nabla_y\",\"nabla_z\",0 AS \"vx\",0 AS \"vy\",0 AS \"vz\",\"redundance_x\",\"redundance_y\",\"redundance_z\",\"omega\",\"t_prio\",\"t_post\",\"outlier\" FROM \"GlobalPointApost\" WHERE \"id\" > ? ORDER BY \"id\" ASC";
		}

		List<PointRow> points = new ArrayList<PointRow>();
		PreparedStatement statement = this.db.getPreparedStatement(isGlobal?sqlGlobalPointApost:sqlLocalPointApost);
		statement.setInt(1, groupId);
		
		ResultSet groupSet = statement.executeQuery();
		if (!groupSet.wasNull()) {
			while(groupSet.next()) {
				int id          = groupSet.getInt("id");
				String pointId  = groupSet.getString("point_id");
				double x        = groupSet.getDouble("x");
				double y        = groupSet.getDouble("y");
				double z        = groupSet.getDouble("z");
				double sigmaX   = groupSet.getDouble("sigma_x");
				double sigmaY   = groupSet.getDouble("sigma_y");
				double sigmaZ   = groupSet.getDouble("sigma_z");
				double nablaX   = groupSet.getDouble("nabla_x");
				double nablaY   = groupSet.getDouble("nabla_y");
				double nablaZ   = groupSet.getDouble("nabla_z");
				double vX       = groupSet.getDouble("vx");
				double vY       = groupSet.getDouble("vy");
				double vZ       = groupSet.getDouble("vz");
				double rX       = groupSet.getDouble("redundance_x");
				double rY       = groupSet.getDouble("redundance_y");
				double rZ       = groupSet.getDouble("redundance_z");
				double omega    = groupSet.getDouble("omega");
				double tPrio    = groupSet.getDouble("t_prio");
				double tPost    = groupSet.getDouble("t_post");
				boolean outlier = groupSet.getBoolean("outlier"); 
				
				PointRow point = new PointRow(id, pointId, x, y, z, true);
				point.setOutlier(outlier);
				point.setStdX(sigmaX);
				point.setStdY(sigmaY);
				point.setStdZ(sigmaZ);
				point.setRedundanceX(rX);
				point.setRedundanceY(rY);
				point.setRedundanceZ(rZ);
				point.setNablaX(nablaX);
				point.setNablaY(nablaY);
				point.setNablaZ(nablaZ);
				point.setErrorX(vX);
				point.setErrorY(vY);
				point.setErrorZ(vZ);
				point.setOmega(omega);
				point.setTprio(tPrio);
				point.setTpost(tPost);
				
				points.add(point);
			}
		}
		return points;
	}
	
	private void deleteTableRow(DataRow rowData) {
		try {
			if (rowData.getId() < 0)
				return;
			
			String tableName = "";
			if (rowData instanceof PolarObservationRow) 
				tableName = "PolarObservation";
			else if(rowData instanceof PointRow)
				tableName = "PointApriori";
			if (tableName.isEmpty())
				return;
			
			String sql = "DELETE FROM \"" + tableName + "\" WHERE \"id\" = ?";
			PreparedStatement statement = this.db.getPreparedStatement(sql);
			statement.setInt(1, rowData.getId());
			statement.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Speichert einen Roh-Datensatz (Polarbeobachtungen oder Koordinaten)
	 * @param groupId
	 * @param rowData
	 * @return isSaved
	 */
	public boolean saveTableRow(int groupId, DataRow rowData) {
		try {
			if (rowData instanceof PolarObservationRow) {
				PolarObservationRow obs = (PolarObservationRow)rowData;
				int id = obs.getId();
				String pointId = obs.getEndPointId();
				double dist    = obs.getDistance3D();
				double azimuth = obs.getAzimuth();
				double zenith  = obs.getZenith();
				boolean enable = obs.isEnabled();
				Date obsDate   = obs.getObservationTime();
				String sql;
				// INSERT
				if (id < 0) 
					sql = "INSERT INTO \"PolarObservation\" (\"group_id\", \"point_id\", \"distance\", \"azimuth\", \"zenith\", \"enable\", \"date\") VALUES (?,?,?,?,?,?,?)";
				// UPDATE
				else 
					sql = "UPDATE \"PolarObservation\" SET \"group_id\" = ?, \"point_id\" = ?, \"distance\" = ?, \"azimuth\" = ?, \"zenith\" = ?, \"enable\" = ?, \"date\" = ? WHERE \"id\" = ?";
				
				PreparedStatement statement = this.db.getPreparedStatement(sql);
				statement.setInt(1, groupId);
				statement.setString(2, pointId);
				statement.setDouble(3, dist);
				statement.setDouble(4, azimuth);
				statement.setDouble(5, zenith);
				statement.setBoolean(6, enable);
				if (obsDate == null)
					statement.setNull(7, java.sql.Types.TIMESTAMP);
				else
					statement.setTimestamp(7, new Timestamp(obsDate.getTime()));
	
				if (id >= 0) 
					statement.setInt(8, id);
				
				// Speichern
				if (statement.executeUpdate()>0) {
					// Bestimme bei neuen Daten die letzte id
					if (id < 0) 
						id = this.db.getLastInsertId();
				}
				
				if (id >= 0)
					obs.setId(id);
			}
			else if(rowData instanceof PointRow) {
				// Es muss die globale Gruppe sein
				// die Group-ID ist demnach dem ROOT-Element zu entnehmen, wenn diese nicht uebergeben wurde
				if (groupId < 0)
					groupId = this.dataTreeMenu.getGlobalSystemId();
				
				PointRow point = (PointRow)rowData;
				
				int id = point.getId();
				String pointId = point.getPointId();
				double x0 = point.getX();
				double y0 = point.getY();
				double z0 = point.getZ();
				boolean enable = point.isEnabled();
				String sql;
				// INSERT
				if (id < 0) 
					sql = "INSERT INTO \"PointApriori\" (\"group_id\", \"point_id\", \"x0\", \"y0\", \"z0\", \"enable\") VALUES (?,?,?,?,?,?)";
				// UPDATE
				else 
					sql = "UPDATE \"PointApriori\" SET \"group_id\" = ?, \"point_id\" = ?, \"x0\" = ?, \"y0\" = ?, \"z0\" = ?, \"enable\" = ? WHERE \"id\" = ?";
				
				PreparedStatement statement = this.db.getPreparedStatement(sql);
				statement.setInt(1, groupId);
				statement.setString(2, pointId);
				statement.setDouble(3, x0);
				statement.setDouble(4, y0);
				statement.setDouble(5, z0);
				statement.setBoolean(6, enable);
				
				if (id >= 0) 
					statement.setInt(7, id);
				
				// Speichern
				if (statement.executeUpdate()>0) {
					// Bestimme bei neuen Daten die letzte id
					if (id < 0)
						id = this.db.getLastInsertId();
				}
				
				if (id >= 0)
					point.setId(id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
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
	 * Leert die Tabellen mit der Gruppen-ID
	 * @param node
	 */
	private void truncateSystem(SystemNode node) {
		try {
			String sqlPointAprio = "DELETE FROM \"PointApriori\" WHERE \"group_id\" = ?";
			String sqlPolar      = "DELETE FROM \"PolarObservation\" WHERE \"group_id\" = ?";
			//String sqlGlobalCoVar = "UPDATE \"GeneralSetting\" SET \"covar\" = ? WHERE \"id\" = 1";
			PreparedStatement statement = this.db.getPreparedStatement(sqlPointAprio);
			statement.setInt(1, node.getId());
			statement.execute();
			
			statement = this.db.getPreparedStatement(sqlPolar);
			statement.setInt(1, node.getId());
			statement.execute();
			
			if (node instanceof GlobalSystemNode) {
				this.deleteCovarianceMatrix(-1); // Globale a-priori Covar loeschen 
			}
			
			node.getUserObject().clearTables();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Liest eine Rohdatendatei in den aktuellen Knoten ein
	 */
	private void importSourceFile(SystemNode node) {
		DefaultFileFilter[] filters = new DefaultFileFilter[] {
				new DefaultFileFilter(),
				new CoVarFileFilter(),
				new TXTFileFilter(),
				new XYZFileFilter(),
				new XMLFileFilter()
		};
		File selectedFile = this.openSingleFileDialog(filters, JFileChooser.OPEN_DIALOG);
		if(selectedFile == null)
			return;

		LockFileReader reader = null;
		if(selectedFile.getName().toLowerCase().endsWith(".xml")) {
			reader = new XMLFileReader(this, selectedFile);
			if (reader.readSourceFile()) {
				this.selectRootNode();
				// Speichere nach XML-Import auch die Covar
				this.saveGlobalCovarianceMatrixApriori(((GlobalTabbedPane)this.dataTreeMenu.getRootObject()).getUpperSymmMatrixTable().getModel().getMatrix());
				if (!((XMLFileReader)reader).isValidDocument()) {
					JOptionPane.showMessageDialog(this.owner, 
							String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "msg.invalidXMLBody"), selectedFile.getName()),
							this.babel.getString(this.getClass().getSimpleName(), "msg.invalidXMLTitle"),
							JOptionPane.INFORMATION_MESSAGE
					);
				}
			}
			else {
				JOptionPane.showMessageDialog(this.owner, 
						String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "err.ioFileErrorBody"), selectedFile.getName()),
						this.babel.getString(this.getClass().getSimpleName(), "err.ioFileErrorTitle"),
						JOptionPane.ERROR_MESSAGE
				);
			}
		}
		else {
			if ((selectedFile.getName().toLowerCase().endsWith(".qxx") || selectedFile.getName().toLowerCase().endsWith(".cxx")) && this.dataTreeMenu.getRootObject() instanceof GlobalTabbedPane) {
				GlobalTabbedPane tabbedPane = (GlobalTabbedPane) this.dataTreeMenu.getRootObject();
				reader = new CoVarFileReader(node.getId(), this, selectedFile, 
						tabbedPane.getUpperSymmMatrixTable().getRowCount());
			}
			else if (node instanceof GlobalSystemNode)
				reader = new PointFileReader(node.getId(), this, selectedFile);
		
			else if (node instanceof LocalSystemNode)
				reader = new ObservationFileReader((LocalSystemNode)node, this, selectedFile, 
						selectedFile.getName().toLowerCase().endsWith(".xyz"));
			else
				return;
			
			if (reader.readSourceFile()) {
				DataTabbedPane dataTabbedPane = node.getUserObject();
				this.loadSystem(node.getId(), dataTabbedPane);
				if (dataTabbedPane instanceof GlobalTabbedPane) {
					this.saveGlobalCovarianceMatrixApriori(((GlobalTabbedPane)dataTabbedPane).getUpperSymmMatrixTable().getModel().getMatrix());
				}
				JOptionPane.showMessageDialog(this.owner, 
						String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "msg.ioReadCompleteBody"), ((RowFileReader)reader).getRowCounter(), selectedFile.getName()),
						this.babel.getString(this.getClass().getSimpleName(), "msg.ioReadCompleteTitle"),
						JOptionPane.INFORMATION_MESSAGE
				);
			}
			else {
				JOptionPane.showMessageDialog(this.owner, 
						String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "err.ioFileErrorBody"), selectedFile.getName()),
						this.babel.getString(this.getClass().getSimpleName(), "err.ioFileErrorTitle"),
						JOptionPane.ERROR_MESSAGE
				);
			}
		}		
	}
	
	private void exportReport() {
		List<String> templates = this.getTemplates();
		HTMLTemplateFileFilter filters[] = new HTMLTemplateFileFilter[templates.size()];
		for (int j=0, i=templates.size()-1; i>=0; i--, j++) {
			filters[j] = new HTMLTemplateFileFilter(templates.get(i));
		}
		File reportFile = this.openSingleFileDialog(filters, JFileChooser.SAVE_DIALOG);
		HTMLTemplateFileFilter selectedFilter = (HTMLTemplateFileFilter)this.fileChooser.getFileFilter();
		String templateName = selectedFilter.getName();
		if (reportFile == null || templateName == null || templateName.trim().isEmpty())
			return;
		this.createReport(reportFile, templateName);
	}
	
	private void saveGroupName(int id, String newGroupName) throws SQLException {
		String sqlGroup = "UPDATE \"PointGroup\" SET \"name\" = ? WHERE \"id\" = ?";
		PreparedStatement statement = this.db.getPreparedStatement(sqlGroup);
		statement.setString(1, newGroupName);
		statement.setInt(2, id);
		statement.executeUpdate();
	}
	
	/**
	 * Fuegt ein leeres lokales System hinzu
	 * @param startPointId
	 */
	public int addLocalSystem(String startPointId) {
		return this.addSystem(startPointId, false);
	}
	
	/**
	 * Fuegt ein leeres System hinzu und liefert die ID zurueck
	 * @param startPointId
	 * @param isGlobalPointGroup
	 * @return id
	 */
	private int addSystem(String startPointId, boolean isGlobalPointGroup) {
		if (!this.hasDataBase())
			return -1;
		int id = -1;
		try {
			while (!isGlobalPointGroup && startPointId.trim().isEmpty()) {
				startPointId = (String)JOptionPane.showInputDialog(
						this.owner,
						this.babel.getString(this.getClass().getSimpleName(), "msg.newLocalSystemBody"),
						this.babel.getString(this.getClass().getSimpleName(), "msg.newLocalSystemTitle"),
						JOptionPane.QUESTION_MESSAGE
				);
				// Anwender hat Dialog abgebrochen
				if (startPointId == null)
					return -1;
			}
			String nodeName; 
			if (isGlobalPointGroup)
				nodeName = this.babel.getString("DataTreeMenu", "root");
			else
				nodeName = String.format(Locale.ENGLISH, this.babel.getString("DataTreeMenu", "leaf"), startPointId);
			// Fuege Datensatz hinzu zur DB und ermittle die hierfuer benutzte ID
			String sqlGroup      = "INSERT INTO \"PointGroup\" (\"fixed_tx\", \"fixed_ty\", \"fixed_tz\", \"fixed_m\", \"fixed_rx\", \"fixed_ry\", \"fixed_rz\", \"enable\", \"target_system\", \"name\", \"point_id\", \"sigma2aprio\") VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
			String sqlStochModel = "INSERT INTO \"LasertrackerUncertaintyParameter\" (\"id\",\"sigma_x_instrument\",\"sigma_y_instrument\",\"sigma_z_instrument\",\"sigma_scale_instrument\",\"sigma_add_instrument\",\"sigma_fourier_aa1_instrument\",\"sigma_fourier_aa2_instrument\",\"sigma_fourier_ba1_instrument\",\"sigma_fourier_ba2_instrument\",\"sigma_fourier_ae0_instrument\",\"sigma_fourier_ae1_instrument\",\"sigma_fourier_ae2_instrument\",\"sigma_fourier_be1_instrument\",\"sigma_fourier_be2_instrument\",\"sigma_alpha_instrument\",\"sigma_gamma_instrument\",\"sigma_ex_instrument\",\"sigma_by_instrument\",\"sigma_bz_instrument\",\"sigma_scale_target\",\"sigma_add_target\",\"sigma_azimuth_target\",\"sigma_zenith_target\",\"sigma_centring_azimuth_target\",\"sigma_centring_zenith_target\") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String sqlDistType   = "INSERT INTO \"DistributionUncertaintyParameter\" (\"id\",\"distribution_x_instrument\",\"distribution_y_instrument\",\"distribution_z_instrument\",\"distribution_scale_instrument\",\"distribution_add_instrument\",\"distribution_fourier_aa1_instrument\",\"distribution_fourier_aa2_instrument\",\"distribution_fourier_ba1_instrument\",\"distribution_fourier_ba2_instrument\",\"distribution_fourier_ae0_instrument\",\"distribution_fourier_ae1_instrument\",\"distribution_fourier_ae2_instrument\",\"distribution_fourier_be1_instrument\",\"distribution_fourier_be2_instrument\",\"distribution_alpha_instrument\",\"distribution_gamma_instrument\",\"distribution_ex_instrument\",\"distribution_by_instrument\",\"distribution_bz_instrument\",\"distribution_scale_target\",\"distribution_add_target\",\"distribution_azimuth_target\",\"distribution_zenith_target\",\"distribution_centring_azimuth_target\",\"distribution_centring_zenith_target\") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String sqlCorrModel  = "INSERT INTO \"CorrelationFunctionParameter\" (\"group_id\", \"obs_type\", \"corr_fun\",\"sigma\",\"max_dt\",\"coef_a\",\"coef_b\",\"coef_c\") VALUES (?,?,?,?,?,?,?,?)";
			TransformationParameterSet trafoSet = new TransformationParameterSet3D();
			
			// Defaulteinstellung --> Maßstab im lokalen System fest (m=1)
			if (!isGlobalPointGroup)
				trafoSet.setRestriction(TransformationParameterSet.FIXED_SCALE);

			PreparedStatement statementGroup = this.db.getPreparedStatement(sqlGroup);
			statementGroup.setBoolean(1, trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_X)); //tx
			statementGroup.setBoolean(2, trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Y)); //ty
			statementGroup.setBoolean(3, trafoSet.isRestricted(TransformationParameterSet.FIXED_TRANSLATION_Z)); //tz
			statementGroup.setBoolean(4, trafoSet.isRestricted(TransformationParameterSet.FIXED_SCALE));         // m
			statementGroup.setBoolean(5, trafoSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_X));    //rx
			statementGroup.setBoolean(6, trafoSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Y));    //ry
			statementGroup.setBoolean(7, trafoSet.isRestricted(TransformationParameterSet.FIXED_ROTATION_Z));    //rz
											
			statementGroup.setBoolean(8, true); //enable
			statementGroup.setBoolean(9, isGlobalPointGroup); //globale Gruppe
			
			statementGroup.setString(10, nodeName); // Name der Gruppe
			statementGroup.setString(11, startPointId); // Punktnummer des Standpunktes bei Polargruppe
			
			statementGroup.setDouble(12, 1.0); // Varianzfaktor a-priori fuer die Gruppe
			
			// Fuege Gruppe hinzu
			int isExecuted = statementGroup.executeUpdate();
			
			if (isExecuted > 0) {
				// Bestimme die ID der Gruppe
				id = this.db.getLastInsertId();
				if (id >= 0) {
					if (isGlobalPointGroup) {
						this.dataTreeMenu.setGlobalSystemId(id);
					}
					else {
						UncertaintyModel uncertaintyModel = new LasertrackerUncertaintyModel();
						double sigmas[] = uncertaintyModel.getDefaultSigmas();
						//for (int i=5; i<=15; i++)
						//	sigmas[i] *= Constant.RHO_RAD2GRAD;
						//for (int i=21; i<=22; i++)
						//	sigmas[i] *= Constant.RHO_RAD2GRAD;
												
						//double sigmas[] = new double[] {0.000010,0.000010,0.000010,0.0000005,0.0000004,0.000020,0.000023,0.000025,0.000028,0.000069,0.000047,0.000066,0.000056,0.000006,0.000040,0.000024,0.00000001,0.00000007,0.00000010,0.000002,0.000010,0.000006,0.000006};
						statementGroup = this.db.getPreparedStatement(sqlStochModel);
						statementGroup.setInt(1, id); 
						for (int i=0, j=2; i<sigmas.length; i++, j++) {
							double scale = uncertaintyModel.isAngle(i)?Constant.RHO_RAD2GRAD:1.0;
							statementGroup.setDouble(j, scale*sigmas[i]);
						}
						statementGroup.executeUpdate();
						
						statementGroup = this.db.getPreparedStatement(sqlDistType);
						statementGroup.setInt(1, id); 
						for (int i=0, j=2; i<sigmas.length; i++, j++) {
							statementGroup.setInt(j, PreAnalysis.DISTRIBUTION_NORMAL);
						}
						statementGroup.executeUpdate();

						int obsTypes[] = new int[] {LasertrackerUncertaintyModel.DISTANCE3D, LasertrackerUncertaintyModel.AZIMUTHANGLE, LasertrackerUncertaintyModel.ZENITHANGLE};
						statementGroup = this.db.getPreparedStatement(sqlCorrModel);
						for (int k=0; k<obsTypes.length; k++) {
							int obsType = obsTypes[k];
							statementGroup.setInt(1, id); 
							statementGroup.setInt(2, obsType);
							statementGroup.setInt(3, -1); // Func
							
							statementGroup.setDouble(4, 1); // Sigma
							statementGroup.setDouble(5, 0); // maxDt
							
							statementGroup.setDouble(6, 1); // a
							statementGroup.setDouble(7, 0); // b
							statementGroup.setDouble(8, 0); // c
							
							statementGroup.executeUpdate();
						}
						this.dataTreeMenu.addLocalSystemNode(id, nodeName, startPointId);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	private void removeLocalSystem(SystemNode systemNode) {
		String sqls[] = new String[] {
				"DELETE FROM \"PointGroup\" WHERE \"id\" = ?",
				"DELETE FROM \"TransformationParameter\" WHERE \"id\" = ?",
				"DELETE FROM \"DistributionUncertaintyParameter\" WHERE \"id\" = ?",
				"DELETE FROM \"LasertrackerUncertaintyParameter\" WHERE \"id\" = ?",
				"DELETE FROM \"CorrelationFunctionParameter\" WHERE \"group_id\" = ?"
		};
		
		try {
			if (systemNode instanceof LocalSystemNode) {
				this.truncateSystem(systemNode);
				
				for (int i=0; i<sqls.length; i++) {
					String sql = sqls[i];
					PreparedStatement statement = this.db.getPreparedStatement(sql);
					statement.setInt(1, systemNode.getId());
					statement.execute();
				}
				// Baue Menu neu auf
				this.dataTreeMenu.removeNode(systemNode);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Aktiviert/Deaktiviert eine lokale Gruppe und speichert die zu bestimmenden Trafo-Parameters
	 * @param id
	 * @param enable
	 * @param fixedTrafoParams
	 */
	public void saveLocalGroupSettings(int id, boolean enable, double sigma2apriori, boolean fixedTrafoParams[]) {
		try {
			String sql = "";
			
			if (fixedTrafoParams == null && id < 0)
				return;
			else if (fixedTrafoParams == null)
				sql = "UPDATE \"PointGroup\" SET \"enable\" = ? WHERE \"target_system\" = FALSE AND \"id\" = ?";
			else if (id < 0)
				sql = "UPDATE \"PointGroup\" SET \"fixed_tx\" = ?, \"fixed_ty\" = ?, \"fixed_tz\" = ?, \"fixed_rx\" = ?, \"fixed_ry\" = ?, \"fixed_rz\" = ?, \"fixed_m\" = ? WHERE \"target_system\" = FALSE";
			else 
				sql = "UPDATE \"PointGroup\" SET \"fixed_tx\" = ?, \"fixed_ty\" = ?, \"fixed_tz\" = ?, \"fixed_rx\" = ?, \"fixed_ry\" = ?, \"fixed_rz\" = ?, \"fixed_m\" = ?, \"sigma2aprio\" = ?, \"enable\" = ? WHERE \"target_system\" = FALSE AND \"id\" = ?";
			
			PreparedStatement statement = this.db.getPreparedStatement(sql);

			if (fixedTrafoParams!=null) {
				for (int i=0; i<fixedTrafoParams.length; i++) {
					statement.setBoolean(i+1, fixedTrafoParams[i]);
				}
				if (id >= 0) {
					statement.setDouble(fixedTrafoParams.length+1, sigma2apriori);
					statement.setBoolean(fixedTrafoParams.length+2, enable);
					statement.setInt(fixedTrafoParams.length+3, id);
				}
			}
			else {
				statement.setBoolean(1, enable);
				statement.setInt(2, id);
			}
			statement.executeUpdate();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Speichert die Kovarianzmatrix des Zielsystems
	 * @param Cxx
	 */
	public void saveGlobalCovarianceMatrixApriori(Matrix Cll) {
		try {
			int groupId = -1; //globale a-priori Covar des Zielsystems
			this.deleteCovarianceMatrix(groupId);
			String sqlMatrixInsert = "INSERT INTO \"CovarianceMatrix\" (\"id\", \"row\", \"column\", \"value\") VALUES (?,?,?,?)";
			PreparedStatement statementInsert = this.db.getPreparedStatement(sqlMatrixInsert);
			
			for (int row=0; row<Cll.numRows(); row++) {
				for (int column=row; column<Cll.numColumns(); column++) {					
					// Fuege neue Datensaetze ein
					statementInsert.setInt(1, groupId);
					statementInsert.setInt(2, row);
					statementInsert.setInt(3, column);
					statementInsert.setDouble(4, Cll.get(row, column));
					statementInsert.execute();
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Speichert die a-priori Covar des Zielsystems
	 * @param row
	 * @param column
	 * @param data
	 */
	public void saveGlobalCovarianceMatrixApriori(int row, int column, double data[]) {
		this.saveCovarianceMatrixRow(-1, row, column, data);
	}
	
	/**
	 * Speichert ein Element der CoVar
	 * @param matrixId
	 * @param row
	 * @param column
	 * @param data
	 */
	public void saveCovarianceMatrixElement(int matrixId, int row, int column, double data) {
		this.saveCovarianceMatrixRow(matrixId, row, column, new double[] {data});
	}
	
	/**
	 * Speichert eine Zeile der CoVar beginnend ab column
	 * @param matrixId
	 * @param row
	 * @param column
	 * @param data
	 */
	public void saveCovarianceMatrixRow(int matrixId, int row, int column, double data[]) {
		try {
			String sqlMatrixInsert = "INSERT INTO \"CovarianceMatrix\" (\"id\", \"row\", \"column\", \"value\") VALUES (?,?,?,?)";
			String sqlMatrixDelete = "DELETE FROM \"CovarianceMatrix\" WHERE \"id\" = ? AND \"row\" = ? AND \"column\" = ?";
			PreparedStatement statementInsert = this.db.getPreparedStatement(sqlMatrixInsert);
			PreparedStatement statementDelete = this.db.getPreparedStatement(sqlMatrixDelete);
			
			for (int i=0; i<data.length; i++) {
				int r = Math.min(row, column+i);
				int c = Math.max(row, column+i);
	
				// Loesche zunachst betreffliche Datensaetze
				statementDelete.setInt(1, matrixId);
				statementDelete.setInt(2, r);
				statementDelete.setInt(3, c);
				statementDelete.execute();

				// Fuege neue Datensaetze ein
				statementInsert.setInt(1, matrixId);
				statementInsert.setInt(2, r);
				statementInsert.setInt(3, c);
				statementInsert.setDouble(4, data[i]);
				statementInsert.execute();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void saveSettings(SystemNode node) {
		this.saveSettings(node, false, false);
	}
	
	private void saveSettings(SystemNode node, boolean bequeathOptions, boolean bequeathUncertainties) {
		if (node instanceof GlobalSystemNode) {
			GlobalSystemNode globalNode = (GlobalSystemNode)node;
			GlobalSettingPanel globalSettingPanel = (GlobalSettingPanel)globalNode.getUserObject().getSettingPanel();
			this.saveGlobalSettings(
					globalSettingPanel.getProjectIdentifier(), 
					globalSettingPanel.getMaxIteration(),
					globalSettingPanel.getMonteCarloSamples(),
					globalSettingPanel.getPropertyValueAndTestPower(),
					globalSettingPanel.isFreenetAdjustment(),
					globalSettingPanel.getUncertaintyModelType(),
					globalSettingPanel.exportCovarianceMatrix(),
					globalSettingPanel.applyVarianceFactorAposteriori()
			);
		}
		else if (node instanceof LocalSystemNode) {
			LocalSystemNode localNode = (LocalSystemNode)node;
			LocalSettingPanel localSettingPanel = (LocalSettingPanel)localNode.getUserObject().getSettingPanel();
			int id = bequeathOptions || bequeathUncertainties?-1:localNode.getId();
			
			if (bequeathUncertainties || !bequeathUncertainties && id >= 0) {
				this.saveLocalUncertainties(
						id, 
						localSettingPanel.getAprioriStandardDeviationsAndDistributions(),
						localSettingPanel.getTimeDependentCoorelationFunctionValues()
				);
			}
			if (bequeathOptions || !bequeathOptions && id >= 0) {
				this.saveLocalGroupSettings(
						id, 
						localSettingPanel.isEnabled(),
						localSettingPanel.getVarianceFactorApriori(),
						localSettingPanel.getFixedTransformationParameter()
				);
			}
		}
	}
	
	/**
	 * Speichert die globalen Projekteinstellungen
	 * 
	 * @param projectDetails
	 * @param iteration
	 * @param monteCarloSamples
	 * @param alphaBeta
	 * @param freenet
	 * @param uncertaintyModelType
	 */
	private void saveGlobalSettings(String projectDetails[], int iteration, int monteCarloSamples, double alphaBeta[], boolean freenet, int uncertaintyModelType, boolean exportCovar, boolean applySigmaApost) {
		try {
			String sqlSetting = "UPDATE \"GeneralSetting\" SET \"name\"=?,\"description\"=?,\"operator\"=?,\"alpha\"=?,\"beta\"=?,\"iteration\"=?,\"monte_carlo_samples\"=?,\"freenet\"=?,\"stochastic_model\"=?,\"export_covar\"=?,\"appy_sigma_apost\"=? WHERE \"id\"=1";

			PreparedStatement statement = this.db.getPreparedStatement(sqlSetting);
			statement.setString(1, projectDetails[0]);
			statement.setString(2, projectDetails[1]);
			statement.setString(3, projectDetails[2]);
			
			statement.setDouble(4, alphaBeta[0]);
			statement.setDouble(5, alphaBeta[1]);

			statement.setInt(6, iteration);
			statement.setInt(7, monteCarloSamples);
			
			statement.setBoolean(8, freenet);
			statement.setInt(9, uncertaintyModelType);
			
			statement.setBoolean(10, exportCovar);
			statement.setBoolean(11, applySigmaApost);
			
			statement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Speichert die lokalen Einstellungen zum Unsicherheitsbudget
	 * @param id
	 * @param sigmas
	 * @param fixedTrafParams
	 * @param enable
	 */
	public void saveLocalUncertainties(int id, UncertaintyRow uncertaintyRows[], TimeDependentUncertaintyRow timeDependentUncertaintyRows[]) {
		try {
			String sqlWhere = id < 0?" WHERE \"id\" > ?":" WHERE \"id\" = ?";
			String sqlStochModel = "UPDATE \"LasertrackerUncertaintyParameter\" SET \"sigma_x_instrument\"=?,\"sigma_y_instrument\"=?,\"sigma_z_instrument\"=?,\"sigma_scale_instrument\"=?,\"sigma_add_instrument\"=?,\"sigma_fourier_aa1_instrument\"=?,\"sigma_fourier_aa2_instrument\"=?,\"sigma_fourier_ba1_instrument\"=?,\"sigma_fourier_ba2_instrument\"=?,\"sigma_fourier_ae0_instrument\"=?,\"sigma_fourier_ae1_instrument\"=?,\"sigma_fourier_ae2_instrument\"=?,\"sigma_fourier_be1_instrument\"=?,\"sigma_fourier_be2_instrument\"=?,\"sigma_alpha_instrument\"=?,\"sigma_gamma_instrument\"=?,\"sigma_ex_instrument\"=?,\"sigma_by_instrument\"=?,\"sigma_bz_instrument\"=?,\"sigma_scale_target\"=?,\"sigma_add_target\"=?,\"sigma_azimuth_target\"=?,\"sigma_zenith_target\"=?,\"sigma_centring_azimuth_target\"=?,\"sigma_centring_zenith_target\"=? " + sqlWhere;
			String sqlDistTypes  = "UPDATE \"DistributionUncertaintyParameter\" SET \"distribution_x_instrument\"=?,\"distribution_y_instrument\"=?,\"distribution_z_instrument\"=?,\"distribution_scale_instrument\"=?,\"distribution_add_instrument\"=?,\"distribution_fourier_aa1_instrument\"=?,\"distribution_fourier_aa2_instrument\"=?,\"distribution_fourier_ba1_instrument\"=?,\"distribution_fourier_ba2_instrument\"=?,\"distribution_fourier_ae0_instrument\"=?,\"distribution_fourier_ae1_instrument\"=?,\"distribution_fourier_ae2_instrument\"=?,\"distribution_fourier_be1_instrument\"=?,\"distribution_fourier_be2_instrument\"=?,\"distribution_alpha_instrument\"=?,\"distribution_gamma_instrument\"=?,\"distribution_ex_instrument\"=?,\"distribution_by_instrument\"=?,\"distribution_bz_instrument\"=?,\"distribution_scale_target\"=?,\"distribution_add_target\"=?,\"distribution_azimuth_target\"=?,\"distribution_zenith_target\"=?,\"distribution_centring_azimuth_target\"=?,\"distribution_centring_zenith_target\"=? " + sqlWhere;
			String sqlCorrModel  = "UPDATE \"CorrelationFunctionParameter\" SET \"corr_fun\"=?,\"sigma\"=?,\"max_dt\"=?,\"coef_a\"=?,\"coef_b\"=?,\"coef_c\"=? " + sqlWhere + " AND \"obs_type\" = ?";

			PreparedStatement statement = this.db.getPreparedStatement(sqlStochModel);

			for (int i=0; i<uncertaintyRows.length; i++) {
				statement.setDouble(i+1, uncertaintyRows[i].getValue());
			}
			statement.setInt(uncertaintyRows.length+1, id);
			statement.executeUpdate();
			
			statement = this.db.getPreparedStatement(sqlDistTypes);

			for (int i=0; i<uncertaintyRows.length; i++) {
				statement.setDouble(i+1, uncertaintyRows[i].getDistribution());
			}
			statement.setInt(uncertaintyRows.length+1, id);
			statement.executeUpdate();
			if (timeDependentUncertaintyRows != null) {
				int obsTypes[] = new int[] {LasertrackerUncertaintyModel.DISTANCE3D, LasertrackerUncertaintyModel.AZIMUTHANGLE, LasertrackerUncertaintyModel.ZENITHANGLE};
				statement = this.db.getPreparedStatement(sqlCorrModel);
				for (int i=0; i<obsTypes.length; i++) {
					TimeDependentUncertaintyRow corrFun = timeDependentUncertaintyRows[i];
					statement.setInt(1, corrFun.getCorrelationFunctionType());
					statement.setDouble(2, corrFun.getUncertainty());
					statement.setDouble(3, corrFun.getMaximalTimeDifference());
					statement.setDouble(4, corrFun.getCoefA());
					statement.setDouble(5, corrFun.getCoefB());
					statement.setDouble(6, corrFun.getCoefC());
					statement.setInt(7, id);
					statement.setInt(8, obsTypes[i]);
					statement.executeUpdate();
				}
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prueft, ob es eine neue Version von LOGI im Web gibt
	 */
	private void checkUpdates() {
		UpdateChecker updateChecker = new UpdateChecker(
				"http://bundleadjust.sourceforge.net/update.php",
				Logi.VERSION,
				Logi.BUILD
		);
		if (!updateChecker.connectServer("logi")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.updateBody"), 
					this.babel.getString(this.getClass().getSimpleName(), "err.updateTitle"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		switch (updateChecker.hasUpdates()) {
			case 0:
				JOptionPane.showMessageDialog(this.owner, 
						this.babel.getString(this.getClass().getSimpleName(), "msg.noUpdateBody"), 
						this.babel.getString(this.getClass().getSimpleName(), "msg.noUpdateTitle"), 
						JOptionPane.INFORMATION_MESSAGE);
			break;
			
			case 1:
				if (Desktop.isDesktopSupported()) {
					if (JOptionPane.showConfirmDialog(
							this.owner, 
							String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "msg.updateAvailableDownloadBody"), updateChecker.getReleasInformation().getVersionsNumber(), updateChecker.getReleasInformation().getBuildNumber()), 
							this.babel.getString(this.getClass().getSimpleName(), "msg.updateAvailableDownloadTitle"), 
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					
						try { 
							Desktop.getDesktop().browse( updateChecker.getReleasInformation().getURI() ); 
						} 
						catch ( Exception e ) { 
							JOptionPane.showMessageDialog(this.owner, 
								this.babel.getString(this.getClass().getSimpleName(), "err.downloadUpdateBody"), 
								this.babel.getString(this.getClass().getSimpleName(), "err.downloadUpdateTitle"),
								JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				else {
					JOptionPane.showMessageDialog(this.owner, 
						String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "msg.updateAvailable"), updateChecker.getReleasInformation().getVersionsNumber(), updateChecker.getReleasInformation().getBuildNumber()), 
						this.babel.getString(this.getClass().getSimpleName(), "msg.updateAvailableTitle"),  
						JOptionPane.INFORMATION_MESSAGE);
				}
				
			break;
				
			default:
				if (JOptionPane.showConfirmDialog(
						this.owner, 
						this.babel.getString(this.getClass().getSimpleName(), "msg.possibleUpdateAvailableBody"), 
						this.babel.getString(this.getClass().getSimpleName(), "msg.possibleUpdateAvailableTitle"), 
				        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					
					try { 
						Desktop.getDesktop().browse( updateChecker.getReleasInformation().getURI() ); 
					} 
					catch ( Exception e ) { 
						JOptionPane.showMessageDialog(this.owner, 
								this.babel.getString(this.getClass().getSimpleName(), "err.downloadUpdateBody"), 
								this.babel.getString(this.getClass().getSimpleName(), "err.downloadUpdateTitle"),
								JOptionPane.ERROR_MESSAGE);
					}
					
				}
			break;
		}
		
	}
	
	
	/**
	 * Schließt die Datenbank
	 */
	private void closeDataBase() {
		if (this.db != null && this.db.isOpen())
			this.db.close();
	}
	
	private boolean checkDataBaseVersion(boolean isNewDataBase) throws SQLException {
		boolean validDB = true;
		
		double dbVersion   = BundleAdjustmentProjectDatabase.DATABASE_VERSION;
		double logiVersion = Logi.VERSION;
		int logiBuild      = Logi.BUILD;
		
		PreparedStatement statement = null;
		String sqlSetVersion, sqlSelectVersion = "SELECT \"logi_version\", \"logi_build\", \"db_version\" FROM \"GeneralSetting\" WHERE \"id\" = 1";

		if (isNewDataBase) {
			GlobalSettingPanel globalSettingPanel = (GlobalSettingPanel)this.dataTreeMenu.getRootObject().getSettingPanel();
			sqlSetVersion = "UPDATE \"GeneralSetting\" SET \"name\"=?,\"description\"=?,\"operator\"=?,\"db_version\"=?,\"logi_version\"=?,\"logi_build\"=? WHERE \"id\" = 1";
			statement = this.db.getPreparedStatement(sqlSetVersion);
			statement.setString(1, globalSettingPanel.getProjectIdentifier()[0]);
			statement.setString(2, globalSettingPanel.getProjectIdentifier()[1]);
			statement.setString(3, globalSettingPanel.getProjectIdentifier()[2]);
			statement.setDouble(4, BundleAdjustmentProjectDatabase.DATABASE_VERSION);
			statement.setDouble(5, Logi.VERSION);
			statement.setInt(6, Logi.BUILD);

			statement.execute();
		}		
		sqlSetVersion = "UPDATE \"GeneralSetting\" SET \"db_version\"=?,\"logi_version\"=?,\"logi_build\"=? WHERE \"id\" = 1";

		statement = this.db.getPreparedStatement(sqlSelectVersion);
		ResultSet result = statement.executeQuery();

		if (!result.wasNull() && result.next()) {
			logiVersion = result.getDouble("logi_version");
			logiBuild   = result.getInt("logi_build");
			dbVersion   = result.getDouble("db_version");	
			if (dbVersion < BundleAdjustmentProjectDatabase.DATABASE_VERSION) {
				System.out.println(this.getClass().getSimpleName()+" Aktualisiere Datenbank von Version " + dbVersion + " auf " + BundleAdjustmentProjectDatabase.DATABASE_VERSION);
				Map<Double, String> dbUpdates = new LinkedHashMap<Double, String>();
				dbUpdates.put(1.01,  "ALTER TABLE \"PointGroup\" ADD \"sigma2aprio\" DOUBLE DEFAULT 1.0\r\n");
				 
				dbUpdates.put(1.0121, "CREATE TABLE \"DistributionUncertaintyParameter\" (\"id\" INTEGER NOT NULL PRIMARY KEY,\"distribution_x_instrument\" TINYINT NOT NULL,\"distribution_y_instrument\" TINYINT NOT NULL,\"distribution_z_instrument\" TINYINT NOT NULL,\"distribution_scale_instrument\" TINYINT NOT NULL,\"distribution_add_instrument\" TINYINT NOT NULL,\"distribution_fourier_aa1_instrument\" TINYINT NOT NULL,\"distribution_fourier_aa2_instrument\" TINYINT NOT NULL,\"distribution_fourier_ba1_instrument\" TINYINT NOT NULL,\"distribution_fourier_ba2_instrument\" TINYINT NOT NULL,\"distribution_fourier_ae0_instrument\" TINYINT NOT NULL,\"distribution_fourier_ae1_instrument\" TINYINT NOT NULL,\"distribution_fourier_ae2_instrument\" TINYINT NOT NULL,\"distribution_fourier_be1_instrument\" TINYINT NOT NULL,\"distribution_fourier_be2_instrument\" TINYINT NOT NULL,\"distribution_alpha_instrument\" TINYINT NOT NULL,\"distribution_gamma_instrument\" TINYINT NOT NULL,\"distribution_ex_instrument\" TINYINT NOT NULL,\"distribution_by_instrument\" TINYINT NOT NULL,\"distribution_bz_instrument\" TINYINT NOT NULL,\"distribution_scale_target\" TINYINT NOT NULL,\"distribution_add_target\" TINYINT NOT NULL,\"distribution_azimuth_target\" TINYINT NOT NULL,\"distribution_zenith_target\" TINYINT NOT NULL,\"distribution_centring_azimuth_target\" TINYINT NOT NULL,\"distribution_centring_zenith_target\" TINYINT NOT NULL)\r\n");
				dbUpdates.put(1.0122, "ALTER TABLE \"GeneralSetting\" DROP \"stochastic_model\" \r\n");
				dbUpdates.put(1.0123, "ALTER TABLE \"GeneralSetting\" ADD \"stochastic_model\" INTEGER DEFAULT " + PreAnalysis.UNCERTAINTY_MODEL_UNSCENTED_TRANSFORMATION + "\r\n");
				dbUpdates.put(1.0124, "ALTER TABLE \"GeneralSetting\" ADD \"monte_carlo_samples\" INTEGER DEFAULT " + PreAnalysis.MONTE_CARLO_SAMPLES + "\r\n");
				dbUpdates.put(1.02,   "INSERT INTO \"DistributionUncertaintyParameter\" SELECT \"id\", "+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+","+PreAnalysis.DISTRIBUTION_NORMAL+" FROM \"PointGroup\"\r\n");
				dbUpdates.put(1.021,  "CREATE TABLE \"CovarianceMatrix\" (\"id\" INTEGER NOT NULL, \"row\" INTEGER NOT NULL, \"column\" INTEGER NOT NULL, \"value\" DOUBLE NOT NULL,PRIMARY KEY(\"id\",\"row\",\"column\"))" + "\r\n");
				dbUpdates.put(1.03,   "ALTER TABLE \"PointGroup\" DROP \"covar\"\r\n");
				dbUpdates.put(1.04,   "ALTER TABLE \"GeneralSetting\" ADD \"export_covar\" BOOLEAN DEFAULT FALSE\r\n");
				dbUpdates.put(1.05,   "ALTER TABLE \"GlobalPointApost\" ADD \"covar_index\" INTEGER DEFAULT -1\r\n");
				dbUpdates.put(1.051,  "ALTER TABLE \"PolarObservation\" ADD \"date\" TIMESTAMP DEFAULT NULL\r\n");
				dbUpdates.put(1.052,  "CREATE TABLE \"CorrelationFunctionParameter\" (\"id\" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,\"group_id\" INTEGER NOT NULL,\"obs_type\" INTEGER NOT NULL,\"corr_fun\" INTEGER NOT NULL,\"sigma\" DOUBLE NOT NULL,\"max_dt\" DOUBLE NOT NULL,\"coef_a\" DOUBLE NOT NULL,\"coef_b\" DOUBLE NOT NULL,\"coef_c\" DOUBLE NOT NULL)\r\n");
				dbUpdates.put(1.053,  "INSERT INTO \"CorrelationFunctionParameter\" (\"group_id\",\"obs_type\",\"corr_fun\",\"sigma\",\"max_dt\",\"coef_a\",\"coef_b\",\"coef_c\") SELECT \"id\", "+LasertrackerUncertaintyModel.DISTANCE3D+",   -1, 1, 0, 1, 0, 0 FROM \"PointGroup\"\r\n");
				dbUpdates.put(1.054,  "INSERT INTO \"CorrelationFunctionParameter\" (\"group_id\",\"obs_type\",\"corr_fun\",\"sigma\",\"max_dt\",\"coef_a\",\"coef_b\",\"coef_c\") SELECT \"id\", "+LasertrackerUncertaintyModel.AZIMUTHANGLE+", -1, 1, 0, 1, 0, 0 FROM \"PointGroup\"\r\n");
				dbUpdates.put(1.060,  "INSERT INTO \"CorrelationFunctionParameter\" (\"group_id\",\"obs_type\",\"corr_fun\",\"sigma\",\"max_dt\",\"coef_a\",\"coef_b\",\"coef_c\") SELECT \"id\", "+LasertrackerUncertaintyModel.ZENITHANGLE+",  -1, 1, 0, 1, 0, 0 FROM \"PointGroup\"\r\n");
				dbUpdates.put(1.070,  "ALTER TABLE \"GeneralSetting\" ADD \"appy_sigma_apost\" BOOLEAN DEFAULT TRUE\r\n");
				
				for ( Map.Entry<Double, String> sql : dbUpdates.entrySet() ) {
					if (sql.getKey() > dbVersion) {
						statement = this.db.getPreparedStatement(sql.getValue());
						statement.execute();

						// Speichere die Version des DB-Updates
						statement = this.db.getPreparedStatement(sqlSetVersion);
						statement.setDouble(1, sql.getKey());
						statement.setDouble(2, Logi.VERSION);
						statement.setInt(3, Logi.BUILD);
						statement.execute();
					}
				}
				
				// Speichertechnik fuer Covar hat sich geandert - kopiere alte globale a-priori Matrix um
				if (dbVersion < 1.03) {
					String sqlMatrix = "SELECT \"covar\" FROM \"GeneralSetting\" WHERE \"id\"=1";
					PreparedStatement statementMatrix = this.db.getPreparedStatement(sqlMatrix);
					ResultSet groupSet = statementMatrix.executeQuery();
					if (!groupSet.wasNull() && groupSet.next()) {
						Matrix Cll = MathExtension.getDeserializedMatrix(groupSet.getString("covar"));
						if (Cll != null)
							this.saveGlobalCovarianceMatrixApriori(Cll);
					}
				}
			}
			// Setze aktuelle Versionen vom LOGI und der DB
			statement = this.db.getPreparedStatement(sqlSetVersion);
			statement.setDouble(1, BundleAdjustmentProjectDatabase.DATABASE_VERSION);
			statement.setDouble(2, Logi.VERSION);
			statement.setInt(3, Logi.BUILD);
			statement.execute();
		}
		else
			validDB = false;

		if (dbVersion > BundleAdjustmentProjectDatabase.DATABASE_VERSION){
			JOptionPane.showMessageDialog(this.owner, 
				String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "err.logiToOldToLoadProjectBody"), dbVersion, BundleAdjustmentProjectDatabase.DATABASE_VERSION),
				this.babel.getString(this.getClass().getSimpleName(), "err.logiToOldToLoadProjectTitle"),
				JOptionPane.ERROR_MESSAGE
			);
			validDB = false;
		}
		else if (logiVersion > Logi.VERSION || (logiVersion == Logi.VERSION && logiBuild > Logi.BUILD)) {
			JOptionPane.showMessageDialog(this.owner, 
				String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "msg.dbCreatedByNewerVersionBody"), logiVersion+"."+logiBuild, Logi.VERSION+"."+Logi.BUILD ),
				this.babel.getString(this.getClass().getSimpleName(), "msg.dbCreatedByNewerVersionTitle"),
				JOptionPane.INFORMATION_MESSAGE
			);
		}
		return validDB;
	}
	
	private List<String> getTemplates() {
		File root = new File(this.TEMPLATE_PATH);
		File[] files = root.listFiles();
		if (files == null)
			return null;
		List<String> templates = new ArrayList<String>();
		TMPLFileFilter filter = new TMPLFileFilter();
		for (int i=0; i<files.length; i++) {
			if (files[i].isFile() && filter.accept(files[i])) {
				templates.add(files[i].getName());
			}
		}
		return templates;
	}
	
	/**
	 * Kopiert eine Datei src nach dest
	 * @param src Eingangsdatei
	 * @param dest Ausgangsdatei
	 * @return isCopied Status des Kopiervorgangs
	 */
	private boolean copyFile( String src, String dest ) { 
		FileChannel inChannel  = null;  
		FileChannel outChannel = null;
		boolean isCopied = true;
		
		try {
			inChannel  = new FileInputStream(src).getChannel();
			outChannel = new FileOutputStream(dest).getChannel(); 
        	
			inChannel.transferTo(0, inChannel.size(), outChannel); 
		} catch (IOException e) { 
			e.printStackTrace();
			isCopied = false;
		} finally { 
            if (inChannel != null) 
            	try { inChannel.close();  } catch ( IOException e ) { }  
            if (outChannel != null) 
            	try { outChannel.close(); } catch ( IOException e ) { }  
        }
		return isCopied;
    }
	
	private void createReport(File reportFile, String templateName) {
		if (reportFile == null)
			return;
		
		templateName = templateName==null||templateName.isEmpty()?"Default.tmpl":templateName;
		String subTemplateDir = templateName.toLowerCase().endsWith(".tmpl")?templateName.substring(0, templateName.lastIndexOf('.')):templateName;
		Report report = null;
		try {
			String templatePath    = new File(".").getCanonicalPath()+File.separator+this.TEMPLATE_PATH;
			String templateSubPath = new File(".").getCanonicalPath()+File.separator+this.TEMPLATE_PATH+File.separator+subTemplateDir;
			Hashtable<String, Object> tmpl_args = new Hashtable<String, Object>();
			tmpl_args.put("filename", templateName);
			tmpl_args.put("debug", Boolean.FALSE);
			tmpl_args.put("case_sensitive", Boolean.TRUE);
			tmpl_args.put("strict", Boolean.TRUE);
			tmpl_args.put("loop_context_vars", Boolean.TRUE);
			tmpl_args.put("global_vars", Boolean.TRUE);

			if (!new File(templateSubPath).isDirectory()) {
				tmpl_args.put("path", new String[] { ".", templatePath } );
			}
			else {
				tmpl_args.put("path", new String[] { ".", templatePath, templateSubPath } );
				tmpl_args.put("search_path_on_include", new String[] { "."+File.separator+subTemplateDir, templateSubPath } );
			}
			report = new Report(this.db, tmpl_args);
			
		} catch (FileNotFoundException e) {
			report = null;
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			report = null;
			e.printStackTrace();
		} catch (IllegalStateException e) {
			report = null;
			e.printStackTrace();
		} catch (IOException e) {
			report = null;
			e.printStackTrace();
		}
		
		if (report == null) {
			JOptionPane.showMessageDialog(this.owner,
					this.babel.getString(this.getClass().getSimpleName(), "err.createReportTemplateBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.createReportTemplateTitle"),
				    JOptionPane.ERROR_MESSAGE
			);
			return;
		}

		if (!report.toFile(reportFile)) {
			JOptionPane.showMessageDialog(this.owner,
					String.format( this.babel.getString(this.getClass().getSimpleName(), "err.createReportBody"), reportFile.getName()),
					this.babel.getString(this.getClass().getSimpleName(), "err.createReportTitle"),
				    JOptionPane.ERROR_MESSAGE
			);
		}
		else if (Desktop.isDesktopSupported()) {
			if (JOptionPane.showConfirmDialog(this.owner, 
					String.format( this.babel.getString(this.getClass().getSimpleName(), "msg.createAndOpenReportBody"), reportFile.getName()),
					this.babel.getString(this.getClass().getSimpleName(), "msg.createAndOpenReportTitle"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().browse( reportFile.toURI() );
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this.owner, 
							String.format( this.babel.getString(this.getClass().getSimpleName(), "err.openReportBody"), reportFile.getName()),
							this.babel.getString(this.getClass().getSimpleName(), "err.openReportTitle"),
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				} 
			}
		}	
		else {
			JOptionPane.showMessageDialog(this.owner,
					String.format( this.babel.getString(this.getClass().getSimpleName(), "msg.createReportBody"), reportFile.getName()),
					this.babel.getString(this.getClass().getSimpleName(), "msg.createReportTitle"),
				    JOptionPane.INFORMATION_MESSAGE
			);
		}	
	}

	public File openSingleFileDialog(DefaultFileFilter filter, int dialog) {
		return this.openSingleFileDialog(new DefaultFileFilter[] {filter}, dialog);
	}
	
	public File openSingleFileDialog(DefaultFileFilter filters[], int dialog) {
		if (filters == null)
			filters = new DefaultFileFilter[] {new DefaultFileFilter()};

		// Pruefe, ob es die selben Filter sind
		FileFilter existingFileFilters[] = this.fileChooser.getChoosableFileFilters();
		boolean hasSameFileFilter = existingFileFilters.length == filters.length;
		
		if (hasSameFileFilter) {
			for (int i=0; i<existingFileFilters.length; i++) {
				FileFilter ef = existingFileFilters[i];
				boolean foundFilter = false;
				for (int j=0; j<filters.length; j++) {
					if (filters[j].getClass().equals(ef.getClass())) {
						foundFilter = true;
						break;
					}
				}
				if (!foundFilter) {
					hasSameFileFilter = false;
					break;
				}
			}
		}
		if (!hasSameFileFilter) {
			this.fileChooser.removeAllChoosableFileFilters();
			for (DefaultFileFilter filter : filters) 
				this.fileChooser.addChoosableFileFilter(filter);
		}

		this.fileChooser.setMultiSelectionEnabled(false);
		this.fileChooser.setSelectedFile(new File(""));
		
		if(dialog == JFileChooser.OPEN_DIALOG && this.fileChooser.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION ) 
			return this.fileChooser.getSelectedFile();
		else if(dialog == JFileChooser.SAVE_DIALOG) {
			boolean stopLoop = false;
			File selectedFile = null;
			do {
				selectedFile = null;
				if(this.fileChooser.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION ) {
					selectedFile = this.fileChooser.getSelectedFile();
					DefaultFileFilter filter = (DefaultFileFilter)this.fileChooser.getFileFilter();
					if (filter != null && !filter.accept(selectedFile)) {
						selectedFile = new File(selectedFile + filter.getDefaultExtension());
						this.fileChooser.setSelectedFile(selectedFile);
					}
					// prüfe, ob Dateien bereits vorhanden
					int overwriteFile = -1;
					if (!selectedFile.exists() || (selectedFile.exists() &&
							(overwriteFile = JOptionPane.showConfirmDialog(this.owner,
									this.babel.getString(this.getClass().getSimpleName(), "msg.fileExistBody"),
									this.babel.getString(this.getClass().getSimpleName(), "msg.fileExistTitle"),
									JOptionPane.YES_NO_CANCEL_OPTION)) == JOptionPane.YES_OPTION) ) {
							stopLoop = true;
							break;
					}
					else if(overwriteFile == JOptionPane.CANCEL_OPTION) {
						continue;	
					}
					else if(overwriteFile == JOptionPane.NO_OPTION) {
						return null;	
					}
				}
		        else { 
		        	return null;
		        }
			} while(!stopLoop);
			
			return selectedFile;
		}
		return null;
	}

	/**
	 * Starte Ausgleichung und transformiere alle Sub-Systeme ins globale System
	 * Bestimmt zuvor aus den polaren Elementen die lokalen Koordinaten und deren Kovarianzmatrix
	 * Speichert abschließend alle Resultate
	 *  
	 */
	private void adjustTransformation() {
		
		final IndeterminateProgressDialog progressDialog = new IndeterminateProgressDialog(this.owner, this.babel);

		new Thread(progressDialog).start();

		TransformationSchedule trafoTask = new TransformationSchedule(
				progressDialog, 
				this.db
		);
		trafoTask.addPropertyChangeListener(this);
		trafoTask.start();
	}
	
	public Babel getBabel() {
		return this.babel;
	}
	
	private void selectRootNode() {
		DataTabbedPane dataTabbedPane = this.dataTreeMenu.getRootObject();
		int indices[] = this.dataTreeMenu.getSelectionRows();
		if (indices.length > 0 && indices[0] != 0) {
			dataTabbedPane.setSelectedIndex(0);
			this.dataTreeMenu.setSelectionRow(0);
		}
		else {
			dataTabbedPane.setSelectedIndex(0);
			this.loadSystem(this.dataTreeMenu.getGlobalSystemId(), dataTabbedPane);
		}
	}
		
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if (this.hasDataBase() && e.getSource() == this.dataTreeMenu ) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.dataTreeMenu.getLastSelectedPathComponent();
			if (node == null) 
	        	return;
	        Object nodeObj = node.getUserObject();

	        if ((node.isLeaf() || node.isRoot()) && nodeObj instanceof JComponent) {
				this.dataSplitPane.setRightComponent( (JComponent)nodeObj );
				
				//System.out.println(this.getClass().getSimpleName()+" Node gewechselt; lade... " + nodeObj);
				
				if (node instanceof SystemNode && nodeObj instanceof DataTabbedPane) {
					int id = ((SystemNode)node).getId();
					DataTabbedPane dataTabbedPane = (DataTabbedPane)nodeObj;
					this.loadSystem(id, dataTabbedPane);
				}
	        }
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (!this.hasDataBase())
			return;
		
		int firstRow = e.getFirstRow();
		if (firstRow < 0)
			return;
		if (e.getSource() instanceof DataTableModel && this.dataTreeMenu.getLastSelectedPathComponent() instanceof SystemNode) {
			SystemNode node = (SystemNode) this.dataTreeMenu.getLastSelectedPathComponent();

			DataTableModel model = (DataTableModel)e.getSource();
			DataRow rowData = model.getRowData(firstRow);
			
			if (rowData == null || node == null)
				return;
			
			if ((e.getType()==TableModelEvent.INSERT || e.getType()==TableModelEvent.UPDATE) && rowData.isComplete()) {
				//System.out.println(this.getClass().getSimpleName()+" Tabellenänderung in Zeile "+ firstRow+"  "+(e.getType()==TableModelEvent.UPDATE)+"  ID: "+node.getId()+"  "+TableModelEvent.UPDATE+"  "+e);
				this.saveTableRow(node.getId(), rowData);
			}
			else if(e.getType()==TableModelEvent.DELETE) {
				//System.out.println(this.getClass().getSimpleName()+" Tabellenzeile gelöscht: Zeile "+ firstRow+"  "+(e.getType()==TableModelEvent.DELETE)+"  ID: "+node.getId()+"  "+TableModelEvent.UPDATE+"  "+e);
				this.deleteTableRow(rowData);
				this.loadSystem(node.getId(), node.getUserObject());
			}
		}
		else if ((e.getSource() instanceof UncertaintyTableModel || e.getSource() instanceof TimeDependentUncertaintyTableModel ) && this.dataTreeMenu.getLastSelectedPathComponent() instanceof SystemNode) {
			//System.out.println(this.getClass().getSimpleName() + " UncertaintyTableModel-DATA-CHANEGED " + e.getSource().getClass().getSimpleName());
			SystemNode systemNode = (SystemNode) this.dataTreeMenu.getLastSelectedPathComponent();
			this.saveSettings(systemNode);
		}
		else if (e.getSource() instanceof UpperSymmMatrixTableModel && this.dataTreeMenu.getLastSelectedPathComponent() instanceof SystemNode) {
			UpperSymmMatrixTableModel model = (UpperSymmMatrixTableModel)e.getSource();
			if ((e.getType()==TableModelEvent.INSERT || e.getType()==TableModelEvent.UPDATE)) {
				int col = e.getColumn();			
				if (col < 0) {
					this.saveGlobalCovarianceMatrixApriori(model.getMatrix());
				}
				else {
					Double value = (Double)model.getValueAt(firstRow, col);
					if (value != null)
						this.saveCovarianceMatrixElement(-1, firstRow, col, value);
				}
			}
		}
//		else if (e.getSource() instanceof UpperSymmMatrixTableModel && this.dataTreeMenu.getLastSelectedPathComponent() instanceof SystemNode) {
//			UpperSymmMatrixTableModel model = (UpperSymmMatrixTableModel)e.getSource();
//			this.saveGlobalCovarianceMatrixApriori(model.getMatrix());
//			//System.out.println(this.getClass().getSimpleName()+" Tabellenänderung CoVar in Zeile "+ firstRow+" ");
//		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		System.out.println(this.getClass().getSimpleName()+" Ein Button wurde geklickt: "+e);
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.dataTreeMenu.getLastSelectedPathComponent();
		SystemNode systemNode = null; 
		if (node instanceof SystemNode) {
			systemNode = (SystemNode)node;
		}
		
		if (e.getActionCommand().equals(NavigationToolBar.NEW_PROJECT)) {
			this.createNewProject();
		}
		else if (e.getActionCommand().equals(NavigationToolBar.OPEN_PROJECT)) {
			this.openExistingProject();
		}
		else if (e.getActionCommand().equals(NavigationToolBar.CHECK_UPDATES)) {
			this.checkUpdates();
		}
		else if (this.hasDataBase()) {
			if (e.getActionCommand().equals(NavigationToolBar.COPY_PROJECT)) {
				this.copyProject();
			}
			else if (e.getActionCommand().equals(NavigationToolBar.ADD_LOCAL_SYSTEM)) {
				this.addSystem("", false);
			}
			else if (e.getActionCommand().equals(NavigationToolBar.REMOVE_LOCAL_SYSTEM)) {
				this.removeLocalSystem(systemNode);
			}
			else if (e.getActionCommand().equals(NavigationToolBar.TRUNCATE_SYSTEM) && systemNode != null) {
				this.truncateSystem(systemNode);
			}
			else if (e.getActionCommand().equals(NavigationToolBar.IMPORT_FILE) && systemNode != null) {
				this.importSourceFile(systemNode);
			}
			else if (e.getActionCommand().equals(NavigationToolBar.EXPORT_REPORT) && systemNode != null) {
				this.exportReport();
			}
			else if (e.getActionCommand().equals(NavigationToolBar.ADJUST)) {
				this.adjustTransformation();
			}
			else if ((e.getActionCommand().equals(SettingPanel.EXCLUDE_LOCAL_GROUP) || e.getActionCommand().equals(SettingPanel.TRANSFORMATION_PARAMETER) || e.getActionCommand().equals(SettingPanel.LEAST_SQUARE_SETTING)  || e.getActionCommand().equals(SettingPanel.UNCERTAINTY_MODEL)) && systemNode != null) {
				this.saveSettings(systemNode);
			}
			else if (e.getActionCommand().equals(NavigationToolBar.SAVE_SETTINGS) && systemNode != null) {

				if (systemNode instanceof LocalSystemNode) {
					BequeathOption[] possibilities = {
							new BequeathOption(this.babel.getString(this.getClass().getSimpleName(), "bequeathUncertainties"), BequeathOption.UNCERTAINTIES),
							new BequeathOption(this.babel.getString(this.getClass().getSimpleName(), "bequeathTransParameter"), BequeathOption.TRANSFORMATION_PARAMETERS), 
							new BequeathOption(this.babel.getString(this.getClass().getSimpleName(), "bequeathTransParameterAndUncertainties"), BequeathOption.TRANSFORMATION_PARAMETERS_AND_UNCERTAINTIES)
					};
					
					BequeathOption option = (BequeathOption)JOptionPane.showInputDialog(
							this.owner,
							this.babel.getString(this.getClass().getSimpleName(), "msg.bequeathOptionBody"),
							this.babel.getString(this.getClass().getSimpleName(), "msg.bequeathOptionTitle"),
							JOptionPane.QUESTION_MESSAGE,
							null,
							possibilities,
							possibilities[2]
					);

					if (option != null && !option.toString().trim().isEmpty()) {
					    if (option.getType() ==  BequeathOption.UNCERTAINTIES) 
					    	this.saveSettings(systemNode, false, true);
					    else if (option.getType() ==  BequeathOption.TRANSFORMATION_PARAMETERS) 
					    	this.saveSettings(systemNode, true, false);
					    else if (option.getType() ==  BequeathOption.TRANSFORMATION_PARAMETERS_AND_UNCERTAINTIES) 
					    	this.saveSettings(systemNode, true, true);
					    
					}
				}
				
				// Globaler Node
				else
					this.saveSettings(systemNode);
			}
		}
		else {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
	}
	
	@Override
	public void windowClosed(WindowEvent arg0) {
		this.closeDataBase();
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		this.closeDataBase();
		System.out.println("Thank you for using Logi - Local Observations to Global Integration\r\nby Michael Loesler <http://derletztekick.com>");
		System.exit(0);
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent e) {
//		System.out.printf( this.getClass().getSimpleName() + "  Property '%s': '%s' -> '%s'%n", e.getPropertyName(), e.getOldValue(), e.getNewValue() );
		
		// Liefere Fehlermedlungen
		if (e.getPropertyName().equals("TRANSFORMATION_OUT_OF_MEMORY")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.outOfMemoryBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.outOfMemoryTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("PRE_ANALYSIS_FAILD_NO_DATABASE") || e.getPropertyName().equals("PRE_ANALYSIS_FAILD_COULD_NOT_CONNECT_TO_DATABASE")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.noDatabaseTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("PRE_ANALYSIS_FAILD_SQL_ERROR")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.sqlBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.sqlTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_NOT_ENOUGH_OBSERVATIONS")) {
			JOptionPane.showMessageDialog(this.owner, 
					String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "err.notEnoughObsBody"), e.getNewValue()),
					this.babel.getString(this.getClass().getSimpleName(), "err.notEnoughObsTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_INTERRUPT")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "msg.interruptBody"),
					this.babel.getString(this.getClass().getSimpleName(), "msg.interruptTitle"),
					JOptionPane.INFORMATION_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_ITERATION_LIMIT_REACHED")) {
			JOptionPane.showMessageDialog(this.owner, 
					String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "msg.iterationBody"), e.getNewValue()),
					this.babel.getString(this.getClass().getSimpleName(), "msg.iterationTitle"),
					JOptionPane.INFORMATION_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_DEGREE_OF_FREEDOM_INACCURATE")) {
			JOptionPane.showMessageDialog(this.owner, 
					String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "msg.degreeOfFreedomBody"), e.getOldValue(), e.getNewValue()),
					this.babel.getString(this.getClass().getSimpleName(), "msg.degreeOfFreedomTitle"),
					JOptionPane.INFORMATION_MESSAGE
			);
		}
		
		else if (e.getPropertyName().equals("TRANSFORMATION_SINGULAR_MATRIX")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.singularBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.singularTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_SINGULAR_MATRIX")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.singularBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.singularTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_NOT_ENOUGH_GLOBAL_POINTS")) {
			JOptionPane.showMessageDialog(this.owner, 
					String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "err.notEnoughGlobalPointsBody"), e.getNewValue()),
					this.babel.getString(this.getClass().getSimpleName(), "err.notEnoughGlobalPointsTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_NOT_ENOUGH_SOURCE_SYSTEMS")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.notEnoughLocalSystemsBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.notEnoughLocalSystemsTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_NO_UNIQUE_SYSTEM_FOUND")) {
			JOptionPane.showMessageDialog(this.owner, 
					String.format(Locale.ENGLISH, this.babel.getString(this.getClass().getSimpleName(), "err.noUniqueGlobalSystemBody"), e.getNewValue()),
					this.babel.getString(this.getClass().getSimpleName(), "err.noUniqueGlobalSystemTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_GLOBAL_SINGULAR_COVAR")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.singularCovarGlobalBody"), 
					this.babel.getString(this.getClass().getSimpleName(), "err.singularCovarGlobalTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("TRANSFORMATION_LOCAL_SINGULAR_COVAR")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.singularCovarLocalBody"), 
					this.babel.getString(this.getClass().getSimpleName(), "err.singularCovarLocalTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		
		else if (e.getPropertyName().equals("TRANSFORMATION_FATAL_ERROR")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.fatalErrorBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.fatalErrorTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("BUNDLEADJUSTMENT_DATA_SAVED_FAILED")) {
			JOptionPane.showMessageDialog(this.owner, 
					this.babel.getString(this.getClass().getSimpleName(), "err.saveResultBody"),
					this.babel.getString(this.getClass().getSimpleName(), "err.saveResultTitle"),
					JOptionPane.ERROR_MESSAGE
			);
		}
		else if (e.getPropertyName().equals("SCHEDULE_FINISHED")) {
			this.selectRootNode();
		}
		// Dies betrifft die Textfelder auf den Setting-Panels bspw. A-PRIORI Varianzfaktor
		else if (e.getPropertyName().equals("value") && e.getOldValue() != null && this.dataTreeMenu.getLastSelectedPathComponent() instanceof SystemNode) {
			SystemNode systemNode = (SystemNode) this.dataTreeMenu.getLastSelectedPathComponent();
			this.saveSettings(systemNode);
		}
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());
        try {
            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode)(node.getChildAt(index));
        } 
        catch (NullPointerException exc) {}
        
        if (this.hasDataBase() && node instanceof SystemNode) {
        	int id = ((SystemNode)node).getId();
        	String newNodeName = node.toString();
        	try {
				this.saveGroupName(id, newNodeName);
			} catch (SQLException se) {
				se.printStackTrace();
			}
        }
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		if (this.dataTreeMenu.getLastSelectedPathComponent() instanceof GlobalSystemNode) {
			SystemNode systemNode = (SystemNode) this.dataTreeMenu.getLastSelectedPathComponent();
			this.saveSettings(systemNode);
		}
	}
	
	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {}

	@Override
	public void focusGained(FocusEvent e) {}

	
}
