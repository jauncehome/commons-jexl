/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jexl3.scripting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JexlScriptEngineTest {
    public static class Errors {
        public int illegal() {
            throw new IllegalArgumentException("jexl");
        }
        public int npe() {
            throw new NullPointerException("jexl");
        }
    }
    private static final List<String> NAMES = Arrays.asList("JEXL", "Jexl", "jexl",
                                                            "JEXL2", "Jexl2", "jexl2",
                                                            "JEXL3", "Jexl3", "jexl3");
    private static final List<String> EXTENSIONS = Arrays.asList("jexl", "jexl2", "jexl3");

    private static final List<String> MIMES = Arrays.asList("application/x-jexl",
                                                            "application/x-jexl2",
                                                            "application/x-jexl3");

    private static final String LF = System.lineSeparator();

    @AfterEach
    void tearDown() {
        JexlBuilder.setDefaultPermissions(null);
        JexlScriptEngine.setInstance(null);
        JexlScriptEngine.setPermissions(null);
    }

    @Test
    void testCompile() throws Exception {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final JexlScriptEngine engine = (JexlScriptEngine) manager.getEngineByName("JEXL");
        final ScriptContext ctxt = engine.getContext();
        final String str = null;
        final Reader reader = null;
        assertThrows(NullPointerException.class, () -> engine.compile(str));
        assertThrows(NullPointerException.class, () -> engine.compile(reader));
        final CompiledScript script0 = engine.compile(new StringReader("3 + 4"));
        assertEquals(engine, script0.getEngine());
        Object result = script0.eval();
        assertEquals(7, result);
        result = script0.eval();
        assertEquals(7, result);
        result = engine.eval(new StringReader("38 + 4"));
        assertEquals(42, result);
        result = engine.eval("38 + 4");
        assertEquals(42, result);
        // next test
        final CompiledScript script1 = engine.compile("3 + 4");
        assertEquals(engine, script1.getEngine());
        Object result1 = script1.eval();
        assertEquals(7, result1);
        result1 = script1.eval();
        assertEquals(7, result1);
        // next test
        ctxt.setAttribute("x", 20, ScriptContext.ENGINE_SCOPE);
        ctxt.setAttribute("y", 22, ScriptContext.ENGINE_SCOPE);
        final CompiledScript script2 = engine.compile("x + y");
        Object result2 = script2.eval();
        assertEquals(42, result2);
        ctxt.setAttribute("x", -20, ScriptContext.ENGINE_SCOPE);
        ctxt.setAttribute("y", -22, ScriptContext.ENGINE_SCOPE);
        result2 = script2.eval();
        assertEquals(-42, result2);
    }

    @Test
    void testDirectNew() throws Exception {
        final ScriptEngine engine = new JexlScriptEngine();
        final Integer initialValue = 123;
        assertEquals(initialValue,engine.eval("123"));
    }

    @Test
    void testDottedNames() throws Exception {
        final ScriptEngineManager manager = new ScriptEngineManager();
        assertNotNull(manager, "Manager should not be null");
        final ScriptEngine engine = manager.getEngineByName("JEXL");
        assertNotNull(engine, "Engine should not be null (JEXL)");
        engine.eval("this.is.a.test=null");
        assertNull(engine.get("this.is.a.test"));
        assertEquals(Boolean.TRUE, engine.eval("empty(this.is.a.test)"));
        final Object mymap = engine.eval("testmap={ 'key1' : 'value1', 'key2' : 'value2' }");
        assertInstanceOf(Map.class, mymap);
        assertEquals(2,((Map<?, ?>)mymap).size());
    }

    @Test
    void testErrors() throws Exception {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final JexlScriptEngine engine = (JexlScriptEngine) manager.getEngineByName("JEXL");
        engine.put("errors", new Errors());
        assertInstanceOf(NullPointerException.class, assertThrows(ScriptException.class, () -> engine.eval("errors.npe()")).getCause());
        assertInstanceOf(IllegalArgumentException.class, assertThrows(ScriptException.class, () -> engine.eval("errors.illegal()")).getCause());
        final CompiledScript script0 = engine.compile("errors.npe()");
        assertInstanceOf(NullPointerException.class, assertThrows(ScriptException.class, script0::eval).getCause());
        final CompiledScript script1 = engine.compile("errors.illegal()");
        assertInstanceOf(IllegalArgumentException.class, assertThrows(ScriptException.class, script1::eval).getCause());
    }

    @Test
    void testNulls() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        assertNotNull(manager, "Manager should not be null");
        final ScriptEngine engine = manager.getEngineByName("jexl3");
        assertNotNull(engine, "Engine should not be null (name)");
        assertNotNull(engine.getFactory());
        assertThrows(NullPointerException.class, () -> engine.eval((String) null));
        assertThrows(NullPointerException.class, () -> engine.eval((Reader) null));
        final ScriptContext ctxt = null;
        assertThrows(NullPointerException.class, () -> engine.eval((String) null, ctxt));
        assertThrows(NullPointerException.class, () -> engine.eval((Reader) null, ctxt));
    }

    @Test
    void testScopes() throws Exception {
        final ScriptEngineManager manager = new ScriptEngineManager();
        assertNotNull(manager, "Manager should not be null");
        final ScriptEngine engine = manager.getEngineByName("jexl3");
        assertNotNull(engine, "Engine should not be null (name)");
        manager.put("global", 1);
        engine.put("local", 10);
        manager.put("both", 7);
        engine.put("both", 7);
        engine.eval("local=local+1");
        engine.eval("global=global+1");
        engine.eval("both=both+1"); // should update engine value only
        engine.eval("newvar=42;");
        assertEquals(2,manager.get("global"));
        assertEquals(11,engine.get("local"));
        assertEquals(7,manager.get("both"));
        assertEquals(8,engine.get("both"));
        assertEquals(42,engine.get("newvar"));
        assertNull(manager.get("newvar"));
    }

    @Test
    void testScriptEngineFactory() {
        final JexlScriptEngineFactory factory = new JexlScriptEngineFactory();
        assertEquals("JEXL Engine", factory.getParameter(ScriptEngine.ENGINE));
        assertEquals("3.4", factory.getParameter(ScriptEngine.ENGINE_VERSION));
        assertEquals("JEXL", factory.getParameter(ScriptEngine.LANGUAGE));
        assertEquals("3.4", factory.getParameter(ScriptEngine.LANGUAGE_VERSION));
        assertNull(factory.getParameter("THREADING"));
        assertEquals(NAMES, factory.getParameter(ScriptEngine.NAME));
        assertEquals(EXTENSIONS, factory.getExtensions());
        assertEquals(MIMES, factory.getMimeTypes());

        assertEquals("42;", factory.getProgram("42"));
        assertEquals("str.substring(3,4)", factory.getMethodCallSyntax("str", "substring", "3", "4"));
    }

    @Test
    void testScripting() throws Exception {
        final ScriptEngineManager manager = new ScriptEngineManager();
        assertNotNull(manager, "Manager should not be null");
        final ScriptEngine engine = manager.getEngineByName("jexl3");
        final Integer initialValue = 123;
        assertEquals(initialValue, engine.eval("123"));
        assertEquals(initialValue, engine.eval("0;123")); // multiple statements
        final ScriptException xscript = assertThrows(ScriptException.class,
                () -> engine.eval("sys=context.class.forName(\"java.lang.System\");now=sys.currentTimeMillis();"));
        final JexlException.Method xjexl = (JexlException.Method) xscript.getCause();
        assertEquals("forName", xjexl.getMethod());
        engine.put("value", initialValue);
        assertEquals(initialValue, engine.get("value"));
        final Integer newValue = 124;
        assertEquals(newValue, engine.eval("old=value;value=value+1"));
        assertEquals(initialValue, engine.get("old"));
        assertEquals(newValue, engine.get("value"));
        assertEquals(engine.getContext(), engine.get(JexlScriptEngine.CONTEXT_KEY));
        // Check behavior of JEXL object
        assertEquals(engine.getContext().getReader(), engine.eval("JEXL.in"));
        assertEquals(engine.getContext().getWriter(), engine.eval("JEXL.out"));
        assertEquals(engine.getContext().getErrorWriter(), engine.eval("JEXL.err"));
        assertEquals(System.class, engine.eval("JEXL.System"));
    }

    @Test
    void testScriptingGetBy() {
        final ScriptEngineManager manager = new ScriptEngineManager();
        assertNotNull(manager, "Manager should not be null");
        for (final String name : NAMES) {
            final ScriptEngine engine = manager.getEngineByName(name);
            assertNotNull(engine, "Engine should not be null (name)");
        }
        for (final String extension : EXTENSIONS) {
            final ScriptEngine engine = manager.getEngineByExtension(extension);
            assertNotNull(engine, "Engine should not be null (extension)");
        }
        for (final String mime : MIMES) {
            final ScriptEngine engine = manager.getEngineByMimeType(mime);
            assertNotNull(engine, "Engine should not be null (mime)");
        }
    }
    @Test
    void testScriptingInstance0() throws Exception {
        JexlScriptEngine.setPermissions(JexlPermissions.UNRESTRICTED);
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("jexl3");
        final Long time2 = (Long) engine.eval(
                "sys=context.class.forName(\"java.lang.System\");"
                        + "now=sys.currentTimeMillis();");
        assertTrue(time2 <= System.currentTimeMillis());
    }

    @Test
    void testScriptingPermissions1() throws Exception {
        JexlBuilder.setDefaultPermissions(JexlPermissions.UNRESTRICTED);
        JexlScriptEngine.setPermissions(null);
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("jexl3");
        final Long time2 = (Long) engine.eval(
                "sys=context.class.forName(\"java.lang.System\");"
                        + "now=sys.currentTimeMillis();");
        assertTrue(time2 <= System.currentTimeMillis());
    }

    @Test
    void testMain0() throws Exception {
        final StringWriter strw = new StringWriter();
        final StringReader strr = new StringReader("a=20\nb=22\na+b\n//q!\n");
        Main.run(new BufferedReader(strr), new PrintWriter(strw), null);
        final String ctl = "> >> 20" + LF +
                "> >> 22" + LF +
                "> >> 42" + LF +
                "> ";
        Assertions.assertEquals(ctl, strw.toString());
    }

    @Test
    void testMain1() throws Exception {
        final StringWriter strw = new StringWriter();
        final StringReader strr = new StringReader("args[0]+args[1]");
        Main.run(new BufferedReader(strr), new PrintWriter(strw), new Object[]{20, 22});
        final String ctl = ">>: 42" + LF;
        Assertions.assertEquals(ctl, strw.toString());
    }

    @Test
    void testMain2() throws Exception {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final PrintStream originalOut = System.out;
        Path file = null;
        try {
            System.setOut(new PrintStream(outContent));
            file = Files.createTempFile("test-jsr233", ".jexl");
            final BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()));
            writer.write("a=20;\nb=22;\na+b\n");
            writer.close();
            final String ctl = ">>: 42" + LF;
            Main.main(new String[]{file.toString()});
            Assertions.assertEquals(ctl, outContent.toString());
        } finally {
            System.setOut(originalOut);
            if (file != null) {
                Files.delete(file);
            }
        }
    }

}
