package org.coode.matrix.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collections;

import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.coode.matrix.model.api.AbstractMatrixModel;
import org.coode.matrix.model.api.MatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.ui.action.AddAnnotationAction;
import org.coode.matrix.ui.action.FitColumnsToContentAction;
import org.coode.matrix.ui.action.FitColumnsToWindowAction;
import org.coode.matrix.ui.action.RemoveColumnAction;
import org.coode.matrix.ui.component.MatrixTreeTable;
import org.coode.matrix.ui.editor.OWLObjectListEditor;
import org.coode.matrix.ui.editor.SimpleStringListEditor;
import org.coode.matrix.ui.renderer.OWLObjectListRenderer;
import org.coode.matrix.ui.renderer.OWLObjectsRenderer;
import org.coode.matrix.ui.renderer.SimpleStringListRenderer;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import uk.ac.manchester.cs.bhig.jtreetable.CellEditorFactory;

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
public abstract class AbstractTreeMatrixView<R extends OWLObject> extends AbstractOWLSelectionViewComponent implements CellEditorFactory {
    private static final long serialVersionUID = 1L;
    protected MatrixTreeTable<R> treeTable;

    private OWLObjectListRenderer objectListRen;
    private TableCellEditor objectListEditor;

    private TableCellRenderer simpleStringListRenderer;
    private SimpleStringListEditor simpleStringListEditor;

    private OWLObjectListParser parser;

    private TreeSelectionListener selectionlistener = new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
            handleTreeSelectionChanged();
        }
    };

    private boolean updatingSelection = false;
    private R lastSelectedOWLObject = null;


    @Override
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

        add(treeTable, BorderLayout.CENTER);

        createDefaultCellRendererAndEditor();

        tree.addTreeSelectionListener(selectionlistener);

        addAction(new AddAnnotationAction(getOWLEditorKit(), treeTable), "A", "A");
        addAction(new RemoveColumnAction(getOWLEditorKit(), treeTable), "B", "A");

        addAction(new FitColumnsToContentAction(treeTable.getTable(), "Fit columns to content", null), "C", "A");

        addAction(new FitColumnsToWindowAction(treeTable.getTable(), "Fit columns to window", null), "C", "B");

        initialiseMatrixView();
    }


    @Override
    public void disposeView() {
        treeTable.getTree().dispose();
        treeTable.getTree().removeTreeSelectionListener(selectionlistener);
        treeTable.dispose();
        selectionlistener = null;
    }


    protected abstract OWLObjectHierarchyProvider<R> getHierarchyProvider();


    protected abstract MatrixModel<R> createMatrixModel(OWLObjectTree<R> tree);


    protected abstract void initialiseMatrixView() throws Exception;


    @Override
    protected R updateView() {
        if (!updatingSelection){
            lastSelectedOWLObject = getSelectedOWLEntity();
            if (lastSelectedOWLObject == null) {
                return null;
            }
            final OWLObjectTree<R> tree = treeTable.getTree();
            R treeSel = tree.getSelectedOWLObject();
            if (treeSel != null) {
                if (lastSelectedOWLObject.equals(treeSel)) {
                    return lastSelectedOWLObject;
                }
            }
            try{
            tree.setSelectedOWLObject(lastSelectedOWLObject);
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                tree.scrollPathToVisible(treePath);
                lastSelectedOWLObject = ((OWLObjectTreeNode<R>) treePath.getLastPathComponent()).getOWLObject();
            }
            }
            catch(ClassCastException e){
                // @@TODO this is a hack to solve an issue with the IndByTypeProvider that will be fixed in build109
            }
        }
        return lastSelectedOWLObject;
    }


    protected TableCellRenderer getCellRendererForColumn(Object columnObject) {
        if (columnObject instanceof AbstractMatrixModel.AnnotationLangPair && ((AbstractMatrixModel.AnnotationLangPair)columnObject).getFilterObject() != null){
            return simpleStringListRenderer;
        }
        return objectListRen;
    }


    public final TableCellEditor getEditor(int row, int col) {
        return getCellEditor(treeTable.getModel().getColumnObjectAtModelIndex(col));
    }


    protected TableCellEditor getCellEditor(Object columnObject) {
        if (columnObject instanceof AbstractMatrixModel.AnnotationLangPair){
            AbstractMatrixModel.AnnotationLangPair pair = (AbstractMatrixModel.AnnotationLangPair) columnObject;
            final String filter = pair.getFilterObject();
            if (filter != null){
                simpleStringListEditor.setFilter(filter);
                return simpleStringListEditor;
            }
        }
        return objectListEditor;
    }


    protected final MatrixTreeTable getTreeTable() {
        return treeTable;
    }


    protected final void setEditorType(OWLObjectListParser.ParseType type) {
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
        simpleStringListEditor = new SimpleStringListEditor(getOWLModelManager());

        objectListRen = new OWLObjectListRenderer(objRen);
        simpleStringListRenderer = new SimpleStringListRenderer();
    }


    protected void handleTreeSelectionChanged() {
        updatingSelection = true;
        if (!isPinned()) {
            TreePath path = treeTable.getTree().getSelectionPath();
            if (path != null) {
                R owlObject = ((OWLObjectTreeNode<R>) path.getLastPathComponent()).getOWLObject();
                if (owlObject instanceof OWLEntity){
                    setGlobalSelection((OWLEntity)owlObject);
                }
            }
            else {
                // Update from OWL selection model
                updateViewContentAndHeader();
            }
        }
        updatingSelection = false;
    }
}
