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
package com.arc.widgets.internal.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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

public class ComponentFactory implements IComponentFactory, IComponentMapper {

    private Map<Component,IComponent> mComponentMap = new WeakHashMap<Component,IComponent>();
    
    /**
     * Given a parent component see if it is a wrapper, if so extract from it.
     */
    private static Container extract(Object parent) {
        try {
            if (parent instanceof IContainer) {
                return (Container) ((IContainer) parent).getComponent();
            }
            return (Container) parent;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Parent not instance of IContainer or Container");
        }
    }
    /**
     * Make a container, and add it to a parent container.
     * All containers conceptually use what AWT refers to as a "GridBagLayout"
     * and SWT refers to as a "GridLayout".
     * @param parent the parent container
     * @param style the style of the container.
     */
    @Override
    public IContainer makeContainer(IContainer parent, int style) {
        return new SwingContainer(parent, style, 0,this);
    }
    
    @Override
    public IContainer makeContainer(IContainer parent, ILayoutManager layout){
        return new SwingContainer(parent,layout,this);
    }
    @Override
    public IContainer makeGridContainer(IContainer parent, int columns) {
        if (columns <= 0)
            throw new IllegalArgumentException("Bad column count: " + columns);
        return new SwingContainer(parent, GRID_STYLE, columns,this);
    }
    /**
     * Make a container that  only displays one of its children at any given time.
     */
    @Override
    public ICardContainer makeCardContainer(IContainer parent) {
        return new CardContainer(parent,this);
    }
    @Override
    public ILabel makeLabel(IContainer parent, String text) {
        return new Label(parent, text,this);
    }
    @Override
    public IButton makeButton(IContainer parent) {
        return new Button(parent,this);
    }
    @Override
    public IButton makeCheckBox(IContainer parent) {
        return new CheckBox(parent,this);
    }
    @Override
    public IButton makeRadioButton(IContainer parent) {
        return new RadioButton(parent,this);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makePushButton(com.arc.widgets.IContainer)
     */
    @Override
    public IButton makeToggleButton(IContainer parent) {
        return new ToggleButton(parent,this);
    }

    @Override
    public IChoice makeComboBox(IContainer parent, boolean writable) {
        return new Choice(parent, writable,this);
    }
    @Override
    public ITextField makeTextField(IContainer parent) {
        return new TextField(parent,this);
    }
    @Override
    public ITabbedPane makeTabbedPane(IContainer parent, int tabPosition) {
        return new TabbedPane(parent, tabPosition,this);
    }
    public IComponent makeVerticalGlue(IContainer parent) {
        return new GenericComponent(parent, Box.createVerticalGlue(),this);
    }
    public IComponent makeHorizontalGlue(IContainer parent) {
        return new GenericComponent(
            parent,
            Box.createHorizontalGlue(),this);
    }
    public IComponent makeVerticalStrut(IContainer parent, int height) {
        return new GenericComponent(
            parent,
            Box.createVerticalStrut(height),this);
    }
    public IComponent makeHorizontalStrut(IContainer parent, int width) {
        return new GenericComponent(
            parent,
            Box.createHorizontalStrut(width),this);
    }
    @Override
    public ISplitPane makeSplitPane(IContainer parent, boolean horizontal) {
         return new SplitPane(parent,horizontal,this);
    }

    @Override
    public IWindow makeDialog(Object owner, boolean modal) {
        Window w;
	if (owner instanceof Window) w = (Window)owner;
	else
	    w = SwingUtilities.getWindowAncestor((Component) owner);
        if (w instanceof java.awt.Frame)
            return new Dialog((java.awt.Frame) w, modal,this);
        if (w instanceof java.awt.Dialog)
            return new Dialog((java.awt.Dialog) w, modal);
        throw new IllegalArgumentException("No parent for dialog");
    }
    @Override
    public IWindow makeFrame(Object owner) {
        return new Frame(this);
    }

    @Override
    public IFileChooser makeFileChooser(Object parent, int style) {
        return new FileChooser(extract(parent), style);
    }

    /**
     * Make a color based on a name.
     */
    private Color getColor(String name) {
        if (name.length() > 0 && Character.isDigit(name.charAt(0)))
            return Color.getColor(name);
        name = name.toLowerCase();
        if (name.equals("black"))
            return Color.black;
        if (name.equals("white"))
            return Color.white;
        if (name.equals("blue"))
            return Color.blue;
        if (name.equals("red"))
            return Color.red;
        if (name.equals("green"))
            return Color.green;
        if (name.equals("yellow"))
            return Color.yellow;
        if (name.equals("orange"))
            return Color.orange;
        throw new IllegalArgumentException("Don't understand color " + name);
    }
    
    @Override
    public IColor makeColor(Object color){
        final Color c = (Color)color;
        return new IColor(){
            @Override
            public Object getObject() { return c; }
        };
    }
    
    @Override
    public IColor makeColor(String name){
        final Color c = getColor(name);
        return new IColor(){
            @Override
            public Object getObject() { return c; }
        };
    }
    /**
     * Make a color based on RGB
     */
    @Override
    public IColor makeColor(int r, int g, int b) {
        final Color c = new Color(r, g, b);
        return new IColor(){
            @Override
            public Object getObject() { return c; }
        };
    }
    /**
     * Make a font.
     * @param name name of the font.
     * @param bold if true, font is to be bold.
     * @param italic if true, font is to be italic.
     * @param size point size of font.
     */
    @Override
    public IFont makeFont(
        String name,
        boolean bold,
        boolean italic,
        int size) {
        int style = 0;
        if (italic)
            style |= Font.ITALIC;
        if (bold)
            style |= Font.BOLD;
        Font f = new Font(name, style, size);
        return new SwingFont(f);
    }
    
    @Override
    public IFont makeFont(Object font){
        if (!(font instanceof Font)){
            throw new IllegalArgumentException("Not a font object");
        }
        return new SwingFont((Font)font);
    }

    /**
     * Make an image
     */
    @Override
    public IImage makeImage(URL path) {
        final ImageIcon icon = new ImageIcon(path);
        return new SwingImage(icon);
    }

    @Override
    public void enterDispatchLoop(IWindow frame) {
    }
    /**
     * @see com.arc.widgets.IComponentFactory#showErrorDialog(Object, String)
     */
    @Override
    public void showErrorDialog(Object owner, String message) {
        JOptionPane.showMessageDialog((Component)owner, message,
            "Internal Error",JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * @see com.arc.widgets.IComponentFactory#showMessageDialog(Object, String)
     */
    @Override
    public void showMessageDialog(Object owner, String message) {
        JOptionPane.showMessageDialog((Component)owner, message,
            "Information",JOptionPane.INFORMATION_MESSAGE);
    }

	/**
	 * @see com.arc.widgets.IComponentFactory#dispose()
	 */
	@Override
    public void dispose() {
	}
    /*override*/
    @Override
    public IContainer wrapContainer(Object component, int style) {        
        final Component c = (Component)component;
        IComponent ic = findWrapperFor(c);
        if (ic != null) return (IContainer)ic;
        IContainer parent = null;
//        if (c.getParent() != null)
//            parent = wrapContainer(c.getParent(),GRID_STYLE);
        return new SwingContainer(parent,style,1,this){
            @Override
            protected Component instantiate(){
                return c;
            }
        };
    }
    /*override*/
    @Override
    public IComponent wrapComponent(Object component) {
        final Component c = (Component)component;
        IComponent ic = findWrapperFor(c);
        if (ic != null) return ic;
        IContainer parent = null;
//        if (c.getParent() != null)
//            parent = wrapContainer(c.getParent(),GRID_STYLE);
        return new SwingComponent(parent,this){
            @Override
            protected Component instantiate(){
                return c;
            }
        };
    }
    /*override*/
    @Override
    public void mapComponent(Component c, IComponent ic) {
        mComponentMap.put(c,ic);
        
    }
    /*override*/
    @Override
    public void unmapComponent(Component c) {
        mComponentMap.remove(c);
        
    }
    /*override*/
    @Override
    public IComponent findWrapperFor(Component c) {
        return mComponentMap.get(c);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeTreeList(com.arc.widgets.IContainer)
     */
    @Override
    public ITreeList makeTreeList(IContainer parent) {
        throw new IllegalArgumentException("tree list not yet implemented");
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makePopupMenu(com.arc.widgets.IComponent)
     */
    @Override
    public IMenu makePopupMenu(IComponent parent) {

        return new PopupMenu(parent,this);
        
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeMenuItem(com.arc.widgets.IMenu)
     */
    @Override
    public IMenuItem makeMenuItem(IMenu parent) {
        return new MenuItem(parent,this);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeCheckBoxMentuItem(com.arc.widgets.IMenu)
     */
    @Override
    public IButton makeCheckBoxMenuItem(IMenu parent) {
        return new CheckBoxMenuItem(parent,this);
    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeRadioButtonMenuItem(com.arc.widgets.IMenu)
     */
    @Override
    public IButton makeRadioButtonMenuItem(IMenu parent) {
        return makeCheckBoxMenuItem(parent);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeMenuBar(com.arc.widgets.IContainer)
     */
    @Override
    public IMenuBar makeMenuBar(IContainer parent) {
        return new MenuBar(parent,this);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeMenu(com.arc.widgets.IMenuItem)
     */
    @Override
    public IMenu makeMenu(IMenuBar parent) {
        return new Menu(parent,this);
        
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeMenu(com.arc.widgets.IMenu)
     */
    @Override
    public IMenu makeMenu(IMenu parent) {
        return new Menu(parent,this);
        
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeScrollPane(com.arc.widgets.IContainer)
     */
    @Override
    public IScrollPane makeScrollPane(IContainer parent, boolean clientWillScroll) {
        // TODO Auto-generated method stub
        throw new IllegalArgumentException("Not yet implemented");
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#copyToClipBoard(java.lang.String)
     */
    @Override
    public void copyToClipBoard(String selection) {
        Clipboard clipboard =
            Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection data = new StringSelection(selection);
        clipboard.setContents(data, data);       
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeAttributedString(java.lang.String)
     */
    @Override
    public IAttributedString makeAttributedString(String s) {
        return new SwingAttributedString(s);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeTextCanvas(com.arc.widgets.IContainer)
     */
    @Override
    public ITextCanvas makeTextCanvas(IContainer parent, boolean includeIconColumn, boolean selectable) {
        // TODO Auto-generated method stub
        throw new IllegalArgumentException("Not yet implemented");
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeProgress(com.arc.widgets.IContainer, boolean, int)
     */
    @Override
    public IProgress makeProgress(IContainer parent, boolean vertical, int preferredLength) {
//      TODO Auto-generated method stub
        throw new IllegalArgumentException("Not yet implemented");
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeList(com.arc.widgets.IContainer)
     */
    @Override
    public IList makeList(IContainer parent, boolean multi) {
        // TODO Auto-generated method stub
        throw new IllegalArgumentException("Not yet implemented");
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeImage(int, int)
     */
    @Override
    public IImage makeImage(int width, int height, int depth) {
//      TODO Auto-generated method stub
        throw new IllegalArgumentException("Not yet implemented");
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponentFactory#makeImageWidget(com.arc.widgets.IContainer, com.arc.widgets.IImage)
     */
    @Override
    public IImageWidget makeImageWidget(IContainer parent, IImage image) {
        // TODO Auto-generated method stub
//      TODO Auto-generated method stub
        throw new IllegalArgumentException("Not yet implemented");
    }
    
    @Override
    public IToolBar makeToolBar(IContainer parent){
        throw new IllegalArgumentException("Not yet implemented");
    }
    @Override
    public ITable makeTable (IContainer parent) {
        throw new IllegalArgumentException("Not yet implemented");
    }

}
