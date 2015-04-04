package org.coode.matrix.ui.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import org.coode.matrix.model.api.MatrixModel;
import org.coode.matrix.model.impl.ClassMembershipTreeMatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owlapi.model.OWLClass;

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
public class IndividualTypesMatrixView extends AbstractTreeMatrixView<OWLClass> implements Findable<OWLClass> {
    private static final long serialVersionUID = 1L;
    @Override
    protected void initialiseMatrixView() {
    }

    @Override
    protected OWLObjectHierarchyProvider<OWLClass> getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider();
    }

    @Override
    protected MatrixModel<OWLClass> createMatrixModel(OWLObjectTree<OWLClass> tree) {
        return new ClassMembershipTreeMatrixModel(tree, getOWLModelManager());
    }

    @Override
    protected TableCellEditor getCellEditor(Object columnObject) {
        TableCellEditor editor = super.getCellEditor(columnObject);
        if (columnObject instanceof String){
            setEditorType(OWLObjectListParser.ParseType.INDIVIDUAL);
        }
        else{
            setEditorType(OWLObjectListParser.ParseType.LITERAL);
        }
        return editor;
    }

        public List<OWLClass> find(String match) {
        return new ArrayList<OWLClass>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLClasses(match));
    }


    public void show(OWLClass cls) {
        getTreeTable().getTree().setSelectedOWLObject(cls);
    }
}
