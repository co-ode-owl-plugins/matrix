package org.coode.matrix.model.api;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreePath;

import org.coode.matrix.model.helper.AnnotatorHelper;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLOntologyChangeVisitorAdapter;

import uk.ac.manchester.cs.bhig.jtreetable.AbstractTreeTableModel;

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
public abstract class AbstractMatrixModel<R extends OWLObject> extends AbstractTreeTableModel<R>
        implements MatrixModel<R> {

    protected OWLModelManager mngr;

    protected AnnotatorHelper annotHelper;

    private OWLObjectTree<R> tree;

    private OWLOntologyChangeListener ontChangeListener = new OWLOntologyChangeListener(){

        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
            handleOntologyChanges(changes);
        }
    };


    public AbstractMatrixModel(OWLObjectTree<R> tree, OWLModelManager mngr) {
        super(tree);
        this.tree = tree;
        this.mngr = mngr;
        mngr.addOntologyChangeListener(ontChangeListener);
        this.annotHelper = new AnnotatorHelper(mngr);
    }


    public void dispose(){
        mngr.removeOntologyChangeListener(ontChangeListener);
    }


    @Override
    public final String getColumnName(int column) {
        final Object colObj = getColumnObjectAtModelIndex(column);
        return renderColumnTitle(colObj);
    }


    public Object getMatrixValue(R rowObj, Object columnObj) {
        if (rowObj instanceof OWLEntity && columnObj instanceof AbstractMatrixModel.AnnotationLangPair){
            AnnotationLangPair pair = (AnnotationLangPair) columnObj;
            String filter = pair.getFilterObject();
            if (filter != null){
                return annotHelper.getAnnotationValues((OWLEntity)rowObj, pair.getColumnObject(), filter);
            }
            else{
                return annotHelper.getAnnotationValues((OWLEntity)rowObj, pair.getColumnObject());
            }
        }
        return null;
    }


    public List<OWLOntologyChange> setMatrixValue(R rowObj, Object columnObj, Object value) {
        if (rowObj instanceof OWLEntity && value instanceof Set && columnObj instanceof AbstractMatrixModel.AnnotationLangPair){
            AnnotationLangPair pair = (AnnotationLangPair)columnObj;
            if (pair.getColumnObject() != null){
                if (pair.getFilterObject() != null){
                    return annotHelper.setAnnotationValues((OWLEntity)rowObj,
                                                           pair.getColumnObject(),
                                                           (Set<OWLAnnotationValue>)value,
                                                           mngr.getActiveOntology(),
                                                           pair.getFilterObject());
                }
                else{
                    return annotHelper.setAnnotationValues((OWLEntity)rowObj,
                                                           pair.getColumnObject(),
                                                           (Set<OWLAnnotationValue>)value,
                                                           mngr.getActiveOntology());
                }
            }
        }
        throw new InvalidParameterException("Cannot set value (" + value + ") of " + rowObj + " on  column " + columnObj);
    }


    public List<OWLOntologyChange> addMatrixValue(R rowObj, Object columnObj, Object value) {
        throw new InvalidParameterException("Cannot add value (" + value + ") for " + rowObj + " on  column " + columnObj);
    }


    @Override
    public final void setValueAt(Object value, R rowObject, int col) {
        Object columnObject = getColumnObjectAtModelIndex(col);
        if (columnObject != null) {
            List<OWLOntologyChange> changes = setMatrixValue(rowObject, columnObject, value);
            if (!changes.isEmpty()){
                mngr.applyChanges(changes);
            }
        }
    }


    @Override
    public final Object getValueAt(R rowObject, int col) {
        final Object columnObject = getColumnObjectAtModelIndex(col);
        return getMatrixValue(rowObject, columnObject);
    }


    // overload because the objects are not of type R - they are nodes
    @Override
    public R getNodeForRow(int row) {
        final TreePath path = tree.getPathForRow(row);
        if (path != null){
            return (R)((OWLObjectTreeNode) path.getLastPathComponent()).getOWLObject();
        }
        return null;
    }


    protected String renderColumnTitle(Object columnObject) {
        String label;
        if (columnObject instanceof AbstractMatrixModel.AnnotationLangPair){
            AnnotationLangPair pair = (AnnotationLangPair) columnObject;
            label = mngr.getRendering(pair.getColumnObject());
            if (pair.getFilterObject() != null){
                label += " (" + pair.getFilterObject() + ")";
            }
        }
        else if (columnObject instanceof OWLObject) {
            label = mngr.getRendering((OWLObject) columnObject);
        }
        else {
            label = columnObject.toString();
        }
        return label;
    }



    protected void handleOntologyChanges(List<? extends OWLOntologyChange> changes) {
        final Set<OWLEntity> entitiesReferencedByRemoveAxioms = new HashSet<OWLEntity>();
        OWLOntologyChangeVisitor visitor = new OWLOntologyChangeVisitorAdapter() {
            @Override
            public void visit(RemoveAxiom removeAxiom) {
                entitiesReferencedByRemoveAxioms.addAll(removeAxiom.getSignature());
            }
        };
        for (OWLOntologyChange change : changes){
            change.accept(visitor);
        }

        for (OWLEntity entity : entitiesReferencedByRemoveAxioms){
            if (getColumns().contains(entity)){
                if (!isEntityReferenced(entity)){
                    removeColumn(entity);
                }
            }
        }
    }


    private boolean isEntityReferenced(OWLEntity entity) {
        for (OWLOntology ont : mngr.getActiveOntologies()){
            if (ont.containsEntityInSignature(entity)){
                return true;
            }
        }
        return false;
    }

    public static class AnnotationLangPair extends AbstractColumnFilterPair<OWLAnnotationProperty, String> {

        public AnnotationLangPair(OWLAnnotationProperty object, String filter) {
            super(object, filter);
        }
    }
}
