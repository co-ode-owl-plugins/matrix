package org.coode.matrix.ui.view;

import org.coode.matrix.model.api.MatrixModel;
import org.coode.matrix.model.impl.RestrictionTreeMatrixModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.ui.action.AddDataPropertyAction;
import org.coode.matrix.ui.action.AddObjectPropertyAction;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.swing.table.TableCellEditor;
import java.util.ArrayList;
import java.util.List;

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
public class ClassMatrixView extends AbstractTreeMatrixView<OWLClass> implements Findable<OWLClass> {

    private int threshold = 10;

    protected void initialiseMatrixView() throws Exception {
        addAction(new AddObjectPropertyAction(getOWLEditorKit(), getTreeTable(), true), "A", "B");
        addAction(new AddDataPropertyAction(getOWLEditorKit(), getTreeTable(), true), "A", "C");
    }

    protected OWLObjectHierarchyProvider<OWLClass> getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider();
    }

    protected final MatrixModel<OWLClass> createMatrixModel(OWLObjectTree<OWLClass> tree) {
        return new RestrictionTreeMatrixModel(tree, getHierarchyProvider(), getOWLModelManager());
    }

    protected boolean isOWLClassView() {
        return true;
    }

    protected TableCellEditor getCellEditor(OWLClass cls, Object columnObject) {

        if (columnObject instanceof RestrictionTreeMatrixModel.PropertyRestrictionPair){

            final RestrictionTreeMatrixModel.PropertyRestrictionPair pair = (RestrictionTreeMatrixModel.PropertyRestrictionPair) columnObject;

            // make sure the editor only shows the appropriate type
            if (pair.getColumnObject() instanceof OWLObjectProperty){
                setEditorType(OWLObjectListParser.ParseType.DESCRIPTION);
            }
            else{
                setEditorType(OWLObjectListParser.ParseType.DATARANGE);
            }

            TableCellEditor editor = getQuickEditor(cls, pair);
            if (editor != null){
                return editor;
            }
        }
        else{
            setEditorType(OWLObjectListParser.ParseType.LITERAL);
        }

        return super.getCellEditor(cls, columnObject);
    }


    public List<OWLClass> find(String match) {
        return new ArrayList<OWLClass>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLClasses(match));
    }


    public void show(OWLClass cls) {
        getTreeTable().getTree().setSelectedOWLObject(cls);
    }

    private TableCellEditor getQuickEditor(OWLClass cls, RestrictionTreeMatrixModel.PropertyRestrictionPair pair) {
//            OWLPropertyExpression p = pair.getColumnObject();
//            if (new PropertyHelper(getOWLModelManager()).isFunctional(p)) {
//                if (getTreeTable().getModel().isValueRestricted(cls, p)) {
//                    Set fillers = getTreeTable().getModel().getSuggestedFillers(cls, p, threshold);
//                    if (!fillers.isEmpty()) {
//                        List values = new ArrayList(fillers);
//                        values.add(0, RestrictionTreeMatrixModel.NONE);
//                        JComboBox dropDown = new JComboBox(values.toArray());
//                        Object value = getTreeTable().getModel().getMatrixValue(cls, p);
//                        if (value instanceof FillerModel){
//                            value = ((FillerModel)value).getAssertedFillersFromSupers();
//                        }
//                        if (((Set<OWLObject>)value).size() == 1){
//                            value = ((Set<OWLObject>)value).iterator().next();
//                            if (values.contains(value)){
//                                dropDown.setSelectedItem(value);
//                            }
//                        }
//                        dropDown.setRenderer(new OWLCellRenderer(getOWLEditorKit()));
//                        return new DefaultCellEditor(dropDown);
//                    }
//                }
//            }
        return null;
    }

}