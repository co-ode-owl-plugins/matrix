package org.coode.matrix.ui.action;

import org.coode.matrix.ui.component.AnnotationURIList;
import org.coode.matrix.ui.component.MatrixTreeTable;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URI;

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
public class AddAnnotationAction extends DisposableAction {

    private static final String LABEL = "Add annotation column to matrix";

    private OWLEditorKit eKit;
    private MatrixTreeTable table;

    public AddAnnotationAction(OWLEditorKit eKit, MatrixTreeTable table) {
        super(LABEL, OWLIcons.getIcon("property.annotation.add.png"));
        this.eKit = eKit;
        this.table = table;
    }

    public void dispose() {
    }

    public void actionPerformed(ActionEvent actionEvent) {
        UIHelper helper = new UIHelper(eKit);

        AnnotationURIList uriList = new AnnotationURIList(eKit);

        if (helper.showDialog("Pick an annotation URI", uriList) == JOptionPane.OK_OPTION){
            URI uri = uriList.getSelectedURI();
            if (uri != null){
                table.addColumn(uri);
            }
        }
    }
}
