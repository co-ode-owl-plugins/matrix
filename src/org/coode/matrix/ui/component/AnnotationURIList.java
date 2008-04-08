package org.coode.matrix.ui.component;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.vocab.DublinCoreVocabulary;
import org.semanticweb.owl.vocab.OWLRDFVocabulary;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
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
 * Date: Jul 5, 2007<br><br>
 *
 * Extracted from OWLAnnotationEditor
 */
public class AnnotationURIList extends MList {

    private OWLEditorKit eKit;
    private URI uriBeingAdded;

    public AnnotationURIList(OWLEditorKit eKit) {
        super();

        this.eKit = eKit;

        setCellRenderer(new OWLCellRenderer(eKit) {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                setTransparent();
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        rebuildAnnotationURIList();
    }

    protected void handleAdd() {
                handleAddURI();
            }

    public URI getSelectedURI() {
        Object selVal = getSelectedValue();
        if (selVal instanceof URIItem) {
            return ((URIItem) selVal).uri;
        }
        return null;
    }

    private void handleAddURI() {
        try {
            String uriString = JOptionPane.showInputDialog(this,
                                                           "Please specify an annotation URI",
                                                           "Annotation URI",
                                                           JOptionPane.PLAIN_MESSAGE);
            if (uriString != null) {
                URI uri = new URI(uriString);
                if (!uri.isAbsolute()) {
                    uri = URI.create(eKit.getModelManager().getActiveOntology().getURI() + "#" + uri.toString());
                }
                uriBeingAdded = uri;
            }
            rebuildAnnotationURIList();
        }
        catch (URISyntaxException e) {
            ProtegeApplication.getErrorLog().handleError(Thread.currentThread(), e);            
        }
    }

    private void rebuildAnnotationURIList() {
        // Custom
        // Built in
        // Dublin core

        java.util.List list = new ArrayList();

        java.util.List<URIItem> custom = new ArrayList<URIItem>();
        Set<URI> customURIs = new HashSet<URI>();
        for (OWLOntology ont : eKit.getOWLModelManager().getOntologies()) {
            customURIs.addAll(ont.getAnnotationURIs());
        }
        if (uriBeingAdded != null) {
            customURIs.add(uriBeingAdded);
        }
        customURIs.removeAll(OWLRDFVocabulary.BUILT_IN_ANNOTATION_PROPERTIES);
        customURIs.removeAll(DublinCoreVocabulary.ALL_URIS);
        for (URI uri : customURIs) {
            custom.add(new URIItem(uri));
        }
        list.add(new MListSectionHeader() {
            public String getName() {
                return "Custom annotation URIs";
            }


            public boolean canAdd() {
                return true;
            }
        });


        Collections.sort(custom);
        list.addAll(custom);

        list.add(new MListSectionHeader() {
            public String getName() {
                return "Built in annotation URIs";
            }


            public boolean canAdd() {
                return false;
            }
        });

        java.util.List<URIItem> builtIn = new ArrayList<URIItem>();
        for (URI uri : OWLRDFVocabulary.BUILT_IN_ANNOTATION_PROPERTIES) {
            builtIn.add(new URIItem(uri));
        }
        Collections.sort(builtIn);
        list.addAll(builtIn);


        list.add(new MListSectionHeader() {
            public String getName() {
                return "Dublin Core annotation URIs";
            }


            public boolean canAdd() {
                return false;
            }
        });

        java.util.List<URIItem> dublinCore = new ArrayList<URIItem>();
        for (URI uri : DublinCoreVocabulary.ALL_URIS) {
            dublinCore.add(new URIItem(uri));
        }
        Collections.sort(dublinCore);
        list.addAll(dublinCore);


        setListData(list.toArray());
        if (uriBeingAdded != null) {
            setSelectedURI(uriBeingAdded);
        }
        else {
            setSelectedURI(OWLRDFVocabulary.RDFS_COMMENT.getURI());
        }
    }

    private void setSelectedURI(URI uri) {
        for (int i = 0; i < getModel().getSize(); i++) {
            Object o = getModel().getElementAt(i);
            if (o instanceof URIItem) {
                URIItem item = (URIItem) o;
                if (item.uri.equals(uri)) {
                    setSelectedIndex(i);
                    ensureIndexIsVisible(i);
                    break;
                }
            }
        }
    }

    private class URIItem implements MListItem, Comparable<URIItem> {

        private URI uri;


        public URIItem(URI uri) {
            this.uri = uri;
        }


        public String toString() {
            String ren = uri.getFragment();
            if (ren == null) {
                int sep = uri.toString().lastIndexOf("/");
                if (sep != -1) {
                    ren = uri.toString().substring(sep + 1, uri.toString().length());
                }
                else {
                    return uri.toString();
                }
            }
            return ren;
        }


        public boolean isEditable() {
            return false;
        }


        public void handleEdit() {
        }


        public boolean isDeleteable() {
            return false;
        }


        public boolean handleDelete() {
            return false;
        }


        public String getTooltip() {
            return uri.toString();
        }


        public int compareTo(URIItem o) {
            return this.toString().compareTo(o.toString());
        }
    }
}
