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
package com.metaware.guihili.builder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import org.xml.sax.SAXException;

import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IFileChooser;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

/**
 * Generate a FileChooser dialog...
 */
public class FileChooserBuilder extends ComponentBuilder {

    public FileChooserBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) {
    }

    public void setTitle(String text) {
        _title = text;
    }

    public void setAction(String actionName) {
        _actionName = actionName;
    }

    public void setProperty(String name) {
        _property = name;
    }

    public void setDirectory(String name) {
        _directory = name;
    }

    public void setMultiselect(boolean value) {
        if (value) mStyle |= IComponentFactory.FILE_MULTI;
    }

    public void setList(String name) {
        _listName = name;
    }

    @Override
    public void setTooltip(String tooltip) {
        _tooltip = tooltip;
    }

    /**
     * Add a filter specification.
     */
    @Override
    public void addChild(Object child, Element element) {
        _filterList.add(child);
    }

    public void setParent(String parentName) {
//        _parentName = parentName;
    }
    public void setButtonLabel(String buttonLabel) {
        _buttonLabel = buttonLabel;
    }

    public void setMode(String mode) throws SAXException {
        mode = mode.toLowerCase();
        if (mode.indexOf("dir") >= 0) {
            if (mode.indexOf("file") < 0) 
                mStyle |= IComponentFactory.FILE_DIRS;
        }
        else if (mode.indexOf("file")< 0)
            throw new SAXException("Invalid fileChooser mode: " + mode);
    }

    private void construct() {
        _fileChooser =
            _gui.getComponentFactory().makeFileChooser(_gui.getParent(), mStyle);
        if (_title != null)
            _gui.processTextOrProperty(_title, new ITextWrapper() {
            @Override
            public void setText(String text) {
                _fileChooser.setTitle(text);
            }
            @Override
            public String getText() {
                return _fileChooser.getTitle();
            }
        });
        if (_buttonLabel != null)
            _gui.processTextOrProperty(_buttonLabel, new ITextWrapper() {
            @Override
            public void setText(String text) {
                _buttonLabel = text;
            }
            @Override
            public String getText() {
                return _buttonLabel;
            }
        });
        if (_tooltip != null)
            _gui.processTextOrProperty(_tooltip, new ITextWrapper() {
            @Override
            public void setText(String text) {
                _fileChooser.setToolTip(text);
            }
            @Override
            public String getText() {
                return _fileChooser.getToolTip();
            }
        });
        if (_directory != null){
            _gui.processTextOrProperty(_directory, new ITextWrapper() {
                @Override
                public void setText(String text) {
                    _fileChooser.setDirectory(new File(text));
                }
                @Override
                public String getText() {
                    File f = _fileChooser.getDirectory();
                    if (f == null) return null;
                    return f.toString();
                }
            });
        }
        /*
        int fsize = _filterList.size();
        for (int i = 0; i < fsize; i++)
            _fileChooser.addChoosableFileFilter(
                (FileFilter) _filterList.get(i));
        if (fsize > 0)
            _fileChooser.setFileFilter((FileFilter) _filterList.get(0));
        */
    }

    @Override
    public Object returnObject() {
        _gui.addAction(_actionName, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (_fileChooser == null)
                    construct();
                if (_fileChooser.open() != null) {
                    if (_property != null) {
                        try {
                            if ((mStyle & IComponentFactory.FILE_MULTI) != 0)
                                _gui.setProperty(
                                    _property,
                                    _fileChooser.getFiles());
                            else
                                _gui.setProperty(
                                    _property,
                                    _fileChooser.getFile());
                        }
                        catch (PropertyVetoException x) {
                        }
                    }
                    if (_listName != null) {
                        Object l = _gui.getProperty(_listName);
                        if (l instanceof DefaultListModel) {
                            DefaultListModel list = (DefaultListModel) l;
                            if ((mStyle & IComponentFactory.FILE_MULTI)!=0) {
                                File[] files = _fileChooser.getFiles();
                                for (int i = 0; i < files.length; i++)
                                    list.addElement(files[i]);
                            }
                            else
                                list.addElement(_fileChooser.getFile());
                        }
                        else
                            System.err.println(
                                "File chooser can't append to non-mutable list! (name="
                                    + _listName
                                    + ", class="
                                    + (l == null
                                        ? "null"
                                        : l.getClass().getName())
                                    + ')');
                    }
                }
            }
        });
        return null; // What do we return here?
    }

    private IFileChooser _fileChooser;
    private List<Object> _filterList = new ArrayList<Object>(1);
    private String _directory = ".";
    private String _actionName;
    private String _listName;
    private String _tooltip;
    private String _title;
    private String _property;
//    private String _parentName;
    private String _buttonLabel = "Open";
    private int mStyle;
}
