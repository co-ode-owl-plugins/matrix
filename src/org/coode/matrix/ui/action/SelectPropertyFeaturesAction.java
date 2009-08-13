package org.coode.matrix.ui.action;

import org.coode.matrix.model.helper.PropertyHelper;
import org.coode.matrix.ui.component.MatrixTreeTable;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
public class SelectPropertyFeaturesAction extends DisposableAction {

    private static final String LABEL = "Select property feature columns";

    private OWLEditorKit eKit;
    private MatrixTreeTable table;

    private Class<? extends OWLProperty> type;


    public SelectPropertyFeaturesAction(Class<? extends OWLProperty> type, OWLEditorKit eKit, MatrixTreeTable table) {
        super(LABEL, (OWLObjectProperty.class.isAssignableFrom(type))? OWLIcons.getIcon("property.object.png") : OWLIcons.getIcon("property.data.png"));
        this.type = type;
        this.eKit = eKit;
        this.table = table;
    }

    public void dispose() {
    }

    public void actionPerformed(ActionEvent actionEvent) {
        UIHelper uihelper = new UIHelper(eKit);

        PropertyFeaturesPanel featuresPanel = new PropertyFeaturesPanel(type);

        if (uihelper.showDialog("Pick which features you want to be visible", featuresPanel) == JOptionPane.OK_OPTION){
            for (PropertyHelper.OWLPropertyCharacteristic c : featuresPanel.getCharacteristics()) {
                if (table.getModel().getColumns().contains(c)){
                    if (!featuresPanel.isVisible(c)) {
                        table.removeColumn(c);
                    }
                }
                else {
                    if (featuresPanel.isVisible(c)){
                        table.addColumn(c);
                    }
                }
            }
        }
    }

    class PropertyFeaturesPanel extends JPanel{

        private Map<JCheckBox, PropertyHelper.OWLPropertyCharacteristic> checkMap = new HashMap<JCheckBox, PropertyHelper.OWLPropertyCharacteristic>();

        public PropertyFeaturesPanel(Class<? extends OWLProperty> type) {
            setLayout(new BorderLayout());
            Box box = new Box(BoxLayout.Y_AXIS);

            for (PropertyHelper.OWLPropertyCharacteristic c : PropertyHelper.OWLPropertyCharacteristic.values()){
                if ((OWLObjectProperty.class.isAssignableFrom(type) && c.isObjectPropertyCharacteristic()) ||
                    (OWLDataProperty.class.isAssignableFrom(type) && c.isDataPropertyCharacteristic())){
                    JCheckBox check = new JCheckBox(c.toString());
                    check.setSelected(table.getModel().getModelIndexOfColumn(c) != -1);
                    checkMap.put(check, c);
                    box.add(check);
                    box.add(Box.createVerticalStrut(7));
                }
            }

            add(box);
        }

        public Set<PropertyHelper.OWLPropertyCharacteristic> getVisible(){
            Set<PropertyHelper.OWLPropertyCharacteristic> visible = new HashSet<PropertyHelper.OWLPropertyCharacteristic>();
            for (JCheckBox cb : checkMap.keySet()){
                if (cb.isSelected()){
                    visible.add(checkMap.get(cb));
                }
            }
            return visible;
        }

        public boolean isVisible(PropertyHelper.OWLPropertyCharacteristic c){
            for (JCheckBox cb : checkMap.keySet()){
                if (checkMap.get(cb).equals(c)){
                    return cb.isSelected();
                }
            }
            return false;
        }

        public Set<PropertyHelper.OWLPropertyCharacteristic> getCharacteristics(){
            return new HashSet<PropertyHelper.OWLPropertyCharacteristic>(checkMap.values());
        }
    }
}
