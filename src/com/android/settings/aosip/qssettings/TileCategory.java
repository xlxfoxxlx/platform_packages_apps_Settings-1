/*
 * Copyright (C) 2015 Androis Open Source Illusion Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.aosip.qssettings;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.aosip.seekbar.SeekBarPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.Utils;

public class TileCategory extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    
    private static final String PREF_QS_TRANSPARENT_SHADE = "qs_transparent_shade";
    private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";

    private SeekBarPreference mQSShadeAlpha;
    private ListPreference mNumColumns;
    private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;

    private final Configuration mCurConfig = new Configuration();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aosip_tiles);
        PreferenceScreen prefSet = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

        // QS shade alpha
        mQSShadeAlpha =
                (SeekBarPreference) prefSet.findPreference(PREF_QS_TRANSPARENT_SHADE);
        int qSShadeAlpha = Settings.System.getInt(resolver,
                Settings.System.QS_TRANSPARENT_SHADE, 255);
        mQSShadeAlpha.setValue(qSShadeAlpha / 1);
        mQSShadeAlpha.setOnPreferenceChangeListener(this);

        // Number of columns
        mNumColumns = (ListPreference) findPreference("sysui_qs_num_columns");
        int numColumns = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_NUM_TILE_COLUMNS, getDefaultNumColums(),
                UserHandle.USER_CURRENT);
        mNumColumns.setValue(String.valueOf(numColumns));
        updateNumColumnsSummary(numColumns);
        mNumColumns.setOnPreferenceChangeListener(this);

        // Tile Animations
        mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
        int tileAnimationStyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_STYLE, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
        updateTileAnimationStyleSummary(tileAnimationStyle);
        updateAnimTileDuration(tileAnimationStyle);
        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
        int tileAnimationDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ANIM_TILE_DURATION, 2000,
                UserHandle.USER_CURRENT);
        mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
        updateTileAnimationDurationSummary(tileAnimationDuration);
        mTileAnimationDuration.setOnPreferenceChangeListener(this);
    }


    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.OWLSNEST;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        if (preference == mQSShadeAlpha) {
            int alpha = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                   Settings.System.QS_TRANSPARENT_SHADE, alpha * 1);
            return true;
        } else if (preference == mNumColumns) {
            int numColumns = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.QS_NUM_TILE_COLUMNS,
                    numColumns, UserHandle.USER_CURRENT);
            updateNumColumnsSummary(numColumns);
            return true;
        } else if (preference == mTileAnimationStyle) {
            int tileAnimationStyle = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_STYLE,
                    tileAnimationStyle, UserHandle.USER_CURRENT);
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileDuration(tileAnimationStyle);
            return true;
        } else if (preference == mTileAnimationDuration) {
            int tileAnimationDuration = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(), Settings.System.ANIM_TILE_DURATION,
                    tileAnimationDuration, UserHandle.USER_CURRENT);
            updateTileAnimationDurationSummary(tileAnimationDuration);
            return true;
         }
         return false;
    }
    private void updateNumColumnsSummary(int numColumns) {
        String prefix = (String) mNumColumns.getEntries()[mNumColumns.findIndexOfValue(String
                .valueOf(numColumns))];
        mNumColumns.setSummary(getResources().getString(R.string.qs_num_columns_showing, prefix));
    }

    private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
        String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                .valueOf(tileAnimationStyle))];
        mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
    }

    private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
        String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                .valueOf(tileAnimationDuration))];
        mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
    }

    private void updateAnimTileDuration(int tileAnimationStyle) {
        if (mTileAnimationDuration != null) {
            if (tileAnimationStyle == 0) {
                mTileAnimationDuration.setSelectable(false);
            } else {
                mTileAnimationDuration.setSelectable(true);
            }
        }
    }

    private int getDefaultNumColums() {
        try {
            Resources res = getPackageManager()
                    .getResourcesForApplication("com.android.systemui");
            int val = res.getInteger(res.getIdentifier("quick_settings_num_columns", "integer",
                    "com.android.systemui")); // better not be larger than 5, that's as high as the
                                              // list goes atm
            return Math.max(1, val);
        } catch (Exception e) {
            return 3;
        }
    }
}


