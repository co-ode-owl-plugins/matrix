package org.coode.matrix.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Collection;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.coode.matrix.model.impl.FillerModel;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owlapi.model.OWLObject;

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

    private static final Color NOT_EDITABLE_COLOUR = new Color(50, 50, 50);
    private static final Color EDITABLE_COLOUR = new Color(0, 0, 0);


    private OWLObjectsRenderer ren;

    private JPanel p;

    private Component delegate;

    private DefaultTableCellRenderer defaultCellRenderer = new DefaultTableCellRenderer();


    public OWLObjectListRenderer(OWLObjectsRenderer ren) {
        this.ren = ren;
        p = new JPanel(){
            private static final long serialVersionUID = 1L;

            // for some reason the BoxLayout reports a prefSize of 0, 0 so do this by hand
            @Override
            public Dimension getPreferredSize() {
                int prefWidth = 0;
                int prefHeight = 0;
                for (Component c : getComponents()){
                    final Dimension pref = c.getPreferredSize();
                    prefWidth += pref.width;
                    prefHeight = Math.max(prefHeight, pref.height);
                }
                
                return new Dimension(prefWidth, prefHeight);
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
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
            addFillers(fillerModel.getAssertedFillersFromEquiv(), EDITABLE_COLOUR, null, Font.BOLD, isSelected);
            addFillers(fillerModel.getAssertedFillersFromSupers(), EDITABLE_COLOUR, null, Font.PLAIN, isSelected);
            addFillers(fillerModel.getInheritedFillers(), NOT_EDITABLE_COLOUR, null, Font.PLAIN, isSelected);
            delegate = p;
        }
        else{
            if (value instanceof Collection) {
                value = ren.render((Collection<OWLObject>) value);
            }
            delegate = defaultCellRenderer.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, col);
        }
        return delegate;
    }


    public Dimension getPreferredSize() {
        return delegate.getPreferredSize();
    }


    private void addFillers(Set<OWLObject> fillers, Color color, Color background, int style, boolean isSelected) {
        if (!fillers.isEmpty()){
            String labelString = ren.render(fillers);
            if (NOT_EDITABLE_COLOUR.equals(color)){
                labelString = "(" + labelString + ")";
            }
            JLabel label = new JLabel(labelString);
            Font font = OWLRendererPreferences.getInstance().getFont();
            if (style != Font.PLAIN){
                font = font.deriveFont(style);
            }
            label.setFont(font);
            if (!isSelected){
                label.setForeground(color);
            }
            else{
                // invert the colour to make it more visible against the selection
                label.setForeground(new Color(255-color.getRed(), 255-color.getBlue(), 255-color.getGreen()));
            }
            if (background != null){
                label.setBackground(background);
                label.setOpaque(!isSelected);
            }
            p.add(label);
            p.add(Box.createHorizontalStrut(4));
        }
    }
}
