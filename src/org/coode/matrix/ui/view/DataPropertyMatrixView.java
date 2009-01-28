package org.coode.matrix.ui.view;

import org.coode.matrix.model.api.MatrixModel;
import org.coode.matrix.model.helper.PropertyHelper;
import org.coode.matrix.model.impl.DataPropertyTreeMatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser2;
import org.coode.matrix.ui.action.SelectPropertyFeaturesAction;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owl.model.OWLDataProperty;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
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
public class DataPropertyMatrixView extends AbstractTreeMatrixView<OWLDataProperty> implements Findable<OWLDataProperty> {

    protected void initialiseMatrixView() throws Exception {

        addAction(new SelectPropertyFeaturesAction(OWLDataProperty.class, getOWLEditorKit(), getTreeTable()), "A", "B");

        getTreeTable().getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    protected OWLObjectHierarchyProvider<OWLDataProperty> getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getOWLDataPropertyHierarchyProvider();
    }

    protected MatrixModel<OWLDataProperty> createMatrixModel(OWLObjectTree<OWLDataProperty> tree) {
        return new DataPropertyTreeMatrixModel(tree, getOWLModelManager());
    }

    protected boolean isOWLObjectPropertyView() {
        return true;
    }

    protected TableCellEditor getCellEditor(OWLDataProperty p, Object colObj) {
        if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            if (colObj instanceof PropertyHelper.OWLPropertyFeature){
                switch((PropertyHelper.OWLPropertyFeature)colObj){
                    case DOMAIN:
                        setEditorType(OWLObjectListParser2.CLASS);
                        return super.getCellEditor(p, colObj);
                    case RANGE:
                        setEditorType(OWLObjectListParser2.DATARANGE);
                        return super.getCellEditor(p, colObj);

                }
            }
        }
        else if (colObj instanceof URI){
            setEditorType(OWLObjectListParser2.LITERAL);
            return super.getCellEditor(p, colObj);
        }
        // otherwise, this will be one of the boolean characteristics - so just use the table default
        return null;
    }

    protected TableCellRenderer getCellRendererForColumn(Object colObj) {
        if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case DOMAIN:
                case RANGE:
                    return super.getCellRendererForColumn(colObj);
            }
        }
        return super.getCellRendererForColumn(colObj);
    }


    public List<OWLDataProperty> find(String match) {
        return new ArrayList<OWLDataProperty>(getOWLModelManager().getEntityFinder().getMatchingOWLDataProperties(match));
    }


    public void show(OWLDataProperty prop) {
        getTreeTable().getTree().setSelectedOWLObject(prop);
    }
}