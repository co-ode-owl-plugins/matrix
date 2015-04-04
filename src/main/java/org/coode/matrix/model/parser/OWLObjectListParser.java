package org.coode.matrix.model.parser;

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

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 19, 2008<br><br>
 */
public class OWLObjectListParser {

    private static final String SEPARATOR = ",";

    public enum ParseType{
        LITERAL,
        CLASS,
        OBJPROP,
        DATAPROP,
        INDIVIDUAL,
        DESCRIPTION,
        DATARANGE,
        ANNOTATIONPROP
    }

    private Set<ParseType> types;

    private OWLModelManager mngr;


    public OWLObjectListParser(OWLModelManager mngr) {
        this.mngr = mngr;
    }


    public void setTypes(Set<ParseType> types) {
        this.types = types;
    }

    
    public boolean isWellFormed(String expression) throws OWLExpressionParserException {
        getValues(expression);
        return true;
    }


    public Set<OWLObject> getValues(String expression) throws OWLExpressionParserException {
        Set<OWLObject> results = new HashSet<OWLObject>();
        try {

            for (ParseType type : types){
                switch(type){
                    case LITERAL:
                        String[] strings = expression.split(SEPARATOR);
                        for (String string : strings) {
                            string = string.trim();
                            try{
                                results.add(createParser(string).parseLiteral(null));
                            }
                            catch (ParserException e) {
                                results.add(mngr.getOWLDataFactory().getOWLLiteral(string));
                            }
                        }
                        break;
                    case CLASS:
                        results.addAll(createParser(expression).parseClassExpressionList());
                        break;
                    case OBJPROP:
                        results.addAll(createParser(expression).parseObjectPropertyList());
                        break;
                    case DATAPROP:
                        results.addAll(createParser(expression).parseDataPropertyList());
                        break;
                    case INDIVIDUAL:
                        results.addAll(createParser(expression).parseIndividualList());
                        break;
                    case DESCRIPTION:
                        results.addAll(createParser(expression).parseClassExpressionList());
                        break;
                    case DATARANGE:
                        results.addAll(createParser(expression).parseDataRangeList());
                        break;
                    case ANNOTATIONPROP:
                        results.addAll(createParser(expression).parseAnnotationPropertyList());
                        break;
                }
            }
        }
        catch (ParserException e) {
            throw ParserUtil.convertException(e);
        }

        return results;
    }


    private ManchesterOWLSyntaxParser createParser(String expression) {
        ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
        parser.setStringToParse(expression);
        parser.setOWLEntityChecker(new ProtegeOWLEntityChecker(mngr.getOWLEntityFinder()));
        return parser;
    }
}
