/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jexl3.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A script scope, stores the declaration of parameters and local variables as symbols.
 * <p>This also acts as the functional scope and variable definition store.</p>
 * @since 3.0
 */
public final class Scope {
    /**
     * The value of an as-yet  undeclared but variable, for instance: x; before var x;.
     */
    static final Object UNDECLARED = new Object() {
        @Override public String toString() {
            return "??";
        }
    };
    /**
     * The value of a declared but undefined variable, for instance: var x;.
     */
    static final Object UNDEFINED = new Object() {
        @Override public String toString() {
            return "?";
        }
    };
    /**
     * The empty string array.
     */
    private static final String[] EMPTY_STRS = {};
    /**
     * The parent scope.
     */
    private final Scope parent;
    /**
     * The number of parameters.
     */
    private int parms;
    /**
     * The number of local variables.
     */
    private int vars;
    /**
     * The map of named variables aka script parameters and local variables.
     * Each parameter is associated to a symbol and is materialized as an offset in the stacked array used
     * during evaluation.
     */
    private Map<String, Integer> namedVariables;
    /**
     * The map of local captured variables to parent scope variables, ie closure.
     */
    private Map<Integer, Integer> capturedVariables;
    /**
     * Let symbols.
     */
    private LexicalScope lexicalVariables;

    /**
     * Creates a new scope with a list of parameters.
     * @param scope the parent scope if any
     * @param parameters the list of parameters
     */
    public Scope(final Scope scope, final String... parameters) {
        if (parameters != null) {
            parms = parameters.length;
            namedVariables = new LinkedHashMap<>();
            for (int p = 0; p < parms; ++p) {
                namedVariables.put(parameters[p], p);
            }
        } else {
            parms = 0;
        }
        vars = 0;
        parent = scope;
    }

    /**
     * Marks a symbol as a lexical, declared through let or const.
     * @param s the symbol
     * @return true if the symbol was not already present in the lexical set
     */
    public boolean addLexical(final int s) {
        if (lexicalVariables == null) {
            lexicalVariables = new LexicalScope();
        }
        return lexicalVariables.addSymbol(s);
    }

    /**
     * Creates a frame by copying values up to the number of parameters.
     * <p>This captures the captured variables values.</p>
     * @param frame the caller frame
     * @param args the arguments
     * @return the arguments array
     */
    public Frame createFrame(final boolean ref, final Frame frame, final Object...args) {
        if (namedVariables == null) {
            return null;
        }
        final Object[] arguments = new Object[namedVariables.size()];
        Arrays.fill(arguments, UNDECLARED);
        final Frame newFrame;
        if (frame != null && capturedVariables != null && parent != null) {
            for (final Map.Entry<Integer, Integer> capture : capturedVariables.entrySet()) {
                final Integer target = capture.getKey();
                final Integer source = capture.getValue();
                final Object arg = frame.capture(source);
                arguments[target] = arg;
            }
            newFrame = frame.newFrame(this, arguments, 0);
        } else {
            newFrame = ref
            ? new ReferenceFrame(this, arguments, 0)
            : new Frame(this, arguments, 0);
        }
        return newFrame.assign(args);
    }

    /**
     * Declares a parameter.
     * <p>
     * This method creates an new entry in the symbol map.
     * </p>
     * @param param the parameter name
     * @return the register index storing this variable
     */
    public int declareParameter(final String param) {
        if (namedVariables == null) {
            namedVariables = new LinkedHashMap<>();
        } else if (vars > 0) {
            throw new IllegalStateException("cant declare parameters after variables");
        }
        return namedVariables.computeIfAbsent(param, name -> {
            final int register = namedVariables.size();
            parms += 1;
            return register;
        });
    }

    /**
     * Declares a local variable.
     * <p>
     * This method creates an new entry in the symbol map.
     * </p>
     * @param varName the variable name
     * @return the register index storing this variable
     */
    public int declareVariable(final String varName) {
        if (namedVariables == null) {
            namedVariables = new LinkedHashMap<>();
        }
        return namedVariables.computeIfAbsent(varName, name -> {
           final int register = namedVariables.size();
            vars += 1;
            // check if local is redefining captured
            if (parent != null) {
                final Integer pr = parent.getSymbol(name, true);
                if (pr != null) {
                    if (capturedVariables == null) {
                        capturedVariables = new LinkedHashMap<>();
                    }
                    capturedVariables.put(register, pr);
                }
            }
            return register;
        });
    }

    /**
     * Gets the (maximum) number of arguments this script expects.
     * @return the number of parameters
     */
    public int getArgCount() {
        return parms;
    }

    /**
     * Gets the captured index of a given symbol, ie the target index of a symbol in a child scope.
     * @param symbol the symbol index
     * @return the target symbol index or null if the symbol is not captured
     */
    public Integer getCaptured(final int symbol) {
        if (capturedVariables != null) {
            for (final Map.Entry<Integer, Integer> capture : capturedVariables.entrySet()) {
                final Integer source = capture.getValue();
                if (source == symbol) {
                    return capture.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Gets the index of a captured symbol, ie the source index of a symbol in a parent scope.
     * @param symbol the symbol index
     * @return the source symbol index or -1 if the symbol is not captured
     */
    public int getCaptureDeclaration(final int symbol) {
        final Integer declared = capturedVariables != null ? capturedVariables.get(symbol)  : null;
        return declared != null ? declared : -1;
    }

    /**
     * Gets this script captured symbols names, i.e. local variables defined in outer scopes and used
     * by this scope.
     * @return the captured names
     */
    public String[] getCapturedVariables() {
        if (capturedVariables != null) {
            final List<String> captured = new ArrayList<>(vars);
            for (final Map.Entry<String, Integer> entry : namedVariables.entrySet()) {
                final int symnum = entry.getValue();
                if (symnum >= parms && capturedVariables.containsKey(symnum)) {
                    captured.add(entry.getKey());
                }
            }
            if (!captured.isEmpty()) {
                return captured.toArray(new String[0]);
            }
        }
        return EMPTY_STRS;
    }

    /**
     * Gets this script local variable, i.e. symbols assigned to local variables excluding captured variables.
     * @return the local variable names
     */
    public String[] getLocalVariables() {
        if (namedVariables == null || vars <= 0) {
            return EMPTY_STRS;
        }
        final List<String> locals = new ArrayList<>(vars);
        for (final Map.Entry<String, Integer> entry : namedVariables.entrySet()) {
            final int symnum = entry.getValue();
            if (symnum >= parms && (capturedVariables == null || !capturedVariables.containsKey(symnum))) {
                locals.add(entry.getKey());
            }
        }
        return locals.toArray(new String[0]);
    }

    /**
     * Gets this script parameters, i.e. symbols assigned before creating local variables.
     * @return the parameter names
     */
    public String[] getParameters() {
        return getParameters(0);
    }

    /**
     * Gets this script parameters.
     * @param bound number of known bound parameters (curry)
     * @return the parameter names
     */
     String[] getParameters(final int bound) {
        final int unbound = parms - bound;
        if (namedVariables == null || unbound <= 0) {
            return EMPTY_STRS;
        }
        final String[] pa = new String[unbound];
        int p = 0;
        for (final Map.Entry<String, Integer> entry : namedVariables.entrySet()) {
            final int argn = entry.getValue();
            if (argn >= bound && argn < parms) {
                pa[p++] = entry.getKey();
            }
        }
        return pa;
    }

    Scope getParent() {
        return parent;
    }

    /**
     * Checks whether an identifier is a local variable or argument, ie a symbol.
     * If this fails, look in parents for symbol that can be captured.
     * @param name the symbol name
     * @return the symbol index
     */
    public Integer getSymbol(final String name) {
        return getSymbol(name, true);
    }

    /**
     * Checks whether an identifier is a local variable or argument, ie a symbol.
     * @param name the symbol name
     * @param capture whether solving by capturing a parent symbol is allowed
     * @return the symbol index
     */
    private Integer getSymbol(final String name, final boolean capture) {
        Integer register = namedVariables != null ? namedVariables.get(name) : null;
        if (register == null && capture && parent != null) {
            final Integer pr = parent.getSymbol(name, true);
            if (pr != null) {
                if (capturedVariables == null) {
                    capturedVariables = new LinkedHashMap<>();
                }
                if (namedVariables == null) {
                    namedVariables = new LinkedHashMap<>();
                }
                register = namedVariables.size();
                namedVariables.put(name, register);
                capturedVariables.put(register, pr);
            }
        }
        return register;
    }

    /**
     * Gets this script symbols names, i.e. parameters and local variables.
     * @return the symbol names
     */
    public String[] getSymbols() {
        return namedVariables != null ? namedVariables.keySet().toArray(new String[0]) : EMPTY_STRS;
    }

    /**
     * Checks whether a given symbol is captured.
     * @param symbol the symbol number
     * @return true if captured, false otherwise
     */
    public boolean isCapturedSymbol(final int symbol) {
        return capturedVariables != null && capturedVariables.containsKey(symbol);
    }

    /**
     * Checks whether a symbol is declared through a let or const.
     * @param s the symbol
     * @return true if symbol was declared through let or const
     */
    public boolean isLexical(final int s) {
        return lexicalVariables != null && s >= 0 && lexicalVariables.hasSymbol(s);
    }
}
