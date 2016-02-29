/*
 * Copyright (C) 2015 Android Open Source Illusion Project
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

package com.android.settings.aosip.statusbar;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;

import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;


public class BatteryCategory extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String BATTERY_ICON = "battery_icon_color";
    private static final String BATTERY_TEXT = "battery_text_color";

    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;
 
    private static final int WHITE                  = 0xffffffff;
    private static final int HOLO_BLUE_LIGHT        = 0xff33b5e5;
    private static final int RED_500                = 0xfff44336;
    private static final int BLACK_TRANSLUCENT      = 0x99000000;
    private static final int RED_900_TRANSLUCENT    = 0x99b71c1c;
 
    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET  = 0;

    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarBatteryShowPercent;
    private ColorPickerPreference mBatteryIcon;
    private ColorPickerPreference mBatteryText;

    private ContentResolver mResolver;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.OWLSNEST;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.aosip_battery);

        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryShowPercent =
                (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        int batteryStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int batteryShowPercent = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        enableStatusBarBatteryDependents(batteryStyle);
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);
         }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.aosip_icons);
        mResolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

         mBatteryIcon =
                 (ColorPickerPreference) findPreference(BATTERY_ICON);
         intColor = Settings.System.getInt(mResolver,
                 Settings.System.BATTERY_ICON_COLOR,
                 WHITE); 
         mBatteryIcon.setNewPreviewColor(intColor);
         hexColor = String.format("#%08x", (0xffffffff & intColor));
         mBatteryIcon.setSummary(hexColor);
         mBatteryIcon.setDefaultColors(WHITE, HOLO_BLUE_LIGHT);
         mBatteryIcon.setOnPreferenceChangeListener(this);

         mBatteryText =
                 (ColorPickerPreference) findPreference(BATTERY_TEXT);
         intColor = Settings.System.getInt(mResolver,
                 Settings.System.BATTERY_TEXT_COLOR,
                 WHITE); 
         mBatteryText.setNewPreviewColor(intColor);
         hexColor = String.format("#%08x", (0xffffffff & intColor));
         mBatteryText.setSummary(hexColor);
         mBatteryText.setDefaultColors(WHITE, HOLO_BLUE_LIGHT);
         mBatteryText.setOnPreferenceChangeListener(this);
    }

	public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_RESET:
                 showDialogInner(DLG_RESET);
                 return true;
              default:
                 return super.onContextItemSelected(item);
         }
     }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        String hex;
        int intHex;
        if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            enableStatusBarBatteryDependents(batteryStyle);
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            int batteryShowPercent = Integer.valueOf((String) newValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
            mStatusBarBatteryShowPercent.setSummary(
                    mStatusBarBatteryShowPercent.getEntries()[index]);
            return true;
         }  else if (preference == mBatteryIcon) {
             hex = ColorPickerPreference.convertToARGB(
                     Integer.valueOf(String.valueOf(newValue)));
             intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(mResolver,
                     Settings.System.BATTERY_ICON_COLOR, intHex);
             preference.setSummary(hex);
             return true;
           }  else if (preference == mBatteryText) {
             hex = ColorPickerPreference.convertToARGB(
                     Integer.valueOf(String.valueOf(newValue)));
             intHex = ColorPickerPreference.convertToColorInt(hex);
             Settings.System.putInt(mResolver,
                     Settings.System.BATTERY_TEXT_COLOR, intHex);
             preference.setSummary(hex);
             return true;
        }
        return false;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN ||
                batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT) {
            mStatusBarBatteryShowPercent.setEnabled(false);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
        }
     }

        private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        BatteryCategory getOwner() {
            return (BatteryCategory) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.dlg_reset_values_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.dlg_reset_android,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                             Settings.System.putInt(getOwner().mResolver,
                                     Settings.System.BATTERY_ICON_COLOR,
                                     WHITE);
                             Settings.System.putInt(getOwner().mResolver,
                                     Settings.System.BATTERY_TEXT_COLOR,
                                     WHITE);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.dlg_reset_aosip,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                             Settings.System.putInt(getOwner().mResolver,
                                     Settings.System.BATTERY_ICON_COLOR,
                                     HOLO_BLUE_LIGHT);
                             Settings.System.putInt(getOwner().mResolver,
                                     Settings.System.BATTERY_TEXT_COLOR,
                                     HOLO_BLUE_LIGHT);
                            getOwner().refreshSettings();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
           }
        }
   }


