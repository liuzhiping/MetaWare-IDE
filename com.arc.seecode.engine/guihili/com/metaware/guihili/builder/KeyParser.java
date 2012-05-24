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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

/**
 * Parse a key string into its KeyStroke equivalent.
 * <pre>
 *    M+x   
 *    S+x
 *    A+x
 *    C+x
 *    MS+x
 *    MC+x
 *    MC+x
 *    ...
 * </pre>
 */
public class KeyParser {
    public static KeyStroke parse(String s)
		throws IllegalArgumentException
	{
	if (s == null || s.length() == 0) throw new IllegalArgumentException(s);
	String key = s;
	int modifiers = 0;
	int ch = 0;
	int i = key.indexOf('+');
	if (i >= 0){
	    for (int j = 0; j < i; j++){
		switch(key.charAt(j)){
		    case 'a':
		    case 'A': modifiers |= InputEvent.ALT_MASK; break;
		    case 'm':
		    case 'M': modifiers |= InputEvent.META_MASK; break;
		    case 's':
		    case 'S': modifiers |= InputEvent.SHIFT_MASK; break;
		    case 'c':
		    case 'C': modifiers |= InputEvent.CTRL_MASK; break;
		    default:
			throw new IllegalArgumentException(s);
		    }
		}
	    key = key.substring(i+1);
	    }
	if (key.length() == 1)
	    ch = Character.toUpperCase(key.charAt(0));
	else
	if (key.length() == 0)
	    throw new IllegalArgumentException(s);
	else
	    try{
		ch = KeyEvent.class.getField("VK_" + key.toUpperCase()).getInt(null);
		}
	    catch(Exception x){
		throw new IllegalArgumentException(s);
		}
	return KeyStroke.getKeyStroke(ch,modifiers);
	}
    }
