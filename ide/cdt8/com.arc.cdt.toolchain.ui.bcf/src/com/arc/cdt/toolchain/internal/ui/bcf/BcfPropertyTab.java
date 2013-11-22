package com.arc.cdt.toolchain.internal.ui.bcf;

import java.io.File;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.arc.cdt.toolchain.internal.ui.bcf.IBCFBlock.IChangeListener;
import com.arc.cdt.toolchain.ui.bcf.Activator;
import com.arc.cdt.toolchain.ui.bcf.ToolChainFlavorFactory;

/**
 * The "CAT Synchronization" tab in the Project Properties Settings page.
 * Applies to ARC-type projects only.
 * @author pickensd
 *
 */
public class BcfPropertyTab extends AbstractCBuildPropertyTab {
    private ICPropertyProvider provider;
    private boolean dirty = false;
    private boolean updateLaunchOnApply = false;

    private static final String SUBSTRINGS[] = {".arc", ".av2em.", ".av2hs", ".a5."};

    /**
     * This function identifies project types for which ARChitect configuration information
     * is to be associated. 
     * @param projectType the project type.
     * @return true if it an ARC project type.
     */
    private static boolean isArcProjectType(IProjectType projectType){
        if (projectType == null) return false; //should not happen
        String id = projectType.getId();
        for (String s: SUBSTRINGS){
            if (id.indexOf(s) > 0)
                return true;
        }
        return false;
    }

    
    /**
     * This function identifies configurations  for which ARChitect configuration information
     * is to be associated. 
     * @param config the configuration
     * @return true if it an ARC project type.
     */
    private static boolean isArcConfiguration(IConfiguration config){
        if (config == null) return false; //should not happen
        if (!config.isManagedBuildOn()) return false;
        IProjectType projectType = config.getProjectType();
        if (projectType != null) return isArcProjectType(projectType);
        // A project created by "Convert Existing to Makefile project" won't have a project type
        // (probably a bug). Use the config name.
        String id = config.getBaseId();
        for (String s: SUBSTRINGS){
            if (id.indexOf(s) > 0)
                return true;
        }
        return false;
    }

    public BcfPropertyTab() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createControls(Composite parent, ICPropertyProvider provider) {
        assert(provider != null);
        super.createControls(parent,provider);
        this.provider = provider;
        createControlsLocal(usercomp);
    }

    @Override
    public boolean canBeVisible() {
        IConfiguration cfg = getCfg();
        if (cfg == null || !cfg.isManagedBuildOn()) return false;     
        return isArcConfiguration(cfg);       
    }

    @Override
    protected void performApply(ICResourceDescription src,
            ICResourceDescription dst) {
        applyTCF();			
    }
    
    private boolean associateSettingsFile(){
        IConfiguration cfg = getCfg();
        if (cfg != null) {
            File file = settingsFileBlock.getTcfLocation();
            if(file == null ){
                String message = settingsFileBlock.getErrorMessage();
                if(message != null)
                    BcfUtils.displayBCFError(message, usercomp);
            }
            //valid to proceed further
            boolean newBcf = BcfUtils.associateSettingsFile(file,cfg);
            dirty |= newBcf;
            return newBcf;
        }
        return false;
    }

    private void applyTCF() {
        IProject p = provider.getProject();
        IConfiguration cfg = getCfg();
        if (cfg != null) {
            boolean newBcf = associateSettingsFile();
            try {
                // We update build config if there is a new BCF.
                // Otherwise, it was updated by a "Synchronize".
                // We update the launch configs if we have a new bcf, or if 
                // there was a previous "Synchronize" action (which set updateLaunchOnApply variable)
                BcfUtils.applySettingsFileToConfig(settingsFileBlock.getTcfLocation(),cfg,newBcf,newBcf | updateLaunchOnApply);
                updateLaunchOnApply = false;
            } catch (RuntimeException e){
                throw e;
            } catch (Exception e) {
                displayErrorBox(e);

            }
        }
        if (dirty && p != null) {
            ManagedBuildManager.saveBuildInfo(p, true); 
            dirty = false;
        }
    }
    
    
    @Override
    protected void performOK(){
        applyTCF();
    }

    @Override
    protected void performDefaults() {
        final IConfiguration config = getCfg();
        if (config != null) {
            String path;
            try {
                path = BcfUtils.getTcfFilePath(config);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                displayErrorBox(ex);
                return;
            }
            settingsFileBlock.setTcfLocation(path);
            // After all defaults are set from plugin.xml, we must set the true
            // target version and sync with BCF config if set
            updateLaunchOnApply = false;
            this.usercomp.getDisplay().asyncExec(new Runnable(){
                @Override
                public void run(){
                    try {
                        ToolChainFlavorFactory.Get(config.getToolChain()).setProcessorFamily(config);
                    } catch (Exception e) {
                        Activator.log(e.getMessage(), e);
                    }
                    try {
                        BcfUtils.applySettingsFileToConfig(settingsFileBlock.getTcfLocation(),config,true,true);
                    } catch (Exception e) {
                        displayErrorBox(e);
                    } 
                }
            });
        }
    }

    @Override
    protected void updateData(ICResourceDescription rcfg) {
        IConfiguration cfg = getCfg();
        if (cfg != null) {
            settingsFileBlock.updateConfig(cfg);  
            try {
                settingsFileBlock.setTcfLocation(BcfUtils.getTcfFilePath(cfg));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                displayErrorBox(e);
            }
        }
    }

    @Override
    protected void updateButtons() {
    }

    private void setSyncButtonEnablement(){
        IConfiguration cfg = getCfg();
        syncButton.setEnabled(settingsFileBlock != null && settingsFileBlock.getTcfLocation() != null && cfg != null && settingsFileBlock.getErrorMessage() == null);
    }
 
    /*
    private void exportSettings(){
        FileDialog dialog = new FileDialog(errorLabel.getShell(),SWT.SAVE);
        dialog.setText("Export to settings file");
        dialog.setFilterExtensions(new String[]{BcfUtils.CFG_FILTER});
        dialog.setOverwrite(true);
        String fn = dialog.open();
        if (fn != null){
            File f = new File(fn);
            String baseName = f.getName();
            if (baseName.indexOf('.') < 0){
                fn += BcfUtils.CFG_FILTER.substring(1); // append ".cfg" if no extension given
            }
            IConfiguration cfg = rcfg != null? AbstractCBuildPropertyTab.getCfg(rcfg.getConfiguration()) : null;
            if (cfg != null)
                try {
                    BcfUtils.export(cfg, fn);
                }
                  catch (RuntimeException x){
                      throw x;
                } catch (Exception ex) {
                    IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                            ex.getMessage(), ex);
                    ErrorDialog.openError(errorLabel.getShell(), "Write Error", ex.getMessage(), status);
                    Activator.log(ex.getMessage(), ex);
                }
        }
    }
    */

    private void createControlsLocal(Composite parent){
        parent.setLayout(new GridLayout(1,true));
        final Composite panel = new Composite(parent,SWT.NONE);
        //panel.setLayoutData(new GridData(GridData.FILL_BOTH));
        panel.setLayout(new GridLayout(1,true));
        panel.setLayoutData(new GridData(GridData.FILL_BOTH));

        errorLabel = new Label(panel,SWT.LEFT);
        errorLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));

        settingsFileBlock = BcfChooserListBlock.create(panel);
        settingsFileBlock.addChangeListener(new IChangeListener(){

            @Override
            public void onChange(IBCFBlock block) {
                String msg = block.getErrorMessage();
                errorLabel.setText(msg != null?msg:"");
                panel.layout();
                setSyncButtonEnablement();
                if (msg == null || msg.length() == 0)
                    associateSettingsFile();

            }});
        settingsFileBlock.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        final Composite statusSyncPanel = new Composite(panel,SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalAlignment = GridData.END;
        gd.verticalIndent = 50;
        statusSyncPanel.setLayoutData(gd);
        statusSyncPanel.setLayout(new GridLayout(3,false));
        
        
        syncStatusLabel = new Label(statusSyncPanel,SWT.LEFT);
        syncStatusLabel.setText("");
        Font f = syncStatusLabel.getFont();
        // Make label italic and smaller than other fonts so as to make it less obtrusive.
        f = new Font(syncStatusLabel.getDisplay(), f.getFontData()[0].getName(), f.getFontData()[0].getHeight(), SWT.ITALIC);
        syncStatusLabel.setFont(f);
        syncStatusLabel.setForeground(syncStatusLabel.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        
        syncButton = new Button(statusSyncPanel,SWT.PUSH);
        syncButton.setText("Synchronize");
        syncButton.setToolTipText("Synchronize this project's setting with those of the selected defaults file");

        syncButton.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                IConfiguration cfg = getCfg();
                if (cfg != null) {
                    try {
                        // Don't update launches until "Apply" or "OK", in case of Cancel.
                        // but build config is not flushed if Cancel.
                        dirty |= BcfUtils.applySettingsFileToConfig(settingsFileBlock.getTcfLocation(),cfg,true,false);
                        updateLaunchOnApply = true;
                        syncStatusLabel.setText("Synchronization with TCF file completed.     ");
                        panel.layout();
                        final Display display = panel.getDisplay();
                        // With 3 seconds and dismiss "Completed" message.
                        Thread t = new Thread("Sync status thread"){
                            @Override
                            public void run(){
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    //Shouldn't get here
                                }
                                display.syncExec(new Runnable(){

                                    @Override
                                    public void run() {
                                        if (!display.isDisposed() && !panel.isDisposed()){
                                            clearSyncStatusLabel(panel);
                                        }
                                        
                                    }});
                            }
                        };
                        t.start();
                    } catch (Exception ex) {
                        displayErrorBox(ex);
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }});
        
        /*Button exportButton = new Button(statusSyncPanel,SWT.PUSH);
        exportButton.setText("Export...");
        exportButton.setToolTipText("Export current configuration settings into a configuration defaults file" );
        exportButton.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                exportSettings();                
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {              
            }});

        settingsFileBlock.addChangeListener(new IBCFBlock.IChangeListener(){

            @Override
            public void onChange(IBCFBlock block) {
                clearSyncStatusLabel(panel);
                setSyncButtonEnablement();				
            }});
            */
        setSyncButtonEnablement();
    }
    
    private void clearSyncStatusLabel(Composite panel){
        syncStatusLabel.setText("");
        panel.layout();
    }

    private void displayErrorBox(Exception ex) {
        BcfUtils.displayBCFError(ex, usercomp);
    }

    private IBCFBlock settingsFileBlock;
    private Button syncButton;
    private Label errorLabel;
    private Label syncStatusLabel;

}
