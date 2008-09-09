package org.coode.matrix.ui.renderer;

import org.coode.matrix.model.impl.FillerModel;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Collection;
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
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 3, 2007<br><br>
 */
public class OWLObjectListRenderer implements TableCellRenderer {

    private static final Color NOT_EDITABLE_COLOUR = new Color(80, 80, 80);
    private static final Color EDITABLE_COLOUR = new Color(0, 0, 0);

    private static final Color ASSERTED_COLOUR = new Color(255, 255, 255);
    private static final Color INHERITED_COLOUR = new Color(255, 255, 160);

    private OWLObjectsRenderer ren;

    private JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));

    private Component delegate;

    private DefaultTableCellRenderer defaultCellRenderer = new DefaultTableCellRenderer();


    public OWLObjectListRenderer(OWLObjectsRenderer ren) {
        this.ren = ren;
    }

    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        if (value instanceof FillerModel){
            p.removeAll();
            if (isSelected){
                p.setBackground(jTable.getSelectionBackground());
            }
            else{
                p.setBackground(jTable.getBackground());
            }
            FillerModel fillerModel = (FillerModel) value;
            addFillers(fillerModel.getAssertedFillersFromEquiv(), NOT_EDITABLE_COLOUR, ASSERTED_COLOUR);
            addFillers(fillerModel.getAssertedFillersFromSupers(), EDITABLE_COLOUR, ASSERTED_COLOUR);
            addFillers(fillerModel.getInheritedFillers(), NOT_EDITABLE_COLOUR, INHERITED_COLOUR);
            delegate = p;
        }
        else if (value instanceof Collection) {
            value = ren.render((Collection<OWLObject>) value);
            delegate = defaultCellRenderer.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, col);
        }
        return delegate;
    }


    public Dimension getPreferredSize() {
        return delegate.getPreferredSize();
    }


    private void addFillers(Set<OWLDescription> fillers, Color color, Color background) {
        if (!fillers.isEmpty()){
            JLabel label = new JLabel(ren.render(fillers));
            label.setFont(OWLRendererPreferences.getInstance().getFont());
            label.setForeground(color);
            label.setBackground(background);
            p.add(label);
        }
    }
}
