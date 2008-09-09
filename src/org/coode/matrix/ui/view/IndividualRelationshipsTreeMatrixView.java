package org.coode.matrix.ui.view;

import org.coode.matrix.model.api.MatrixModel;
import org.coode.matrix.model.hierarchy.ClassAndIndividualHierarchyProvider;
import org.coode.matrix.model.impl.IndividualRelationsTreeMatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.ui.action.AddObjectPropertyAction;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObjectProperty;

import javax.swing.table.TableCellEditor;

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
public class IndividualRelationshipsTreeMatrixView extends AbstractTreeMatrixView<OWLEntity> {

    private OWLObjectHierarchyProvider<OWLEntity> hierarchyProvider;

    protected boolean isOWLIndividualView() {
        return true;
    }

    protected void initialiseMatrixView() throws Exception {
        addAction(new AddObjectPropertyAction(getOWLEditorKit(), getTreeTable()), "A", "B");
    }

    protected OWLObjectHierarchyProvider<OWLEntity> getHierarchyProvider() {
        if (hierarchyProvider == null){
            final OWLModelManager mngr = getOWLModelManager();
            hierarchyProvider = new ClassAndIndividualHierarchyProvider(mngr.getOWLOntologyManager(),
                                                                        mngr.getOWLClassHierarchyProvider(),
                                                                        mngr.getActiveOntologies());
        }
        return hierarchyProvider;
    }

    protected MatrixModel<OWLEntity> createMatrixModel(OWLObjectTree<OWLEntity> tree) {
        return new IndividualRelationsTreeMatrixModel(tree, getOWLModelManager());
    }

    protected TableCellEditor getCellEditor(OWLEntity rowObject, Object columnObject) {
        if (columnObject instanceof OWLObjectProperty){
            setEditorType(OWLObjectListParser.INDIVIDUAL);
        }
        else{
            setEditorType(OWLObjectListParser.DATATYPE);
        }
        return super.getCellEditor(rowObject, columnObject);
    }

    public void disposeView() {
        hierarchyProvider.dispose();
        super.disposeView();
    }
}
