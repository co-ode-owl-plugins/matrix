package org.coode.matrix.model.impl;

import org.coode.matrix.model.api.AbstractMatrixModel;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.vocab.OWLRDFVocabulary;

import java.net.URI;
import java.util.Collections;
import java.util.List;
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
public class ClassTranslationTreeMatrixModel extends AbstractMatrixModel<OWLClass> {

    public ClassTranslationTreeMatrixModel(OWLObjectTree<OWLClass> tree, OWLModelManager mngr) {
        super(tree, mngr);

        addColumn(new AnnotationLanguagePair(OWLRDFVocabulary.RDFS_LABEL.getURI(), ""));
        addColumn(new AnnotationLanguagePair(OWLRDFVocabulary.RDFS_LABEL.getURI(), "en"));
    }

    protected String getTreeColumnLabel() {
        return "Class";
    }

    public boolean isSuitableCellValue(Object value, int row, int col) {
        return value instanceof String;
    }

    public boolean isSuitableColumnObject(Object columnObject) {
        return columnObject instanceof URI;
    }

    public boolean isValueRestricted(OWLClass cls, Object annot) {
        return false;
    }


    public Object getMatrixValue(OWLClass rowObj, Object columnObj) {
        return annotHelper.getAnnotationValues(rowObj,
                                        ((AnnotationLanguagePair)columnObj).getAnnotURI(),
                                        ((AnnotationLanguagePair)columnObj).getLang());
    }

    public List<OWLOntologyChange> setMatrixValue(OWLClass rowObj, Object columnObj, Object value) {
        return super.setMatrixValue(rowObj, columnObj, value);    //@@TODO replace call to super
    }

    public Set getSuggestedFillers(OWLClass cls, Object annot, int threshold) {
        return Collections.EMPTY_SET;
    }

    class AnnotationLanguagePair{

        private URI annotURI;
        private String lang;

        public AnnotationLanguagePair(URI annotURI, String lang) {
            this.annotURI = annotURI;
            this.lang = lang;
        }

        public URI getAnnotURI() {
            return annotURI;
        }

        public String getLang() {
            return lang;
        }

        public String toString(){
            return annotURI + "(" + lang + ")";
        }
    }
}
