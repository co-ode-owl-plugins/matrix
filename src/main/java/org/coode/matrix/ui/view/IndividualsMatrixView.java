package org.coode.matrix.ui.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import org.coode.matrix.model.api.MatrixModel;
import org.coode.matrix.model.impl.PropertyAssertionsMatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.ui.action.AddDataPropertyAction;
import org.coode.matrix.ui.action.AddObjectPropertyAction;
import org.protege.editor.owl.model.hierarchy.IndividualsByTypeHierarchyProvider;
import org.protege.editor.owl.ui.tree.CountingOWLObjectTreeCellRenderer;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;

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
public class IndividualsMatrixView extends AbstractTreeMatrixView<OWLObject> implements Findable<OWLEntity> {
    private static final long serialVersionUID = 1L;

    @Override
    protected void initialiseMatrixView() {
        addAction(new AddObjectPropertyAction(getOWLEditorKit(), getTreeTable()), "A", "B");
        addAction(new AddDataPropertyAction(getOWLEditorKit(), getTreeTable()), "A", "C");

        final OWLObjectTree<OWLObject> tree = getTreeTable().getTree();
        tree.setCellRenderer(new CountingOWLObjectTreeCellRenderer<OWLObject>(getOWLEditorKit(), tree));
    }


    @Override
    protected boolean isOWLIndividualView() {
        return true;
    }


    @Override
    protected IndividualsByTypeHierarchyProvider getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getOWLIndividualsByTypeHierarchyProvider();
    }


    @Override
    protected MatrixModel<OWLObject> createMatrixModel(OWLObjectTree<OWLObject> tree) {
        return new PropertyAssertionsMatrixModel(tree, getOWLModelManager());
    }


    @Override
    protected TableCellEditor getCellEditor(Object columnObject) {
        if (columnObject instanceof OWLObjectProperty){
            setEditorType(OWLObjectListParser.ParseType.INDIVIDUAL);
        }
        else{
            setEditorType(OWLObjectListParser.ParseType.LITERAL);
        }
        return super.getCellEditor(columnObject);
    }


    @Override
    public void disposeView() {
        super.disposeView();
    }

    public List<OWLEntity> find(String match) {
        List<OWLEntity> results = new ArrayList<OWLEntity>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLClasses(match));
        results.retainAll(getHierarchyProvider().getRootClasses());
        results.addAll(getOWLModelManager().getOWLEntityFinder().getMatchingOWLIndividuals(match));
        return results;
    }


    public void show(OWLEntity entity) {
        getTreeTable().getTree().setSelectedOWLObject(entity);
    }
}
