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

import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.LasertrackerUncertaintyModel;
import com.derletztekick.geodesy.bundleadjustment.v2.pointgroup.uncertainty.UncertaintyModel;
import com.derletztekick.geodesy.bundleadjustment.v2.preadjustment.PreAnalysis;
import com.derletztekick.geodesy.logi.sql.DataManager;
import com.derletztekick.geodesy.logi.table.row.PointRow;
import com.derletztekick.geodesy.logi.table.row.PolarObservationRow;
import com.derletztekick.geodesy.logi.table.row.UncertaintyRow;
import com.derletztekick.tools.geodesy.Constant;
import com.derletztekick.tools.io.LockFileReader;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.ErrorHandler;
public class XMLFileReader extends LockFileReader implements ErrorHandler {
	private File xmlFile;
	private final DataManager dataManager;
	private boolean isValidDocument = true;
	private UncertaintyModel uncertaintyModel = null;
	public XMLFileReader(DataManager dataManager, File xmlFile)  {
		super(xmlFile);
		this.xmlFile = xmlFile;
		this.dataManager = dataManager;
	}
	
	@Override
	public boolean readSourceFile() {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		docBuilderFactory.setValidating(false);
		
		docBuilderFactory.setCoalescing(false);
		docBuilderFactory.setExpandEntityReferences(false);
		docBuilderFactory.setIgnoringComments(false);
		docBuilderFactory.setIgnoringElementContentWhitespace(false);

		docBuilderFactory.setNamespaceAware(true);
		docBuilderFactory.setXIncludeAware(false);
		
		DocumentBuilder docBuilder;
		Document doc;
		try {
			Schema schema = schemaFactory.newSchema(new File("xsd/logi.xsd"));
			docBuilderFactory.setSchema(schema);
			docBuilder = docBuilderFactory.newDocumentBuilder();
			docBuilder.setErrorHandler(this);
			doc = docBuilder.parse(this.xmlFile);
			// Lese globale Einstellungen
			// Root-Element
			Element root = doc.getDocumentElement();
			root.getAttribute("type");
			//boolean isFreenet = root.getAttribute("type").equalsIgnoreCase("free");
			
			NodeList globalPoints = doc.getElementsByTagName("point");
			for (int i=0; i<globalPoints.getLength(); i++) {
				Element polarPoint = (Element)globalPoints.item(i);
				try {
					PointRow point = new PointRow(
							-1,
							polarPoint.getAttribute("pid"),
							Double.parseDouble(polarPoint.getAttribute("x")),
							Double.parseDouble(polarPoint.getAttribute("y")),
							Double.parseDouble(polarPoint.getAttribute("z")),
							true
					);
					this.dataManager.saveTableRow(-1, point);
				}
				catch (NumberFormatException e) {}
			}
			
			NodeList polarPointGroups = doc.getElementsByTagName("polarPointGroup");
			for (int i=0; i<polarPointGroups.getLength(); i++) {
				Element polarPointGroup  = (Element)polarPointGroups.item(i);
				NodeList polarPoints     = polarPointGroup.getElementsByTagName("polarPoint");
				NodeList uncertainties   = polarPointGroup.getElementsByTagName("uncertainty");
				NodeList trafoParameters = polarPointGroup.getElementsByTagName("transformationParameter");
				// Fuege eine neue Gruppe hinzu
				int groupId = this.dataManager.addLocalSystem( polarPointGroup.getAttribute("sid") );
				if (groupId < 0)
					continue;
				
				// Fuege Punkte dieser Gruppe hinzu
				for (int j=0; j<polarPoints.getLength(); j++) {
					Element polarPoint = (Element)polarPoints.item(j);
					try {
						double d = Double.parseDouble(polarPoint.getAttribute("distance"));
						double a = Double.parseDouble(polarPoint.getAttribute("azimuth"));
						double z = Double.parseDouble(polarPoint.getAttribute("zenith"));
						
						PolarObservationRow obs = new PolarObservationRow(
								-1,
								polarPoint.getAttribute("pid"),
								d, a, z, true, null
						);
						// Strecke muss groesser Null sein und Winkel zw. +/-400, sonst abbrechen
						if (d > Constant.EPS && z >= -400 && z <= 400 && a >= -400 && a <= 400)
							this.dataManager.saveTableRow(groupId, obs);
					}
					catch (NumberFormatException e) {}
				}
				
				// Ermittle a-priori Unsicherheiten der Gruppe
				
				// Default-Values
				this.uncertaintyModel = new LasertrackerUncertaintyModel();
				double uncertaintiesLasertracker[] = this.uncertaintyModel.getDefaultSigmas();
				UncertaintyRow[] uncertaintyRows   = new UncertaintyRow[uncertaintiesLasertracker.length];
				for (int j=0; j<uncertaintiesLasertracker.length; j++) {
					double scale = this.uncertaintyModel.isAngle(j)?Constant.RHO_RAD2GRAD:1.0;
					uncertaintyRows[j] = new UncertaintyRow("", scale*uncertaintiesLasertracker[j], PreAnalysis.DISTRIBUTION_NORMAL);	
				}
				
				// Ueberschreiben mit neuen Werten
				for (int j=0; j<uncertainties.getLength(); j++) {
					Element uncertainty = (Element)uncertainties.item(j);
					try { 
						String name         = uncertainty.getAttribute("name");
						double value        = Double.parseDouble(uncertainty.getAttribute("value"));
						String distribution = uncertainty.getAttribute("distribution");
						uncertaintyRows     = this.mergeUncertaintyRow(uncertaintyRows, name, value, distribution);						
					}
					catch (Exception e) {}
				}
				// Speichere ermittelte Unsicherheiten und Verteilungen
				if (uncertainties.getLength() > 0 && uncertaintyRows != null) {
					this.dataManager.saveLocalUncertainties(groupId, uncertaintyRows, null);
				}
				
				// Zu bestimmende Trafo-Parameter einlesen
				
				// Default-Values (Reihenfolge: tx, ty, tz, rx, ry, rz, m)
				boolean fixedTransformationParameter[] = new boolean[]{false, false, false, false, false, false, false};
				// feste/zu schaetzende Transformationsparameter einlesen
				for (int j=0; j<trafoParameters.getLength(); j++) {
					Element trafoParameter = (Element)trafoParameters.item(j);
					try { 
						String name     = trafoParameter.getAttribute("name");
						boolean isFixed = Boolean.parseBoolean(trafoParameter.getAttribute("fixed"));
						
						fixedTransformationParameter = this.mergeTransformationParameter(fixedTransformationParameter, name, isFixed);
					}
					catch (Exception e) {}
				}
				// Speichere Trafo-Parameter Einstellungen
				if (trafoParameters.getLength() > 0 && fixedTransformationParameter != null) {
					this.dataManager.saveLocalGroupSettings(groupId, true, 1.0, fixedTransformationParameter);
				}
			}			
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			this.isValidDocument = false;
			return false;
		} catch (SAXException e) {
			this.isValidDocument = false;
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			this.isValidDocument = false;
			e.printStackTrace();
			return false;
		}
		return true;
	}
		
	private boolean[] mergeTransformationParameter(boolean fixedTransformationParameter[], String name, boolean isFixed) {
		if (name.equalsIgnoreCase("tx"))
			fixedTransformationParameter[0] = isFixed;
		else if (name.equalsIgnoreCase("ty"))
			fixedTransformationParameter[1] = isFixed;
		else if (name.equalsIgnoreCase("tz"))
			fixedTransformationParameter[2] = isFixed;
		else if (name.equalsIgnoreCase("rx"))
			fixedTransformationParameter[3] = isFixed;
		else if (name.equalsIgnoreCase("ry"))
			fixedTransformationParameter[4] = isFixed;
		else if (name.equalsIgnoreCase("rz"))
			fixedTransformationParameter[5] = isFixed;
		else if (name.equalsIgnoreCase("m"))
			fixedTransformationParameter[6] = isFixed;
		return fixedTransformationParameter;
	}
	
	private UncertaintyRow[] mergeUncertaintyRow(UncertaintyRow[] uncertaintyRows, String name, double value, String distribution) {
		if (this.uncertaintyModel == null)
			return uncertaintyRows;
		
		// Bestimme Index im Array
		int index = this.uncertaintyModel.getIndexByAbbreviation(name);
		if (index < 0)
			return uncertaintyRows;
		
		// Bestimme Verteilung
		int distType = PreAnalysis.DISTRIBUTION_NORMAL;
		if (distribution.equals("triangular"))
			distType = PreAnalysis.DISTRIBUTION_TRIANGULAR;
		else if (distribution.equals("uniform")) 
			distType = PreAnalysis.DISTRIBUTION_UNIFORM;

		UncertaintyRow row = new UncertaintyRow(name, value>0?value:uncertaintyRows[index].getValue(), distType);	
		// "Sortiere" Werte ins Array...
		
		uncertaintyRows[index] = row;		
		return uncertaintyRows;
	}

	/**
	 * Liefert <code>true</code>, wenn das Dokument valid ist
	 * @return isValidDocument
	 */
	public boolean isValidDocument() {
		return this.isValidDocument;
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		e.getMessage();
		this.isValidDocument = false;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		e.getMessage();
		this.isValidDocument = false;
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		e.getMessage();	
	}

	@Override
	public void parse(String line) {
		// TODO Auto-generated method stub
	}
}
