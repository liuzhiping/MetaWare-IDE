/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *     Synopsys
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;
	import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.PageLayout;
import org.eclipse.cdt.ui.newui.UIMessages;

import org.eclipse.cdt.internal.ui.CPluginImages;

	public class CDTMainWizardPage extends WizardNewProjectCreationPage implements IWizardItemsListListener {
		private static final Image IMG_CATEGORY = CPluginImages.get(CPluginImages.IMG_OBJS_SEARCHFOLDER);
		private static final Image IMG_ITEM = CPluginImages.get(CPluginImages.IMG_OBJS_VARIABLE);

		public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

		private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
		private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
		private static final String CLASS_NAME = "class"; //$NON-NLS-1$
		public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$ 

	    // widgets
	    private Tree tree;
	    private Composite right;
	    private Button show_sup;
	    private Label right_label;
   
	    public CWizardHandler h_selected = null;
		private Label categorySelectedLabel;

	    /**
	     * Creates a new project creation wizard page.
	     *
	     * @param pageName the name of this page
	     */
	    public CDTMainWizardPage(String pageName) {
	        super(pageName);
	        setPageComplete(false);
	    }

	    /** (non-Javadoc)
	     * Method declared on IDialogPage.
	     */
	    @Override
		public void createControl(Composite parent) {
	    	super.createControl(parent);
	    	
	    	createDynamicGroup((Composite)getControl()); 
			switchTo(updateData(tree, right, show_sup, CDTMainWizardPage.this, getWizard()),
					getDescriptor(tree));

			setPageComplete(validatePage());
	        setErrorMessage(null);
	        setMessage(null);
	    }
	    
	    private void createDynamicGroup(Composite parent) {
	        Composite c = new Composite(parent, SWT.NONE);
	        c.setLayoutData(new GridData(GridData.FILL_BOTH));
	    	c.setLayout(new GridLayout(2, true));
	    	
	        Label l1 = new Label(c, SWT.NONE);
	        l1.setText(UIMessages.getString("CMainWizardPage.0")); //$NON-NLS-1$
	        l1.setFont(parent.getFont());
	        l1.setLayoutData(new GridData(GridData.BEGINNING));
	        
	        right_label = new Label(c, SWT.NONE);
	        right_label.setFont(parent.getFont());
	        right_label.setLayoutData(new GridData(GridData.BEGINNING));
	    	
	        tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
	        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
	        tree.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TreeItem[] tis = tree.getSelection();
					if (tis == null || tis.length == 0) return;
					switchTo((CWizardHandler)tis[0].getData(), (EntryDescriptor)tis[0].getData(DESC));
					setPageComplete(validatePage());
				}});
	        tree.getAccessible().addAccessibleListener(
					 new AccessibleAdapter() {                       
		                 @Override
						public void getName(AccessibleEvent e) {
		                	 for (int i = 0; i < tree.getItemCount(); i++) {
		                		 if (tree.getItem(i).getText().equals(e.result))
		                			 return;
		                	 }
	                         e.result = UIMessages.getString("CMainWizardPage.0"); //$NON-NLS-1$
		                 }
		             }
				 );
	        right = new Composite(c, SWT.NONE);
	        right.setLayoutData(new GridData(GridData.FILL_BOTH));
	        right.setLayout(new PageLayout());

	        show_sup = new Button(c, SWT.CHECK);
	        show_sup.setText(UIMessages.getString("CMainWizardPage.1")); //$NON-NLS-1$
	        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	        gd.horizontalSpan = 2;
	        show_sup.setLayoutData(gd);
	        show_sup.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (h_selected != null)
						h_selected.setSupportedOnly(show_sup.getSelection());
					switchTo(updateData(tree, right, show_sup, CDTMainWizardPage.this, getWizard()),
							getDescriptor(tree));
				}} );

	        // restore settings from preferences
			show_sup.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOSUPP));
	    }
	    
	    @Override
		public IWizardPage getNextPage() {
			return (h_selected == null) ? null : h_selected.getSpecificPage();
	    }		

	    public URI getProjectLocation() {
	    	return useDefaults() ? null : getLocationURI();
	    }

	    /**
	     * Returns whether this page's controls currently all contain valid 
	     * values.
	     *
	     * @return <code>true</code> if all controls are valid, and
	     *   <code>false</code> if at least one is invalid
	     */
	    @Override
		protected boolean validatePage() {
    		setMessage(null);
	    	if (!super.validatePage())
	    		return false;

	        if (getProjectName().indexOf('#') >= 0) {
	            setErrorMessage(UIMessages.getString("CDTMainWizardPage.0"));	             //$NON-NLS-1$
	            return false;
	        }
	        
	        boolean bad = true; // should we treat existing project as error
	        
	        IProject handle = getProjectHandle();
	        if (handle.exists()) {
	        	if (getWizard() instanceof IWizardWithMemory) {
	        		IWizardWithMemory w = (IWizardWithMemory)getWizard();
	        		if (w.getLastProjectName() != null && w.getLastProjectName().equals(getProjectName()))
	        			bad = false;
	        	}
	        	if (bad) { 
	        		setErrorMessage(UIMessages.getString("CMainWizardPage.10")); //$NON-NLS-1$
	        	    return false;
	        	}
	        }

	        if (bad) { // skip this check if project already created 
	        	try {
	        		IFileStore fs;
		        	URI p = getProjectLocation();
		        	if (p == null) {
		        		fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
		        		fs = fs.getChild(getProjectName());
		        	} else
		        		fs = EFS.getStore(p);
	        		IFileInfo f = fs.fetchInfo();
		        	if (f.exists()) {
		        		if (f.isDirectory()) {
		        			setMessage(UIMessages.getString("CMainWizardPage.7"), IMessageProvider.WARNING); //$NON-NLS-1$
		        		} else {
		        			setErrorMessage(UIMessages.getString("CMainWizardPage.6")); //$NON-NLS-1$
		        			return false;
		        		}
		        	}
	        	} catch (CoreException e) {
	        		CUIPlugin.log(e.getStatus());
	        	}
	        }
	        
	        if (!useDefaults()) {
	            IStatus locationStatus = ResourcesPlugin.getWorkspace().validateProjectLocationURI(handle,
	            		getLocationURI());
	            if (!locationStatus.isOK()) {
	                setErrorMessage(locationStatus.getMessage());
	                return false;
	            }
	        }

	        if (tree.getItemCount() == 0) {
	        	setErrorMessage(UIMessages.getString("CMainWizardPage.3")); //$NON-NLS-1$
	        	return false;
	        }
	        
	        // it is not an error, but we cannot continue
	        if (h_selected == null) {
	            setErrorMessage(null);
		        return false;	        	
	        }

	        String s = h_selected.getErrorMessage(); 
			if (s != null) {
        		setErrorMessage(s);
        		return false;
	        }
	        
            setErrorMessage(null);
	        return true;
	    }

	    /**
	     * 
	     * @param tree
	     * @param right
	     * @param show_sup
	     * @param ls
	     * @param wizard
	     * @return : selected Wizard Handler.
	     */
		public static CWizardHandler updateData(Tree tree, Composite right, Button show_sup, IWizardItemsListListener ls, IWizard wizard) {
			// remember selected item
			TreeItem[] selection = tree.getSelection();
			TreeItem selectedItem = selection.length>0 ? selection[0] : null; 
			String savedLabel = selectedItem!=null ? selectedItem.getText() : null;
			String savedParentLabel = getParentText(selectedItem);
			
			tree.removeAll();
			IExtensionPoint extensionPoint =
				    Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
			if (extensionPoint == null) return null;
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions == null) return null;
			
			List<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
			for (int i = 0; i < extensions.length; ++i)	{
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(ELEMENT_NAME)) {
						CNewWizard w = null;
						try {
							w = (CNewWizard) element.createExecutableExtension(CLASS_NAME);
						} catch (CoreException e) {
							System.out.println(UIMessages.getString("CMainWizardPage.5") + e.getLocalizedMessage()); //$NON-NLS-1$
							return null; 
						}
						if (w == null) return null;
						w.setDependentControl(right, ls);
						for (EntryDescriptor ed : w.createItems(show_sup.getSelection(), wizard))	
							items.add(ed);
					}
				}
			}
			// If there is a EntryDescriptor which is default for category, make sure it 
			// is in the front of the list.
			for (int i = 0; i < items.size(); ++i)
			{
				EntryDescriptor ed = items.get(i);
				if (ed.isDefaultForCategory())
				{
					items.remove(i);
					items.add(0, ed);
					break;
				}				
			}
			
			// bug # 211935 : allow items filtering.
			if (ls != null) // NULL means call from prefs
				items = ls.filterItems(items);
			addItemsToTree(tree, items);
			
			if (tree.getItemCount() > 0) {
				TreeItem target = null;
				// try to search item which was selected before
				if (savedLabel!=null) {
					target = findItem(tree, savedLabel, savedParentLabel);
				}
				if (target == null) {
					target = tree.getItem(0);
					if (target.getItemCount() != 0)
						target = target.getItem(0);
				}
				tree.setSelection(target);
				
				//SYNOPSYS CUSTOMIZATION: width is off due to some sort of layout problem; Compensate.
				tree.getShell().pack(true);
				Point shellSize = tree.getShell().getSize();
				tree.getShell().setSize(shellSize.x*5/4,shellSize.y);
				
				return (CWizardHandler)target.getData();
			}
			return null;
		}

		private static String getParentText(TreeItem item) {
			if (item==null || item.getParentItem()==null)
				return ""; //$NON-NLS-1$
			return item.getParentItem().getText();
		}

		private static TreeItem findItem(Tree tree, String label, String parentLabel) {
			for (TreeItem item : tree.getItems()) {
				TreeItem foundItem = findTreeItem(item, label, parentLabel);
				if (foundItem!=null)
					return foundItem;
			}
			return null;
		}

		private static TreeItem findTreeItem(TreeItem item, String label, String parentLabel) {
			if (item.getText().equals(label) && getParentText(item).equals(parentLabel))
				return item;
			
			for (TreeItem child : item.getItems()) {
				TreeItem foundItem = findTreeItem(child, label, parentLabel);
				if (foundItem!=null)
					return foundItem;
			}
			return null;
		}

		private static void addItemsToTree(Tree tree, List<EntryDescriptor> items) {
		//  Sorting is disabled because of users requests	
		//	Collections.sort(items, CDTListComparator.getInstance());
			
			ArrayList<TreeItem> placedTreeItemsList = new ArrayList<TreeItem>(items.size());
			ArrayList<EntryDescriptor> placedEntryDescriptorsList = new ArrayList<EntryDescriptor>(items.size());
			for (EntryDescriptor wd : items) {
				if (wd.getParentId() == null) {
					wd.setPath(wd.getId());
					TreeItem ti = new TreeItem(tree, SWT.NONE);
					ti.setText(TextProcessor.process(wd.getName()));
					ti.setData(wd.getHandler());
					ti.setData(DESC, wd);
					ti.setImage(calcImage(wd));
					placedTreeItemsList.add(ti);
					placedEntryDescriptorsList.add(wd);
				}
			}
			while(true) {
				boolean found = false;
				Iterator<EntryDescriptor> it2 = items.iterator();
				while (it2.hasNext()) {
					EntryDescriptor wd1 = it2.next();
					if (wd1.getParentId() == null) continue;
					for (int i = 0; i< placedEntryDescriptorsList.size(); i++) {
						EntryDescriptor wd2 = placedEntryDescriptorsList.get(i);
						if (wd2.getId().equals(wd1.getParentId())) {
							found = true;
							wd1.setParentId(null);
							CWizardHandler h = wd2.getHandler();
							/* If neither wd1 itself, nor its parent (wd2) have a handler
							 * associated with them, and the item is not a category,
							 * then skip it. If it's category, then it's possible that
							 * children will have a handler associated with them.
							 */
							if (h == null && wd1.getHandler() == null && !wd1.isCategory())
								break;

							wd1.setPath(wd2.getPath() + "/" + wd1.getId()); //$NON-NLS-1$
							wd1.setParent(wd2);
							if (h != null) {
								if (wd1.getHandler() == null && !wd1.isCategory())
									wd1.setHandler((CWizardHandler)h.clone());
								if (!h.isApplicable(wd1))
									break;
							}
							
							TreeItem p = placedTreeItemsList.get(i);
							TreeItem ti = new TreeItem(p, SWT.NONE);
							ti.setText(wd1.getName());
							ti.setData(wd1.getHandler());
							ti.setData(DESC, wd1);
							ti.setImage(calcImage(wd1));
							placedTreeItemsList.add(ti);
							placedEntryDescriptorsList.add(wd1);
							break;
						}
					}
				}
				// repeat iterations until all items are placed.
				if (!found) break;
			}
			// orphan elements (with not-existing parentId) are ignored
		}

		private void switchTo(CWizardHandler h, EntryDescriptor ed) {
			if (h == null) 
				h = ed.getHandler();
			if (ed.isCategory())
				h = null;
			try {
				if (h != null) 
					h.initialize(ed);
			} catch (CoreException e) { 
				h = null;
			}
			if (h_selected != null) 
				h_selected.handleUnSelection();
			h_selected = h;
			if (h == null) {
				if (ed.isCategory()) {
					if (categorySelectedLabel == null) {
						categorySelectedLabel = new Label(right, SWT.WRAP);
						categorySelectedLabel.setText(
								UIMessages.getString("CDTMainWizardPage.1"));  //$NON-NLS-1$
						right.layout();
					}
					categorySelectedLabel.setVisible(true);
				}
				return;
			}
			right_label.setText(h_selected.getHeader());
			if (categorySelectedLabel != null)
				categorySelectedLabel.setVisible(false);
			h_selected.handleSelection();
			h_selected.setSupportedOnly(show_sup.getSelection());
		}


		public static EntryDescriptor getDescriptor(Tree _tree) {
			TreeItem[] sel = _tree.getSelection();
			if (sel == null || sel.length == 0) 
				return null;
			return (EntryDescriptor)sel[0].getData(DESC);
		}
		
		public void toolChainListChanged(int count) {
			setPageComplete(validatePage());
			getWizard().getContainer().updateButtons();
		}

		public boolean isCurrent() { return isCurrentPage(); }
		
		private static Image calcImage(EntryDescriptor ed) {
			if (ed.getImage() != null) return ed.getImage();
			if (ed.isCategory()) return IMG_CATEGORY;
			return IMG_ITEM;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public List filterItems(List items) {
			return items;
		}
}

