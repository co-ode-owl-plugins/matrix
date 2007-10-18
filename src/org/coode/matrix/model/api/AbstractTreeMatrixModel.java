package org.coode.matrix.model.api;

import org.coode.jtreetable.TreeTableModelAdapter;
import org.coode.matrix.model.helper.AnnotatorHelper;
import org.coode.matrix.ui.renderer.OWLObjectTreeTableCellRenderer;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntologyChange;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
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
public abstract class AbstractTreeMatrixModel<R extends OWLObject>
        extends TreeTableModelAdapter implements TreeMatrixModel<R> {

    private MatrixColumnModel columns;

    protected OWLModelManager mngr;

    protected AnnotatorHelper annotHelper;

    private ContentsChangedListener listener = new ContentsChangedListener() {

        public void valuesAdded(Collection values) {
            fireTableStructureChanged();
        }

        public void valuesRemoved(Collection values) {
            fireTableStructureChanged();
        }
    };

    public AbstractTreeMatrixModel(OWLObjectTreeTableCellRenderer treeRenderer, OWLModelManager mngr) {
        super(new OWLTreeTableModel<R>(treeRenderer.getHierarchyProvider()), treeRenderer);
        this.mngr = mngr;

        annotHelper = new AnnotatorHelper(mngr);

        columns = new MatrixColumnModel();
        columns.addContentsChangedListener(listener);
    }

    protected abstract String getTreeColumnLabel();

    public final boolean addColumn(Object column, int index) {
        if (index == -1){
            return columns.add(column);
        }
        else{
            return columns.add(column, index);
        }
    }

    public void removeColumn(Object obj) {
        columns.remove(obj);
    }

    public List getColumnObjects() {
        return new ArrayList(columns.values);
    }

    public final R getOWLObjectForRow(int row) {
        return ((OWLObjectTreeNode<R>) nodeForRow(row)).getOWLObject();
    }

    public final Object getObjectForColumn(int column) {
        if (column > 0) {
            return columns.get(column - 1);
        }
        return null;
    }

    public final int getColumnCount() {
        return columns.size() + 1;
    }

    public final String getColumnName(int column) {
        if (column > 0) {
            Object columnObject = columns.get(column - 1);
            return renderColumnTitle(columnObject);
        }
        else{
            return getTreeColumnLabel();
        }
    }

    protected String renderColumnTitle(Object columnObject) {
        if (columnObject instanceof URI){
            return ((URI)columnObject).getFragment();
        }
        if (columnObject instanceof OWLObject) {
            return mngr.getOWLObjectRenderer().render((OWLObject) columnObject, mngr.getOWLEntityRenderer());
        }
        else {
            return columnObject.toString();
        }
    }

    public Object getMatrixValue(R rowObj, Object columnObj) {
        if (rowObj instanceof OWLEntity && columnObj instanceof URI){
            return annotHelper.getAnnotationValues((OWLEntity)rowObj, (URI)columnObj);
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
        throw new InvalidParameterException("Cannot set annotation to value " + value);
    }

    public final Object getValueAt(int row, int column) {
        if (column > 0) {
            Object xObj = columns.get(column - 1);
            return getMatrixValue(getOWLObjectForRow(row), xObj);
        }
        else {
            return getOWLObjectForRow(row);
        }
    }

    public final void setValueAt(Object value, int row, int column) {
        Object columnObject = getObjectForColumn(column);
        if (columnObject != null) {
            List<OWLOntologyChange> changes = setMatrixValue(getOWLObjectForRow(row), columnObject, value);
            if (!changes.isEmpty()){
                mngr.applyChanges(changes);
            }
        }
    }

    public final TreeTableModelAdapter getTreeTableModelAdapter() {
        return this;
    }

    protected final AnnotatorHelper getAnnotationHelper(){
        return annotHelper;
    }
}
