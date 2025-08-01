/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jexl3;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests for malformed expressions and scripts. ({@link org.apache.commons.jexl3.JexlEngine#createExpression(String)} and
 * {@link org.apache.commons.jexl3.JexlEngine#createScript(String)} should throw {@link org.apache.commons.jexl3.JexlException}s).
 */
class ParseFailuresTest extends JexlTestCase {

    static final Log LOGGER = LogFactory.getLog(ParseFailuresTest.class.getName());

    /**
     * Create the test.
     */
    public ParseFailuresTest() {
        super("ParseFailuresTest");
    }

    @Test
    void testMalformedExpression1() throws Exception {
        // this will throw a JexlException
        final String badExpression = "eq";
        final JexlException pe = assertThrows(JexlException.class, () -> JEXL.createExpression(badExpression),
                () -> "Parsing \"" + badExpression + "\" should result in a JexlException");
        LOGGER.debug(pe);
    }

    @Test
    void testMalformedExpression2() throws Exception {
        // this will throw a TokenMgrErr, which we rethrow as a JexlException
        final String badExpression = "?";
        final JexlException pe = assertThrows(JexlException.class, () -> JEXL.createExpression(badExpression),
                () -> "Parsing \"" + badExpression + "\" should result in a JexlException");
        LOGGER.debug(pe);
    }

    @Test
    void testMalformedScript1() throws Exception {
        // this will throw a TokenMgrErr, which we rethrow as a JexlException
        final String badScript = "eq";
        final JexlException pe = assertThrows(JexlException.class, () -> JEXL.createExpression(badScript),
                () -> "Parsing \"" + badScript + "\" should result in a JexlException");
        LOGGER.debug(pe);
    }

    @Test
    void testMalformedScript2() throws Exception {
        // this will throw a TokenMgrErr, which we rethrow as a JexlException
        final String badScript = "?";
        final JexlException pe = assertThrows(JexlException.class, () -> JEXL.createExpression(badScript),
                () -> "Parsing \"" + badScript + "\" should result in a JexlException");
        LOGGER.debug(pe);
    }

    @Test
    void testMalformedScript3() throws Exception {
        // this will throw a TokenMgrErr, which we rethrow as a JexlException
        final String badScript = "foo=1;bar=2;a?b:c:d;";
        final JexlException pe = assertThrows(JexlException.class, () -> JEXL.createExpression(badScript),
                () -> "Parsing \"" + badScript + "\" should result in a JexlException");
        LOGGER.debug(pe);
    }

}
