/*
 * Copyright (c) 2006, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.tdk.signaturetest.core;

import com.sun.tdk.signaturetest.model.ClassDescription;

import java.util.*;

/**
 * @author Roman Makarchuk
 */
public class ClassSet {

    private final static class Counter {

        int count;

        Counter(int c) {
            count = c;
        }

        void increment() {
            ++count;
        }

        void decrement() {
            --count;
        }

        int intValue() {
            return count;
        }
    }

    private final ClassHierarchy hierarchy;
    private final boolean trackDependeces;
    private final Map<String, Object> classNamesInProcess = new HashMap<>();

    public ClassSet(ClassHierarchy hierarchy, boolean trackDependeces) {
        this.hierarchy = hierarchy;
        this.trackDependeces = trackDependeces;
    }

    public void addClass(String fqname) {
        addClass(fqname, true);
    }

    private void addClass(String fqname, boolean externalCall) {

        if (classNamesInProcess.get(fqname) != null) {
            return;
        }

        classNamesInProcess.put(fqname, null);

        try {
            Counter counter = classes.get(fqname);
            try {

                ClassDescription cl = hierarchy.load(fqname);
                // acrobatic feat : ClassCorrector replaces invisible class
                // with nearest visible subclass
                // Note: this is a temporary solution!
                if (!hierarchy.isAccessible(cl) && !externalCall) {
                    if (!cl.isInterface()) {
                        addClass(cl.getSuperClass().getQualifiedName(), false);
                    }
                }

                if (counter == null) {
                    classes.put(fqname, new Counter(0));

                    if (trackDependeces) {
                        Set<String> dep = cl.getDependences();
                        for (String o : dep) {
                            addClass(o, false);
                        }
                    }

                } else {
                    counter.increment();
                }

            } catch (ClassNotFoundException e) {
                missingClasses.add(fqname);
            }

        } finally {
            classNamesInProcess.remove(fqname);
        }
    }

    public void removeClass(String fqname) {

        if (classNamesInProcess.get(fqname) != null) {
            return;
        }

        classNamesInProcess.put(fqname, null);

        try {

            Counter counter = classes.get(fqname);
            if (counter != null) {

                int c = counter.intValue();
                if (c == 0) {
                    classes.remove(fqname);

                    try {
                        ClassDescription cl = hierarchy.load(fqname);

                        Set<String> dep = cl.getDependences();
                        for (String o : dep) {
                            removeClass(o);
                        }

                    } catch (ClassNotFoundException e) {
                        assert missingClasses.contains(fqname);
                    }

                } else {
                    assert c > 0;
                    counter.decrement();
                }

            } else {
                assert true;
            }
        } finally {
            classNamesInProcess.remove(fqname);
        }

    }

    public Set<String> getClasses() {
        return Collections.unmodifiableSet(classes.keySet());
    }

    public Set<String> getMissingClasses() {
        return Collections.unmodifiableSet(missingClasses);
    }

    private final Map<String, Counter> classes = new HashMap<>();
    private final Set<String> missingClasses = new HashSet<>();
}
