package org.coode.matrix.ui.renderer;

import java.awt.Component;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.semanticweb.owlapi.model.OWLLiteral;

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
            Set<OWLLiteral> constants = (Set <OWLLiteral>)value;
            if (constants.size() == 1){
                value = constants.iterator().next().getLiteral();
            }
            else{
                StringBuilder str = new StringBuilder();
                for (OWLLiteral constant : constants){
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
