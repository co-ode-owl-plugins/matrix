package org.coode.matrix.model.impl;

import java.net.URI;
import java.util.Collection;
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
import org.coode.matrix.model.helper.PropertyHelper;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 4, 2007<br><br>
 */
public class DataPropertyTreeMatrixModel extends AbstractMatrixModel<OWLDataProperty> {

    private PropertyHelper helper;

    public DataPropertyTreeMatrixModel(OWLObjectTree<OWLDataProperty> tree, OWLModelManager mngr) {
        super(tree, mngr);

        helper = new PropertyHelper(mngr);

        for (PropertyHelper.OWLPropertyCharacteristic c : PropertyHelper.OWLPropertyCharacteristic.values()){
            if (c.isDataPropertyCharacteristic()){
                addColumn(c);
            }
        }

        for (PropertyHelper.OWLPropertyFeature f : PropertyHelper.OWLPropertyFeature.values()){
            if (f.isDataPropertyFeature()){
                addColumn(f);
            }
        }
    }


    public String getTreeColumnLabel() {
        return "Data Property";
    }


    @Override
    public Object getMatrixValue(OWLDataProperty prop, Object colObj) {
        if (colObj instanceof PropertyHelper.OWLPropertyCharacteristic){
            return helper.getPropertyCharacteristic(prop, (PropertyHelper.OWLPropertyCharacteristic)colObj);
        }
        else if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case DOMAIN: return helper.getDomains(prop);
                case RANGE: return helper.getRanges(prop);
            }
            return null;
        }
        return super.getMatrixValue(prop, colObj);
    }


    @Override
    public List<OWLOntologyChange> setMatrixValue(OWLDataProperty prop, Object colObj, Object value) {
        if (colObj instanceof PropertyHelper.OWLPropertyCharacteristic){
            return helper.setPropertyCharacteristic((Boolean) value, prop,
                                                           (PropertyHelper.OWLPropertyCharacteristic) colObj,
                                                           mngr.getActiveOntology());
        }
        else if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case DOMAIN: return helper.setDomains(prop, (Set<OWLClassExpression>)value, mngr.getActiveOntology());
                case RANGE: return helper.setRanges(prop, (Set<OWLDataRange>)value, mngr.getActiveOntology());
            }
            return Collections.emptyList();
        }
        return super.setMatrixValue(prop, colObj, value);
    }


    @Override
    public List<OWLOntologyChange> addMatrixValue(OWLDataProperty prop, Object colObj, Object value) {
        if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case DOMAIN:
                    Collection<OWLClassExpression> domains = helper.getDomains(prop);
                    domains.add((OWLClassExpression)value);
                    return helper.setDomains(prop, domains, mngr.getActiveOntology());

                case RANGE:
                    Collection<OWLDataRange> ranges = helper.getRanges(prop);
                    ranges.add((OWLDataRange)value);
                    return helper.setRanges(prop, ranges, mngr.getActiveOntology());

            }
            return Collections.emptyList();
        }
        return super.addMatrixValue(prop, colObj, value);
    }


    public boolean isSuitableCellValue(Object value, int row, int col) {
        final Object colObj = getColumnObjectAtModelIndex(col);
        if (colObj instanceof PropertyHelper.OWLPropertyFeature){
            switch((PropertyHelper.OWLPropertyFeature)colObj){
                case DOMAIN: return value instanceof OWLClass;
                case RANGE: return value instanceof OWLDataRange;
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


    public boolean isValueRestricted(OWLDataProperty rowObject, Object columnObject) {
        return false;
    }

    public Set getSuggestedFillers(OWLDataProperty rowObject, Object columnObject, int threshold) {
        return Collections.EMPTY_SET;
    }

    @Override
    public Class getColumnClass(int column) {
        final Object colObj = getColumnObjectAtModelIndex(column);
        if (colObj instanceof PropertyHelper.OWLPropertyCharacteristic){
            return Boolean.class;
        }
        return super.getColumnClass(column);
    }
}