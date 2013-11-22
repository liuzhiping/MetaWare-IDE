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


import java.awt.Component;


/**
 * A class for representing a component and a constraint. It is used to map components into a container that has a
 * particular layout manager.
 * @author J. David Pickens
 * @version 6/30/99
 */

class ConstrainedComponent {

    ConstrainedComponent(Component comp, Object constraint) {
        _component = comp;
        _constraint = constraint;
    }

    Object getConstraint () {
        return _constraint;
    }

    Component getComponent () {
        return _component;
    }

    private Object _constraint;

    private Component _component;
}
