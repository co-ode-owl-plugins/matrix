package org.coode.matrix.ui.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import org.coode.matrix.model.api.MatrixModel;
import org.coode.matrix.model.impl.RestrictionTreeMatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.ui.action.AddDataPropertyAction;
import org.coode.matrix.ui.action.AddObjectPropertyAction;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owlapi.model.OWLClass;
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
public class ClassMatrixView extends AbstractTreeMatrixView<OWLClass> implements Findable<OWLClass> {
    private static final long serialVersionUID = 1L;

    @Override
    protected void initialiseMatrixView() {
        addAction(new AddObjectPropertyAction(getOWLEditorKit(), getTreeTable(), true), "A", "B");
        addAction(new AddDataPropertyAction(getOWLEditorKit(), getTreeTable(), true), "A", "C");
    }

    @Override
    protected OWLObjectHierarchyProvider<OWLClass> getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider();
    }

    @Override
    protected final MatrixModel<OWLClass> createMatrixModel(OWLObjectTree<OWLClass> tree) {
        return new RestrictionTreeMatrixModel(tree, getHierarchyProvider(), getOWLModelManager());
    }

    @Override
    protected boolean isOWLClassView() {
        return true;
    }

    @Override
    protected TableCellEditor getCellEditor(Object columnObject) {

        if (columnObject instanceof RestrictionTreeMatrixModel.PropertyRestrictionPair){

            final RestrictionTreeMatrixModel.PropertyRestrictionPair pair = (RestrictionTreeMatrixModel.PropertyRestrictionPair) columnObject;

            // make sure the editor only shows the appropriate type
            if (pair.getColumnObject() instanceof OWLObjectProperty){
                setEditorType(OWLObjectListParser.ParseType.DESCRIPTION);
            }
            else{
                setEditorType(OWLObjectListParser.ParseType.DATARANGE);
            }
        }
        else{
            setEditorType(OWLObjectListParser.ParseType.LITERAL);
        }

        return super.getCellEditor(columnObject);
    }


    public List<OWLClass> find(String match) {
        return new ArrayList<OWLClass>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLClasses(match));
    }


    public void show(OWLClass cls) {
        getTreeTable().getTree().setSelectedOWLObject(cls);
    }
}