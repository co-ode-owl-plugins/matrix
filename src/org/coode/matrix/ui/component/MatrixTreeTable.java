package org.coode.matrix.ui.component;

import org.coode.jtreetable.JTreeTable;
import org.coode.matrix.model.api.TreeMatrixModel;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.table.OWLObjectDropTargetListener;
import org.protege.editor.owl.ui.transfer.OWLObjectDropTarget;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntologyManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.util.Collection;
import java.util.HashSet;

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
public class MatrixTreeTable<R extends OWLObject> extends JTreeTable
        implements OWLObjectDropTarget, LinkedObjectComponent {

    private OWLModelManager mngr;

    private TreeMatrixModel<R> model;

    private Cursor defaultCursor;

    public MatrixTreeTable(TreeMatrixModel<R> model, OWLModelManager mngr) {
        super(model.getTreeTableModelAdapter(), model.getTreeRenderer());

        this.model = model;
        this.mngr = mngr;

        defaultCursor = getCursor();

        setShowGrid(true);

        setGridColor(Color.LIGHT_GRAY);//getSelectionBackground());

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        getColumnModel().getColumn(0).setPreferredWidth(300);

        DropTarget dt = new DropTarget(this, new MatrixDropTargetListener(this));

        setDragEnabled(false);
    }

    public R getSelectedOWLObject() {
        if (getSelectedRow() >= 0) {
            return (R) getValueAt(getSelectedRow(), 0);
        }
        else {
            return null;
        }
    }

    public boolean dropOWLObjects(java.util.List<OWLObject> owlObjects, Point pt, int type) {
        boolean result = false;
        for (OWLObject obj : owlObjects) {
            // if any of the objects are dropped successfully, return true
            // this behaviour may need revising depending on expectations
            if (dropOWLObject(obj, pt, type)) {
                result = true;
            }
        }
        return result;
    }


    public boolean dropOWLObject(OWLObject owlObject, Point pt, int type) {

        boolean result = false;

        int dropRow = rowAtPoint(pt);
        int dropColumn = columnAtPoint(pt);
        if (model.isSuitableCellValue(owlObject, dropRow, dropColumn)) {

            int[] selectedRows = getSelectedRows();
            boolean droppedInSelection = false;
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
                    addValue(owlObject, selectedRow, dropColumn);
                }
                repaint();
            }
            else {
                addValue(owlObject, dropRow, dropColumn);
                getComponentAt(pt).repaint();
            }
            result = true;
        }
        else if (model.isSuitableColumnObject(owlObject)) {
            dropColumn = Math.max(columnAtPoint(pt), 1);
            result = model.addColumn(owlObject, dropColumn - 1);
        }
        return result;
    }


    private void addValue(OWLObject owlObject, int row, int col) {
        if (model.getTreeTableModelAdapter().isCellEditable(row, col)) {
            Object o = getValueAt(row, col);
            HashSet newValue = new HashSet();
            if (o instanceof Collection && o != null) {
                newValue.addAll((Collection) o);
            }
            newValue.add(owlObject);
            setValueAt(newValue, row, col);
        }
    }

    public OWLModelManager getOWLModelManager() {
        return mngr;
    }
//
//    public boolean getScrollableTracksViewportWidth() {
//        return (getPreferredSize().width <= getParent().getSize().width);
//    }

    //////////////////////////////////////////

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
    public class MatrixDropTargetListener extends OWLObjectDropTargetListener {

        private MatrixTreeTable table;


        public MatrixDropTargetListener(MatrixTreeTable component) {
            super(component);
            this.table = component;
        }


        public void dragOver(DropTargetDragEvent dtde) {
            super.dragOver(dtde);

            Transferable t = dtde.getTransferable();
            if (isAcceptableTransferable(t)) {
                if (table.getSelectedRows().length < 2) {
                    Point pt = dtde.getLocation();
                    int row = table.rowAtPoint(pt);
                    int col = table.columnAtPoint(pt);

                    if (row != -1 && col > 0) {
                        OWLObject obj = getDropObjects(t).iterator().next();
                        if (model.isSuitableCellValue(obj, row, col)) {
                            // Provide a bit of feedback by selecting the table row
                            table.getSelectionModel().setSelectionInterval(row, row);
                        }
                    }
                }
            }
        }
    }
}
