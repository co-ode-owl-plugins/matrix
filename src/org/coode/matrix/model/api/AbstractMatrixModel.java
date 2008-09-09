package org.coode.matrix.model.api;

import org.coode.matrix.model.helper.AnnotatorHelper;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.semanticweb.owl.model.*;
import uk.ac.manchester.cs.bhig.jtreetable.AbstractTreeTableModel;

import java.net.URI;
import java.security.InvalidParameterException;
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
public abstract class AbstractMatrixModel<R extends OWLObject> extends AbstractTreeTableModel<R>
        implements MatrixModel<R> {

    protected OWLModelManager mngr;

    protected AnnotatorHelper annotHelper;

    private Map filterMap = new HashMap();

    private OWLObjectTree<R> tree;

    private OWLOntologyChangeListener ontChangeListener = new OWLOntologyChangeListener(){

        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
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


    public final String getColumnName(int column) {
        return renderColumnTitle(getColumnObjectAtModelIndex(column));
    }


    public Object getMatrixValue(R rowObj, Object columnObj) {
        if (rowObj instanceof OWLEntity && columnObj instanceof URI){
            int col = getModelIndexOfColumn(columnObj);
            Object filter = getFilterForColumn(col);
            if (filter != null && filter instanceof String){
                return annotHelper.getAnnotationValues((OWLEntity)rowObj, (URI)columnObj, (String)filter);
            }
            else{
                return annotHelper.getAnnotationValues((OWLEntity)rowObj, (URI)columnObj);
            }
        }
        else{
            return null;
        }
    }


    public List<OWLOntologyChange> setMatrixValue(R rowObj, Object columnObj, Object value) {
        if (columnObj instanceof URI && rowObj instanceof OWLEntity && value instanceof Set){
            return annotHelper.setAnnotationValues((OWLEntity)rowObj,
                                                   (URI)columnObj,
                                                   (Set<OWLObject>)value,
                                                   mngr.getActiveOntology());
        }
        throw new InvalidParameterException("Cannot set value (" + value + ") of " + rowObj + " on  column " + columnObj);
    }


    public List<OWLOntologyChange> addMatrixValue(R rowObj, Object columnObj, Object value) {
        throw new InvalidParameterException("Cannot add value (" + value + ") for " + rowObj + " on  column " + columnObj);
    }


    public final void setValueAt(Object value, R rowObject, int col) {
        Object columnObject = getColumnObjectAtModelIndex(col);
        if (columnObject != null) {
            List<OWLOntologyChange> changes = setMatrixValue(rowObject, columnObject, value);
            if (!changes.isEmpty()){
                mngr.applyChanges(changes);
            }
        }
    }


    public final Object getValueAt(R rowObject, int col) {
        final Object columnObject = getColumnObjectAtModelIndex(col);
        return getMatrixValue(rowObject, columnObject);
    }


    // overload because the objects are not of type R - they are nodes
    public R getNodeForRow(int row) {
        return (R)((OWLObjectTreeNode)tree.getPathForRow(row).getLastPathComponent()).getOWLObject();
    }


    protected String renderColumnTitle(Object columnObject) {
        String label;
        if (columnObject instanceof URI){
            label = ((URI)columnObject).getFragment();
        }
        else if (columnObject instanceof OWLObject) {
            label = mngr.getRendering((OWLObject) columnObject);
        }
        else {
            label = columnObject.toString();
        }

        Object filter = getFilterForColumn(columnObject);
        if (filter != null){
            label += "(" + filter + ")";
        }
        return label;
    }



    private void handleOntologyChanges(List<? extends OWLOntologyChange> changes) {
        final Set<OWLEntity> entitiesReferencedByRemoveAxioms = new HashSet<OWLEntity>();
        OWLOntologyChangeVisitor visitor = new OWLOntologyChangeVisitor() {

            public void visit(AddAxiom addAxiom) {
            }


            public void visit(RemoveAxiom removeAxiom) {
                entitiesReferencedByRemoveAxioms.addAll(removeAxiom.getEntities());
            }


            public void visit(SetOntologyURI setOntologyURI) {
                //To change body of implemented methods use File | Settings | File Templates.
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
            if (ont.containsEntityReference(entity)){
                return true;
            }
        }
        return false;
    }

////////////////// Filters


    public void setFilterForColumn(int col, Object filter) {
        filterMap.put(getColumnObjectAtModelIndex(col), filter);
    }


    public Object getFilterForColumn(int col) {
        return getFilterForColumn(getColumnObjectAtModelIndex(col));
    }


    public Object getFilterForColumn(Object obj) {
        return filterMap.get(obj);
    }
}
