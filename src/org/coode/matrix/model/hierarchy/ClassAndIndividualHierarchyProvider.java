package org.coode.matrix.model.hierarchy;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProviderListener;
import org.protege.editor.owl.model.hierarchy.AbstractOWLObjectHierarchyProvider;
import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.OWLAxiomVisitorAdapter;
import org.coode.matrix.model.helper.IndividualsHelper;

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
public class ClassAndIndividualHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLEntity> {

    private OWLObjectHierarchyProvider<OWLClass> clsHP;

    private IndividualsHelper helper;

    private OWLAxiomVisitor addAxiomVisitor = new OWLAxiomVisitorAdapter(){
        public void visit(OWLClassAssertionAxiom owlClassAssertionAxiom) {
            OWLDescription type = owlClassAssertionAxiom.getDescription();
            if (type instanceof OWLClass){
                fireNodeChanged((OWLClass)type);
            }
        }
    };

    private OWLOntologyChangeListener ontChangeListener = new OWLOntologyChangeListener() {

        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
            for (OWLOntologyChange change : changes){
                change.getAxiom().accept(addAxiomVisitor);
            }
        }
    };

    public ClassAndIndividualHierarchyProvider(OWLOntologyManager mngr,
                                               OWLObjectHierarchyProvider<OWLClass> clsHP,
                                               Set<OWLOntology> ontologies) {
        super(mngr);

        this.clsHP = clsHP;
        this.helper = new IndividualsHelper(mngr, ontologies);

        mngr.addOntologyChangeListener(ontChangeListener);
    }

    public void setOntologies(Set<OWLOntology> ontologies) {
        helper.setOntologies(ontologies);
    }

    public void dispose() {
        super.dispose();
        getManager().removeOntologyChangeListener(ontChangeListener);
        clsHP = null;
    }

    public Set<OWLEntity> getRoots() {
        Set<OWLEntity> roots = new HashSet<OWLEntity>();
        roots.addAll(clsHP.getRoots());
        return roots;
    }

    public Set<OWLEntity> getChildren(OWLEntity object) {
        Set<OWLEntity> children = new HashSet<OWLEntity>();
        if (object instanceof OWLClass) {
            children.addAll(clsHP.getChildren((OWLClass) object));
            children.addAll(helper.getMembers((OWLClass) object));
        }
        return children;
    }

    public Set<OWLEntity> getParents(OWLEntity object) {
        if (object instanceof OWLClass) {
            return new HashSet<OWLEntity>(clsHP.getParents((OWLClass) object));
        }
        else if (object instanceof OWLIndividual) {
            return new HashSet<OWLEntity>(helper.getNamedTypes((OWLIndividual) object));
        }
        return Collections.emptySet();
    }

    public Set<OWLEntity> getEquivalents(OWLEntity object) {
        if (object instanceof OWLClass) {
            return new HashSet<OWLEntity>(clsHP.getEquivalents((OWLClass) object));
        }
        else {
            return Collections.emptySet();
        }
    }

    public boolean containsReference(OWLEntity object) {
        return object instanceof OWLClass || object instanceof OWLIndividual;
    }


    public void addListener(OWLObjectHierarchyProviderListener listener) {
        clsHP.addListener(listener);
        super.addListener(listener);
    }

    public void removeListener(OWLObjectHierarchyProviderListener listener) {
        clsHP.removeListener(listener);
        super.removeListener(listener);
    }
}
