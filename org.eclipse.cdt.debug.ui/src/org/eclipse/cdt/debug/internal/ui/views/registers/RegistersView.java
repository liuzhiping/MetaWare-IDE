

package org.eclipse.cdt.debug.internal.ui.views.registers;


import org.eclipse.cdt.debug.internal.ui.views.registers.RegistersViewer.Format;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;


public class RegistersView extends AbstractDebugView implements ISelectionListener, IDebugEventSetListener {
    
    public static String REG_VIEW_ID = "org.eclipse.cdt.debug.ui.RegisterView";
    
    //private static String BANK_CONFIG_KEY = "org.eclipse.cdt.debug.ui.register_viewer.bank";
    //private static String FORMAT_CONFIG_KEY = "org.eclipse.cdt.debug.ui.register_viewer.FORMAT";

    private RegistersViewer viewer;

    private boolean fHidden;

    private ComboContribution fBankCombo = null;

    private IToolBarManager fToolBarManager = null;

    private IStackFrame fStackFrame = null;

    private static final Format FORMATS[] = {
            Format.HEXADECIMAL,
            Format.SIGNED_DECIMAL,
            Format.UNSIGNED_DECIMAL,
            Format.FLOAT,
            Format.BINARY,
            Format.FRACTIONAL_31,
            Format.OCTAL };

    private ComboContribution fFormatCombo;

    private boolean fToolBarCleared = true;

    private ComboContribution fFilterCombo;

    private IDebugTarget fTarget;

    @Override
    protected void configureToolBar (IToolBarManager tbm) {
        fToolBarManager = tbm;
    }

    private void populateToolBar (IStackFrame frame) {
        IToolBarManager tbm = fToolBarManager;
        if (tbm == null)
            return;
        boolean update = false;
        if (fToolBarCleared) {
            fToolBarCleared  = false;
            String filterTip = "Apply a filter to display a subset of registers";
            tbm.add(new LabelContribution("Label_Filter_1", " Filter: ", filterTip));

            SelectionListener listener = new SelectionListener() {

                public void widgetDefaultSelected (SelectionEvent e) {
                    widgetSelected(e);
                }

                public void widgetSelected (SelectionEvent e) {
                    viewer.applyFilter(((CCombo) e.getSource()).getText());
                }
            };

            if (fFilterCombo == null)
                fFilterCombo = new ComboContribution("filter", true, filterTip, listener, null);
            else fFilterCombo.reset();
            tbm.add(fFilterCombo);

            if (fFormatCombo == null)
                fFormatCombo = new ComboContribution("format", false, "Display format", new SelectionListener() {

                    public void widgetDefaultSelected (SelectionEvent e) {
                        // @todo Auto-generated method stub

                    }

                    public void widgetSelected (SelectionEvent e) {
                        viewer.setFormat(FORMATS[((CCombo) e.getSource()).getSelectionIndex()]);

                    }
                }, FORMATS);
            else
                fFormatCombo.reset();
            tbm.add(fFormatCombo);

            if (fBankCombo == null) {
                fBankCombo = new ComboContribution("banks", false, "Select register bank", new SelectionListener() {

                    public void widgetDefaultSelected (SelectionEvent e) {
                    }

                    public void widgetSelected (SelectionEvent e) {
                        viewer.setBank(((CCombo) e.getSource()).getSelectionIndex());

                    }
                }, null);
            }
            else fBankCombo.reset();
            tbm.add(fBankCombo);
            // <SWTBUG>: if the toolbar doesn't have any buttons (e.g., only
            // comboboxes), neither Windows nor Linux version of SWT (as of 3.3.2) 
            // computes the height correctly. Windows renders it too short and Linux renders
            // to high.
            // We must add a dummy button with an invisible 1-pixel
            // wide icon. It won't be seen in the toolbar unless the mouse happens
            // to go over it. If anyone has a better solution, please make appropriate
            // corrections. This problem has already cost me a day! (D.P.)
            addDummyButtonHackForWindows(tbm);
            // </SWTBUG>
            update = true;

        }
        if (viewer.getBank() != fBankCombo.getSelectionIndex()){
            fBankCombo.setSelectionIndex(viewer.getBank());
        }
        if (viewer.getFormat() != fFormatCombo.getSelection()){
            fFormatCombo.setSelection(viewer.getFormat());
        }
        if (fillBankCombo(frame) || update)
            tbm.update(true);
    }

    /**
     * Append a dummy button to the toolbar to get around a Windows toolbar rendering
     * bug in Eclipse 3.3
     * @param tbm the toolbar manager being appended to.
     */
    private static void addDummyButtonHackForWindows (IToolBarManager tbm) {
        ImageRegistry imageRegistry = CDebugUIPlugin.getDefault().getImageRegistry();
        final String DUMMY_ICON = "IconToFixWindowsSWTBug";
        ImageDescriptor image = imageRegistry.getDescriptor(DUMMY_ICON);
        if (image == null) {
            final ImageData data = new ImageData(1, 15, 1, new PaletteData(new RGB[] {
                    new RGB(0, 0, 0),
                    new RGB(255, 255, 255) }));
            data.transparentPixel = 0;
            image = new ImageDescriptor() {

                @Override
                public ImageData getImageData () {
                    return data;
                }
            };
            imageRegistry.put(DUMMY_ICON, image);
        }
        Action dummyAction = new Action("", image) {

            @Override
            public void run () {
            }

        };
        dummyAction.setId("dummy1");
        tbm.add(dummyAction);
    }

    @Override
    protected void createActions () {
        // @todo Auto-generated method stub

    }

    @Override
    protected Viewer createViewer (Composite parent) {
        viewer = new RegistersViewer(parent);
        getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
        DebugPlugin.getDefault().addDebugEventListener(this);
        fHidden = false;
        setViewerInput(getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW));
        return viewer;
    }

    @Override
    protected void fillContextMenu (IMenuManager menu) {
        // @todo Auto-generated method stub

    }

    @Override
    protected String getHelpContextId () {
        return "org.eclipse.cdt.debug.ui.register_view";
    }

    /**
     * Called each time a selection changes in the Debug View. {@inheritDoc} Implementation of overridden (or abstract)
     * method.
     * @param part
     * @param selection
     */
    public void selectionChanged (IWorkbenchPart part, ISelection selection) {
        setViewerInput(selection);
    }

    private void setViewerInput (ISelection selection) {
        if (selection instanceof IStructuredSelection && this.isVisible()) {
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            IStackFrame frame = null;
            
            if (obj instanceof IStackFrame) {
                frame = (IStackFrame)obj;

            }
            else if (obj instanceof IThread) {
                IThread thread = (IThread) obj;
                IStackFrame[] frames;
                try {
                    frames = thread.getStackFrames();
                }
                catch (DebugException e) {
                    viewer.setInput(null);
                    return; // something went wrong
                }
                if (frames.length > 0) {
                    frame = frames[0];
                }
            }
            
            if (frame != fStackFrame) {
                viewer.setInput(frame);
                fStackFrame = frame;               
                if (frame != null) {
                    fTarget = frame.getDebugTarget();
                    populateToolBar(frame);  
                }                 
            }
            // selection goes null when entering a stack frame.
            // Don't clear toolbar in such a case to avoid flicker
            // as it is immediately repopulated.
            if (frame == null && obj != null && fToolBarManager != null && !fToolBarCleared) {
                clearToolBar();
            }
        }
    }

    /**
     * @todo davidp needs to add a method comment.
     */
    private void clearToolBar () {
        fToolBarCleared = true;
        fToolBarManager.removeAll();
        fToolBarManager.update(true);
    }

    /**
     * Fill in the Bank combobox with the names of register banks if they have changed.
     * We may be switching between multiple instances of the debugger with different targets.
     * @param frame the stack frame.
     * @return true if anything changed.
     */
    private boolean fillBankCombo (IStackFrame frame) {
        boolean changed = false;
        try {
            IRegisterGroup groups[] = frame.getRegisterGroups();
            if (fBankCombo != null) {
                Object existingItems[] = fBankCombo.getItems();
                boolean regenerate = false;
                if (existingItems != null && existingItems.length == groups.length + 1) {
                    for (int i = 0; i < groups.length; i++) {
                        if (!existingItems[i + 1].equals(groups[i].getName())) {
                            regenerate = true;
                            break;
                        }
                    }
                }
                else
                    regenerate = true;
                if (regenerate) {
                    String items[] = new String[groups.length + 1];
                    items[0] = "All";
                    for (int i = 0; i < groups.length; i++) {
                        items[i + 1] = groups[i].getName();
                    }
                    fBankCombo.setItems(items);
                    changed = true;
                }
            }
        }
        catch (DebugException e) {
             // to messed up to compute register banks
        }
        return changed;
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    @Override
    public void dispose () {
        if (fToolBarManager != null && !fToolBarCleared) {
            fToolBarCleared = true;
            fToolBarManager.removeAll();
        }
        super.dispose();
        DebugPlugin.getDefault().removeDebugEventListener(this);
        getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
        viewer.getControl().dispose();
        viewer = null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
     */
    @Override
    protected void becomesHidden () {
        viewer.setVisible(false);
        fHidden = true;
        DebugPlugin.getDefault().removeDebugEventListener(this);
        super.becomesHidden();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
     */
    @Override
    protected void becomesVisible () {
        super.becomesVisible();
        viewer.setVisible(true);
        if (fHidden) {
            fHidden = false;
            DebugPlugin.getDefault().addDebugEventListener(this);
        }
        setViewerInput(getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW));
    }

    public void handleDebugEvents (DebugEvent[] events) {
        // Check for register value changes by the user and update
        for (DebugEvent event : events) {
            if (event.getKind() == DebugEvent.CHANGE && event.getSource() instanceof IRegister) {
                handleRegisterChangeEvent(event);
            }
            else if (event.getKind() == DebugEvent.TERMINATE && event.getSource() instanceof IDebugTarget){
                handleTargetTerminationEvent(event);
            }
            else if (event.getKind() == DebugEvent.CREATE && event.getSource() instanceof IDebugTarget){
                handleTargetCreationEvent(event);
            }
            else if (event.getKind() == DebugEvent.RESUME && event.getSource() instanceof IDebugTarget){
                handleTargetResumeEvent(event);
            }
        }
    }

    /**
     * Called when a register-change event occurs.
     * @param event the register-change event
     */
    private void handleRegisterChangeEvent (DebugEvent event) {
        if (!fHidden && viewer != null) {
            viewer.update((IRegister) event.getSource());
        }
    }
    
    /**
     * Called when a process resumes.
     * @param event the resume event.
     */
    private void handleTargetResumeEvent (DebugEvent event) {
        if (!fHidden && viewer != null && this.fStackFrame != null &&
            this.fStackFrame.getDebugTarget() == event.getSource()){
            this.asyncExec(new Runnable() {
                public void run () {
                    viewer.clearDiffs();
                }
            });
        }
    }
    
    /**
     * Called when a debug session terminates.
     * @param event the debug session termination event.
     */
    private void handleTargetTerminationEvent (DebugEvent event) {
        IDebugTarget target = (IDebugTarget)event.getSource();
        if (target == fTarget){
            this.asyncExec(new Runnable() {
                public void run () {
                    clearToolBar();
                }
            });
        }
//        ILaunch launch = target.getLaunch();
//        if (launch != null && viewer != null && launch.getDebugTarget() == target) { // primary target?
//            ILaunchConfiguration config = launch.getLaunchConfiguration();
//            if (config != null)
//                saveState(config);
//        }
    }
    
    /**
     * Called when a debug session terminates.
     * @param event the debug session termination event.
     */
    private void handleTargetCreationEvent (DebugEvent event) {
        IDebugTarget target = (IDebugTarget)event.getSource();
        ILaunch launch = target.getLaunch();
        if (launch != null && launch.getDebugTarget() == target) { // primary target?
            ILaunchConfiguration config = launch.getLaunchConfiguration();
            if (config != null)
                restoreState(config);
        }
    }
    
//    private void saveState (ILaunchConfiguration config) {
//        // Save the selected bank and format in the Launch Configuration
//        // TODO
//    }
    
    private void restoreState(ILaunchConfiguration config){
     // Restore the selected bank and format in the Launch Configuration
        // TODO
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param site
     * @param memento
     * @throws PartInitException
     */
    @Override
    public void init (IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        setPartName("Registers");
    }
    
    @Override
    public String getTitleToolTip(){
        return "Tabular registers view.";
    }
    
}
