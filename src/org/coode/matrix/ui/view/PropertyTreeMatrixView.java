package org.coode.matrix.ui.view;

import org.coode.matrix.model.api.AbstractTreeMatrixModel;
import org.coode.matrix.model.api.TreeMatrixModel;
import org.coode.matrix.model.impl.ObjectPropertyTreeMatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.model.helper.ObjectPropertyHelper;
import org.coode.matrix.ui.renderer.OWLObjectTreeTableCellRenderer;
import org.coode.matrix.ui.action.SelectPropertyFeaturesAction;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.semanticweb.owl.model.OWLObjectProperty;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
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
public class PropertyTreeMatrixView extends AbstractTreeMatrixView<OWLObjectProperty> {

    private ObjectPropertyHelper objPropHelper;

    protected void initialiseMatrixView() throws Exception {
        objPropHelper = new ObjectPropertyHelper(getOWLModelManager());

        addAction(new SelectPropertyFeaturesAction(getOWLEditorKit(), getMatrixModel(), objPropHelper), "A", "B");
    }

    protected OWLObjectHierarchyProvider<OWLObjectProperty> getHierarchyProvider() {
        return getOWLModelManager().getOWLObjectPropertyHierarchyProvider();
    }

    protected AbstractTreeMatrixModel<OWLObjectProperty> createMatrixModel(OWLObjectTreeTableCellRenderer<OWLObjectProperty> tree) {
        return new ObjectPropertyTreeMatrixModel(tree, getOWLModelManager());
    }

    protected boolean isOWLObjectPropertyView() {
        return true;
    }

    protected TableCellEditor getCellEditor(Object object, OWLObjectProperty p) {
        if (object instanceof String){
            switch (objPropHelper.toIndex((String)object)) {
                case ObjectPropertyHelper.DOMAIN:
                case ObjectPropertyHelper.RANGE:
                    setEditorType(OWLObjectListParser.CLASS);
                    return super.getCellEditor(object, p);
                case ObjectPropertyHelper.INVERSE:
                    setEditorType(OWLObjectListParser.OBJPROP);
                    return super.getCellEditor(object, p);
            }
        }
        else if (object instanceof URI){
            setEditorType(OWLObjectListParser.DATATYPE);
            return super.getCellEditor(object, p);
        }
        // otherwise, this will be one of the boolean characteristics - so just use the table default
        return null;
    }

    protected TableCellRenderer getCellRendererForColumn(Object columnObject) {
        if (columnObject instanceof String){
            switch (objPropHelper.toIndex((String)columnObject)) {
                case ObjectPropertyHelper.DOMAIN:
                case ObjectPropertyHelper.RANGE:
                case ObjectPropertyHelper.INVERSE:
                    return super.getCellRendererForColumn(columnObject);
            }
        }
        else if (columnObject instanceof URI){
            return super.getCellRendererForColumn(columnObject);
        }
        return null;
    }

//    protected void setColumnWidths(TableColumnModel tcm) {
//        for (int i = ObjectPropertyHelper.START_FEATURES+1; i <= ObjectPropertyHelper.END_FEATURES+1; i++) {
//            tcm.getColumn(i).setMinWidth(40);
//            tcm.getColumn(i).setMaxWidth(40);
//        }
//    }
}
