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
 * Provides low-level introspective services.
 * <p>
 * This internal package is not intended for public usage and there is <strong>no</strong>
 * guarantee that its public classes or methods will remain as is in subsequent
 * versions.
 * </p>
 * <p>
 * The IntrospectorBase, ClassMap, MethodKey, MethodMap form the
 * base of the introspection service. They allow describing classes and their
 * methods, keeping them in a cache (@see IntrospectorBase) to speed up property
 * getters/setters and method discovery used during expression evaluation.
 * </p>
 * <p>
 * The cache materialized in Introspector creates one entry per class containing a map of all
 * accessible public methods keyed by name and signature.
 * </p>
 */
package org.apache.commons.jexl3.internal.introspection;
