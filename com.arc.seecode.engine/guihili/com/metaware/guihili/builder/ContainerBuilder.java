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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.List;

import org.xml.sax.SAXException;

import com.arc.mw.util.Cast;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILabel;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.MalformedExpressionException;

/**
 * class for constructing things that are containers.
 * 
 * @author J. David Pickens
 */
public class ContainerBuilder extends ComponentBuilder {
    public ContainerBuilder(Gui gui) {
        super(gui);
    }

    /**
     * Becomes the title of the dialog if this container is the top level one.
     * We grab it from the Element.
     */
    public void setTitle(String title) {
        try {
            _gui.setProperty("title", title);
        } catch (PropertyVetoException x) {
        }
    }

    /**
     * Prepare to start a new object to be built. After this call, the "set<em>Property</em>(value)"
     * method will be called to set each attribute that is flagged as a
     * "property" Others are set by calling "setAttribute()"
     */
    @Override
    public void startNewInstance(Element element) throws SAXException {
        // If we have any part that is "expandable" then expand this container.
        String name = element.getName();
        if (name.equals("container") || name.equals("box"))
            if (containsExpandable(element))
                setExpandable(true);
    }

    public void setGap(int gap) {
        mVGap = gap;
        mHGap = gap;
    }

    public void setVGap(int gap) {
        mVGap = gap;
    }

    public void setHGap(int gap) {
        mHGap = gap;
    }

    public void setLine_up_columns(boolean b) {
        mLineUpColumns = b;
    }

    public void setLeftMargin(int left) {
        mLeft = left;
        mMarginsSet = true;
    }

    public void setRightMargin(int right) {
        mRight = right;
        mMarginsSet = true;
    }

    public void setTopMargin(int top) {
        mTop = top;
        mMarginsSet = true;
    }

    public void setBottomMargin(int bottom) {
        mBottom = bottom;
        mMarginsSet = true;
    }

    /**
     * Return true if element "e" is a component that is "expandable" or is a
     * vertical container for such. This is needed to handle old legacy guihili.
     */
    static boolean containsExpandable(Element e) {
        List<Element> elements = Cast.toType(e.elements());
        for (Element k : elements) {
            String ex = k.attributeValue("expandable");
            if ("1".equals(ex))
                return true;
            if (k.getName().equals("container") || k.getName().equals("box")) {
                //String dir = k.attributeValue("direction");
                if (true /*|| dir == null || dir.indexOf("hor") < 0*/) {
                    if (containsExpandable(k))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void beginChildren(Element e) throws SAXException {
        // If we have embedded "set", then force new environment
        if (e.element("set") != null) {
            newEnvironment();
        }
        mParent = _gui.getParent();
        IContainer c = makeContainer();
        // If a column layout has an element that is "expandable" then
        // expand the container horizontally also. This is to conform to
        // old legacy guihili
        if (getComponent() != null && mMarginsSet) {
            IComponent ic = getComponent();
            ic.setMargins(mTop, mLeft, mBottom, mRight);
        }

        super.beginChildren(e);
        _gui.setParent(c);
    }
    
    public void setColumns(int columns){
        mColumns = columns;
        if (columns > 1){
            mStyle = IComponentFactory.GRID_STYLE;
        }
    }
    
    public void setRows(int rows){
        // Don't know what to do here
    }
    
    public void setEqualWidth(boolean b){
        //mEqualWidths = b;
    }

    /**
     * Return the actual GUI container.
     */
    protected IContainer makeContainer() {
        IContainer c;
        if (mStyle == IComponentFactory.GRID_STYLE) {
            c = _gui.getComponentFactory().makeGridContainer(mParent, mColumns);
        } else
            c = _gui.getComponentFactory().makeContainer(mParent, mStyle);
        setComponent(c);

        if (mHGap >= 0)
            c.setHorizontalSpacing(mHGap);
        if (mVGap >= 0)
            c.setVerticalSpacing(mVGap);
        if (mMarginsSet)
            c.setMargins(mTop, mLeft, mBottom, mRight);
        finishComponent(c);
        return c;
    }

    protected int getGap() {
        return mVGap;
    }

    /**
     * Direction is "vertical" or "horizontal"
     */
    public void setDirection(String direction) {
        if (direction.equalsIgnoreCase("vertical")) {
            mStyle = IComponentFactory.COLUMN_STYLE;
        } else
            mStyle = IComponentFactory.ROW_STYLE;
    }

    /**
     * an action to be performed when "save" action occurs. Inherited from
     * Guihili
     */
    public void setOn_save(final Object action) {
        final IEnvironment env = _gui.getEnvironment();
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    if (getComponent() != null && getComponent().isEnabled())
                        _gui.getEvaluator().evaluate(action, env);
                } catch (MalformedExpressionException x) {
                    _gui.handleException(x.getMessage(), x);
                }
            }
        };
        _gui.addAction("OK", a);
    }

    /*
     * Given a component that may be a container, return the leading label, if
     * there is one. @param c the container to line up columns in. @param top if
     * true, this is the top most container.
     */

    private ILabel leadingLabelFor(IComponent c, boolean top) {
        if (c instanceof ILabel && !top)
            return (ILabel) c;
        if (c instanceof IContainer) {
            IContainer container = (IContainer) c;
            IComponent kids[] = container.getChildren();
            if (kids.length > 0) {
                if (kids.length == 1)
                    return leadingLabelFor(kids[0], top);
                if (container.getContainerStyle() == IComponentFactory.ROW_STYLE
                        || container.getContainerStyle() == IComponentFactory.FLOW_STYLE)
                    return leadingLabelFor(kids[0], false);
            }
        }
        return null;
    }

    /*
     * Go through container and look for panels that are aligned vertical. Make
     * sure that if the have any JLabel's that are under each other, make them
     * the same width.
     */
    private int computeWidestLeadingLabel(IContainer c) {
        if (c.getContainerStyle() == IComponentFactory.COLUMN_STYLE) {
            IComponent component[] = c.getChildren();
            int width = 0;
            for (int i = 0; i < component.length; i++) {
                int w = 0;
                ILabel label = leadingLabelFor(component[i], true);
                if (label != null) {
                    w = label.computeSize(0, 0).width;
                } else if (component[i] instanceof IContainer)
                    w = computeWidestLeadingLabel((IContainer) component[i]);
                width = Math.max(width, w);
            }
            return width;
        }
        return 0;
    }

    private void lineUpColumns(IContainer c) {
        int width = computeWidestLeadingLabel(c);
        if (width > 0) {
            // give some clearence
            fixLeadingLabelsWithin(c, width + 4);
        }
    }

    private void fixLeadingLabelsWithin(IContainer c, int labelWidth) {
        if (c.getContainerStyle() == IComponentFactory.COLUMN_STYLE) {
            IComponent component[] = c.getChildren();
            for (int i = 0; i < component.length; i++) {
                ILabel label = leadingLabelFor(component[i], true);
                if (label != null) {
                    /*
                     * System.out.println( "Setting label \"" + label.getText() +
                     * "\" to width " + labelWidth);
                     */
                    label.setPreferredSize(labelWidth, label.computeSize(
                            labelWidth, 0).height);
                } else if (component[i] instanceof IContainer)
                    fixLeadingLabelsWithin((IContainer) component[i],
                            labelWidth);
            }
        }
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        _gui.setParent(mParent); // restore parent
    }

    @Override
    protected Object returnObject() throws SAXException {
        Object o = super.returnObject();
        if (mLineUpColumns)
            lineUpColumns((IContainer) o);
        return o;
    }

    private int mStyle = IComponentFactory.COLUMN_STYLE;

    private int mVGap = -1;

    private int mHGap = -1;

    private boolean mLineUpColumns;

    private int mColumns = 1;

    private IContainer mParent; // This objects parent

    private int mLeft = 0, mRight = 0, mTop = 0, mBottom = 0;

    private boolean mMarginsSet;

    /**
     * @see com.arc.xml.AbstractBuilder#addChild(Object, Element)
     */
    @Override
    protected void addChild(Object object, Element child) throws SAXException {
        // if (object instanceof IComponent
        // && getComponent() instanceof IContainer) {
        // IComponent c = (IComponent) object;
        // _gui.registerChild((IContainer) getComponent(), c);
        // }
    }

}
