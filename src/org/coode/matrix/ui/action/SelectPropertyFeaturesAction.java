package org.coode.matrix.ui.action;

import org.coode.matrix.model.helper.ObjectPropertyHelper;
import org.coode.matrix.ui.component.MatrixTreeTable;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;

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

    public SelectPropertyFeaturesAction(OWLEditorKit eKit, MatrixTreeTable table) {
        super(LABEL, OWLIcons.getIcon("property.object.png"));
        this.eKit = eKit;
        this.table = table;
    }

    public void dispose() {
    }

    public void actionPerformed(ActionEvent actionEvent) {
        UIHelper uihelper = new UIHelper(eKit);

        PropertyFeaturesPanel featuresPanel = new PropertyFeaturesPanel();

        if (uihelper.showDialog("Pick which features you want to be visible", featuresPanel) == JOptionPane.OK_OPTION){
            for (ObjectPropertyHelper.Characteristic c : ObjectPropertyHelper.Characteristic.values()) {
                if (featuresPanel.isVisible(c)) {
                    if (!table.containsColumn(c)){
                        table.addColumn(c);// @@TODO , helper.toIndex(col));
                    }
                }
                else {
                    if (table.containsColumn(c)){
                        table.removeColumn(c);
                    }
                }
            }
        }
    }

    class PropertyFeaturesPanel extends JPanel{

        private Map<JCheckBox, ObjectPropertyHelper.Characteristic> checkMap = new HashMap<JCheckBox, ObjectPropertyHelper.Characteristic>();

        public PropertyFeaturesPanel() {
            setLayout(new BorderLayout());
            Box box = new Box(BoxLayout.Y_AXIS);

            for (ObjectPropertyHelper.Characteristic c : ObjectPropertyHelper.Characteristic.values()){
                JCheckBox check = new JCheckBox(c.toString());
                check.setSelected(table.containsColumn(c));
                checkMap.put(check, c);
                box.add(check);
                box.add(Box.createVerticalStrut(7));
            }

            add(box);
        }

        public Set<ObjectPropertyHelper.Characteristic> getVisible(){
            Set<ObjectPropertyHelper.Characteristic> visible = new HashSet<ObjectPropertyHelper.Characteristic>();
            for (JCheckBox cb : checkMap.keySet()){
                if (cb.isSelected()){
                    visible.add(checkMap.get(cb));
                }
            }
            return visible;
        }

        public boolean isVisible(ObjectPropertyHelper.Characteristic c){
            for (JCheckBox cb : checkMap.keySet()){
                if (checkMap.get(cb).equals(c)){
                    return cb.isSelected();
                }
            }
            return false;
        }
    }
}
