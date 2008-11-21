package org.coode.matrix.ui.action;

import org.coode.matrix.model.api.AbstractMatrixModel;
import org.coode.matrix.ui.component.MatrixTreeTable;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.frame.AnnotationURIList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;

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
public class AddAnnotationAction extends DisposableAction {

    private static final String LABEL = "Add annotation column to matrix";

    private OWLEditorKit eKit;
    private MatrixTreeTable table;

    private AnnotationURIList uriList;

    private JComboBox langSelector;

    private JPanel pane;


    public AddAnnotationAction(OWLEditorKit eKit, MatrixTreeTable table) {
        super(LABEL, OWLIcons.getIcon("property.annotation.add.png"));
        this.eKit = eKit;
        this.table = table;
    }


    public void dispose() {
    }

    
    public void actionPerformed(ActionEvent actionEvent) {
        if (pane == null){
            createUI();
        }
        uriList.rebuildAnnotationURIList();

        if (new UIHelper(eKit).showDialog(LABEL, pane, uriList) == JOptionPane.OK_OPTION){
            URI uri = uriList.getSelectedURI();
            if (uri != null){
                String lang = (String)langSelector.getSelectedItem();
                if (lang == null || lang.length() == 0){
                    lang = null;
                }
                table.addColumn(new AbstractMatrixModel.AnnotationLangPair(uri, lang));
            }
        }
    }


    private void createUI() {
        uriList = new AnnotationURIList(eKit);
        JComponent uriScroller = new JScrollPane(uriList);
        uriScroller.setAlignmentX(0.0f);
        JComponent uriPanel = new Box(BoxLayout.PAGE_AXIS);
        uriPanel.setAlignmentX(0.0f);
        JLabel annotLabel = new JLabel("Annotation URI: ");
        annotLabel.setAlignmentX(0.0f);
        uriPanel.add(annotLabel);
        uriPanel.add(Box.createVerticalStrut(6));
        uriPanel.add(uriScroller);

        UIHelper helper = new UIHelper(eKit);

        langSelector = helper.getLanguageSelector();
        langSelector.setAlignmentX(0.0f);
        JComponent langPanel = new Box(BoxLayout.PAGE_AXIS);
        langPanel.setAlignmentX(0.0f);
        JLabel label = new JLabel("Language filter: ");
        label.setAlignmentX(0.0f);
        langPanel.add(label);
        langPanel.add(Box.createVerticalStrut(6));
        langPanel.add(langSelector);
        langPanel.add(Box.createVerticalStrut(6));
        final JLabel filterInstructions = new JLabel("<html>Leave the filter blank to show all annotations or use !" +
                                                     " to display annotations with no language set." +
                                                     "</html>");
        filterInstructions.setForeground(new Color(50, 50, 50));
        langPanel.add(filterInstructions);

        pane = new JPanel(new BorderLayout(24, 24));
        pane.setPreferredSize(new Dimension(400, 500));
        pane.add(uriPanel, BorderLayout.CENTER);
        pane.add(langPanel, BorderLayout.SOUTH);
    }
}
