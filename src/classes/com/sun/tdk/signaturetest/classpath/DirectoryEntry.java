/*
 * Copyright (c) 1999, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tdk.signaturetest.classpath;

import com.sun.tdk.signaturetest.core.AppContext;
import com.sun.tdk.signaturetest.core.context.BaseOptions;
import com.sun.tdk.signaturetest.core.context.Option;
import com.sun.tdk.signaturetest.model.ExoticCharTools;
import com.sun.tdk.signaturetest.util.I18NResourceBundle;
import com.sun.tdk.signaturetest.util.SwissKnife;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;

/**
 * @author Maxim Sokolnikov
 */
class DirectoryEntry extends ClasspathEntry {

    private static final I18NResourceBundle i18n = I18NResourceBundle.getBundleForClass(DirectoryEntry.class);

    /**
     * The qualified name of {@code this} directory.
     */
    private String directoryName = "";

    public DirectoryEntry(ClasspathEntry previous, String name) throws IOException {
        super(previous);
        init(name);
    }

    public DirectoryEntry(ClasspathEntry previous) throws IOException {
        super(previous);
    }

    public void init(String directoryName) throws IOException {
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            throw new IOException(i18n.getString("DirectoryEntry.error.invdir", directoryName));
        }

        this.directoryName = directoryName;
        classes = new LinkedHashSet<>();
        scanDirectory(directory, "");
        currentPosition = classes.iterator();
    }

    @Override
    public void close() {
        // nothing to do
    }

    /**
     * Find all classes placed in the given {@code directory} including
     * those placed in subdirectories. (Recursively walk subdirectories tree, if
     * needed.) Qualified names for all class files found inside the given
     * {@code directory} are collected in {@code this}
     * <b>DirectoryEntry</b> instance.
     *
     * @param directory   Directory to scan.
     * @param packageName Package name for classes inside the given
     *                    {@code directory}.
     */
    private void scanDirectory(File directory, String packageName) {
        // check for infinite loop which could occurs in the case of
        // cyclic symbolic link.

        int pos, startPos = 0, depth = 0;
        do {
            pos = packageName.indexOf('.', startPos);
            if (pos != -1) {
                depth++;
                startPos = pos + 1;
            }
            if (depth > 512) {
                return;
            }
        } while (pos != -1);

        // -----------------------------------------------------------------
        BaseOptions bo = AppContext.getContext().getBean(BaseOptions.class);
        try {
            String[] files = directory.list();
            if (files == null) {
                return;
            }

            StringBuffer buf = new StringBuffer();

            for (String file : files) {
                File current = new File(directory, file);
                String namePrefix = packageName.isEmpty() ? "" : (packageName + ".");
                if (current.isDirectory()) {
                    scanDirectory(current, namePrefix + file);
                } else if (file.endsWith(JAVA_CLASSFILE_EXTENSION)) {

                    buf.setLength(0);
                    buf.append(namePrefix);
                    buf.append(file, 0, file.length() - JAVA_CLASSFILE_EXTENSION_LEN);

                    String className = buf.toString();
                    if (!contains(className)) {
                        classes.add(className.intern());
                    }
                }
            }
        } catch (SecurityException e) {
            if (bo.isSet(Option.DEBUG)) {
                SwissKnife.reportThrowable(e);
            }
        }
    }

    /**
     * Returns <b>FileInputStream</b> instance providing bytecode for the
     * required class, if the class could be found by the given qualified name
     * in {@code this} <b>DirectoryEntry</b> instance.
     *
     * @param name Qualified name of the class required.
     * @throws ClassNotFoundException The named class is not found in
     *                                {@code this} <b>DirectoryEntry</b>.
     * @see java.io.FileInputStream
     */
    public InputStream findClass(String name) throws IOException, ClassNotFoundException {
        if (!classes.contains(name)) {
            throw new ClassNotFoundException(name);
        }

        name = ExoticCharTools.decodeExotic(name);
        return SwissKnife.approveFileInputStream(constructFileName(name));
    }

    /**
     * Replace dots in the given qualified class {@code name} with
     * appropriate files separator symbol.
     *
     * @see java.io.File#separator
     */
    private String constructFileName(String name) {

        // construct name of the class file

        return directoryName +
                File.separator +
                name.replace('.', File.separatorChar) +
                JAVA_CLASSFILE_EXTENSION;
    }
}
