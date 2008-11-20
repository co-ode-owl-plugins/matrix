package org.coode.matrix.ui.action;

import org.coode.matrix.model.impl.RestrictionTreeMatrixModel;
import org.coode.matrix.ui.component.MatrixTreeTable;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.selector.OWLDataPropertySelectorPanel;
import org.semanticweb.owl.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
public class AddDataPropertyAction extends DisposableAction {

    private static final String LABEL = "Add data property column to matrix";

    private OWLEditorKit eKit;
    private MatrixTreeTable table;

    private OWLDataPropertySelectorPanel propSelector;

    private JComboBox typeSelector;

    private JPanel pane;

    private boolean showRestrictionType;


    public AddDataPropertyAction(OWLEditorKit eKit, MatrixTreeTable table){
        this(eKit, table, false);
    }


    public AddDataPropertyAction(OWLEditorKit eKit, MatrixTreeTable table, boolean showRestrictionType) {
        super(LABEL, OWLIcons.getIcon("property.data.add.png"));
        this.eKit = eKit;
        this.table = table;
        this.showRestrictionType = showRestrictionType;

        propSelector = new OWLDataPropertySelectorPanel(eKit);
        JComponent uriScroller = new JScrollPane(propSelector);
        uriScroller.setAlignmentX(0.0f);
        JComponent uriPanel = new Box(BoxLayout.PAGE_AXIS);
        uriPanel.setAlignmentX(0.0f);
        JLabel annotLabel = new JLabel("Data property: ");
        annotLabel.setAlignmentX(0.0f);
        uriPanel.add(annotLabel);
        uriPanel.add(Box.createVerticalStrut(6));
        uriPanel.add(uriScroller);

        pane = new JPanel(new BorderLayout(24, 24));
        pane.setPreferredSize(new Dimension(400, 500));
        pane.add(uriPanel, BorderLayout.CENTER);

        if (showRestrictionType){
            typeSelector = createTypeSelector();
            typeSelector.setAlignmentX(0.0f);
            JComponent langPanel = new Box(BoxLayout.PAGE_AXIS);
            langPanel.setAlignmentX(0.0f);
            JLabel label = new JLabel("Restriction type: ");
            label.setAlignmentX(0.0f);
            langPanel.add(label);
            langPanel.add(Box.createVerticalStrut(6));
            langPanel.add(typeSelector);
            pane.add(langPanel, BorderLayout.SOUTH);
        }
    }


    private JComboBox createTypeSelector() {
        Class[] types = {OWLDataSomeRestriction.class, OWLDataAllRestriction.class};
        JComboBox c = new JComboBox(types);
        c.setRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
                o = RestrictionTreeMatrixModel.PropertyRestrictionPair.render((Class)o);
                return super.getListCellRendererComponent(jList, o, i, b, b1);
            }
        });
        return c;
    }


    public void dispose() {
        // do nothing
    }


    public void actionPerformed(ActionEvent actionEvent) {
        if (new UIHelper(eKit).showDialog(LABEL, pane, propSelector) == JOptionPane.OK_OPTION){
            for (OWLDataProperty p : propSelector.getSelectedObjects()){
                if (showRestrictionType){
                    Class<? extends OWLQuantifiedRestriction<OWLDataPropertyExpression, OWLDataRange>> type;
                    type = (Class<? extends OWLQuantifiedRestriction<OWLDataPropertyExpression, OWLDataRange>>) typeSelector.getSelectedItem();
                    final RestrictionTreeMatrixModel.PropertyRestrictionPair<OWLDataPropertyExpression, OWLDataRange> pair;
                    pair = new RestrictionTreeMatrixModel.PropertyRestrictionPair<OWLDataPropertyExpression, OWLDataRange>(p, type);
                    table.addColumn(pair);
                }
                else{
                    table.addColumn(p);
                }
            }
        }
    }
}