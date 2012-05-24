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

import org.xml.sax.SAXException;

import com.arc.widgets.IContainer;
import com.arc.widgets.IWindow;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

/**
 * class for constructing a JDialog
 * @author J. David Pickens
 */
public class DialogBuilder extends ContainerBuilder {
    public DialogBuilder(Gui gui) {
        super(gui);
    }

    /**
     * the title attribute
     */
    @Override
    public void setTitle(String title) {
        _title = title;
    }

    public void setModal(boolean modal) {
        _isModal = modal;
    }

    public void setOwner(String owner) {
//        _ownerName = owner;
        setupOwner(owner);
    }

    private void setupOwner(String owner) {
//        _owner = _gui.getComponent(owner);
    }

 
    /**
     * Define the action that will materialize the dialog
     */
    public void setCreateAction(String actionName) {
        _createAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                _dialog.open();
            }
        };
        _gui.addAction(actionName, _createAction);
    }
    /**
     * Define the action that will dispose of the dialog
     */
    public void setDisposeAction(String actionName) {
        _disposeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                _dialog.hide();
            }
        };
        if (actionName != null)
            _disposeAction = _gui.addAction(actionName, _disposeAction);
    }

    @Override
    protected Object returnObject() throws SAXException{
        if (_disposeAction == null) setDisposeAction(null);

        if (_title != null)
            _gui.processTextOrProperty(_title, new ITextWrapper() {
            @Override
            public String getText() {
                return _dialog.getTitle();
            }
            @Override
            public void setText(String text) {
                _dialog.setTitle(text);
            }
        });
        if (_disposeAction != null) {
            _dialog.addWindowObserver(new IWindow.IObserver() {
                @Override
                public void windowClosing(IWindow w) {
                    _disposeAction.actionPerformed(null);
                }

                @Override
                public void windowClosed(IWindow w) {
                }

                @Override
                public void windowActivated(IWindow w) {
                }

                @Override
                public void windowDeactivated(IWindow w) { 
                }

                @Override
                public void windowIconified(IWindow w) {
                }

                @Override
                public void windowDeiconified(IWindow w) {  
                }
            });
        }
        handleEnablement(_dialog.getContents());
        _dialog.pack();
        return _dialog;
    }

    private IWindow _dialog;
    private String _title;
    private boolean _isModal;
    private ActionListener _createAction;
    private ActionListener _disposeAction;
//    private String _ownerName = "main";
//    private IComponent _owner;

    /**
     * @see com.metaware.guihili.builder.ContainerBuilder#makeContainer()
     */
    @Override
    protected IContainer makeContainer() {
        _dialog = _gui.getComponentFactory().makeDialog(_gui.getFrame(),_isModal);
        return _dialog.getContents();
    }
}
