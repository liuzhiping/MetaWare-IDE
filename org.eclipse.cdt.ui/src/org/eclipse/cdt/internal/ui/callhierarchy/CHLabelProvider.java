/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.ImageImageDescriptor;

public class CHLabelProvider extends LabelProvider implements IColorProvider {
	private final static int LABEL_OPTIONS_SIMPLE = CElementBaseLabels.ALL_FULLY_QUALIFIED
			| CElementBaseLabels.M_PARAMETER_TYPES | CElementBaseLabels.TEMPLATE_ARGUMENTS;
	private final static int LABEL_OPTIONS_SHOW_FILES= LABEL_OPTIONS_SIMPLE | CElementBaseLabels.MF_POST_FILE_QUALIFIED;
	
    private CUILabelProvider fCLabelProvider= new CUILabelProvider(LABEL_OPTIONS_SIMPLE, CElementImageProvider.OVERLAY_ICONS);
    private CHContentProvider fContentProvider;
    private HashMap<String, Image> fCachedImages= new HashMap<String, Image>();
	private Color fColorInactive;
    
    public CHLabelProvider(Display display, CHContentProvider cp) {
        fColorInactive= display.getSystemColor(SWT.COLOR_DARK_GRAY);
        fContentProvider= cp;
    }
    
    @Override
	public Image getImage(Object element) {
        if (element instanceof CHNode) {
            CHNode node= (CHNode) element;
            Image image= null;
            if (node.isInitializer()) {
    			ImageDescriptor desc= CElementImageProvider.getFunctionImageDescriptor();
    			image= CUIPlugin.getImageDescriptorRegistry().get(desc);
            }
            else {
            	ICElement decl= node.getOneRepresentedDeclaration();
            	if (decl != null) {
            		image= fCLabelProvider.getImage(decl);
            	}
            }
            if (image != null) {
            	return decorateImage(image, node);
            }
        }
        return super.getImage(element);
    }

    @Override
	public String getText(Object element) {
        if (element instanceof CHNode) {
            CHNode node= (CHNode) element;
            ICElement decl= node.getOneRepresentedDeclaration();
            if (decl != null) {
            	String label;
            	if (node.isMultiDef()) {
            		int options= fCLabelProvider.getTextFlags();
            		fCLabelProvider.setTextFlags(LABEL_OPTIONS_SIMPLE);
            		label= fCLabelProvider.getText(decl);
            		fCLabelProvider.setTextFlags(options);
            	}
            	else {
            		label= fCLabelProvider.getText(decl);
            		if (node.isInitializer()) {
            			label= addInitializerDecoration(label);
            		}
            	}
            	int refCount= node.getReferenceCount();
            	if (refCount > 1) {
            		label += NLS.bind(" ({0} {1})", new Integer(refCount), CHMessages.CHLabelProvider_matches);  //$NON-NLS-1$
            	}
            	return label;
            }
        }
        return super.getText(element);
    }
    
    private String addInitializerDecoration(String label) {
    	int i= 0;
    	char[] content= label.toCharArray();
    	for (i = 0; i < content.length; i++) {
			char c = content[i];
			if (c == '-' || Character.isWhitespace(c)) {
				break;
			}
		}
    	StringBuffer buf= new StringBuffer(label.length() + 10);
    	buf.append("{init "); //$NON-NLS-1$
    	buf.append(content, 0, i);
    	buf.append("}()"); //$NON-NLS-1$
    	buf.append(content, i, content.length-i);

    	return buf.toString();
	}

	@Override
	public void dispose() {
        fCLabelProvider.dispose();
        for (Image image : fCachedImages.values()) {
            image.dispose();
        }
        fCachedImages.clear();
        super.dispose();
    }

    private Image decorateImage(Image image, CHNode node) {
        int flags= 0;        
        if (node.isRecursive()) {
            flags |= CElementImageDescriptor.RECURSIVE_RELATION;
        }
        else if (fContentProvider.hasChildren(node)) {
            if (fContentProvider.getComputeReferencedBy()) {
                flags |= CElementImageDescriptor.REFERENCED_BY;
            }
            else {
            	if (node.isMultiDef()) {
            		flags |= CElementImageDescriptor.RELATES_TO_MULTIPLE;
            	}
            	else {
            		flags |= CElementImageDescriptor.RELATES_TO;
            	}
            }
        }
        if (node.isReadAccess()) {
        	flags |= CElementImageDescriptor.READ_ACCESS;
        }
        if (node.isWriteAccess()) {
        	flags |= CElementImageDescriptor.WRITE_ACCESS;
        }

        String key= image.toString()+String.valueOf(flags);
        Image result= fCachedImages.get(key);
        if (result == null) {
            ImageDescriptor desc= new CElementImageDescriptor(
                    new ImageImageDescriptor(image), flags, new Point(20,16));
            result= desc.createImage();
            fCachedImages.put(key, result);
        }
        return result;
    }

    public Color getBackground(Object element) {
        return null;
    }

    public Color getForeground(Object element) {
    	if (element instanceof CHMultiDefNode) {
    		return fColorInactive;
    	}
    	return null;
    }

    public void setShowFiles(boolean show) {
		fCLabelProvider.setTextFlags(show ? LABEL_OPTIONS_SHOW_FILES : LABEL_OPTIONS_SIMPLE);
    }
}
