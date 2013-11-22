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
package org.eclipse.cdt.debug.internal.ui.views.registers;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;


class ComboContribution extends ControlContribution {
    private boolean fEditable;

    private String fToolTip;

    private SelectionListener fListeners;

    private Object[] fItems;

    private Control fControl = null;

    private CCombo fCombo = null;

    private int fColumns = 10;
    
    private int fSelection = 0;

    protected ComboContribution(
        String id,
        boolean editable,
        String tooltip,
        SelectionListener listener,
        Object items[]) {
        super(id);
        fEditable = editable;
        fToolTip = tooltip;
        fListeners = listener;
        fItems = items;
        if (items != null) {
            int max = 0;
            for (Object item : items) {
                max = Math.max(max, item.toString().length());
            }
            fColumns = max;
        }
    }

    void setItems (String items[]) {
        fItems = items;
        if (fCombo != null) {
            fCombo.setItems(items);
            fCombo.select(0);
        }
    }

    Object[] getItems () {
        return fItems;
    }
    
    int getSelectionIndex() { return fCombo != null?fCombo.getSelectionIndex():fSelection; }
    
    Object getSelection() {
        if (fItems != null) {
            if (fCombo != null) return fItems[fCombo.getSelectionIndex()];
            return fItems[fSelection];
        }
        return null;
    }
    
    void reset(){
        fCombo = null;
        fControl = null;
    }
    
    void setSelection(Object o){
        if (fItems != null) {
            for (int i = 0; i < fItems.length; i++){
                if (fItems[i] == o) {
                    fSelection = i;
                    if (fCombo != null) fCombo.select(i);
                    break;
                }
            }
        }
    }
    
    void setSelectionIndex(int index) {
        fSelection = index;
        if (fCombo != null) fCombo.select(index);
    }

    @Override
    protected Control createControl (Composite parent) {
        // NOTE: we must use "CCombo" instead of "Combo" because
        // the latter doesn't render correctly under Windows
        // when the toolbar wraps.
        // NOTE2: we must wrap the control in a composite so that we
        // can control the layout. Otherwise, Combos end up very narrow if
        // they are empty.
        Composite comboContainer = new Composite(parent, SWT.NONE);
        comboContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        comboContainer.setLayout(new MyComboLayout());
        final CCombo combo = new CCombo(comboContainer, SWT.DROP_DOWN |
            SWT.BORDER |
            (fEditable ? 0 : SWT.READ_ONLY));
        combo.setToolTipText(fToolTip);
        combo.setData("name",this.getId()); // for GUI tester
        if (fListeners != null)
            combo.addSelectionListener(fListeners);
        if (fItems != null) {
            for (Object item : fItems) {
                combo.add(item.toString());
            }
            combo.select(fSelection);
        }
        combo.addSelectionListener(new SelectionListener() {

            private boolean pending = false;

            public void widgetDefaultSelected (SelectionEvent e) {
                String items[] = combo.getItems();
                String text = combo.getText();
                if (!pending) {
                    try {
                        pending = true;
                        boolean selected = false;
                        for (int i = 0; i < items.length; i++) {
                            if (items[i].equals(text)) {
                                combo.select(i);
                                selected = true;
                                break;
                            }
                        }
                        if (!selected) {
                            combo.add(text);
                            combo.select(combo.getItemCount() - 1);
                            fItems = combo.getItems();
                            fSelection = combo.getSelectionIndex();
                        }
                    }
                    finally {
                        pending = false;
                    }
                }
            }

            public void widgetSelected (SelectionEvent e) {

            }
        });
        fCombo = combo;
        fControl = comboContainer;
        return comboContainer;
    }

    @Override
    public void dispose () {
        if (fControl != null) {
            fControl.dispose();
        }
        fCombo = null;
        fControl = null;
    }

    @Override
    protected int computeWidth (Control control) {
        return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
    }

    /**
     * A layout that wraps a combobox that is constrained to a particular width.
     * @author davidp
     * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
     * @version $Revision$
     * @lastModified $Date$
     * @lastModifiedBy $Author$
     * @reviewed 0 $Revision:1$
     */
    class MyComboLayout extends Layout {

        MyComboLayout() {
        }

        @Override
        protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
            for (Control kid : composite.getChildren()) {
                Point size = kid.computeSize(wHint, hHint, flushCache);
                if (fColumns > 0) {
                    GC gc = new GC(kid);
                    FontMetrics fm = gc.getFontMetrics();
                    size.y = Math.max(size.y, fm.getHeight() + 2);
                    // We assume the downarrow on the right is a square: its width is the same
                    // as its height. Also assume a left border of 2 pixels.
                    // Often the "average" char width seems too conservatibe. Add 1.
                    size.x = (fColumns + 1) * fm.getAverageCharWidth() + size.y + 2;
                    gc.dispose();
                }
                return size;
            }
            return new Point(0, 0); // shouldn't get here
        }

        @Override
        protected void layout (Composite composite, boolean flushCache) {
            Point size = composite.getSize();
            for (Control kid : composite.getChildren()) {
                kid.setBounds(0, 0, size.x, size.y);
            }
        }
    }

}
