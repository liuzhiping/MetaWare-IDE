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
package com.arc.widgets.internal.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IScrollBar;
import com.arc.widgets.IScrollPane;

/**
 * @author David Pickens
 */
class SWTScrollPane extends Container implements IScrollPane {


    private IScrollBar mVerticalScrollBar = null;
    private IScrollBar mHorizontalScrollBar = null;
    //private boolean mVerticalIsInfinite;
    //private Composite mScroller;
    private boolean clientWillManage;

    /**
     * @param parent
     * @param mapper
     */
    SWTScrollPane(IContainer parent,
            boolean clientWillScroll,
            IComponentMapper mapper) {
        super(parent, IComponentFactory.NO_STYLE, 0, mapper);
        this.clientWillManage = clientWillScroll;
        setFlags(SWT.V_SCROLL|SWT.H_SCROLL);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollPane#getVerticalScrollBar()
     */
    @Override
    public IScrollBar getVerticalScrollBar() {
        if (mVerticalScrollBar == null){
            Composite c = (Composite)getComponent();
            mVerticalScrollBar = new SWTScrollBar(c.getVerticalBar());
        }
        return mVerticalScrollBar;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IScrollPane#getHorizontalScrollBar()
     */
    @Override
    public IScrollBar getHorizontalScrollBar() {
        if (mHorizontalScrollBar == null){
            Composite c = (Composite)getComponent();
            mHorizontalScrollBar = new SWTScrollBar(c.getHorizontalBar());
        }
        return mHorizontalScrollBar;
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        
        if (this.clientWillManage) {
            Composite c = (Composite) super.instantiate();
            c.setLayout(new MySliderLayout());
            return c;
        }
        else {
            ScrolledComposite c = new ScrolledComposite(this.getParentComposite(),SWT.V_SCROLL|SWT.H_SCROLL){
                @Override
                public Point computeSize(int hhint,int vhint, boolean changed){
                    if (changed) {
                        Control content = getContent();
                        //We assume the trim has negligible demands on the content size.
                        //But we want to take into account wrapping labels when the width is constrained.
                        Point size = content.computeSize(hhint,vhint,true);
                        setMinSize(size);                    
                    }
                    return super.computeSize(hhint,vhint,changed);
                }
            };
            c.setExpandHorizontal(true);
            c.setExpandVertical(true);
            return c;        
        }

    }
    
    static class MySliderLayout extends Layout {

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
         */
        @Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            Control kids[] = composite.getChildren();
            //Should have just one kid
            if (kids.length > 0){
                return kids[0].computeSize(wHint,hHint,flushCache);
            }
            return new Point(0,0);            
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
         */
        @Override
		protected void layout(Composite composite, boolean flushCache) {
            Control kids[] = composite.getChildren();
            Rectangle rect = composite.getClientArea();
            if (kids.length > 0){
            	//Can't pass rect.width and rect.height as "hints" because
            	// "Composite" will uncontionally return them without
            	// consulting the layout!!
                //Point kidSize = kids[0].computeSize(SWT.DEFAULT,SWT.DEFAULT,flushCache);
                kids[0].setBounds(rect);
            }
        }       
    }
}
