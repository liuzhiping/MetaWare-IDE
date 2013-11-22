package com.arc.cdt.toolchain.internal.ui.bcf;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ArcBcfWizardPage extends MBSCustomPage implements IBCFLocation {

    private static final String PAGE_ID = "com.arc.cdt.toolchain.bcf";

    private IBCFBlock bcfBlock;

    public ArcBcfWizardPage() {
        super(PAGE_ID);
    }

    @Override
    public String getName() {
        return "TCF Association";
    }

    @Override
    public void createControl(Composite parent) {
        bcfBlock = BcfChooserListBlock.create(parent);
        IProjectType pt = BcfUtils.getSelectedProjectType();
        boolean updated = false;
        // Choose the first configuration since they all have the same target processor
        if (pt != null) {
            IConfiguration configs[] = pt.getConfigurations();
            if (configs != null && configs.length > 0){
                bcfBlock.updateConfig(configs[0]);
                updated = true;
            }
        }
        if (!updated)
            bcfBlock.updateConfig(null);
        bcfBlock.addChangeListener(new IBCFBlock.IChangeListener() {

            @Override
            public void onChange(IBCFBlock block) {
                getWizard().getContainer().updateMessage();
                getWizard().getContainer().updateButtons();
            }
        });
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public Control getControl() {
        return bcfBlock != null ? bcfBlock.getControl() : null;
    }

    @Override
    public String getDescription() {
        return "Associates a Tool Configuration file to this project from which defaults are derived";
    }

    @Override
    public String getErrorMessage() {
        return bcfBlock != null ? bcfBlock.getErrorMessage() : null;
    }

    @Override
    public Image getImage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTitle() {
        return "ARChitect-generated Tool Configuration File";

    }

    @Override
    public void performHelp() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDescription(String description) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setImageDescriptor(ImageDescriptor image) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTitle(String title) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setVisible(boolean visible) {
        getControl().setVisible(visible);

    }

    @Override
    protected boolean isCustomPageComplete() {
        return getErrorMessage() == null;
    }

    @Override
    public File getBcfLocation() {
        return bcfBlock != null ? bcfBlock.getTcfLocation() : null;
    }

}
