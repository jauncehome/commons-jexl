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

public final class ASTNumberLiteral extends JexlNode implements JexlNode.Constant<Number> {
    /**
     */
    private static final long serialVersionUID = 1L;
    /** The number parser. */
    private final NumberParser nlp;

    ASTNumberLiteral(final int id) {
        super(id);
        nlp = new NumberParser();
    }

    @Override
    public Number getLiteral() {
        return nlp.getLiteralValue();
    }

    public Class<? extends Number> getLiteralClass() {
        return nlp.getLiteralClass();
    }

    @Override
    protected boolean isConstant(final boolean literal) {
        return true;
    }

    public boolean isInteger() {
        return nlp.isInteger();
    }

    @Override
    public Object jjtAccept(final ParserVisitor visitor, final Object data) {
        return visitor.visit(this, data);
    }

    /**
     * Sets this node as a natural literal.
     * Originally from OGNL.
     * @param s the natural as string
     */
    void setNatural(final String s) {
        nlp.assignNatural(s);
    }

    /**
     * Sets this node as a real literal.
     * Originally from OGNL.
     * @param s the real as string
     */
    void setReal(final String s) {
        nlp.assignReal(s);
    }

    @Override
    public String toString() {
        return nlp.toString();
    }
}
