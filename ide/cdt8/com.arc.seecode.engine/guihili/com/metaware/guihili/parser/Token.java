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

/**
 * A token from a guihili stream. The <code>{@link #getKind kind}</code>
 * property identifies the token.
 * 
 * @author David Pickens
 * @version April 24, 2002
 */
class Token {
    static final int EOF = 0;

    static final int IDENTIFIER = 1;

    static final int INTEGER = 2;

    static final int STRING = 3;

    static final int LCURLY = 4; // {

    static final int RCURLY = 5; // }

    static final int LESSTHAN = 6; // <

    static final int GREATERTHAN = 7; // >

    static final int DOT = 8; // .

    static final int LPAREN = 9; // (

    static final int RPAREN = 10; // )

    static final int DELIM = 11; // arbitrary delimitor (e.g., + - *)

    static final int EQUAL = 12; // =

    static final int SLASH = 13; // /

    static final int INCLUDE = 14; // #include

    static final int C_INCLUDE = 15; // #c_include

    private Token() {
    }

    Token(Token t) {
        set(t.getKind(), t.getText(), t.getSourceRef(), t.getLine(), t
                .getColumn());
    }

    @Override
    public String toString() {
        return getText();
    }

    /**
     * Create a token.
     * 
     * We recycle token space to prevent gc from getting swampped by the
     * tokenizer.
     * <P>
     * 
     * @param kind
     *            the id of the token.
     * @param text
     *            the text of the token.
     * @param sref
     *            the stream where the token came from.
     * @param line
     *            the line number it the stream (starting at 1)
     * @param column
     *            the character position in the line (starting at 1)
     */
    static Token make(int kind, String text, SourceRef sref, int line,
            int column) {
        if (sLastToken >= sToken.length)
            sLastToken = 0;
        Token t = sToken[sLastToken++];
        t.set(kind, text, sref, line, column);
        return t;
    }

    private void set(int kind, String text, SourceRef sref, int line, int column) {
        mKind = kind;
        mText = text;
        mSref = sref;
        mLine = line;
        mColumn = column;
    }

    private static Token[] sToken; // cache of tokens

    private static int sLastToken;
    static {
        sToken = new Token[10];
        for (int i = 0; i < sToken.length; i++)
            sToken[i] = new Token();
    }

    int getKind() {
        return mKind;
    }

    String getText() {
        return mText;
    }

    SourceRef getSourceRef() {
        return mSref;
    }

    int getLine() {
        return mLine;
    }

    int getColumn() {
        return mColumn;
    }

    private int mKind;

    private int mLine;

    private int mColumn;

    private String mText;

    private SourceRef mSref;
}
