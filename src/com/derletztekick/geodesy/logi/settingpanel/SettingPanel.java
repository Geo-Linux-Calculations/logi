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

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.derletztekick.geodesy.logi.sql.DataManager;
import com.derletztekick.geodesy.logi.textfieldverifier.AbsoluteValueFormattedTextFieldVerifier;
import com.derletztekick.geodesy.logi.textfieldverifier.LimitedValueDocument;
import com.derletztekick.geodesy.logi.textfieldverifier.NonEmptyStringFormattedTextFieldVerifier;
import com.derletztekick.tools.babel.Babel;


public abstract class SettingPanel extends JPanel {
	public static final String  EXCLUDE_LOCAL_GROUP      = "EXCLUDE_LOCAL_GROUP",
								LEAST_SQUARE_SETTING     = "LEAST_SQUARE_SETTING",
								UNCERTAINTY_MODEL        = "UNCERTAINTY_MODEL",
								TRANSFORMATION_PARAMETER = "TRANSFORMATION_PARAMETER";
	private static final long serialVersionUID = 1707927660376928062L;
	private final DataManager dataManager;
	protected Babel babel;
	private NumberFormat doubleFormat;
	private NumberFormat intFormat;
	
	public SettingPanel(DataManager dataManager) {
		this.dataManager = dataManager;
		this.babel = this.dataManager.getBabel();
		this.initNumberFormats();
	}
	
	protected final DataManager getDataManager() {
		return this.dataManager;
	}
		
	protected JTextField getNonEmptyValueTextField() {
		JTextField jft = new JTextField();
		jft.setDocument(new LimitedValueDocument(250));
		jft.setHorizontalAlignment(JFormattedTextField.LEFT);
        jft.setInputVerifier(new NonEmptyStringFormattedTextFieldVerifier());
		return jft;
	}
		
	protected JFormattedTextField getFormattedTextField(NumberFormat format, boolean excludeZeroValues) {
		// http://download.oracle.com/javase/tutorial/uiswing/components/formattedtextfield.html
		JFormattedTextField jft = new JFormattedTextField(format);
        jft.setInputVerifier(new AbsoluteValueFormattedTextFieldVerifier(excludeZeroValues));
        jft.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        jft.setHorizontalAlignment(JFormattedTextField.RIGHT);
        //((NumberFormatter)jft.getFormatter()).setAllowsInvalid(false);
		return jft;
	}
		
	private void initNumberFormats() {
		this.doubleFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
		this.intFormat = NumberFormat.getIntegerInstance(Locale.ENGLISH);
		
		this.doubleFormat.setMinimumFractionDigits(4);
		this.doubleFormat.setMaximumFractionDigits(8);
		this.doubleFormat.setMinimumIntegerDigits(1);
		
		
		this.doubleFormat.setGroupingUsed(false);
		this.intFormat.setGroupingUsed(false);
	}
	
	public NumberFormat getDoubleFormat() {
		return this.doubleFormat;
	}
	
	public NumberFormat getIntegerFormat() {
		return this.intFormat;
	}
}
