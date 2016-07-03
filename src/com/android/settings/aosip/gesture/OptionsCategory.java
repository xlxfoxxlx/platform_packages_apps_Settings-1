/*
 * Copyright (C) 2015 Android Open Source Illusion Project
 * Copyright (C) 2013 The ChameleonOS Project
 * Copyright (C) 2016 The Dirty Unicorns project
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

package com.android.settings.aosip.gesture;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.view.Gravity;

import com.android.settings.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.utils.du.ActionHandler;
import com.android.internal.utils.du.Config;
import com.android.internal.utils.du.Config.ActionConfig;
import com.android.internal.utils.du.Config.ButtonConfig;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.solo.ActionPreference;
import com.android.settings.solo.ActionFragment;

public class OptionsCategory extends ActionFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Gestures";

    private static final String THREE_FINGER_GESTURE = "three_finger_gesture_action";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control"; 

    private ActionPreference mThreeFingerSwipeGestures;
    private SwitchPreference mEnabledPref;
    private SwitchPreference mStatusBarBrightnessControl; 
    private ListPreference mPositionPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.aosip_options);

        PreferenceScreen prefSet = getPreferenceScreen();

        mThreeFingerSwipeGestures = (ActionPreference) findPreference(THREE_FINGER_GESTURE);
        mThreeFingerSwipeGestures.setTag(THREE_FINGER_GESTURE);
        mThreeFingerSwipeGestures.setActionConfig(getSwipeThreeFingerGestures());
        mThreeFingerSwipeGestures.setDefaultActionConfig(new ActionConfig(getActivity()));


        final ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBrightnessControl = (SwitchPreference) findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);
        int statusBarBrightnessControl = Settings.System.getInt(getContentResolver(),
                STATUS_BAR_BRIGHTNESS_CONTROL, 0);
        mStatusBarBrightnessControl.setChecked(statusBarBrightnessControl != 0);
        try {
            if (Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setEnabled(false);
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_brightness_control_info);
            }
        } catch (SettingNotFoundException e) {
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
          if (preference == mStatusBarBrightnessControl) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(), STATUS_BAR_BRIGHTNESS_CONTROL,
                    value ? 1 : 0);
            return true;
           }
        return false;
    }

    @Override
    protected void findAndUpdatePreference(ActionConfig action, String tag) {
        if (TextUtils.equals(THREE_FINGER_GESTURE, tag)) {
            ActionConfig newAction;
            if (action == null) {
                newAction = mThreeFingerSwipeGestures.getDefaultActionConfig();
            } else {
                newAction = action;
            }
            mThreeFingerSwipeGestures.setActionConfig(newAction);
            setSwipeThreeFingerGestures(newAction);
        } else {
            super.findAndUpdatePreference(action, tag);
        }
    }

    private ActionConfig getSwipeThreeFingerGestures() {
        ButtonConfig config = ButtonConfig.getButton(mContext,
                Settings.Secure.THREE_FINGER_GESTURE, true);
        ActionConfig action;
        if (config == null) {
            action = new ActionConfig(getActivity());
        } else {
            action = config.getActionConfig(ActionConfig.PRIMARY);
        }
        return action;
    }

    private void setSwipeThreeFingerGestures(ActionConfig action) {
        ButtonConfig config = new ButtonConfig(getActivity());
        config.setActionConfig(action, ActionConfig.PRIMARY);
        ButtonConfig.setButton(getActivity(), config, Settings.Secure.THREE_FINGER_GESTURE, true);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.OWLSNEST;
    }
}
