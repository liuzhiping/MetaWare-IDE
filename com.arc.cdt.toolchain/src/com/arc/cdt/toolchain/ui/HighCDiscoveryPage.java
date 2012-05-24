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
package com.arc.cdt.toolchain.ui;


import java.util.List;

import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;



/**
 * The page for configuring how includes and symbols are discovered for standard makefiles.
 * @author davidp
 */
public class HighCDiscoveryPage extends AbstractDiscoveryPage {

    private static final String PROVIDER_ID = "HighCSpecsFile";

    private static final String DEFAULT_ARGS = "-Hbatchnotmp -Hnoobj ${plugin_state_location}/${specs_file}";

    private Text compileField;

    private Text argsField;

    public HighCDiscoveryPage() {
        super();
    }

    @Override
    protected boolean isValid () {
        return compileField.getText() != null && compileField.getText().trim().length() != 0;
    }

    @Override
    protected void populateBuildInfo (IScannerConfigBuilderInfo2 buildInfo) {
        buildInfo.setBuildOutputParserEnabled(true);
        buildInfo.setProviderOutputParserEnabled(PROVIDER_ID, true);
        buildInfo.setProviderRunCommand(PROVIDER_ID, this.compileField.getText());
        String args = argsField.getText();
        if (args == null)
            args = "";
        args = args.trim();
        if (args.length() > 0)
            args = " " + args;
        buildInfo.setProviderRunArguments(PROVIDER_ID, DEFAULT_ARGS + args);
    }


    @Override
    protected void restoreFromBuildinfo (IScannerConfigBuilderInfo2 buildInfo) {
        populateFrom(buildInfo);
    }

    private void populateFrom (IScannerConfigBuilderInfo2 buildInfo) {
        String providerID = PROVIDER_ID;
        String cmd = buildInfo.getProviderRunCommand(PROVIDER_ID);
        if (cmd == null){
            //Might be older stuff; look at other provider ID's that we used to use
            List<String> providerIds = buildInfo.getProviderIdList();
            for (String p: providerIds){
                cmd = buildInfo.getProviderRunCommand(p);
                if (cmd != null && cmd.length() > 0){
                    providerID = p;
                    break;
                }
            }
        }
        if (cmd == null) { cmd = "???"; }
        String args = buildInfo.getProviderRunArguments(providerID);
        compileField.setText(cmd);
        if (args != null)
            setArgsField(args);
    }

    /**
     * @todo davidp needs to add a method comment.
     * @param args
     */
    private void setArgsField (String args) {
        if (args != null && args.startsWith(DEFAULT_ARGS)) {
            String extras = args.substring(DEFAULT_ARGS.length());
            if (extras.startsWith(" "))
                extras = extras.substring(1);
            argsField.setText(extras);
        }
    }

    public void createControl (Composite parent) {
        ScrolledComposite scroller = new ScrolledComposite(parent,SWT.H_SCROLL|SWT.V_SCROLL);
        scroller.setExpandHorizontal(true);
        scroller.setExpandVertical(true);
        Composite page = new Composite(scroller,0);
        scroller.setContent(page);
        page.setLayout(new FormLayout());

        Label compileLabel = new Label(page, SWT.LEFT);
        compileLabel.setText("Compile command: ");
        FormData formData = new FormData();
        formData.left = new FormAttachment(0, 10);
        compileLabel.setLayoutData(formData);

        compileField = new Text(page,SWT.BORDER);

        formData = new FormData();
        formData.left = new FormAttachment(compileLabel, 10, SWT.RIGHT);
        formData.right = new FormAttachment(100, -10);
        compileField.setLayoutData(formData);

        String[] explanation = new String[] {
                " ",
                "The above command is invoked with the arguments",
                "   -Hbatch",
                "And the output is parsed to determine the location of include files",
                "and to determine implicitly-defined preprocessor symbols.",
                " ",
                "If your make file employs options that could affect these (e.g.,",
                "\"-Hansi\", \"-Ifoo\"), then specify them below.",
                " " };
        Control prev = compileLabel;
        for (String s : explanation) {
            Label label = new Label(page, SWT.LEFT);
            label.setText(s);
            formData = new FormData();
            formData.left = new FormAttachment(0, 10);
            formData.top = new FormAttachment(prev, 8, SWT.BOTTOM);
            label.setLayoutData(formData);
            prev = label;
        }

        Label argsLabel = new Label(page, SWT.LEFT);
        argsLabel.setText("Extra arguments: ");
        formData = new FormData();
        formData.left = new FormAttachment(0, 10);
        formData.top = new FormAttachment(prev, 20, SWT.BOTTOM);
        formData.right = new FormAttachment(compileLabel, 0, SWT.RIGHT);
        argsLabel.setLayoutData(formData);

        argsField = new Text(page, SWT.LEFT | SWT.BORDER);
        formData = new FormData();
        formData.left = new FormAttachment(argsLabel, 10, SWT.RIGHT);
        formData.top = new FormAttachment(prev, 20, SWT.BOTTOM);
        formData.right = new FormAttachment(100, -10);
        argsField.setLayoutData(formData);

        populateFrom(getContainer().getBuildInfo());
        
        scroller.setMinSize(page.computeSize(SWT.DEFAULT,SWT.DEFAULT));

        setControl(scroller);
    }

}
