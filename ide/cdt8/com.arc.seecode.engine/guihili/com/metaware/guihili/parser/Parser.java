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
package com.metaware.guihili.parser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.IFileResolver;
import com.metaware.guihili.MalformedExpressionException;

/**
 * The actual Guihili parser.
 * It makes classic guihili look like XML.
 *
 * @author David Pickens
 * @version April 24, 2002
 */
class Parser implements org.xml.sax.XMLReader {
	/*
	 * Create a parser for a particular input stream.
	 * We pass the SAX {@link ContentHandler ContentHandler} interface
	 * so that Guihili appears to be XML!
	 *
	 * @param resolver handles #include directives; associated input stream with
	 * an unqualified name.
	 * @param eval expression evaluator that is required by "#include" and "#c_include"
	 * directives.
	 */
	Parser(IFileResolver resolver, IEvaluator eval, IEnvironment env) {
		mResolver = resolver;
		mEvaluator = eval;
		mEnvironment = env;
	}

	/**
	 * Overriding XML parser to handle Guihili
	 */
	@Override
    public void parse(org.xml.sax.InputSource input)
		throws SAXException, IOException {
		if (input.getCharacterStream() != null)
			parse(input.getCharacterStream(), input.getSystemId());
		else if (input.getByteStream() != null) {
			parse(input.getByteStream(), input.getSystemId());
		} else if (input.getSystemId() != null) {
			String fn = input.getSystemId();
			if (fn.startsWith("file:"))
				parse(fn.substring(5));
			else
				parse(new URL(fn).openStream(), fn);
		} else
			throw new FileNotFoundException("Can't resolve input");
	}

	/**
	 * Parse a stream
	 */
	public void parse(InputStream s, String name)
		throws SAXException, IOException {
		parse(new InputStreamReader(s), name);
	}
	/**
	 * parse a file name
	 */
	@Override
    public void parse(String systemId) throws SAXException, IOException {
		InputSource r = mResolver.openFile(systemId);
		if (r != null)
			parse(r);
		else
			throw new FileNotFoundException("Can't open " + systemId);
	}
	/*
	 * Parse the contents of a stream. Any exceptions will reference the
	 * line, assuming that the stream is positioned at the first line.
	 *
	 * @exception IOException some sort of error occurred on input.
	 * @exception SAXException a syntactic or lexical error occurred.
	 */
	void parse(Reader r, String streamName) throws SAXException, IOException {
		SourceRef sref = new SourceRef(streamName != null ? streamName : "");
		mTokenizer = new Tokenizer(r, sref, mErrorHandler);
		if (mLexicalHandler != null)
			mTokenizer.setLexicalHandler(mLexicalHandler);
		mContentHandler.setDocumentLocator(new SourceLocator(sref, 1));
		mContentHandler.startDocument();
		// We need one root element named "gui"
		mContentHandler.startElement(null, null, "gui", new AttributesImpl());

		Token t = nextToken();
		// make "current token" the first token of the stream.
		while (t.getKind() != Token.EOF) {
			parseComponent();
			t = currentToken();
		}

		mContentHandler.endElement(null, null, "gui");
		mContentHandler.endDocument();
	}

	/*
	 * Parse a single component.
	 *
	 */
	private void parseComponent() throws SAXException, IOException {
		Token t = currentToken();
		mContentHandler.setDocumentLocator(
			new SourceLocator(t.getSourceRef(), t.getLine(), t.getColumn()));
		char closeParen = ')';
		int startLine = t.getLine();
		switch (t.getKind()) {
			case Token.EOF :
				return;
			case Token.LESSTHAN : // <
				closeParen = '>';
                //$FALL-THROUGH$
            case Token.LPAREN : // (
				t = nextToken();
				break;
			case Token.INCLUDE :
				doInclude(false);
				return;
			case Token.C_INCLUDE :
				doInclude(true);
				return;
			default :
				error(t, "'(' expected.");
				return;
		}
		//
		// NOTE: 1.4 of AttributesImpl has a bug in "removeAttribute".
		// So, we cannot use it for buffering up attributes because we can't
		// delete the "component" one.
		// Therefore, we use a hash map.
		// Actually, we use a LinkedHashMap so that we maintain order;
		// guihili requires this because some attributes enter things in environment
		// that are referenced by subsequent attributes.
		HashMap<String,String> attrMap = new LinkedHashMap<String,String>();
		//
		// The old syntax was:
		// ( component=name attr1=... attr2=... )
		// We now permit
		// ( name attr1=... attr2=... )
		String nodeName = null;
		t = currentToken();
		if (t.getKind() == Token.IDENTIFIER) {
			Token peek = peekToken();
			if (peek.getKind() != Token.EQUAL) {
				nodeName = t.getText();
				nextToken();
			} else
				// For old Guihili component, we emit a "legacy=1" attribute to
				// relax attribute name chacking.
				attrMap.put("legacy", "1");
		}
		//
		// Parse property assignments
		//
		boolean done = false;
		while (!done) {
			t = currentToken();
			switch (t.getKind()) {
				case Token.IDENTIFIER :
					parseAttribute(attrMap);
					break;
				case Token.INCLUDE :
					doInclude(false);
					break;
				case Token.C_INCLUDE :
					doInclude(true);
					break;
				default :
					done = true;
					break;
			}
		}
		if (nodeName == null) {
			nodeName = attrMap.remove("component");
			if (nodeName == null)
				error(t, "Missing \"component\" attribute");
		}
		AttributesImpl attrs = new AttributesImpl();
        for (Map.Entry<String,String> entry: attrMap.entrySet()){
			attrs.addAttribute(
				null,
				null,
				entry.getKey(),
				"CDATA",
				entry.getValue());
		}
		mContentHandler.startElement(null, null, nodeName, attrs);
		// Place all simple string attributes in attribute list.
		// The others appear as elements.

		// Now parse embedded nodes
		while (true) {
			t = currentToken();
			switch (t.getKind()) {
				case Token.LPAREN : // (
				case Token.LESSTHAN : // <
					parseComponent();
					break;
				case Token.EOF :
					error(
						t,
						""
							+ closeParen
							+ " missing; starts at line "
							+ startLine);
					break;
				case Token.RPAREN :
					if (closeParen == ')') {
						mContentHandler.endElement(null, null, nodeName);
						nextToken();
						return;
					}
					error(t, "')' out of context.");
					break;
				case Token.GREATERTHAN :
					if (closeParen == '>') {
						mContentHandler.endElement(null, null, nodeName);
						nextToken();
						return;
					}
					error(t, "'>' out of context.");
					break;
				case Token.INCLUDE :
					doInclude(false);
					break;
				case Token.C_INCLUDE :
					doInclude(true);
					break;
				default :
					error(
						t,
						t.getText()
							+ " invalid in this context (id="
							+ t.getKind()
							+ ")");
					break;
			}
		}
	}
	
	private static String computeFileTrace(SourceRef sref, int line){
		String s = sref.getName() + ", line " + line;
		if (sref.getParentSource() != null) {
			s += "\n    from " + computeFileTrace(sref.getParentSource(),sref.getParentLine());
		}
		return s;
	}
	/**
	 * Handle a "#include" or "#c_include".
	 * We're positioned at the "#include" or "#c_include".
	 * <pre>
	 *  #include <i>name</i>
	 *  #c_include { <i>boolean-expression</i>} <i>name</i>
	 * <pre>
	 * Upon return, we're positioned at the first token beyond the #include directive.
	 * The tokenizer will return this vary token when the include file ends.
	 * @param conditional true if its a #c_include.
	 */
	private void doInclude(boolean conditional)
		throws org.xml.sax.SAXException, IOException {
		boolean doit = true;
		SourceRef sref = currentToken().getSourceRef();
		int line = currentToken().getLine();
		int col = currentToken().getColumn();
		nextToken();
		try {
			if (conditional) {
				String b = parseValue();
				if (!mEvaluator.evaluateBoolean(b, mEnvironment))
					// parse and evaluate boolean expression
					doit = false;
			}
			String fno = this.parseValue();
			if (doit) {
				String fn =
					mEvaluator.evaluateStringExpression(fno, mEnvironment);
				fn = fn.toLowerCase();
				InputSource input = mResolver.openFile(fn);
				Reader reader = null;
				if (input != null) {
					reader = input.getCharacterStream();
					if (reader == null && input.getByteStream() != null) {
						reader = new InputStreamReader(input.getByteStream());
					}
				}
				if (reader == null || input == null)
					throw new IOException(
						"Can't open include file \"" + fn + "\"\n    from " + computeFileTrace(sref,line));
				// System.out.println("Just opened "+ reader.getSystemId());
				mTokenizer.push(
					reader,
					new SourceRef(sref, line, input.getSystemId()));
				nextToken();
			}
		} catch (MalformedExpressionException x) {
			org.xml.sax.SAXParseException sx =
				new org.xml.sax.SAXParseException(
					x.getMessage(),
					"",
					sref.toString(),
					line,
					col);
			if (mErrorHandler != null)
				mErrorHandler.error(sx);
			else
				throw sx;
		}
	}

	/*
	 * Parse a single of property assignment.
	 * We convert expressions into Lisp.
	 * <pre>
	 *    id    = "id"
	 *    id.id = "( select id id )"
	 *    number = "number"
	 *    "string" = "string"
	 *    { action operands... } = "( action operands... )"
	 * </pre>
	 *
	 * @param list attribute list to be appended to.
	 */
	private void parseAttribute(Map<String,String> map) throws SAXException, IOException {
		Token t = currentToken();
		if (t.getKind() == Token.IDENTIFIER) {
			String name = t.getText();
			t = nextToken();
			if (t.getKind() != Token.EQUAL) {
				error(t, "'=' expected, instead of `" + t.getText() + "'");
			} else
				t = nextToken();
			String value = parseValue();
			//System.out.println(name + "=" + value);
			map.put(name, value);
		}
	}

	/**
	 * Parse a value that is to be assigned to a property name.
	 * Type of the result is one of:
	 * <dl>
	 * <dt>String
	 * <dd> an identifier, string, or integer
	 * <dt> List
	 * <dd> a list of values.
	 * <dt> Selection
	 * <dd> a selection of the form: <code>value.id</code>.
	 * </dl>
	 * @param fromLisp if true, we're called from within a lisp expression.
	 * @return the value
	 * @exception IOException some sort of error occurred on input.
	 * @exception SAXException a syntactic or lexical error occurred.
	 */
	private String parseValue(boolean fromLisp)
		throws IOException, SAXException {
		Token t = currentToken();
		// Check for "{ ... }"
		String text = t.getText();
		switch (t.getKind()) {
			case Token.LCURLY :
				{
					StringBuffer buf = new StringBuffer(50);
					if (!fromLisp)
						buf.append("$");
					parseLispExpression(buf);
					return buf.toString();
				}
			case Token.INTEGER :
				nextToken();
				return text;
			case Token.IDENTIFIER :
				{
					// a.b.c ==> (select (select a b) c)
					t = nextToken();
					while (t.getKind() == Token.DOT) {
						StringBuffer buf = new StringBuffer();
						if (!fromLisp)
							buf.append('$');
						buf.append("( select ");
						buf.append(text);
						buf.append(' ');
						t = nextToken();
						if (t.getKind() == Token.IDENTIFIER) {
							buf.append(t.getText());
							buf.append(')');
						} else
							error(
								t,
								"Identifier expected after \"" + text + ".\"");
						text = buf.toString();
						t = nextToken();
					}
					return text;
				}
			case Token.STRING :
				{
					nextToken();
					return text;
				}

			case Token.EOF :
				error(t, "premature end-of-file");
				return null;
			default :
				error(
					t,
					"Unrecognized token in this context: \""
						+ t.getText()
						+ "\"");
				return null;
		}
	}

	private String parseValue() throws IOException, SAXException {
		return parseValue(false);
	}
	/**
	 * Parse lisp expression and create string representation of it
	 */
	private void parseLispExpression(StringBuffer buf)
		throws SAXException, IOException {
		int startLine = currentToken().getLine();
		// Convert to Lisp expression.
		buf.append("(");
		nextToken();
		boolean done = false;
		while (!done) {
			Token t = currentToken();
			switch (t.getKind()) {
				case Token.RCURLY :
					buf.append(" )");
					nextToken();
					done = true;
					break;
				case Token.LCURLY :
					parseLispExpression(buf);
					break;
				case Token.EOF :
					error(
						t,
						"premature EOF. '{' not terminated at line "
							+ startLine);
					//}
					done = true;
					break;
				case Token.STRING :
					{
						String s = t.getText();
						buf.append(" \"");
						if (s.indexOf('"') >= 0) {
							for (int i = 0; i < s.length(); i++) {
								if (s.charAt(i) == '"')
									buf.append('\\');
								buf.append(s.charAt(i));
							}
						} else
							buf.append(s);
						buf.append('"');
						nextToken();
					}
					break;
				default :
					{
						buf.append(' ');
						String s = parseValue(true);
						if (s != null)
							buf.append(s);
					}
					break;
			}
		}
	}

	@Override
    public ContentHandler getContentHandler() {
		return mContentHandler;
	}
	@Override
    public org.xml.sax.DTDHandler getDTDHandler() {
		return mDTDHandler;
	}
	@Override
    public org.xml.sax.EntityResolver getEntityResolver() {
		return mEntityResolver;
	}
	@Override
    public org.xml.sax.ErrorHandler getErrorHandler() {
		return mErrorHandler;
	}
	@Override
    public boolean getFeature(String name) {
		return mFeatures.contains(name);
	}
	@Override
    public Object getProperty(String name) {
		return mProperties.get(name);
	}

	@Override
    public void setContentHandler(ContentHandler h) {
		mContentHandler = h;
	}
	@Override
    public void setDTDHandler(org.xml.sax.DTDHandler h) {
		mDTDHandler = h;
	}
	@Override
    public void setEntityResolver(org.xml.sax.EntityResolver r) {
		mEntityResolver = r;
	}
	@Override
    public void setErrorHandler(org.xml.sax.ErrorHandler e) {
		mErrorHandler = e;
	}
	@Override
    public void setFeature(String name, boolean v)
		throws SAXNotRecognizedException, SAXNotSupportedException {
		if (v)
			mFeatures.add(name);
		else
			mFeatures.remove(name);
		if (name.equals("http://xml.org/sax/features/namespaces") && v)
			throw new SAXNotSupportedException(name);
		if (name.equals("http://xml.org/sax/features/namespace-prefixes") && v)
			throw new SAXNotSupportedException(name);
		if (name.equals("http://xml.org/sax/features/string-interning") && v) {
			throw new SAXNotSupportedException(name);
		} else if (name.equals("http://xml.org/sax/features/validation") && v)
			throw new SAXNotSupportedException(name);
		else
			throw new SAXNotRecognizedException(name);
	}
	/**
	 * The only properties we recognized is the LexicalHandler, which
	 * we use to process comments.
	 */
	@Override
    public void setProperty(String name, Object v)
		throws SAXNotRecognizedException, SAXNotSupportedException {
		if (name.equals("http://xml.org/sax/handlers/LexicalHandler")
			|| name.equals("http://xml.org/sax/handlers/lexical-handler")) {
			mProperties.put(name, v);
			mLexicalHandler = (LexicalHandler) v;
			if (mTokenizer != null)
				mTokenizer.setLexicalHandler(mLexicalHandler);
		} else
			throw new SAXNotRecognizedException(name);
	}

	/**
	 * Diagnose a syntax error.
	 */
	private void error(Token t, String msg) throws SAXException {
		SAXParseException x =
			new SAXParseException(
				msg + "\n    from " + computeFileTrace(t.getSourceRef(),t.getLine()),
				"",
				t.getSourceRef().toString(),
				t.getLine(),
				t.getColumn());
		if (mErrorHandler == null)
			throw x;
		mErrorHandler.error(x);
	}

//	private boolean isEof() {
//		return currentToken().getKind() == Token.EOF;
//	}
	private Token currentToken() {
		return mCurrent;
	}
	private Token peekToken() throws SAXException, IOException {
		if (mPeek == null)
			mPeek = mTokenizer.nextToken();
		return mPeek;
	}
	private Token nextToken() throws SAXException, IOException {
		Token t;
		if (mPeek != null) {
			t = mPeek;
			mPeek = null;
		} else
			t = mTokenizer.nextToken();
		//System.out.println("Token: `" + t.getText() + "' at " + t.getSourceRef() + ", line=" + t.getLine());
		mCurrent = t;
		return t;
	}

	private Tokenizer mTokenizer;
	private IEvaluator mEvaluator;
	private IEnvironment mEnvironment;
	private IFileResolver mResolver;
	private org.xml.sax.ContentHandler mContentHandler;
	private org.xml.sax.ErrorHandler mErrorHandler;
	private HashMap<String,Object> mProperties = new HashMap<String,Object>();
	private org.xml.sax.EntityResolver mEntityResolver;
	private org.xml.sax.DTDHandler mDTDHandler;
	private HashSet<String> mFeatures = new HashSet<String>();
	private LexicalHandler mLexicalHandler;
	private Token mPeek; // lookahead by one token
	private Token mCurrent; //  current token.
}
