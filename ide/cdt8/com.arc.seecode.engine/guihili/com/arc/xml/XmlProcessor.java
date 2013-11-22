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


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.QName;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.arc.mw.util.Cast;


/**
 * This class reads in an instance of a "meta-XML" description, and produces an XML processor that can be used to read
 * another XML file.
 * <P>
 * To invoke the resulting processor, invoke {@link #read(InputSource,IBuilderInstantiator,String,ErrorHandler)}. The
 * resulting <code>{@link Document}</code> is tree. The root element's
 * <code>{@link Element#getData() getData()}</code> contains the resulting data structure.
 * <P>
 * Here is how to use it:
 * 
 * <pre>
 * 
 *  ...
 *  try {
 *     XmlProcessor processor = new XmlProcessor(&quot;meta.xml&quot;, new MyInstantiator(),&quot;my.package&quot;);
 *     Document d = processor.read(&quot;myfile.xml&quot;);
 *     return d.getRootElement().getData();
 *     }
 *  catch(SAXParseExcepption){ ... }
 *  catch(SAXException){ ... }
 *  catch(IOException){ ... }
 *  catch(BadXmlException){ ... }
 *  
 * </pre>
 * 
 * @author J. David Pickens
 * @version 5/6/02
 * @version 6/28/99
 */
public class XmlProcessor {

    /**
     * Create XML processor from a <code>File</code> object. If the root package name is not null, then every class
     * name reference that isn't prefixed by "/" is assumed relative to that package.
     * @param xmlPath the path of the meta-XML file.
     */
    public XmlProcessor(File xmlPath) throws IOException, BadXmlException {
        this(Resolver.createInputSource(xmlPath));
    }

    /**
     * Create XML processor from a <code>File</code> object. If the root package name is not null, then every class
     * name reference that isn't prefixed by "/" is assumed relative to that package.
     * @param url the path of the meta-XML file.
     */
    public XmlProcessor(URL url) throws IOException, BadXmlException{
        this(Resolver.createInputSource(url));
    }

    public XmlProcessor(InputSource input) throws BadXmlException {
        try {
            Document doc = new MySAXReader().read(input);
            Element root = (Element) doc.getRootElement();
            processMeta(root);
        }
        catch (DocumentException x) { // Shouldn't happen
            throw new BadXmlException(x.getMessage());
        }
    }

    public XmlProcessor(String file) throws IOException, BadXmlException {
        this(new File(file));
    }

    /**
     * If we need to use a custom parser, then set it here.
     */
    public void setXMLReader (XMLReader reader) {
        mXMLReader = reader;
    }

    /**
     * Invoked by {@link Binding}class to get instantiator.
     */
    IBuilderInstantiator getInstantiator () {
        return mInstantiator;
    }

    /**
     * Invoked by {@link Binding}class to get root package.
     */
    String getRootPackage () {
        return mRootPackage;
    }

    /**
     * Read a document from the source given the constraints defined by the meta-xml that was received in the
     * {@link #XmlProcessor(File) constructor}.
     * <P>
     * The result is a document whose root element's <code>getData()</code> method will return the resulting data
     * obect.
     * @param source the XML file to be read.
     * @param instantiator the object for instantiating classes to form objects as we walk the XML document.
     * @param rootPackage the package relative to which classes are located.
     * @param ehandler error handler
     */
    public Document read (
            InputSource source,
            IBuilderInstantiator instantiator,
            String rootPackage,
            ErrorHandler ehandler) throws SAXParseException, SAXException {
        mInstantiator = instantiator;
        mRootPackage = rootPackage;
        SAXReader reader = new MySAXReader();
        if (mXMLReader != null)
            reader.setXMLReader(mXMLReader);
        mErrorHandler = ehandler;
        reader.setErrorHandler(ehandler);
        try {
            Document doc = reader.read(source);
            walk(doc);
            return doc;
        }
        catch (SAXParseException x) {
            if (x.getException() instanceof RuntimeException)
                throw (RuntimeException) x.getException();
            if (mErrorHandler != null)
                mErrorHandler.error(x);
            else
                throw x;
        }
        catch (SAXException x) {
            // unwrap nested exceptions
            while (x.getException() != null) {
                Exception t = x.getException();
                if (t instanceof SAXException && !(t instanceof SAXParseException))
                    x = (SAXException) t;
                else if (t instanceof RuntimeException)
                    throw (RuntimeException) t;
            }
            if (mErrorHandler != null && x instanceof SAXParseException) {
                mErrorHandler.error((SAXParseException) x);
            }
            else
                throw x;
        }
        catch (DocumentException x) {
            throw new SAXException("DocumentException", x);
        }
        return null;
    }

    private void walk (Document doc) throws SAXParseException, SAXException {
        Element e = (Element) doc.getRootElement();
        // This will be the root element
        IBinding b = mRoot.getBinding(e.getName());
        if (b == null)
            error(e, "Don't recognize root node \"" + e.getName() + "\"");
        else {
            IBuilder bb = null;
            try {
                bb = b.getBuilder();
            }
            catch (RuntimeException x) {
                throw x;
            }
            catch (Exception x) {
                error(e, "Can't instantiate builder for root \"" + e.getName() + "\": " + x.getMessage());
                return;
            }
            bb.build(e, b, null);
        }
    }

    /**
     * Process:
     * 
     * <pre>
     * 
     *    &lt;bindings&gt; ... &lt;/bindings&gt;
     *  
     * </pre>
     */
    private void processMeta (Element root) throws BadXmlException {
        if (!root.getName().equalsIgnoreCase("bindings"))
            metaError(root, "Root of meta XML must be \"bindings\"");
        mRoot = new Binding("<root>", null, null, null, this);
        doBindingKids(mRoot, root);
    }

    // /**
    // * Process a "binding" definition:
    // * <pre>
    // * &lt;binding tag="tagname" class="classname" [inherits="tagname"] &gt;
    // * &lt;attr name="propertyName" [type="{int|boolean|float|string}"]/&gt;
    // * ...
    // * &lt;binding tag="tagname" [class="classname" ]&gt;
    // * ...
    // * &lt;/binding&gt;
    // * </pre>
    // *
    // * a "inherits" specification means that the tag inherits the behavior of
    // * a preceeding tag ( that is, it inherits the attributes that it excepts)
    // * <p>
    // * A nesting binding definition refs to children that are allowed.
    // * If no "class" is specified then any preceeding definition is used.
    // *
    // */
    // private Binding createBinding(Element element)
    // throws BadXmlException
    // {
    // return createBinding(null,element);
    // }

    /**
     * Extract attribute from element. Return null if attribute isn't defined. Complain if it is required, but is
     * missing.
     */
    private String getAttribute (Element element, String name, boolean required) throws BadXmlException {
        String attribute = element.attributeValue(name);
        if (attribute == null || attribute.length() == 0) {
            if (required)
                metaError(element, "Required attribute " + name + " is missing.");
            attribute = null;
        }
        return attribute;
    }

    /**
     * Process a single binding definition.
     * @param parent the tag inwhich this node will be defined
     * @param element the definition of the binding.
     */
    private Binding createBinding (Binding parent, Element element) throws BadXmlException {
        String tagName = getAttribute(element, "tag", true);
        String className = getAttribute(element, "class", parent == null);
        String baseName = getAttribute(element, "inherits", false);
        Binding baseBinding = null;
        if (baseName != null) {
            Binding p = parent;
            while (baseBinding == null && p != null) {
                baseBinding = (Binding) p.getBinding(baseName);
                p = (Binding) p.getParent();
            }
            if (baseBinding == null)
                metaError(element, "For tag " + tagName + ": base binding " + baseName + " not found");
        }
        /*
         * If no class is specified and no base, then we're referencing a previously defined binding.
         */
        if (className == null && baseBinding == null && parent != null) {
            /*
             * Hey, we may be referencing the parent for a recursive structure. E.g., <menu> <menu...> </menu> </menu>
             */
            if (parent.getTagName().equals(tagName))
                baseBinding = parent;
            else {
                Binding parentParent = (Binding) parent.getParent();
                while (baseBinding == null && parentParent != null) {
                    IBinding b = parentParent.getBinding(tagName);
                    if (b != null) {
                        baseBinding = (Binding) b;
                    }
                    else
                        parentParent = (Binding) parentParent.getParent();
                }
            }
        }
        if (className == null) {
            if (baseBinding != null)
                className = baseBinding.getClassName();
            // -- No, permit classless tags as "abstract"
            // else
            // error(element,"tag " + tagName + " is missing a class attribute");
        }
        Binding binding = new Binding(tagName, className, baseBinding, parent, this);
        doBindingKids(binding, element);
        return binding;
    }

    private void doBindingKids(Binding binding, Element element)
            throws BadXmlException {
        List<Element> list = Cast.toType(element.elements());
        for (Element kid : list) {
            String tag = kid.getName();
            if (tag.equals("attr") || tag.equals("attribute")) {
                AttributeDef attr = defineAttribute(binding, kid);
                if (attr != null) // null if error
                    binding.addAttribute(attr);
            } else if (tag.equals("binding")) {
                Binding b = createBinding(binding, kid);
                binding.addBinding(b);
            } else
                metaError(kid, "unrecognized child of <binding> node");
        }
    }


    /**
     * Error in the meta-xml processing
     */
    void metaError (String s) throws BadXmlException {
        throw new BadXmlException(s);
    }

    void metaError (Element element, String s) throws BadXmlException {
        throw new BadXmlException(element, s);
    }

    void error (Locator loc, String msg, Exception exception) throws SAXException {
        SAXParseException x = new SAXParseException(msg, loc, exception);
        if (mErrorHandler != null) {
            mErrorHandler.error(x);
        }
        else
            throw x;
    }

    void error (Locator e, String msg) throws SAXException {
        error(e, msg, null);
    }

    void error (Element e, String msg) throws SAXException {
        error(e.getLocator(), "tag " + e.getName() + ": " + msg);
    }

    /**
     * Handle an attribute specification.
     * 
     * <pre>
     * 
     *  &lt;attr name=&quot;...&quot; type=&quot;...&quot; &gt;/&gt;
     *  
     * </pre>
     * 
     * <p>
     * An attribute may correspond to an object, in which case it will have a single binding node underneath.
     * <p>
     * 
     * <pre>
     * 
     *  &lt;attr name=&quot;...&quot; type=&quot;...&quot; &gt;
     *     &lt;binding ... /&gt;
     *  &lt;/attr&gt;
     *  
     * </pre>
     */
    AttributeDef defineAttribute (Binding owner, Element element) throws BadXmlException {
        String name = getAttribute(element, "name", true);
        String typeName = getAttribute(element, "type", false);
        String required = getAttribute(element, "required", false);
        String aggregate = getAttribute(element, "aggregate", false);
        String alias = getAttribute(element, "alias", false);
        String deferred = getAttribute(element, "delay", false);
        if (name == null)
            return null;
        boolean isRequired = required != null && required.equalsIgnoreCase("true");
        boolean delayEval = deferred != null && deferred.equalsIgnoreCase("true");
        List<Element> list = Cast.toType(element.elements());
        int kidCnt = list.size();
        int type = kidCnt > 0 ? IAttributeDef.OBJECT : IAttributeDef.STRING;
        if (kidCnt > 0 && aggregate != null)
            metaError(element, "\"aggregate\" attribute applies to object attributes only");

        if (typeName != null) {
            typeName = typeName.toLowerCase();
            if (kidCnt > 0)
                metaError(element, "Superfluous attribute type: " + typeName);
            else if (typeName.equals("int"))
                type = IAttributeDef.INT;
            else if (typeName.equals("string"))
                type = IAttributeDef.STRING;
            else if (typeName.startsWith("bool"))
                type = IAttributeDef.BOOLEAN;
            else if (typeName.startsWith("float"))
                type = IAttributeDef.FLOAT;
            else if (typeName.startsWith("action"))
                type = IAttributeDef.ACTION;
            else if (typeName.startsWith("list"))
                type = IAttributeDef.LIST;
            else
                metaError(element, "Bad attribute type: " + typeName);
        }
        /*
         * If the name starts with "*", then it is to be considered a property of the Builder class.
         */
        boolean isProperty = true;
        if (name.charAt(0) == '*') {
            name = name.substring(1);
            isProperty = false;
        }
        if (kidCnt > 0) {
            Element kid = list.get(0);
            if (kid.getName().equals("binding")) {
                Binding binding = createBinding(owner, kid);
                return new AttributeDef(name, binding, isProperty, isRequired,
                        aggregate != null && aggregate.equalsIgnoreCase("true"));
            }
            metaError(kid, "Unrecognized child of attribute " + name);
        }
        AttributeDef ap = new AttributeDef(name, type, isProperty, isRequired, delayEval);
        if (alias != null)
            ap.setAlias(alias);
        return ap;
    }

    private Binding mRoot;

    private String mRootPackage; // prefixed to all class references

    private IBuilderInstantiator mInstantiator;

    private ErrorHandler mErrorHandler;

    private XMLReader mXMLReader; // custom reader (e.g., guihili)
}

/**
 * Customized SAXReader that creates elements with an implicit "where" attribute.
 */


class MySAXReader extends SAXReader {

    MySAXReader() {
        super(new MyDocumentFactory());
    }

    @Override
    public SAXContentHandler createContentHandler (XMLReader reader) {
        return new MySAXContentHandler((MyDocumentFactory) getDocumentFactory());
    }
}


class MySAXContentHandler extends SAXContentHandler {

    MySAXContentHandler(MyDocumentFactory factory) {
        super(factory);
        mFactory = factory;
    }

    @Override
    public void setDocumentLocator (Locator loc) {
        mFactory.setSourceLocator(loc);
    }

    private MyDocumentFactory mFactory;
}

/**
 * A document factory that creates {@link Element}instances for elements.
 */


class MyDocumentFactory extends DocumentFactory {

    @Override
    public org.dom4j.Element createElement (QName name) {
        return new Element(name, mLoc.getSystemId(), mLoc.getLineNumber());
    }

    void setSourceLocator (Locator loc) {
        mLoc = loc;
    }

    private Locator mLoc;
}

/**
 * An attribute definition. If the "property" attribute is set, then reflection will be used to set attributes by
 * calling "setFoo(xxx)" If "alias" is set, then this attribute is an alias for the "alias" one.
 */


class AttributeDef implements IAttributeDef {

    public AttributeDef(String name, int type, boolean isProperty, boolean isRequired, boolean defer) {
        mName = name;
        mType = type;
        mIsProperty = isProperty;
        mIsRequired = isRequired;
        mIsAggregate = false;
        mBinding = null;
        mDelay = defer; // delay evaluating
    }

    public AttributeDef(String name) {
        this(name, IAttributeDef.STRING, false, false, false);
    }

    public AttributeDef(String name, Binding binding, boolean isProperty, boolean isRequired, boolean isAggregate) {
        this(name, OBJECT, isProperty, isRequired, false);
        mIsAggregate = isAggregate;
        mBinding = binding;
    }

    @Override
    public String getName () {
        return mName;
    }

    @Override
    public int getType () {
        return mType;
    }

    @Override
    public boolean isProperty () {
        return mIsProperty;
    }

    @Override
    public boolean isAggregate () {
        return mIsAggregate;
    }

    @Override
    public boolean isRequired () {
        return mIsRequired;
    }

    @Override
    public IBinding getBinding () {
        return mBinding;
    }

    @Override
    public boolean delayEvaluation () {
        return mDelay;
    }

    void setAlias (String name) {
        mAlias = name;
    }

    String getAlias () {
        return mAlias;
    }

    private String mName;

    private int mType;

    private boolean mIsProperty;

    private boolean mIsRequired;

    private boolean mIsAggregate;

    private IBinding mBinding;

    private String mAlias;

    private boolean mDelay;
}

/**
 * An implementation of the {@link IBinding}interface. There is one of these per element node of the "meta-xml" schema.
 */


class Binding implements IBinding {

    /**
     * @param tagname the name of this node being defined.
     * @param className the name of the Java class that is to process this node.
     * @param base another node, if any, that this node will "inherit" from; that is, will process that same attributes
     * and sub-nodes.
     * @param parent if not null, the node that this node must reside in.
     * @param processor the XmlProcessor so that we can get instantiator and root package.
     */
    Binding(String tagname, String className, Binding base, Binding parent, XmlProcessor processor) {
        mTagName = tagname;
        mClassName = className;
        mClass = null; // Evaluate lazily
        mBase = base;
        mParent = parent;
        mSubBindings = new HashMap<String,IBinding>();
        mAttributes = new HashMap<String,IAttributeDef>();
        mProcessor = processor;
    }

    @Override
    public String getTagName () {
        return mTagName;
    }

    @Override
    public IBinding getParent () {
        return mParent;
    }

    /**
     * Lookup an attribute name and return the AttributeDef object if it exists; null otherwise.
     */
    @Override
    public IAttributeDef getAttribute (String attrName) {
        IAttributeDef attr = lookupAttribute(attrName);
        if (attr == null && mBase != null) {
            attr = mBase.getAttribute(attrName);
        }
        return attr;
    }

    @Override
    public Map<String,IAttributeDef>getAttributes () {
        return mAttributes;
    }

    @Override
    public Map<String,IBinding>getBindings () {
        return mSubBindings;
    }

    String getClassName () {
        return mClassName;
    }

    /**
     * Lazily load the builder class for this tag
     */
    @SuppressWarnings("unchecked")
	Class<IBuilder> getBuilderClass () throws ClassNotFoundException {
        if (mClass == null && mClassName != null && mClassName.length() > 0) {
            String className = mClassName;
            if (mClassName.charAt(0) == '.') {
                String rootPackage = mProcessor.getRootPackage();
                if (rootPackage == null)
                    throw new ClassNotFoundException("root package is missing for " + className);
                className = rootPackage + className;
            }
            mClass = (Class<IBuilder>)Class.forName(className);
        }
        return mClass;
    }

    @Override
    public IBuilder getBuilder () throws NoSuchMethodException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, SecurityException, ClassNotFoundException {
        Class<IBuilder> builderClass = this.getBuilderClass();
        if (builderClass == null)
            throw new ClassNotFoundException("No builder for tag \"" + getTagName() + "\"");
        return mProcessor.getInstantiator().instantiate(builderClass);
    }

    @Override
    public IBinding getBinding (String tagName) {
        tagName = tagName.toLowerCase();
        IBinding b = lookupBinding(tagName);
        /*
         * If tag isn't found, then look for definitions of "tagName" in parent bindings and see if this one is a
         * subclass. For example a "container" tag may function as a "component" tag.
         */
        if (b == null) {
            IBinding superBinding = getSuperBinding(tagName);
            IBinding base = superBinding;
            while (b == null && base != null) {
                b = lookupBinding(base.getTagName());
                if (b == null)
                    base = base.getBase();
            }
            if (b != null)
                return superBinding;
        }
        if (b == null && mBase != null) {
            b = mBase.getBinding(tagName);
        }
        return b;
    }

    /**
     * Look at parents for a binding with tag. tagName assumed to have already been downshifted to lowercase.
     */
    private IBinding getSuperBinding (String tagName) {
        Binding parent = (Binding) getParent();
        if (parent != null) {
            IBinding b = parent.getBinding(tagName);
            if (b == null)
                b = parent.getSuperBinding(tagName);
            return b;
        }
        return null;
    }

    @Override
    public IBinding getBase () {
        return mBase;
    }

    void addAttribute (AttributeDef attr) throws BadXmlException {
        String name = attr.getName();
        String alias = attr.getAlias();
        if (alias != null) {
            AttributeDef d = (AttributeDef) getAttribute(alias);
            if (d == null)
                throw new BadXmlException("attribute definition \""
                        + attr.getName()
                        + "\" has reference to alias \""
                        + alias
                        + "\" that isn't yet defined for "
                        + getTagName());
            attr = d;
        }
        if (mAttributes.put(name, attr) != null)
            throw new BadXmlException("attribute definition \""
                    + attr.getName()
                    + "\" appears more than once in binding "
                    + getTagName());
    }

    void addBinding (IBinding binding) throws BadXmlException {
        if (mSubBindings.put(binding.getTagName().toLowerCase(), binding) != null)
            throw new BadXmlException("binding definition \""
                    + binding.getTagName()
                    + "\" appears more than once in binding "
                    + getTagName());
    }

    private AttributeDef lookupAttribute (String name) {
        return (AttributeDef) mAttributes.get(name);
    }

    private IBinding lookupBinding (String name) {
        return mSubBindings.get(name);
    }

    private String mTagName;

    private String mClassName;

    private Class<IBuilder> mClass;

    private Binding mBase;

    private IBinding mParent;

    private HashMap<String,IAttributeDef> mAttributes;

    private HashMap<String, IBinding> mSubBindings;

    private XmlProcessor mProcessor;
}
