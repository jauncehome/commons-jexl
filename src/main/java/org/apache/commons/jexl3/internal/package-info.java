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
 * <h2>Provides utilities for introspection services.</h2>
 * <p>
 * This internal package is not intended for public usage and there is <strong>no</strong>
 * guarantee that its public classes or methods will remain as is in subsequent
 * versions.
 * </p>
 * <p>
 * This set of classes implement the various forms of setters and getters
 * used by JEXL. These are specialized forms for 'pure' properties, discovering
 * methods of the {s,g}etProperty form, for Maps, Lists and Ducks -
 * attempting to discover a 'get' or 'set' method, making an object walk and
 * quack.
 * </p>
 */
package org.apache.commons.jexl3.internal;
