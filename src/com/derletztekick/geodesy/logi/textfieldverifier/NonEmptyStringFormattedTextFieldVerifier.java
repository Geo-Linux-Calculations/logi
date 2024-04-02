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

package com.derletztekick.geodesy.logi.textfieldverifier;

import java.awt.Color;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class NonEmptyStringFormattedTextFieldVerifier extends InputVerifier {

	@Override
	public boolean verify(JComponent comp) {
		if (comp instanceof JTextField) {
			JTextField textField = (JTextField) comp;
			String content = textField.getText();
			if (content == null || content.trim().isEmpty())
				return false;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean shouldYieldFocus(JComponent comp) {
        if (!this.verify(comp)) {
        	comp.setForeground(Color.RED);
            return false;
        }
        else {
        	comp.setForeground(Color.BLACK);
            return true;
        }
    }

}
