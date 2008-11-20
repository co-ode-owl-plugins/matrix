package org.coode.matrix.model.impl;

import org.coode.matrix.model.api.AbstractColumnFilterPair;
import org.coode.matrix.model.api.AbstractMatrixModel;
import org.coode.matrix.model.helper.FillerHelper;
import org.coode.matrix.model.helper.PropertyHelper;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owl.model.*;

import java.net.URI;
import java.util.*;
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
public class RestrictionTreeMatrixModel extends AbstractMatrixModel<OWLClass> {

    public static final String NONE = "none";

    private FillerHelper fillerHelper;
    private PropertyHelper objHelper;


    public RestrictionTreeMatrixModel(OWLObjectTree<OWLClass> tree, OWLObjectHierarchyProvider<OWLClass> hp, OWLModelManager mngr) {
        super(tree, mngr);
        fillerHelper = new FillerHelper(mngr, hp);
        objHelper = new PropertyHelper(mngr);
    }


    public String getTreeColumnLabel() {
        return "Class";
    }


    public Object getMatrixValue(OWLClass rowObject, Object columnObject) {
        if (columnObject instanceof PropertyRestrictionPair){
            return createFillerModel(rowObject, (PropertyRestrictionPair)columnObject, fillerHelper);
        }
        else {
            return super.getMatrixValue(rowObject, columnObject);
        }
    }

    private <P extends OWLPropertyExpression, R extends OWLPropertyRange> FillerModel<P, R> createFillerModel(OWLClass cls,
                                                                                                                    PropertyRestrictionPair<P, R> pair,
                                                                                                                    FillerHelper helper){
        return new FillerModel<P, R>(cls, pair, helper);
    }


    public List<OWLOntologyChange> setMatrixValue(OWLClass cls, Object col, Object value) {
        List<OWLOntologyChange> changes = new ArrayList <OWLOntologyChange>();

        if (col instanceof PropertyRestrictionPair){
            final PropertyRestrictionPair<OWLPropertyExpression, OWLPropertyRange> pair = (PropertyRestrictionPair)col;
            OWLPropertyExpression prop = pair.getColumnObject();

            Set<OWLPropertyRange> fillers = getFillerSet(value);
            if (fillers != null){
                changes = fillerHelper.setFillers(cls,
                                                  prop,
                                                  fillers,
                                                  pair.getFilterObject(),
                                                  mngr.getActiveOntology());
            }
        }
        else{
            changes = super.setMatrixValue(cls, col, value);
        }

        return changes;
    }


    public List<OWLOntologyChange> addMatrixValue(OWLClass cls, Object colObj, Object value) {

        if (colObj instanceof PropertyRestrictionPair){
            final PropertyRestrictionPair<OWLPropertyExpression, OWLPropertyRange> pair = (PropertyRestrictionPair) colObj;
            OWLPropertyExpression objProp = pair.getColumnObject();
            Set<OWLPropertyRange> values = getFillerSet(value);
            if (objHelper.isFunctional(objProp)){
                if (fillerHelper.getAssertedFillers(cls, objProp, pair.getFilterObject()).isEmpty()){
                    return performAdd(cls, objProp, values, pair.getFilterObject());
                }
            }
            else{
                return performAdd(cls, objProp, values, pair.getFilterObject());
            }
        }
        else{
            return super.addMatrixValue(cls, colObj, value);
        }

        return Collections.emptyList();
    }


    private Set<OWLPropertyRange> getFillerSet(Object value) {
        Set<OWLPropertyRange> fillers = null;
        if (value.equals(NONE)) {
            fillers = Collections.EMPTY_SET;
        }
        else if (value instanceof Set) {
            fillers = (Set<OWLPropertyRange>)value;
        }
        else if (value instanceof OWLPropertyRange){
            fillers = Collections.singleton((OWLPropertyRange)value);
        }
        return fillers;
    }


    protected String renderColumnTitle(Object columnObject) {
        if (columnObject instanceof PropertyRestrictionPair){
            PropertyRestrictionPair<OWLPropertyExpression, OWLPropertyRange> pair = (PropertyRestrictionPair)columnObject;
            return mngr.getRendering(pair.getColumnObject()) + " (" + pair.render(pair.getFilterObject()) + ")";
        }
        return super.renderColumnTitle(columnObject);
    }


    private <P extends OWLPropertyExpression, R extends OWLPropertyRange> List<OWLOntologyChange> performAdd(OWLClass cls,
                                                                                                             P objProp,
                                                                                                             Set<R> values,
                                                                                                             Class<? extends OWLQuantifiedRestriction<P, R>> type) {
        if (values != null && !values.isEmpty()){
            return fillerHelper.addNamedFillers(cls, objProp, type, values, mngr.getActiveOntology());
        }
        return Collections.emptyList();
    }


    public boolean isSuitableCellValue(Object value, int row, int col) {
        Object colObj = getColumnObjectAtModelIndex(col);
        if (colObj instanceof PropertyRestrictionPair){
            if (((PropertyRestrictionPair)colObj).getColumnObject() instanceof OWLObjectProperty){
                return value instanceof OWLClass;
            }
            else{
                return value instanceof OWLDataRange;
            }
        }
        return false;
    }


    public Object getSuitableColumnObject(Object columnObject) {
        if (columnObject instanceof OWLObjectProperty){
            return new PropertyRestrictionPair<OWLObjectPropertyExpression, OWLDescription>((OWLObjectProperty)columnObject, OWLObjectSomeRestriction.class);
        }
        else if (columnObject instanceof OWLDataProperty){
            return new PropertyRestrictionPair<OWLDataPropertyExpression, OWLDataRange>((OWLDataProperty)columnObject, OWLDataSomeRestriction.class);
        }
        else if (columnObject instanceof URI){
            return columnObject;
        }
        return null;
    }


    public boolean isValueRestricted(OWLClass cls, Object p) {
        return false;
//        return fillerHelper.fillersRestricted((OWLObjectProperty)p);
    }


    public Set getSuggestedFillers(OWLClass cls, Object p, int threshold) {
        return null;
//        return fillerHelper.getSuggestedFillers(cls, (OWLObjectProperty)p, threshold);
    }


    public static class PropertyRestrictionPair<P extends OWLPropertyExpression, R extends OWLPropertyRange> extends AbstractColumnFilterPair<P, Class<? extends OWLQuantifiedRestriction<P, R>>> {

        private static Map<Class, String> renderMap = new HashMap<Class, String>();
        static {
            renderMap.put(OWLObjectSomeRestriction.class, "some");
            renderMap.put(OWLObjectAllRestriction.class, "only");
            renderMap.put(OWLDataSomeRestriction.class, "some");
            renderMap.put(OWLDataAllRestriction.class, "only");
        }

        public PropertyRestrictionPair(P object, Class<? extends OWLQuantifiedRestriction<P, R>> filter) {
            super(object, filter);
        }


        public static String render(Class restrictionClass) {
            return renderMap.get(restrictionClass);
        }
    }
}