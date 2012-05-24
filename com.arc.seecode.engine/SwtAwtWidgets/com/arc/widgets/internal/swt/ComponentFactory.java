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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IAttributedString;
import com.arc.widgets.IButton;
import com.arc.widgets.ICardContainer;
import com.arc.widgets.IChoice;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFileChooser;
import com.arc.widgets.IFont;
import com.arc.widgets.IImage;
import com.arc.widgets.IImageWidget;
import com.arc.widgets.ILabel;
import com.arc.widgets.ILayoutManager;
import com.arc.widgets.IList;
import com.arc.widgets.IMenu;
import com.arc.widgets.IMenuBar;
import com.arc.widgets.IMenuItem;
import com.arc.widgets.IProgress;
import com.arc.widgets.IScrollPane;
import com.arc.widgets.ISplitPane;
import com.arc.widgets.ITabbedPane;
import com.arc.widgets.ITable;
import com.arc.widgets.ITextCanvas;
import com.arc.widgets.ITextField;
import com.arc.widgets.IToolBar;
import com.arc.widgets.ITreeList;
import com.arc.widgets.IWindow;

/**
 * A component factory based on SWT widgets.
 * 
 * @author pickens
 * 
 *  
 */
public class ComponentFactory implements IComponentFactory, IComponentMapper {

    private Display mDisplay;

    private Map<URL,IImage> mImageMap = null;

    //    /**
    //     * Given a parent component see if it is a wrapper, if so extract from it.
    //     */
    //    private static Composite extract(Object parent) {
    //        if (parent instanceof IContainer) { return (Composite) ((IContainer)
    // parent)
    //                .getComponent(); }
    //        return (Composite) parent;
    //    }

    public ComponentFactory() {
        this(new Display());
    }

    public ComponentFactory(Display d) {
        mDisplay = d;
    }

    @Override
    public IContainer makeContainer(IContainer parent, ILayoutManager layout) {
        return new Container(parent, layout, this);
    }
    @Override
    public IContainer makeContainer(IContainer parent, int style) {
        return new Container(parent, style, 0, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeScrollPane(com.arc.widgets.IContainer)
     */
    @Override
    public IScrollPane makeScrollPane(IContainer parent, boolean clientWillScroll) {
        return new SWTScrollPane(parent, clientWillScroll,this);
    }

    @Override
    public IContainer makeGridContainer(IContainer parent, int columns) {
        return new Container(parent, GRID_STYLE, columns, this);
    }

    /**
     * Make a container that only displays one of its children at any given
     * time.
     */
    @Override
    public ICardContainer makeCardContainer(IContainer parent) {
        return new CardContainer(parent, this);
    }

    @Override
    public ILabel makeLabel(IContainer parent, String text) {
        return new SWTLabel(parent, text, this);
    }

    @Override
    public IButton makeButton(IContainer parent) {
        return new SWTButton(parent, SWT.PUSH, this);
    }

    @Override
    public IButton makeCheckBox(IContainer parent) {
        return new SWTButton(parent, SWT.CHECK, this);
    }

    @Override
    public IButton makeRadioButton(IContainer parent) {
        return new SWTButton(parent, SWT.RADIO, this);
    }

    @Override
    public IButton makeToggleButton(IContainer parent) {
        return new SWTButton(parent, SWT.TOGGLE, this);
    }

    @Override
    public IChoice makeComboBox(IContainer parent, boolean writable) {
        return new SWTChoice(parent, writable, this);
    }

    @Override
    public ITextField makeTextField(IContainer parent) {
        return new SWTTextField(parent, this);
    }

    @Override
    public ITabbedPane makeTabbedPane(IContainer parent, int tabPosition) {
        int style = SWT.TOP;
        switch (tabPosition) {
            case TABS_ON_TOP:
                style = SWT.TOP;
                break;
            case TABS_ON_BOTTOM:
                style = SWT.BOTTOM;
                break;
            case TABS_ON_LEFT:
                style = SWT.LEFT;
                break;
            case TABS_ON_RIGHT:
                style = SWT.RIGHT;
                break;
        }
        return new SWTCTabbedPane(parent, style, this);
    }

    @Override
    public ISplitPane makeSplitPane(IContainer parent, boolean horizontal) {
        return new SWTSplitPane(parent, horizontal, this);
    }

    @Override
    public IWindow makeDialog(Object owner, boolean modal) {
        if (owner instanceof IComponent){
            owner = ((IComponent)owner).getComponent();
        }
        SWTFrame frame = owner != null?new SWTFrame(((Control) owner).getShell(), modal, this)
                            : new SWTFrame(mDisplay,modal,this);
        if (mFrames == null)
            mFrames = new ArrayList<SWTFrame>();
        mFrames.add(frame);
        return frame;
    }

    @Override
    public IWindow makeFrame(Object owner) {
        SWTFrame frame;
        if (owner == null)
            frame = new SWTFrame(mDisplay, false, this);
        else
            frame = new SWTFrame(((Control) owner).getShell(), false, this);
        if (mFrames == null)
            mFrames = new ArrayList<SWTFrame>();
        mFrames.add(frame);
        return frame;
    }

    @Override
    public IFileChooser makeFileChooser(Object parent, int style) {
        Control control;
        if (parent instanceof IComponent){
            control = (Control)((IComponent)parent).getComponent();
        }
        else control = (Control)parent;
        return new SWTFileChooser(control, style);
    }

    /**
     * Make a color based on a name.
     */
    @Override
    public IColor makeColor(String name) {
        name = name.toLowerCase();
        if (name.equals("black"))
            return makeColor(0, 0, 0);
        if (name.equals("white"))
            return makeColor(255, 255, 255);
        if (name.equals("blue"))
            return makeColor(0, 0, 255);
        if (name.equals("red"))
            return makeColor(255, 0, 0);
        if (name.equals("green"))
            return makeColor(0, 255, 0);
        if (name.equals("yellow"))
            return makeColor(0, 255, 255);
        if (name.equals("orange"))
            return makeColor(255, 255, 0);
        if (name.equals("gray"))
            return makeColor(100,100,100);
        if (name.equals("magenta"))
        	return makeColor(255,0,255);
        throw new IllegalArgumentException("Don't understand color " + name);
    }

    /**
     * Make a color based on RGB
     */
    @Override
    public IColor makeColor(int r, int g, int b) {
        if (mColors == null)
            mColors = new ArrayList<IColor>();
        for (IColor ic: mColors){
            Color c = (Color)ic.getObject();
            if (c.getRed() == r && c.getGreen() == g &&
                    c.getBlue() == b)
                return ic;
        }
        final Color color = new Color(mDisplay, r, g, b);
        IColor icolor = new IColor() {

            @Override
            public Object getObject() {
                return color;
            }
        };
        mColors.add(icolor); // to dispose of later
        return icolor;
    }
    
    @Override
    public IColor makeColor(Object color){
        final Color c = (Color)color;
        return new IColor() {
           @Override
        public Object getObject() {
               return c;
           }
           @Override
           public int hashCode(){
        	   return c.hashCode();
           }
           @Override 
           public boolean equals(Object o){
               return o instanceof IColor && ((IColor)o).getObject() == c;
           }
       };
    }

    /**
     * Make a font.
     * 
     * @param name
     *            name of the font.
     * @param bold
     *            if true, font is to be bold.
     * @param italic
     *            if true, font is to be italic.
     * @param size
     *            point size of font.
     */
    @Override
    public IFont makeFont(String name, boolean bold, boolean italic, int size) {
        int style = SWT.NORMAL;
        if (italic)
            style = SWT.ITALIC;
        if (bold)
            style |= SWT.BOLD;
        if (mFonts != null){
            // See if we already have it
            for (IFont f: mFonts){
                FontData fd = ((Font)f.getObject()).getFontData()[0];
                if (name.equals(fd.getName()) &&
                    style == fd.getStyle() &&
                    size == fd.getHeight()){
                    return f;
                }
            }
        }
        Font f = new Font(mDisplay, name, size, style);
        if (mFonts == null)
            mFonts = new ArrayList<IFont>();
        IFont newFont =  new SWTFont(f);
        mFonts.add(newFont);
        return newFont;
    }
    
    @Override
    public IFont makeFont (Object font) {
        if (!(font instanceof Font)) {
            throw new IllegalArgumentException("Bad font type");
        }
        return new SWTFont((Font) font);
    }
    
    @Override
    public IImage makeImage(int width, int height, int depth){
        Image i = new Image(mDisplay,width,height);
        i.getImageData().depth = depth;
        return new SWTImage(i);
    }

    /**
     * Make an image
     */
    @Override
    public IImage makeImage(URL path) {
        Image image = null;
        if (mImageMap == null)
            mImageMap = new WeakHashMap<URL,IImage>();
        IImage i = mImageMap.get(path);
        if (i != null && !((Image)i.getObject()).isDisposed())
            return i;

        try {
            InputStream in = path.openStream();
            image = new Image(mDisplay, in);
        } catch (IOException x) {
            throw new IllegalArgumentException("Nonexistent URL for image: "
                    + path);
        } catch (SWTException x){
            // Return "unrecognized format" as a null
            if (x.getMessage() != null && x.getMessage().indexOf("unrecognized") >= 0){
                return null;
            }
            throw x;
        }
        i = new SWTImage(image);
        mImageMap.put(path,i);
        return i;
    }

    @Override
    public void enterDispatchLoop(IWindow frame) {
        Shell shell = ((SWTFrame) frame).getShell();
        while (!shell.isDisposed())
            while (!mDisplay.readAndDispatch())
                mDisplay.sleep();
    }

    /**
     * @see com.arc.widgets.IComponentFactory#showErrorDialog(Object, String)
     */
    @Override
    public void showErrorDialog(Object owner, String message) {
        Shell shell = null;
        if (owner instanceof Control)
            shell = ((Control) owner).getShell();
        MessageBox m = new MessageBox(shell, SWT.OK);
        m.setMessage(message);
        m.open();
    }
    
    /**
     * @see com.arc.widgets.IComponentFactory#showMessageDialog(Object, String)
     */
    @Override
    public void showMessageDialog(Object owner, String message) {
        Shell shell = null;
        if (owner instanceof Control)
            shell = ((Control) owner).getShell();
        MessageBox m = new MessageBox(shell, SWT.OK);
        m.setMessage(message);
        m.open();
    }

    /**
     * @see com.arc.widgets.IComponentFactory#dispose()
     */
    @Override
    public void dispose() {
        if (mFrames != null) {
            for (int i = 0; i < mFrames.size(); i++)
               mFrames.get(i).dispose();
            mFrames.clear();
        }
        if (mImageMap != null) {
            Iterator<Map.Entry<URL,IImage>> each = mImageMap.entrySet().iterator();
            while (each.hasNext()){
                Map.Entry<URL,IImage> e = each.next();
                IImage image = e.getValue();
                each.remove();
                Image i = (Image)image.getObject();
                if (!i.isDisposed())
                    i.dispose();
            }
        }
        if (mFonts != null) {
            for (int i = 0; i < mFonts.size(); i++)
                 ((Font)mFonts.get(i).getObject()).dispose();
            mFonts.clear();
        }
        if (mColors != null) {
            for (int i = 0; i < mColors.size(); i++) {
                ((Color)mColors.get(i).getObject()).dispose();
            }
            mColors.clear();
        }
    }

    private List<SWTFrame> mFrames; //dialogs to be disposed of

    private List <IFont>mFonts;

    private List <IColor>mColors;

    /* override */
    @Override
    public IContainer wrapContainer(final Object component, int style) {
        final Composite composite = (Composite) component;
        IComponent ic = findWrapperFor(composite);
        if (ic != null)
            return (IContainer) ic;
        Container c = new Container(null, style, 1, this) {
            @Override
            protected Widget instantiate() {
                setStyleFor(composite);
                return composite;
            }
        };
        if (composite.getLayoutData() instanceof GridData) {
            c.setGridData((GridData) composite.getLayoutData());
        }
        ic = c;
        mapComponent(composite, ic);
        return (IContainer) ic;
    }

    /* override */
    @Override
    public IComponent wrapComponent(final Object component) {
        IComponent ic = findWrapperFor((Composite) component);
        if (ic != null)
            return ic;
        ic = new Component(null, this) {
            @Override
            protected Widget instantiate() {
                return ((Composite) component);
            }
        };
        mapComponent((Composite) component, ic);
        return ic;
    }

    /* override */
    @Override
    public void mapComponent(final Widget c, IComponent ic) {
        mComponentMap.put(c, ic);
        c.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                mComponentMap.remove(c);

            }
        });

    }

    /* override */
    @Override
    public IComponent findWrapperFor(Widget c) {
        return mComponentMap.get(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeTreeList(com.arc.widgets.IContainer)
     */
    @Override
    public ITreeList makeTreeList(IContainer parent) {
        return new SWTTreeList(parent, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makePopupMenu(com.arc.widgets.IComponent)
     */
    @Override
    public IMenu makePopupMenu(IComponent parent) {
        return new SWTPopupMenu(parent, this);
    }

    private Map<Widget,IComponent> mComponentMap = new WeakHashMap<Widget,IComponent>();

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeMenuItem(com.arc.widgets.IMenu)
     */
    @Override
    public IMenuItem makeMenuItem(IMenu parent) {
        return new SWTMenuItem(parent, this, SWT.PUSH);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeMenuBar(com.arc.widgets.IContainer)
     */
    @Override
    public IMenuBar makeMenuBar(IContainer parent) {
        return new SWTMenuBar(parent, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeMenu(com.arc.widgets.IMenuBar)
     */
    @Override
    public IMenu makeMenu(IMenuBar parent) {
        return new SWTMenu(parent, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeMenu(com.arc.widgets.IMenu)
     */
    @Override
    public IMenu makeMenu(IMenu parent) {
        return new SWTMenu(parent, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeCheckBoxMenuItem(com.arc.widgets.IMenu)
     */
    @Override
    public IButton makeCheckBoxMenuItem(IMenu parent) {
        return new SWTMenuItem(parent, this, SWT.CHECK);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeRadioButtonMenuItem(com.arc.widgets.IMenu)
     */
    @Override
    public IButton makeRadioButtonMenuItem(IMenu parent) {
        return new SWTMenuItem(parent, this, SWT.RADIO);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#copyToClipBoard(java.lang.String)
     */
    @Override
    public void copyToClipBoard(String s) {
        Clipboard clipboard = new Clipboard(mDisplay);
        TextTransfer textTransfer = TextTransfer.getInstance();
        clipboard.setContents(new Object[] { s },
                new Transfer[] { textTransfer });
        clipboard.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeAttributedString(java.lang.String)
     */
    @Override
    public IAttributedString makeAttributedString(String s) {
        return new SWTAttributedString(mDisplay,s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponentFactory#makeTextCanvas(com.arc.widgets.IContainer)
     */
    @Override
    public ITextCanvas makeTextCanvas(IContainer parent, boolean includeIconColumn, boolean selectable) {
        return new SWTTextCanvas(parent, includeIconColumn, selectable, this);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeProgrss(com.arc.widgets.IContainer, boolean, int)
     */
    @Override
    public IProgress makeProgress(IContainer parent, boolean vertical, int preferredLength) {
        return new SWTProgress(parent,this,vertical);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeList(com.arc.widgets.IContainer)
     */
    @Override
    public IList makeList(IContainer parent, boolean multi) {
        return new SWTList(parent,multi,this);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeImageWidget(com.arc.widgets.IContainer, com.arc.widgets.IImage)
     */
    @Override
    public IImageWidget makeImageWidget(IContainer parent, IImage image) {
        return new SWTImageWidget(parent,(SWTImage)image,this);
    }
    
    @Override
    public IToolBar makeToolBar(IContainer parent){
        return new SWTToolBar(parent,this);
    }

    @Override
    public ITable makeTable (IContainer parent) {
        return new SWTTable(parent,this);
    }

}
