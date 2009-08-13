package org.coode.matrix.model.helper;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;

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

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 1, 2007<br><br>
 */
public abstract class AbstractQuantifiedRestrictionVisitorAdapter extends OWLObjectVisitorAdapter {

    private Set<OWLOntology> onts;
    private Set<OWLObject> visitedObjects = new HashSet<OWLObject>();

    public AbstractQuantifiedRestrictionVisitorAdapter(Set<OWLOntology> onts) {
        this.onts = onts;
    }

    public void visit(OWLClass cls){
        if (!visitedObjects.contains(cls)){
            visitedObjects.add(cls); // prevent cycles
            for (OWLOntology ont : onts){
                for (OWLAxiom subcls : ont.getSubClassAxiomsForSubClass(cls)){
                    subcls.accept(this);
                }
                for (OWLAxiom equivAxiom : ont.getEquivalentClassesAxioms(cls)){
                    equivAxiom.accept(this);
                }
            }
        }
    }

    public void visit(OWLObjectSomeValuesFrom restriction) {
        handleObjectRestriction(restriction);
    }

    public void visit(OWLObjectMinCardinality restriction) {
        handleCardinality(restriction);
    }

    public void visit(OWLObjectExactCardinality restriction) {
        handleCardinality(restriction);
    }


    public void visit(OWLObjectIntersectionOf owlObjectIntersectionOf) {
        if (!visitedObjects.contains(owlObjectIntersectionOf)){
            visitedObjects.add(owlObjectIntersectionOf);
            for (OWLClassExpression desc : owlObjectIntersectionOf.getOperands()) {
                desc.accept(this);
            }
        }
    }

    protected void handleCardinality(OWLObjectCardinalityRestriction restriction) {
        if (!visitedObjects.contains(restriction)){
            visitedObjects.add(restriction);
            if (restriction.getCardinality() > 0){
                handleObjectRestriction(restriction);
            }
        }
    }

    protected abstract void handleObjectRestriction(OWLQuantifiedRestriction<OWLObjectPropertyExpression, OWLClassExpression> restriction);
}
