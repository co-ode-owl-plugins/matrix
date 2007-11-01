package org.coode.matrix.model.impl;

import org.coode.matrix.model.helper.FillerHelper;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectProperty;

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
public class FillerModel {

    private OWLClass cls;
    private OWLObjectProperty p;
    private FillerHelper helper;

    public FillerModel(OWLClass cls, OWLObjectProperty p, FillerHelper helper){
        this.cls = cls;
        this.p = p;
        this.helper = helper;
    }

    public Set<OWLDescription> getAssertedFillersFromSupers(){
        return helper.getAssertedNamedFillers(cls, p);
    }

    public Set<OWLDescription> getInheritedFillers(){
        return helper.getInheritedNamedFillers(cls, p);
    }

    public Set<OWLDescription> getAssertedFillersFromEquiv(){
        return helper.getAssertedNamedFillersFromEquivs(cls, p);
    }
}
