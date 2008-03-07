package org.coode.matrix.model.helper;

import org.semanticweb.owl.model.*;

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
public class IndividualsHelper {

    private OWLOntologyManager mngr;
    private Set<OWLOntology> onts;

    public IndividualsHelper(OWLOntologyManager mngr, Set<OWLOntology> ontologies) {
        this.mngr = mngr;
        this.onts = ontologies;
    }

    public void setOntologies(Set<OWLOntology> onts){
        this.onts = onts;
    }

    public Set<OWLIndividual> getRelationships(OWLIndividual ind, OWLObjectProperty prop) {
        Set<OWLIndividual> values = null;
        Map<OWLObjectPropertyExpression, Set<OWLIndividual>> relationships = new HashMap<OWLObjectPropertyExpression, Set<OWLIndividual>>();
        for (OWLOntology ont : onts){
            relationships.putAll(ind.getObjectPropertyValues(ont));
        }
        if (!relationships.isEmpty()) {
            values = relationships.get(prop);
        }
        if (values == null) {
            values = Collections.emptySet();
        }
        return values;
    }


    public List<OWLOntologyChange> setRelationships(OWLIndividual subject, OWLObjectProperty p,
                                                    Set<OWLIndividual> objects, OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // generate the axioms that should be in the ontology after this has completed
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (OWLIndividual object : objects){
            OWLObjectPropertyAssertionAxiom relationAxiom =
                    mngr.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(subject, p, object);
            axioms.add(relationAxiom);
        }

        // go through the ontologies removing appropriate relations not in this set
        for (OWLOntology ont : onts){
            for (OWLAxiom ax : ont.getReferencingAxioms(subject)){
                if (ax instanceof OWLObjectPropertyAssertionAxiom &&
                    ((OWLObjectPropertyAssertionAxiom)ax).getSubject().equals(subject)){
                    if (axioms.contains(ax)){
                        axioms.remove(ax); // we're satisfied this axiom is already in the ontologies, no need to create or check against it
                    }
                    else{
                        if (((OWLObjectPropertyAssertionAxiom)ax).getProperty().equals(p)){
                            // we already know the object does not match otherwise it would be in the "axioms" set
                            changes.add(new RemoveAxiom(ont, ax));
                        }
                    }

                }
            }
        }

        // add all remaining new axioms to the active ontology
        for (OWLAxiom ax : axioms){
            changes.add(new AddAxiom(activeOnt, ax));
        }

        return changes;
    }

    public List<OWLOntologyChange> addRelationships(OWLIndividual subject, OWLObjectProperty p,
                                                    Set<OWLIndividual> objects, OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // generate the axioms that should be in the ontology after this has completed
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (OWLIndividual object : objects){
            OWLObjectPropertyAssertionAxiom relationAxiom =
                    mngr.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(subject, p, object);
            axioms.add(relationAxiom);
        }
        // add all remaining new axioms to the active ontology
        for (OWLAxiom ax : axioms){
            changes.add(new AddAxiom(activeOnt, ax));
        }
        return changes;
    }


    public Set<OWLIndividual> getMembers(OWLClass cls) {
        Set<OWLIndividual> instances = new HashSet<OWLIndividual>();

        final OWLClass thing = mngr.getOWLDataFactory().getOWLThing();
        if (cls.equals(thing)){
            for (OWLOntology ont : onts){
                for (OWLIndividual ind : ont.getReferencedIndividuals()){
                    if (ind.getTypes(ont).isEmpty() || ind.getTypes(ont).contains(thing)){
                        instances.add(ind);
                    }
                }
            }
        }
        else{
            for (OWLOntology ont : onts){
                for (OWLAxiom ax : ont.getReferencingAxioms(cls)){
                    if (ax instanceof OWLClassAssertionAxiom &&
                        ((OWLClassAssertionAxiom)ax).getDescription().equals(cls)){
                        instances.add(((OWLClassAssertionAxiom)ax).getIndividual());
                    }
                }
            }
        }
        return instances;
    }

    public List<OWLOntologyChange> setMembers(OWLClass cls, Set<OWLIndividual> members, OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // generate the axioms that should be in the ontology after this has completed
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (OWLIndividual member : members){
            OWLClassAssertionAxiom axiom = mngr.getOWLDataFactory().getOWLClassAssertionAxiom(member, cls);
            axioms.add(axiom);
        }

        // go through the ontologies removing appropriate relations not in this set
        for (OWLOntology ont : onts){
            for (OWLAxiom ax : ont.getReferencingAxioms(cls)){
                if (ax instanceof OWLClassAssertionAxiom &&
                    ((OWLClassAssertionAxiom)ax).getDescription().equals(cls)){
                    if (axioms.contains(ax)){
                        axioms.remove(ax); // we're satisfied this axiom is already in the ontologies, no need to create or check against it
                    }
                    else{
                        // we already know the member does not match otherwise it would be in the "axioms" set
                        changes.add(new RemoveAxiom(ont, ax));
                    }

                }
            }
        }

        // add all remaining new axioms to the active ontology
        for (OWLAxiom ax : axioms){
            changes.add(new AddAxiom(activeOnt, ax));
        }

        return changes;
    }


    public Set<OWLDescription> getTypes(OWLIndividual individual) {
        Set<OWLDescription> types = new HashSet<OWLDescription>();
        for (OWLOntology ont : onts){
            types.addAll(individual.getTypes(ont));
        }
        return types;
    }

    public Set<OWLClass> getNamedTypes(OWLIndividual individual) {
        Set<OWLClass>parents = Collections.emptySet();
        Set<OWLDescription> types = getTypes(individual);
        if (types.size() > 0) {
            parents = new HashSet<OWLClass>();
            for (OWLDescription type : types) {
                if (type instanceof OWLClass) {
                    parents.add((OWLClass) type);
                }
            }
        }
        return parents;
    }
}
