package org.coode.matrix.ui.editor;

import org.apache.log4j.Logger;
import org.coode.matrix.model.impl.FillerModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.ui.renderer.OWLObjectsRenderer;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLAutoCompleter;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.EventObject;
import java.util.Set;

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
public class OWLObjectListEditor extends AbstractCellEditor implements TableCellEditor {

    private static Logger logger = Logger.getLogger(OWLObjectListEditor.class);

    private OWLObjectsRenderer ren;

    private JTextField editor;

    private Set<OWLObject> originalFillers;

    private OWLObjectListParser parser;

    private OWLAutoCompleter ac;

    private OWLEditorKit eKit;

    private OWLExpressionChecker<Set<OWLObject>> checker = new OWLExpressionChecker<Set<OWLObject>>(){
        public void check(String text) throws OWLExpressionParserException {
            OWLObjectListEditor.this.parser.isWellFormed(text);
        }

        public Set<OWLObject> createObject(String text) throws OWLExpressionParserException {
            return OWLObjectListEditor.this.parser.getValues(text);
        }
    };


    public OWLObjectListEditor(OWLEditorKit eKit, OWLObjectsRenderer ren, OWLObjectListParser parser) {
        super();

        this.eKit = eKit;
        this.ren = ren;
        this.parser = parser;

        editor = new JTextField();
        editor.setBorder(null);

        // workaround to ensure the editor properly gets the focus (and caret)
        // which is required in order for Ac to work
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4256006
        editor.addAncestorListener( new AncestorListener(){
            public void ancestorAdded(AncestorEvent e){
                editor.requestFocus();
                // also, remove the default selection otherwise the first character gets lost
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        final int end = editor.getDocument().getLength();
                        editor.setSelectionStart(end);
                        editor.setSelectionEnd(end);
                    }
                });
            }
            public void ancestorMoved(AncestorEvent e){}
            public void ancestorRemoved(AncestorEvent e){}
        });
    }


    public boolean stopCellEditing() {
        ac.uninstall();
        return super.stopCellEditing();
    }


    public void cancelCellEditing() {
        ac.uninstall();
        super.cancelCellEditing();
    }


    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        if (value instanceof FillerModel){
            originalFillers = ((FillerModel)value).getAssertedFillersFromSupers();
        }
        else{
            originalFillers = (Set<OWLObject>) value;
        }

        editor.setFont(OWLRendererPreferences.getInstance().getFont());
        editor.setText((value != null) ? ren.render(originalFillers) : "");
        ac = new OWLAutoCompleter(eKit, editor, checker);

        return editor;
    }


    public Object getCellEditorValue() {
        if (editor.getText().trim().length() == 0){
            return Collections.emptySet();
        }
        try {
            return parser.getValues(editor.getText());
        }
        catch (OWLExpressionParserException e) {
            logger.warn(e.getMessage());
        }
        return originalFillers;
    }


    public boolean isCellEditable(EventObject eventObject) {
        if (eventObject instanceof MouseEvent) {
            return ((MouseEvent) eventObject).getClickCount() >= 2;
        }
        else {
            return super.isCellEditable(eventObject);
        }
    }
}