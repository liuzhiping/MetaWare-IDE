/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *     Warren Paul (Nokia) - 174238
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileGenerator;

public class NewClassWizardUtil {

    /**
     * Returns the parent source folder of the given element. If the given
     * element is already a source folder, the element itself is returned.
     * 
     * @param element the C Element
     * @return the source folder
     */
    public static ICContainer getSourceFolder(ICElement element) {
        ICContainer folder = null;
        boolean foundSourceRoot = false;
        ICElement curr = element;
        while (curr != null && !foundSourceRoot) {
            if (curr instanceof ICContainer && folder == null) {
                folder = (ICContainer)curr;
            }
            foundSourceRoot = (curr instanceof ISourceRoot);
            curr = curr.getParent();
        }
        if (folder == null) {
            ICProject cproject = element.getCProject();
            folder = cproject.findSourceRoot(cproject.getProject());
        }
        return folder;
    }
    
    /**
     * Returns the parent source folder for the given path. If the given
     * path is already a source folder, the corresponding C element is returned.
     * 
     * @param path the path
     * @return the source folder
     */
    public static ICContainer getSourceFolder(IPath path) {
        if (path == null)
            return null;
        while (path.segmentCount() > 0) {
            IResource res = getWorkspaceRoot().findMember(path);
            if (res != null && res.exists()) {
                int resType = res.getType();
                if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
                    ICElement elem = CoreModel.getDefault().create(res.getFullPath());
                    if (elem != null) {
                        ICContainer sourceFolder = getSourceFolder(elem);
                        if (sourceFolder != null)
                            return sourceFolder;
                        if (resType == IResource.PROJECT) {
                        	if (elem instanceof ICContainer) {
                                return (ICContainer)elem;
                        	}
                        }
                    }
                }
            }
            path = path.removeLastSegments(1);
        }
        return null;
    }
    
    /**
     * Returns the parent source folder for the given resource. If the given
     * resource is already a source folder, the corresponding C element is returned.
     * 
     * @param resource the resource
     * @return the source folder
     */
    public static ICContainer getSourceFolder(IResource resource) {
        if (resource != null && resource.exists()) {
            int resType = resource.getType();
            if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
                ICElement elem = CoreModel.getDefault().create(resource.getFullPath());
                if (elem != null) {
                    ICContainer sourceFolder = getSourceFolder(elem);
                    if (sourceFolder != null)
                        return sourceFolder;
                }
            } else {
                return getSourceFolder(resource.getParent());
            }
        }
        return null;
    }
    
    /**
     * Returns the first source root in the given project. If the project has
     * no source roots as children, the project itself is returned.
     * 
     * @param cproject
     * @return the source root
     */
    public static ISourceRoot getFirstSourceRoot(ICProject cproject) {
        ISourceRoot folder = null;
        try {
            if (cproject.exists()) {
                ISourceRoot[] roots = cproject.getSourceRoots();
                if (roots != null && roots.length > 0)
                    folder = roots[0];
            }
        } catch (CModelException e) {
            CUIPlugin.log(e);
        }
        if (folder == null) {
            folder = cproject.findSourceRoot(cproject.getResource());
        }
        return folder;
    }

    /**
     * Returns the C Element which corresponds to the given selection.
     * 
     * @param selection the selection to be inspected
     * @return a C element matching the selection, or <code>null</code>
     * if no C element exists in the given selection
     */
    public static ICElement getCElementFromSelection(IStructuredSelection selection) {
        ICElement celem = null;
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;            
                
                celem = (ICElement) adaptable.getAdapter(ICElement.class);
                if (celem == null) {
                    IResource resource = (IResource) adaptable.getAdapter(IResource.class);
                    if (resource != null && resource.getType() != IResource.ROOT) {
                        while (celem == null && resource.getType() != IResource.PROJECT) {
                            celem = (ICElement) resource.getAdapter(ICElement.class);
                            resource = resource.getParent();
                        }
                        if (celem == null) {
                            celem = CoreModel.getDefault().create(resource); // c project
                        }
                    }
                }
            }
        }
        return celem;
    }
    
    /**
     * Returns the C Element which corresponds to the active editor.
     * 
     * @return a C element matching the active editor, or <code>null</code>
     * if no C element can be found
     */
    public static ICElement getCElementFromEditor() {
        ICElement celem = null;
        IWorkbenchPart part = CUIPlugin.getActivePage().getActivePart();
        if (part instanceof ContentOutline) {
            part = CUIPlugin.getActivePage().getActiveEditor();
        }
        if (part instanceof IViewPartInputProvider) {
            Object elem = ((IViewPartInputProvider)part).getViewPartInput();
            if (elem instanceof ICElement) {
                celem = (ICElement) elem;
            }
        }
        if (celem == null && part instanceof CEditor) {
            IEditorInput input = ((IEditorPart)part).getEditorInput();
            if (input != null) {
                final IResource res = (IResource) input.getAdapter(IResource.class);
                if (res != null && res instanceof IFile) {
                    celem = CoreModel.getDefault().create((IFile)res);
                }
            }
        }
        return celem;
    }
    
    /**
     * Returns the parent namespace for the given element. If the given element is
     * already a namespace, the element itself is returned.
     *
     * @param element the given C Element
     * @return the C Element for the namespace, or <code>null</code> if not found
     */
    public static ICElement getNamespace(ICElement element) {
        ICElement curr = element;
        while (curr != null) {
            int type = curr.getElementType();
            if (type == ICElement.C_UNIT) {
                break;
            }
            if (type == ICElement.C_NAMESPACE) {
                return curr;
            }
            curr = curr.getParent();
        }
        return null;
    }
    
    /**
     * Creates a header file name from the given class name. This is the file name
     * to be used when the class is created. eg. "MyClass" -> "MyClass.h"
     * 
     * @param className the class name
     * @return the header file name for the given class
     */
    public static String createHeaderFileName(String className) {
        return NewSourceFileGenerator.generateHeaderFileNameFromClass(className);
    }
    
    /**
     * Creates a source file name from the given class name. This is the file name
     * to be used when the class is created. eg. "MyClass" -> "MyClass.cpp"
     * 
     * @param className the class name
     * @return the source file name for the given class
     */
    public static String createSourceFileName(String className) {
        return NewSourceFileGenerator.generateSourceFileNameFromClass(className);
    }
    
    /**
     * Returns the workspace root.
     * 
     * @return the workspace root
     */ 
    public static IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
    
    /**
     * Resolve the location of the given class.
     * 
     * @param type the class to resolve
     * @param context the runnable context
     * @return the class location, or <code>null</code> if not found
     */
    public static ITypeReference resolveClassLocation(ITypeInfo type, IRunnableContext context) {
        return type.getResolvedReference();
    }

    private static final int[] CLASS_TYPES = { ICElement.C_CLASS, ICElement.C_STRUCT };
    
    /**
     * Returns all classes/structs which are accessible from the include
     * paths of the given project.
     * 
     * @param cProject the given project
     * @return array of classes/structs
     */
    public static ITypeInfo[] getReachableClasses(ICProject cProject) {
        ITypeInfo[] elements = AllTypesCache.getTypes(new TypeSearchScope(true), CLASS_TYPES);
        if (elements != null && elements.length > 0) {
            if (cProject != null) {
            	IProject project = cProject.getProject();
                IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
                if (provider != null) {
                    //TODO get the scanner info for the actual source folder
                    IScannerInfo info = provider.getScannerInformation(project);
                    if (info != null) {
                        String[] includePaths = info.getIncludePaths();
                        List<ITypeInfo> filteredTypes = new ArrayList<ITypeInfo>();
                        for (int i = 0; i < elements.length; ++i) {
                            ITypeInfo baseType = elements[i];
                            if (isTypeReachable(baseType, cProject, includePaths)) {
                                filteredTypes.add(baseType);
                            }
                        }
                        return filteredTypes.toArray(new ITypeInfo[filteredTypes.size()]);
                    }
                }
            }
        }
        return elements;
    }
    
    /**
     * Checks whether the given type can be found in the given project or the
     * given include paths.
     * 
     * @param type the type
     * @param project the project
     * @param includePaths the include paths
     * @return <code>true</code> if the given type is found
     */
    public static boolean isTypeReachable(ITypeInfo type, ICProject project, String[] includePaths) {
        ICProject baseProject = type.getEnclosingProject();
        if (baseProject != null && baseProject.equals(project)) {
            return true;
        }
        
        // check the include paths
        ITypeReference ref = type.getResolvedReference();
        IPath location = ref == null ? null : ref.getLocation();
        boolean isTypeLocation= true;
        if (location == null) {
        	isTypeLocation= false;
        	
        	if (baseProject != null) {
                location = baseProject.getProject().getLocation();
        	}
            if (location == null)
            	return false;
        }
    
        for (int i = 0; i < includePaths.length; ++i) {
            IPath includePath = new Path(includePaths[i]);
            if (isTypeLocation) {
				if (includePath.isPrefixOf(location))
                    return true;
            } else {
                // we don't have the real location, so just check the project path
				if (location.isPrefixOf(includePath))
                    return true;
            }
        }
        
        return false;
    }

	public static final int SEARCH_MATCH_ERROR = -1;  // some error, search failed
	public static final int SEARCH_MATCH_NOTFOUND = 0;  // no match found
	public static final int SEARCH_MATCH_FOUND_EXACT = 1;   // exact match
	public static final int SEARCH_MATCH_FOUND_EXACT_ANOTHER_TYPE = 2; // same name found, by different type
	public static final int SEARCH_MATCH_FOUND_ANOTHER_NAMESPACE = 3; // same type name exits in different scope
	public static final int SEARCH_MATCH_FOUND_ANOTHER_TYPE = 4; // same name used by another type in different scope

	/**
	 * Search for the given qualified name of the give
	 * @param typeName  qualified name of the type to search
	 * @param project  
	 * @param queryType Class of interface type to search for (e.g. ICPPClassType.class)
	 * @return one of {@link #SEARCH_MATCH_ERROR}, 
	 * {@link #SEARCH_MATCH_FOUND_ANOTHER_NAMESPACE},
	 * {@link #SEARCH_MATCH_FOUND_ANOTHER_TYPE}, 
	 * {@link #SEARCH_MATCH_FOUND_EXACT_ANOTHER_TYPE},
	 * {@link #SEARCH_MATCH_FOUND_EXACT} or
	 * {@link #SEARCH_MATCH_NOTFOUND}.	 
	 */
	public static int searchForCppType(IQualifiedTypeName typeName, ICProject project, Class<?> queryType) {
		IIndex index= null;
		try {
			if (project != null) {
				index = CCorePlugin.getIndexManager().getIndex(project);
				index.acquireReadLock();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return SEARCH_MATCH_ERROR;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return SEARCH_MATCH_NOTFOUND;
		}
		if (index == null) {
			return SEARCH_MATCH_ERROR;
		}
		try {
			String fullyQualifiedTypeName = typeName.getFullyQualifiedName();
			try {
				IndexFilter filter= IndexFilter.getDeclaredBindingFilter(ILinkage.CPP_LINKAGE_ID, true);
				//bug 165636: findBindings(char[][]...) does not find nested nodes (classes)
				//therefore switching back to findBindings(Pattern...)
				IBinding[] bindings = index.findBindings(typeName.getName().toCharArray(), false, filter, new NullProgressMonitor());
				boolean sameTypeNameExists = false; 
				boolean sameNameDifferentTypeExists = false;

				for (int i = 0; i < bindings.length; ++i) {
					ICPPBinding binding = (ICPPBinding)bindings[i];

					//get the fully qualified name of this binding
					String bindingFullName = CPPVisitor.renderQualifiedName(binding.getQualifiedName());
					Class<? extends ICPPBinding> currentNodeType = binding.getClass();
					// full binding				
					if (queryType.isAssignableFrom(currentNodeType)) {						
						if (bindingFullName.equals(fullyQualifiedTypeName)) {
							//bug 165636: there is a match only if there is a definition for the binding
							//otherwise, users can create a new class definition for this binding
							if (index.findDefinitions(binding).length > 0)	{
								return SEARCH_MATCH_FOUND_EXACT;
							}							
						} else {
							// same type , same name , but different name space
							// see if there is an exact match;
							sameTypeNameExists = true;
						}
					} else if(ICPPClassType.class.isAssignableFrom(currentNodeType) || 
							IEnumeration.class.isAssignableFrom(currentNodeType) || // TODO - this should maybe be ICPPEnumeration
							ICPPNamespace.class.isAssignableFrom(currentNodeType) ||
							ITypedef.class.isAssignableFrom(currentNodeType) ||
							ICPPBasicType.class.isAssignableFrom(currentNodeType))
					{						
						if (bindingFullName.equals(fullyQualifiedTypeName))	{
							return SEARCH_MATCH_FOUND_EXACT_ANOTHER_TYPE;
						}
						// different type , same name , but different name space
						sameNameDifferentTypeExists = true;
					}
				}
				if(sameTypeNameExists){
					return SEARCH_MATCH_FOUND_ANOTHER_NAMESPACE;
				}

				if(sameNameDifferentTypeExists){
					return SEARCH_MATCH_FOUND_ANOTHER_TYPE;
				}
			} catch (CoreException e) {
				return SEARCH_MATCH_ERROR;
			} catch (DOMException e) {
				return SEARCH_MATCH_ERROR;
			}
			return SEARCH_MATCH_NOTFOUND;
		}
		finally {
			index.releaseReadLock();
		}
	}
}
