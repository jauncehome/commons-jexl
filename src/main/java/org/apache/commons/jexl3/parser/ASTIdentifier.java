/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jexl3.parser;

/**
 * Identifiers, variables, ie symbols.
 */
public class ASTIdentifier extends JexlNode {
    /**
     */
    private static final long serialVersionUID = 1L;
    /** The redefined variable flag. */
    private static final int REDEFINED = 0;
    /** The shaded variable flag. */
    private static final int SHADED = 1;
    /** The captured variable flag. */
    private static final int CAPTURED = 2;
    /** The lexical variable flag. */
    private static final int LEXICAL = 3;
    /** The const variable flag. */
    private static final int CONST = 4;
    /**
     * Checks the value of a flag in the mask.
     * @param ordinal the flag ordinal
     * @param mask the flags mask
     * @return the mask value with this flag or-ed in
     */
    private static boolean isSet(final int ordinal, final int mask) {
        return (mask & 1 << ordinal) != 0;
    }
    /**
     * Sets the value of a flag in a mask.
     * @param ordinal the flag ordinal
     * @param mask the flags mask
     * @param value true or false
     * @return the new flags mask value
     */
    private static int set(final int ordinal, final int mask, final boolean value) {
        return value? mask | 1 << ordinal : mask & ~(1 << ordinal);
    }
    protected String name;

    protected int symbol = -1;

    protected int flags;

    ASTIdentifier(final int id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return null;
    }

    public int getSymbol() {
        return symbol;
    }

    public boolean isCaptured() {
        return isSet(CAPTURED, flags);
    }

    @Override
    public boolean isConstant() {
        return isSet(CONST, flags);
    }

    public boolean isLexical() {
        return isSet(LEXICAL, flags);
    }

    public boolean isRedefined() {
        return isSet(REDEFINED, flags);
    }

    public boolean isShaded() {
        return isSet(SHADED, flags);
    }

    @Override
    public Object jjtAccept(final ParserVisitor visitor, final Object data) {
        return visitor.visit(this, data);
    }

    public void setCaptured(final boolean f) {
        flags = set(CAPTURED, flags, f);
    }

    public void setConstant(final boolean f) {
        flags = set(CONST, flags, f);
    }

    public void setLexical(final boolean f) {
        flags = set(LEXICAL, flags, f);
    }

    public void setRedefined(final boolean f) {
        flags = set(REDEFINED, flags, f);
    }

    public void setShaded(final boolean f) {
        flags = set(SHADED, flags, f);
    }

    void setSymbol(final int r, final String identifier) {
        symbol = r;
        name = identifier;
    }

    void setSymbol(final String identifier) {
        if (identifier.charAt(0) == '#') {
            symbol = Integer.parseInt(identifier.substring(1));
        }
        name = identifier;
    }

    @Override
    public String toString() {
        return name;
    }
}
