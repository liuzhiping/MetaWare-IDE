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
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URL;

import org.xml.sax.SAXException;

import com.arc.widgets.IButton;
import com.arc.widgets.IChoice;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFileChooser;
import com.arc.widgets.IImage;
import com.arc.widgets.ITextField;
import com.metaware.guihili.Gui;
import com.metaware.guihili.builder.ButtonBuilder;

/**
 * Construct a button that brings up a File dialog. A property is set
 * appropriately.
 */
public class FileDialogBuilder extends ButtonBuilder {
    public FileDialogBuilder(Gui gui) {
        super(gui);
    }

    public void setDirectory(String dir) {
        mDir = dir;
    }

    @Override
    public void setProperty(String name) {
        mProperty = name;
    }

    // Target component; typically a textfield or editable combobox
    public void setTarget(String name) {
        mTarget = name;
    }
    
    public void setQuoteIfSpaces(boolean v){
        mQuoteIfSpaces = v;
    }
    
    public void setUnix(boolean v){
        mUnix  = v;
    }

    protected boolean doDirectoriesOnly() {
        return false;
    }

    @Override
    public Object returnObject() throws SAXException {
        final IButton b = (IButton) super.returnObject();
        if (b.getImage() == null && b.getText() == null
                || b.getText().length() == 0) {
            URL gifUrl = FileDialogBuilder.class.getResource("browse.gif");
            IImage image = _gui.getComponentFactory().makeImage(gifUrl);
            b.setImage(image);
        }
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (mFileChooser == null) {
                    int style = IComponentFactory.FILE_OPEN;
                    if (doDirectoriesOnly())
                        style |= IComponentFactory.FILE_DIRS;
                    mFileChooser = _gui.getComponentFactory().makeFileChooser(
                            _gui.getFrame(), style);
                    if (mDir != null)
                        mFileChooser.setDirectory(new File(mDir));
                }
                
                String args = null; // attempt to preserve arguments

                if (mProperty != null) {
                    Object fn = _gui.getProperty(mProperty);
                    File f = null;
                    if (fn instanceof File) f = (File)fn;
                    else if (fn instanceof String && ((String)fn).length() > 0) {
                        String s = fn.toString();
                        if (s.startsWith("\"")){
                            int qi = s.indexOf('"',1);
                            if (qi > 0) {
                                args = s.substring(qi+1).trim();
                                s = s.substring(1,qi);
                            }
                        }
                        else if (mQuoteIfSpaces){
                            int si = s.indexOf(' ');
                            if (si > 0) {
                                args = s.substring(si).trim();
                                s = s.substring(0,si);
                            }
                        }
                        if (s.equals(".")){
                            s = _gui.getEnvironment().getWorkingDirectory().getPath();  //Use working directory that process will use.
                        }
                        f = new File(s);
                    }
                    if (f != null && !f.isAbsolute()){
                        f = new File(_gui.getEnvironment().getWorkingDirectory(),f.toString());
                    }
                    if (f != null && f.getParentFile() != null) {
                        mFileChooser.setDirectory(f.getParentFile());
                        mFileChooser.setFile(f);
                    }
                    else {
                        File d;
                        if (mDir == null || mDir.equals("."))
                            d = _gui.getEnvironment().getWorkingDirectory();
                        else d = new File(mDir);
                        mFileChooser.setDirectory(d);
                    }
                }
                File result = mFileChooser.open();
                if (result != null) {
                    try {
                        String file = result.toString();
                        if (mUnix && File.separatorChar == '\\' && file.indexOf('\\')>= 0){
                            file = file.replaceAll("\\\\", "/");
                        }
                        if (mQuoteIfSpaces) {
                            if (file.indexOf(' ') > 0){
                                file = "\"" + file + "\"";
                            }
                        }
                        if (args != null && args.length() > 0){
                            file = file + " " + args;
                        }
                        if (mProperty != null)
                            _gui.setProperty(mProperty, file);
                        else if (mTarget != null) {
                            IComponent c = _gui.getComponent(mTarget);
                            if (c instanceof IContainer){
                                // Assume to be a labeled combo or textfield.
                                IComponent kids[] = ((IContainer)c).getChildren();
                                if (kids.length == 2){
                                    c = kids[1];
                                }
                            }
                            if (c instanceof ITextField)
                                ((ITextField) c).setText(file);
                            else if (c instanceof IChoice)
                                ((IChoice) c).setSelection(file);
                            else
                                _gui.handleException("filedialog target "
                                        + mTarget
                                        + " not defined or invalid "
                                        + (c == null ? "" : "instance of "
                                                + c.getClass().getName()),
                                        new Error());
                        }
                    } catch (PropertyVetoException x) {
                    }
                }
            }
        });
        return b;
    }

    private IFileChooser mFileChooser;

    private String mProperty;

    private String mTarget;

    private String mDir;
    
    private boolean mQuoteIfSpaces = false;

    private boolean mUnix = false;

}
