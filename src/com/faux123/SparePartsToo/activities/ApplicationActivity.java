/*
 * Copyright (C) 2011 The CyanogenMod Project
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
/* CMParts by CyanogenMod project modded by Faux123 */

package com.faux123.SparePartsToo.activities;

import com.faux123.SparePartsToo.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.IPackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

public class ApplicationActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String INSTALL_LOCATION_PREF = "pref_install_location";

    private static final String SWITCH_STORAGE_PREF = "pref_switch_storage";

    private static final String MOVE_ALL_APPS_PREF = "pref_move_all_apps";

    private static final String LOG_TAG = "SparePartsToo";

    private CheckBoxPreference mSwitchStoragePref;

    private CheckBoxPreference mMoveAllAppsPref;

    private ListPreference mInstallLocationPref;

    private IPackageManager mPm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (mPm == null) {
            Log.wtf(LOG_TAG, "Unable to get PackageManager!");
        }

        setTitle(R.string.application_settings_title_subhead);
        addPreferencesFromResource(R.xml.application_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mInstallLocationPref = (ListPreference) prefSet.findPreference(INSTALL_LOCATION_PREF);
        String installLocation = "0";
        try {
            installLocation = String.valueOf(mPm.getInstallLocation());
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Unable to get default install location!", e);
        }
        mInstallLocationPref.setValue(installLocation);
        mInstallLocationPref.setOnPreferenceChangeListener(this);

        mSwitchStoragePref = (CheckBoxPreference) prefSet.findPreference(SWITCH_STORAGE_PREF);
        mSwitchStoragePref.setChecked((SystemProperties.getInt("persist.sys.vold.switchexternal", 0) == 1));

        if (SystemProperties.get("ro.vold.switchablepair","").equals("")) {
            mSwitchStoragePref.setSummary(R.string.pref_storage_switch_unavailable);
            mSwitchStoragePref.setEnabled(false);
        }

        mMoveAllAppsPref = (CheckBoxPreference) prefSet.findPreference(MOVE_ALL_APPS_PREF);
        mMoveAllAppsPref.setChecked(Settings.Secure.getInt(getContentResolver(),
            Settings.Secure.ALLOW_MOVE_ALL_APPS_EXTERNAL, 0) == 1);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mMoveAllAppsPref) {
            Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ALLOW_MOVE_ALL_APPS_EXTERNAL, mMoveAllAppsPref.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mSwitchStoragePref) {
            SystemProperties.set("persist.sys.vold.switchexternal",
		mSwitchStoragePref.isChecked() ? "1" : "0");
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mInstallLocationPref) {
            if (newValue != null) {
                try {
                    mPm.setInstallLocation(Integer.valueOf((String)newValue));
                    return true;
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "Unable to get default install location!", e);
                }
            }
        }
        return false;
    }

}
