package org.coode.matrix.model.impl;

import org.coode.matrix.model.api.AbstractMatrixModel;
import org.coode.matrix.model.helper.FillerHelper;
import org.coode.matrix.model.helper.ObjectPropertyHelper;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owl.model.*;

import java.net.URI;
import java.util.ArrayList;
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
public class ExistentialTreeMatrixModel extends AbstractMatrixModel<OWLClass> {

    public static final String NONE = "none";

    private FillerHelper fillerHelper;
    private ObjectPropertyHelper objHelper;


    public ExistentialTreeMatrixModel(OWLObjectTree<OWLClass> tree, OWLModelManager mngr) {
        super(tree, mngr);
        fillerHelper = new FillerHelper(mngr);
        objHelper = new ObjectPropertyHelper(mngr);
    }


    public String getTreeColumnLabel() {
        return "Class";
    }


    public Object getMatrixValue(OWLClass rowObject, Object columnObject) {
        if (columnObject instanceof OWLObjectProperty){
            return new FillerModel(rowObject, (OWLObjectProperty) columnObject, fillerHelper);
        }
        else {
            return super.getMatrixValue(rowObject, columnObject);
        }
    }


    public List<OWLOntologyChange> setMatrixValue(OWLClass cls, Object prop, Object value) {
        List<OWLOntologyChange> changes = new ArrayList <OWLOntologyChange>();

        if (prop instanceof OWLObjectProperty){
            if (value.equals(NONE)) {
                changes = fillerHelper.setNamedFillers(Collections.EMPTY_SET, cls,
                                                       (OWLObjectProperty)prop, mngr.getActiveOntology());
            }
            else if (value instanceof Set) {
                changes = fillerHelper.setNamedFillers((Set<OWLDescription>) value, cls,
                                                       (OWLObjectProperty)prop, mngr.getActiveOntology());
            }
            else if (value instanceof OWLClass){
                changes = fillerHelper.setNamedFillers(Collections.singleton((OWLDescription)value), cls,
                                                       (OWLObjectProperty)prop, mngr.getActiveOntology());
            }
        }
        else{
            changes = super.setMatrixValue(cls, prop, value);
        }

        return changes;
    }


    public List<OWLOntologyChange> addMatrixValue(OWLClass cls, Object prop, Object value) {

        if (prop instanceof OWLObjectProperty){
            final OWLObjectProperty objProp = (OWLObjectProperty) prop;
            if (objHelper.isFunctional(objProp)){
                if (fillerHelper.getAssertedNamedFillers(cls, objProp).isEmpty()){
                    return performAdd(cls, objProp, value);
                }
            }
            else{
                return performAdd(cls, objProp, value);
            }
        }
        else{
            return super.addMatrixValue(cls, prop, value);
        }

        return Collections.emptyList();
    }

    private List<OWLOntologyChange> performAdd(OWLClass cls, OWLObjectProperty objProp, Object value) {

        Set<OWLDescription> newValues = null;

        if (value instanceof Set) {
            newValues = (Set<OWLDescription>) value;
        }
        else if (value instanceof OWLClass){
            newValues = Collections.singleton((OWLDescription)value);
        }

        if (newValues != null && !newValues.isEmpty()){
            return fillerHelper.addNamedFillers(newValues, cls, objProp, mngr.getActiveOntology());
        }

        return Collections.emptyList();
    }


    public boolean isSuitableCellValue(Object value, int row, int col) {
        return value instanceof OWLClass;
    }


    public boolean isSuitableColumnObject(Object columnObject) {
        return columnObject instanceof OWLObjectProperty || columnObject instanceof URI;
    }


    public boolean isValueRestricted(OWLClass cls, Object p) {
        return fillerHelper.fillersRestricted((OWLObjectProperty)p);
    }


    public Set getSuggestedFillers(OWLClass cls, Object p, int threshold) {
        return fillerHelper.getSuggestedFillers(cls, (OWLObjectProperty)p, threshold);
    }


    protected String renderColumnTitle(Object columnObject) {
        String title = super.renderColumnTitle(columnObject);
        if (columnObject instanceof OWLProperty){
            title += " some";
        }
        return title;
    }
}