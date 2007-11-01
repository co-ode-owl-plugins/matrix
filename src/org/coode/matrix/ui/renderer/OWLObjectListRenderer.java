package org.coode.matrix.ui.renderer;

import org.coode.matrix.model.impl.FillerModel;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObject;

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
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 3, 2007<br><br>
 */
public class OWLObjectListRenderer extends DefaultTableCellRenderer {

    private OWLObjectsRenderer ren;

    public OWLObjectListRenderer(OWLObjectsRenderer ren) {
        this.ren = ren;
    }

    public Component getTableCellRendererComponent(JTable jTable, Object object, boolean b, boolean b1, int i, int i1) {
        if (object instanceof FillerModel){
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
            p.setPreferredSize(getPreferredSize());
            if (jTable.isCellSelected(i, i1)){
                p.setBackground(jTable.getSelectionBackground());
            }
            else{
                p.setBackground(jTable.getBackground());
            }
            FillerModel fillerModel = (FillerModel) object;
            addFillers(fillerModel.getAssertedFillersFromEquiv(), p, Color.RED);
            addFillers(fillerModel.getAssertedFillersFromSupers(), p, Color.BLACK);
            addFillers(fillerModel.getInheritedFillers(), p, Color.GRAY);
            return p;
        }
        else if (object instanceof Set) {
            object = ren.render((Set<OWLObject>) object);
        }
        return super.getTableCellRendererComponent(jTable, object, b, b1, i, i1);
    }

    private void addFillers(Set<OWLDescription> fillers, JPanel component, Color color) {
        if (!fillers.isEmpty()){
            JLabel label = new JLabel(ren.render(fillers));
            label.setForeground(color);
            component.add(label);
        }
    }
}
