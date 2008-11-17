package org.coode.matrix.ui.editor;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.OWLUntypedConstant;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 17, 2008<br><br>
 */
public class SimpleStringListEditor  extends AbstractCellEditor implements TableCellEditor {

    private JTextField textField;

    private static Pattern p;

    private String filter;

    private OWLModelManager mngr;


    public SimpleStringListEditor(OWLModelManager mngr) {
        this.mngr = mngr;
        textField = new JTextField();
        textField.setBorder(null);
    }


    public void setFilter(String lang){
        this.filter = lang;
    }


    public Object getCellEditorValue() {
        Set<OWLUntypedConstant> constants = new HashSet<OWLUntypedConstant>();
        String value = textField.getText();
        if (value.length() > 0){
            for (String s : parseStringsList(value)){
                constants.add(mngr.getOWLDataFactory().getOWLUntypedConstant(s, filter));
            }
        }
        return constants;
    }


    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        StringBuilder str = new StringBuilder();
        for (OWLUntypedConstant constant : (Set<OWLUntypedConstant>)value){
            if (str.length() > 0){
                str.append(", ");
            }
            str.append(constant.getLiteral());
        }
        textField.setText(str.toString());
        return textField;
    }

    public static java.util.List<String> parseStringsList(String s){
        java.util.List<String> values = new ArrayList<String>();

        if (p == null){
            p = Pattern.compile("\\G[,]?\\s*\"(.*?)\"\\s*");
        }

        Matcher m = p.matcher(s);
        while (m.find()){
            values.add(m.group(1));
        }

        if (values.isEmpty()){
            values.add(s);
        }
        return values;
    }


    public static void main(String[] args) {

        String[] tests = {
                "",
                "\"\"",
                "this is a single one out of quotes",
                "\"this is a single one in quotes\"",
                "\"first\", \"second\"",
                "\"first\", \"second\", \"third\"",
                "\"first but followed by no comma\", \"second\"",
                "\"first\" second without quotes",
                "first without quotes \"second\"",
        };

        int i=0;

        for (String test : tests){
            System.out.println("\ntest " + i++);
            for (String s : SimpleStringListEditor.parseStringsList(test)){
                System.out.println(s);
            }
        }
    }
}
