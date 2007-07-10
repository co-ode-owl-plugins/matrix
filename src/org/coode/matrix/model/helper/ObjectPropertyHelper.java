package org.coode.matrix.model.helper;

import org.semanticweb.owl.model.*;
import org.protege.editor.owl.model.OWLModelManager;

import java.util.*;

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
public class ObjectPropertyHelper {

    public static final List<String> names = Arrays.asList(
            "Fun", "Sym", "InvF", "Tra", "Anti-Sym", "Refl", "Irrefl", "Domain", "Range", "Inverse");

    public static final int FUNCTIONAL = 0;
    public static final int SYMMETRIC = 1;
    public static final int INVERSE_FUNCTIONAL = 2;
    public static final int TRANSITIVE = 3;
    public static final int ANTI_SYMMETRIC = 4;
    public static final int REFLEXIVE = 5;
    public static final int IRREFLEXIVE = 6;
    public static final int DOMAIN = 7;
    public static final int RANGE = 8;
    public static final int INVERSE = 9;

    private static final int START_FEATURES = FUNCTIONAL;
    private static final int END_FEATURES = IRREFLEXIVE;
    public static final int AXIOM_TYPE_COUNT = INVERSE;

    private OWLModelManager mngr;

    public ObjectPropertyHelper(OWLModelManager mngr) {
        this.mngr = mngr;
    }

    public final boolean isCharacteristic(Object o) {
        if (o instanceof String){
            int feature = toIndex((String)o);
            return feature >= START_FEATURES && feature <= END_FEATURES;
        }
        return false;
    }

    public final boolean getPropertyCharacteristic(OWLObjectProperty p, String characteristic) {

        OWLAxiom searchAxiom = createAxiomOfType(p, names.indexOf(characteristic));

        for (OWLOntology ont : mngr.getActiveOntologies()){
            if (ont.getReferencingAxioms(p).contains(searchAxiom)){
                return true;
            }
        }
        return false;
    }

    private final OWLAxiom createAxiomOfType(OWLObjectProperty p, int type){
        mngr.getOWLDataFactory();
        switch (type) {
            case FUNCTIONAL:
                return mngr.getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(p);
            case INVERSE_FUNCTIONAL:
                return mngr.getOWLDataFactory().getOWLInverseFunctionalObjectPropertyAxiom(p);
            case SYMMETRIC:
                return mngr.getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(p);
            case TRANSITIVE:
                return mngr.getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(p);
            case ANTI_SYMMETRIC:
                return mngr.getOWLDataFactory().getOWLAntiSymmetricObjectPropertyAxiom(p);
            case REFLEXIVE:
                return mngr.getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(p);
            case IRREFLEXIVE:
                return mngr.getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(p);
        }
        return null;
    }

    public List<OWLOntologyChange> setPropertyCharacteristic(boolean value, OWLObjectProperty p,
                                                             String characteristic, OWLOntology activeOnt) {

        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        OWLAxiom newAxiom = createAxiomOfType(p, names.indexOf(characteristic));

        if (value){ // if the characteristic is set, make sure the axiom exists
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

    public List<OWLOntologyChange> setInverses(OWLObjectProperty property, Set<OWLObjectProperty> inverses,
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

    public List<OWLOntologyChange> setDomains(OWLObjectProperty property, Set<OWLDescription> domains,
                                              OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // create the set of axioms we want to end up with
        Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
        for (OWLDescription domain : domains){
            newAxioms.add(mngr.getOWLDataFactory().getOWLObjectPropertyDomainAxiom(property, domain));
        }

        // remove any domain axioms on this property that don't match the new ones
        // and filter existing ones out of the new axioms set
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLAxiom ax : ont.getReferencingAxioms(property)){
                if (ax instanceof OWLObjectPropertyDomainAxiom &&
                    ((OWLObjectPropertyDomainAxiom)ax).getProperty().equals(property)){
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

    public List<OWLOntologyChange> setRanges(OWLObjectProperty property, Set<OWLDescription> ranges, OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // create the set of axioms we want to end up with
        Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
        for (OWLDescription range : ranges){
            newAxioms.add(mngr.getOWLDataFactory().getOWLObjectPropertyRangeAxiom(property, range));
        }

        // remove any range axioms on this property that don't match the new ones
        // and filter existing ones out of the new axioms set
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLAxiom ax : ont.getReferencingAxioms(property)){
                if (ax instanceof OWLObjectPropertyRangeAxiom &&
                    ((OWLObjectPropertyRangeAxiom)ax).getProperty().equals(property)){
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

//    public List<String> getColumnNames() {
//        return names;
//    }

    public int toIndex(String colName) {
        return names.indexOf(colName);
    }

    public String toString(int feature) {
        return names.get(feature);
    }

    public Set<OWLDescription> getRanges(OWLObjectProperty p) {
        Set<OWLDescription> ranges = new HashSet<OWLDescription>();
        for (OWLOntology ont : mngr.getActiveOntologies()){
            ranges.addAll(p.getRanges(ont));
        }
        return ranges;
    }

    public Set<OWLDescription> getDomains(OWLObjectProperty p) {
        Set<OWLDescription> domains = new HashSet<OWLDescription>();
        for (OWLOntology ont : mngr.getActiveOntologies()){
            domains.addAll(p.getDomains(ont));
        }
        return domains;
    }

    public Set<OWLObjectProperty> getInverses(OWLObjectProperty p) {
        Set<OWLObjectProperty> inverses = new HashSet<OWLObjectProperty>();
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLObjectPropertyExpression inv : p.getInverses(ont)){
                if (inv instanceof OWLObjectProperty){
                    inverses.add((OWLObjectProperty)inv);
                }
            }
        }
        return inverses;
    }
}
