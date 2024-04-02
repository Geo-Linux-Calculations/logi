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

package com.derletztekick.geodesy.logi.settingpanel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;
import com.derletztekick.geodesy.logi.combobox.DoubleComboBox;
import com.derletztekick.geodesy.logi.combobox.IntegerComboBox;
import com.derletztekick.geodesy.logi.combobox.NumberComboBox;
import com.derletztekick.geodesy.logi.sql.DataManager;

import layout.TableLayout;


public class GlobalSettingPanel extends SettingPanel {
	private static final long serialVersionUID = 1707927660376928062L;

	private TableLayout panelLayout;
	private String name;
	private List<JTextField> projectIdentifierFields = new ArrayList<JTextField>();
	private IntegerComboBox maxIterationCB;
	private IntegerComboBox monteCarloSamplesCB;
	private DoubleComboBox alphaCB;
	private DoubleComboBox betaCB;
	private JRadioButton freeAdjustment;
	private JRadioButton dynamicAdjustment;
	private JCheckBox exportCoVar, applySigmaApost;
	private final String projectName = "Logi (GNU-GPL)";
	private final String projectDesc = "The OpenSource Similarity Transformation Program for Polar Observations";
	private final String operatorName = (System.getProperty( "user.name" )==null)?"Staff":System.getProperty( "user.name" );

	private JRadioButton useUncertaintyPOUModel;
	private JRadioButton useUncertaintyMCSModel;
	private JRadioButton useUncertaintyUTModel;
	
	private final Double[] defaultPropertyValues = new Double [] {
		0.1, 1.0, 2.5, 5.0, 10.0
	};
			
	private final Double[] defaultTestPowerValues = new Double [] {
		70.0, 75.0, 80.0, 85.0, 90.0, 95.0
	};
		
	private final Integer[] defaultIterationValues = new Integer [] {
		0, 10, 20, 50, 100, 200, 500, 1000, 10000
	};
	
	private final Integer[] defaultMonteCarloSamplesValues = new Integer [] {
		100, 500, 1000, 5000, 10000, 100000
	};
	
	public GlobalSettingPanel(DataManager dataManager) {
		super(dataManager);
		this.name = this.babel.getString("SettingPanel", "tab.setting");
		this.init();
	}
	
	public void reload(String projectIdentifier[], int iter, int mcs, double alpha, double beta, boolean isFreeNetAdjustment, int uncertaintyModelType, boolean exportCoVar, boolean applySigmaApost) {
		this.freeAdjustment.setSelected(isFreeNetAdjustment);
		this.dynamicAdjustment.setSelected(!isFreeNetAdjustment);
		this.exportCoVar.setSelected(exportCoVar);
		this.applySigmaApost.setSelected(applySigmaApost);
		
		this.useUncertaintyMCSModel.setSelected(uncertaintyModelType == PreAnalysis.UNCERTAINTY_MODEL_MONTE_CARLO_SIMULATION);
		this.useUncertaintyPOUModel.setSelected(uncertaintyModelType == PreAnalysis.UNCERTAINTY_MODEL_PROPAGATION_OF_UNCERTAINTY);
		this.useUncertaintyUTModel.setSelected(uncertaintyModelType  == PreAnalysis.UNCERTAINTY_MODEL_UNSCENTED_TRANSFORMATION);
		
		this.projectIdentifierFields.get(0).setText(projectIdentifier[0] == null || projectIdentifier[0].trim().isEmpty()?this.projectName:projectIdentifier[0].trim());
		this.projectIdentifierFields.get(1).setText(projectIdentifier[1] == null || projectIdentifier[1].trim().isEmpty()?this.projectDesc:projectIdentifier[1].trim());
		this.projectIdentifierFields.get(2).setText(projectIdentifier[2] == null || projectIdentifier[2].trim().isEmpty()?this.operatorName:projectIdentifier[2].trim());
				
		this.monteCarloSamplesCB.setText(String.valueOf(mcs));
		this.maxIterationCB.setText(String.valueOf(iter));
		this.alphaCB.setText(String.valueOf(alpha));
		this.betaCB.setText(String.valueOf(beta));
		
		this.exportCoVar.setSelected(exportCoVar);
		this.applySigmaApost.setSelected(applySigmaApost);
		this.freeAdjustment.setSelected(isFreeNetAdjustment);
		this.dynamicAdjustment.setSelected(!isFreeNetAdjustment);
		
		this.useUncertaintyMCSModel.setSelected(uncertaintyModelType == PreAnalysis.UNCERTAINTY_MODEL_MONTE_CARLO_SIMULATION);
		this.useUncertaintyPOUModel.setSelected(uncertaintyModelType == PreAnalysis.UNCERTAINTY_MODEL_PROPAGATION_OF_UNCERTAINTY);
		this.useUncertaintyUTModel.setSelected(uncertaintyModelType  == PreAnalysis.UNCERTAINTY_MODEL_UNSCENTED_TRANSFORMATION);
		
	}
	
	private void init() {
		String identifierLabels[] = new String[] {
				this.babel.getString("SettingPanel", "project.name"),
				this.babel.getString("SettingPanel", "project.description"),
				this.babel.getString("SettingPanel", "project.operator")
        };
		
		String leastSquareLabels[] = new String[] {
				this.babel.getString("SettingPanel", "lsa.freenet"),
				this.babel.getString("SettingPanel", "lsa.dynmaic"),
				
				this.babel.getString("SettingPanel", "lsa.iteration"),
				this.babel.getString("SettingPanel", "lsa.montecarlo"),
				
				this.babel.getString("SettingPanel", "lsa.alpha"),
				this.babel.getString("SettingPanel", "lsa.beta"),
				
				this.babel.getString("SettingPanel", "lsa.covar"),
				this.babel.getString("SettingPanel", "lsa.applysigma")
        };
		
		String uncertaintyModelLabels[] = new String[] {
				this.babel.getString("SettingPanel", "uncertainty.pou"),
				this.babel.getString("SettingPanel", "uncertainty.mcs"),
				this.babel.getString("SettingPanel", "uncertainty.ut")
        };
		
		
		// @see http://java.sun.com/products/jfc/tsc/articles/tablelayout/
		// @see http://www.java-forum.org/blogs/tfa/39-snippet-tablelayout.html
        // moegliche Werte in Groessen-Array
        //     Integer:               Breite der Spalte / Hoehe der Zeile in Pixeln
        //     Double:                Anteil der verfuegbaren Groesse (z.B. 0.25 fuer ein viertel)
        //     TableLayout.FILL:      Komponente fuellt Zellen aus
        //     TableLayout.PREFERRED: bevorzugte Groesse der Komponente 
		double size[][] = { 
                { TableLayout.FILL }, // Columns
                { TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED } };// Rows
       
        this.setBorder( new EmptyBorder(10,30,10,30) );
        this.panelLayout = new TableLayout(size);
        this.setLayout(this.panelLayout );
        
        int curRow = 0;
        this.add(this.getProjectIdentifierPanel(this.babel.getString("SettingPanel", "border.project"), identifierLabels), "0," + curRow);
        curRow += 2;
        this.add(this.getLeastSquareSettingPanel(this.babel.getString("SettingPanel", "border.lsa"), leastSquareLabels), "0," + curRow);
        curRow += 2;
        this.add(this.getUncertaintyModelPanel(this.babel.getString("SettingPanel", "border.uncertainty"), uncertaintyModelLabels), "0," + curRow);
        
	}
	
	private JPanel getProjectIdentifierPanel(String title, String labels[]) {
		JPanel panel = new JPanel();
		int curRow = 0;
		
		double size[][] = new double[2][];
		size[0] = new double[] {TableLayout.FILL, 275, 20, 150, TableLayout.FILL }; // Columns
		size[1] = new double[labels.length];
		for (int i=0; i<labels.length; i++) {
			size[1][i] = 25; // Rows
		}
		TableLayout layout = new TableLayout(size);
		panel.setLayout( layout );
		// http://download.oracle.com/javase/tutorial/uiswing/components/border.html
		Border border = BorderFactory.createTitledBorder( title );
		panel.setBorder(border);
		
		// Fuege Komponenten hinzu
		for (String labelText : labels) {
        	JLabel label = new JLabel( labelText );
        					// int top, int left, int bottom, int right
        	//label.setBorder( new EmptyBorder(2,70,2,20) );
        	label.setHorizontalAlignment(JLabel.LEFT);
//        	JTextField field = new JTextField();
        	
        	
        	JTextField field = this.getNonEmptyValueTextField();
        	this.projectIdentifierFields.add(field);
        	panel.add(label, "1," + curRow);
        	panel.add(field, "3," + curRow++);
        }
		
		this.projectIdentifierFields.get(0).setText(this.projectName);
		this.projectIdentifierFields.get(1).setText(this.projectDesc);
		this.projectIdentifierFields.get(2).setText(this.operatorName);	
		
		DataManager dataManager = this.getDataManager();
		for (int i=0; i<this.projectIdentifierFields.size(); i++)
        	this.projectIdentifierFields.get(i).addFocusListener(dataManager);

		return panel;
	}
	
	private JPanel getLeastSquareSettingPanel(String title, String labels[]) {
		DataManager dataManager = this.getDataManager();
		JPanel panel = new JPanel();
		int curRow = 0;
		
		double size[][] = new double[2][];
		size[0] = new double[] {TableLayout.FILL, 275, 20, 150, TableLayout.FILL }; // Columns
		size[1] = new double[labels.length+2];
		for (int i=0; i<size[1].length; i++) 
			size[1][i] = 25; // Rows

		// Zwei Leerzeilen
		size[1][2] = 10;
		size[1][7] = 10;
		
		TableLayout layout = new TableLayout(size);
		panel.setLayout( layout );
		// http://download.oracle.com/javase/tutorial/uiswing/components/border.html
		Border border = BorderFactory.createTitledBorder( title );
		panel.setBorder(border);
		
		NumberComboBox comboBoxes[] = new NumberComboBox[] {
				this.maxIterationCB = new IntegerComboBox(this.defaultIterationValues, this.defaultIterationValues[2]),
				this.monteCarloSamplesCB = new IntegerComboBox(this.defaultMonteCarloSamplesValues, this.defaultMonteCarloSamplesValues[3]),
				this.alphaCB = new DoubleComboBox(this.defaultPropertyValues,  this.defaultPropertyValues[0]),
				this.betaCB  = new DoubleComboBox(this.defaultTestPowerValues,  this.defaultTestPowerValues[2])
		};
		
		// Ausgleichungsmodell
		ButtonGroup groupAdjust = new ButtonGroup();
		this.freeAdjustment = new JRadioButton(labels[curRow], true);
		panel.add(this.freeAdjustment, "1," + curRow + ",3," + curRow++);
		this.dynamicAdjustment = new JRadioButton(labels[curRow]);
		panel.add(this.dynamicAdjustment, "1," + curRow + ",3," + curRow++);
		groupAdjust.add(this.freeAdjustment);
		groupAdjust.add(this.dynamicAdjustment);
		curRow++; // Fuer Luecke
		for (int i=0; i<comboBoxes.length; i++) {
			String labelText = labels[curRow-1];
			NumberComboBox cb = comboBoxes[i];
			JLabel label = new JLabel( labelText );
			label.setHorizontalAlignment(JLabel.LEFT);
			panel.add(label, "1," + curRow);
			panel.add(cb, "3," + curRow++);
			cb.setActionCommand(SettingPanel.LEAST_SQUARE_SETTING);
			cb.addFocusListener(dataManager);
			cb.addActionListener(dataManager);
		}

		// Export CoVar
		curRow++; // Fuer Luecke
		this.exportCoVar = new JCheckBox(labels[curRow-2], false);
		panel.add(this.exportCoVar, "1," + curRow + ",3," + curRow++);
		this.applySigmaApost = new JCheckBox(labels[curRow-2], true);
		panel.add(this.applySigmaApost, "1," + curRow + ",3," + curRow++);
		
		this.freeAdjustment.setActionCommand(SettingPanel.LEAST_SQUARE_SETTING);
        this.freeAdjustment.addActionListener(dataManager);
        this.dynamicAdjustment.setActionCommand(SettingPanel.LEAST_SQUARE_SETTING);
        this.dynamicAdjustment.addActionListener(dataManager);
        
        this.exportCoVar.setActionCommand(SettingPanel.LEAST_SQUARE_SETTING);
        this.exportCoVar.addActionListener(dataManager);
        
        this.applySigmaApost.setActionCommand(SettingPanel.LEAST_SQUARE_SETTING);
        this.applySigmaApost.addActionListener(dataManager);
        
		return panel;
	}
	
	private JPanel getUncertaintyModelPanel(String title, String labels[]) {
		DataManager dataManager = this.getDataManager();
		JPanel panel = new JPanel();
		int curRow = 0;
		
		double size[][] = new double[2][];
		size[0] = new double[] {TableLayout.FILL, 275, 20, 150, TableLayout.FILL }; // Columns
		size[1] = new double[labels.length];
		for (int i=0; i<size[1].length; i++) {
			size[1][i] = 25; // Rows
		}
		TableLayout layout = new TableLayout(size);
		panel.setLayout( layout );
		// http://download.oracle.com/javase/tutorial/uiswing/components/border.html
		Border border = BorderFactory.createTitledBorder( title );
		panel.setBorder(border);
		
		// Stochastisches Modell
		ButtonGroup groupStochModel = new ButtonGroup();
		this.useUncertaintyPOUModel = new JRadioButton(labels[curRow]);
		panel.add(this.useUncertaintyPOUModel, "1," + curRow + ",3," + curRow++);
		this.useUncertaintyMCSModel = new JRadioButton(labels[curRow]);
		panel.add(this.useUncertaintyMCSModel, "1," + curRow + ",3," + curRow++);
		this.useUncertaintyUTModel = new JRadioButton(labels[curRow], true);
		panel.add(this.useUncertaintyUTModel,  "1," + curRow + ",3," + curRow++);
		
		this.useUncertaintyUTModel.setActionCommand(SettingPanel.UNCERTAINTY_MODEL);
		this.useUncertaintyPOUModel.setActionCommand(SettingPanel.UNCERTAINTY_MODEL);
		this.useUncertaintyMCSModel.setActionCommand(SettingPanel.UNCERTAINTY_MODEL);
		
		groupStochModel.add(this.useUncertaintyPOUModel);
		groupStochModel.add(this.useUncertaintyMCSModel);
		groupStochModel.add(this.useUncertaintyUTModel);
		
        this.useUncertaintyPOUModel.addActionListener(dataManager);
        this.useUncertaintyMCSModel.addActionListener(dataManager);
		this.useUncertaintyUTModel.addActionListener(dataManager);
		return panel;
	}
	
	public String[] getProjectIdentifier() {
		String identifier[] = new String[this.projectIdentifierFields.size()];
		for (int i=0; i<identifier.length; i++) {
			identifier[i] = this.projectIdentifierFields.get(i).getText();
		}
		return identifier;
	}
	
	public boolean isFreenetAdjustment() {
		return this.freeAdjustment.isSelected();
	}
	
	public double[] getPropertyValueAndTestPower() {
		return new double[] {
				this.alphaCB.getValue(),
				this.betaCB.getValue()
		};
	}
	
	public boolean exportCovarianceMatrix() {
		return this.exportCoVar.isSelected();
	}
	
	public boolean applyVarianceFactorAposteriori() {
		return this.applySigmaApost.isSelected();
	}
	
	public int getMonteCarloSamples() {
		return (int)this.monteCarloSamplesCB.getValue();
	}
	
	public int getMaxIteration() {
		return (int)this.maxIterationCB.getValue();
	}
	
	public int getUncertaintyModelType() {
		if (this.useUncertaintyMCSModel.isSelected())
			return PreAnalysis.UNCERTAINTY_MODEL_MONTE_CARLO_SIMULATION;
		else if (this.useUncertaintyPOUModel.isSelected())
			return PreAnalysis.UNCERTAINTY_MODEL_PROPAGATION_OF_UNCERTAINTY;
		return PreAnalysis.UNCERTAINTY_MODEL_UNSCENTED_TRANSFORMATION;
	}
			
	@Override
	public String toString() {
		return this.name;
	}
}
