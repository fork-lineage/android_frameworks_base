/*
 * Copyright (C) 2020 The Pixel Experience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.internal.util.custom;

import android.app.Application;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PixelPropsUtils {

    public static final String PACKAGE_GMS = "com.google.android.gms";
    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final String DEVICE = "ro.product.device";

    private static final Map<String, Object> propsToChangePixel6;
    private static final String[] packagesToChangePixel6 = {
            "com.google.android.apps.customization.pixel",
            "com.google.android.apps.nexuslauncher",
            "com.google.android.apps.subscriptions.red",
            "com.google.android.apps.wallpaper",
            "com.google.android.apps.wallpaper.pixel",
            "com.google.android.settings.intelligence",
            "com.google.pixel.dynamicwallpapers",
            "com.google.pixel.livewallpaper",
            PACKAGE_GMS
    };

    private static final Map<String, Object> propsToChangePixel5;

    private static final Map<String, Object> propsToChangePixelXL;
    private static final String[] packagesToChangePixelXL = {
            "com.google.android.apps.photos"
    };

    private static final Map<String, ArrayList<String>> propsToKeep;
    private static final String[] extraPackagesToChange = {
        "com.android.chrome",
        "com.breel.wallpapers20",
        "com.netflix.mediaclient"
    };

    private static final String[] packagesToKeep = {
        "com.google.android.GoogleCamera",
        "com.google.android.GoogleCamera.Cameight",
        "com.google.android.GoogleCamera.Go",
        "com.google.android.GoogleCamera.Urnyx",
        "com.google.android.GoogleCameraAsp",
        "com.google.android.GoogleCameraCVM",
        "com.google.android.GoogleCameraEng",
        "com.google.android.GoogleCameraEng2",
        "com.google.android.MTCL83",
        "com.google.android.UltraCVM",
        "com.google.android.apps.cameralite",
        "com.google.ar.core",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.kids",
        "com.google.android.apps.youtube.music"
    };

    // Codenames for currently supported Pixels by Google
    private static final String[] pixelCodenames = {
        "oriole",
        "raven",
        "redfin",
        "barbet",
        "bramble",
        "sunfish",
        "coral",
        "flame",
        "bonito",
        "sargo",
        "crosshatch",
        "blueline"
    };

    private static volatile boolean sIsGms = false;

    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put("com.google.android.settings.intelligence", new ArrayList<>(Collections.singletonList("FINGERPRINT")));
        propsToChangePixel6 = new HashMap<>();
        propsToChangePixel6.put("BRAND", "google");
        propsToChangePixel6.put("MANUFACTURER", "Google");
        propsToChangePixel6.put("DEVICE", "raven");
        propsToChangePixel6.put("PRODUCT", "raven");
        propsToChangePixel6.put("MODEL", "Pixel 6 Pro");
        propsToChangePixel6.put("FINGERPRINT", "google/redfin/redfin:13/TP1A.221105.002/9080065:user/release-keys");
        propsToChangePixel5 = new HashMap<>();
        propsToChangePixel5.put("BRAND", "google");
        propsToChangePixel5.put("MANUFACTURER", "Google");
        propsToChangePixel5.put("DEVICE", "redfin");
        propsToChangePixel5.put("PRODUCT", "redfin");
        propsToChangePixel5.put("MODEL", "Pixel 5");
        propsToChangePixel5.put("FINGERPRINT", "google/redfin/redfin:13/TP1A.221105.002/9080065:user/release-keys");
        propsToChangePixelXL = new HashMap<>();
        propsToChangePixelXL.put("BRAND", "google");
        propsToChangePixelXL.put("MANUFACTURER", "Google");
        propsToChangePixelXL.put("DEVICE", "marlin");
        propsToChangePixelXL.put("PRODUCT", "marlin");
        propsToChangePixelXL.put("MODEL", "Pixel XL");
        propsToChangePixelXL.put("FINGERPRINT", "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");
    }

    public static void setProps(Application app) {
        final String packageName = app.getPackageName();
        final String processName = app.getProcessName();
        if (packageName == null){
            return;
        }
        if (Arrays.asList(pixelCodenames).contains(SystemProperties.get(DEVICE).replace("lineage_", ""))) return;
        if (packageName.equals(PACKAGE_GMS) &&
                processName.equals(PACKAGE_GMS + ".unstable")) {
            sIsGms = true;
            setPropValue("TYPE", "userdebug");
        }
        if ((packageName.startsWith("com.google.") && !Arrays.asList(packagesToKeep).contains(packageName))
               || Arrays.asList(extraPackagesToChange).contains(packageName)) {
            Map<String, Object> propsToChange = propsToChangePixel5;

            if (Arrays.asList(packagesToChangePixel6).contains(packageName)) {
                propsToChange = propsToChangePixel6;
            }

            if (Arrays.asList(packagesToChangePixelXL).contains(packageName)) {
                propsToChange = propsToChangePixelXL;
            }

            if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)) {
                    if (DEBUG) Log.d(TAG, "Not defining " + key + " prop for: " + packageName);
                    continue;
                }
                if (DEBUG) Log.d(TAG, "Defining " + key + " prop for: " + packageName);
                setPropValue(key, value);
            }
        }
        // Set proper indexing fingerprint
        if (packageName.equals("com.google.android.settings.intelligence")) {
            setPropValue("FINGERPRINT", Build.VERSION.INCREMENTAL);
        }
    }

    private static void setPropValue(String key, Object value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static boolean isCallerSafetyNet() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        // Check stack for SafetyNet
        if (sIsGms && isCallerSafetyNet()) {
            throw new UnsupportedOperationException();
        }
    }
}
