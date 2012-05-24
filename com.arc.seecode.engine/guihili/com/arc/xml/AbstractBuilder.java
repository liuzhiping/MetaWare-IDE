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
package com.arc.xml;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.arc.mw.util.Cast;


/**
 * An implementation of IBuilder that can serve as a base class. It uses the "template" pattern to common up control.
 * <P>
 * The default implementation of {@link #build} first calls {@link #startNewInstance(Element)}. Then
 * {@link #setAttribute(Element,String,IAttributeDef,String) setAttribute()} is called for each attribute.
 * {@link #beginChildren(Element)} is called immediately before children are to be processed and after all attributes
 * have been set. {@link #addChild(Object,Element)} is called to add the processed children. Finally,
 * {@link #returnObject()}is called to return the result.
 * @author J. David Pickens
 * @version 5/8/02
 */
public abstract class AbstractBuilder implements IBuilder {

    /**
     * Prepare to start a new object to be built. After this call, the
     * {@link #setAttribute(Element,String,IAttributeDef,String) setAttribute()} method is called for every attribute.
     * @param element the element that we're building from.
     * @throws SAXException if element is somehow not what was expected.
     */
    protected void startNewInstance (Element element) throws SAXException {
    }

    /**
     * This is called after attributes are set but prior to children.
     * @throws SAXException if element is somehow not what was expected.
     */
    protected void beginChildren (Element element) throws SAXException {
    }

    /**
     * Add an unnamed child object.
     * @param object the object constructed for the child.
     * @param child the XML element from which the object was constructed.
     * @throws SAXException if element is somehow not what was expected.
     */
    protected void addChild (Object object, Element child) throws SAXException {
        if (mChildren == null)
            mChildren = new ArrayList<Object>();
        mChildren.add(object);
    }

    /**
     * 
     * @param text
     * @throws SAXException if element is somehow not what was expected.
     */
    protected void addText (String text) throws SAXException {
    }

    /**
     * Return children
     */
    public List<Object> getChildren () {
        if (mChildren == null)
            mChildren = new ArrayList<Object>(0);
        return mChildren;
    }

    /**
     * Return the object being constructed.
     */
    abstract protected Object returnObject () throws SAXException;

    /**
     * Process an element.
     * @param e the element
     * @param binding the binding definition for the element's parent.
     * @return the constructed object, which is also the value of the data property of the element.
     */
    @Override
    public Object build (Element e, IBinding binding, IBuilder parentBuilder) throws SAXException {
        startNewInstance(e);
        doAttributes(e, binding);
        try {
            beginChildren(e);
            List<Element> kids = Cast.toType(e.elements());
            for (Element kid : kids) {
                doChild(kid, binding);
            }
            // Here we decorate the element with the constructed object.
            Object data = null;
            try {
                data = returnObject();
                e.setData(data);
            }
            catch (SAXParseException x) {
                throw x;
            }
            catch (SAXException x) {
                Exception ex = x;
                if (x.getException() != null)
                    ex = x.getException();
                error(e, ex.getMessage(), ex);
            }
            return data;
        }
        finally {
            cleanup(); // if any cleanup to do.
        }
    }

    /**
     * Called after everything is built to clean things up.
     */
    protected void cleanup () {
    }

    /**
     * Process a child.
     * @param child the child element.
     * @param parentBinding the binding definition of the parent.
     * @return the constructed object, or null if there isn't one.
     */
    protected Object doChild (Element child, IBinding parentBinding) throws SAXException {
        IBinding childBinding = parentBinding.getBinding(child.getName());
        /* If not found, look for wild card binding... */
        if (childBinding == null)
            childBinding = parentBinding.getBinding("*Element");
        if (childBinding == null) {
            error(child, "Not recognized as child of \"" + parentBinding.getTagName() + "\" in this context.");
        }
        else {
            IBuilder builder = getBuilderFrom(childBinding, child);
            Object d = builder.build(child, childBinding, this);
            if (d != null)
                addChild(d, child);
            return d;
        }
        return null;
    }

    /**
     * Given a binding, return the Builder object that can be used to construct corresponding objects.
     * @param binding the binding from which an object is to be built.
     * @param element the corresponding tag (for benefit of error diagnostics).
     */
    private IBuilder getBuilderFrom (IBinding binding, Element element) throws SAXException {
        try {
            return binding.getBuilder();
        }
        catch (InvocationTargetException x) {
            Throwable t = x.getTargetException();
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            Exception e = x;
            if (t instanceof Exception)
                e = (Exception) t;
            error(element, "Can't instantiate tag " + binding.getTagName() + " (" + t.getMessage() + ")", e);
        }
        catch (Exception x) {
            error(element, "Can't instantiate tag " + binding.getTagName() + " (" + x.getMessage() + ")", x);
        }
        return null;
    }

    /**
     * Given an attribute value, convert it to an object according to the corresponding Attribute Definition. By return
     * it as a string, it will later be coerced to an integer or boolean as need be.
     * @param value the value of an attribute.
     * @param adef the corresponding attribute definition.
     * @throws SAXException if element is somehow not what was expected.
     */
    protected Object valueOf (String value, IAttributeDef adef) throws SAXException {
        return value;
    }

    /**
     * Handle undeclared attribute
     * @param e the element on which the attribute was found
     * @param binding the associated binding definition.
     */
    protected void unknownAttribute (Element e, IBinding binding, Attribute a) throws SAXException {
        error(e, "attribute \"" + a.getName() + "\" of node \"" + binding.getTagName() + "\" isn't recognized");
    }

    /**
     * Check that attributes of element are valid and that all required attributes are there Apply attributes that are
     * valid to the builder via reflection by calling <code>"set<i>Property</i>"</code>.
     */
    protected void doAttributes (Element e, IBinding binding) throws SAXException {
        List<Attribute> attrs = Cast.toType(e.attributes());
        for (Attribute a : attrs) {
            IAttributeDef adef = binding.getAttribute(a.getName());
            if (adef == null)
                unknownAttribute(e, binding, a);
            else
                try {
                    setAttribute(e, a.getName(), adef, a.getValue());
                }
                catch (NoSuchMethodException x) {
                    error(e, "Can't set attribute " + adef.getName());
                }
        }
        /*
         * Look for missing required attributes
         */
        for (IAttributeDef a : binding.getAttributes().values()) {
            if (a.isRequired() && e.attributeValue(a.getName()) == null && !accessedByAlias(e, binding, a))
                error(e, "Required attribute \"" +
                    a.getName() +
                    "\" for tag \"" +
                    binding.getTagName() +
                    "\" is missing");
        }
    }

    /**
     * Return true if there is an attribute definition in element "e" that references attribute definition "a". Used to
     * diagnose missing required attributes.
     */
    static boolean accessedByAlias (Element e, IBinding binding, IAttributeDef adef) {
        List<Attribute> attrs = Cast.toType(e.attributes());
        for (Attribute a : attrs) {
            IAttributeDef ad = binding.getAttribute(a.getName());
            if (adef == ad)
                return true;
        }
        return false;
    }

    /**
     * Set an attribute value by looking for a property within this object to set.
     * @param e the element to which attribute belongs.
     * @param name attribute name (which may differ from <code>adef.getName()</code> if an alias).
     * @param adef the attribute definition.
     * @param value value of the attribute.
     */
    protected void setAttribute (Element e, String name, IAttributeDef adef, String value) throws SAXException,
        NoSuchMethodException {
        try {
            Object v = valueOf(value, adef);
            // If a string, then coerce to integer or boolean, if necessary
            if (v instanceof String)
                PropertySetter.setProperty(this, adef, (String) v);
            else
                PropertySetter.setProperty(this, adef, v);
        }
        catch (NoSuchMethodException x) {
            throw x;
        }
        catch (RuntimeException x) {
            throw x;
        }
        catch (Exception x) {
            Exception xe = x;
            if (x instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) x).getTargetException();
                if (t instanceof Exception)
                    xe = (Exception) t;
                else if (t instanceof Error)
                    throw (Error) t;
            }
            String msg = "";
            if (xe instanceof SAXException && ((SAXException) xe).getException() != null) {
                msg = xe.getMessage();
                xe = ((SAXException) xe).getException();
            }
            error(e.getLocator(), "Can't set attribute " +
                adef.getName() +
                " in node tagged as " +
                e.getTagName() +
                ": " +
                msg, xe);
        }
    }

    protected void error (Element e, String msg, Exception exception) throws SAXException {
        error(e.getLocator(), msg, exception);
    }

    protected void error (Locator loc, String msg, Exception exception) throws SAXException {
        SAXParseException x = new SAXParseException(msg, loc, exception);
        throw x;
    }

    protected void error (Locator e, String msg) throws SAXException {
        error(e, msg, null);
    }

    protected void error (Element e, String msg) throws SAXException {
        error(e.getLocator(), "tag " + e.getName() + ": " + msg);
    }

    private List<Object> mChildren;
}
