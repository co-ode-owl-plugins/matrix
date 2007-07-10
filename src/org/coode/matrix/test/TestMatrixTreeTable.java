package org.coode.matrix.test;

import org.apache.log4j.Logger;
import org.coode.matrix.model.api.AbstractTreeMatrixModel;
import org.coode.matrix.ui.component.MatrixTreeTable;
import org.coode.matrix.ui.renderer.OWLObjectTreeTableCellRenderer;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.OWLEntityComparator;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntologyChange;

import javax.swing.*;
import java.net.URI;
import java.util.Comparator;
import java.util.Set;
import java.util.Collections;
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
public class TestMatrixTreeTable {

    private static MatrixTreeTable table;

    public static void main(String[] args) {
        OWLEditorKitFactory eKitFac = new OWLEditorKitFactory();
        OWLEditorKit eKit = new OWLEditorKit(eKitFac);
        OWLModelManager mngr = new OWLModelManagerImpl();
        try {
            mngr.loadOntology(new URI("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl"));
            OWLObjectHierarchyProvider<OWLClass> provider = mngr.getOWLClassHierarchyProvider();

            Comparator comp = new OWLEntityComparator(mngr);
            OWLObjectTreeTableCellRenderer<OWLClass> ren = new OWLObjectTreeTableCellRenderer<OWLClass>(eKit, provider, comp);

            AbstractTreeMatrixModel<OWLClass> model = new AbstractTreeMatrixModel<OWLClass>(ren, mngr) {

                public Object getMatrixValue(OWLClass rowObject, Object columnObject) {
                    return "hehe";
                }

                public List<OWLOntologyChange> setMatrixValue(OWLClass rowObj, Object columnObj, Object value) {
                    return Collections.EMPTY_LIST;
                }

                public boolean isSuitableCellValue(Object value, int row, int col) {
                    return false;  //@@TODO implement
                }

                public boolean isSuitableColumnObject(Object columnObject) {
                    return false;  //@@TODO implement
                }

                public boolean isValueRestricted(OWLClass rowObject, Object columnObject) {
                    return false;
                }

                public Set getSuggestedFillers(OWLClass rowObject, Object columnObject, int threshold) {
                    return Collections.EMPTY_SET;
                }

                protected String getTreeColumnLabel() {
                    return "Entity";
                }
            };

            table = new MatrixTreeTable<OWLClass>(model, mngr);

            JOptionPane.showMessageDialog(null, new JScrollPane(table));
        }
        catch (Exception e) {
            Logger.getLogger(TestMatrixTreeTable.class).error(e);
        }

        System.exit(0);
    }
}
