package org.coode.matrix.ui.view;

import org.coode.matrix.model.api.MatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.ui.action.AddAnnotationAction;
import org.coode.matrix.ui.action.FitColumnsToContentAction;
import org.coode.matrix.ui.action.FitColumnsToWindowAction;
import org.coode.matrix.ui.action.RemoveColumnAction;
import org.coode.matrix.ui.component.MatrixTreeTable;
import org.coode.matrix.ui.editor.OWLObjectListEditor;
import org.coode.matrix.ui.renderer.OWLObjectListRenderer;
import org.coode.matrix.ui.renderer.OWLObjectsRenderer;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.OWLEntityComparator;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import uk.ac.manchester.cs.bhig.jtreetable.CellEditorFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;

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
public abstract class AbstractTreeMatrixView<R extends OWLEntity> extends AbstractOWLSelectionViewComponent implements CellEditorFactory {

    private static final boolean FILTERS_SUPPORTED = false;

    private MatrixTreeTable<R> treeTable;

//    private  tree;

    private OWLEntityComparator<R> comparator;

    private OWLObjectListRenderer objectListRen;

    private TableCellEditor objectListEditor;

    private OWLObjectListParser parser;

    private TreeSelectionListener selectionlistener = new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
            handleTreeSelectionChanged();
        }
    };


    private MouseListener columnFilterMouseListener = new MouseAdapter(){
        public void mouseClicked(MouseEvent mouseEvent) {
            int col = treeTable.getTable().getColumnModel().getColumnIndexAtX(mouseEvent.getX());
            handleColumnFilterRequest(col);
        }
    };


    public final void initialiseView() throws Exception {

        setLayout(new BorderLayout(6, 6));

        OWLObjectHierarchyProvider<R> hierarchy = getHierarchyProvider();

        OWLModelManagerTree<R> tree = new OWLModelManagerTree<R>(getOWLEditorKit(), hierarchy);

        MatrixModel<R> model = createMatrixModel(tree);

        treeTable = new MatrixTreeTable<R>(tree, model, getOWLModelManager());

        treeTable.getTable().setDefaultRenderer(Object.class, new TableCellRenderer(){
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                int modelIndex = treeTable.getTable().convertColumnIndexToModel(col);
                final Object colObj = treeTable.getModel().getColumnObjectAtModelIndex(modelIndex);
                TableCellRenderer delegate = getCellRendererForColumn(colObj);
                return delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            }
        });

        treeTable.setCellEditorFactory(this);

        if (filtersSupported()){
            JTableHeader header = treeTable.getTable().getTableHeader();
            header.addMouseListener(columnFilterMouseListener);
        }

        add(treeTable, BorderLayout.CENTER);

        createDefaultCellRendererAndEditor();

        tree.addTreeSelectionListener(selectionlistener);

        addAction(new AddAnnotationAction(getOWLEditorKit(), treeTable), "A", "A");
        addAction(new RemoveColumnAction(getOWLEditorKit(), treeTable), "B", "A");

        addAction(new FitColumnsToContentAction(treeTable.getTable(), "Fit columns to content", null), "C", "A");

        addAction(new FitColumnsToWindowAction(treeTable.getTable(), "Fit columns to window", null), "C", "B");

        initialiseMatrixView();
    }


    public void disposeView() {
        treeTable.getTree().dispose();
        treeTable.getTree().removeTreeSelectionListener(selectionlistener);
        JTableHeader header = treeTable.getTable().getTableHeader();
        header.removeMouseListener(columnFilterMouseListener);
        treeTable.dispose();
        selectionlistener = null;
    }


    protected abstract OWLObjectHierarchyProvider<R> getHierarchyProvider();


    protected abstract MatrixModel<R> createMatrixModel(OWLObjectTree<R> tree);


    protected abstract void initialiseMatrixView() throws Exception;


    protected boolean filtersSupported() {
        return FILTERS_SUPPORTED;
    }


    protected final Comparator<R> getOWLEntityComparator() {
        if (comparator == null){
            comparator = new OWLEntityComparator<R>(getOWLModelManager()){

                public int compare(R o1, R o2) {
                    if (o1 instanceof OWLIndividual && o2 instanceof OWLClass){
                        return -1;
                    }
                    else if (o2 instanceof OWLIndividual && o1 instanceof OWLClass){
                        return 1;
                    }
                    return super.compare(o1, o2);
                }
            };
        }
        return comparator;
    }


    protected R updateView() {
        R selectedOWLObject = getSelectedOWLEntity();
        if (selectedOWLObject == null) {
            return null;
        }
        final OWLObjectTree<R> tree = treeTable.getTree();
        R treeSelCls = tree.getSelectedOWLObject();
        if (treeSelCls != null) {
            if (selectedOWLObject.equals(treeSelCls)) {
                return selectedOWLObject;
            }
        }
        tree.setSelectedOWLObject(selectedOWLObject);
        TreePath treePath = tree.getSelectionPath();
        if (treePath != null) {
            tree.scrollPathToVisible(treePath);
            return ((OWLObjectTreeNode<R>) treePath.getLastPathComponent()).getOWLObject();
        }
        return selectedOWLObject;
    }


    protected TableCellRenderer getCellRendererForColumn(Object columnObject) {
        return objectListRen;
    }


    public TableCellEditor getEditor(int row, int col) {
        return getCellEditor(treeTable.getModel().getNodeForRow(row),
                             treeTable.getModel().getColumnObjectAtModelIndex(col));
    }


    protected TableCellEditor getCellEditor(R rowObject, Object columnObject) {
        return objectListEditor;
    }


    protected final MatrixTreeTable getTreeTable() {
        return treeTable;
    }


    /**
     *
     * @param type - one of "CLASS, OBJPROP, DATAPROP, INDIVIDUAL, DATATYPE" constants from OWLObjectListParser
     */
    protected final void setEditorType(int type) {
        parser.setTypes(Collections.singleton(type));
    }


    private R getSelectedOWLEntity() {
        if (isOWLClassView()) {
            return (R) getOWLEditorKit().getWorkspace().getOWLSelectionModel().getLastSelectedClass();
        }
        else if (isOWLObjectPropertyView()) {
            return (R) getOWLEditorKit().getWorkspace().getOWLSelectionModel().getLastSelectedObjectProperty();
        }
        else if (isOWLDataPropertyView()) {
            return (R) getOWLEditorKit().getWorkspace().getOWLSelectionModel().getLastSelectedDataProperty();
        }
        else if (isOWLIndividualView()) {
            return (R) getOWLEditorKit().getWorkspace().getOWLSelectionModel().getLastSelectedIndividual();
        }
        else {
            return (R) getOWLEditorKit().getWorkspace().getOWLSelectionModel().getSelectedEntity();
        }
    }


    private void createDefaultCellRendererAndEditor() {
        OWLObjectsRenderer objRen = new OWLObjectsRenderer(getOWLModelManager());

        parser = new OWLObjectListParser(getOWLModelManager());

        objectListEditor = new OWLObjectListEditor(getOWLEditorKit(), objRen, parser);
        objectListRen = new OWLObjectListRenderer(objRen);
    }


    private void handleColumnFilterRequest(int col) {
        final Object colObj = treeTable.getModel().getColumnModel().getColumn(col).getHeaderValue();
        if (col >= 0 && colObj instanceof URI){
            String lang = JOptionPane.showInputDialog("Enter a language filter (or leave blank for all)");
            if (lang == null || lang.length() == 0){
                lang = null;
            }
            treeTable.getModel().setFilterForColumn(colObj, lang);
            treeTable.revalidate();
        }
    }


    private void handleTreeSelectionChanged() {
        if (!isPinned()) {
            TreePath path = treeTable.getTree().getSelectionPath();
            if (path != null) {
                R owlObject = ((OWLObjectTreeNode<R>) path.getLastPathComponent()).getOWLObject();
                setSelectedEntity(owlObject);
            }
            else {
                // Update from OWL selection model
                updateViewContentAndHeader();
            }
        }
    }
}
