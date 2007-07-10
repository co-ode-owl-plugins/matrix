package org.coode.matrix.ui.renderer;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeCellRenderer;
import org.semanticweb.owl.model.OWLObject;
import org.coode.jtreetable.TreeTableCellRenderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
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
public class OWLObjectTreeTableCellRenderer<N extends OWLObject> extends OWLObjectTree<N>
        implements TreeTableCellRenderer {

    protected int visibleRow;
    private JTable hostTable;

    private OWLObjectHierarchyProvider<N> provider;

    public OWLObjectTreeTableCellRenderer(OWLEditorKit eKit,
                                          OWLObjectHierarchyProvider<N> provider,
                                          Comparator<N> comparator) {
        super(eKit, provider, comparator);

        this.provider = provider;

        setOpaque(false);

        OWLObjectTreeCellRenderer treeCellRenderer = new OWLObjectTreeCellRenderer(eKit);

        setCellRenderer(treeCellRenderer);

        expandRow(0);
    }


    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, 0, w, hostTable.getHeight());
    }


    public void paint(Graphics g) {
        g.translate(0, -visibleRow * getRowHeight());
        super.paint(g);
    }


    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {

        if (isSelected) {
            setOpaque(true);
            setBackground(table.getSelectionBackground());
        }
        else {
            setOpaque(false);
            setBackground(table.getBackground());
        }

        hostTable = table;
        visibleRow = row;
        return this;
    }

    public JTree getTree() {
        return this;
    }

    public OWLObjectHierarchyProvider<N> getHierarchyProvider() {
        return provider;
    }
}
