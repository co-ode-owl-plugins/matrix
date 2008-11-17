package org.coode.matrix.model.parser;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.description.OWLExpressionParserException;
import org.protege.editor.owl.model.description.manchester.DataTypeMapper;
import org.protege.editor.owl.model.description.manchester.DataTypeMapperImpl;
import org.protege.editor.owl.model.description.manchester.EntityMapper;
import org.protege.editor.owl.model.description.manchester.EntityMapperImpl;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owl.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
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
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 3, 2007<br><br>
 */
public class OWLObjectListParser {

    private static final String SEPARATOR = ",";
    public static final int DATATYPE = 0;
    public static final int CLASS = 1;
    public static final int OBJPROP = 2;
    public static final int DATAPROP = 3;
    public static final int INDIVIDUAL = 4;
    public static final int DESCRIPTION = 5;

    private EntityMapper entityMapper;

    private Set<Integer> types;
    private OWLModelManager mngr;


    public OWLObjectListParser(OWLModelManager mngr) {
        this.mngr = mngr;
        entityMapper = new EntityMapperImpl(mngr);
    }

    public boolean isWellFormed(String expression) throws OWLExpressionParserException {
        getValues(expression);
        return true;
    }


    public Set<OWLObject> getValues(String expression) throws OWLExpressionParserException {
        Set<OWLObject> values = new HashSet<OWLObject>();
        if (expression.length() > 0) {
            final OWLExpressionChecker<OWLDescription> checker = mngr.getOWLExpressionCheckerFactory().getOWLDescriptionChecker();
            String[] strings = expression.split(SEPARATOR);
            for (String string : strings) {
                string = string.trim();
                if (isDescriptionParser()){
                    values.add(checker.createObject(string));
                }
                else if (isDatatypeParser()) {
                    values.add(parseDatatype(string));
                }
                else{
                    OWLEntity oe = entityMapper.getOWLEntity(string);
                    if (oe != null) {
                        if ((isClassParser() && oe instanceof OWLClass) ||
                            (isIndividualParser() && oe instanceof OWLIndividual) ||
                            (isObjectPropertyParser() && oe instanceof OWLObjectProperty) ||
                            (isDatatypePropertyParser() && oe instanceof OWLDataProperty)) {
                            values.add(oe);
                        }
                    }
                    else {
                        int tokenStartIndex = expression.indexOf(string);
                        throw new OWLExpressionParserException("Cannot find entity to match " + string,
                                                               tokenStartIndex,
                                                               tokenStartIndex + 1,
                                                               isClassParser(),
                                                               isObjectPropertyParser(),
                                                               isDatatypePropertyParser(),
                                                               isIndividualParser(),
                                                               isDatatypeParser(),
                                                               new HashSet<String>());
                    }
                }
            }
        }
        return values;
    }

    private OWLObject parseDatatype(String string) throws OWLExpressionParserException {
        Pattern p = Pattern.compile("\"(.+)\"((@([a-z0-9]+))|(\\^\\^(.+)))?");
        Matcher m = p.matcher(new StringBuffer(string));
        if (m.matches()){
            String value = m.group(1);
            String lang = m.group(4);
            String type = m.group(6);

            if (type == null){
            return mngr.getOWLDataFactory().getOWLUntypedConstant(value, lang);
            }
            else{
                DataTypeMapper datatypeMapper = new DataTypeMapperImpl();
                URI dtURI = datatypeMapper.getOWLDataTypeURI(type);
                if (dtURI != null){
                    OWLDataType dt = mngr.getOWLDataFactory().getOWLDataType(dtURI);
                    return mngr.getOWLDataFactory().getOWLTypedConstant(value, dt);
                }
                else{
                    throw new OWLExpressionParserException("Cannot match datatype " + string,
                                               m.start(6),
                                               m.start(6)+1,
                                               isClassParser(),
                                               false,
                                               false,
                                               false,
                                               true,
                                               new HashSet<String>());
                }
            }
        }
        else{
            return mngr.getOWLDataFactory().getOWLUntypedConstant(string);
        }
//            throw new OWLExpressionParserException("Cannot match data " + string,
//                                               0,
//                                               1,
//                                               isClassParser(),
//                                               isObjectPropertyParser(),
//                                               isDatatypePropertyParser(),
//                                               isIndividualParser(),
//                                               isDatatypeParser(),
//                                               new HashSet<String>());
    }

    public static void main(String[] args){
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {

                System.out.println("%nEnter your regex: ");
                Pattern pattern =
                        null;
                pattern = Pattern.compile(br.readLine());
                Matcher matcher =
                        pattern.matcher("\"some text\"^^String");

                boolean found = false;
                while (matcher.find()) {
                    System.out.println("I found the text [" + matcher.group() + "] starting at " +
                                   "index " + matcher.start() + " and ending at index "+ matcher.end());
                    for (int i=0; i<=matcher.groupCount(); i++){
                        System.out.println("matcher.group(" + i + ") = " + matcher.group(i));
                    }
                    found = true;
                }
                if(!found){
                    System.out.println("NO match");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
//
//    public static void main(String[] args) {
//        final String s = "\"some text\"@pt";
//
//
//        Pattern p = Pattern.compile("\\\"([.]+)\\\"(@([a-z,0-9]+))??");
//        Matcher m = p.matcher(new StringBuffer(s));
//        if (m.matches()){
//            String value = m.group(0);
////                String langUsed = m.group(1);
//            String lang = m.group(2);
//
//            System.out.println("value = " + value);
//            System.out.println("lang = " + lang);
//        }
//        else{
//            m.
//        }
//    }

    protected boolean isDatatypeParser() {
        return (types != null && types.contains(DATATYPE));
    }

    protected boolean isObjectPropertyParser() {
        return (types != null && types.contains(OBJPROP));
    }

    protected boolean isDatatypePropertyParser() {
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

    public void setTypes(Set<Integer> types) {
        this.types = types;
    }
}
