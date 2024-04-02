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

package com.derletztekick.geodesy.logi.table;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;

public class TableRowHeader extends JViewport implements AdjustmentListener{

	private static final long serialVersionUID = -2411657998607114276L;
	private JTable  table;
    private Cell cell;
    private boolean useColumHeader = false;
    private Map<Integer, String> tableRowHead = new LinkedHashMap<Integer, String>();

    public TableRowHeader( JTable table, JScrollPane parent ) {
    	this(table, parent, 70, false );
    }
    
    public TableRowHeader( JTable table, JScrollPane parent, int width ) {
    	this(table, parent, width, false );
    }
    
    public TableRowHeader( JTable table, JScrollPane parent, int width, boolean useColumHeader ) {
    	this.useColumHeader = useColumHeader;
        this.table = table;
        this.cell = new Cell();
        this.setPreferredSize( new Dimension( width, 0 ) );

        parent.setRowHeader(this);
        parent.getVerticalScrollBar().addAdjustmentListener( this );
        parent.getHorizontalScrollBar().addAdjustmentListener( this );
    }
    
    public void setTableRowHead(int index, String value) {
    	this.tableRowHead.put(index, value);
    	this.cell.set( index );
    	repaint();
    }

    public void adjustmentValueChanged( AdjustmentEvent e ) {
        repaint();
    }

    protected void paintComponent(Graphics g){
        super.paintComponent( g );
        Rectangle rec = TableRowHeader.this.getViewRect();

        int y = 0;
        int rows = table.getRowCount();
        int index = 0;

        if( rows == 0 )
            return;

        if( y + table.getRowHeight( 0 ) < rec.y ){
            while( index < rows ){
                int height = table.getRowHeight( index );

                if( y + height < rec.y ){
                    y += height;
                    index++;
                }
                else
                    break;
            }
        }

        int max = rec.y + rec.height;
        int width = getWidth();

        while( y < max && index < rows ){
            int height = table.getRowHeight( index );
            this.cell.set( index );
            SwingUtilities.paintComponent( g, cell, this, 0, y-rec.y, width, height );

            y += height;
            index++;
        }
    }

    private class Cell extends JLabel {
		private static final long serialVersionUID = 7348826085576853229L;

		public void set( int row ) {
            setHorizontalAlignment( CENTER );
            setForeground( TableRowHeader.this.getForeground() );
            setBackground( TableRowHeader.this.getBackground() );
            setFont( TableRowHeader.this.getFont() );
            if (useColumHeader) {
            	setText( table.getModel().getColumnName(row) );
            }
            else
            	setText( tableRowHead.get(row) );
            setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );
        }
    }
}
