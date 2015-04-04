package org.coode.matrix.ui.editor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 17, 2008<br><br>
 */
public class SimpleStringListEditor  extends AbstractCellEditor implements TableCellEditor {
    private static final long serialVersionUID = 1L;
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
        Set<OWLLiteral> constants = new HashSet<OWLLiteral>();
        String value = textField.getText();
        if (value.length() > 0){
            for (String s : parseStringsList(value)){
                if (filter == null || filter.equals("!")){
                    constants.add(mngr.getOWLDataFactory().getOWLLiteral(s));
                }
                else{
                    constants.add(mngr.getOWLDataFactory().getOWLLiteral(s, filter));
                }
            }
        }
        return constants;
    }


    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        StringBuilder str = new StringBuilder();
        for (OWLLiteral constant : (Set<OWLLiteral>)value){
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
