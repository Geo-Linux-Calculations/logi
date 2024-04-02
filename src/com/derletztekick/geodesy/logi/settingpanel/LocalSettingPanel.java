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

import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.LasertrackerUncertaintyModel;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.correlation.CorrelationFunction;
import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;
import com.derletztekick.geodesy.logi.sql.DataManager;
import com.derletztekick.geodesy.logi.table.TimeDependentUncertaintyTable;
import com.derletztekick.geodesy.logi.table.UncertaintyTable;
import com.derletztekick.geodesy.logi.table.model.TimeDependentUncertaintyTableModel;
import com.derletztekick.geodesy.logi.table.model.UncertaintyTableModel;
import com.derletztekick.geodesy.logi.table.row.CorrelationFunctionOption;
import com.derletztekick.geodesy.logi.table.row.DistributionOption;
import com.derletztekick.geodesy.logi.table.row.TimeDependentUncertaintyRow;
import com.derletztekick.geodesy.logi.table.row.UncertaintyRow;

import layout.TableLayout;

public class LocalSettingPanel extends SettingPanel {
	
	private static final long serialVersionUID = -1502069897029329744L;
	private TableLayout panelLayout;
	private String name;
	private JFormattedTextField sigma2aprioTextField = this.getFormattedTextField(this.getDoubleFormat(), true);
	private UncertaintyTableModel targetDependentUncertaintyTableModel;
	private UncertaintyTableModel instrumentDependentUncertaintyTableModel;
	private TimeDependentUncertaintyTableModel timeDependentUncertaintyTableModel;
	private JCheckBox enableCB;
	private JCheckBox trafoParamCB[];

	public LocalSettingPanel(DataManager dataManager) {
		super(dataManager);
		this.name = this.babel.getString("SettingPanel", "tab.setting");
		this.init();
	}
	
	private void init() {
		String columnUncertaintyNames[] = new String[] {
				this.babel.getString("SettingPanel", "uncertainty.col0"),
				this.babel.getString("SettingPanel", "uncertainty.col1")
		};

		String columnTimeUncertaintyNames[] = new String[] {
				this.babel.getString("SettingPanel", "uncertainty.time0"),
				this.babel.getString("SettingPanel", "uncertainty.time1"),
				this.babel.getString("SettingPanel", "uncertainty.time2"),
				this.babel.getString("SettingPanel", "uncertainty.time3"),
				this.babel.getString("SettingPanel", "uncertainty.time4"),
				this.babel.getString("SettingPanel", "uncertainty.time5")
		};

		Map<Integer, DistributionOption> distributions = new LinkedHashMap<Integer, DistributionOption>(3);
		distributions.put(PreAnalysis.DISTRIBUTION_NORMAL, new DistributionOption(PreAnalysis.DISTRIBUTION_NORMAL, this.babel.getString("SettingPanel", "distribution.normal")));
		distributions.put(PreAnalysis.DISTRIBUTION_TRIANGULAR, new DistributionOption(PreAnalysis.DISTRIBUTION_TRIANGULAR, this.babel.getString("SettingPanel", "distribution.triangular")));
		distributions.put(PreAnalysis.DISTRIBUTION_UNIFORM, new DistributionOption(PreAnalysis.DISTRIBUTION_UNIFORM, this.babel.getString("SettingPanel", "distribution.uniform")));

		Map<Integer, CorrelationFunctionOption> correlationFunctions = new LinkedHashMap<Integer, CorrelationFunctionOption>(3);
		correlationFunctions.put(-1, new CorrelationFunctionOption(-1, "f(\u0394T)=0"));
		correlationFunctions.put(CorrelationFunction.EXP_COS_CORR_FUN,  new CorrelationFunctionOption(CorrelationFunction.EXP_COS_CORR_FUN,  "f(\u0394T)=\u03C3\u00B2\u00B7a\u00B7exp(-b\u00B7|\u0394T|)\u00B7cos(c\u00B7|\u0394T|)"));
		correlationFunctions.put(CorrelationFunction.GAUSSIAN_CORR_FUN, new CorrelationFunctionOption(CorrelationFunction.GAUSSIAN_CORR_FUN, "f(\u0394T)=\u03C3\u00B2\u00B7a\u00B7exp(-b\u00B7\u0394T\u00B2)\u00B7cos(c\u00B7\u0394T\u00B2)"));
		
		String sigmaInstrumentLabels[] = null;
		String sigmaTargetLabels[] = null;
		String observationTypeLabels[] = null;
		
		UncertaintyRow uncertaintyInstrumentRows[] = null;
		UncertaintyRow uncertaintyTargetRows[] = null;
		      
		String trafoParamLabels[] = new String[] {
				this.babel.getString("SettingPanel", "trafo.tx"),
				this.babel.getString("SettingPanel", "trafo.ty"),
				this.babel.getString("SettingPanel", "trafo.tz"),
				
				this.babel.getString("SettingPanel", "trafo.rx"),
				this.babel.getString("SettingPanel", "trafo.ry"),
				this.babel.getString("SettingPanel", "trafo.rz"),
				
				this.babel.getString("SettingPanel", "trafo.m"),
		};
		
		sigmaInstrumentLabels = new String[] {
				this.babel.getString("SettingPanel", "uncertainty.fix0"),
				this.babel.getString("SettingPanel", "uncertainty.fix1"),
				this.babel.getString("SettingPanel", "uncertainty.fix2"),

				this.babel.getString("SettingPanel", "uncertainty.fix3"), 
				this.babel.getString("SettingPanel", "uncertainty.fix4"), 

				this.babel.getString("SettingPanel", "uncertainty.fix5"), 
				this.babel.getString("SettingPanel", "uncertainty.fix6"), 
				this.babel.getString("SettingPanel", "uncertainty.fix7"), 
				this.babel.getString("SettingPanel", "uncertainty.fix8"), 

				this.babel.getString("SettingPanel", "uncertainty.fix9"), 
				this.babel.getString("SettingPanel", "uncertainty.fix10"),
				this.babel.getString("SettingPanel", "uncertainty.fix11"),
				this.babel.getString("SettingPanel", "uncertainty.fix12"),
				this.babel.getString("SettingPanel", "uncertainty.fix13"),

				this.babel.getString("SettingPanel", "uncertainty.fix14"),
				this.babel.getString("SettingPanel", "uncertainty.fix15"),

				this.babel.getString("SettingPanel", "uncertainty.fix16"),
				this.babel.getString("SettingPanel", "uncertainty.fix17"),
				this.babel.getString("SettingPanel", "uncertainty.fix18")
		};
		
		sigmaTargetLabels = new String[] {
				this.babel.getString("SettingPanel", "uncertainty.vari0"),
				this.babel.getString("SettingPanel", "uncertainty.vari1"),
				this.babel.getString("SettingPanel", "uncertainty.vari2"),
				this.babel.getString("SettingPanel", "uncertainty.vari3"),
				this.babel.getString("SettingPanel", "uncertainty.vari4"),
				this.babel.getString("SettingPanel", "uncertainty.vari5")	
        };
		
		observationTypeLabels = new String[] {
				this.babel.getString("SettingPanel", "observationtype.distance"),
				this.babel.getString("SettingPanel", "observationtype.azimuth"),
				this.babel.getString("SettingPanel", "observationtype.zenith")
		};
		
		uncertaintyInstrumentRows = new UncertaintyRow[sigmaInstrumentLabels.length];
		uncertaintyTargetRows     = new UncertaintyRow[sigmaTargetLabels.length];
		
		for (int i=0; i<uncertaintyInstrumentRows.length; i++)
			uncertaintyInstrumentRows[i] = new UncertaintyRow(sigmaInstrumentLabels[i], 1.0, PreAnalysis.DISTRIBUTION_NORMAL);
		
		for (int i=0; i<uncertaintyTargetRows.length; i++)
			uncertaintyTargetRows[i] = new UncertaintyRow(sigmaTargetLabels[i], 1.0, PreAnalysis.DISTRIBUTION_NORMAL);
			
		// @see http://java.sun.com/products/jfc/tsc/articles/tablelayout/
		// @see http://www.java-forum.org/blogs/tfa/39-snippet-tablelayout.html
        // mögliche Werte in Größen-Array
        //     Integer:               Breite der Spalte / Höhe der Zeile in Pixeln
        //     Double:                Anteil der verfügbaren Größe (z.B. 0.25 für ein viertel)
        //     TableLayout.FILL:      Komponente füllt Zellen aus
        //     TableLayout.PREFERRED: bevorzugte Größe der Komponente 
        double size[][] = { 
                { TableLayout.FILL }, // Columns
                { TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED} };// Rows
        this.setBorder( new EmptyBorder(10,30,10,30) );
        this.panelLayout = new TableLayout(size);
        this.setLayout(this.panelLayout );
        
        int curRow = 0;

        // GUI-Elememente
        DataManager dataManager = this.getDataManager();
        this.enableCB = new JCheckBox(this.babel.getString("SettingPanel", "enable"), true);
        this.enableCB.setActionCommand(SettingPanel.EXCLUDE_LOCAL_GROUP);
        this.enableCB.addActionListener( dataManager );

        this.instrumentDependentUncertaintyTableModel = new UncertaintyTableModel(columnUncertaintyNames, distributions, uncertaintyInstrumentRows); 
        this.targetDependentUncertaintyTableModel     = new UncertaintyTableModel(columnUncertaintyNames, distributions, uncertaintyTargetRows); 	
        this.timeDependentUncertaintyTableModel       = new TimeDependentUncertaintyTableModel(
        		columnTimeUncertaintyNames, 
        		observationTypeLabels, 
        		correlationFunctions,
        		new TimeDependentUncertaintyRow(-1, LasertrackerUncertaintyModel.DISTANCE3D, 1.0, 0.0, 1.0, 0.0, 0.0),
        		new TimeDependentUncertaintyRow(-1, LasertrackerUncertaintyModel.AZIMUTHANGLE, 1.0, 0.0, 1.0, 0.0, 0.0),
    			new TimeDependentUncertaintyRow(-1, LasertrackerUncertaintyModel.ZENITHANGLE, 1.0, 0.0, 1.0, 0.0, 0.0)
        );
        
        this.instrumentDependentUncertaintyTableModel.addTableModelListener(dataManager);
        this.targetDependentUncertaintyTableModel.addTableModelListener(dataManager);
        this.timeDependentUncertaintyTableModel.addTableModelListener(dataManager);
        
        this.sigma2aprioTextField.addPropertyChangeListener("value", dataManager);
        
        this.add(this.enableCB, "0," + curRow);
        curRow += 2;
        this.add(this.getTransformationParameterGroup(this.babel.getString("SettingPanel", "border.trafo"), trafoParamLabels), "0," + curRow);
        curRow += 2;
        this.add(this.getSigmaAprioriTextField(this.babel.getString("SettingPanel", "border.sigma2aprio"), this.babel.getString("SettingPanel", "uncertainty.sigma2aprio"), this.sigma2aprioTextField), "0," + curRow);
        curRow += 2;
        this.add(this.getTablePanel(
        			this.babel.getString("SettingPanel", "border.instr"), 
        			this.instrumentDependentUncertaintyTableModel), "0," + curRow);
        curRow += 2;
        this.add(this.getTablePanel(
    			this.babel.getString("SettingPanel", "border.target"), 
    			this.targetDependentUncertaintyTableModel), "0," + curRow);
        
        curRow += 2;
        this.add(this.getTablePanel(
    			this.babel.getString("SettingPanel", "border.correlation"), 
    			this.timeDependentUncertaintyTableModel), "0," + curRow);
	}
	
	public void reload(boolean enable, double sigma2priori, UncertaintyRow uncertaintyRows[], TimeDependentUncertaintyRow timeDependentUncertaintyRows[], boolean fixedTrafoParameters[] ) {
		this.enableCB.setSelected(enable);

		UncertaintyRow instrumentenDependentUncertaintyRows[] = this.instrumentDependentUncertaintyTableModel.getUncertaintyRows();
		UncertaintyRow targetDependentUncertaintyRows[]       = this.targetDependentUncertaintyTableModel.getUncertaintyRows();
		TimeDependentUncertaintyRow timeUncertaintyRows[]     = this.timeDependentUncertaintyTableModel.getTimeDependentUncertaintyRows();
		
		for (int i=0; uncertaintyRows != null && i<this.instrumentDependentUncertaintyTableModel.getRowCount(); i++) {
			instrumentenDependentUncertaintyRows[i].setDistribution(uncertaintyRows[i].getDistribution());
			instrumentenDependentUncertaintyRows[i].setValue(uncertaintyRows[i].getValue());
		}
		
		for (int i=0; uncertaintyRows != null && i<this.targetDependentUncertaintyTableModel.getRowCount(); i++) {
			targetDependentUncertaintyRows[i].setDistribution(uncertaintyRows[this.instrumentDependentUncertaintyTableModel.getRowCount()+i].getDistribution());
			targetDependentUncertaintyRows[i].setValue(uncertaintyRows[this.instrumentDependentUncertaintyTableModel.getRowCount()+i].getValue());
		}
		
		for (int i=0; timeDependentUncertaintyRows != null && i<this.timeDependentUncertaintyTableModel.getRowCount(); i++) {
			timeUncertaintyRows[i].setCorrelationFunctionType(timeDependentUncertaintyRows[i].getCorrelationFunctionType());
			timeUncertaintyRows[i].setCoefA(timeDependentUncertaintyRows[i].getCoefA());
			timeUncertaintyRows[i].setCoefB(timeDependentUncertaintyRows[i].getCoefB());
			timeUncertaintyRows[i].setCoefC(timeDependentUncertaintyRows[i].getCoefC());
			timeUncertaintyRows[i].setUncertainty(timeDependentUncertaintyRows[i].getUncertainty());
			timeUncertaintyRows[i].setMaximalTimeDifference(timeDependentUncertaintyRows[i].getMaximalTimeDifference());
		}
				
		for (int i=0; fixedTrafoParameters != null && i<fixedTrafoParameters.length; i++) {
			this.trafoParamCB[i].setSelected( !fixedTrafoParameters[i] );
		}
		
		this.sigma2aprioTextField.setValue(sigma2priori);
	}
	
	private JPanel getTransformationParameterGroup(String title, String paramLabels[]) {
		JPanel panel = new JPanel();
		DataManager dataManager = this.getDataManager();
		double size[][] = new double[][] {
				{TableLayout.FILL, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL}, // Columns
				{25, 25, 25}
		};
		
		TableLayout layout = new TableLayout(size);
		panel.setLayout( layout );
		Border border = BorderFactory.createTitledBorder( title );
		panel.setBorder(border);
		
		this.trafoParamCB = new JCheckBox[paramLabels.length];
		int row = 0;
		for (int i=0, col=1; i<paramLabels.length; i++) {
			JCheckBox cb = new JCheckBox(paramLabels[i], i+1<paramLabels.length);
			cb.setActionCommand(SettingPanel.TRANSFORMATION_PARAMETER);
			cb.addActionListener(dataManager);
			this.trafoParamCB[i] = cb;
			panel.add(cb, col + "," + row);
			if (col == 5) {
				col = 1;
				row++;
			}
			else
				col+=2;
		}
		return panel;
	}
	
	private JPanel getSigmaAprioriTextField(String title, String sigmaLabel, JFormattedTextField field) {
		JPanel panel = new JPanel();
		int curRow = 0;
		
		double size[][] = new double[2][];
		size[0] = new double[] {TableLayout.FILL, 250, 20, 125, TableLayout.FILL }; // Columns
		size[1] = new double[] {25};
		
		TableLayout layout = new TableLayout(size);
		panel.setLayout( layout );
		// http://download.oracle.com/javase/tutorial/uiswing/components/border.html
		Border border = BorderFactory.createTitledBorder( title );
		panel.setBorder(border);
       	JLabel label = new JLabel( sigmaLabel );
       					// int top, int left, int bottom, int right
      	//label.setBorder( new EmptyBorder(2,70,2,20) );
       	label.setHorizontalAlignment(JLabel.LEFT);
       	panel.add(label, "1," + curRow);
       	panel.add(field, "3," + curRow++);       
		return panel;
	}
	
	private JScrollPane getTablePanel(String title, UncertaintyTableModel tableModel) {
//		JPanel panel = new JPanel();
//		double size[][] = new double[][] {
//				{TableLayout.FILL, 10, TableLayout.PREFERRED, 10, TableLayout.FILL}, // Columns
//				{TableLayout.PREFERRED} // Rows
//		};
		
//		TableLayout layout = new TableLayout(size);
//		panel.setLayout( layout );
//		Border border = BorderFactory.createTitledBorder( title );
//		panel.setBorder(border);
		
		UncertaintyTable table = new UncertaintyTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder( title )); // BorderFactory.createEmptyBorder()
		table.setTableRowHeader(scrollPane);
//		panel.add(scrollPane, "2,0");
		scrollPane.setPreferredSize(new Dimension( (int)table.getPreferredScrollableViewportSize().getWidth()-10, (int)table.getPreferredSize().getHeight()+60));
//		layout.setColumn(2, table.getPreferredScrollableViewportSize().getWidth()-10); // -10 == je 5px Rand
//		layout.setRow(0, table.getPreferredSize().getHeight()+40); // +40, da das Scollpanel einen Rahmen hat --> Puffer, damit 
		return scrollPane;
	}
	
	private JScrollPane getTablePanel(String title, TimeDependentUncertaintyTableModel tableModel) {
		TimeDependentUncertaintyTable table = new TimeDependentUncertaintyTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder( title )); 
		table.setTableRowHeader(scrollPane);
		scrollPane.setPreferredSize(new Dimension( (int)table.getPreferredScrollableViewportSize().getWidth()-10, (int)table.getPreferredSize().getHeight()+60));
		return scrollPane;
	}
	
	/**
	 * Liefert ein Boolsches-Array indem <code>true</code> bedeute, dass der Parameter fest und
	 * <code>false</code> dass der Parameter frei (= zu schaetzen) ist. 
	 * 
	 * @return fixedTransformationParameters
	 */
	public boolean[] getFixedTransformationParameter() {
		boolean param[] = new boolean[this.trafoParamCB.length];
		for (int i=0; i<param.length; i++) {
			param[i] = !this.trafoParamCB[i].isSelected();
		}
		return param;
	}
	
	/**
	 * Liefert die vom Nutzer eingegebenen a-priori Standardabweichungen 
	 * und Verteilung der Instrumentenparameter und der unabhaengigien Einflussparameter
	 * @return uncertaintyValues
	 */
	public UncertaintyRow[] getAprioriStandardDeviationsAndDistributions() {
		UncertaintyRow uncertaintyRows[] = new UncertaintyRow[this.instrumentDependentUncertaintyTableModel.getRowCount() + this.targetDependentUncertaintyTableModel.getRowCount()];
		UncertaintyRow instrumentenDependentUncertaintyRows[] = this.instrumentDependentUncertaintyTableModel.getUncertaintyRows();
		UncertaintyRow targetDependentUncertaintyRows[] = this.targetDependentUncertaintyTableModel.getUncertaintyRows();
		
		for (int i=0; i<this.instrumentDependentUncertaintyTableModel.getRowCount(); i++) {
			uncertaintyRows[i] = instrumentenDependentUncertaintyRows[i];
		}
		
		for (int i=0; i<this.targetDependentUncertaintyTableModel.getRowCount(); i++) {
			uncertaintyRows[this.instrumentDependentUncertaintyTableModel.getRowCount()+i] = targetDependentUncertaintyRows[i];
		}
		return uncertaintyRows;
	}
	
	/**
	 * Liefert die Einstellungen zu den zeit-bezogenen Unsicherheiten
	 * @return corrFuncValues
	 */
	public TimeDependentUncertaintyRow[] getTimeDependentCoorelationFunctionValues() {
		return this.timeDependentUncertaintyTableModel.getTimeDependentUncertaintyRows();
	}
	
	/**
	 * Liefert den a-priori Varianzfaktor
	 * @return sigma2aprio
	 */
	public double getVarianceFactorApriori() {
		return Double.parseDouble(this.sigma2aprioTextField.getText());
	}
	
	/**
	 * Liefert <code>true</code>, wenn die Gruppe in der AGL
	 * beruecksichtigt werden soll
	 */
	public boolean isEnabled() {
		return this.enableCB.isSelected();
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
