package org.coode.matrix.ui.action;

import org.protege.editor.core.ui.view.DisposableAction;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
/*
* Copyright (C) 2007, University of Manchester
*
* Modifications to the initial code base are copyright of their
* respective authors, or their employers as appropriate.  Authorship
* of the modifications may be determined from the ChangeLog placed at
* the end of this file.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.

* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.

* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Sep 9, 2008<br><br>
 */
public class FitColumnsToContentAction extends DisposableAction {

    private JTable table;


    public FitColumnsToContentAction(JTable table, String name, Icon icon) {
        super(name, icon);
        this.table = table;
    }


    public void actionPerformed(ActionEvent event) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        packColumns(table);
    }


    private void packColumns(JTable table) {
        for (int i=0; i<table.getColumnCount(); i++){
            final TableColumn col = table.getColumnModel().getColumn(i);
            int w = getPreferredColumnWidth(table, i, col);
            col.setPreferredWidth(w);
        }
    }


    private int getPreferredColumnWidth(JTable table, int index, TableColumn col){
        int width = 0;

        // get column header renderer
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            final JTableHeader header = table.getTableHeader();
            if (header != null){
                renderer = header.getDefaultRenderer();
            }
        }

        // Get width of column header
        if (renderer != null){
            Component comp = renderer.getTableCellRendererComponent(
                    table, col.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().width;
        }

        // Get maximum width of column data
        for (int r=0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, index);
            Component comp = renderer.getTableCellRendererComponent(
                    table, table.getValueAt(r, index), false, false, r, index);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        width = width + (table.getColumnModel().getColumnMargin());

        return width;
    }


    public void dispose() {
        table = null;
    }
}
