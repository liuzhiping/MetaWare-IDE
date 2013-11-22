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

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import org.xml.sax.ext.LexicalHandler;

/**
 * Guihili tokenizer.
 * 
 * @author David Pickens
 * @version April 24, 2002
 */
class Tokenizer {

    private static final int EOF = -1;

    private static final int INCLUDE_POP = -2; // when we pop an include

    /**
     * Create a tokenizer.
     * <P>
     * 
     * @param in
     *            the stream from which we're reading.
     * @param streamName
     *            the asasociated source reference object (for error
     *            diagnostics)
     */
    Tokenizer(Reader in, SourceRef streamName, org.xml.sax.ErrorHandler handler) {
        mErrorHandler = handler;
        mMainSref = streamName;
        push(in, streamName, false);
    }

    /**
     * The SAX lexical handler is used to process comments.
     */
    void setLexicalHandler(LexicalHandler handler) {
        mLexicalHandler = handler;
    }

    /**
     * Read the next token and return it.
     */
    Token nextToken() throws IOException, org.xml.sax.SAXException {
        while (true) {
            int c = peek();
            switch (c) {
            case '(':
                return singleChar(Token.LPAREN, "("); /* )) */
            /* (( */case ')':
                return singleChar(Token.RPAREN, ")");
            case '<':
                return singleChar(Token.LESSTHAN, "<"); /* > */
            case '>':
                return singleChar(Token.GREATERTHAN, ")");
            case '.':
                return singleChar(Token.DOT, ".");
            case '{':
                return singleChar(Token.LCURLY, "{"); /* }} */
            /* {{ */case '}':
                return singleChar(Token.RCURLY, "}");
            case '=':
                return singleChar(Token.EQUAL, "=");
            case '#': {
                // #include name
                // #include "string"
                // #c_include { boolean-expression } name
                // #c_include { boolean-expression } "string"
                int line = mLine;
                int col = mColumn;
                SourceRef sref = mSref;
                StringBuffer text = new StringBuffer(20);
                text.append('#');
                c = read();
                while (isAlnum(c)) {
                    text.append((char) c);
                    c = read();
                }
                String name = text.toString();
                if (name.equals("#include")) {
                    mCurrent = Token.make(Token.INCLUDE, name, sref, line, col);
                } else if (name.equals("#c_include")) {
                    mCurrent = Token.make(Token.C_INCLUDE, name, sref, line,
                            col);
                } else {
                    mCurrent = Token.make(Token.IDENTIFIER, name, sref, line,
                            col);
                }
                return mCurrent;
            }

            case '\n':
            case ' ':
            case '\t':
            case '\r':
                read();
                break; // skip white space

            case '"': {
                StringBuffer text = new StringBuffer(100);
                int col = mColumn;
                int line = mLine;
                SourceRef sref = mSref;
                while (true) {
                    c = read();
                    if (c == EOF || c == '\n') {
                        error("String not properly terminated");
                        break;
                    }
                    if (c == '\\') {
                        c = read();
                        if (c == EOF) {
                            error("String not properly terminated");
                            break;
                        }
                    } else if (c == '"') {
                        read();
                        break;
                    }
                    text.append((char) c);
                }
                mCurrent = Token.make(Token.STRING, text.toString(), sref,
                        line, col);
                return mCurrent;
            }
            case '/': {// check for comment: "//"
                int line = mLine;
                int col = mColumn;
                SourceRef sref = mSref;
                c = read();
                if (c == '/') {
                    StringBuffer buf = new StringBuffer(100);
                    do {
                        c = read();
                        if (c != '\n') buf.append((char) c);
                    } while (c != EOF && c != '\n');
                    if (mLexicalHandler != null)
                            mLexicalHandler.comment(buf.toString()
                                    .toCharArray(), 0, buf.length());
                } else {
                    mCurrent = Token.make(Token.SLASH, "/", sref, line, col);
                    return mCurrent;
                }
                break;
            }
            case INCLUDE_POP: // Just popped include, "mCurrent" set to stashed
                              // token
            //System.out.println("TOKENIZER: include_pop returning " +
            // (mCurrent != null?mCurrent.getText():"<null>"));
                mPeek = mPeekToRestore;
                mCurrent = mTokenToRestore;
                if (mCurrent == null) nextToken();
                mTokenToRestore = null;
                return mCurrent;
            case EOF:
                mCurrent = Token.make(Token.EOF, "", mSref, mLine, -1);
                return mCurrent;
            default: {
                int line = mLine;
                int col = mColumn;
                SourceRef sref = mSref;
                if (isAlpha(c)) {
                    StringBuffer text = new StringBuffer(20);
                    while (isAlnum(c)) {
                        text.append((char) c);
                        read();
                        c = peek();
                    }
                    mCurrent = Token.make(Token.IDENTIFIER, text.toString(),
                            sref, line, col);
                    return mCurrent;
                }
                if (isDigit(c)) {
                    StringBuffer text = new StringBuffer(20);
                    while (isDigit(c)) {
                        text.append((char) c);
                        read();
                        c = peek();
                    }
                    mCurrent = Token.make(Token.INTEGER, text.toString(), sref,
                            line, col);
                    return mCurrent;
                }
                error("Unrecognizable character: " + (char) c);
            }
            }
        }
    }

    private boolean isAlpha(int c) {
        return Character.isJavaIdentifierStart((char) c);
    }

    private boolean isAlnum(int c) {
        return Character.isJavaIdentifierPart((char) c);
    }

    private boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    private Token singleChar(int kind, String text) throws IOException {
        Token t = mCurrent = Token.make(kind, text, mSref, mLine, mColumn);
        read();
        return t;
    }

    Token getCurrentToken() {
        return mCurrent;
    }

    /**
     * Push a new input source, such as when an #include directive is
     * encountered.
     */
    private void push(Reader in, SourceRef sref, boolean isInclude) {
        //System.out.println("TOKENIZER: push " + sref);
        mSref = sref;
        mInputStack.push(new Input(in, sref, mCurrent, mPeek, isInclude));
        mPeek = 0;
    }

    /**
     * Push a new input source, such as when an #include directive is
     * encountered.
     */
    public void push(Reader in, SourceRef sref) {
        push(in, sref, true);
    }

    /**
     * Read a character.
     * 
     * @return the character or EOF at end-of-file.
     */
    private int read() throws IOException {
        if (mInputStack.empty()) return EOF;
        Input in = mInputStack.peek();
        mLine = in.getLine();
        mColumn = in.getColumn();
        int c = in.read();
        if (c < 0) {
            //System.out.println("TOKENIZER: popping " + mSref);
            in.close();
            mInputStack.pop();
            if (in.isInclude()) {
                mTokenToRestore = in.getTokenToRestore();
                mPeekToRestore = in.getSavedPeek();
                // We may have include at the end of main source file.
                mSref = mInputStack.isEmpty() ? mMainSref
                        : mInputStack.peek().getSourceRef();
                //System.out.println("TOKENIZER: restoring " + mSref + ",
                // peek=" + (char)mPeekToRestore + "; token=" +
                // mCurrent.getText());
                c = INCLUDE_POP;
            } else {
                c = EOF;
            }
        }
        mPeek = c;
        return c;
    }

    private int peek() throws IOException {
        if (mPeek == 0) read();
        return mPeek;
    }

    private void error(String msg) throws org.xml.sax.SAXException {
        org.xml.sax.SAXParseException x = new org.xml.sax.SAXParseException(
                msg, "", mSref.toString(), mLine, mColumn);
        if (mErrorHandler == null) throw x;
        mErrorHandler.error(x);
    }

    private int mPeek = 0;

    private int mPeekToRestore = 0; // after an include is popped
    private Token mTokenToRestore = null;
    private int mLine = 0;

    private int mColumn = 0;

    private Stack<Input> mInputStack = new Stack<Input>();

    private Token mCurrent;

    private SourceRef mSref; // current stream being read from

    private SourceRef mMainSref; // Main source file

    private org.xml.sax.ErrorHandler mErrorHandler;

    private LexicalHandler mLexicalHandler;
}
/**
 * A wrapper for a Reader that maintains the name of the stream and the current
 * line number.
 */

class Input {

    /**
     * @param in
     *            the input stream for include file.
     * @param streamName
     *            the name of the stream.
     * @param current
     *            token to be restored when the include pops.
     * @param peek
     *            peek character to restored when the include pops
     * @param isInclude
     *            if true, then this is an include file.
     */
    Input(Reader in, SourceRef streamName, Token current, int peek,
            boolean isInclude) {
        mLine = 1;
        mColumn = 0;
        mReader = in;
        mInclude = isInclude;
        mSavedPeek = peek;
        mStreamName = streamName;
        // We must make a copy because the Token objects are reused!
        if (current != null) mTokenToRestore = new Token(current);
    }

    final int read() throws IOException {
        int c = mReader.read();
        mColumn++;
        if (c == '\n') {
            mLine++;
            mColumn = 0;
        }
        return c;
    }

    final void close() throws IOException {
        mReader.close();
    }

    final int getLine() {
        return mLine;
    }

    final int getColumn() {
        return mColumn;
    }

    final Token getTokenToRestore() {
        return mTokenToRestore;
    }

    final SourceRef getSourceRef() {
        return mStreamName;
    }

    final int getSavedPeek() {
        return mSavedPeek;
    }

    final boolean isInclude() {
        return mInclude;
    }

    private int mLine;

    private int mColumn;

    private boolean mInclude;

    private int mSavedPeek;

    private Reader mReader;

    private SourceRef mStreamName;

    private Token mTokenToRestore;
}
