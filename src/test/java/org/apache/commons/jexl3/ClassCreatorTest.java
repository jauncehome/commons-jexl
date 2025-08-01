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
package org.apache.commons.jexl3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Basic check on automated class creation
 */
@SuppressWarnings({"UnnecessaryBoxing", "AssertEqualsBetweenInconvertibleTypes"})
class ClassCreatorTest extends JexlTestCase {
    public static class BigObject {
        @SuppressWarnings("unused")
        private final byte[] space = new byte[MEGA];
        private final int id;

        public BigObject(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
    // A weak reference on class
    static final class ClassReference extends WeakReference<Class<?>> {
        ClassReference(final Class<?> clazz, final ReferenceQueue<Object> queue) {
            super(clazz, queue);
        }
    }
    public static class ContextualCtor {
        int value = -1;

        public ContextualCtor(final JexlContext ctxt) {
            value = (Integer) ctxt.get("value");
        }

        public ContextualCtor(final JexlContext ctxt, final int v) {
            value = (Integer) ctxt.get("value") + v;
        }

        public int getValue() {
            return value;
        }
    }
    // A soft reference on instance
    static final class InstanceReference extends SoftReference<Object> {
        InstanceReference(final Object obj, final ReferenceQueue<Object> queue) {
            super(obj, queue);
        }
    }

    public static class NsTest implements JexlContext.NamespaceFunctor {
        private final String className;

        public NsTest(final String cls) {
            className = cls;
        }
        @Override
        public Object createFunctor(final JexlContext context) {
            final JexlEngine jexl = JexlEngine.getThreadEngine();
            return jexl.newInstance(className, context);
        }

    }

    public static class TwoCtors {
        int value;

        public TwoCtors(final int v) {
            this.value = v;
        }

        public TwoCtors(final Number x) {
            this.value = -x.intValue();
        }

        public int getValue() {
            return value;
        }
    }

    static final Log logger = LogFactory.getLog(JexlTestCase.class);

    static final int LOOPS = 8;

    // A space hog class
    static final int MEGA = 1024 * 1024;

    private File base;

    private JexlEngine jexl;

    public ClassCreatorTest() {
        super("ClassCreatorTest");
    }

    private void deleteDirectory(final File dir) {
        if (dir.isDirectory()) {
            for (final File file : dir.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    void functorTwo(final Object nstest) throws Exception {
        // create jexl2 with a 'test' namespace
        final Map<String, Object> ns = new HashMap<>();
        ns.put("test", nstest);
        final JexlEngine jexl2 = new JexlBuilder().namespaces(ns).create();
        final JexlContext ctxt = new MapContext();
        ctxt.set("value", 1000);

        // inject 'foo2' as test namespace functor class
        final ClassCreator cctor = new ClassCreator(jexl, base);
        cctor.setSeed(2);
        cctor.setCtorBody("value = (Integer) ctxt.get(\"value\") + 10;");
        Class<?> foo1 = cctor.createClass(true);
        assertSame(foo1.getClassLoader(), cctor.getClassLoader());
        assertEquals("foo2", foo1.getSimpleName());
        Object result = cctor.newInstance(foo1, ctxt);
        assertEquals(foo1, result.getClass());
        jexl2.setClassLoader(cctor.getClassLoader());
        cctor.clear();

        // check the namespace functor behavior
        final JexlScript script = jexl2.createScript("test:getValue()");
        result = script.execute(ctxt, foo1.getName());
        assertEquals(1010, result);

        // change the body
        cctor.setSeed(2);
        cctor.setCtorBody("value = (Integer) ctxt.get(\"value\") + 99;");
        final Class<?> foo11 = cctor.createClass(true);
        assertEquals("foo2", foo1.getSimpleName());
        assertNotSame(foo11, foo1);
        foo1 = foo11;
        result = cctor .newInstance(foo1, ctxt);
        assertEquals(foo1, result.getClass());
        // drum rolll....
        jexl2.setClassLoader(foo1.getClassLoader());
        result = script.execute(ctxt, foo1.getName());
        // tada!
        assertEquals(1099, result);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        base = new File(SystemProperties.getJavaIoTmpdir(), "jexl" + System.currentTimeMillis());
        jexl = JEXL;

    }

    @AfterEach
    @Override
    public void tearDown() {
        deleteDirectory(base);
    }

    @Test
    void testBasicCtor() {
        final JexlScript s = jexl.createScript("(c, v)->{ var ct2 = new(c, v); ct2.value; }");
        Object r = s.execute(null, TwoCtors.class, 10);
        assertEquals(10, r);
        r = s.execute(null, TwoCtors.class, 5 + 5);
        assertEquals(10, r);
        r = s.execute(null, TwoCtors.class, 10d);
        assertEquals(-10, r);
        r = s.execute(null, TwoCtors.class, 100f);
        assertEquals(-100, r);
    }

    @Test
    void testContextualCtor() {
        final MapContext ctxt = new MapContext();
        ctxt.set("value", 42);
        JexlScript s = jexl.createScript("(c)->{ new(c).value }");
        Object r = s.execute(ctxt, ContextualCtor.class);
        assertEquals(42, r);
        s = jexl.createScript("(c, v)->{ new(c, v).value }");
        r = s.execute(ctxt, ContextualCtor.class, 100);
        assertEquals(142, r);
    }

    @Test
    void testFunctor2Class() throws Exception {
        functorTwo(new NsTest(ClassCreator.GEN_CLASS + "foo2"));
    }

    @Test
    void testFunctor2Name() throws Exception {
        functorTwo(ClassCreator.GEN_CLASS + "foo2");
    }

    @Test
    void testFunctorOne() throws Exception {
        final JexlContext ctxt = new MapContext();
        ctxt.set("value", 1000);

        // create a class foo1 with a ctor whose body gets a value
        // from the context to initialize its value
        final ClassCreator cctor = new ClassCreator(jexl, base);
        cctor.setSeed(1);
        cctor.setCtorBody("value = (Integer) ctxt.get(\"value\") + 10;");
        Class<?> foo1 = cctor.createClass(true);
        assertSame(foo1.getClassLoader(), cctor.getClassLoader());
        assertEquals("foo1", foo1.getSimpleName());
        Object result = cctor.newInstance(foo1, ctxt);
        assertEquals(foo1, result.getClass());
        jexl.setClassLoader(cctor.getClassLoader());
        cctor.clear();

        // check we can invoke that ctor using its name or class
        final JexlScript script = jexl.createScript("(c)->{ new(c).value; }");
        result = script.execute(ctxt, foo1);
        assertEquals(1010, result);
        result = script.execute(ctxt, foo1.getName());
        assertEquals(1010, result);

        // re-create foo1 with a different body!
        cctor.setSeed(1);
        cctor.setCtorBody("value = (Integer) ctxt.get(\"value\") + 99;");
        final Class<?> foo11 = cctor.createClass(true);
        assertEquals("foo1", foo1.getSimpleName());
        assertNotSame(foo11, foo1);
        foo1 = foo11;
        result = cctor.newInstance(foo1, ctxt);
        assertEquals(foo1, result.getClass());
        // drum rolll....
        jexl.setClassLoader(foo1.getClassLoader());
        result = script.execute(ctxt, foo1.getName());
        // tada!
        assertEquals(1099, result);
        result = script.execute(ctxt, foo1);
        assertEquals(1099, result);
    }

    @Test
    void testFunctorThree() throws Exception {
        final JexlContext ctxt = new MapContext();
        ctxt.set("value", 1000);

        final ClassCreator cctor = new ClassCreator(jexl, base);
        cctor.setSeed(2);
        cctor.setCtorBody("value = (Integer) ctxt.get(\"value\") + 10;");
        Class<?> foo1 = cctor.createClass(true);
        assertSame(foo1.getClassLoader(), cctor.getClassLoader());
        assertEquals("foo2", foo1.getSimpleName());
        Object result = cctor.newInstance(foo1, ctxt);
        assertEquals(foo1, result.getClass());
        jexl.setClassLoader(cctor.getClassLoader());
        cctor.clear();

        final Map<String, Object> ns = new HashMap<>();
        ns.put("test", foo1);
        final JexlEngine jexl2 = new JexlBuilder().namespaces(ns).create();

        final JexlScript script = jexl2.createScript("test:getValue()");
        result = script.execute(ctxt, foo1.getName());
        assertEquals(1010, result);

        cctor.setSeed(2);
        cctor.setCtorBody("value = (Integer) ctxt.get(\"value\") + 99;");
        final Class<?> foo11 = cctor.createClass(true);
        assertEquals("foo2", foo1.getSimpleName());
        assertNotSame(foo11, foo1);
        foo1 = foo11;
        result = cctor.newInstance(foo1, ctxt);
        assertEquals(foo1, result.getClass());
        // drum rolll....
        jexl2.setClassLoader(foo1.getClassLoader());
        result = script.execute(ctxt, foo1.getName());
        // tada!
        assertEquals(1099, result);
    }

    @Test
    void testMany() throws Exception {
        // abort test if class creator cannot run
        if (!ClassCreator.canRun) {
            return;
        }
        int pass = 0;
        int gced = -1;
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final List<Reference<?>> stuff = new ArrayList<>();
        // keeping a reference on methods prevent classes from being GCed
//        List<Object> mm = new ArrayList<Object>();
        final JexlExpression expr = jexl.createExpression("foo.value");
        final JexlExpression newx = jexl.createExpression("foo = new(clazz)");
        final JexlEvalContext context = new JexlEvalContext();
        final JexlOptions options = context.getEngineOptions();
        options.setStrict(false);
        options.setSilent(true);

        final ClassCreator cctor = new ClassCreator(jexl, base);
        for (int i = 0; i < LOOPS && gced < 0; ++i) {
            cctor.setSeed(i);
            Class<?> clazz;
            if (pass == 0) {
                clazz = cctor.createClass();
            } else {
                clazz = cctor.getClassInstance();
                if (clazz == null) {
                    assertEquals(i, gced);
                    break;
                }
            }
            // this code verifies the assumption that holding a strong reference to a method prevents
            // its owning class from being GCed
//          Method m = clazz.getDeclaredMethod("getValue", new Class<?>[0]);
//          mm.add(m);
            // we should not be able to create foox since it is unknown to the JEXL classloader
            context.set("clazz", cctor.getClassName());
            context.set("foo", null);
            Object z = newx.evaluate(context);
            assertNull(z);
            // check with the class itself
            context.set("clazz", clazz);
            z = newx.evaluate(context);
            assertNotNull(z, clazz + ": class " + i + " could not be instantiated on pass " + pass);
            assertEquals(Integer.valueOf(i), expr.evaluate(context));
            // with the proper class loader, attempt to create an instance from the class name
            jexl.setClassLoader(cctor.getClassLoader());
            z = newx.evaluate(context);
            assertEquals(z.getClass(), clazz);
            assertEquals(Integer.valueOf(i), expr.evaluate(context));
            cctor.clear();
            jexl.setClassLoader(null);

            // on pass 0, attempt to force GC to run and collect generated classes
            if (pass == 0) {
                // add a weak reference on the class
                stuff.add(new ClassReference(clazz, queue));
                // add a soft reference on an instance
                stuff.add(new InstanceReference(clazz.getConstructor().newInstance(), queue));

                // attempt to force GC:
                // while we still have a MB free, create & store big objects
                for (int b = 0; b < 1024 && Runtime.getRuntime().freeMemory() > MEGA; ++b) {
                    final BigObject big = new BigObject(b);
                    stuff.add(new InstanceReference(big, queue));
                }
                // hint it...
                System.gc();
                // let's see if some weak refs got collected
                boolean qr = false;
                while (queue.poll() != null) {
                    final Reference<?> ref = queue.remove(1);
                    if (ref instanceof ClassReference) {
                        gced = i;
                        qr = true;
                    }
                }
                if (qr) {
                    //logger.warn("may have GCed class around " + i);
                    pass = 1;
                    i = 0;
                }
            }
        }

        if (gced < 0) {
            logger.warn("unable to force GC");
            //assertTrue(gced > 0);
        }
    }

    @Test
    void testOne() throws Exception {
        // abort test if class creator cannot run
        if (!ClassCreator.canRun) {
            logger.warn("unable to create classes");
            return;
        }
        final ClassCreator cctor = new ClassCreator(jexl, base);
        cctor.setSeed(1);
        final Class<?> foo1 = cctor.createClass();
        assertEquals("foo1", foo1.getSimpleName());
        cctor.clear();
    }
    @Test
    void test432() throws Exception {
        final ClassCreator cctor = new ClassCreator(jexl, base);
        cctor.setSeed(2);
        cctor.setCtorBody("value = (Integer) ctxt.get(\"value\") + 10;");
        Class<?> foo1 = cctor.createClass(true);
        assertSame(foo1.getClassLoader(), cctor.getClassLoader());
        assertEquals("foo2", foo1.getSimpleName());
        final Map<String, Object> ns = new HashMap<>();
        ns.put("test", foo1.getName());
        // use cache
        final JexlEngine jexl2 = new JexlBuilder().namespaces(ns).cache(16).create();
        jexl2.setClassLoader(cctor.getClassLoader());
        cctor.clear();
        final JexlContext ctxt = new MapContext();
        ctxt.set("value", 1000);
        final JexlScript script = jexl2.createScript("test:getValue()");
        Object result = script.execute(ctxt);
        assertEquals(1010, result);        cctor.setSeed(2);
        cctor.setCtorBody("value = (Integer) ctxt.get(\"value\") + 99;");
        final Class<?> foo11 = cctor.createClass(true);
        assertEquals("foo2", foo1.getSimpleName());
        assertNotSame(foo11, foo1);
        foo1 = foo11;
        // drum rolll....
        jexl2.setClassLoader(foo1.getClassLoader());
        result = script.execute(ctxt);
        // tada!
        assertEquals(1099, result);
    }
}
