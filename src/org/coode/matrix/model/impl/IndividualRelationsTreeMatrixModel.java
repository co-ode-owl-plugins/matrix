package org.coode.matrix.model.impl;

import org.coode.matrix.model.api.AbstractTreeMatrixModel;
import org.coode.matrix.model.helper.IndividualsHelper;
import org.coode.matrix.ui.renderer.OWLObjectTreeTableCellRenderer;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntologyChange;

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

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 4, 2007<br><br>
 */
public class IndividualRelationsTreeMatrixModel extends AbstractTreeMatrixModel<OWLEntity> {

    private IndividualsHelper helper;

    public IndividualRelationsTreeMatrixModel(OWLObjectTreeTableCellRenderer<OWLEntity> tree, OWLModelManager mngr) {
        super(tree, mngr);
        helper = new IndividualsHelper(mngr.getOWLOntologyManager(), mngr.getActiveOntologies());
    }

    protected String getTreeColumnLabel() {
        return "Individual";
    }

    public Object getMatrixValue(OWLEntity entity, Object prop) {
        if (entity instanceof OWLIndividual){
            if (prop instanceof OWLObjectProperty) {
                return helper.getRelationships((OWLIndividual) entity, (OWLObjectProperty)prop);
            }
            else {
                return super.getMatrixValue(entity, prop);
            }
        }
        return null;
    }

    public List<OWLOntologyChange> setMatrixValue(OWLEntity ind, Object prop, Object value) {
        if (prop instanceof OWLObjectProperty){
            return helper.setRelationships((OWLIndividual) ind,
                    (OWLObjectProperty) prop,
                    (Set<OWLIndividual>) value,
                    mngr.getActiveOntology());
        }
        else{
            return super.setMatrixValue(ind, prop, value);
        }
    }


    public List<OWLOntologyChange> addMatrixValue(OWLEntity rowObj, Object columnObj, Object value) {
        if (columnObj instanceof OWLObjectProperty){
            Set<OWLIndividual> values = null;
            if (value instanceof OWLIndividual){
                values = Collections.singleton((OWLIndividual)value);
            }
            else if (value instanceof Set){
                // @@TODO check the contents of the set
                values = (Set<OWLIndividual>)value;
            }
            if (values != null){
                return helper.addRelationships((OWLIndividual) rowObj,
                        (OWLObjectProperty) columnObj,
                        values,
                        mngr.getActiveOntology());
            }
        }
        return super.addMatrixValue(rowObj, columnObj, value);
    }

    public boolean isSuitableCellValue(Object value, int row, int col) {
        return value instanceof OWLIndividual && isCellEditable(row, col);
    }

    public boolean isSuitableColumnObject(Object columnObject) {
        return columnObject instanceof OWLObjectProperty;
    }

    public boolean isValueRestricted(OWLEntity rowObject, Object columnObject) {
        return false;
    }

    public Set getSuggestedFillers(OWLEntity rowObject, Object columnObject, int threshold) {
        return Collections.EMPTY_SET;
    }

    public boolean isCellEditable(int row, int column) {
        return getRowObject(row) instanceof OWLIndividual;
    }
}
