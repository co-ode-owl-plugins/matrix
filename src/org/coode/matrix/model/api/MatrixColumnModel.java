package org.coode.matrix.model.api;

import org.coode.matrix.model.api.ContentsChangedListener;

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
class MatrixColumnModel<O> {

    List<O> values = new LinkedList<O>();

    private List<ContentsChangedListener<O>> listeners = new ArrayList<ContentsChangedListener<O>>();

    public O get(int index) {
        return values.get(index);
    }

    public int indexOf(O object) {
        return values.indexOf(object);
    }

    public int size() {
        return values.size();
    }

    public boolean contains(O value) {
        return values.contains(value);
    }

    public boolean add(O newValue) {
        boolean result = false;
        if (!values.contains(newValue)) {
            result = values.add(newValue);
            if (result) {
                notifyValuesAdded(Collections.singleton(newValue));
            }
        }
        return result;
    }

    public boolean add(O newValue, int position) {
        boolean result = false;
        if (!values.contains(newValue)) {
            if (values.size() < position) {
                values.add(newValue);
            }
            else {
                values.add(position, newValue);
            }
            notifyValuesAdded(Collections.singleton(newValue));
            result = true;
        }
        return result;
    }

    /**
     * Will only add unique values to the axis
     *
     * @param newValues
     * @return true if any values added
     */
    public boolean add(Collection<O> newValues) {
        Collection valuesAdded = new ArrayList();
        for (O value : newValues) {
            if (add(value)) {
                valuesAdded.add(value);
            }
        }
        if (valuesAdded.size() > 0) {
            notifyValuesAdded(newValues);
        }
        return (valuesAdded.size() > 0);
    }

    public boolean remove(O value) {
        boolean result = values.remove(value);
        if (result) {
            notifyValuesRemoved(Collections.singleton(value));
        }
        return result;
    }

    public boolean remove(Collection<O> oldValues) {
        boolean result = values.removeAll(oldValues);
        if (result) {
            notifyValuesRemoved(oldValues);
        }
        return result;
    }

///////////  handle contents changed listeners

    public void addContentsChangedListener(ContentsChangedListener<O> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeContentsChangedListener(ContentsChangedListener<O> listener) {
        listeners.remove(listener);
    }

    private void notifyValuesAdded(Collection<O> newValues) {
        for (ContentsChangedListener<O> listener : listeners) {
            listener.valuesAdded(newValues);
        }
    }

    private void notifyValuesRemoved(Collection<O> oldValues) {
        for (ContentsChangedListener<O> listener : listeners) {
            listener.valuesRemoved(oldValues);
        }
    }
}
