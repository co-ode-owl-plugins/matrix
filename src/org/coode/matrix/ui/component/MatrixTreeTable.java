/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.matrix.ui.component;

import org.coode.matrix.model.api.MatrixModel;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.protege.editor.owl.ui.table.OWLObjectDropTargetListener;
import org.protege.editor.owl.ui.transfer.OWLObjectDropTarget;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntologyChange;
import uk.ac.manchester.cs.bhig.jtreetable.JTreeTable;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.List;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 3, 2008<br><br>
 */
public class MatrixTreeTable<R extends OWLObject> extends JTreeTable<R>
        implements OWLObjectDropTarget, LinkedObjectComponent {


    private OWLModelManager mngr;

    private MatrixModel<R> model;

    private Cursor defaultCursor;

    private TableCellRenderer headerRenderer = new DefaultTableCellRenderer(){
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            // use getColumnName from the model
            col = getTable().convertColumnIndexToModel(col);
            value = getModel().getColumnName(col);
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            // Inherit the colors and font from the header component
            if (table != null) {
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    };

    private OWLModelManagerListener l = new OWLModelManagerListener(){

        public void handleChange(OWLModelManagerChangeEvent event) {
            if (event.getType().equals(EventType.ENTITY_RENDERER_CHANGED)){
                handleRendererChanged();
            }
        }
    };

    public MatrixTreeTable(OWLObjectTree<R> tree, MatrixModel<R> model, OWLModelManager mngr) {
        // constructor ensures createColsFromModel disabled otherwise JTable redraws all columns
        // on a tableChanged() losing any width info columns are added manually in tableChanged()
        super(tree, model);

        this.model = model;

        this.mngr = mngr;

        defaultCursor = getCursor();

        handleRendererChanged();

        mngr.addListener(l);

        getTable().setGridColor(Color.LIGHT_GRAY);

        setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        // allow drop on both the table and the scrollpane
        final Container clientPane = getTable().getParent();
        DropTarget dt = new DropTarget(clientPane, new MatrixDropTargetListener(this));
        clientPane.setDropTarget(dt);
        dt = new DropTarget(getTable(), new MatrixDropTargetListener(this));
        getTable().setDropTarget(dt);

        setupColumns();
    }

    public void dispose(){
        model.dispose();
        mngr.removeListener(l);
    }


    protected String getTreeTitle() {
        return getModel().getTreeColumnLabel();
    }


    private void setupColumns(){
        final TableColumnModel cm = getTable().getColumnModel();
        for (int i=0; i<getTable().getColumnCount(); i++){
            final TableColumn tc = cm.getColumn(i);
            tc.setHeaderRenderer(headerRenderer);
        }
    }


    public OWLModelManager getOWLModelManager() {
        return mngr;
    }


    public OWLObjectTree<R> getTree() {
        return (OWLObjectTree<R>)super.getTree();
    }


    public MatrixModel<R> getModel(){
        return (MatrixModel<R>)super.getModel();
    }


    public R getSelectedOWLObject() {
        if (getTable().getSelectedRow() >= 0) {
            return (R) model.getValueAt(getTable().getSelectedRow(), 0);
        }
        else {
            return null;
        }
    }


    public boolean dropOWLObjects(List<OWLObject> owlObjects, Point pt, int type) {
        boolean result = false;
        int dropRow = getTable().rowAtPoint(pt);
        int dropColumn = getTable().columnAtPoint(pt);
        for (OWLObject obj : owlObjects) {
            // if any of the objects are dropped successfully, return true
            // this behaviour may need revising depending on expectations
            if (dropOWLObject(obj, dropColumn, dropRow)) {
                result = true;
            }
        }
        if (result){
            getTable().repaint();
        }
        return result;
    }


    protected boolean dropOWLObject(OWLObject dropObject, int dropColumn, int dropRow) {

        boolean result = false;

        int modelColumn = getTable().convertColumnIndexToModel(dropColumn);

        if (dropRow >= 0 && model.isSuitableCellValue(dropObject, dropRow, modelColumn)) {

            // droppedInSelection is true when more than one row is selected and one of the selected rows is dropped on
            boolean droppedInSelection = false;
            int[] selectedRows = getTable().getSelectedRows();
            if (selectedRows.length > 1) {
                for (int selectedRow : selectedRows) {
                    if (selectedRow == dropRow) {
                        droppedInSelection = true;
                        break;
                    }
                }
            }

            if (droppedInSelection) {
                for (int selectedRow : selectedRows) {
                    if (addValue(dropObject, selectedRow, modelColumn)){
                        result = true;
                    }
                }
            }
            else {
                result = addValue(dropObject, dropRow, modelColumn);
            }

            if (result){
                repaint(getTable().getCellRect(dropRow, modelColumn, true));
            }
        }
        else{
            Object colObj = model.getSuitableColumnObject(dropObject);
            if (colObj != null) {
                result = addColumn(colObj);
        }
        }
        return result;
    }


    public boolean addColumn(Object object) {
        return addColumn(object, getTable().getColumnCount());
    }


    public boolean addColumn(Object object, int index) {
        boolean success = model.addColumn(object);

        if (success){
            TableColumn c = getTable().getColumnModel().getColumn(index);
            c.setHeaderRenderer(headerRenderer);
        }

        return success;
    }

    
    public boolean removeColumn(Object object){
        model.removeColumn(object);
        return true;
    }


    private boolean addValue(OWLObject value, int row, int col) {
        boolean success = false;
        if (model.isCellEditable(row, col)) {
            R rowObj = model.getNodeForRow(row);
            Object colObj = model.getColumnObjectAtModelIndex(col);
            List<OWLOntologyChange> changes = model.addMatrixValue(rowObj, colObj, value);
            if (!changes.isEmpty()){
                mngr.applyChanges(changes);
                success = true;
            }
        }
        return success;
    }


    private void handleRendererChanged() {
        final int rowHeight = getFontMetrics(OWLRendererPreferences.getInstance().getFont()).getHeight();
        getTree().setRowHeight(rowHeight);
        getTable().validate();
    }


////////////////////////////////////////// implementation of LinkedObjectComponent


    private OWLEntity linkedObject;


    public Point getMouseCellLocation() {
        Point mousePos = getMousePosition();
        if (mousePos == null) {
            return new Point();
        }
        Rectangle cellRect = getMouseCellRect();
        return new Point(mousePos.x - cellRect.x, mousePos.y - cellRect.y);
    }


    public Rectangle getMouseCellRect() {
        Point mousePos = getMousePosition();
        if(mousePos != null) {
            return getTable().getCellRect(getTable().rowAtPoint(mousePos),
                                          getTable().columnAtPoint(mousePos), true);
        }
        else {
            return null;
        }

    }

    //
    //    public Object getCellObject() {
    //        Point mousePosition = getMousePosition();
    //        if (mousePosition == null) {
    //            return null;
    //        }
    //        int row = getTable().rowAtPoint(mousePosition);
    //        int col = getTable().columnAtPoint(mousePosition);
    //        return getTable().getValueAt(row, col);
    //    }


    public void setLinkedObject(OWLObject object) {
        if (object instanceof OWLEntity) {
            linkedObject = (OWLEntity) object;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else {
            linkedObject = null;
            setCursor(defaultCursor);
        }
    }


    public OWLObject getLinkedObject() {
        return linkedObject;
    }


    public JComponent getComponent() {
        return this;
    }


    /**
     * The DnD behaviour for the matrix tree table
     */
    class MatrixDropTargetListener extends OWLObjectDropTargetListener {

        public MatrixDropTargetListener(MatrixTreeTable component) {
            super(component);
        }

        public void dragOver(DropTargetDragEvent dtde) {
            super.dragOver(dtde);

            Transferable t = dtde.getTransferable();
            if (isAcceptableTransferable(t)) {
                if (getTable().getSelectedRows().length < 2) {
                    Point pt = dtde.getLocation();
                    int row = getTable().rowAtPoint(pt);
                    int col = getTable().columnAtPoint(pt);

                    if (row != -1) {
                        OWLObject obj = getDropObjects(t).iterator().next();
                        if (model.isSuitableCellValue(obj, row, col)) {
                            // Provide a bit of feedback by selecting the table row
                            getTable().getSelectionModel().setSelectionInterval(row, row);

                            setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                        }
                        else{
                            setCursor(defaultCursor);
                        }
                    }
                }
            }
        }


        public void dragExit(DropTargetEvent dte) {
            super.dragExit(dte);
            setCursor(defaultCursor);
        }
    }
}
