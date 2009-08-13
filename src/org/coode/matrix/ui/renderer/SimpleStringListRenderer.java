package org.coode.matrix.ui.renderer;

import org.semanticweb.owlapi.model.OWLStringLiteral;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Set;
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
 * Date: Nov 17, 2008<br><br>
 */
public class SimpleStringListRenderer extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        if (value instanceof Set){
            Set<OWLStringLiteral> constants = (Set <OWLStringLiteral>)value;
            if (constants.size() == 1){
                value = constants.iterator().next().getLiteral();
            }
            else{
                StringBuilder str = new StringBuilder();
                for (OWLStringLiteral constant : constants){
                    if (str.length() > 0){
                        str.append(", ");
                    }
                    str.append("\"").append(constant.getLiteral()).append("\"");
                }
                value = str.toString();
            }
        }
        return super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, col);
    }
}
