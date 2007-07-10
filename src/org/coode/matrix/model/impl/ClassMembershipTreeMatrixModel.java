package org.coode.matrix.model.impl;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.coode.matrix.model.api.AbstractTreeMatrixModel;
import org.coode.matrix.model.helper.IndividualsHelper;
import org.coode.matrix.ui.renderer.OWLObjectTreeTableCellRenderer;
import org.protege.editor.owl.model.OWLModelManager;

import java.util.Set;
import java.util.List;
import java.util.Collections;
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
public class ClassMembershipTreeMatrixModel extends AbstractTreeMatrixModel<OWLClass> {

    private static final String COLUMN_NAME = "Members";

    private IndividualsHelper helper;

    public ClassMembershipTreeMatrixModel(OWLObjectTreeTableCellRenderer tree, OWLModelManager mngr) {
        super(tree, mngr);
        helper = new IndividualsHelper(mngr.getOWLOntologyManager(), mngr.getActiveOntologies());
        addColumn(COLUMN_NAME, -1);
    }

    protected String getTreeColumnLabel() {
        return "Class";
    }

    public Object getMatrixValue(OWLClass rowObject, Object columnObject) {
        if (columnObject instanceof String){
            return helper.getMembers(rowObject);
        }
        else {
            return super.getMatrixValue(rowObject, columnObject);
        }
    }

    public List<OWLOntologyChange> setMatrixValue(OWLClass cls, Object col, Object value) {
        if (col instanceof String){
            return helper.setMembers(cls, (Set<OWLIndividual>) value, mngr.getActiveOntology());
        }
        else{
            return super.setMatrixValue(cls, col, value);
        }
    }

    public boolean isSuitableCellValue(Object value, int row, int col) {
        return value instanceof OWLIndividual; // can only drop individuals
    }

    public boolean isSuitableColumnObject(Object columnObject) {
        return false;  // cannot add more columns
    }

    public boolean isValueRestricted(OWLClass rowObject, Object columnObject) {
        return false;
    }

    public Set getSuggestedFillers(OWLClass rowObject, Object columnObject, int threshold) {
        return Collections.EMPTY_SET;
    }
}
