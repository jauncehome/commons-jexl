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

import java.util.Objects;
import java.util.regex.Pattern;

public final class ASTRegexLiteral extends JexlNode implements JexlNode.Constant<Pattern> {

    /**
     */
    private static final long serialVersionUID = 1L;
    /** The actual literal value; the inherited 'value' member may host a cached getter. */

    private Pattern literal;

    ASTRegexLiteral(final int id) {
        super(id);
    }

    /**
     * Gets the literal value.
     * @return the Pattern literal
     */
    @Override
    public Pattern getLiteral() {
        return this.literal;
    }

    @Override
    protected boolean isConstant(final boolean literal) {
        return true;
    }

    @Override
    public Object jjtAccept(final ParserVisitor visitor, final Object data) {
        return visitor.visit(this, data);
    }

    void setLiteral(final String literal) {
        this.literal = Pattern.compile(literal);
    }

    @Override
    public String toString() {
        return Objects.toString(literal, "");
    }
}
