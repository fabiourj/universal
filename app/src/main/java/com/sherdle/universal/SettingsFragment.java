package com.sherdle.universal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Html;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.sherdle.universal.util.Log;
import com.sherdle.universal.util.PurchaseHelper;
/**
 * This fragmnt is used to show a settings page to the user
 */

public class SettingsFragment extends androidx.core.preference.PreferenceFragment {
	
	//You can change this setting if you would like to disable rate-my-app
	boolean HIDE_RATE_MY_APP = false;

	private Preference preferencepurchase;
	
	private AlertDialog dialog;

	public static String SHOW_DIALOG = "show_dialog";

	private int count = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_settings);

		// open play store page
		Preference preferencerate = findPreference("rate");
		preferencerate
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Uri uri = Uri.parse("market://details?id="
								+ getActivity().getPackageName());
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						try {
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(getActivity(),
									"Could not open Play Store",
									Toast.LENGTH_SHORT).show();
							return true;
						}
						return true;
					}
				});

		Preference preferenceVersion = findPreference("version");
		try {
			PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			String version = pInfo.versionName;
			preferenceVersion.setSummary(version);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		preferenceVersion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				count++;
				if (count == 2) {

					FirebaseInstallations.getInstance().getId()
							.addOnCompleteListener(new OnCompleteListener<String>() {
								@Override
								public void onComplete(@NonNull Task<String> task) {
									if (task.isSuccessful()) {
										String installationId = task.getResult();

										// Log
										Intent sharingIntent = new Intent(Intent.ACTION_SEND);
										sharingIntent.setType("text/plain");
										sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App Token");
										sharingIntent.putExtra(Intent.EXTRA_TEXT, installationId);
										startActivity(Intent.createChooser(sharingIntent, "App Token"));

									} else {
										Log.e("Installations", "Unable to get Installation ID");
									}
								}
							});

				}
				return false;
			}
		});

		// open about dialog
		Preference preferenceabout = findPreference("about");
		preferenceabout
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						AlertDialog.Builder ab = null;
						ab = new AlertDialog.Builder(getActivity());
						ab.setMessage(Html.fromHtml(getResources().getString(
								R.string.about_text)));
						ab.setPositiveButton(
								getResources().getString(R.string.ok), null);
						ab.setTitle(getResources().getString(
								R.string.about_header));
						ab.show();
						return true;
					}
				});

		// open about dialog
		Preference preferencelicenses = findPreference("licenses");
		preferencelicenses
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						HolderActivity.startWebViewActivity(getActivity(), "file:///android_asset/open_source_licenses.html", false, true, null);
						return true;
					}
				});

		if (Config.HIDE_DRAWER || Config.DRAWER_OPEN_START) {
			PreferenceCategory generalCategory = (PreferenceCategory) findPreference("general");
			Preference preferencedraweropen = findPreference("menuOpenOnStart");
			generalCategory.removePreference(preferencedraweropen);
		}

		// notifications
		Preference notificationsPreference = findPreference("notifications");
		String oneSignalAppID = getResources().getString(R.string.onesignal_app_id);
		if (null != oneSignalAppID && !oneSignalAppID.equals("")){
			notificationsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Context context = getActivity();
					Intent intent = new Intent();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
						intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
					} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
						intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
						intent.putExtra("app_package", context.getPackageName());
						intent.putExtra("app_uid", context.getApplicationInfo().uid);
					} else {
						intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						intent.addCategory(Intent.CATEGORY_DEFAULT);
						intent.setData(Uri.parse("package:" + context.getPackageName()));
					}
					context.startActivity(intent);
					return true;
				}
			});
		} else {
			PreferenceCategory general = (PreferenceCategory) findPreference("general");
			general.removePreference(notificationsPreference);
		}
		
		// purchase
		preferencepurchase = findPreference("purchase");
		String license = getResources().getString(R.string.google_play_license);
		if (null != license && !license.equals("")){
		
			preferencepurchase
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						PurchaseHelper.getPurchaseHelper(getActivity()).purchase(getActivity());
						return true;
					}
				});
		
			if (getIsPurchased(getActivity())){
				preferencepurchase.setIcon(R.drawable.ic_action_action_done);
			}
		} else {
			PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");
			PreferenceCategory billing = (PreferenceCategory) findPreference("billing");
			preferenceScreen.removePreference(billing);
		}
		
		String[] extra = getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
		if (null != extra && extra.length != 0 && extra[0].equals(SHOW_DIALOG)){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Add the buttons
			builder.setPositiveButton(R.string.settings_purchase, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
						   PurchaseHelper.getPurchaseHelper(getActivity()).purchase(getActivity());
			           }
			       });
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User cancelled the dialog
			           }
			       });
			builder.setTitle(getResources().getString(R.string.dialog_purchase_title));
			builder.setMessage(getResources().getString(R.string.dialog_purchase));

			// Create the AlertDialog
			dialog = builder.create();
			dialog.show();
		}
		
		if (HIDE_RATE_MY_APP){
			PreferenceCategory other = (PreferenceCategory) findPreference("other");
			Preference preference = findPreference("rate");
			other.removePreference(preference);
		}

	}
	public static boolean getIsPurchased(Context c){
		return PurchaseHelper.getPurchaseHelper(c).isPurchased();
	}

}
