/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.matrix.ui.component;

import org.coode.jtreetable.JTreeTable;
import org.coode.matrix.model.api.TreeMatrixModel;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.table.OWLObjectDropTargetListener;
import org.protege.editor.owl.ui.transfer.OWLObjectDropTarget;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntologyChange;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.util.List;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 3, 2008<br><br>
 */
public class MatrixTreeTable<R extends OWLObject> extends JTreeTable implements OWLObjectDropTarget, LinkedObjectComponent {


    private OWLModelManager mngr;

    private TreeMatrixModel<R> model;

    private Cursor defaultCursor;

    private TableCellRenderer headerRenderer = new DefaultTableCellRenderer(){
        public Component getTableCellRendererComponent(JTable table, Object value, boolean b, boolean b1, int y, int x) {
            // use getColumnName from the model
//            int modelIndex = table.getColumnModel().getColumn(x).getModelIndex();
            value = getMatrixModel().getTreeTableModelAdapter().getColumnName(x);
            super.getTableCellRendererComponent(table, value, b, b1, y, x);
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


    public MatrixTreeTable(TreeMatrixModel<R> model, OWLModelManager mngr) {

        super(model.getTreeTableModelAdapter(), model.getColumnModel(), model.getTreeRenderer());

        this.model = model;
        this.mngr = mngr;

        defaultCursor = getCursor();

        setShowGrid(true);

        setGridColor(Color.LIGHT_GRAY);//getSelectionBackground());

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        DropTarget dt = new DropTarget(this, new MatrixDropTargetListener(this));

        setDragEnabled(false);

        // must be disabled otherwise JTable redraws all columns on a tableChanged() losing any width info
        // columns are added manually in tableChanged()
        setAutoCreateColumnsFromModel(false);
        setupColumns();
    }

    // override necessary as the default implementation converts header objects to Strings
    public void createDefaultColumnsFromModel() {
    }

    private void setupColumns(){
        final TableColumnModel cm = getColumnModel();
        for (int i=0; i<getModel().getColumnCount(); i++){
            final TableColumn tc = cm.getColumn(i);
            tc.setHeaderRenderer(headerRenderer);
        }
        cm.getColumn(0).setPreferredWidth(300);
    }

    public boolean dropOWLObjects(List<OWLObject> owlObjects, Point pt, int type) {
        boolean result = false;
        for (OWLObject obj : owlObjects) {
            // if any of the objects are dropped successfully, return true
            // this behaviour may need revising depending on expectations
            int dropRow = rowAtPoint(pt);
            int dropColumn = columnAtPoint(pt);
            if (dropOWLObject(obj, dropColumn, dropRow)) {
                result = true;
            }
        }
        return result;
    }


    public OWLModelManager getOWLModelManager() {
        return mngr;
    }


    public TreeMatrixModel<R> getMatrixModel(){
        return model;
    }


    public R getSelectedOWLObject() {
        if (getSelectedRow() >= 0) {
            return (R) getValueAt(getSelectedRow(), 0);
        }
        else {
            return null;
        }
    }


    protected boolean dropOWLObject(OWLObject owlObject, int dropColumn, int dropRow) {

        boolean result = false;

        if (model.isSuitableCellValue(owlObject, dropRow, dropColumn)) {

            // droppedInSelection is true when more than one row is selected and one of the selected rows is dropped on
            boolean droppedInSelection = false;
            int[] selectedRows = getSelectedRows();
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
                    if (addValue(owlObject, selectedRow, dropColumn)){
                        result = true;
                    }
                }
            }
            else {
                result = addValue(owlObject, dropRow, dropColumn);
            }

            if (result){
                repaint(getCellRect(dropRow, dropColumn, true));                
            }
        }
        else if (model.isSuitableColumnObject(owlObject)) {
            dropColumn = Math.max(dropColumn, 1);
            result = addColumn(owlObject);
        }
        return result;
    }


    public boolean addColumn(Object object) {
        return addColumn(object, getColumnModel().getColumnCount());
    }


    public boolean addColumn(Object object, int index) {
        boolean success = model.addColumn(object, index);

        if (success){
            TableColumn c = getColumnModel().getColumn(index);
            c.setHeaderRenderer(headerRenderer);
            packColumn(index);
        }

        return success;
    }


    public boolean removeColumn(int colIndex){
        Object colObj = model.getColumnObject(colIndex);
        return removeColumn(colObj);
    }


    public boolean removeColumn(Object colObj){
        return model.removeColumn(colObj);
    }


    public boolean containsColumn(Object columnObj) {
        return model.contains(columnObj);
    }


    private boolean addValue(OWLObject value, int row, int col) {
        boolean success = false;
        if (model.getTreeTableModelAdapter().isCellEditable(row, col)) {
            R rowObj = model.getRowObject(row);
            Object colObj = model.getColumnObject(col);
            List<OWLOntologyChange> changes = model.addMatrixValue(rowObj, colObj, value);
            if (!changes.isEmpty()){
                mngr.applyChanges(changes);
                success = true;
            }
        }
        return success;
    }


    private void packColumn(int vColIndex){
        DefaultTableColumnModel colModel = (DefaultTableColumnModel)getColumnModel();
        TableColumn col = colModel.getColumn(vColIndex);

        // Get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(
                this, col.getHeaderValue(), false, false, 0, 0);

        int width = comp.getPreferredSize().width;

        // Get maximum width of column data
        for (int r=0; r < getRowCount(); r++) {
            renderer = getCellRenderer(r, vColIndex);
            comp = renderer.getTableCellRendererComponent(
                    this, getValueAt(r, vColIndex), false, false, r, vColIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        if (comp instanceof JCheckBox){
            col.setPreferredWidth(width);
        }
        else{
            col.setPreferredWidth(Math.max(width, 200));
        }
    }


    // stretch the columns widths if they are too small to fit the viewport
    public boolean getScrollableTracksViewportWidth() {
        return getPreferredSize().width < getParent().getWidth();
//        return true;
//        return getColumnCount() == 1;
//        return getColumnCount()*200 < getParent().getWidth();
    }


////////////////////////////////////////// implementation of LinkedObjectComponent


    private OWLEntity linkedObject;


    public Point getMouseCellLocation() {
        Point mousePos = getMousePosition();
        if (mousePos == null) {
            return new Point();
        }
        int row = rowAtPoint(mousePos);
        int col = columnAtPoint(mousePos);
        Rectangle cellRect = getCellRect(row, col, true);
        return new Point(mousePos.x - cellRect.x, mousePos.y - cellRect.y);
    }


    public Rectangle getMouseCellRect() {
        Point mousePos = getMousePosition();
        if(mousePos != null) {
            return getCellRect(rowAtPoint(mousePos), columnAtPoint(mousePos), true);
        }
        else {
            return null;
        }

    }


    public Object getCellObject() {
        Point mousePosition = getMousePosition();
        if (mousePosition == null) {
            return null;
        }
        int row = rowAtPoint(mousePosition);
        int col = columnAtPoint(mousePosition);
        return getValueAt(row, col);
    }


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
                if (getSelectedRows().length < 2) {
                    Point pt = dtde.getLocation();
                    int row = rowAtPoint(pt);
                    int col = columnAtPoint(pt);

                    if (row != -1 && col > 0) {
                        OWLObject obj = getDropObjects(t).iterator().next();
                        if (model.isSuitableCellValue(obj, row, col)) {
                            // Provide a bit of feedback by selecting the table row
                            getSelectionModel().setSelectionInterval(row, row);
                        }
                    }
                }
            }
        }
    }
}
