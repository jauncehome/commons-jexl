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

/**
 * Using JEXL expressions in JUnit assertions.
 * <h2><a id="intro">Introduction</a></h2>
 * <p>
 * This package only contains one class, Asserter, which

import static org.junit.jupiter.api.Assertions.*;
 * allows you to use a JEXL expression in a JUnit assertion.
 * The following example demonstrates the use of the Asserter
 * class.  An instance is created, and the internal JexlContext
 * is populated via calls to setVariable().  Calls to
 * assertExpression() succeed if the expression evaluates to
 * the value of the second parameter, otherwise an
 * AssertionFailedException is thrown.
 * </p>
 *
 * <pre>
 * Asserter asserter = new Asserter();
 * asserter.setVariable("foo", new Foo());
 * asserter.setVariable("person", "James");
 * asserter.assertExpression("person", "James");
 * asserter.assertExpression("size(person)", Integer.valueOf(5));
 *
 * asserter.assertExpression("foo.getCount()", Integer.valueOf(5));
 * asserter.assertExpression("foo.count", Integer.valueOf(5));
 * </pre>
 */
package org.apache.commons.jexl3.junit;
