package org.coode.matrix.model.impl;

import java.util.Collections;
import java.util.List;
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

import org.coode.matrix.model.api.AbstractMatrixModel;
import org.coode.matrix.model.helper.IndividualsHelper;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 3, 2007<br><br>
 */
public class ClassMembershipTreeMatrixModel extends AbstractMatrixModel<OWLClass> {

    private static final String COLUMN_NAME = "Members";

    private IndividualsHelper helper;

    public ClassMembershipTreeMatrixModel(OWLObjectTree<OWLClass> tree, OWLModelManager mngr) {
        super(tree, mngr);
        helper = new IndividualsHelper(mngr.getOWLOntologyManager(), mngr.getActiveOntologies());
        addColumn(COLUMN_NAME);
    }


    public String getTreeColumnLabel() {
        return "Class";
    }


    @Override
    public Object getMatrixValue(OWLClass rowObject, Object columnObject) {
        if (columnObject instanceof String){
            return helper.getMembers(rowObject);
        }
        else {
            return super.getMatrixValue(rowObject, columnObject);
        }
    }


    @Override
    public List<OWLOntologyChange> addMatrixValue(OWLClass cls, Object col, Object value) {
        if (col instanceof String){
            return helper.addMembers(cls, (Set<OWLIndividual>) value, mngr.getActiveOntology());
        }
        else{
            return super.addMatrixValue(cls, col, value);
        }
    }


    @Override
    public List<OWLOntologyChange> setMatrixValue(OWLClass cls, Object col, Object value) {
        if (col instanceof String){
            return helper.setMembers(cls, (Set<OWLIndividual>) value, mngr.getActiveOntology());
        }
        else{
            return super.setMatrixValue(cls, col, value);
        }
    }


    public boolean isSuitableCellValue(Object value, int row, int col) {
        return value instanceof OWLNamedIndividual; // can only drop individuals
    }


    public Object getSuitableColumnObject(Object columnObject) {
        if (columnObject instanceof OWLAnnotationProperty){
            return columnObject;
        }
        return null;
    }    


    public boolean isValueRestricted(OWLClass rowObject, Object columnObject) {
        return false;
    }


    public Set getSuggestedFillers(OWLClass rowObject, Object columnObject, int threshold) {
        return Collections.EMPTY_SET;
    }
}
