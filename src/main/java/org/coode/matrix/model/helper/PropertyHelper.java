package org.coode.matrix.model.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;

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
public class PropertyHelper {

    public static enum OWLPropertyCharacteristic {

        FUNCTIONAL("Func", true),
        SYMMETRIC("Sym", false),
        INVERSE_FUNCTIONAL("Inv Func", false),
        TRANSITIVE("Trans", false),
        ASYMMETRIC("ASym", false),
        REFLEXIVE("Refl", false),
        IRREFLEXIVE("Irrefl", false);

        private String label;

        private boolean isDataPropertyCharacteristic;


        OWLPropertyCharacteristic(String label, boolean isDataPropertyCharacteristic) {
            this.label = label;
            this.isDataPropertyCharacteristic = isDataPropertyCharacteristic;
        }


        public final OWLAxiom createAxiomOfType(OWLProperty p, OWLDataFactory df){
            if (p.isOWLObjectProperty()){
                switch (this) {
                    case FUNCTIONAL:
                        return df.getOWLFunctionalObjectPropertyAxiom(p.asOWLObjectProperty());
                    case INVERSE_FUNCTIONAL:
                        return df.getOWLInverseFunctionalObjectPropertyAxiom(p.asOWLObjectProperty());
                    case SYMMETRIC:
                        return df.getOWLSymmetricObjectPropertyAxiom(p.asOWLObjectProperty());
                    case TRANSITIVE:
                        return df.getOWLTransitiveObjectPropertyAxiom(p.asOWLObjectProperty());
                    case ASYMMETRIC:
                        return df.getOWLAsymmetricObjectPropertyAxiom(p.asOWLObjectProperty());
                    case REFLEXIVE:
                        return df.getOWLReflexiveObjectPropertyAxiom(p.asOWLObjectProperty());
                    case IRREFLEXIVE:
                        return df.getOWLIrreflexiveObjectPropertyAxiom(p.asOWLObjectProperty());
                }
            }
            else if (p.isOWLDataProperty()){
                if (this.equals(FUNCTIONAL)){
                    return df.getOWLFunctionalDataPropertyAxiom(p.asOWLDataProperty());
                }
            }
            return null;
        }


        public boolean isDataPropertyCharacteristic() {
            return isDataPropertyCharacteristic;
        }

        public static boolean isObjectPropertyCharacteristic() {
            return true;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static enum OWLPropertyFeature {

        DOMAIN("Domain", true),
        RANGE("Range", true),
        INVERSE("Inverse", false);

        private String label;

        private boolean isDataPropertyFeature;


        OWLPropertyFeature(String label, boolean isDataPropertyFeature) {
            this.label = label;
            this.isDataPropertyFeature = isDataPropertyFeature;
        }

        public boolean isDataPropertyFeature() {
            return isDataPropertyFeature;
        }

        public static boolean isObjectPropertyFeature() {
            return true;
        }

        @Override
        public String toString() {
            return label;
        }
    }


    private OWLModelManager mngr;


    public PropertyHelper(OWLModelManager mngr) {
        this.mngr = mngr;
    }


    public final boolean getPropertyCharacteristic(OWLProperty p, OWLPropertyCharacteristic OWLPropertyCharacteristic) {

        OWLAxiom searchAxiom = OWLPropertyCharacteristic.createAxiomOfType(p, mngr.getOWLDataFactory());

        for (OWLOntology ont : mngr.getActiveOntologies()){
            if (ont.getReferencingAxioms(p).contains(searchAxiom)){
                return true;
            }
        }
        return false;
    }



    public List<OWLOntologyChange> setPropertyCharacteristic(boolean value,
                                                             OWLProperty p,
                                                             OWLPropertyCharacteristic characteristic,
                                                             OWLOntology activeOnt) {

        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        OWLAxiom newAxiom = characteristic.createAxiomOfType(p, mngr.getOWLDataFactory());

        if (value){ // if the OWLPropertyCharacteristic is set, make sure the axiom exists
            for (OWLOntology ont : mngr.getActiveOntologies()){
                if (ont.getReferencingAxioms(p).contains(newAxiom)){
                    return Collections.EMPTY_LIST; // its OK, one of the ontologies already has this value set
                }
            }
            changes.add(new AddAxiom(activeOnt, newAxiom));
        }
        else{ // otherwise remove all of the axioms making this true
            for (OWLOntology ont : mngr.getActiveOntologies()){
                if (ont.getReferencingAxioms(p).contains(newAxiom)){
                    changes.add(new RemoveAxiom(ont, newAxiom));
                }
            }
        }

        return changes;
    }

    public List<OWLOntologyChange> setInverses(OWLObjectProperty property,
                                               Set<OWLObjectProperty> inverses,
                                               OWLOntology activeOnt) {

        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // create the set of axioms we want to end up with
        Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
        for (OWLObjectProperty inv : inverses){
            newAxioms.add(mngr.getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(property, inv));
        }

        // remove any inverse axioms on this property that don't match the new ones
        // and filter existing ones out of the new axioms set
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLAxiom ax : ont.getReferencingAxioms(property)){
                if (ax instanceof OWLInverseObjectPropertiesAxiom &&
                    ((OWLInverseObjectPropertiesAxiom)ax).getFirstProperty().equals(property)){
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

    public List<OWLOntologyChange> setDomains(OWLProperty property,
                                              Collection<OWLClassExpression> domains,
                                              OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // create the set of axioms we want to end up with
        Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
        for (OWLClassExpression domain : domains){
            if (property instanceof OWLObjectProperty){
                newAxioms.add(mngr.getOWLDataFactory().getOWLObjectPropertyDomainAxiom((OWLObjectProperty)property, domain));
            }
            else if (property instanceof OWLDataProperty){
                newAxioms.add(mngr.getOWLDataFactory().getOWLDataPropertyDomainAxiom((OWLDataProperty)property, domain));
            }
        }

        // remove any domain axioms on this property that don't match the new ones
        // and filter existing ones out of the new axioms set
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLAxiom ax : ont.getReferencingAxioms(property)){
                if (ax instanceof OWLPropertyDomainAxiom &&
                    ((OWLPropertyDomainAxiom)ax).getProperty().equals(property)){
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

    public List<OWLOntologyChange> setRanges(OWLObjectProperty property, Collection<OWLClassExpression> ranges, OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // create the set of axioms we want to end up with
        Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
        for (OWLClassExpression range : ranges){
                newAxioms.add(mngr.getOWLDataFactory().getOWLObjectPropertyRangeAxiom(property, range));
        }

        return computechanges(property, activeOnt, changes, newAxioms);
    }


    protected List<OWLOntologyChange> computechanges(OWLEntity property,
            OWLOntology activeOnt, List<OWLOntologyChange> changes,
            Set<OWLAxiom> newAxioms) {
        // remove any range axioms on this property that don't match the new ones
        // and filter existing ones out of the new axioms set
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLAxiom ax : ont.getReferencingAxioms(property)){
                if(ont.containsAxiom(ax, Imports.EXCLUDED,AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS)) {
                        newAxioms.remove(ax); // don't need to create a new one
                    }
                    else {
                        changes.add(new RemoveAxiom(ont, ax));
                }
            }
        }

        // add any new axioms that don't already exist in the ontology
        for (OWLAxiom ax : newAxioms){
            changes.add(new AddAxiom(activeOnt, ax));
        }

        return changes;
    }
    public List<OWLOntologyChange> setRanges(OWLDataProperty property, Collection<OWLDataRange> ranges, OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // create the set of axioms we want to end up with
        Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
        for (OWLDataRange range : ranges){
                newAxioms.add(mngr.getOWLDataFactory().getOWLDataPropertyRangeAxiom(property, range));
        }

        return computechanges(property, activeOnt, changes, newAxioms);
    }

    public Collection<OWLClassExpression> getRanges(OWLObjectProperty p) {
        return EntitySearcher.getRanges(p, mngr.getActiveOntologies());
    }

    public Collection<OWLClassExpression> getDomains(OWLObjectPropertyExpression p) {
        return EntitySearcher.getDomains(p, mngr.getActiveOntologies());
    }
    public Collection<OWLDataRange> getRanges(OWLDataProperty p) {
        return EntitySearcher.getRanges(p, mngr.getActiveOntologies());
    }

    public Collection<OWLClassExpression> getDomains(OWLDataProperty p) {
        return EntitySearcher.getDomains(p, mngr.getActiveOntologies());
    }
    public boolean isFunctional(OWLPropertyExpression p) {
        if(p.isObjectPropertyExpression()) {
        return EntitySearcher.isFunctional((OWLObjectPropertyExpression)p, mngr.getActiveOntologies());
        }
        return EntitySearcher.isFunctional((OWLDataProperty)p, mngr.getActiveOntologies());
    }
    public boolean isFunctional(OWLDataProperty p) {
        return EntitySearcher.isFunctional(p, mngr.getActiveOntologies());
    }

    public Set<OWLObjectProperty> getInverses(OWLObjectProperty p) {
        Set<OWLObjectProperty> inverses = new HashSet<OWLObjectProperty>();
        for (OWLObjectPropertyExpression inv : EntitySearcher.getInverses(p,
                mngr.getActiveOntologies())) {
            if (inv instanceof OWLObjectProperty) {
                inverses.add((OWLObjectProperty) inv);
            }
        }
        return inverses;
    }

}
