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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImage;
import com.arc.widgets.IWindow;

/**
 * A builder for an SWT Shell.
 */
class SWTFrame implements IWindow {

    private Shell mShell;

    private Composite mContents;

    private boolean mModal = false;

    private boolean mOpened;

    private WindowChangeListener mWindowListener;

    private Shell mOwnerShell;

    private IComponentFactory mFactory;
    
    private boolean mDisposed = false;

    SWTFrame(Display display, boolean modal, IComponentFactory fact) {
        this(display, null, modal, fact);
    }

    SWTFrame(Composite owner, boolean modal, IComponentFactory fact) {
        this(null, owner, modal, fact);
    }

    SWTFrame(Display display, Composite owner, boolean modal,
            IComponentFactory fact) {
        int style = 0;
        mFactory = fact;
        mModal = modal;
        if (modal)
            style = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM;
        else
            style = SWT.SHELL_TRIM;
        if (owner != null) {
            mOwnerShell = owner.getShell();
            mShell = new Shell(mOwnerShell, style);
        } else
            mShell = new Shell(display, style);
        mShell.setVisible(false); // assume invisible.
        mShell.setLayout(new GridLayout(1, false));
       
        mContents = new Composite(mShell, SWT.NULL);
        GridData d = new GridData();
        d.grabExcessHorizontalSpace = true;
        d.grabExcessVerticalSpace = true;
        d.horizontalAlignment = GridData.FILL;
        d.verticalAlignment = GridData.FILL;
        mContents.setLayoutData(d);
        mContents.setLayout(new GridLayout(1,false));

        mShell.addShellListener(mWindowListener = new WindowChangeListener(this));
    }

    Shell getShell() {
        return mShell;
    }
    
    /**
     * Open window.
     */
    @Override
    public void open() {
        if (!mOpened) {
            mOpened = true;
            if (mOwnerShell != null) {
                Point p = mOwnerShell.getLocation();
                Point size = mOwnerShell.getSize();
                int centerX = p.x + size.x / 2;
                int centerY = p.y + size.y / 2;
                size = mShell.getSize();
                p.x = centerX - size.x / 2;
                p.y = centerY - size.y / 2;
                mShell.setLocation(p);
            }
            mShell.open();
            //dump(mShell,0);
        } else
            mShell.setVisible(true);
        if (mModal) {
            Display display = mShell.getDisplay();
            while (!mShell.isDisposed() && mShell.isVisible()) {
                if (!display.readAndDispatch()) display.sleep();
            }
        }
    }
    
    @Override
    public void setDefaultButton(IButton button){
        mShell.setDefaultButton((Button)button.getComponent());
    }

    static void dump(Control c, int indent) {
        for (int i = 0; i < indent; i++)
            System.out.print(' ');
        System.out.print(c);
        if (c.getData("name") != null)
                System.out.println(" \"" + c.getData("name") + "\"");
        System.out
                .println("; size=" + c.getSize() + "; loc=" + c.getLocation());
        if (c.getLayoutData() instanceof GridData) {
            GridData g = (GridData) c.getLayoutData();
            System.out.println("; halign=" + g.horizontalAlignment);
            System.out.println("; excess=" + g.grabExcessHorizontalSpace);
        }

        if (c instanceof Composite) {
            Composite k = (Composite) c;
            System.out.print("; layout=" + k.getLayout());
            if (!k.isVisible()) System.out.print("; not visible");
            if (k instanceof CTabFolder) {
                CTabFolder tf = (CTabFolder) k;
                System.out.print("; idx=" + tf.getSelectionIndex());
                CTabItem items[] = tf.getItems();
                System.out.println();
                for (int i = 0; i < indent + 2; i++)
                    System.out.print(' ');

                for (int i = 0; i < items.length; i++) {
                    System.out.print("; #" + i + "=" + items[i].getText());
                }
            }
            System.out.println();

            Control kids[] = k.getChildren();
            for (int i = 0; i < kids.length; i++)
                dump(kids[i], indent + 2);
        } else
            System.out.println();
    }

    @Override
    public void layout() {
        mShell.layout();
        mContents.layout(); // don't know why this is necessary
    }

    /**
     * Pack every component to its preferred size.
     */
    @Override
    public void pack () {
        mShell.pack();
        // We want the frame to be wide enough to display the title.
        String title = mShell.getText();
        if (title != null) {
            GC gc = new GC(mShell);
            try {
                Point titleSize = gc.stringExtent(title);
                int titleBarWidth = titleSize.x + titleSize.y + 45; // Discovered by trial-and-error.
                Point size = mShell.getSize();
                if (size.x < titleBarWidth) {
                    size.x = titleBarWidth;
                    mShell.setSize(size);
                }
            }
            finally {
                gc.dispose();
            }
        }
        mShell.layout();
    }

    /**
     * Set size.
     */
    @Override
    public void setSize(int width, int height) {
        mShell.setSize(new Point(width, height));
    }

    /**
     * Set position on screen.
     */
    @Override
    public void setPosition(int x, int y) {
        mShell.setLocation(new Point(x, y));
    }

    /**
     * Hide dialog by making it invisible.
     */
    @Override
    public void hide() {
        mShell.setVisible(false);
    }

    /**
     * Dispose of this dialog.
     */
    @Override
    public void dispose() {
        mDisposed = true;
        mShell.dispose();
    }

    @Override
    public int getWidth() {
        return mShell.getSize().x;
    }

    @Override
    public int getHeight() {
        return mShell.getSize().y;
    }

    @Override
    public void setLocation(int x, int y) {
        mShell.setLocation(x, y);
    }

    @Override
    public Object getComponent() {
        return mShell;
    }


    /**
     * Get content in which we can add components. For swing, the result will be
     * an instance of <code>java.awt.Container</code>.
     */
    @Override
    public IContainer getContents() {
        return mFactory.wrapContainer(mContents, IComponentFactory.COLUMN_STYLE);
    }

    /**
     * Set contents.
     */
    @Override
    public void setContents(IContainer container) {
        throw new IllegalArgumentException("Shell contents can't be changed");
    }

    /**
     * Set title
     */
    @Override
    public void setTitle(String title) {
        mShell.setText(title);
    }

    @Override
    public String getTitle() {
        return mShell.getText();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IWindow#isDisposed()
     */
    @Override
    public boolean isDisposed() {
        return mDisposed;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IWindow#addWindowObserver(com.arc.widgets.IWindow.IObserver)
     */
    @Override
    public void addWindowObserver(IObserver l) {
        mWindowListener.add(l);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IWindow#removeWindowObserver(com.arc.widgets.IWindow.IObserver)
     */
    @Override
    public void removeWindowObserver(IObserver l) {
        mWindowListener.remove(l);       
    }

    @Override
    public void setImage (IImage image) {
        mShell.setImage((Image)image.getObject());
        
    }
}
