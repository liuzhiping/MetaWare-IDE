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
package com.arc.mw.util;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * A table for getting Color objects from string representation
 */

public class ColorTable {
    /**
     * Return color corresponding to name.
     */
    public static Color decode(String name) {
        if (name == null || name.length() == 0)
            return Color.black;
        if (name.charAt(0) == '#') {
            try {
                return Color.decode(name);
            } catch (NumberFormatException x) {
                return Color.black;
            }
        } else {
            Color c = table.get(name.toLowerCase());
            if (c == null)
                c = Color.black;
            return c;
        }
    }

    private static Map<String, Color> table;
    static {
        table = new HashMap<String, Color>();
        table.put("red", Color.red);
        table.put("black", Color.black);
        table.put("green", Color.green);
        table.put("blue", Color.blue);
        table.put("yellow", Color.yellow);
        table.put("white", Color.white);
        table.put("orange", Color.orange);
        table.put("gray", Color.gray);
        table.put("cyan", Color.cyan);
        table.put("lightgray", Color.lightGray);
        table.put("darkgray", Color.darkGray);
        table.put("magenta", Color.magenta);
        table.put("pink", Color.pink);
    }
}
