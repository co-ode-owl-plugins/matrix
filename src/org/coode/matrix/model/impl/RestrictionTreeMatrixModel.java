package org.coode.matrix.model.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coode.matrix.model.api.AbstractColumnFilterPair;
import org.coode.matrix.model.api.AbstractMatrixModel;
import org.coode.matrix.model.helper.FillerHelper;
import org.coode.matrix.model.helper.PropertyHelper;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;

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

    private <R extends OWLPropertyRange, P extends OWLPropertyExpression<R,P>> FillerModel<R, P> createFillerModel(OWLClass cls,
                                                                                                                   PropertyRestrictionPair<R, P> pair,
                                                                                                                    FillerHelper helper){
        return new FillerModel<R, P>(cls, pair, helper);
    }


    public List<OWLOntologyChange> setMatrixValue(OWLClass cls, Object col, Object value) {
        List<OWLOntologyChange> changes = new ArrayList <OWLOntologyChange>();

        if (col instanceof PropertyRestrictionPair){
            final PropertyRestrictionPair pair = (PropertyRestrictionPair)col;
            OWLPropertyExpression prop = (OWLPropertyExpression) pair.getColumnObject();

            Set<OWLPropertyRange> fillers = getFillerSet(value);
            if (fillers != null){
                changes = fillerHelper.setFillers(cls,
                                                  prop,
                                                  fillers,
                                                  (Class) pair.getFilterObject(),
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
            final PropertyRestrictionPair pair = (PropertyRestrictionPair) colObj;
            OWLPropertyExpression objProp = (OWLPropertyExpression) pair.getColumnObject();
            Set<? extends OWLPropertyRange> values = getFillerSet(value);
            if (objHelper.isFunctional(objProp)){
                if (fillerHelper.getAssertedFillers(cls, objProp, (Class) pair.getFilterObject()).isEmpty()){
                    return performAdd(cls, objProp, values, (Class) pair.getFilterObject());
                }
            }
            else{
                return performAdd(cls, objProp, values, (Class) pair.getFilterObject());
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
            PropertyRestrictionPair pair = (PropertyRestrictionPair)columnObject;
            return mngr.getRendering((OWLObject) pair.getColumnObject()) + " (" + pair.render((Class) pair.getFilterObject()) + ")";
        }
        return super.renderColumnTitle(columnObject);
    }


    private <R extends OWLPropertyRange, P extends OWLPropertyExpression<R,P>> List<OWLOntologyChange> performAdd(OWLClass cls,
                                                                                                                  P objProp,
                                                                                                                  Set<R> values,
                                                                                                                  Class<? extends OWLQuantifiedRestriction<R, P, R>> type) {
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
            return new PropertyRestrictionPair<OWLClassExpression, OWLObjectPropertyExpression>((OWLObjectProperty)columnObject, OWLObjectSomeValuesFrom.class);
        }
        else if (columnObject instanceof OWLDataProperty){
            return new PropertyRestrictionPair<OWLDataRange, OWLDataPropertyExpression>((OWLDataProperty)columnObject, OWLDataSomeValuesFrom.class);
        }
        else if (columnObject instanceof URI){
            return columnObject;
        }
        return null;
    }


    public boolean isValueRestricted(OWLClass cls, Object p) {
        return false;
    }


    public Set getSuggestedFillers(OWLClass cls, Object p, int threshold) {
        return null;
    }


    public static class PropertyRestrictionPair<R extends OWLPropertyRange, P extends OWLPropertyExpression<R,P>> extends AbstractColumnFilterPair<P, Class<? extends OWLQuantifiedRestriction<R, P, R>>> {

        private static Map<Class, String> renderMap = new HashMap<Class, String>();
        static {
            renderMap.put(OWLObjectSomeValuesFrom.class, "some");
            renderMap.put(OWLObjectAllValuesFrom.class, "only");
            renderMap.put(OWLDataSomeValuesFrom.class, "some");
            renderMap.put(OWLDataAllValuesFrom.class, "only");
        }

        public PropertyRestrictionPair(P object, Class<? extends OWLQuantifiedRestriction<R, P, R>> filter) {
            super(object, filter);
        }


        public static String render(Class restrictionClass) {
            return renderMap.get(restrictionClass);
        }
    }
}