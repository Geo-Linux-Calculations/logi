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

package com.derletztekick.geodesy.logi.treemenu;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.derletztekick.geodesy.bundleadjustment.v2.unknown.transformationparameter.TransformationParameterSet3D;
import com.derletztekick.geodesy.logi.pane.tabbedpane.DataTabbedPane;
import com.derletztekick.geodesy.logi.pane.tabbedpane.GlobalTabbedPane;
import com.derletztekick.geodesy.logi.pane.tabbedpane.LocalTabbedPane;
import com.derletztekick.geodesy.logi.settingpanel.GlobalSettingPanel;
import com.derletztekick.geodesy.logi.settingpanel.LocalSettingPanel;
import com.derletztekick.geodesy.logi.sql.DataManager;
import com.derletztekick.geodesy.logi.table.DataTable;
import com.derletztekick.geodesy.logi.table.TransformationParameterTable;
import com.derletztekick.geodesy.logi.table.UpperSymmMatrixTable;
import com.derletztekick.geodesy.logi.table.model.PolarObservationTableModel;
import com.derletztekick.geodesy.logi.table.model.RawPointTableModel;
import com.derletztekick.geodesy.logi.table.model.ResultPointTableModel;
import com.derletztekick.geodesy.logi.table.model.TransformationParameterTableModel;
import com.derletztekick.geodesy.logi.table.model.UpperSymmMatrixTableModel;
import com.derletztekick.geodesy.logi.treemenu.node.GlobalSystemNode;
import com.derletztekick.geodesy.logi.treemenu.node.LocalSystemNode;
import com.derletztekick.geodesy.logi.treemenu.node.SystemNode;
import com.derletztekick.tools.babel.Babel;

public class DataTreeMenu extends JTree implements TreeSelectionListener {
	private static final long serialVersionUID = -2262965002339574155L;
	private GlobalSystemNode root;
	private DefaultTreeModel treeModel;
	private final DataManager dataManager;
	private LocalTabbedPane localTabbedPane;
	private Babel babel;

	public DataTreeMenu(DataManager dataManager) {
		this.dataManager = dataManager;
		this.babel = this.dataManager.getBabel();
		
		this.initGroupNodes();
		
		this.addTreeSelectionListener(this);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		this.setEditable(true);
	}
	
	public boolean isEditable() {
		TreePath selPath = this.getSelectionPath();
		if (selPath != null)
			return !(selPath.getLastPathComponent() instanceof GlobalSystemNode);
		return super.isEditable();
	}

	
	private void initRootNode() {
		int rows = 1;

		GlobalSettingPanel settingPanel = new GlobalSettingPanel(this.dataManager);

		String rawPointTableHeader[] = new String[] {
				this.babel.getString("RawPointTable", "col0"),
				this.babel.getString("RawPointTable", "col1"),
				this.babel.getString("RawPointTable", "col2"),
				this.babel.getString("RawPointTable", "col3"),
				this.babel.getString("RawPointTable", "col4"),
				this.babel.getString("RawPointTable", "col5")
		};
		

		String resultPointTableHeader[] = new String[] {
				this.babel.getString("ResultPointTable", "col0"),
				this.babel.getString("ResultPointTable", "col1"),
				this.babel.getString("ResultPointTable", "col2"),
				this.babel.getString("ResultPointTable", "col3"),
				this.babel.getString("ResultPointTable", "col4"),
				this.babel.getString("ResultPointTable", "col5"),
				this.babel.getString("ResultPointTable", "col6")
		};
		
		String resultPointTableExtendedHeader[] = new String[] {
				this.babel.getString("ResultPointTable", "col0"),
				this.babel.getString("ResultPointTable", "col1"),
				this.babel.getString("ResultPointTable", "col2"),
				this.babel.getString("ResultPointTable", "col3"),
				this.babel.getString("ResultPointTable", "col4"),
				this.babel.getString("ResultPointTable", "col5"),
				this.babel.getString("ResultPointTable", "col6"),
				this.babel.getString("ResultPointTable", "col7"),
				this.babel.getString("ResultPointTable", "col8"),
				this.babel.getString("ResultPointTable", "col9"),
				this.babel.getString("ResultPointTable", "col10"),
				this.babel.getString("ResultPointTable", "col11"),
				this.babel.getString("ResultPointTable", "col12"),
				this.babel.getString("ResultPointTable", "col13"),
				this.babel.getString("ResultPointTable", "col14"),
				this.babel.getString("ResultPointTable", "col15"),
				this.babel.getString("ResultPointTable", "col16"),
				this.babel.getString("ResultPointTable", "col17"),
				this.babel.getString("ResultPointTable", "col18"),
				this.babel.getString("ResultPointTable", "col19")
		};
				
		UpperSymmMatrixTable upperSymmMatrixTable = null;
		DataTable[] globalDataTables = null;
		if (this.dataManager.hasDataBase()) {
			UpperSymmMatrixTableModel upperSymmMatrixTableModel = new UpperSymmMatrixTableModel(3, rows);
			upperSymmMatrixTable = new UpperSymmMatrixTable(this.babel.getString("DataTable", "tab.global.covar"), upperSymmMatrixTableModel);
			
			globalDataTables = new DataTable[] {
					new DataTable(this.babel.getString("DataTable", "tab.global.priori"), new RawPointTableModel(rawPointTableHeader, rows, upperSymmMatrixTableModel)),
					new DataTable(this.babel.getString("DataTable", "tab.global.post"),   new ResultPointTableModel(resultPointTableExtendedHeader, rows)),
					new DataTable(this.babel.getString("DataTable", "tab.global.global"), new ResultPointTableModel(resultPointTableHeader, rows))
			};
			
			for (int i=0; i<globalDataTables.length; i++) {
				globalDataTables[i].getModel().addTableModelListener(this.dataManager);
			}
			upperSymmMatrixTableModel.addTableModelListener(this.dataManager);
			
		}

		GlobalTabbedPane globalTabbedPane = new GlobalTabbedPane(settingPanel, globalDataTables, upperSymmMatrixTable);
		GlobalSystemNode globalSystemNode = new GlobalSystemNode(0, this.babel.getString(this.getClass().getSimpleName(), "root"), globalTabbedPane);
		this.root = globalSystemNode;

		if (this.treeModel == null) {
			this.treeModel = new DefaultTreeModel( this.root );
			this.treeModel.addTreeModelListener(this.dataManager);
			this.setModel(this.treeModel);	
		}
		else
			this.treeModel.setRoot( this.root );
	}
	
	private void initLocalTabbedPane() {
		if (this.dataManager.hasDataBase()) {
			int rows = 10;
			
			String paramTableColumnHeader[] = new String[] {
					this.babel.getString("ParameterTable", "col0"),
					this.babel.getString("ParameterTable", "col1"),
					this.babel.getString("ParameterTable", "col2")
			};
			
			String paramTableRowHeader[] = new String[] {
					this.babel.getString("ParameterTable", "row0"),
					this.babel.getString("ParameterTable", "row1"),
					this.babel.getString("ParameterTable", "row2"),
					this.babel.getString("ParameterTable", "row3"),
					this.babel.getString("ParameterTable", "row4"),
					this.babel.getString("ParameterTable", "row5"),
					this.babel.getString("ParameterTable", "row6"),
					this.babel.getString("ParameterTable", "row7"),
					this.babel.getString("ParameterTable", "row8"),
					this.babel.getString("ParameterTable", "row9"),
					this.babel.getString("ParameterTable", "row10")

			};

			String polarTableHeader[] = new String[] {
					this.babel.getString("PolarObservationTable", "col0"),
					this.babel.getString("PolarObservationTable", "col1"),
					this.babel.getString("PolarObservationTable", "col2"),
					this.babel.getString("PolarObservationTable", "col3"),
					this.babel.getString("PolarObservationTable", "col4"),
					this.babel.getString("PolarObservationTable", "col5"),
					this.babel.getString("PolarObservationTable", "col6")
					
			};
			
			String resultPointTableHeader[] = new String[] {
					this.babel.getString("ResultPointTable", "col0"),
					this.babel.getString("ResultPointTable", "col1"),
					this.babel.getString("ResultPointTable", "col2"),
					this.babel.getString("ResultPointTable", "col3"),
					this.babel.getString("ResultPointTable", "col4"),
					this.babel.getString("ResultPointTable", "col5"),
					this.babel.getString("ResultPointTable", "col6")
			};
			
			String resultPointTableExtendedHeader[] = new String[] {
					this.babel.getString("ResultPointTable", "col0"),
					this.babel.getString("ResultPointTable", "col1"),
					this.babel.getString("ResultPointTable", "col2"),
					this.babel.getString("ResultPointTable", "col3"),
					this.babel.getString("ResultPointTable", "col4"),
					this.babel.getString("ResultPointTable", "col5"),
					this.babel.getString("ResultPointTable", "col6"),
					this.babel.getString("ResultPointTable", "col7"),
					this.babel.getString("ResultPointTable", "col8"),
					this.babel.getString("ResultPointTable", "col9"),
					this.babel.getString("ResultPointTable", "col10"),
					this.babel.getString("ResultPointTable", "col11"),
					this.babel.getString("ResultPointTable", "col12"),
					this.babel.getString("ResultPointTable", "col13"),
					this.babel.getString("ResultPointTable", "col14"),
					this.babel.getString("ResultPointTable", "col15"),
					this.babel.getString("ResultPointTable", "col16"),
					this.babel.getString("ResultPointTable", "col17"),
					this.babel.getString("ResultPointTable", "col18"),
					this.babel.getString("ResultPointTable", "col19")
			};
									
			TransformationParameterTable transformationParameterTable = new TransformationParameterTable(
					this.babel.getString("DataTable", "tab.local.param"), 
					new TransformationParameterTableModel(paramTableColumnHeader, paramTableRowHeader, new TransformationParameterSet3D())
			);
			LocalSettingPanel localSettingPanel = new LocalSettingPanel(this.dataManager);

			DataTable[] localDataTables = new DataTable[] {
					new DataTable(this.babel.getString("DataTable", "tab.local.priori"), new PolarObservationTableModel(polarTableHeader, rows)),
					new DataTable(this.babel.getString("DataTable", "tab.local.post"),   new ResultPointTableModel(resultPointTableExtendedHeader, rows)),
					new DataTable(this.babel.getString("DataTable", "tab.local.global"), new ResultPointTableModel(resultPointTableHeader, rows))
			};
			
			for (int i=0; i<localDataTables.length; i++) {
				localDataTables[i].getModel().addTableModelListener(this.dataManager);
			}

			this.localTabbedPane = new LocalTabbedPane(localSettingPanel, localDataTables, transformationParameterTable);		
		}
	}
	
	public LocalSystemNode addLocalSystemNode(int id, String name, String startPointId) {
		return this.addLocalSystemNode(id, name, startPointId, true);
	}
	
	public LocalSystemNode addLocalSystemNode(int id, String name, String startPointId, boolean expand) {
		LocalSystemNode localSystemNode = new LocalSystemNode(id, name, startPointId, this.localTabbedPane);
		this.addNode(localSystemNode);
		if (expand) {
			TreePath path = new TreePath(localSystemNode.getPath());
			this.setSelectionPath(path);
			this.scrollPathToVisible(path);
		}
		return localSystemNode;
	}
	
	public void initGroupNodes() {
		this.removeAllNodes();
		this.initRootNode();
		this.initLocalTabbedPane();
		
		if (this.root.getChildCount()>0) {
			this.setSelectionRow(0);
			this.setRootVisible(true);
		    this.setSelectionInterval(0,0);
		    this.expandPath( this.getSelectionPath() );
		    // oder this.expandRow(0);
		}
		
	    this.validate();
	}
	
	public void setGlobalSystemName(String name) {
		this.root.setUserObject(name);
	}
	
	public void setGlobalSystemId(int id) {
		this.root.setId(id);
	}
	
	public int getGlobalSystemId() {
		return this.root.getId();
	}
	
	public DataTabbedPane getRootObject() {
		return this.root.getUserObject();
	}
	
	public void addNode(DefaultMutableTreeNode node) {
		this.treeModel.insertNodeInto(node, this.root, this.root.getChildCount());
		this.validate();
	}
	
	public void removeNode(DefaultMutableTreeNode node) {
		this.treeModel.removeNodeFromParent(node);
		this.setSelectionRow(0);
		this.validate();
	}
	
	public void removeAllNodes() {
		while(this.root != null && this.root.getChildCount()>0) {
			this.treeModel.removeNodeFromParent((DefaultMutableTreeNode)this.root.getChildAt(0));
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if (e.getSource() != this)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
		if (node == null)
			return;

		if (node.isRoot() || node.isLeaf() && (node instanceof SystemNode)) {
			this.setEditable(true);
		}
		else
			this.setEditable(false);
	}
}
