package com.sherdle.universal.providers.woocommerce.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

public class WooCommerceDebugDialog {
    public static void showDialogIfAuthFailed(String response, final Context context) {
        if (response == null || !response.contains("woocommerce_rest_authentication_error")) return;

        showDialog("Authentication Error",
                "Universal tried to connect to your API but was refused. " +
                "You entered the correct API url, and we found your API but we were " +
                "not allowed to retrieve any products.\n\n" +
                "There can be various reasons for this. You might have used the wrong " +
                "credentials. It can be that your server firewall refuses " +
                "our requests. Or it can be that your Wordpress installation doesn't accept oAuth.\n\n" +
                "Note that this is not a problem of Universal, but instead a server " +
                "side problem.\n\n" +
                "Please visit our Help Center and search for WooCommerce for recommended" +
                "steps to take", context);
    }

    public static void showDialogIfNoCookies(final Context context) {
        showDialog("Login Error",
                "Universal tried to authenticate the user against your login page but" +
                "the page returned no cookies. This usually means that your login page" +
                "is not correctly configured\n\n" +
                "For example, it can be that your server refuses post requests or that " +
                "your login uses an alternative to cookies. " +
                "Note that this is not a problem of Universal, but instead a server " +
                "side problem. You can visit our help center and search for WooCommerce" +
                "for advice on which steps to take next.", context);
    }

    private static void showDialog(final String title, final String message, final Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .show();
            }
        });
    }
}
