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
package com.metaware.guihili.builder.legacy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.xml.sax.SAXException;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IWindow;
import com.metaware.guihili.Gui;
import com.metaware.guihili.builder.DialogBuilder;

/**
 * The Guihili "window". This is modal dialog box with a "Cancel" and possibly
 * "Help" and "Save" buttons on the bottom. It also has a project label in the
 * lower left.
 */
public class WindowBuilder extends DialogBuilder {
    private IContainer mMainPanel;

    public WindowBuilder(Gui gui) {
        super(gui);
        // setModal(true);
    }

    public void setShow_save(boolean v) {
        mShowSave = v;
    }

    @Override
    public void setDoc_title(String title) {
        mDocTitle = title;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        super.setTitle(name);
    }

    @Override
    public IContainer makeContainer() {
        IContainer contents = super.makeContainer();
        // We inherit the behavior from the old guihili processor in
        // which the name of this dialog is also the name of
        // the action that displays it.
        String name = getName();
        if (name != null) {
            setCreateAction(name);
        }
        IComponentFactory f = _gui.getComponentFactory();
        IContainer panel = f.makeContainer(contents,
                IComponentFactory.COLUMN_STYLE);
        IContainer mainPanel = f.makeGridContainer(panel, 1);
        // A bug in SWT implementation: if we don't instantiate
        // before instantiating the buttons panel, it gets
        // added out of order.
        mainPanel.getComponent(); // force instantiation;
        IContainer buttons = f
                .makeContainer(panel, IComponentFactory.ROW_STYLE);
        buttons.setHorizontalSpacing(5);
        buttons.setHorizontalWeight(1.0f);
        buttons.setHorizontalAlignment(IComponent.FILL);
        // final ILabel label = f.makeLabel(buttons, "PROJECT");
        // label.setHorizontalAlignment(IComponent.BEGINNING);
        // label.getComponent();
        // _gui.processProperty("PROJECT", new ITextWrapper() {
        // public void setText(String t) {
        // label.setText(t);
        // }
        // public String getText() {
        // return label.getText();
        // }
        // });
        mCancel = f.makeButton(buttons);
        mCancel.setHorizontalAlignment(IComponent.END);
        mCancel.setHorizontalWeight(1.0f);
        mCancel.setText(mShowSave ? "Cancel" : "OK");
        mCancel.getComponent();
        mSave = mShowSave ? f.makeButton(buttons) : null;
        IButton help = mDocTitle != null ? f.makeButton(buttons) : null;
        if (mSave != null) {
            mSave.setText("Save");
            mSave.getComponent();
        }
        if (help != null) {
            help.setText("Help");
            help.getComponent();
        }
        mMainPanel = mainPanel;
        return mainPanel;
    }

    @Override
    protected Object returnObject() throws SAXException {
        final IWindow dialog = (IWindow) super.returnObject();

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dialog.hide();
            }
        };
        // For time being, cancel and "ok" do same thing.
        mCancel.addActionListener(actionListener);
        if (mSave != null)
            mSave.addActionListener(actionListener);
        // A SWT bug requires this...
        _gui.removeEmptyContainers(mMainPanel);
        dialog.pack(); // pack after removing empty stuff.
        return dialog;
    }

    private String mDocTitle;

    private boolean mShowSave;

    private IButton mCancel;

    private IButton mSave;

}
