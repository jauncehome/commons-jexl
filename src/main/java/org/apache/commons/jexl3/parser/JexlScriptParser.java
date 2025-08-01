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

import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlFeatures;
import org.apache.commons.jexl3.JexlInfo;
import org.apache.commons.jexl3.internal.Scope;

/**
 * The interface that produces a JEXL script AST from a source.
 * @since 3.5.0
 */
public interface JexlScriptParser {
  /**
   * Parses a script or expression.
   *
   * @param info      information structure
   * @param features  the set of parsing features
   * @param src      the expression to parse
   * @param scope     the script frame
   * @return the parsed tree
   * @throws JexlException if any error occurred during parsing
   */
  ASTJexlScript parse(final JexlInfo info, final JexlFeatures features, final String src, final Scope scope);

  /**
   * Parses an embedded JXLT script or expression, an interpolation expression.
   *
   * @param info      information structure
   * @param features  the set of parsing features
   * @param src      the expression to parse
   * @param scope     the script frame
   * @return the parsed tree
   * @throws JexlException if any error occurred during parsing
   */
  default ASTJexlScript jxltParse(JexlInfo info, JexlFeatures features, String src, Scope scope) {
    return parse(info, features, src, scope);
  }

}
