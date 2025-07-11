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

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Field;

import org.apache.commons.jexl3.introspection.JexlPropertyGet;

/**
 * A JexlPropertyGet for public fields.
 */
public final class FieldGetExecutor implements JexlPropertyGet {
    /**
     * Attempts to discover a FieldGetExecutor.
     *
     * @param is the introspector
     * @param clazz the class to find the get method from
     * @param identifier the key to use as an argument to the get method
     * @return the executor if found, null otherwise
     */
    public static JexlPropertyGet discover(final Introspector is, final Class<?> clazz, final String identifier) {
        if (identifier != null) {
            final Field field = is.getField(clazz, identifier);
            if (field != null) {
                return new FieldGetExecutor(field);
            }
        }
        return null;
    }

    /**
     * The public field.
     */
    private final Field field;
    /**
     * Creates a new instance of FieldPropertyGet.
     * @param theField the class public field
     */
    private FieldGetExecutor(final Field theField) {
        field = theField;
    }

    @Override
    public Object invoke(final Object obj) throws Exception {
        return field.get(obj);
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public boolean isConstant() {
        if (field.isEnumConstant()) {
            return true;
        }
        final int modifiers = field.getModifiers();
        // public static final fields are (considered) constants
        return isFinal(modifiers) && isStatic(modifiers);
    }

    @Override
    public boolean tryFailed(final Object rval) {
        return rval == Uberspect.TRY_FAILED;
    }

    @Override
    public Object tryInvoke(final Object obj, final Object key) {
        if (obj.getClass().equals(field.getDeclaringClass()) && key.equals(field.getName())) {
            try {
                return field.get(obj);
            } catch (final IllegalAccessException xill) {
                return Uberspect.TRY_FAILED;
            }
        }
        return Uberspect.TRY_FAILED;
    }

}
