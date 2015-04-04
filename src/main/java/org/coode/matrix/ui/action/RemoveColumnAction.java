package org.coode.matrix.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.coode.matrix.ui.component.MatrixTreeTable;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;

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
public class RemoveColumnAction extends DisposableAction {
    private static final long serialVersionUID = 1L;
    private static final String LABEL = "Remove column from matrix";

    private OWLEditorKit eKit;

    protected MatrixTreeTable treeTable;

    public RemoveColumnAction(OWLEditorKit eKit, MatrixTreeTable table) {
        super(LABEL, OWLIcons.getIcon("property.annotation.remove.png"));
        this.eKit = eKit;
        this.treeTable = table;
    }

    @Override
    public void dispose() {
    }

    public void actionPerformed(ActionEvent actionEvent) {
        int col = treeTable.getTable().getSelectedColumn();
        if (col >= 0) {
            int modelIndex = treeTable.getTable().convertColumnIndexToModel(col);
            final Object colObject = treeTable.getModel().getColumnObjectAtModelIndex(modelIndex);
            treeTable.getModel().removeColumn(colObject);
        }
        else {
            UIHelper helper = new UIHelper(eKit);

            JList selectorList = createSelector();

            if (helper.showDialog(LABEL, new JScrollPane(selectorList)) == JOptionPane.OK_OPTION) {
                final Object columnObj = selectorList.getSelectedValue();
                treeTable.getModel().removeColumn(columnObj);
            }
        }
    }

    private JList createSelector() {
        ListModel listmodel = new AbstractListModel() {
            private static final long serialVersionUID = 1L;
            public int getSize() {
                return treeTable.getModel().getColumnCount();
            }

            public Object getElementAt(int i) {
                int modelIndex = treeTable.getTable().convertColumnIndexToModel(i);
                return treeTable.getModel().getColumnObjectAtModelIndex(modelIndex);
            }
        };

        JList selectorList = new JList(listmodel);

        selectorList.setSelectedIndex(0);

        selectorList.setCellRenderer(new OWLCellRenderer(eKit));

        return selectorList;
    }
}
