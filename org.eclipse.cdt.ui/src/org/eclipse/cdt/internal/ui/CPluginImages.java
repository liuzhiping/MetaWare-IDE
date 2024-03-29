/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google) 
 *     Dmitry Kozlov (CodeSourcery)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Bundle of all images used by the C plugin.
 */
public class CPluginImages {
	public static final IPath ICONS_PATH= new Path("$nl$/icons"); //$NON-NLS-1$

	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry(CUIPlugin.getStandardDisplay());

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;

	static {
		try {
			fgIconBaseURL= new URL(CUIPlugin.getDefault().getBundle().getEntry("/"), "icons/" ); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			CUIPlugin.log(e);
		}
	}	
	private static final String NAME_PREFIX= CUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	public static final String T_OBJ= "obj16/"; //$NON-NLS-1$
	public static final String T_WIZBAN= "wizban/"; //$NON-NLS-1$
	public static final String T_LCL=  "lcl16/"; //$NON-NLS-1$
	public static final String T_DLCL=  "dlcl16/"; //$NON-NLS-1$
	public static final String T_ELCL=  "elcl16/"; //$NON-NLS-1$
	public static final String T_TOOL= "tool16/"; //$NON-NLS-1$
	public static final String T_VIEW= "view16/"; //$NON-NLS-1$
	public static final String T_OVR= "ovr16/"; //$NON-NLS-1$

	public static final String IMG_OBJS_TEMPLATE= NAME_PREFIX + "template_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE= NAME_PREFIX + "variable_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LOCAL_VARIABLE= NAME_PREFIX + "variable_local_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CLASS= NAME_PREFIX + "class_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CLASS_ALT= NAME_PREFIX + "classfo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_NAMESPACE= NAME_PREFIX + "namespace_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_USING= NAME_PREFIX + "using_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_STRUCT= NAME_PREFIX + "struct_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_STRUCT_ALT= NAME_PREFIX + "structfo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_UNION= NAME_PREFIX + "union_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_UNION_ALT= NAME_PREFIX + "unionfo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TYPEDEF= NAME_PREFIX + "typedef_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TYPEDEF_ALT= NAME_PREFIX + "typedeffo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENUMERATION= NAME_PREFIX + "enum_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENUMERATION_ALT= NAME_PREFIX + "enumfo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_UNKNOWN_TYPE= NAME_PREFIX + "unknown_type_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENUMERATOR= NAME_PREFIX + "enumerator_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_FUNCTION= NAME_PREFIX + "function_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PUBLIC_METHOD= NAME_PREFIX + "method_public_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PROTECTED_METHOD= NAME_PREFIX + "method_protected_obj.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_PRIVATE_METHOD= NAME_PREFIX + "method_private_obj.gif";	 //$NON-NLS-1$
	public static final String IMG_OBJS_PUBLIC_FIELD= NAME_PREFIX + "field_public_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PROTECTED_FIELD= NAME_PREFIX + "field_protected_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PRIVATE_FIELD= NAME_PREFIX + "field_private_obj.gif";	 //$NON-NLS-1$
	public static final String IMG_OBJS_KEYWORD= NAME_PREFIX + "keyword_obj.gif";	 //$NON-NLS-1$
	
	public static final String IMG_OBJS_DECLARATION= NAME_PREFIX + "cdeclaration_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_VAR_DECLARATION= NAME_PREFIX + "var_declaration_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDE= NAME_PREFIX + "include_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_MACRO= NAME_PREFIX + "define_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LABEL= NAME_PREFIX + "label_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT= NAME_PREFIX + "c_file_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_HEADER= NAME_PREFIX + "h_file_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_ASM= NAME_PREFIX + "s_file_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_RESOURCE= NAME_PREFIX + "c_resource_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_RESOURCE_H= NAME_PREFIX + "ch_resource_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_RESOURCE_A= NAME_PREFIX + "asm_resource_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SOURCE_ROOT=  NAME_PREFIX + "sroot_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_SOURCE2_ROOT=  NAME_PREFIX + "sroot2_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_FOLDER=  NAME_PREFIX + "fldr_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_CFOLDER=  NAME_PREFIX + "cfolder_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_CONFIG =  NAME_PREFIX + "config.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_ARCHIVE= NAME_PREFIX + "ar_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BINARY= NAME_PREFIX + "bin_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SHLIB= NAME_PREFIX + "shlib_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CEXEC= NAME_PREFIX + "exec_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CEXEC_DEBUG= NAME_PREFIX + "exec_dbg_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CORE= NAME_PREFIX + "core_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CONTAINER= NAME_PREFIX + "container_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ARCHIVES_CONTAINER= NAME_PREFIX + "archives_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BINARIES_CONTAINER= NAME_PREFIX + "binaries_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_OUTPUT_FOLDER= NAME_PREFIX + "output_folder_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LIBRARY= NAME_PREFIX + "lib_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_CONTAINER = NAME_PREFIX + "includes_container.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER = NAME_PREFIX + "hfolder_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_QUOTE_INCLUDES_FOLDER = NAME_PREFIX + "hfolder_quote_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER_SYSTEM = NAME_PREFIX + "fldr_sys_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER_WORKSPACE = NAME_PREFIX + "wsp_includefolder.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ORDER= NAME_PREFIX + "cp_order_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_EXCLUDSION_FILTER_ATTRIB= NAME_PREFIX + "exclusion_filter_attrib.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SOURCE_ATTACH_ATTRIB = NAME_PREFIX + "source_attach_attrib.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ARCHIVE_WSRC= NAME_PREFIX + "ar_src_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_IMPORT_SETTINGS = NAME_PREFIX + "import_settings_wiz.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_EXPORT_SETTINGS = NAME_PREFIX + "export_settings_wiz.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_PROJECT=NAME_PREFIX + "prj_obj.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_INCCONT= NAME_PREFIX + "incc_obj.gif"; 			//$NON-NLS-1$

	// Breakpoint images
	public static final String IMG_OBJS_BREAKPOINT = NAME_PREFIX + "breakpoint.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_DISABLED = NAME_PREFIX + "breakpoint_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_ACTIVE = NAME_PREFIX + "breakpoint_active.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_FIXABLE_PROBLEM= NAME_PREFIX + "quickfix_warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_FIXABLE_ERROR= NAME_PREFIX + "quickfix_error_obj.gif"; //$NON-NLS-1$

	// build console
	public static final String IMG_VIEW_BUILD = NAME_PREFIX + "buildconsole.gif"; //$NON-NLS-1$
	public static final String IMG_VIEW_MENU = NAME_PREFIX + "view_menu.gif";   //$NON-NLS-1$
	public static final String IMG_SAVE_CONSOLE = NAME_PREFIX + "save_console.gif";   //$NON-NLS-1$

	// unknown type
	public static final String IMG_OBJS_UNKNOWN = NAME_PREFIX + "unknown_obj.gif"; //$NON-NLS-1$
	
	public static final ImageDescriptor DESC_BUILD_CONSOLE = createManaged(T_VIEW, IMG_VIEW_BUILD);
	public static final ImageDescriptor IMG_VIEW_MENU_DESC = createManaged(T_VIEW, IMG_VIEW_MENU);
	public static final ImageDescriptor IMG_SAVE_CONSOLE_DESC = createManaged(T_VIEW, IMG_SAVE_CONSOLE);

	public static final ImageDescriptor DESC_OBJS_VARIABLE= createManaged(T_OBJ, IMG_OBJS_VARIABLE);
	public static final ImageDescriptor DESC_OBJS_LOCAL_VARIABLE= createManaged(T_OBJ, IMG_OBJS_LOCAL_VARIABLE);
	public static final ImageDescriptor DESC_OBJS_CLASS= createManaged(T_OBJ, IMG_OBJS_CLASS);
	public static final ImageDescriptor DESC_OBJS_NAMESPACE= createManaged(T_OBJ, IMG_OBJS_NAMESPACE);
	public static final ImageDescriptor DESC_OBJS_USING = createManaged(T_OBJ, IMG_OBJS_USING);
	public static final ImageDescriptor DESC_OBJS_STRUCT= createManaged(T_OBJ, IMG_OBJS_STRUCT);
	public static final ImageDescriptor DESC_OBJS_UNION= createManaged(T_OBJ, IMG_OBJS_UNION);
	public static final ImageDescriptor DESC_OBJS_TYPEDEF= createManaged(T_OBJ, IMG_OBJS_TYPEDEF);
	public static final ImageDescriptor DESC_OBJS_ENUMERATION= createManaged(T_OBJ, IMG_OBJS_ENUMERATION);
	public static final ImageDescriptor DESC_OBJS_UNKNOWN_TYPE= createManaged(T_OBJ, IMG_OBJS_UNKNOWN_TYPE);
	public static final ImageDescriptor DESC_OBJS_ENUMERATOR= createManaged(T_OBJ, IMG_OBJS_ENUMERATOR);
	public static final ImageDescriptor DESC_OBJS_FUNCTION= createManaged(T_OBJ, IMG_OBJS_FUNCTION);
	public static final ImageDescriptor DESC_OBJS_PUBLIC_METHOD= createManaged(T_OBJ, IMG_OBJS_PUBLIC_METHOD);
	public static final ImageDescriptor DESC_OBJS_PROTECTED_METHOD= createManaged(T_OBJ, IMG_OBJS_PROTECTED_METHOD);
	public static final ImageDescriptor DESC_OBJS_PRIVATE_METHOD= createManaged(T_OBJ, IMG_OBJS_PRIVATE_METHOD);
	public static final ImageDescriptor DESC_OBJS_PUBLIC_FIELD= createManaged(T_OBJ, IMG_OBJS_PUBLIC_FIELD);
	public static final ImageDescriptor DESC_OBJS_PROTECTED_FIELD= createManaged(T_OBJ, IMG_OBJS_PROTECTED_FIELD);
	public static final ImageDescriptor DESC_OBJS_PRIVATE_FIELD= createManaged(T_OBJ, IMG_OBJS_PRIVATE_FIELD);
	public static final ImageDescriptor DESC_OBJS_KEYWORD= createManaged(T_OBJ, IMG_OBJS_KEYWORD);			

	public static final ImageDescriptor DESC_OBJS_CLASS_ALT= createManaged(T_OBJ, IMG_OBJS_CLASS_ALT);
	public static final ImageDescriptor DESC_OBJS_STRUCT_ALT= createManaged(T_OBJ, IMG_OBJS_STRUCT_ALT);
	public static final ImageDescriptor DESC_OBJS_UNION_ALT= createManaged(T_OBJ, IMG_OBJS_UNION_ALT);
	public static final ImageDescriptor DESC_OBJS_TYPEDEF_ALT= createManaged(T_OBJ, IMG_OBJS_TYPEDEF_ALT);
	public static final ImageDescriptor DESC_OBJS_ENUMERATION_ALT= createManaged(T_OBJ, IMG_OBJS_ENUMERATION_ALT);

	public static final ImageDescriptor DESC_OBJS_DECLARARION= createManaged(T_OBJ, IMG_OBJS_DECLARATION);
	public static final ImageDescriptor DESC_OBJS_VAR_DECLARARION= createManaged(T_OBJ, IMG_OBJS_VAR_DECLARATION);
	public static final ImageDescriptor DESC_OBJS_INCLUDE= createManaged(T_OBJ, IMG_OBJS_INCLUDE);
	public static final ImageDescriptor DESC_OBJS_MACRO= createManaged(T_OBJ, IMG_OBJS_MACRO);
	public static final ImageDescriptor DESC_OBJS_LABEL= createManaged(T_OBJ, IMG_OBJS_LABEL);
	public static final ImageDescriptor DESC_OBJS_TUNIT= createManaged(T_OBJ, IMG_OBJS_TUNIT);
	public static final ImageDescriptor DESC_OBJS_TUNIT_HEADER= createManaged(T_OBJ, IMG_OBJS_TUNIT_HEADER);
	public static final ImageDescriptor DESC_OBJS_TUNIT_ASM= createManaged(T_OBJ, IMG_OBJS_TUNIT_ASM);
	public static final ImageDescriptor DESC_OBJS_TUNIT_RESOURCE= createManaged(T_OBJ, IMG_OBJS_TUNIT_RESOURCE);
	public static final ImageDescriptor DESC_OBJS_TUNIT_RESOURCE_H= createManaged(T_OBJ, IMG_OBJS_TUNIT_RESOURCE_H);
	public static final ImageDescriptor DESC_OBJS_TUNIT_RESOURCE_A= createManaged(T_OBJ, IMG_OBJS_TUNIT_RESOURCE_A);
	public static final ImageDescriptor DESC_OBJS_SOURCE_ROOT= createManaged(T_OBJ, IMG_OBJS_SOURCE_ROOT);
	public static final ImageDescriptor DESC_OBJS_SOURCE2_ROOT= createManaged(T_OBJ, IMG_OBJS_SOURCE2_ROOT);
	public static final ImageDescriptor DESC_OBJS_FOLDER= createManaged(T_OBJ, IMG_OBJS_FOLDER);
	public static final ImageDescriptor DESC_OBJS_CFOLDER= createManaged(T_OBJ, IMG_OBJS_CFOLDER);
	public static final ImageDescriptor DESC_OBJS_CONFIG = createManaged(T_OBJ, IMG_OBJS_CONFIG);
	public static final ImageDescriptor DESC_OBJS_ARCHIVE= createManaged(T_OBJ, IMG_OBJS_ARCHIVE);
	public static final ImageDescriptor DESC_OBJS_ARCHIVE_WSRC= createManaged(T_OBJ, IMG_OBJS_ARCHIVE);
	public static final ImageDescriptor DESC_OBJS_BINARY= createManaged(T_OBJ, IMG_OBJS_BINARY);
	public static final ImageDescriptor DESC_OBJS_SHLIB= createManaged(T_OBJ, IMG_OBJS_SHLIB);
	public static final ImageDescriptor DESC_OBJS_CEXEC= createManaged(T_OBJ, IMG_OBJS_CEXEC);
	public static final ImageDescriptor DESC_OBJS_CEXEC_DEBUG= createManaged(T_OBJ, IMG_OBJS_CEXEC_DEBUG);
	public static final ImageDescriptor DESC_OBJS_CORE= createManaged(T_OBJ, IMG_OBJS_CORE);
	public static final ImageDescriptor DESC_OBJS_CONTAINER= createManaged(T_OBJ, IMG_OBJS_CONTAINER);
	public static final ImageDescriptor DESC_OBJS_ARCHIVES_CONTAINER= createManaged(T_OBJ, IMG_OBJS_ARCHIVES_CONTAINER);
	public static final ImageDescriptor DESC_OBJS_BINARIES_CONTAINER= createManaged(T_OBJ, IMG_OBJS_BINARIES_CONTAINER);
	public static final ImageDescriptor DESC_OBJS_OUTPUT_FOLDER= createManaged(T_OBJ, IMG_OBJS_OUTPUT_FOLDER);
	public static final ImageDescriptor DESC_OBJS_LIBRARY= createManaged(T_OBJ, IMG_OBJS_LIBRARY);
	public static final ImageDescriptor DESC_OBJS_INCLUDES_CONTAINER= createManaged(T_OBJ, IMG_OBJS_INCLUDES_CONTAINER);
	public static final ImageDescriptor DESC_OBJS_INCLUDES_FOLDER= createManaged(T_OBJ, IMG_OBJS_INCLUDES_FOLDER);
	public static final ImageDescriptor DESC_OBJS_QUOTE_INCLUDES_FOLDER= createManaged(T_OBJ, IMG_OBJS_QUOTE_INCLUDES_FOLDER);
	public static final ImageDescriptor DESC_OBJS_INCLUDES_FOLDER_SYSTEM  = createManaged(T_OBJ, IMG_OBJS_INCLUDES_FOLDER_SYSTEM);
	public static final ImageDescriptor DESC_OBJS_INCLUDES_FOLDER_WORKSPACE= createManaged(T_OBJ, IMG_OBJS_INCLUDES_FOLDER_WORKSPACE);
	public static final ImageDescriptor DESC_OBJS_ORDER= createManaged(T_OBJ, IMG_OBJS_ORDER);
	public static final ImageDescriptor DESC_OBJS_EXCLUSION_FILTER_ATTRIB = createManaged(T_OBJ, IMG_OBJS_EXCLUDSION_FILTER_ATTRIB);
	public static final ImageDescriptor DESC_OBJS_SOURCE_ATTACH_ATTRIB= createManaged(T_OBJ, IMG_OBJS_SOURCE_ATTACH_ATTRIB);
	public static final ImageDescriptor DESC_OBJS_IMPORT_SETTINGS = createManaged(T_OBJ, IMG_OBJS_IMPORT_SETTINGS);
	public static final ImageDescriptor DESC_OBJS_EXPORT_SETTINGS = createManaged(T_OBJ, IMG_OBJS_EXPORT_SETTINGS);
	public static final ImageDescriptor DESC_OVR_PATH_INHERIT= create(T_OVR, "path_inherit_co.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_FOCUS= create(T_OVR, "focus_ovr.gif"); //$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_FIXABLE_PROBLEM= createManaged(T_OBJ, IMG_OBJS_FIXABLE_PROBLEM);
	public static final ImageDescriptor DESC_OBJS_FIXABLE_ERROR= createManaged(T_OBJ, IMG_OBJS_FIXABLE_ERROR);

	public static final ImageDescriptor DESC_OBJS_INCCONT= createManaged(T_OBJ, IMG_OBJS_INCCONT);

	public static final ImageDescriptor DESC_OBJS_UNKNOWN = createManaged(T_OBJ, IMG_OBJS_UNKNOWN);
	// Breakpoint image descriptors
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT = createManaged( T_OBJ, IMG_OBJS_BREAKPOINT );
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_BREAKPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_ACTIVE = createManaged( T_OBJ, IMG_OBJS_BREAKPOINT_ACTIVE );

	public static final String IMG_MENU_SHIFT_RIGHT= NAME_PREFIX + "shift_r_edit.gif"; //$NON-NLS-1$
	public static final String IMG_MENU_SHIFT_LEFT= NAME_PREFIX + "shift_l_edit.gif"; //$NON-NLS-1$
	public static final String IMG_MENU_OPEN_INCLUDE= NAME_PREFIX + "open_include.gif"; //$NON-NLS-1$
	public static final String IMG_MENU_GROUP_INCLUDE= NAME_PREFIX + "group_include.gif"; //$NON-NLS-1$
	public static final String IMG_MENU_SEGMENT_EDIT= NAME_PREFIX + "segment_edit.gif"; //$NON-NLS-1$
	public static final String IMG_MENU_CODE_ASSIST= NAME_PREFIX + "metharg_obj.gif"; //$NON-NLS-1$
	public static final String IMG_MENU_COLLAPSE_ALL= NAME_PREFIX + "collapseall.gif"; //$NON-NLS-1$
	public static final String IMG_CLEAR_CONSOLE= NAME_PREFIX + "clear_co.gif"; //$NON-NLS-1$
	public static final String IMG_SCROLL_LOCK= NAME_PREFIX + "lock_co.gif"; //$NON-NLS-1$
	public static final String IMG_ALPHA_SORTING= NAME_PREFIX + "alphab_sort_co.gif"; //$NON-NLS-1$
	public static final String IMG_TOOL_GOTO_PREV_ERROR= NAME_PREFIX + "prev_error_nav.gif"; //$NON-NLS-1$
	public static final String IMG_TOOL_GOTO_NEXT_ERROR= NAME_PREFIX + "next_error_nav.gif"; //$NON-NLS-1$
	public static final String IMG_EDIT_PROPERTIES= NAME_PREFIX + "prop_edt.gif";	 //$NON-NLS-1$
    
	public static final String IMG_ACTION_HIDE_FIELDS= NAME_PREFIX + "fields_co.gif"; //$NON-NLS-1$
	public static final String IMG_ACTION_SHOW_PUBLIC= NAME_PREFIX + "public_co.gif"; //$NON-NLS-1$
	public static final String IMG_ACTION_HIDE_STATIC= NAME_PREFIX + "static_co.gif"; //$NON-NLS-1$

    public static final String IMG_ACTION_SHOW_REF_BY = NAME_PREFIX + "ch_callers.gif"; //$NON-NLS-1$
    public static final String IMG_ACTION_SHOW_RELATES_TO = NAME_PREFIX + "ch_callees.gif"; //$NON-NLS-1$
    public static final String IMG_ACTION_HIDE_INACTIVE = NAME_PREFIX + "filterInactive.gif"; //$NON-NLS-1$
    public static final String IMG_ACTION_HIDE_SYSTEM = NAME_PREFIX + "filterSystem.gif"; //$NON-NLS-1$
    public static final String IMG_ACTION_HIDE_MACROS = NAME_PREFIX + "filterDefines.gif"; //$NON-NLS-1$
    public static final String IMG_SHOW_NEXT= NAME_PREFIX + "search_next.gif"; //$NON-NLS-1$
    public static final String IMG_SHOW_PREV= NAME_PREFIX + "search_prev.gif"; //$NON-NLS-1$

    public static final String IMG_REFRESH= NAME_PREFIX + "refresh_nav.gif"; //$NON-NLS-1$
    public static final String IMG_LCL_CANCEL= NAME_PREFIX + "progress_stop.gif"; //$NON-NLS-1$

    // view orientation
	public static final String IMG_LCL_HORIZONTAL_ORIENTATION= NAME_PREFIX + "th_horizontal.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_VERTICAL_ORIENTATION= NAME_PREFIX + "th_vertical.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_AUTOMATIC_ORIENTATION= NAME_PREFIX + "th_automatic.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SINGLE_ORIENTATION= NAME_PREFIX + "th_single.gif"; //$NON-NLS-1$

    // hierarchy kind
	public static final String IMG_LCL_TYPE_HIERARCHY= NAME_PREFIX + "hierarchy_co.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SUB_TYPE_HIERARCHY= NAME_PREFIX + "sub_co.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SUPER_TYPE_HIERARCHY= NAME_PREFIX + "super_co.gif"; //$NON-NLS-1$
	public static final String IMG_LCL_SHOW_INHERITED_MEMBERS= NAME_PREFIX + "inher_co.gif"; //$NON-NLS-1$
	
	public static final String IMG_FILESYSTEM= NAME_PREFIX + "filesyst.gif"; //$NON-NLS-1$
	public static final String IMG_WORKSPACE = NAME_PREFIX + "workspace.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_FILESYSTEM = createManaged(T_OBJ, IMG_FILESYSTEM);
	public static final ImageDescriptor DESC_WORKSPACE  = createManaged(T_OBJ, IMG_WORKSPACE);
	
	public static final ImageDescriptor DESC_OBJS_TEMPLATE= createManaged(T_OBJ, IMG_OBJS_TEMPLATE);
	
	public static final ImageDescriptor DESC_OVR_STATIC= create(T_OVR, "static_co.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_CONSTANT= create(T_OVR, "c_ovr.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_VOLATILE= create(T_OVR, "volatile_co.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_TEMPLATE= create(T_OVR, "template_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_RELATESTO= create(T_OVR, "relatesto_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_RELATESTOMULTIPLE= create(T_OVR, "relatestoMultiple_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_REFERENCEDBY= create(T_OVR, "referencedby_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_REC_RELATESTO= create(T_OVR, "rec_relatesto_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_REC_REFERENCEDBY= create(T_OVR, "rec_referencedby_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_SYSTEM_INCLUDE= create(T_OVR, "systeminclude_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_DEFINES= create(T_OVR, "defines_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_INACTIVE= create(T_OVR, "inactive_co.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_READ_ACCESS= create(T_OVR, "read.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_READ_WRITE_ACCESS= create(T_OVR, "readwrite.gif"); //$NON-NLS-1$
    public static final ImageDescriptor DESC_OVR_WRITE_ACCESS= create(T_OVR, "write.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_EXTERNAL_FILE= create(T_OVR, "external_file.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_SETTING= create(T_OVR, "setting_nav.gif"); //$NON-NLS-1$

	public static final ImageDescriptor DESC_OVR_WARNING= create(T_OVR, "warning_co.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_ERROR= create(T_OVR, "error_co.gif"); //$NON-NLS-1$


	public static final ImageDescriptor DESC_WIZABAN_NEW_PROJ= create(T_WIZBAN, "newcprj_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_NEWCLASS= create(T_WIZBAN, "newclass_wiz.gif");	 //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZABAN_C_APP= create(T_WIZBAN, "c_app_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_NEW_FILE= create(T_WIZBAN, "newfile_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_NEW_HEADERFILE= create(T_WIZBAN, "newhfile_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_NEW_SOURCEFILE= create(T_WIZBAN, "newcfile_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_NEWSRCFOLDR= create(T_WIZBAN, "newsrcfldr_wiz.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_EXPORTINDEX= create(T_WIZBAN, "exportzip_wiz.png"); //$NON-NLS-1$

	public static final ImageDescriptor DESC_TOOL_NEWCLASS= create(T_TOOL, "newclass_wiz.gif"); 				//$NON-NLS-1$

	public static final ImageDescriptor DESC_WIZBAN_ADD_LIBRARY = create(T_WIZBAN, "addpath_wiz.gif"); //$NON-NLS-1$
	// For the build image
	public static final String IMG_OBJS_BUILD= NAME_PREFIX + "build_menu.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_MENU = createManaged(T_OBJ, IMG_OBJS_BUILD);

	//for search
	public static final String IMG_OBJS_SEARCH_REF  = NAME_PREFIX + "search_ref_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SEARCH_DECL = NAME_PREFIX + "search_decl_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SEARCH_LINE = NAME_PREFIX + "searchm_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CSEARCH     = NAME_PREFIX + "csearch_obj.gif"; //$NON-NLS-1$
	
	public static final String IMG_OBJS_SEARCHFOLDER = NAME_PREFIX + "fldr_obj.gif";  //$NON-NLS-1$
	public static final String IMG_OBJS_SEARCHPROJECT = NAME_PREFIX + "cprojects.gif";  //$NON-NLS-1$
	
	public static final ImageDescriptor DESC_OBJS_SEARCH_DECL = createManaged(T_OBJ, IMG_OBJS_SEARCH_DECL);
	public static final ImageDescriptor DESC_OBJS_SEARCH_REF  = createManaged(T_OBJ, IMG_OBJS_SEARCH_REF);
	public static final ImageDescriptor DESC_OBJS_SEARCH_LINE = createManaged(T_OBJ, IMG_OBJS_SEARCH_LINE);
	public static final ImageDescriptor DESC_OBJS_CSEARCH     = createManaged(T_OBJ, IMG_OBJS_CSEARCH);
	
	public static final ImageDescriptor DESC_OBJS_SEARCHHIERPROJECT = createManaged(T_OBJ,IMG_OBJS_SEARCHPROJECT);
	public static final ImageDescriptor DESC_OBJS_SEARCHHIERFODLER = createManaged(T_OBJ,IMG_OBJS_SEARCHFOLDER);
	
	// refactoring
	public static final String IMG_OBJS_REFACTORING_FATAL= NAME_PREFIX + "fatalerror_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_ERROR= NAME_PREFIX + "error_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_WARNING= NAME_PREFIX + "warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_INFO= NAME_PREFIX + "info_obj.gif"; 	//$NON-NLS-1$

	public static final ImageDescriptor DESC_REFACTORING_FATAL= createManaged( T_OBJ, IMG_OBJS_REFACTORING_FATAL); 
	public static final ImageDescriptor DESC_REFACTORING_ERROR= createManaged( T_OBJ, IMG_OBJS_REFACTORING_ERROR); 
	public static final ImageDescriptor DESC_REFACTORING_WARNING= createManaged( T_OBJ, IMG_OBJS_REFACTORING_WARNING); 
	public static final ImageDescriptor DESC_REFACTORING_INFO= createManaged ( T_OBJ, IMG_OBJS_REFACTORING_INFO); 	
		
	public static final ImageDescriptor DESC_WIZBAN_REFACTOR_FIELD= create(T_WIZBAN, "fieldrefact_wiz.gif");	//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_REFACTOR_METHOD= create(T_WIZBAN, "methrefact_wiz.gif");	//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_REFACTOR_TYPE= create(T_WIZBAN, "typerefact_wiz.gif"); 	//$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_DEFAULT_CHANGE= create(T_OBJ, "change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_COMPOSITE_CHANGE= create(T_OBJ, "composite_change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_CU_CHANGE= create(T_OBJ, "cu_change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_FILE_CHANGE= create(T_OBJ, "file_change.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_TEXT_EDIT= create(T_OBJ, "text_edit.gif"); //$NON-NLS-1$

	public static final String IMG_PREFERRED = NAME_PREFIX + "tc_preferred.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_PREFERRED = createManaged(T_OBJ, IMG_PREFERRED);
	public static final String IMG_EMPTY = NAME_PREFIX + "tc_empty.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_EMPTY = createManaged(T_OBJ, IMG_EMPTY);

	public static final ImageDescriptor DESC_DLCL_CONFIGURE_ANNOTATIONS= createUnManaged(T_DLCL, "configure_annotations.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_ELCL_CONFIGURE_ANNOTATIONS= createUnManaged(T_ELCL, "configure_annotations.gif"); //$NON-NLS-1$
	
	public static final String IMG_OBJS_QUICK_ASSIST= NAME_PREFIX + "quickassist_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_QUICK_ASSIST = createManaged(T_OBJ, IMG_OBJS_QUICK_ASSIST);
	public static final String IMG_CORRECTION_ADD= NAME_PREFIX + "correction_add.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_CORRECTION_ADD = createManaged(T_OBJ, IMG_CORRECTION_ADD);
	public static final String IMG_CORRECTION_CHANGE= NAME_PREFIX + "correction_change.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_CORRECTION_CHANGE = createManaged(T_OBJ, IMG_CORRECTION_CHANGE);
	public static final String IMG_CORRECTION_RENAME= NAME_PREFIX + "correction_rename.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_CORRECTION_RENAME = createManaged(T_OBJ, IMG_CORRECTION_RENAME);
	public static final String IMG_CORRECTION_LINKED_RENAME= NAME_PREFIX + "correction_linked_rename.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_CORRECTION_LINKED_RENAME = createManaged(T_OBJ, IMG_CORRECTION_LINKED_RENAME);

	public static final String IMG_OBJS_NLS_NEVER_TRANSLATE= NAME_PREFIX + "never_translate.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_NLS_NEVER_TRANSLATE = createManaged(T_OBJ, IMG_OBJS_NLS_NEVER_TRANSLATE);

	public static final ImageDescriptor DESC_ELCL_NAVIGATE_BACKWARD = createUnManaged(T_ELCL, "backward_nav.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_ELCL_NAVIGATE_FORWARD = createUnManaged(T_ELCL, "forward_nav.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_ELCL_OPEN_DECLARATION = createUnManaged(T_ELCL, "goto_input.gif"); //$NON-NLS-1$

	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}
	
	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}
	
	public static Image get(String key) {
		return imageRegistry.get(key);
	}
	
	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}
	
	/*
	 * Creates an image descriptor for the given prefix and name in the JDT UI bundle. The path can
	 * contain variables like $NL$.
	 * If no image could be found, <code>useMissingImageDescriptor</code> decides if either
	 * the 'missing image descriptor' is returned or <code>null</code>.
	 * or <code>null</code>.
	 */
	private static ImageDescriptor create(String prefix, String name, boolean useMissingImageDescriptor) {
		IPath path= ICONS_PATH.append(prefix).append(name);
		return createImageDescriptor(CUIPlugin.getDefault().getBundle(), path, useMissingImageDescriptor);
	}
	
	/*
	 * Creates an image descriptor for the given prefix and name in the JDT UI bundle. The path can
	 * contain variables like $NL$.
	 * If no image could be found, the 'missing image descriptor' is returned.
	 */
	private static ImageDescriptor createUnManaged(String prefix, String name) {
		return create(prefix, name, true);
	}
	
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			CUIPlugin.log(e);
			return null;
		}
	}
	
	/*
	 * Creates an image descriptor for the given path in a bundle. The path can contain variables
	 * like $NL$.
	 * If no image could be found, <code>useMissingImageDescriptor</code> decides if either
	 * the 'missing image descriptor' is returned or <code>null</code>.
	 * Added for 3.1.1.
	 */
	public static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
		URL url= FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		if (useMissingImageDescriptor) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return null;
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *tool16 folders.
	 * 
	 * @param action	the action
	 * @param iconName	the icon name
	 */
	public static void setToolImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, T_TOOL, iconName);
	}
	
	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *lcl16 folders.
	 * 
	 * @param action	the action
	 * @param iconName	the icon name
	 */
	public static void setLocalImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, T_LCL, iconName);
	}

	/**
	 * Sets all available image descriptors for the given action.
	 */	
	public static void setImageDescriptors(IAction action, String type, String relPath) {
	    if (relPath.startsWith(NAME_PREFIX))
	        relPath= relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create("d" + type, relPath)); //$NON-NLS-1$
//		action.setHoverImageDescriptor(create("c" + type, relPath)); //$NON-NLS-1$
		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$

		// We are still not sure about this, let see TF results first.
		//		Use the managed version so that we ensure that there is no resource handle leaks
		//		Let the widget itself manage the disabled/hover attribution.  This was a huge leak
		//ImageDescriptor desc = getImageRegistry().getDescriptor(relPath);
		//if(desc == null) {
		//	desc = createManaged(T + "c" + type, relPath);
		//}	
		//action.setImageDescriptor(desc);
	}
	
	/**
	 * Helper method to access the image registry from the CUIPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
