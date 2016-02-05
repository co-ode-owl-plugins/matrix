/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.matrix.test;

import junit.framework.TestCase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 4, 2008<br><br>
 */
public class Sandbox extends TestCase {

    public static enum MyEnum {One, Two}

    public class TestClass{}

    public void testEnumHeadersTransformedToString(){
        JTable t = new JTable();
        t.setAutoCreateColumnsFromModel(false);
        final TableColumn tc = new TableColumn();
        tc.setHeaderValue(MyEnum.One);

        // doing this by hand works
        assertTrue(tc.getHeaderValue() instanceof MyEnum);

        // even when added to th model
        t.getColumnModel().addColumn(tc);

        assertSame(MyEnum.One, t.getColumnModel().getColumn(0).getHeaderValue());
    }

    // it appears that the default implementation of JTable.createColumnsFromModel converts header objects to Strings
    public void testHeadersTransformedToString3(){
        // try with some arbitrary object
        DefaultTableModel model = new DefaultTableModel();
        TestClass test = new TestClass();
        System.out.println("test = " + test);
        model.addColumn(test);
        JTable table = new JTable(model);
        Object columnHeaderValue = table.getColumnModel().getColumn(0).getHeaderValue();
        System.out.println("columnHeaderValue = " + columnHeaderValue);

        assertSame(test.getClass(), columnHeaderValue.getClass());

        assertSame(test, columnHeaderValue);

        assertEquals(test, columnHeaderValue);
    }
}
