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

import java.net.URL;

/**
 * A factory for create GUI components.
 * There are two implementations: one based on Swing, and the other based
 * on SWT. Since SWT requires the parent container to instantiate
 * a component, we pass the parent to each factory method.
 * <P>
 * For Swing, the parent object will likely be an instance of <code>JPanel</code>;
 * for SWT, an instance of <code>org.eclipse.swt.widgets.Composite</code>.
 * <P>
 * Note that the parent object is <i>not</i> the <code>{@link IContainer}</code> object,
 * but rather the object returned by calling 
 * <code>{@link IComponent#getComponent() getComponent()}</code> on the 
 * <code>{@link IContainer}</code> object.
 */
public interface IComponentFactory {
    /**
     * A container style that means don't set a any layout.
     * Typically used when 
     * {@linkplain #wrapContainer(Object,int) wrapping} an existing container.
     */
    public static final int NO_STYLE=0;
    /**
     * A container style that is a grid.
     * The associated container will use Swing's <code>GridBagLayout</code> or
     * SWT's <code>GridLayout</code>.
     * <P>
     * Each child is place in position left to right, top down.
     * </code>,
     * <code>{@link IComponent#setHorizontalAlignment(int)}</code>,
     * <code>{@link IComponent#setVerticalWeight(double)}</code>,
     * <code>{@link IComponent#setHorizontalWeight(double)}</code>, and

     * <p>
     */
    public static final int GRID_STYLE=1;
    /**
     * A container style that is a horizontal row, that does not wrap.
     * The following methods if a child component are used:
     * <ul>
     * <code>{@link IComponent#setHorizontalAlignment(int)}</code>
     * <code>{@link IComponent#setHorizontalWeight}</code>
     * </ul>
     */
    public static final int ROW_STYLE=2;
    /**
     * A container style that is a vertical column, that does not wrap.
     * The following methods if a child component are used:
     * <ul>
     * <code>{@link IComponent#setVerticalAlignment(int)}</code>
     * <code>{@link IComponent#setVerticalWeight}</code>
     * </ul>
     */
    public static final int COLUMN_STYLE=3;
    /**
     * A container style that is a horizontal row that can wrap.
     * The following methods of a child component are used:
     * <ul>
     * <code>{@link IComponent#setHorizontalAlignment(int)}</code>
     * <code>{@link IComponent#setHorizontalWeight(double)}</code>
     * </ul>
     */
    public static final int FLOW_STYLE=4;
    /**
     * A "card" container.
     */
    public static final int STACK_STYLE=5;
    /**
     * Given a container that was made outside of our framework,
     * wrap it so that we can refer to it.
     * @param component an instance of Container or Composite.
     * @param style the style of the container.
     * @return the container wrapper.
     */
    IContainer wrapContainer(Object component, int style);
    
    /**
     * Given a component that was created outside our framework.
     * wrap it so that we can refer to it.
     * @param component the Component or Control being wrapped.
     * @return a wrapper.
     */
    IComponent wrapComponent(Object component);
    /**
     * Make a non-grid container, and add it to a parent container.
     * <p>
     * The returned object is a wrapper around the implementation-specific
     * container, which is retrieved by calling {@link IComponent#getComponent() getComponent}.
     * <P>
     * @param parent the parent container.
     * @param style the style of the container.
     * @returns a container wrapper, whose <code>{@link IComponent#getComponent() getComponent}
     * </code> method retrieves the container.
     */
    IContainer makeContainer(IContainer parent, int style);
    /**
     * Create a container that is based on a layout manager.
     * @param parent the parent container.
     * @param layout the layout manager that maps the
     * child components into the container.
     * @return a new container based on a layout manager.
     */
    IContainer makeContainer(IContainer parent, ILayoutManager layout);
    /**
     * Make a grid container, and add it to a parent container.
     * <p>
     * The returned object is a wrapper around the implementation-specific
     * container, which is retrieved by calling {@link IComponent#getComponent() getComponent}.
     * <P>
     * @param parent the parent container.
     * @param columns the number of columns in the container.
     * @returns a container wrapper, whose <code>{@link IComponent#getComponent() getComponent}
     * </code> method retrieves the container.
     */
    IContainer makeGridContainer(IContainer parent, int columns);
    /**
     * Make a container that  only displays one of its children at any given time.
     */
    ICardContainer makeCardContainer(IContainer parent);
    ILabel makeLabel(IContainer parent,String text);
    IButton makeButton(IContainer parent);
    IButton makeCheckBox(IContainer parent);
    IButton makeToggleButton(IContainer parent);
    IButton makeRadioButton(IContainer parent);
    IMenuItem makeMenuItem(IMenu parent);
    IMenuBar makeMenuBar(IContainer parent);
    /**
     * Make primary menu within a menu bar.
     * @param parent
     * @return the new menu within the menubar.
     */
    IMenu    makeMenu(IMenuBar parent);
    /**
     * Make a scrollable pane. It may have only one
     * chile.
     * @param parent the parent container.
     * @param clientWillHandleScrolling if true, then the client (caller)
     * will be responsible for scrolling by applying listeners to the scrollbars;
     * otherwise, scrolling is implicit.
     * @return a scroll pane.
     */
    IScrollPane makeScrollPane(IContainer parent, boolean clientWillHandleScrolling);
    /**
     * Make a submenu
     * @param parent
     * @return the new submenu.
     */
    IMenu  makeMenu(IMenu parent);
    IButton makeCheckBoxMenuItem(IMenu parent);
    IButton makeRadioButtonMenuItem(IMenu parent);
    IChoice makeComboBox(IContainer parent, boolean editable);
    ITextField makeTextField(IContainer parent);
    ITreeList makeTreeList(IContainer parent);
    /**
     * Create a selectable list.
     * @param parent parent container
     * @param multi if true, allow multiple selections.
     * @return the new list widget.
     */
    IList makeList(IContainer parent, boolean multi);
    public static final int TABS_ON_TOP = 0;
    public static final int TABS_ON_BOTTOM = 1;
    public static final int TABS_ON_LEFT = 2;
    public static final int TABS_ON_RIGHT = 3;
    ITabbedPane makeTabbedPane(IContainer parent, int tabPosition);
    ISplitPane makeSplitPane(IContainer parent, boolean horizontal);

    /**
     * Make a dialog that is tied to a gui component.
     * @param owner the owning component.
     * @param modal if true, caller will block when {@link IWindow#open()} is called.
     */
    IWindow makeDialog(Object owner, boolean modal);

    /**
     * Make a frame that can be displayed as a top-level component in the
     * host window system. For swing, it will be an instance of <code>JFrame</code>.
     * For SWT, it will be an instance of <code>Shell</code>.
     * @param owner the owning frame, if applicable.
     */
    IWindow makeFrame(Object owner);

    /**
     * File chooser will select file to open for writing.
     */
    public static final int FILE_SAVE=1;   
    /**
     * File chooser will select file to open for reading.
     */
    public static final int FILE_OPEN=2;
    /**
     * File chooser is to allow multiple files to be selected.
     */
    public static final int FILE_MULTI=4;
    /**
     * File chooser to view directories only.
     */
    public static final int FILE_DIRS=8;
    /**
     * Make a file chooser.
     */
    IFileChooser makeFileChooser(Object parent, int style);

    /**
     * Make a color based on a name.
     */
    IColor makeColor(String name);
    
    /**
     * Make a color based on the underlying OS-dependent color object.
     * For Swing it is java.awt.Color; for SWT, it is org.eclipse.swt.graphic.Color.
     */
    IColor makeColor(Object color);
    /**
     * Make a color based on RGB
     */
    IColor makeColor(int r, int g, int b);
    /**
     * Make a font.
     * @param name name of the font.
     * @param bold if true, font is to be bold.
     * @param italic if true, font is to be italic.
     * @param size point size of font.
     */
    IFont makeFont(String name, boolean bold, boolean italic, int size);
    
    /**
     * Create a font wrapper around an OS font object. 
     * @param font an instance of <code>java.awt.Font</code> or <code>org.eclipse.swt.graphics.Font</code>.
     * @return the font wrapper.
     */
    IFont makeFont(Object font);

    /**
     * Make an image from a URL path.
     * @param path the source of the image.
     */
    IImage makeImage(URL path);
    
    /**
     * Make an image that the caller can manipulate.
     * The caller must {@linkplain IImage#dispose dispose} of
     * it when no longer used.
     * @param width width of the image in pixels.
     * @param height height of the image in pixels.
     * @param depth the number of pits per pixel.
     * @return an image for the caller to manipulate.
     */
    IImage makeImage(int width, int height, int depth);

    /**
     * Enter the event dispatch loop for the GUI.
     * For Swing- and AWT-based it does nothing.
     * For SWT, it waits dispatches events from the OS.
     * @param frame the main window of the application.
     */
    void enterDispatchLoop(IWindow frame);
    
    /**
     *  Pop up an error dialog.
     */
    void showErrorDialog(Object owner, String message);
    
    /**
     *  Pop up an information dialog.
     */
    void showMessageDialog(Object owner, String message);
    
    /**
     * Make a popup menu for a component
     *
     */
    IMenu makePopupMenu(IComponent parent);
    
    /**
     * Create toolbar from which {@linkplain IToolItem toolitems} can be
     * created.
     * @param parent
     * @return the toolbar.
     */
    IToolBar makeToolBar(IContainer parent);
    
    /**
     * Create a progress bar.
     * @param parent the parent container.
     * @param vertical whether vertical or horizontal
     * @param preferredLength the preferred length of the progress bar
     * in pixels, or 0 if default is to be used.
     * @return the newly created progress widget.
     */
    IProgress makeProgress(IContainer parent, boolean vertical, int preferredLength);
    
    /**
     * Do whatever is necessary to copy a string to
     * the clip board.
     * @param s the string to be copied to the clipboard.
     */
    void copyToClipBoard(String s);
    
    /**
     * Dispose of all resources that may be cached in this
     * factory.
     */
    void dispose();
    
    /**
     * Make a string that can have embedded color
     * changes (both foreground and background).
     * @param s the string content.
     * @return the new attributed string.
     */
    IAttributedString makeAttributedString(String s);
    
    /**
     * Make text canvas (suitable for SeeCode)
     * @param parent the parent container.
     * @param includeIconColumn add left margin for icons
     * @param selectable if true, then permit text to be selected and highlighted.
     * @return a text canvas
     */
    ITextCanvas makeTextCanvas(IContainer parent, boolean includeIconColumn, boolean selectable);
    
    IImageWidget makeImageWidget(IContainer parent, IImage image);
    
    /**
     * Create a table widget.
     * @param parent the parent container.
     * @return the table widget.
     */
    ITable makeTable(IContainer parent);
    }
