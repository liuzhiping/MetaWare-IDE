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
package com.arc.widgets;

/**
 * A menu containing menu items.
 * @author David Pickens
 */
public interface IMenu extends IMenuItem {
    interface IObserver{
        void menuShown(IMenu menu);
        void menuHidden(IMenu menu);
    }
      /**
       * Show the menu
       * @param x absolute screen x coordinate
       * @param y absolute screen y coordinate
       */
      void show(int x, int y);
      
      /**
       * Append separator to menu.
       *
       */
      void appendSeparator();
      
      /**
       * Add observer to this menu
       */
      void addMenuObserver(IObserver o);
      /**
       * Remove an observer.
       */
      void removeMenuObserver(IObserver o);
}
