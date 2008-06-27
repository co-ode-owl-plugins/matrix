package org.coode.matrix.ui.editor;

import org.coode.matrix.model.impl.FillerModel;
import org.coode.matrix.model.parser.OWLObjectListParser;
import org.coode.matrix.ui.renderer.OWLObjectsRenderer;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.description.OWLExpressionParserException;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLDescriptionAutoCompleter;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObject;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
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

    private OWLObjectsRenderer ren;

    private JTextField editor;

    private Set<OWLDescription> originalFillers;

    private OWLObjectListParser parser;

    public OWLObjectListEditor(OWLEditorKit eKit, OWLObjectsRenderer ren, OWLObjectListParser parser) {
        super();

        this.ren = ren;
        this.parser = parser;

        editor = new JTextField();

        OWLExpressionChecker<Set<OWLObject>> checker = new OWLExpressionChecker<Set<OWLObject>>(){
            public void check(String text) throws OWLExpressionParserException {
                OWLObjectListEditor.this.parser.isWellFormed(text);
            }

            public Set<OWLObject> createObject(String text) throws OWLExpressionParserException {
                return OWLObjectListEditor.this.parser.getValues(text);
            }
        };

        OWLDescriptionAutoCompleter ac = new OWLDescriptionAutoCompleter(eKit, editor, checker);
    }


    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof FillerModel){
            originalFillers = ((FillerModel)value).getAssertedFillersFromSupers();
        }
        else{
            originalFillers = (Set<OWLDescription>) value;
        }

        editor.setText((value != null) ? ren.render(originalFillers) : "");

        return editor;
    }


    public Object getCellEditorValue() {
        try {
            return parser.getValues(editor.getText());
        }
        catch (OWLException e) {
            ProtegeApplication.getErrorLog().handleError(Thread.currentThread(), e);            
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