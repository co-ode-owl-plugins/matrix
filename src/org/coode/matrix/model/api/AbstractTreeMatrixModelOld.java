/*
* Copyright (C) 2007, University of Manchester
*/
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

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 3, 2007<br><br>
 */
public abstract class AbstractTreeMatrixModelOld<R extends OWLObject> extends TreeTableModelAdapter implements TreeMatrixModel<R> {

    private List columns = new ArrayList();

    private List filters = new ArrayList();

    protected OWLModelManager mngr;

    protected AnnotatorHelper annotHelper;
  

    public AbstractTreeMatrixModelOld(OWLObjectTreeTableCellRenderer treeRenderer, OWLModelManager mngr) {
        super(new OWLTreeTableModel<R>(treeRenderer.getHierarchyProvider()), treeRenderer);

        this.mngr = mngr;
        this.annotHelper = new AnnotatorHelper(mngr);
    }


    protected abstract String getTreeColumnLabel();


    public boolean addColumn(Object obj) {
        return add(obj);
    }


    public final boolean addColumn(Object column, int index) {
        if (index < 0){
            return add(column);
        }
        else{
            return add(column, index);
        }
    }


    public final boolean removeColumn(Object obj) {
        return remove(obj);
    }


    public final R getRowObject(int row) {
        return ((OWLObjectTreeNode<R>) nodeForRow(row)).getOWLObject();
    }

    /**
     *
     * @param column indexed by physical columns
     * @return
     */
    public final Object getColumnObject(int column) {
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


    public Object getMatrixValue(R rowObj, Object columnObj) {
        if (rowObj instanceof OWLEntity && columnObj instanceof URI){
            int col = columns.indexOf(columnObj);
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


    public final Object getValueAt(int row, int column) {
        if (column > 0) {
            return getMatrixValue(getRowObject(row), getColumnObject(column));
        }
        else {
            return getRowObject(row);
        }
    }


    public final void setValueAt(Object value, int row, int column) {
        Object columnObject = getColumnObject(column);
        if (columnObject != null) {
            List<OWLOntologyChange> changes = setMatrixValue(getRowObject(row), columnObject, value);
            if (!changes.isEmpty()){
                mngr.applyChanges(changes);
            }
        }
    }


    public final TreeTableModelAdapter getTreeTableModelAdapter() {
        return this;
    }


    protected String renderColumnTitle(Object columnObject) {
        String label;
        if (columnObject instanceof URI){
            label = ((URI)columnObject).getFragment();
        }
        else if (columnObject instanceof OWLObject) {
            label = mngr.getOWLObjectRenderer().render((OWLObject) columnObject, mngr.getOWLEntityRenderer());
        }
        else {
            label = columnObject.toString();
        }

        Object filter = getFilterForColumn(columns.indexOf(columnObject));
        if (filter != null){
            label += "(" + filter + ")";
        }
        return label;
    }


///////////////////////////////

    public boolean add(Object newValue) {
        boolean result = false;
        if (!columns.contains(newValue)) {
            result = columns.add(newValue);
            if (result) {
                filters.add(null);
            }
        }
        return result;
    }

    public boolean add(Object newValue, int position) {
        boolean result = false;
        if (!columns.contains(newValue)) {
            if (columns.size() < position) {
                columns.add(newValue);
                filters.add(null);
            }
            else {
                columns.add(position, newValue);
                filters.add(position, null);
            }
            result = true;
        }
        return result;
    }

    /**
     * Will only add unique columns to the axis
     *
     * @param newValues
     * @return true if any columns added
     */
    public boolean add(Collection newValues) {
        Collection valuesAdded = new ArrayList();
        for (Object value : newValues) {
            if (columns.add(value)) {
                filters.add(null);
                valuesAdded.add(value);
            }
        }
        return (valuesAdded.size() > 0);
    }

    public boolean remove(Object value) {
        boolean result = false;
        int index = columns.indexOf(value);
        if (index >= 0){
            result = columns.remove(value);
            if (result) {
                filters.remove(index);
            }
        }
        return result;
    }


    public boolean remove(Collection oldValues) {
        boolean result = false;

        for (Object value : oldValues){
            int index = columns.indexOf(value);
            if (index >= 0){
                result = columns.remove(value);
                if (result) {
                    filters.remove(index);
                    result = true;
                }
            }
        }
        return result;
    }



    public void setFilterForColumn(int col, Object filter) {
        if (col >= 0 && col <= columns.size()){
            filters.set(col, filter);
        }
    }


    public Object getFilterForColumn(int col) {
        if (col >= 0 && col <= columns.size()){
            return filters.get(col);
        }
        return null;
    }
}
