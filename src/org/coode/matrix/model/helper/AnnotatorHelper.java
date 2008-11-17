package org.coode.matrix.model.helper;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.OWLObjectVisitorAdapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
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
public class AnnotatorHelper {

    private OWLModelManager mngr;

    private AnnotationAxiomCreator annotationAxiomCreator;


    public AnnotatorHelper(OWLModelManager mngr) {
        this.mngr = mngr;
        this.annotationAxiomCreator = new AnnotationAxiomCreator();
    }


    public Set<OWLAnnotation> getAnnotations(OWLEntity entity, URI prop) {
        Set<OWLAnnotation> annots = new HashSet<OWLAnnotation>();
        for (OWLOntology ont : mngr.getActiveOntologies()) {
            annots.addAll(entity.getAnnotations(ont, prop));
        }
        return annots;
    }


    public Set<OWLObject> getAnnotationValues(OWLEntity entity, URI uri) {
        Set<OWLObject> values = new HashSet<OWLObject>();
        for (OWLAnnotation annot : getAnnotations(entity, uri)){
            values.add(annot.getAnnotationValue());
        }
        return values;
    }


    public List<OWLOntologyChange> setAnnotationValues(OWLEntity entity, URI uri,
                                                       Set<OWLObject> values,
                                                       OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // create the set of axioms we want to end up with
        Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
        for (OWLObject value : values){
            newAxioms.add(annotationAxiomCreator.getAxiom(entity, uri, value));
        }

        // remove any axioms on this property that don't match the new ones
        // and filter existing ones out of the new axioms set
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLEntityAnnotationAxiom ax : ont.getEntityAnnotationAxioms(entity)){
                if (ax.getAnnotation().getAnnotationURI().equals(uri)){
                    if (newAxioms.contains(ax)){
                        newAxioms.remove(ax); // don't need to create a new one
                    }
                    else {
                        changes.add(new RemoveAxiom(ont, ax));
                    }
                }
            }
        }

        // add any new axioms that don't already exist in the ontology
        for (OWLAxiom ax : newAxioms){
            changes.add(new AddAxiom(activeOnt, ax));
        }

        return changes;
    }

    public List<OWLOntologyChange> setAnnotationValues(OWLEntity entity, URI uri,
                                                       Set<OWLObject> values,
                                                       OWLOntology activeOnt,
                                                       String lang) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // create the set of axioms we want to end up with
        Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
        for (OWLObject value : values){
            newAxioms.add(annotationAxiomCreator.getAxiom(entity, uri, value));
        }

        // remove any axioms on this property that don't match the new ones
        // and filter existing ones out of the new axioms set
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLEntityAnnotationAxiom ax : ont.getEntityAnnotationAxioms(entity)){
                if (ax.getAnnotation().getAnnotationURI().equals(uri)){
                    if (ax.getAnnotation().getAnnotationValue() instanceof OWLUntypedConstant){
                        OWLUntypedConstant constant = (OWLUntypedConstant)ax.getAnnotation().getAnnotationValue();
                        if (lang.equals(constant.getLang()) ||
                            (lang.equals("!") && constant.getLang() == null)){
                            if (newAxioms.contains(ax)){
                                newAxioms.remove(ax); // don't need to create a new one
                            }
                            else {
                                changes.add(new RemoveAxiom(ont, ax));
                            }
                        }
                    }
                }
            }
        }

        // add any new axioms that don't already exist in the ontology
        for (OWLAxiom ax : newAxioms){
            changes.add(new AddAxiom(activeOnt, ax));
        }

        return changes;
    }

    public Set<OWLObject> getAnnotationValues(OWLEntity entity, URI uri, String lang) {
        Set<OWLObject> values = new HashSet<OWLObject>();
        for (OWLAnnotation annot : getAnnotations(entity, uri)){
            final OWLObject value = annot.getAnnotationValue();
            if (value instanceof OWLUntypedConstant){
                if (lang.equals(((OWLUntypedConstant)value).getLang())){
                    values.add(value);
                }
                else if (lang.equals("!") && ((OWLUntypedConstant)value).getLang() == null){
                    values.add(value);
                }
            }
        }
        return values;
    }


    class AnnotationAxiomCreator extends OWLObjectVisitorAdapter{
        private OWLEntity entity;
        private URI annotURI;
        private OWLAxiom axiom;

        public OWLAxiom getAxiom(OWLEntity entity, URI annotURI, OWLObject value){
            this.entity = entity;
            this.annotURI = annotURI;
            value.accept(this);
            return axiom;
        }

        public void visit(OWLTypedConstant owlTypedConstant) {
            OWLAnnotation annot = mngr.getOWLDataFactory().getOWLConstantAnnotation(annotURI, owlTypedConstant);
            axiom = mngr.getOWLDataFactory().getOWLEntityAnnotationAxiom(entity, annot);
        }

        public void visit(OWLUntypedConstant owlUntypedConstant) {
            OWLAnnotation annot = mngr.getOWLDataFactory().getOWLConstantAnnotation(annotURI, owlUntypedConstant);
            axiom = mngr.getOWLDataFactory().getOWLEntityAnnotationAxiom(entity, annot);
        }
    }
}
