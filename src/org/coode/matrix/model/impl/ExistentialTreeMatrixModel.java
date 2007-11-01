package org.coode.matrix.model.impl;

import org.coode.matrix.model.api.AbstractTreeMatrixModel;
import org.coode.matrix.model.helper.FillerHelper;
import org.coode.matrix.ui.renderer.OWLObjectTreeTableCellRenderer;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.*;

import java.net.URI;
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
 * Date: Jul 3, 2007<br><br>
 */
public class ExistentialTreeMatrixModel extends AbstractTreeMatrixModel<OWLClass> {

    public static final String NONE = "none";

    private FillerHelper helper;

    public ExistentialTreeMatrixModel(OWLObjectTreeTableCellRenderer tree, OWLModelManager mngr) {
        super(tree, mngr);
        helper = new FillerHelper(mngr);
    }

    protected String getTreeColumnLabel() {
        return "Class";
    }

    public Object getMatrixValue(OWLClass rowObject, Object columnObject) {
        if (columnObject instanceof OWLObjectProperty){
            return new FillerModel(rowObject, (OWLObjectProperty)columnObject, helper);
        }
        else {
            return super.getMatrixValue(rowObject, columnObject);
        }
    }

    public List<OWLOntologyChange> setMatrixValue(OWLClass cls, Object prop, Object value) {
        if (prop instanceof OWLObjectProperty){
            if (value.equals(NONE)) {
                return helper.setNamedFillers(Collections.EMPTY_SET, cls,
                                              (OWLObjectProperty)prop, mngr.getActiveOntology());
            }
            else if (value instanceof Set) {
                return helper.setNamedFillers((Set<OWLDescription>) value, cls,
                                              (OWLObjectProperty)prop, mngr.getActiveOntology());
            }
            else if (value instanceof OWLClass){
                return helper.setNamedFillers(Collections.singleton((OWLDescription)value), cls,
                                              (OWLObjectProperty)prop, mngr.getActiveOntology());
            }
        }
        else{
            return super.setMatrixValue(cls, prop, value);
        }
        return Collections.EMPTY_LIST;
    }

    public boolean isSuitableCellValue(Object value, int row, int col) {
        return value instanceof OWLClass;
    }

    public boolean isSuitableColumnObject(Object columnObject) {
        return columnObject instanceof OWLObjectProperty || columnObject instanceof URI;
    }

    public boolean isValueRestricted(OWLClass cls, Object p) {
        return helper.fillersRestricted((OWLObjectProperty)p);
    }

    public Set getSuggestedFillers(OWLClass cls, Object p, int threshold) {
        return helper.getSuggestedFillers(cls, (OWLObjectProperty)p, threshold);
    }


    protected String renderColumnTitle(Object columnObject) {
        String title = super.renderColumnTitle(columnObject);
        if (columnObject instanceof OWLProperty){
            title += " some";
        }
        return title;
    }
}