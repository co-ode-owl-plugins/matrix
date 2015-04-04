package org.coode.matrix.model.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
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
public class FillerHelper {

    private OWLModelManager mngr;

    private OWLObjectHierarchyProvider<OWLClass> hp;


    public FillerHelper(OWLModelManager mngr, OWLObjectHierarchyProvider<OWLClass> hp) {
        this.mngr = mngr;
        this.hp = hp;
    }


    public <R extends OWLPropertyRange, P extends OWLPropertyExpression> Set<R> getAssertedFillers(OWLClass cls, P prop, Class<? extends OWLQuantifiedRestriction<R>> type) {
        Set<R> namedFillers = new HashSet<R>();

        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(cls)){
                final OWLClassExpression superCls = ax.getSuperClass();
                if (type.isAssignableFrom(superCls.getClass())){
                    OWLQuantifiedRestriction<R> restr = type.cast(superCls);
                    if (restr.getProperty().equals(prop)){
                        R filler = restr.getFiller();
                        namedFillers.add(filler);
                    }
                }
            }
        }

        return namedFillers;
    }


    public <R extends OWLPropertyRange, P extends OWLPropertyExpression> Set<R> getInheritedNamedFillers(OWLClass cls, P prop, Class<? extends OWLQuantifiedRestriction<R>> type) {
        Set<R> namedFillers = new HashSet<R>();

        if (!cls.isOWLNothing()){
            for (OWLClass namedSuper : hp.getAncestors(cls)){
                namedFillers.addAll(getAssertedFillers(namedSuper, prop, type));
                namedFillers.addAll(getAssertedNamedFillersFromEquivs(namedSuper, prop, type));
            }
        }
        return namedFillers;
    }


    public <R extends OWLPropertyRange, P extends OWLPropertyExpression> Set<R> getAssertedNamedFillersFromEquivs(OWLClass cls, P prop, Class<? extends OWLQuantifiedRestriction<R>> type) {
        Set<R> namedFillers = new HashSet<R>();

        for (OWLClassExpression equiv : EntitySearcher.getEquivalentClasses(cls, mngr.getActiveOntologies())){
            if (equiv instanceof OWLObjectIntersectionOf){
                for (OWLClassExpression descr : ((OWLObjectIntersectionOf)equiv).getOperands()){
                    if (type.isAssignableFrom(descr.getClass())){
                        OWLQuantifiedRestriction<R> restr = type.cast(descr);
                        if (restr.getProperty().equals(prop)){
                            R filler = restr.getFiller();
                            namedFillers.add(filler);
                        }
                    }
                }
            }
            else{
                if (type.isAssignableFrom(equiv.getClass())){
                    OWLQuantifiedRestriction<R> restr = type.cast(equiv);
                    if (restr.getProperty().equals(prop)){
                        R filler = restr.getFiller();
                        namedFillers.add(filler);
                    }
                }
            }
        }
        return namedFillers;
    }


    public <R extends OWLPropertyRange, P extends OWLPropertyExpression> List<OWLOntologyChange> setFillers(OWLClass cls,
                                                                                                                 P p,
                                                                                                                 Set<R> fillers,
                                                                                                                 Class<? extends OWLQuantifiedRestriction<R>> type,
                                                                                                                 OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // generate the axioms that should be in the ontology after this has completed
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (R filler : fillers){
            OWLRestriction restr = createRestriction(p, filler, type);
            OWLSubClassOfAxiom subclassAxiom = mngr.getOWLDataFactory().getOWLSubClassOfAxiom(cls, restr);
            axioms.add(subclassAxiom);
        }

        // go through the ontologies looking for restrictions if the given type that are not in the new axioms set that we can delete
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLAxiom ax : ont.getReferencingAxioms(cls)){
                if (ax instanceof OWLSubClassOfAxiom && ((OWLSubClassOfAxiom)ax).getSubClass().equals(cls)){
                    if (axioms.contains(ax)){
                        axioms.remove(ax); // we're satisfied this axiom is already in the ontologies, no need to create or check against it
                    }
                    else{
                        OWLClassExpression supercls = ((OWLSubClassOfAxiom)ax).getSuperClass();
                        if (type.isAssignableFrom(supercls.getClass())){
                            if (((OWLRestriction)supercls).getProperty().equals(p)){
                                // we already know the filler does not match otherwise it would be in the "axioms" set
                                changes.add(new RemoveAxiom(ont, ax));
                            }
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


    public <R extends OWLPropertyRange, P extends OWLPropertyExpression> List<OWLOntologyChange> addNamedFillers(OWLClass cls,
                                                                                                                 P p,
                                                                                                                      Class<? extends OWLQuantifiedRestriction<R>> type,
                                                                                                                 Set<R> fillers,
                                                                                                                 OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        for (R filler : fillers){
            OWLRestriction restr = createRestriction(p, filler, type);
            OWLSubClassOfAxiom subclassAxiom = mngr.getOWLDataFactory().getOWLSubClassOfAxiom(cls, restr);
            changes.add(new AddAxiom(activeOnt, subclassAxiom));
        }

        return changes;
    }


    
    private <R extends OWLPropertyRange, P extends OWLPropertyExpression> OWLRestriction createRestriction(P p, R filler, Class<? extends OWLQuantifiedRestriction<R>> restrType) {
        /* This project is an advertisement against generics.
         * 
         * The java compiler can be upset about converting and OWLPropertyExpression to an OWLObjectProperty.
         * It doesn't like the generics.
         */
    	Object pAsAnObject = p;
    	if (OWLObjectSomeValuesFrom.class.isAssignableFrom(restrType)){
            return mngr.getOWLDataFactory().getOWLObjectSomeValuesFrom((OWLObjectPropertyExpression) pAsAnObject, (OWLClassExpression)filler);
        }
        else if (OWLObjectAllValuesFrom.class.isAssignableFrom(restrType)){
            return mngr.getOWLDataFactory().getOWLObjectAllValuesFrom((OWLObjectPropertyExpression)pAsAnObject, (OWLClassExpression)filler);
        }
        else if (OWLDataSomeValuesFrom.class.isAssignableFrom(restrType)){
            return mngr.getOWLDataFactory().getOWLDataSomeValuesFrom((OWLDataPropertyExpression)pAsAnObject, (OWLDataRange)filler);
        }
        else if (OWLDataAllValuesFrom.class.isAssignableFrom(restrType)){
            return mngr.getOWLDataFactory().getOWLDataAllValuesFrom((OWLDataPropertyExpression)pAsAnObject, (OWLDataRange)filler);
        }
        return null;
    }



}
