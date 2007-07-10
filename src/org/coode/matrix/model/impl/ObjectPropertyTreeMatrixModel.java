package org.coode.matrix.model.impl;

import org.semanticweb.owl.model.*;
import org.coode.matrix.model.api.AbstractTreeMatrixModel;
import org.coode.matrix.model.helper.ObjectPropertyHelper;
import org.coode.matrix.ui.renderer.OWLObjectTreeTableCellRenderer;
import org.protege.editor.owl.model.OWLModelManager;

import java.net.URI;
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
 * Date: Jul 4, 2007<br><br>
 */
public class ObjectPropertyTreeMatrixModel extends AbstractTreeMatrixModel<OWLObjectProperty> {

    private ObjectPropertyHelper objPropHelper;

    public ObjectPropertyTreeMatrixModel(OWLObjectTreeTableCellRenderer<OWLObjectProperty> tree, OWLModelManager mngr) {
        super(tree, mngr);

        objPropHelper = new ObjectPropertyHelper(mngr);

        for (String col : ObjectPropertyHelper.names) {
            addColumn(col, -1);
        }
    }

    protected String getTreeColumnLabel() {
        return "Object Property";
    }

    public Object getMatrixValue(OWLObjectProperty prop, Object feature) {
        if (feature instanceof URI) {
            return super.getMatrixValue(prop, feature);
        }
        else if (feature instanceof String){
            final String name = (String) feature;
            if (objPropHelper.isCharacteristic(name)){
                return objPropHelper.getPropertyCharacteristic(prop, name);
            }
            else if (objPropHelper.toIndex(name) == ObjectPropertyHelper.DOMAIN){
                return objPropHelper.getDomains(prop);
            }
            else if (objPropHelper.toIndex(name) == ObjectPropertyHelper.RANGE){
                return objPropHelper.getRanges(prop);
            }
            else if (objPropHelper.toIndex(name) == ObjectPropertyHelper.INVERSE){
                return objPropHelper.getInverses(prop);
            }
        }
        return null;
    }

    public List<OWLOntologyChange> setMatrixValue(OWLObjectProperty prop, Object columnObj, Object value) {
        if (columnObj instanceof String){
            if (objPropHelper.isCharacteristic(columnObj)){
                return objPropHelper.setPropertyCharacteristic((Boolean) value, prop,
                                                               (String) columnObj,
                                                               mngr.getActiveOntology());
            }
            else if (objPropHelper.toIndex((String) columnObj) == ObjectPropertyHelper.INVERSE){
                return objPropHelper.setInverses(prop, (Set<OWLObjectProperty>)value, mngr.getActiveOntology());
            }
            else if (objPropHelper.toIndex((String) columnObj) == ObjectPropertyHelper.DOMAIN){
                return objPropHelper.setDomains(prop, (Set<OWLDescription>)value, mngr.getActiveOntology());
            }
            else if (objPropHelper.toIndex((String) columnObj) == ObjectPropertyHelper.RANGE){
                return objPropHelper.setRanges(prop, (Set<OWLDescription>)value, mngr.getActiveOntology());
            }
        }
        else{
            return super.setMatrixValue(prop, columnObj, (Set<OWLObject>)value);
        }
        return Collections.EMPTY_LIST;
    }

    public boolean isSuitableCellValue(Object value, int row, int col) {
        final Object colObj = getObjectForColumn(col);
        if (colObj instanceof String){
            switch (objPropHelper.toIndex((String)colObj)){
                case ObjectPropertyHelper.INVERSE:
                    return value instanceof OWLObjectProperty;
                case ObjectPropertyHelper.DOMAIN: // fallthrough
                case ObjectPropertyHelper.RANGE:
                    return value instanceof OWLClass;
            }
        }
        return false;
    }

    public boolean isSuitableColumnObject(Object columnObject) {
        return columnObject instanceof URI;
    }

    public boolean isValueRestricted(OWLObjectProperty rowObject, Object columnObject) {
        return false;
    }

    public Set getSuggestedFillers(OWLObjectProperty rowObject, Object columnObject, int threshold) {
        return Collections.EMPTY_SET;
    }

    public Class getColumnClass(int column) {
        final Object colObj = getObjectForColumn(column);
        if (objPropHelper.isCharacteristic(colObj)){
            return Boolean.class;
        }
        return super.getColumnClass(column);
    }
}
