package org.coode.matrix.model.api;

import org.coode.jtreetable.AbstractTreeTableModel;
import org.coode.jtreetable.TreeTableModel;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProviderListener;
import org.semanticweb.owl.model.OWLObject;

import javax.swing.*;

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
class OWLTreeTableModel<O extends OWLObject> extends AbstractTreeTableModel {

    JTree tree;

    private OWLObjectHierarchyProvider<O> hierarchyProvider;

    private OWLObjectHierarchyProviderListener<O> owlHierarchyListener =
            new OWLObjectHierarchyProviderListener<O>() {

                public void nodeChanged(O node) {
                    Object[] path = {root};
                    fireTreeStructureChanged(this, path, new int[]{0}, new Object[]{root});
                }

                public void hierarchyChanged() {
                    Object[] path = {root};
                    fireTreeStructureChanged(this, path, new int[]{0}, new Object[]{root});
                }
            };

    public OWLTreeTableModel(OWLObjectHierarchyProvider<O> hierarchyProvider) {
        super((hierarchyProvider.getRoots().size() > 0) ? hierarchyProvider.getRoots().iterator().next() : null);
        this.hierarchyProvider = hierarchyProvider;
        hierarchyProvider.addListener(owlHierarchyListener);
    }

    public int getRowCount() {
        return tree.getRowCount();
    }

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) {
        return null;
    }

    public Object getValueAt(Object node, int column) {
        return node;
    }

    public Class getColumnClass(int column) {
        return (column == 0) ? TreeTableModel.class : super.getColumnClass(column);
    }

    public Object getChild(Object object, int i) {
        return hierarchyProvider.getChildren((O) object).toArray()[i];
    }

    public int getChildCount(Object object) {
        return hierarchyProvider.getChildren((O) object).size();
    }

    protected void finalize() throws Throwable {
        hierarchyProvider.removeListener(owlHierarchyListener);
        super.finalize();
    }
}
