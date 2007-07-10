package org.coode.matrix.ui.renderer;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRenderer;
import org.semanticweb.owl.model.OWLObject;

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
public class OWLObjectsRenderer {

    private OWLModelManager mngr;

    public OWLObjectsRenderer(OWLModelManager mngr) {
        this.mngr = mngr;
    }

    public String render(Set<OWLObject> objects) {
        if (objects != null && objects.size() > 0) {
            StringBuffer str = new StringBuffer();
            OWLObjectRenderer ren = mngr.getOWLObjectRenderer();
            OWLEntityRenderer ren2 = mngr.getOWLEntityRenderer();
            for (OWLObject obj : objects) {
                str.append(", ");
                str.append(ren.render(obj, ren2));
            }
            return str.substring(2);
        }
        return "";
    }
}
