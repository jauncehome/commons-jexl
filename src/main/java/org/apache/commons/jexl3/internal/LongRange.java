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
package org.apache.commons.jexl3.internal;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator on a long range.
 */
final class AscLongIterator implements Iterator<Long> {
    /** The lower boundary. */
    private final long min;
    /** The upper boundary. */
    private final long max;
    /** The current value. */
    private long cursor;

    /**
     * Creates a iterator on the range.
     * @param l low boundary
     * @param h high boundary
     */
    AscLongIterator(final long l, final long h) {
        min = l;
        max = h;
        cursor = min;
    }

    @Override
    public boolean hasNext() {
        return cursor <= max;
    }

    @Override
    public Long next() {
        if (cursor <= max) {
            return cursor++;
        }
        throw new NoSuchElementException();
    }
}

/**
 * An iterator on a long range.
 */
final class DescLongIterator implements Iterator<Long> {
    /** The lower boundary. */
    private final long min;
    /** The upper boundary. */
    private final long max;
    /** The current value. */
    private long cursor;

    /**
     * Creates a iterator on the range.
     * @param l low boundary
     * @param h high boundary
     */
    DescLongIterator(final long l, final long h) {
        min = l;
        max = h;
        cursor = max;
    }

    @Override
    public boolean hasNext() {
        return cursor >= min;
    }

    @Override
    public Long next() {
        if (cursor >= min) {
            return cursor--;
        }
        throw new NoSuchElementException();
    }
}

/**
 * A range of longs.
 * <p>
 * Behaves as a readonly collection of longs.
 */
public abstract class LongRange implements Collection<Long> {
    /**
     * Ascending long range.
     */
    public static class Ascending extends LongRange {
        /**
         * Constructs a new instance.
         * @param from lower boundary
         * @param to upper boundary
         */
        protected Ascending(final long from, final long to) {
            super(from, to);
        }

        @Override
        public Iterator<Long> iterator() {
            return new AscLongIterator(min, max);
        }
    }
    /**
     * Descending long range.
     */
    public static class Descending extends LongRange {
        /**
         * Constructs a new instance.
         * @param from upper boundary
         * @param to lower boundary
         */
        protected Descending(final long from, final long to) {
            super(from, to);
        }

        @Override
        public Iterator<Long> iterator() {
            return new DescLongIterator(min, max);
        }
    }

    /**
     * Creates a range, ascending or descending depending on boundaries order.
     * @param from the lower inclusive boundary
     * @param to   the higher inclusive boundary
     * @return a range
     */
    public static LongRange create(final long from, final long to) {
        if (from <= to) {
            return new LongRange.Ascending(from, to);
        }
        return new LongRange.Descending(to, from);
    }

    /** The lower boundary. */
    protected final long min;

    /** The upper boundary. */
    protected final long max;

    /**
     * Creates a new range.
     * @param from the lower inclusive boundary
     * @param to   the higher inclusive boundary
     */
    protected LongRange(final long from, final long to) {
        min = from;
        max = to;
    }

    @Override
    public boolean add(final Long e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends Long> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final Object o) {
        if (o instanceof Number) {
            final long v = ((Number) o).longValue();
            return min <= v && v <= max;
        }
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        for (final Object cc : c) {
            if (!contains(cc)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LongRange other = (LongRange) obj;
        if (this.min != other.min) {
            return false;
        }
        if (this.max != other.max) {
            return false;
        }
        return true;
    }

    /**
     * Gets the interval maximum value.
     * @return the high boundary
     */
    public long getMax() {
        return max;
    }

    /**
     * Gets the interval minimum value.
     * @return the low boundary
     */
    public long getMin() {
        return min;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        //CSOFF: MagicNumber
        hash = 13 * hash + (int) (this.min ^ this.min >>> 32);
        hash = 13 * hash + (int) (this.max ^ this.max >>> 32);
        //CSON: MagicNumber
        return hash;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public abstract Iterator<Long> iterator();

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return (int) (max - min + 1);
    }

    @Override
    public Object[] toArray() {
        final int size = size();
        final Object[] array = new Object[size];
        for (int a = 0; a < size; ++a) {
            array[a] = min + a;
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) {
        final Class<?> ct = array.getClass().getComponentType();
        final int length = size();
        T[] copy = array;
        if (ct.isAssignableFrom(Long.class)) {
            if (array.length < length) {
                copy = (T[]) Array.newInstance(ct, length);
            }
            for (int a = 0; a < length; ++a) {
                Array.set(copy, a, min + a);
            }
            if (length < copy.length) {
                copy[length] = null;
            }
            return copy;
        }
        throw new UnsupportedOperationException();
    }
}
