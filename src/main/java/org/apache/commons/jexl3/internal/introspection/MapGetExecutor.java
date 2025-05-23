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

package org.apache.commons.jexl3.internal.introspection;

import java.util.Map;

/**
 * Specialized executor to get a property from a Map.
 * @since 2.0
 */
public final class MapGetExecutor extends AbstractExecutor.Get {
    /** The java.util.map.get method used as an active marker in MapGet. */
    private static final java.lang.reflect.Method MAP_GET =
            initMarker(Map.class, "get", Object.class);
    /**
     * Attempts to discover a MapGetExecutor.
     *
     * @param is the introspector
     * @param clazz the class to find the get method from
     * @param identifier the key to use as an argument to the get method
     * @return the executor if found, null otherwise
     */
    public static MapGetExecutor discover(final Introspector is, final Class<?> clazz, final Object identifier) {
        // we still need to ensure permissions grant access to get(...)
        if (Map.class.isAssignableFrom(clazz) && is.getMethod(clazz, "get", identifier) != null) {
            return new MapGetExecutor(clazz, MAP_GET, identifier);
        }
        return null;
    }

    /** The property. */
    private final Object property;

    /**
     * Creates an instance.
     * @param clazz he class the get method applies to
     * @param method the method held by this executor
     * @param key the property to get
     */
    private MapGetExecutor(final Class<?> clazz, final java.lang.reflect.Method method, final Object key) {
        super(clazz, method);
        property = key;
    }

    @Override
    public Object getTargetProperty() {
        return property;
    }

    @Override
    public Object invoke(final Object obj) {
        @SuppressWarnings("unchecked") // ctor only allows Map instances - see discover() method
        final Map<Object, ?> map = (Map<Object, ?>) obj;
        return map.get(property);
    }

    @Override
    public Object tryInvoke(final Object obj, final Object key) {
        if (obj != null
            && method != null
            && objectClass.equals(obj.getClass())
            && (property == null && key == null
                || property != null && key != null && property.getClass().equals(key.getClass()))) {
            @SuppressWarnings("unchecked") // ctor only allows Map instances - see discover() method
            final Map<Object, ?> map = (Map<Object, ?>) obj;
            return map.get(key);
        }
        return TRY_FAILED;
    }
}
