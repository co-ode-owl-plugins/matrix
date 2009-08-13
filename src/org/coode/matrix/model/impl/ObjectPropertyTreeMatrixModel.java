package org.coode.matrix.model.impl;

import org.coode.matrix.model.api.AbstractMatrixModel;
import org.coode.matrix.model.helper.PropertyHelper;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owlapi.model.*;

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
 * Date: Jul 4, 2007<br><br>
 */
public class ObjectPropertyTreeMatrixModel extends AbstractMatrixModel<OWLObjectProperty> {

    private PropertyHelper objPropHelper;

    public ObjectPropertyTreeMatrixModel(OWLObjectTree<OWLObjectProperty> tree, OWLModelManager mngr) {
        super(tree, mngr);

        objPropHelper = new PropertyHelper(mngr);

        for (PropertyHelper.OWLPropertyCharacteristic c : PropertyHelper.OWLPropertyCharacteristic.values()){
            if (c.isObjectPropertyCharacteristic()){
                addColumn(c);
            }
        }

        for (PropertyHelper.OWLPropertyFeature f : PropertyHelper.OWLPropertyFeature.values()){
            if (f.isObjectPropertyFeature()){
                addColumn(f);
            }
        }
    }

    public String getTreeColumnLabel() {
        return "Object Property";
    }

    public Object getMatrixValue(OWLObjectProperty prop, Object colObj) {
        if (colObj instanceof PropertyHelper.OWLPropertyCharacteristic){
            return objPropHelper.getPropertyCharacteristic(prop, (PropertyHelper.OWLPropertyCharacteristic)colObj);
        }
        else if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case DOMAIN: return objPropHelper.getDomains(prop);
                case RANGE: return objPropHelper.getRanges(prop);
                case INVERSE: return objPropHelper.getInverses(prop);
            }
            return null;
        }
        return super.getMatrixValue(prop, colObj);
    }

    public List<OWLOntologyChange> setMatrixValue(OWLObjectProperty prop, Object colObj, Object value) {
        if (colObj instanceof PropertyHelper.OWLPropertyCharacteristic){
            return objPropHelper.setPropertyCharacteristic((Boolean) value, prop,
                                                           (PropertyHelper.OWLPropertyCharacteristic) colObj,
                                                           mngr.getActiveOntology());
        }
        else if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case DOMAIN: return objPropHelper.setDomains(prop, (Set<OWLClassExpression>)value, mngr.getActiveOntology());
                case RANGE: return objPropHelper.setRanges(prop, (Set<OWLPropertyRange>)value, mngr.getActiveOntology());
                case INVERSE: return objPropHelper.setInverses(prop, (Set<OWLObjectProperty>)value, mngr.getActiveOntology());
            }
            return Collections.emptyList();
        }
        return super.setMatrixValue(prop, colObj, value);
    }


    public List<OWLOntologyChange> addMatrixValue(OWLObjectProperty prop, Object colObj, Object value) {
        if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case DOMAIN:
                    Set<OWLClassExpression> domains = objPropHelper.getDomains(prop);
                    domains.add((OWLClassExpression)value);
                    return objPropHelper.setDomains(prop, domains, mngr.getActiveOntology());

                case RANGE:
                    Set<OWLPropertyRange> ranges = objPropHelper.getRanges(prop);
                    ranges.add((OWLClassExpression)value);
                    return objPropHelper.setRanges(prop, ranges, mngr.getActiveOntology());

                case INVERSE:
                    Set<OWLObjectProperty> inverses = objPropHelper.getInverses(prop);
                    inverses.add((OWLObjectProperty)value);
                    return objPropHelper.setInverses(prop, inverses, mngr.getActiveOntology());
            }
            return Collections.emptyList();
        }
        return super.addMatrixValue(prop, colObj, value);
    }


    public boolean isSuitableCellValue(Object value, int row, int col) {
        final Object colObj = getColumnObjectAtModelIndex(col);
        if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case INVERSE: return value instanceof OWLObjectProperty;
                case DOMAIN: // fallthrough
                case RANGE: return value instanceof OWLClass;
            }
        }
        return false;
    }


    public Object getSuitableColumnObject(Object columnObject) {
        if (columnObject instanceof URI){
            return columnObject;
        }
        return null;
    }


    public boolean isValueRestricted(OWLObjectProperty rowObject, Object columnObject) {
        return false;
    }

    public Set getSuggestedFillers(OWLObjectProperty rowObject, Object columnObject, int threshold) {
        return Collections.EMPTY_SET;
    }

    public Class getColumnClass(int column) {
        final Object colObj = getColumnObjectAtModelIndex(column);
        if (colObj instanceof PropertyHelper.OWLPropertyCharacteristic){
            return Boolean.class;
        }
        return super.getColumnClass(column);
    }
}
