package org.coode.matrix.model.helper;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.semanticweb.owl.model.*;

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
public class FillerHelper {

    private OWLModelManager mngr;

    private PropertyHelper objPropHelper;

    private OWLObjectHierarchyProvider<OWLClass> hp;


    public FillerHelper(OWLModelManager mngr, OWLObjectHierarchyProvider<OWLClass> hp) {
        this.mngr = mngr;
        this.hp = hp;
        this.objPropHelper = new PropertyHelper(mngr);
    }


    public <P extends OWLPropertyExpression, R extends OWLPropertyRange> Set<R> getAssertedFillers(OWLClass cls, P prop, Class<? extends OWLQuantifiedRestriction<P, R>> type) {
        Set<R> namedFillers = new HashSet<R>();

        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLSubClassAxiom ax : ont.getSubClassAxiomsForLHS(cls)){
                final OWLDescription superCls = ax.getSuperClass();
                if (type.isAssignableFrom(superCls.getClass())){
                    OWLQuantifiedRestriction<P, R> restr = type.cast(superCls);
                    if (restr.getProperty().equals(prop)){
                        R filler = restr.getFiller();
                        namedFillers.add(filler);
                    }
                }
            }
        }

        return namedFillers;
    }


    public <P extends OWLPropertyExpression, R extends OWLPropertyRange> Set<R> getInheritedNamedFillers(OWLClass cls, P prop, Class<? extends OWLQuantifiedRestriction<P, R>> type) {
        Set<R> namedFillers = new HashSet<R>();

        if (!cls.isOWLNothing()){
            for (OWLClass namedSuper : hp.getAncestors(cls)){
                namedFillers.addAll(getAssertedFillers(namedSuper, prop, type));
                namedFillers.addAll(getAssertedNamedFillersFromEquivs(namedSuper, prop, type));
            }
        }
        return namedFillers;
    }


    public <P extends OWLPropertyExpression, R extends OWLPropertyRange> Set<R> getAssertedNamedFillersFromEquivs(OWLClass cls, P prop, Class<? extends OWLQuantifiedRestriction<P, R>> type) {
        Set<R> namedFillers = new HashSet<R>();

        for (OWLDescription equiv : cls.getEquivalentClasses(mngr.getActiveOntologies())){
            if (equiv instanceof OWLObjectIntersectionOf){
                for (OWLDescription descr : ((OWLObjectIntersectionOf)equiv).getOperands()){
                    if (type.isAssignableFrom(descr.getClass())){
                        OWLQuantifiedRestriction<P, R> restr = type.cast(descr);
                        if (restr.getProperty().equals(prop)){
                            R filler = restr.getFiller();
                            namedFillers.add(filler);
                        }
                    }
                }
            }
            else{
                if (type.isAssignableFrom(equiv.getClass())){
                    OWLQuantifiedRestriction<P, R> restr = type.cast(equiv);
                    if (restr.getProperty().equals(prop)){
                        R filler = restr.getFiller();
                        namedFillers.add(filler);
                    }
                }
            }
        }
        return namedFillers;
    }


    public <P extends OWLPropertyExpression, R extends OWLPropertyRange> List<OWLOntologyChange> setFillers(OWLClass cls,
                                                                                                            P p,
                                                                                                            Set<R> fillers,
                                                                                                            Class<? extends OWLQuantifiedRestriction<P, R>> type,
                                                                                                            OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        // generate the axioms that should be in the ontology after this has completed
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (R filler : fillers){
            OWLRestriction restr = createRestriction(p, filler, type);
            OWLSubClassAxiom subclassAxiom = mngr.getOWLDataFactory().getOWLSubClassAxiom(cls, restr);
            axioms.add(subclassAxiom);
        }

        // go through the ontologies looking for restrictions if the given type that are not in the new axioms set that we can delete
        for (OWLOntology ont : mngr.getActiveOntologies()){
            for (OWLAxiom ax : ont.getReferencingAxioms(cls)){
                if (ax instanceof OWLSubClassAxiom && ((OWLSubClassAxiom)ax).getSubClass().equals(cls)){
                    if (axioms.contains(ax)){
                        axioms.remove(ax); // we're satisfied this axiom is already in the ontologies, no need to create or check against it
                    }
                    else{
                        OWLDescription supercls = ((OWLSubClassAxiom)ax).getSuperClass();
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


    public <P extends OWLPropertyExpression, R extends OWLPropertyRange> List<OWLOntologyChange> addNamedFillers(OWLClass cls,
                                                                                                                 P p,
                                                                                                                 Class<? extends OWLQuantifiedRestriction<P, R>> type,
                                                                                                                 Set<R> fillers,
                                                                                                                 OWLOntology activeOnt) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        for (R filler : fillers){
            OWLRestriction restr = createRestriction(p, filler, type);
            OWLSubClassAxiom subclassAxiom = mngr.getOWLDataFactory().getOWLSubClassAxiom(cls, restr);
            changes.add(new AddAxiom(activeOnt, subclassAxiom));
        }

        return changes;
    }


    private <P extends OWLPropertyExpression, R extends OWLPropertyRange> OWLRestriction createRestriction(P p, R filler, Class<? extends OWLQuantifiedRestriction<P, R>> restrType) {
        if (OWLObjectSomeRestriction.class.isAssignableFrom(restrType)){
            return mngr.getOWLDataFactory().getOWLObjectSomeRestriction((OWLObjectPropertyExpression)p, (OWLDescription)filler);
        }
        else if (OWLObjectAllRestriction.class.isAssignableFrom(restrType)){
            return mngr.getOWLDataFactory().getOWLObjectAllRestriction((OWLObjectPropertyExpression)p, (OWLDescription)filler);
        }
        else if (OWLDataSomeRestriction.class.isAssignableFrom(restrType)){
            return mngr.getOWLDataFactory().getOWLDataSomeRestriction((OWLDataPropertyExpression)p, (OWLDataRange)filler);
        }
        else if (OWLDataAllRestriction.class.isAssignableFrom(restrType)){
            return mngr.getOWLDataFactory().getOWLDataAllRestriction((OWLDataPropertyExpression)p, (OWLDataRange)filler);
        }
        return null;
    }


    //    /**
//     * Gets a set of named classes that would be sensible fillers for an existential restr
//     * on the given class, along the given property.
//     * <p/>
//     * Currently only takes into consideration the range of the property.
//     * In future, should look at other restrictions on the class
//     *
//     * @param cls            currently ignored
//     * @param p
//     * @param limit          on number of returned classes - if overstepped, returns null
//     * @return set of named classes if range exists
//     *         empty set if no range exists
//     *         null if limit is exceeded
//     * @throws OWLException
//     */
//    public Set<OWLClass> getSuggestedFillers(OWLClass cls, OWLObjectProperty p, int limit) {
//        // @@TODO check if supers have any restrs on this prop - if so, only restrict further
//
//        Set<OWLClass> possibleFillers = new HashSet<OWLClass>();
//
//        for (OWLDescription range : objPropHelper.getRanges(p)) {
//            if (!range.isAnonymous()) {
//                possibleFillers.add(range.asOWLClass());
//                possibleFillers.addAll(hp.getDescendants(range.asOWLClass()));
//            }
//
//            if (possibleFillers.size() > limit){
//                return Collections.EMPTY_SET;
//            }
//        }
//
//        // @@TODO if objectProp has an inverse, check things that cannot have this prop
//
//        return possibleFillers;
//    }


//    // if there is at least one named range, then return true
//    public boolean fillersRestricted(OWLObjectProperty p) {
//        final Set<OWLDescription> ranges = objPropHelper.getRanges(p);
//        for (OWLDescription range : ranges){
//            if (!range.isAnonymous()){
//                return true;
//            }
//        }
//        return false;
//    }

//    class NamedFillerExtractor extends AbstractQuantifiedRestrictionVisitorAdapter {
//
//        private Set<OWLDescription> fillers = new HashSet<OWLDescription>();
//        private OWLProperty p;
//
//        public NamedFillerExtractor(OWLProperty p, Set<OWLOntology> onts) {
//            super(onts);
//            this.p = p;
//        }
//
//        protected void handleObjectRestriction(OWLQuantifiedRestriction<OWLObjectPropertyExpression, OWLDescription> restriction) {
//            if (restriction.getProperty().equals(p) &&
//                restriction.getFiller() instanceof OWLClass){
//                fillers.add(restriction.getFiller());
//            }
//        }
//
//        public Set<OWLDescription> getFillers(){
//            return fillers;
//        }
//    }
}
