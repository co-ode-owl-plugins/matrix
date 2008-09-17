package org.coode.matrix.model.helper;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
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
public class FillerHelper {

    private OWLModelManager mngr;

    private ObjectPropertyHelper objPropHelper;

    private OWLObjectHierarchyProvider<OWLClass> hp;


    public FillerHelper(OWLModelManager mngr, OWLObjectHierarchyProvider<OWLClass> hp) {
        this.mngr = mngr;
        this.hp = hp;
        this.objPropHelper = new ObjectPropertyHelper(mngr);
    }

    /**
     * Gets a set of named classes that would be sensible fillers for an existential restr
     * on the given class, along the given property.
     * <p/>
     * Currently only takes into consideration the range of the property.
     * In future, should look at other restrictions on the class
     *
     * @param cls            currently ignored
     * @param p
     * @param limit          on number of returned classes - if overstepped, returns null
     * @return set of named classes if range exists
     *         empty set if no range exists
     *         null if limit is exceeded
     * @throws OWLException
     */
    public Set<OWLClass> getSuggestedFillers(OWLClass cls, OWLObjectProperty p, int limit) {
        // @@TODO check if supers have any restrs on this prop - if so, only restrict further

        Set<OWLClass> possibleFillers = new HashSet<OWLClass>();

        for (OWLDescription range : objPropHelper.getRanges(p)) {
            if (!range.isAnonymous()) {
                possibleFillers.add(range.asOWLClass());
                possibleFillers.addAll(hp.getDescendants(range.asOWLClass()));
            }

            if (possibleFillers.size() > limit){
                return Collections.EMPTY_SET;
            }
        }

        // @@TODO if objectProp has an inverse, check things that cannot have this prop

        return possibleFillers;
    }


    // if there is at least one named range, then return true
    public boolean fillersRestricted(OWLObjectProperty p) {
        final Set<OWLDescription> ranges = objPropHelper.getRanges(p);
        for (OWLDescription range : ranges){
            if (!range.isAnonymous()){
                return true;
            }
        }
        return false;
    }

    public Set<OWLDescription> getAssertedNamedFillers(OWLClass cls, OWLObjectProperty prop) {
        Set<OWLDescription> namedFillers = new HashSet<OWLDescription>();

        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLSubClassAxiom ax : ont.getSubClassAxiomsForLHS(cls)){
                final OWLDescription superCls = ax.getSuperClass();
                if (superCls instanceof OWLObjectSomeRestriction){
                    if (((OWLObjectSomeRestriction)superCls).getProperty().equals(prop)){
                        OWLDescription filler = ((OWLObjectSomeRestriction) superCls).getFiller();
                        if (!filler.isAnonymous()){
                            namedFillers.add(filler);
                        }
                    }
                }
            }
        }

        return namedFillers;
    }


    public Set<OWLDescription> getInheritedNamedFillers(OWLClass cls, OWLObjectProperty prop) {
        Set<OWLDescription> namedFillers = new HashSet<OWLDescription>();

        for (OWLClass namedSuper : hp.getAncestors(cls)){
            namedFillers.addAll(getAssertedNamedFillers(namedSuper, prop));
            namedFillers.addAll(getAssertedNamedFillersFromEquivs(namedSuper, prop));
        }

        return namedFillers;
    }


    public List<OWLOntologyChange> setNamedFillers(Set<OWLDescription> fillers, OWLClass cls,
                                                   OWLObjectProperty p, OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // generate the axioms that should be in the ontology after this has completed
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (OWLDescription filler : fillers){
            OWLObjectSomeRestriction svf = mngr.getOWLDataFactory().getOWLObjectSomeRestriction(p, filler);
            OWLSubClassAxiom subclassAxiom = mngr.getOWLDataFactory().getOWLSubClassAxiom(cls, svf);
            axioms.add(subclassAxiom);
        }

        // go through the ontologies looking for svf restrictions not in this set that we can delete
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLAxiom ax : ont.getReferencingAxioms(cls)){
                if (ax instanceof OWLSubClassAxiom && ((OWLSubClassAxiom)ax).getSubClass().equals(cls)){
                    if (axioms.contains(ax)){
                        axioms.remove(ax); // we're satisfied this axiom is already in the ontologies, no need to create or check against it
                    }
                    else{
                        OWLDescription supercls = ((OWLSubClassAxiom)ax).getSuperClass();
                        if (supercls instanceof OWLObjectSomeRestriction){
                            if (((OWLObjectSomeRestriction)supercls).getProperty().equals(p)){
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


    public List<OWLOntologyChange> addNamedFillers(Set<OWLDescription> fillers, OWLClass cls, OWLObjectProperty p, OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        for (OWLDescription filler : fillers){
            OWLObjectSomeRestriction svf = mngr.getOWLDataFactory().getOWLObjectSomeRestriction(p, filler);
            OWLSubClassAxiom subclassAxiom = mngr.getOWLDataFactory().getOWLSubClassAxiom(cls, svf);
            changes.add(new AddAxiom(activeOnt, subclassAxiom));
        }

        return changes;
    }


    public Set<OWLDescription> getAssertedNamedFillersFromEquivs(OWLClass cls, OWLObjectProperty p) {
        NamedFillerExtractor extractor = new NamedFillerExtractor(p, mngr.getActiveOntologies());
            for (OWLDescription equiv : hp.getEquivalents(cls)){
                equiv.accept(extractor);
            }
        return extractor.getFillers();
    }


    class NamedFillerExtractor extends AbstractExistentialVisitorAdapter {

        private Set<OWLDescription> fillers = new HashSet<OWLDescription>();
        private OWLObjectProperty p;

        public NamedFillerExtractor(OWLObjectProperty p, Set<OWLOntology> onts) {
            super(onts);
            this.p = p;
        }

        protected void handleRestriction(OWLQuantifiedRestriction<OWLObjectPropertyExpression, OWLDescription> restriction) {
            if (restriction.getProperty().equals(p) &&
                restriction.getFiller() instanceof OWLClass){
                fillers.add(restriction.getFiller());
            }
        }

        public Set<OWLDescription> getFillers(){
            return fillers;
        }
    }
}
