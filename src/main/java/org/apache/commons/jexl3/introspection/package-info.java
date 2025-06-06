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
 * <h2>Provides high-level introspective services.</h2>
 * <p>
 * The Uberspect, JexlMethod, JexlPropertyGet and JexlPropertySet interfaces
 * form the exposed face of introspective services.
 * </p>
 * <p>
 * The Uberspectimpl is the concrete class implementing the Uberspect interface.
 * Deriving from this class is the preferred way of augmenting Jexl introspective
 * capabilities when special needs to be fulfilled or when default behaviors
 * need to be modified.
 * </p>
 */
package org.apache.commons.jexl3.introspection;
