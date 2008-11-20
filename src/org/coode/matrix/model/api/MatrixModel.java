package org.coode.matrix.model.api;

import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntologyChange;
import uk.ac.manchester.cs.bhig.jtreetable.TreeTableModel;

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
public interface MatrixModel<R extends OWLObject> extends TreeTableModel<R> {

    String getTreeColumnLabel();

    Object getMatrixValue(R rowObject, Object columnObject);

    List<OWLOntologyChange> setMatrixValue(R rowObj, Object columnObj, Object value);

    List<OWLOntologyChange> addMatrixValue(R rowObj, Object columnObj, Object value);

    boolean isSuitableCellValue(Object value, int row, int col);

    Object getSuitableColumnObject(Object columnObject);

    boolean isValueRestricted(R rowObject, Object columnObject);

    Set getSuggestedFillers(R rowObject, Object columnObject, int threshold);

//    void setFilterForColumn(Object columnObject, Object filter);
//
//    Object getFilterForColumn(Object obj);

    void dispose();
}
