package org.coode.matrix.ui.action;

import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.coode.matrix.model.api.TreeMatrixModel;
import org.coode.matrix.model.helper.ObjectPropertyHelper;
import org.coode.matrix.ui.component.AnnotationURIList;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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
    private TreeMatrixModel model;
    private ObjectPropertyHelper helper;

    public SelectPropertyFeaturesAction(OWLEditorKit eKit, TreeMatrixModel model, ObjectPropertyHelper helper) {
        super(LABEL, OWLIcons.getIcon("property.object.png"));
        this.eKit = eKit;
        this.model = model;
        this.helper = helper;
    }

    public void dispose() {
    }

    public void actionPerformed(ActionEvent actionEvent) {
        UIHelper uihelper = new UIHelper(eKit);

        PropertyFeaturesPanel uriList = new PropertyFeaturesPanel();

        if (uihelper.showDialog("Pick which features you want to be visible", uriList) == JOptionPane.OK_OPTION){
            for (final String col : ObjectPropertyHelper.names) {
                if (uriList.isVisible(col)) {
                    model.addColumn(col, helper.toIndex(col));
                }
                else {
                    model.removeColumn(col);
                }
            }
        }
    }

    class PropertyFeaturesPanel extends JPanel{

        private List<JCheckBox> checkMap = new ArrayList<JCheckBox>();

        public PropertyFeaturesPanel() {
            setLayout(new BorderLayout());
            Box box = new Box(BoxLayout.Y_AXIS);

            for (int i=0; i<ObjectPropertyHelper.names.size(); i++){
                final String name = ObjectPropertyHelper.names.get(i);
                JCheckBox check = new JCheckBox(name);
                check.setSelected(model.getColumnObjects().contains(name));
                checkMap.add(check);
                box.add(check);
                box.add(Box.createVerticalStrut(7));
            }

            add(box);
        }

        public List<String> getVisible(){
            List<String> visible = new ArrayList<String>();
            for (int i=0; i<ObjectPropertyHelper.names.size(); i++){
                if (checkMap.get(i).isSelected()){
                    visible.add(ObjectPropertyHelper.names.get(i));
                }
            }
            return visible;
        }

        public boolean isVisible(String col){
            int index = helper.toIndex(col);
            return checkMap.get(index).isSelected();
        }
    }
}
