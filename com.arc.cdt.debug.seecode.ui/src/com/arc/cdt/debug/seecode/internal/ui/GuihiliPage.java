/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.cdt.debug.seecode.internal.ui;


import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;

import org.dom4j.DocumentException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.options.ConfigurationException;
import com.arc.cdt.debug.seecode.options.ISeeCodeOptionsAugmenter;
import com.arc.cdt.debug.seecode.options.SeeCodeOptions;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.cdt.debug.seecode.ui.views.IContextHelpIds;
import com.arc.debugger.EngineLocator;
import com.arc.mw.util.ListAnalyzer;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.engine.config.OptFileResolver;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.metaware.guihili.EnvironmentFactory;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IExceptionHandler;
import com.metaware.guihili.IHelpHandler;
import com.metaware.guihili.PropertyStorage;


/**
 * A class for creating the Guihili options page.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class GuihiliPage {

    private static final int MIN_PREFERRED_WIDTH = 800;

    private static final int MIN_PREFERRED_HEIGHT = 600; // ARCompact requires this

    // private Composite mPanel;

    // private static final String TARGET = "target"; // key in attribute map

    private Gui fGUI;

    // private static final String GUIHILI_PROPERTIES =
    // "seecode.config.properties";

    private transient boolean mPendingPropertyChange = false;

    private Composite mParentControl;

    private boolean mCreated = false;

    private StyledText mOptions; // where seecode options placed.

    private List<String> mLastSeeCodeOptions = null;

    private Color mBlue = null;

    private transient boolean mPropertyChangeSuppressed = false;

    private long mTimeOfLastOptionUpdate = 0;

    private boolean mInvalidPagePending = false;

    private boolean mDirty = false;

    private String fPermanentErrorMessage = null;

    // private static final int DEFAULT_OPTIONS_HEIGHT = 40;

    private ISeeCodeOptionsAugmenter mSeeCodeOptionsAugmenter = null;

    private Runnable fInvokeWhenFirstDirtied;

    private IErrorSetter fErrorSetter;

    private Collection<String> mOriginalPropertyNames = null;

    public interface IErrorSetter {

        public void setErrorMessage (String msg);
    }

    /**
     * Create instance on behalf of a CMPD process number.
     * @param invokeWhenFirstDirtied called when properties are first dirted (to enable OK buttons, etc.)
     * @param errorSetter callback for setting error messages.
     */
    public GuihiliPage(Runnable invokeWhenFirstDirtied, IErrorSetter errorSetter) {
        this.fInvokeWhenFirstDirtied = invokeWhenFirstDirtied;
        this.fErrorSetter = errorSetter;
    }

    class OptionFieldUpdater extends Thread {

        private boolean _terminated = false;

        private boolean _updatePending = false;

        private static final int UPDATE_WAIT_TIME = 500;

        private Runnable _updateRunner;

        private Display _display;

        OptionFieldUpdater() {
            super("OptionFieldUpdater");
            _display = mParentControl.getShell().getDisplay();
            _updateRunner = new Runnable() {

                @Override
                public void run () {
                    updateSeeCodeOptions();
                }
            };

        }

        @Override
        public void run () {
            while (!_terminated) {
                synchronized (this) {
                    while (!_updatePending && !_terminated) {
                        try {
                            wait();
                        }
                        catch (InterruptedException e) {
                        }
                    }
                }
                if (!_terminated) {
                    try {
                        sleep(UPDATE_WAIT_TIME);
                        synchronized (this) {
                            _updatePending = false;
                        }
                        _display.asyncExec(_updateRunner);
                    }
                    catch (InterruptedException e) {
                    }
                }

            }
        }

        public void terminate () {
            _terminated = true;
            interrupt();
        }

        public synchronized void update () {
            if (!_updatePending) {
                _updatePending = true;
                notifyAll();
            }
        }
    }

    public Control getControl () {
        return mParentControl;
    }

    private OptionFieldUpdater mOptionFieldUpdater;

    private String fTarget;

    private int fProcessCount = 0;

    /**
     * Here we create the Guihili-based panel.
     */
    public void createControl (Composite parent) {
        // Foreground for SeeCode options
        mBlue = new Color(parent.getShell().getDisplay(), 0, 0, 255);
        // Guihili manager may have been created already
        // if "setDefaults" or "initializeFrom" called
        // first.
        // CORRECTION: we don't want to create the
        // control until we know the target platform.
        // if (mGui == null) createGui(null);
        // Create a dummy component that we will fill
        // in lazily when we know what the target is.

        mParentControl = new Composite(parent, 0);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(mParentControl, IContextHelpIds.PREFIX + "debug_options");
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        mParentControl.setLayoutData(gd);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        mParentControl.setLayout(layout);

        mParentControl.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed (DisposeEvent e) {
                if (fGUI != null) {
                    // disposes of images and such
                    fGUI.dispose();
                    fGUI = null;
                }
                if (mBlue != null)
                    mBlue.dispose();
                if (mOptionFieldUpdater != null)
                    mOptionFieldUpdater.terminate();
            }
        });

        if (fGUI != null) {
            createControlActual();
        }
    }

    protected void setDirty (boolean v) {
        mDirty = v;
    }

    public boolean isDirty () {
        return mDirty;
    }

    /**
     * Display page that indicates a missing, or unrecognizable, executable.
     * @param parent
     */
    private void displayInvalidPage (Composite parent) {
        if (mInvalidPagePending)
            return;
        mInvalidPagePending = true;
        Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        text.setLayoutData(gd);
        text
            .setText("\n\n\nThe associated program is not an\nELF executable or has an\nunrecognized target processor type.");
        final Font font = new Font(parent.getDisplay(), text.getFont().getFontData()[0].getName(), 16, SWT.ITALIC);
        text.setFont(font);
        text.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed (DisposeEvent e) {
                if (!font.isDisposed())
                    font.dispose();
            }
        });
        parent.layout(true);
    }

    private void createControlActual () {
        mCreated = false; // for benefit of property change listener
        fGUI.setFrame(mParentControl.getShell());
        IComponentFactory factory = UISeeCodePlugin.getWidgetFactory();
        fGUI.setComponentFactory(factory);
// ScrolledComposite scroller = new ScrolledComposite(mParentControl,SWT.V_SCROLL|SWT.H_SCROLL);
// scroller.setExpandVertical(true);
// scroller.setExpandHorizontal(true);
        Composite scroller = mParentControl;
        // We can't compute the Guihili panel until the
        // target machine is known.
        IContainer pane = factory.wrapContainer(scroller, IComponentFactory.NO_STYLE);
        MyLayout layout = new MyLayout();
        mParentControl.setLayout(layout);
        fGUI.setParent(pane);

        try {
            // Toggle.set("TRACE_GUI", true);
            fGUI.readXML("features");
        }
        catch (SAXParseException x) {
            String msg = "At " + x.getSystemId() + ", line " + x.getLineNumber();
            Exception e = x.getException();
            if (e != null)
                msg += ": " + e.getMessage();
            else
                msg += ": " + x.getMessage();            
            setErrorMessage("Internal error:\n" + msg);
            String scdir = (String)fGUI.getEnvironment().getSymbolValue("SCDIR");
            if (scdir == null) scdir = "";
            else scdir = "(at " + scdir + ")";
            UISeeCodePlugin.showError("(Internal Error) Debugger Options File Parse Error", 
            		"A syntax error was detected in one of the MetaWare Debugger's Options\n"+
            		"Description files "+
            		scdir + "\n\n" + msg);
        }
        catch (SAXException x) {
            Throwable e = x.getException();
            String msg;
            if (e != null) {
                if (e instanceof DocumentException){
                	DocumentException dx = (DocumentException)e;
                	if (dx.getNestedException() != null){
                		e = dx.getNestedException();
                	}              	
                }
                msg = e.getMessage();
                if (msg == null || msg.trim().length() == 0){
                	msg = e.toString();
                }
            }
            else
                msg = x.toString();
            setErrorMessage("Form description error\n(" + msg + ")");
            String scdir = (String)fGUI.getEnvironment().getSymbolValue("SCDIR");
            if (scdir == null) scdir = "";
            else scdir = "(at " + scdir + ")";
            UISeeCodePlugin.showError("Debugger Installation Error", 
                		"The MetaWare debugger " + scdir + 
                		"\nmay not be installed correctly or there is an internal error\n" +
                		"with one of its Debugger Options description files:\n\n"+
                		msg);
        }
        catch (FileNotFoundException x) {
            if (x.getMessage() != null && x.getMessage().indexOf("feature") >= 0) {
                setErrorMessage("Can't find the debugger on the search path");
                UISeeCodePlugin.showError("Installation Error", "Cannot find the MetaWare debugger on the search path");
            }
            else {
                setErrorMessage("The debugger seems to be missing option description files: " + x);
                String scdir = (String)fGUI.getEnvironment().getSymbolValue("SCDIR");
                if (scdir == null) scdir = "";
                else scdir = "(at " + scdir + ")";
                UISeeCodePlugin.showError("Installation Error", "The MetaWare debugger " + scdir + 
                		"\nmay not be installed correctly or there is an internal error\n" +
                		"with one of its Debugger Options description files:\n\n"+
                		"Cannot find the debugger description file: " +x);
            }
        }
        catch (Exception x) {
            setErrorMessage(x.toString());
            String scdir = (String)fGUI.getEnvironment().getSymbolValue("SCDIR");
            if (scdir == null) scdir = "";
            else scdir = "(at " + scdir + ") ";
            UISeeCodePlugin.showError("Options File Exception", 
            		"An unexpected exception occurred while reading a MetaWare Debugger\n"+
            		scdir +"Options Description file: " + x.toString());
        }
        Control[] children = scroller.getChildren();
        if (children.length > 0) { // should always be true
// scroller.setMinSize(children[0].computeSize(SWT.DEFAULT,SWT.DEFAULT));
// scroller.setContent(children[0]);
            constructOptionField(scroller);
        }
        else
            mOptions = null; // something messed up.
        mParentControl.layout(true);
        mCreated = true;
        mOriginalPropertyNames = fGUI.getPropertyNames(); // in case we need to revert.
        // If shell already rendered, then size may have increased; so we must
        // repack.
        // However, don't do it for Linux/GTK. For some reason, the dialog
        // just keeps getting wider and wider.
        // CORRECTON: packing just plain messes things up for some unknown reason.
        // The dialog gets pathalogically large on Windows with Eclipse 3.2
        // Just estimate a minimum size and reset the bounds without moving the
        // display, unless the display goes outside the screen
        if (mParentControl.getShell().isVisible()) {
            // NO. pack() has issues under Linux and under Windows with Eclipse 3.2
            // It causes the display to be pathologically large. Don't know why.
            // mParentControl.getShell().pack(true);
            Rectangle bounds = mParentControl.getShell().getBounds();
            Rectangle screenSize = mParentControl.getShell().getDisplay().getClientArea();
            boolean changed = false;
            if (bounds.width < MIN_PREFERRED_WIDTH) {
                bounds.width = Math.min(screenSize.width, MIN_PREFERRED_WIDTH);
                if (bounds.x + bounds.width > screenSize.width) {
                    bounds.x = screenSize.width - bounds.width;
                }
                changed = true;
            }
            if (bounds.height < MIN_PREFERRED_HEIGHT) {
                bounds.height = Math.min(screenSize.height, MIN_PREFERRED_HEIGHT);
                if (bounds.y + bounds.height > screenSize.height) {
                    bounds.y = screenSize.height - bounds.height;
                }
                changed = true;
            }
            if (changed)
                mParentControl.getShell().setBounds(bounds);
        }
        // printSizes(scroller,0);
    }

    /**
     * Contruct text field that displays seecode options; have it be now wider than the main dialog above it; it should
     * wrap.
     * @param mainDialog
     */
    private void constructOptionField (final Control mainDialog) {
        final Label label = new Label(mParentControl, SWT.LEFT);
        label.setText("Debugger Options: ");

        mOptions = new StyledText(mParentControl, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP /* |SWT.V_SCROLL */);
        mOptions.setData("name", "debugger_options"); // For GUI tester
        // We have the option field updated by a
        // timing thread so as to only update it once when
        // there are multiple property changes.
        mOptionFieldUpdater = new OptionFieldUpdater();
        mOptionFieldUpdater.start();

        updateSeeCodeOptions();

    }

    /**
     * Check if we need to read the Guihili. True if it hasn't yet been read, or if our target CPU has changed.
     * @param config
     */
    private void createGuihiliIfNecessary (
        String launchName,
        IProject project,
        String target,
        File workingDir,
        String[] env) { // If
        if (fGUI != null &&
            (fGUI.getEnvironment().getSymbolValue("MANAGED") == null ||
                fGUI.getEnvironment().getSymbolValue("MANAGED").equals("0") == isManaged(project) || 
                target != null && !target.equals(fTarget))) {
            fGUI.dispose();
            fGUI = null;
        }
        if (fGUI == null) {
            mInvalidPagePending = false;
            // If we're displaying Guihili from a previous
            // launch configuration, get rid of it.
            if (mParentControl != null) {
                Control prev[] = mParentControl.getChildren();
                for (int i = 0; i < prev.length; i++) {
                    prev[i].dispose();
                }
                if (mOptionFieldUpdater != null) {
                    mOptionFieldUpdater.terminate();
                    mOptionFieldUpdater = null;
                }
            }
        }
        if (target != null) {
            if (fGUI == null)
                createGuihili(launchName, project, target, workingDir, env);
        }
        else if (mParentControl != null)
            displayInvalidPage(mParentControl);
    }

    /**
     * Return whether or not the project of the executable being debugged is a "managed" project. If so, we grab target
     * information from the build properties.
     * @return whether or not the project of the executable being debugged is a "managed" project.
     */
    private boolean isManaged (IProject project) {
        if (project == null)
            return false;
        IProjectNature nature = null;
        try {
            // Prior to CDT 4.0, makefile projects were modelled as "non-managed".
            nature = project.getNature(ManagedCProjectNature.MNG_NATURE_ID);
            if (nature == null)
                return false;
        }
        catch (CoreException e) {
            // what to do?
            return false;
        }
        if (!ManagedBuildManager.manages(project))
            return false;
        // Now look if the project does indeed have the MetaWare toolchain. If not,
        // then we assume it to be a makefile project, and, therefore, is not managed.
        // FOLLOWING LOGIC COPIED FROM CDT code:
        IManagedBuildInfo mi = ManagedBuildManager.getBuildInfo(project);
        if (mi == null)
            return false;
        IConfiguration cfg = mi.getDefaultConfiguration();
        if (cfg != null && cfg.getBuilder() != null) {
            return cfg.getBuilder().isManagedBuildOn();
        }
        return false;
    }

    protected void setErrorMessage (String s) {
        fPermanentErrorMessage = s;
        fErrorSetter.setErrorMessage(s);
    }

    private Control focusedControl = null; // Control with the "focus"

    private String helpMessage = null;

    private static boolean isWindows () {
        return Platform.getOS().equals("win32");
    }

    /**
     * Detect if the conditions that cause cr96516 are present.
     * @param widget a widget that is about to get focus so that Help works.
     * @return true if the condition for cr96516 is present.
     */
    private static boolean hasSetFocusBug (Widget widget) {
        if (!(widget instanceof Button))
            return false;
        if (!isWindows())
            return false;
        Button button = (Button) widget;
        if ((button.getStyle() & SWT.RADIO) == 0)
            return false;
        if (button.getSelection())
            return false; // must not be set
        if (!(button.getParent() instanceof Group))
            return false;
        return true;
    }

    private static Button getRadioButtonWithSelection (Button button) {
        if (button.getSelection())
            return button;
        if (!(button.getParent() instanceof Group))
            return null;
        Group group = (Group) button.getParent();
        for (Control kid : group.getChildren()) {
            if (kid instanceof Button && (((Button) kid).getStyle() & SWT.RADIO) != 0 && ((Button) kid).getSelection())
                return (Button) kid;
        }
        return null;
    }

    private void createGuihili (
        String launchName,
        IProject project,
        final String target,
        File workingDir,
        String envStrings[]) {
        this.setErrorMessage(null);
        if (target == null)
            return;
        String scdir = EngineLocator.computeSCDIR(target, envStrings);
        IEnvironment env = EnvironmentFactory.create(workingDir, envStrings);
        final Gui g = new Gui(env, new OptFileResolver(scdir));
        g.setExceptionHandler(new IExceptionHandler() {

            @Override
            public void handleException (Throwable x) {
                SeeCodePlugin.log(x);
            }

            @Override
            public void handleException (String msg, Throwable x) {
                SeeCodePlugin.log(msg, x);

            }
        });

        g.setHelpHandler(new IHelpHandler() {

            @Override
            public void associateHelpMessage (IComponent c, final String msg) {
                if (c.getComponent() instanceof Control) {
                    final Control control = (Control) c.getComponent();
                    if (control.isDisposed())
                        return; // I don't know how, but this has been known to happen
                    control.getDisplay().asyncExec(new Runnable() {

                        private void addHelpListener (final Control widget, HelpListener listener) {
                            boolean added = false;
                            if (widget.isDisposed()) return; // Yes, it happens. Don't know how.
                            if (widget instanceof Composite) {
                                for (Control kid : ((Composite) widget).getChildren()) {
                                    addHelpListener(kid, listener);
                                    added = true;
                                }
                            }
                            if (!added) {
                                widget.addHelpListener(listener);
                                widget.addMouseTrackListener(new MouseTrackListener() {

                                    @Override
                                    public void mouseEnter (MouseEvent e) {
                                        focusedControl = widget;
                                        helpMessage = msg;
                                        // CR96516: setting focus to a radio button in a group incorrectly
                                        // selects the button under Windows! (Eclipse 3.3, 3.4)
                                        // So, undo the damage
                                        // <CR96516_HACK>
                                        if (hasSetFocusBug(widget)) {
                                            final Button buttonWithSelection = getRadioButtonWithSelection((Button) widget);
                                            widget.setFocus();
                                            if (buttonWithSelection != null) {
                                                control.getDisplay().asyncExec(new Runnable() {

                                                    @Override
                                                    public void run () {
                                                        ((Button) widget).setSelection(false);
                                                        if (!buttonWithSelection.getSelection()){
                                                            buttonWithSelection.setSelection(true);                                           
                                                            buttonWithSelection.notifyListeners(SWT.Selection,null);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                        else
                                            // </CR96516_HACK>
                                            widget.setFocus();
                                    }

                                    @Override
                                    public void mouseExit (MouseEvent e) {
                                        focusedControl = null;
                                    }

                                    @Override
                                    public void mouseHover (MouseEvent e) {
                                        // @todo Auto-generated method stub

                                    }
                                });
                            }
                        }

                        @Override
                        public void run () {

                            HelpListener helpListener = new HelpListener() {

                                @Override
                                public void helpRequested (HelpEvent e) {
                                    if (focusedControl != null) {
                                        HelpDialog.show(focusedControl, helpMessage);
                                    }
                                    else {
                                        HelpDialog.show(control, msg);
                                    }
                                }
                            };
                            addHelpListener(control, helpListener);

                        }
                    });
                }

            }
        });
        addSpecialPropertyListeners(g);
        fGUI = g;
        try {
            g.setProperty("whichOptions", "machine");
            g.setProperty("PROJECT", launchName);
            g.setProperty("SCXISS_WD", project != null && project.getLocation() != null?project.getLocation().toOSString():Utilities.getWorkspacePath());
            List<String> cores = new ArrayList<String>(Math.min(256, fProcessCount) + 1);
            cores.add("<Choose Core>");
            for (int i = 1; i <= fProcessCount; i++) {
                cores.add("" + i);
            }
            g.setProperty("CORE_LIST", cores);
        }
        catch (PropertyVetoException e) {
        }

        // IDE symbol is used to bypass GUI options
        env.putSymbolValue("IDE", "Eclipse");
        // We set "MANAGED" to "2" starting with IDE 8.4.0
        // In the future we may have guihili take into account such things.
        env.putSymbolValue("MANAGED", isManaged(project) ? "2" : "0");
        env.putSymbolValue("TARG", target.toUpperCase());
        String host = System.getProperty("os.name");
        env.putSymbolValue("HOST", host.indexOf("indows") > 0 ? "windows" : host.toLowerCase());
        env.putSymbolValue("SCDIR", scdir);
        env.putSymbolValue("CURRENT", "CURRENT_NOT_USED");
        env.putSymbolValue("XISS_SUPPORT", 1);
        // if (engine.build_id >= 1482)  // No engine is running at this point!! Assume it is latest.
            env.putSymbolValue("SCXISS_SUPPORT", 1);
            
        env.putSymbolValue("NO_DIR_XLATION", "1"); // IDE's directory translation is to be used
        setDirty(false);
        g.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange (PropertyChangeEvent event) {
                // System.out.println("Property " + event.getPropertyName() +
                // "="
                // + event.getNewValue());
                // We're only interested in property changes
                // after the dialog is rendered. Otherwise, they are
                // side-effects of reading the guihili file and we
                // don't care about them.
                if (mCreated && !mPropertyChangeSuppressed) {
                    if (mOptions != null) {
                        // Cannot update immediately, because the
                        // transitive updates of properties isn't
                        // necessarily complete.
                        // Also, one widget click may cause multiple
                        // property changes. Thus, we delay the options field
                        // update for about 500 milliseconds until the
                        // dust settles.
                        mOptionFieldUpdater.update();

                    }
                }
                if (!isDirty() && !mPendingPropertyChange) {
                    // Prevent infinite recursion. The act of updating
                    // the buttons will cause "performApply" which
                    // causes this call to be made recursively.
                    setDirty(true);
                    // performApply is called as an unfortunate side-effect of
                    // updating the buttons. Therefore, set a flag to skip the
                    // call logic. See performApply below.
                    mPendingPropertyChange = true;
                    // We must call updateButtons after all property
                    // change listeners have executed, or else things
                    // will be in an indeterminant state.
                    if (mParentControl != null) { /* We may not be showing yet */
                        Display d = mParentControl.getDisplay();
                        if (d != null && fInvokeWhenFirstDirtied != null)
                            d.asyncExec(new Runnable() {

                                @Override
                                public void run () {
                                    try {
                                    	if (!mParentControl.isDisposed())
                                            fInvokeWhenFirstDirtied.run();
                                    }
                                    finally {
                                        mPendingPropertyChange = false;
                                    }
                                }
                            });
                    }
                }
            }
        });
        if (mParentControl != null) {
            createControlActual();
        }
    }

    /* override */
    public void setDefaults (IGuihiliCallback configuration) {
        boolean dirty = isDirty();
        setDirty(true); // force regeneration
        setConfigurationFromGuihili(configuration, ISeeCodeOptionsAugmenter.PropState.DEFAULTS);
        if (!mCreated && mParentControl != null && fGUI != null) {
            createControlActual();
        }
        setDirty(dirty);
    }

    public String getProperty (String key) {
        return this.fGUI != null ? (String) fGUI.getProperty(key) : null;
    }

    private List<String> updateSeeCodeOptions () {
        // A configuration problem elsewhere could
        // prevent the options field from being created.
        if (mOptions == null)
            return null;
        // We may have a boundary case where the field
        // is being updated as the page is being closed...
        if (mOptions.isDisposed())
            return null;
        List<String> list = computeSeeCodeOptions();

        if (mLastSeeCodeOptions != null) {
            mOptions.setText("");
            ListAnalyzer a = new ListAnalyzer();
            a.analyze(list, mLastSeeCodeOptions);
            int cnt = a.getElementCount();
            for (int i = 0; i < cnt; i++) {
                String arg = a.getElement(i);
                int textSize = mOptions.getCharCount();
                if (a.followsDeletedElement(i)) {
                    if (textSize == 0) {
                        mOptions.append(" ");
                        textSize = 1;
                    }
                    mOptions.setStyleRange(new StyleRange(textSize - 1, 1, null, mBlue));
                }
                mOptions.append(StringUtil.arrayToArgString(new String[] { arg }));
                if (a.isNew(i)) {
                    mOptions.setStyleRange(new StyleRange(textSize, arg.length(), mBlue, null));
                }
                if (i + 1 < cnt)
                    mOptions.append(" ");
            }
        }
        else
            mOptions.setText(StringUtil.listToArgString(list));
        long thisTime = System.currentTimeMillis();
        // In case we have more than one update from a sequence
        // of property changes, don't update our "previous" list unless
        // time has elapsed.
        if (thisTime - mTimeOfLastOptionUpdate > 500) {
            mLastSeeCodeOptions = new ArrayList<String>(list);
            mTimeOfLastOptionUpdate = thisTime;
        }
        mOptions.layout(true);
        mOptions.getParent().layout(true);
        return list;
    }

    /**
     * @param configuration
     */
    private void setConfigurationFromGuihili (IGuihiliCallback configuration, ISeeCodeOptionsAugmenter.PropState state) {

        IProject project = configuration.getProject();
        String target = configuration.getTargetCPU();
        fProcessCount = configuration.getProcessCount();
        createGuihiliIfNecessary(
            configuration.getLaunchName(),
            project,
            target,
            configuration.getWorkingDirectory(),
            configuration.getEnvironment());
        fTarget = target;
        String errorKey = "CONFIG";
        error(errorKey, null);
        if (fGUI == null) // target processor not yet known; debug dialog will be blank
            return;
        try {
            applyBuildOptions(fGUI, state, project);
        }
        catch (ConfigurationException e1) {
            error(errorKey, e1.getMessage());
        }

        Collection<String> names = fGUI.getPropertyNames();
        List<String> swahiliArgs = configuration.getSwahiliArguments();
        if (isDirty() || swahiliArgs == null || swahiliArgs.size() == 0) {
            List<String> options = computeSeeCodeOptions();
            configuration.setSwahiliArguments(options);
        }

        // We must encode as map of strings; that's
        // the only kind that Eclipse configuration
        // stuff will take.
        Properties props = PropertyStorage.encode(names, fGUI);
        configuration.setProperties(props);

        // <HACK>
        // The xISS generates secret source files in ".xiss*" directories. These must be filtered
        // so that they don't mistakenly get used as source files.
        // We go ahead and add the exclusion filter unconditionally to the project. Even if the
        // user cancels this operation, or later retargets to non-xISS, the filter benignly remains.
        // If there is a better way of doing this, then by all means, change this.
        if (props != null && project != null && "XISS".equals(props.get("ARC_target"))) {
            excludeSourceFilesFrom(project, ".xiss*/**");
        }
        // </HACK>
    }

    /**
     * Given a project and an exclusion filter, append the filter to the list of patterns to be exluded as source
     * directories, unless the pattern is already there.
     * @param project the associated project.
     * @param filter the source-path pattern to excluce.
     */
    private static void excludeSourceFilesFrom (IProject project, String filter) {
        ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
        ICProjectDescription des = mngr.getProjectDescription(project, false);
        if (des != null) {
            ICConfigurationDescription cfgd = des.getDefaultSettingConfiguration();
            IPath pattern = new Path(filter);
            if (!doesContainExclusionPattern(project, cfgd, pattern)) {
                des = mngr.getProjectDescription(project, true); // get writable version
                ICConfigurationDescription cfgds[] = des.getConfigurations();
                for (ICConfigurationDescription c : cfgds) {
                    appendExclusionPattern(project, c, pattern);
                }
                try {
                    mngr.setProjectDescription(project, des); // required to flush cache of old stuff
                }
                catch (CoreException e) {
                    UISeeCodePlugin.log(e);
                }
            }
        }
    }

    private static boolean doesContainExclusionPattern (IProject project, ICConfigurationDescription cfgd, IPath pattern) {
        ICSourceEntry srcEntries[] = cfgd.getSourceEntries();
        boolean found = false;
        for (ICSourceEntry e : srcEntries) {
            if (project.getFullPath().equals(e.getFullPath())) {
                for (IPath p : e.getExclusionPatterns()) {
                    if (p.equals(pattern)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        return found;
    }

    private static void appendExclusionPattern (IProject project, ICConfigurationDescription cfgd, IPath pattern) {
        if (!doesContainExclusionPattern(project, cfgd, pattern)) {
            ICSourceEntry srcEntries[] = cfgd.getSourceEntries();
            ICSourceEntry e = null;
            for (ICSourceEntry entry : srcEntries) {
                if (project.getFullPath().equals(entry.getFullPath())) {
                    e = entry;
                    break;
                }
            }
            if (e == null) {
                ICSourceEntry newSrcEntries[] = new ICSourceEntry[srcEntries.length + 1];
                System.arraycopy(srcEntries, 0, newSrcEntries, 0, srcEntries.length);
                newSrcEntries[newSrcEntries.length - 1] = new CSourceEntry(project.getFullPath(),
                    new IPath[] { pattern }, 0);
                try {
                    cfgd.setSourceEntries(newSrcEntries);
                }
                catch (Exception e1) {
                    UISeeCodePlugin.log(e1);
                }
            }
            else {
                IPath patterns[] = e.getExclusionPatterns();
                IPath newPatterns[] = new IPath[patterns.length + 1];
                System.arraycopy(patterns, 0, newPatterns, 0, patterns.length);
                newPatterns[newPatterns.length - 1] = pattern;
                ICSourceEntry newE = new CSourceEntry(project.getFullPath(), newPatterns, 0);
                for (int i = 0; i < srcEntries.length; i++) {
                    if (srcEntries[i] == e) {
                        srcEntries[i] = newE;
                    }
                }
                try {
                    cfgd.setSourceEntries(srcEntries);
                }
                catch (Exception e1) {
                    UISeeCodePlugin.log(e1);
                }
            }
        }
    }

    /**
     * Compute SeeCode options to passed to Swahili
     */
    @SuppressWarnings("unchecked")
    private List<String> computeSeeCodeOptions () {
        // the "OK" action causes the ARG_ACTION property
        // to be set.
        Action action = fGUI.getAction(Gui.GEN_ARG_ACTION);
        if (action != null && isDirty()) {
            ArrayList<String> targs = new ArrayList<String>();
            try {
                // First will be "-targs=XXX"
                targs.add("-targs=" + fTarget.toUpperCase());
                // Don't issue property change from this action.
                mPropertyChangeSuppressed = true;
                fGUI.setProperty("ARG_ACTION", targs);
                // HACK: an old anchronism - "ACTION" is
                // a argument list that is appended to each
                // time "OK" action is fired.
                fGUI.setProperty("ACTION", null);
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Gui.GEN_ARG_ACTION));
            }
            catch (PropertyVetoException e) {
                e.printStackTrace();
            }
            finally {
                mPropertyChangeSuppressed = false;
            }
        }
        /*
         * else setErrorMessage("No OK action");
         */

        List<String> list = (List<String>) fGUI.getProperty("ARG_ACTION");
        List<String> slist;
        if (list != null) { // will be null if invoked from on-the-fly launcher.
            slist = new ArrayList<String>(list.size());
            slist.addAll(list);
        }
        else {
            // On the fly launch; all guihili defaults; just specify target.
            slist = new ArrayList<String>();
            slist.add("-targs=" + fTarget.toUpperCase());
            slist.add("-toggle=include_local_symbols=1");
            slist.add("-profile");
        }
        if (mSeeCodeOptionsAugmenter != null) {
            // Augment arguments from build config settings
            // (e.g., "-a7")
            String[] args = slist.toArray(new String[slist.size()]);
            args = mSeeCodeOptionsAugmenter.augmentArguments(args);
            slist = Arrays.asList(args);
        }
        return slist;
    }

    private static boolean containsSameElements (Collection<String> a, Collection<String> b) {
        return a != null && b != null && a.size() == b.size() && a.containsAll(b);
    }

// private static void P(String name, Collection<? extends Object>col){
// System.out.println();
// System.out.println(name);
// List<String> list = new ArrayList<String>(col.size());
// for (Object s: col){
// list.add((String)s);
// }
// Collections.sort(list);
// for (String s: list){
// System.out.println(s);
// }
// }

    /* override */
    public void initializeFrom (IGuihiliCallback configuration) {
        List<String> originalSwahiliArgs = configuration.getSwahiliArguments();
        IProject project = configuration.getProject();
        fTarget = configuration.getTargetCPU();
        fProcessCount = configuration.getProcessCount();
        if (fTarget != null) {
            createGuihiliIfNecessary(configuration.getLaunchName(), project, fTarget, configuration
                .getWorkingDirectory(), configuration.getEnvironment());
        }
        else
            fGUI = null;
        if (fGUI == null) {
            setDirty(false);
            return; // target CPU not recognized
        }
        try {
            mSeeCodeOptionsAugmenter = isManaged(project) ? SeeCodeOptions.readOptionMapping(fTarget, project) : null;
            if (mSeeCodeOptionsAugmenter != null){ // establish defaults 
                this.mSeeCodeOptionsAugmenter.augmentDefaults(fGUI.getPropertyMap());
            }
        }
        catch (ConfigurationException e1) {
            setErrorMessage(e1.getMessage());
        }
        try {
            Properties props = configuration.getProperties();
            if (props != null) {
                // P("GUI before",fGUI.getPropertyMap().keySet());
                // P("props",props.keySet());
                // P("Original",mOriginalPropertyNames);
                // Get rid of all new properties so that they will default.
                if (mOriginalPropertyNames != null) {
                    // Map<String,Object> map = fGUI.getPropertyMap();
                    // "retainAll" doesn't fire change listeners
                    // map.keySet().retainAll(mOriginalPropertyNames);
                    Collection<String> names = fGUI.getPropertyNames();
                    List<String> exclude = new ArrayList<String>(names);
                    exclude.removeAll(mOriginalPropertyNames);
                    for (String s : exclude) {
                        try {
                            fGUI.setProperty(s, null);
                        }
                        catch (PropertyVetoException e) {
                        }
                    }
                }
                // P("GUI after retaining",fGUI.getPropertyMap().keySet());
                boolean savePendingPropertyChange = mPropertyChangeSuppressed;
                try {
                    mPropertyChangeSuppressed = true; // suppress property changes tuff
                    PropertyStorage.decode(fGUI, props);
                }
                finally {
                    mPropertyChangeSuppressed = savePendingPropertyChange;
                }
                // P("GUI afterwards",fGUI.getPropertyMap().keySet());
            }
            applyBuildOptions(fGUI, props == null || props.size() == 0?ISeeCodeOptionsAugmenter.PropState.DEFAULTS:ISeeCodeOptionsAugmenter.PropState.LOADING, project);
        }
        catch (ConfigurationException e) {
            setErrorMessage(e.getMessage());
        }
        if (mParentControl != null && !mCreated) {
            createControlActual();
        }
        setDirty(true); // Force ARG_ACTION regeneration
        updateSeeCodeOptions();
        // If the project properties changed, then those arguments that are dependent on project properties may
        // have also changed.
        setDirty(!containsSameElements(computeSeeCodeOptions(), originalSwahiliArgs));
    }

    /**
     * Extract the target-specific compiler options (e.g. "-arc700") and set properties appropriately.
     * @param gui the guihili whose property values are to be updated.
     * @param launchConfig the corresponding launch configuration
     * @param applyingDefaults true if this is the initial defaults values of all properties.
     * @throws ConfigurationException
     */
    private void applyBuildOptions (Gui gui, ISeeCodeOptionsAugmenter.PropState state, IProject project) throws ConfigurationException {
        if (isManaged(project)) {
            ISeeCodeOptionsAugmenter aug = SeeCodeOptions.readOptionMapping(fTarget, project);
            if (aug != null)
                aug.augmentProperties(gui.getPropertyMap(), state);
            // else there is no correlation between build options and
            // debugger options.

            // If the project against which we are building was imported from
            // an old MetaDeveloper project space, retrieve the SeeCode options
            if (state == ISeeCodeOptionsAugmenter.PropState.DEFAULTS) {
                if (project != null) {
                    String configID = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration().getId();
                    Map<String, String> map = UISeeCodePlugin.getDefault().getDefaultSeeCodeOptions(project, configID);
                    if (map != null) {
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            try {
                                fGUI.setProperty(entry.getKey(), entry.getValue());
                            }
                            catch (PropertyVetoException e) {
                            }
                        }
                    }
                }

            }
        }
    }

    /* override */
    public void performApply (IGuihiliCallback configuration) {
        setConfigurationFromGuihili(configuration, ISeeCodeOptionsAugmenter.PropState.UPDATING);
        setDirty(false);
    }

    public void performOK () {
        if (fGUI != null) {
            // OK action may pop up errors or warnings...
            Action okAction = fGUI.getAction("OK");
            if (okAction != null) {
                okAction.actionPerformed(new ActionEvent(fGUI, ActionEvent.ACTION_PERFORMED, "OK"));
            }
        }
    }

    /**
     * A custom layout so that the SeeCode options field doesn't contribute to the total width of the dialog, since the
     * options field is suppose to wrap.
     * @author pickens
     * @currentOwner <a href="mailto:pickens@arc.com">pickens</a>
     * @version $Revision$
     * @lastModified $Date$
     * @lastModifiedBy $Author$
     * @reviewed 0 $Revision:1$
     */
    static class MyLayout extends Layout {

        private static final int VERTICAL_SPACING = 4;

        private static final int HORIZONTAL_MARGIN = 4;

        private static final int VERTICAL_MARGIN = 4;

        private static final int HORIZONTAL_SPACING = 4;

        private int _optionsFieldHeight = 0;

        private int _mainDialogHeight = -1;

        @Override
        protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
            int width = 0;
            int height = 0;
            _optionsFieldHeight = 0;
            Control mainDialog = null;
            Control optionsLabel = null;
            Control optionsField = null;
            for (Control kid : composite.getChildren()) {
                if (kid instanceof Label)
                    optionsLabel = kid;
                else if (kid instanceof StyledText)
                    optionsField = kid;
                else
                    mainDialog = kid;
            }
            if (mainDialog != null) {
            	 //NOTE: launch config dialog updates too frequently for no reason. 
                // Since Guihili for ARC is large, it causes a very noticeable delay. So,
                // ignore the fact that "flushCache" is true. (cr100049)
            	Point size = mainDialog.computeSize(wHint,hHint,false/*flushcache*/);
            	width = size.x;
            	height = size.y;
            	_mainDialogHeight = height;
            }
            int optionsFieldWidth = width;
            if (optionsLabel != null){
            	optionsFieldWidth = width - optionsLabel.computeSize(SWT.DEFAULT,SWT.DEFAULT,false).x - HORIZONTAL_SPACING;
            }
            if (optionsField != null) {
            	if (optionsFieldWidth <= 0) optionsFieldWidth = SWT.DEFAULT;
            	Point size = optionsField.computeSize(optionsFieldWidth,SWT.DEFAULT,flushCache);
                // Fudge optionsfield height by 4. Linux/GTK version seems to compute a height that is too short.
            	_optionsFieldHeight = size.y + 4;
            }
            
            return new Point(width + HORIZONTAL_MARGIN * 2, 
                _optionsFieldHeight + _mainDialogHeight + VERTICAL_SPACING +
                VERTICAL_MARGIN *
                2);
        }

        @Override
        protected void layout (Composite composite, boolean flushCache) {
            if (flushCache) {
                _mainDialogHeight = 0;
                _optionsFieldHeight = 0;
            }
            Rectangle area = composite.getClientArea();

            area.x += HORIZONTAL_MARGIN;
            area.width -= HORIZONTAL_MARGIN;
            area.y += VERTICAL_MARGIN;
            area.height -= VERTICAL_MARGIN;
            Control mainDialog = null;
            Control optionsLabel = null;
            Control optionsField = null;
            for (Control kid : composite.getChildren()) {
                if (kid instanceof Label)
                    optionsLabel = kid;
                else if (kid instanceof StyledText)
                    optionsField = kid;
                else
                    mainDialog = kid;
            }

            if (_mainDialogHeight <= 0 && mainDialog != null) {
                _mainDialogHeight = mainDialog.computeSize(area.width, SWT.DEFAULT, flushCache).y;
            }
            if (_optionsFieldHeight <= 0 && optionsLabel != null) {
                Point labelSize = optionsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
                _optionsFieldHeight = labelSize.y;
                int labelWidth = labelSize.x;
                // Fudge optionsfield hieght by 4. Linux/GTK version seems to compute a height that is too short.
                if (optionsField != null) {
                    int fieldWidth = area.width - labelWidth - HORIZONTAL_SPACING;
                    if (fieldWidth > 0)
                        _optionsFieldHeight = Math.max(
                            optionsField.computeSize(fieldWidth, SWT.DEFAULT, false).y + 4,
                            _optionsFieldHeight);
                }
            }
            int dialogHeight = area.height - _optionsFieldHeight - VERTICAL_SPACING;
            if (dialogHeight > _mainDialogHeight)
                dialogHeight = _mainDialogHeight;
            if (mainDialog != null) {
                mainDialog.setBounds(area.x, area.y, area.width, dialogHeight);
            }
            int labelWidth = 0;
            int yCoord = area.y + dialogHeight + VERTICAL_SPACING;
            int fieldHeight = area.height - yCoord;
            if (optionsLabel != null) {
                Point labelSize = optionsLabel.computeSize(SWT.DEFAULT, _optionsFieldHeight, false);
                labelWidth = labelSize.x;
                optionsLabel.setBounds(area.x, yCoord, labelSize.x, fieldHeight);
            }
            if (optionsField != null) {
                optionsField.setBounds(area.x + labelWidth + HORIZONTAL_SPACING, yCoord, area.width -
                    labelWidth -
                    HORIZONTAL_SPACING, fieldHeight);
            }

        }

    }

    private static Pattern startPattern = Pattern.compile("XISS_bridge_(\\d+)_start");

    private static Pattern targetPattern = Pattern.compile("XISS_bridge_(\\d+)_target_address");

    private static Pattern lengthPattern = Pattern.compile("XISS_bridge_(\\d+)_length");

    private static Pattern cpuPattern = Pattern.compile("XISS_bridge_(\\d+)_target");

    private static long toInteger (Object v) {
        if (v instanceof Number)
            return ((Number) v).longValue();
        if (v instanceof String) {
            String s = v.toString();
            if (s.startsWith("0x") || s.startsWith("0X"))
                return Long.parseLong(s.substring(2), 16);
            return Long.parseLong(s);
        }
        throw new NumberFormatException("Not a valid number " + v);
    }

    /**
     * Handle special properties (e.g., xISS Bridge stuff).
     * @param g
     */
    private void addSpecialPropertyListeners (Gui g) {
        handleXissStuff(g);
    }

    private Map<String, String> pendingErrors = null;

    private void error (String property, String msg) {
        if (msg != null && pendingErrors == null) {
            pendingErrors = new HashMap<String, String>(3);
        }
        if (pendingErrors != null) {
            if (msg == null)
                pendingErrors.remove(property);
            else
                pendingErrors.put(property, msg);
            if (fPermanentErrorMessage == null) {
                if (pendingErrors.size() == 0) {
                    fErrorSetter.setErrorMessage(null);
                }
                else {
                    for (String s : pendingErrors.values()) {
                        fErrorSetter.setErrorMessage(s);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Return the XISS bridge target corresponding to nothing. Originally it was "<Choose Core>". But in case it
     * changes, we find the alternate name.
     */
    private static String computeDisabledBridgeTarget (Gui g) {
        String tryIt = (String) g.getProperty("XISS_bridge_5_target");
        if (tryIt != null && tryIt.indexOf("oose") > 0)
            return tryIt;
        tryIt = (String) g.getProperty("XISS_bridge_7_target");
        if (tryIt != null && tryIt.indexOf("oose") > 0)
            return tryIt;
        tryIt = (String) g.getProperty("XISS_bridge_3_target");
        if (tryIt != null && tryIt.indexOf("oose") > 0)
            return tryIt;
        return "<Choose Core>";
    }

    private void handleXissStuff (final Gui g) {
        g.addPropertyChangeListener(new PropertyChangeListener() {

            private Set<String> boundProperties = new HashSet<String>();

            private boolean checkValue (String what, final PropertyChangeEvent evt) {
                long ivalue = 0;
                String errorKey = evt.getPropertyName();
                error(errorKey, null);
                try {
                    ivalue = toInteger(evt.getNewValue());
                    // A "8192" may be converting to "0x2000", which is OK.
                    // But we get into an error loop if "0" is being converted to "0x0".
                    if (evt.getOldValue() != null && ivalue == toInteger(evt.getOldValue()))
                        return false;
                }
                catch (NumberFormatException x) {
                    error(errorKey, what + " must be an integer multiple of 8192");
                    // restoreOldValue(evt);
                    return false;
                }

                if (ivalue % 0x2000 != 0 || ivalue == 0 && evt.getPropertyName().indexOf("length") > 0) {
                    error(errorKey, what + " must be a page (8K) multiple.");
                    // restoreOldValue(evt);
                    return false;
                }

                return true;
            }

// /**
// * @param g
// * @param evt
// * @throws PropertyVetoException
// */
// private void restoreOldValue(final PropertyChangeEvent evt)
// throws PropertyVetoException {
// PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
//
// public void run() {
// try {
// g.setProperty(evt.getPropertyName(), evt.getOldValue());
// } catch (PropertyVetoException e) {
// }
// }});
//                
// }

            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                // Deal with xISS bridge. When start address is set,
                // set the target address to it; and vice-versa.
                String name = evt.getPropertyName();
                boundProperties.remove(name); // no longer bound once modified.

                if (name.charAt(0) == 'X' && name.startsWith("XISS_bridge_")) {
                    Matcher m = startPattern.matcher(name);
                    String otherProperty = null;
                    if (m.matches()) {
                        if (!checkValue("Bridge start address", evt))
                            return;
                        otherProperty = "XISS_bridge_" + m.group(1) + "_target_address";
                    }
                    else {
                        m = targetPattern.matcher(name);
                        if (m.matches()) {
                            if (!checkValue("Bridge target address", evt))
                                return;
                            otherProperty = "XISS_bridge_" + m.group(1) + "_start";
                        }
                        else {
                            m = lengthPattern.matcher(name);
                            if (m.matches()) {
                                checkValue("Bridge length", evt);
                            }
                            else if (pendingErrors != null) {
                                m = cpuPattern.matcher(name);
                                if (m.matches()) {
                                    if (((String) evt.getNewValue()).indexOf("Choose") >= 0) {
                                        // This bridge was just disabled. Remove an pending errors
                                        // related to it.
                                        String which = m.group(1);
                                        String start = "XISS_bridge_" + which + "_start";
                                        String target = "XISS_bridge_" + which + "_target_address";
                                        String length = "XISS_bridge_" + which + "_length";
                                        try {
                                            if (pendingErrors.get(start) != null) {
                                                g.setProperty(start, "0x0");
                                            }
                                            if (pendingErrors.get(target) != null) {
                                                g.setProperty(target, "0x0");
                                            }
                                            if (pendingErrors.get(length) != null) {
                                                g.setProperty(length, "0x2000");
                                            }
                                        }
                                        catch (PropertyVetoException e) {
                                            // shouldn't get here.
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (otherProperty != null) {
                        if (!boundProperties.contains(otherProperty)) {
                            if ("0x0".equals(g.getProperty(otherProperty))) {
                                boundProperties.add(otherProperty);
                            }
                        }
                        if (boundProperties.contains(otherProperty)) {
                            try {
                                g.setProperty(otherProperty, evt.getNewValue());
                            }
                            catch (PropertyVetoException e) {

                            }
                            finally {
                                boundProperties.add(otherProperty); // restore it in case it was removed.
                            }
                        }
                    }
                }
                // If switching to non-xISS, then clear errors (cr96254) that are pending.
                // We assign valid values to erroneous entries, then disable bridge target
                // in case they re-enable xISS.
                // We must not do this is this thread because of setting property X to A triggers
                // a setting of property X to B will cause an infinite recursive loop in the
                // property change listener stuff.
                String target = (String) g.getProperty("ARC_target");
                if (!"XISS".equals(target) && pendingErrors != null && pendingErrors.size() > 0) {
                    mParentControl.getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run () {
                            if (pendingErrors == null) return; // in case of race condition
                            String disabledBridgeTarget = computeDisabledBridgeTarget(g); // e.g. "<Choose Core>"
                            for (Map.Entry<String, String> entry : pendingErrors.entrySet()) {
                                final String key = entry.getKey();

                                if (key.startsWith("XISS_bridge_")) {
                                    error(key, null);
                                    try {
                                        g.setProperty(key, key.endsWith("length") ? "0x2000" : "0x0");
                                    }
                                    catch (PropertyVetoException e1) {
                                    } // give a valid value
                                    int i = key.indexOf('_', 12);
                                    if (i > 0) {
                                        String coreSpec = key.substring(0, i) + "_target";
                                        // Disable faulty bridge spec
                                        try {
                                            g.setProperty(coreSpec, disabledBridgeTarget);
                                        }
                                        catch (PropertyVetoException e) {
                                        }
                                    }
                                }
                            }

                        }
                    });
                }
            }
        });
    }
}
