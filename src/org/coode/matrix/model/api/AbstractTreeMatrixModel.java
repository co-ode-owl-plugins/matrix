package org.coode.matrix.model.api;

import org.coode.jtreetable.TreeTableModelAdapter;
import org.coode.matrix.model.helper.AnnotatorHelper;
import org.coode.matrix.ui.renderer.OWLObjectTreeTableCellRenderer;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntologyChange;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public abstract class  AbstractTreeMatrixModel<R extends OWLObject> extends TreeTableModelAdapter implements TreeMatrixModel<R> {

    protected OWLModelManager mngr;

    protected AnnotatorHelper annotHelper;

    // lets stop being silly and use the table column model - then we don't have to keep in sync
    private TableColumnModel colModel;

    private Map filterMap = new HashMap();


    public AbstractTreeMatrixModel(OWLObjectTreeTableCellRenderer treeRenderer, OWLModelManager mngr) {
        super(new OWLTreeTableModel<R>(treeRenderer.getHierarchyProvider()), treeRenderer);

        this.colModel = createColumnModel();
        this.mngr = mngr;
        this.annotHelper = new AnnotatorHelper(mngr);
    }

    // create the model with single 
    private TableColumnModel createColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(new TableColumn());
        return model;
    }


    protected abstract String getTreeColumnLabel();


    public boolean addColumn(Object obj) {
        return addColumn(obj, colModel.getColumnCount());
    }


    public final boolean addColumn(Object newValue, int index) {
        if (!contains(newValue)) {
            TableColumn tc = new TableColumn(index);
            tc.setHeaderValue(newValue);
            tc.setModelIndex(index);
            colModel.addColumn(tc);
            return true;
        }
        return false;
    }


    public final boolean removeColumn(Object obj) {
        for (int i=0; i< colModel.getColumnCount(); i++){
            TableColumn tc = colModel.getColumn(i);
            if (obj.equals(tc.getHeaderValue())){
                colModel.removeColumn(tc);
                return true;
            }
        }
        return false;
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
        return colModel.getColumn(column).getHeaderValue();
    }


    public final int getColumnCount() {
        return colModel.getColumnCount();
    }


    public final String getColumnName(int column) {
        if (column != 0){
        return renderColumnTitle(getColumnObject(column));
        }
        else{
            return getTreeColumnLabel();
        }
    }


    public Object getMatrixValue(R rowObj, Object columnObj) {
        if (rowObj instanceof OWLEntity && columnObj instanceof URI){
            int col = indexOf(columnObj);
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

    // @@TODO add to MatrixModel interface
    public int indexOf(Object obj) {
        for (int i=0; i< colModel.getColumnCount(); i++){
            if (obj.equals(colModel.getColumn(i).getHeaderValue())){
                return i;
            }
        }
        return -1;
    }


    // @@TODO add to MatrixModel interface
    public boolean contains(Object value) {
        return indexOf(value) != -1;
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

    public TableColumnModel getColumnModel() {
        return colModel;
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

        Object filter = getFilterForColumn(columnObject);
        if (filter != null){
            label += "(" + filter + ")";
        }
        return label;
    }


///////////////////////////////

//    /**
//     * Will only add unique columns to the axis
//     *
//     * @param newValues
//     * @return true if any columns added
//     */
//    public boolean add(Collection newValues) {
//        boolean result = false;
//        for (Object value : newValues) {
//            if (add(value)) {
//                result = true;
//            }
//        }
//        return result;
//    }


//    public boolean remove(Collection oldValues) {
//        boolean result = false;
//        for (int i=0; i< colModel.getColumnCount(); i++){
//            TableColumn tc = colModel.getColumn(i);
//            if (oldValues.contains(tc.getHeaderValue())){
//                colModel.removeColumn(tc);
//                result = true;
//            }
//        }
//        return result;
//    }


    public void setFilterForColumn(int col, Object filter) {
        filterMap.put(getColumnObject(col), filter);
    }


    public Object getFilterForColumn(int col) {
        return getFilterForColumn(getColumnObject(col));
    }


    public Object getFilterForColumn(Object obj) {
        return filterMap.get(obj);
    }
}
