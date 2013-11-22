/*
 * IToolOptionSetting
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package com.arc.cdt.testutil;

import org.eclipse.cdt.managedbuilder.core.IOption;

import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetReference;


/**
 * An interface for setting MetaWare toolset properties.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IToolOptionSetting {
    /**
     * Return all of the compiler options that can be selected.
     *<P>
     * <B>NOTE:</B> client must not alter the state if resulting objects
     * directly. Must call {@link #setOptionValue} intead.
     * @return the list of compiler options that can be set.
     * @throws WidgetSearchException 
     */
    IOption[] getCompilerOptions() throws WidgetSearchException;
    
    /**
     * Return all of the linker options that can be selected.
     * <P>
     * <B>NOTE:</B> client must not alter the state if resulting objects
     * directly. Must call {@link #setOptionValue} intead.
     * @return the list of linker options that can be set.
     * @throws WidgetSearchException 
     */
    IOption[] getLinkerOptions() throws WidgetSearchException;
    
    /**
     * Return all of the assembler options that can be selected.
     * <P>
     * <B>NOTE:</B> client must not alter the state if resulting objects
     * directly. Must call {@link #setOptionValue} intead.
     * @return the list of assembler options that can be set.
     * @throws WidgetSearchException 
     */
    IOption[] getAssemblerOptions() throws WidgetSearchException;
    
    /**
     * Return all of the archiver options that can be selected.
     * <P>
     * <B>NOTE:</B> client must not alter the state if resulting objects
     * directly. Must call {@link #setOptionValue} intead.
     * @return the list of archiver options that can be set.
     */
    IOption[] getArchiverOptions();
    
    /**
     * Returns the setting of the corresponding widget for the given option.
     * The result will be Boolean, String, or String array, dependending on 
     * the type of the option.
     * @param option the option whose value is requested.
     * @return the value of the corresponding widget.
     * @throws WidgetSearchException 
     */
    Object getValue(IOption option) throws WidgetSearchException;
    
    /**
     * Sets the value of the option by locating the corresponding widget
     * and modifying it accordingly via <code>java.awt.Robot</code>.
     * <P>
     * The value is a Boolean, String, or String array, depending on the
     * type of the option.
     * 
     * @param option the option to be modified.
     * @param value new value, whose type is based on that of the option.
     */
    void setOptionValue(IOption option, Object value) throws WidgetSearchException;
    
    /**
     * Return whether or not the widget associated with the given option is
     * enabled.
     * @param option the option of interest.
     * @return whether or not the widget associated with the given option is
     * enabled.
     * @throws WidgetSearchException
     */
    boolean isEnabled(IOption option) throws WidgetSearchException;
    
    /**
     * Restore default values for all options by selecting the "Restore Default"
     * button.
     * @throws WidgetSearchException
     */
    void restoreDefault() throws WidgetSearchException;
    
    /**
     * Return the compiler command-line as it reflects the widgets settings.
     * @return the compiler command-line as it reflects the widgets settings.
     * @throws WidgetSearchException 
     */
    String getCompilerCommandLine() throws WidgetSearchException;
    
    /**
     * Return the linker command-line as it reflects the widgets settings.
     * @return the linker command-line as it reflects the widgets settings.
     * @throws WidgetSearchException 
     */
    String getLinkerCommandLine() throws WidgetSearchException;
    
    
    /**
     * Return the assembler command-line as it reflects the widgets settings.
     * @return the assembler command-line as it reflects the widgets settings.
     * @throws WidgetSearchException 
     */
    String getAssemblerCommandLine() throws WidgetSearchException;
    
    /**
     * Return the archiver command-line as it reflects the widgets settings.
     * @return the archiver command-line as it reflects the widgets settings.
     * @throws WidgetSearchException 
     */
    String getArchiverCommandLine() throws WidgetSearchException;

    /**
     * @return the name of the compiler as it appears in the build settings page.
     * @throws WidgetSearchException
     */
    public String getCompilerName () throws WidgetSearchException;

    /**
     * @return the name of the linker as it appears in the build settings page.
     * @throws WidgetSearchException
     */
    public String getLinkerName () throws WidgetSearchException;

    /**
     * @return the name of the assembler as it appears in the build settings page.
     * @throws WidgetSearchException
     */
    public String getAssemblerName () throws WidgetSearchException;

    /**
     * @return the name of the archiver as it appears in the build settings page.
     * @throws WidgetSearchException
     */
    public String getArchiverName () throws WidgetSearchException;
    
    /**
     * Return whether or not there is a linker specification; if not, we assume this is for a
     * static library that employs the archiver.
     * @return whether or not there is a linker specification.
     */
    public boolean hasLinker () throws WidgetSearchException;
    
    /**
     * Return a reference to the widget corresponding to an option.
     * @param option the option whose widget we desire.
     * @return the corresponding widget.
     * @throws WidgetSearchException if widget can't be located or is not visible.
     */
    IWidgetReference findWidgetLocatorForOption(IOption option) throws WidgetSearchException;
}
