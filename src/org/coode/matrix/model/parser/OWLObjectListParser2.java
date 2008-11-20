package org.coode.matrix.model.parser;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.description.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.semanticweb.owl.expression.ParserException;
import org.semanticweb.owl.model.OWLObject;

import java.util.HashSet;
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
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 19, 2008<br><br>
 */
public class OWLObjectListParser2 {

    private static final String SEPARATOR = ",";

    public static final int LITERAL = 0;
    public static final int CLASS = 1;
    public static final int OBJPROP = 2;
    public static final int DATAPROP = 3;
    public static final int INDIVIDUAL = 4;
    public static final int DESCRIPTION = 5;
    public static final int DATARANGE = 6;

    private Set<Integer> types;
    private OWLModelManager mngr;


    public OWLObjectListParser2(OWLModelManager mngr) {
        this.mngr = mngr;
    }

    public boolean isWellFormed(String expression) throws OWLExpressionParserException {
        getValues(expression);
        return true;
    }


    public Set<OWLObject> getValues(String expression) throws OWLExpressionParserException {
        Set<OWLObject> results = new HashSet<OWLObject>();
        try {
            if (isDescriptionParser()){
                results.addAll(createParser(expression).parseDescriptionList());
            }
            else if (isObjectPropertyParser()){
                results.addAll(createParser(expression).parseObjectPropertyList());
            }
            else if (isDataPropertyParser()){
                results.addAll(createParser(expression).parseDataPropertyList());
            }
            else if (isIndividualParser()){
                results.addAll(createParser(expression).parseIndividualList());
            }
            else if (isClassParser()){
                results.addAll(createParser(expression).parseDescriptionList());
            }
            else if (isDataRangeParser()){
                results.addAll(createParser(expression).parseDataRangeList());
            }
            else if (isLiteralParser()){
                String[] strings = expression.split(SEPARATOR);
                for (String string : strings) {
                    string = string.trim();
                    results.add(createParser(string).parseConstant());
                }
            }
        }
        catch (ParserException e) {
            throw ParserUtil.convertException(e);
        }

        return results;
    }


    private ManchesterOWLSyntaxEditorParser createParser(String expression) {
        ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(mngr.getOWLDataFactory(), expression);
        parser.setOWLEntityChecker(new ProtegeOWLEntityChecker(mngr));
        parser.setBase(mngr.getActiveOntology().getURI().toString());
        return parser;
    }


    protected boolean isLiteralParser() {
        return (types != null && types.contains(LITERAL));
    }

    protected boolean isObjectPropertyParser() {
        return (types != null && types.contains(OBJPROP));
    }

    protected boolean isDataPropertyParser() {
        return (types != null && types.contains(DATAPROP));
    }

    protected boolean isIndividualParser() {
        return (types != null && types.contains(INDIVIDUAL));
    }

    protected boolean isClassParser() {
        return (types != null && types.contains(CLASS));
    }

    protected boolean isDescriptionParser() {
        return (types != null && types.contains(DESCRIPTION));
    }

    protected boolean isDataRangeParser() {
        return (types != null && types.contains(DATARANGE));
    }

    public void setTypes(Set<Integer> types) {
        this.types = types;
    }
}
