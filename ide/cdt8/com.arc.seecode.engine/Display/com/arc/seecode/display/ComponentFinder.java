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
package com.arc.seecode.display;

import org.eclipse.swt.widgets.ToolBar;

import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.IToolBar;
import com.arc.widgets.IToolItem;
import com.arc.widgets.IWidget;

/**
 * Functions for searching for components.
 * 
 * @author David Pickens
 */
public class ComponentFinder {
    // Map<Container,List<Component>>
    //private static WeakHashMap sComponentsFor = new WeakHashMap();
    /**
     * Given the name of a component, id, search the container, C, for a
     * component with that name. When found, return it. For performance, we
     * cache the component list into a map for future access.
     * <P>
     * <b>Correction: </b> we no longer cache the components. A {@link ToolBar}
     * widget has contents that dynamically change as a display is narrowed or
     * widened. Thus, caching things messes things up.
     * 
     * @param id
     *            the name of the component being sought.
     * @param container
     *            the container being searched.
     * @return the component within C with name "id", or else null.
     */
    public static IWidget findComponent(String id, IContainer container) {

        IComponent[] kids = container.getChildren();
        for (int i = 0; i < kids.length; i++) {
            if (id.equals(kids[i].getName())){
                return kids[i];
            }
            if (kids[i] instanceof IContainer) {
                IWidget c = findComponent(id,
                        (IContainer) kids[i]);
                if (c != null)
                    return c;
            }
            else if (kids[i] instanceof IToolBar){
                IToolItem[] items = ((IToolBar)kids[i]).getItems();
                for (IToolItem item: items){
                    if (id.equals(item.getName()))
                        return item;
                }
            }
        }
        return null;
    }


    /*
     * static void dots(int depth) { for (int i = 0; i < depth; i++)
     * System.out.print(". "); } static void pl(Component C, int depth, int cnt) {
     * dots(depth); System.out.print(cnt+" "); if (C instanceof Container) {
     * SOP("container "+C); Container CC = (Container)C; dots(depth); SOP("
     * manager"+CC.getLayout()); Component [] A = CC.getComponents(); for (int i =
     * 0; i < A.length; i++) { pl(A[i],depth+1,i); } } else SOP(C.toString()); }
     * static void print_layouts(Container f) { pl(f,0,0); }
     */
    static void dots(int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print(". ");
        }
    }

    private static void SOP(String s) {
        System.out.print(s);
    }

    static void treePrint(IComponent C, int depth, int cnt) {
        dots(depth);
        System.out.print(cnt + " ");

        if (C.getName() != null) {
            System.out.print("name=" + C.getName() + " ");
        }
        if (C instanceof IContainer) {
            SOP("container " + C);
            IContainer CC = (IContainer) C;
            dots(depth);
            IComponent[] A = CC.getChildren();
            for (int i = 0; i < A.length; i++) {
                treePrint(A[i], depth + 1, i);
            }
        } else {
            SOP(C.toString());
        }
    }

    public static void printComponents(IContainer f) {
        treePrint(f, 0, 0);
    }

}

