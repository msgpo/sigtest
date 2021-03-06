/*
 * Copyright (c) 2007, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tdk.signaturetest.sigfile;

import java.util.*;

public class FeaturesHolder {

    private Set<Feature> supportedFeatures;
    public static final Feature ConstInfo = new Feature("#NoConstInfo");
    public static final Feature TigerInfo = new Feature("#NoTigerFeaturesInfo");
    public static final Feature MergeModeSupported = new Feature("#MergeModeSupported");
    public static final Feature BuildMembers = new Feature("#BuildMembers");
    public static final Feature NonStaticConstants = new Feature("#NonStaticConstants");
    public static final Feature ListOfHiders = new Feature("#ListOfHiders");
    public static final Feature XHiders = new Feature("#XHiders");
    public static final Feature OuterClasses = new Feature("#OuterClasses");
    public static final Feature CopyRight = new Feature("#CopyRight");
    public static final Feature ModuleInfo = new Feature("#ModuleInfo");
    public static final Feature AnnDevVal = new Feature("#AnnoDefVals");

    public void addSupportedFeature(Feature feature) {
        ensureInitialized();
        supportedFeatures.add(feature);
    }

    public Set<Feature> getSupportedFeatures() {
        ensureInitialized();
        return supportedFeatures;
    }

    private void ensureInitialized() {
        if (supportedFeatures == null) {
            supportedFeatures = new HashSet<>();
        }
    }

    protected void removeSupportedFeature(Feature feature) {
        ensureInitialized();
        supportedFeatures.remove(feature);
    }

    public boolean isFeatureSupported(Feature feature) {
        ensureInitialized();
        return supportedFeatures.contains(feature);
    }

    protected boolean isInitialized() {
        return supportedFeatures != null;
    }

    public void setFeatures(Set<Feature> features) {
        supportedFeatures = new HashSet<>(features);
    }

    public void retainFeatures(Set<Feature> features) {
        supportedFeatures.retainAll(features);
    }

    public static class Feature {

        private String pragma;

        public void setText(String newText) {
            pragma = newText;
        }

        private Feature(String pragma) {
            this.pragma = pragma;
        }

        public String toString() {
            return pragma;
        }

        boolean match(String line) {
            return pragma.equals(line);
        }
    }
}
