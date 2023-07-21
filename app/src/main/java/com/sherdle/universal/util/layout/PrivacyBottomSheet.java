package com.sherdle.universal.util.layout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sherdle.universal.R;

public class PrivacyBottomSheet extends BottomSheetDialogFragment {

    private static String PRIVACY_PREF_STORAGE = "PrivacyPreferences";
    private static String PRIVACY_STORAGE_KEY = "not_agreed_yet";

    public static void showPrivacySheetIfNeeded(AppCompatActivity context){
        if (context.getResources().getString(R.string.privacy_policy_url).length() == 0) return;

        SharedPreferences settings = context.getSharedPreferences(PRIVACY_PREF_STORAGE, 0);
        if (settings.getBoolean(PRIVACY_STORAGE_KEY, true)) {
            PrivacyBottomSheet bottomSheet = new PrivacyBottomSheet();
            bottomSheet.show(context.getSupportFragmentManager(), bottomSheet.getTag());
            bottomSheet.setCancelable(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_privacy, container, false);

        TextView summaryView = v.findViewById(R.id.privacy_summary_text);
        String summary = getResources().getString(R.string.privacy_policy_summary,  getResources().getString(R.string.privacy_policy_url));
        Spanned result = HtmlCompat.fromHtml(summary,HtmlCompat.FROM_HTML_MODE_LEGACY);
        summaryView.setText(result);
        summaryView.setMovementMethod(LinkMovementMethod.getInstance());

        Button button = v.findViewById(R.id.privacy_accept_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getContext().getSharedPreferences(PRIVACY_PREF_STORAGE, 0);
                settings.edit().putBoolean(PRIVACY_STORAGE_KEY, false).apply();
                dismiss();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

}
