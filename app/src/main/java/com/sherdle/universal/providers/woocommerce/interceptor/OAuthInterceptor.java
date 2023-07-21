package com.sherdle.universal.providers.woocommerce.interceptor;

import android.util.Base64;

import com.sherdle.universal.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OAuthInterceptor implements Interceptor {

    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String OAUTH_VERSION_VALUE = "1.0";

    private static final String BASIC_CONSUMER_KEY = "consumer_key";
    private static final String BASIC_CONSUMER_SECRET = "consumer_secret";

    private final String consumerKey;
    private final String consumerSecret;

    //If true, oAuth 1.0 authentication will be used, if false, Basic Auth (SSL only)
    private final boolean OAUTH = true;

    private OAuthInterceptor(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        if (OAUTH) {
            HttpUrl originalHttpUrl = original.url();

            Log.d("URL", original.url().toString());
            Log.d("URL", original.url().scheme());
            Log.d("encodedpath", original.url().encodedPath());
            Log.d("query", "" + original.url().query());
            Log.d("path", "" + original.url().host());
            Log.d("encodedQuery", "" + original.url().encodedQuery());
            Log.d("method", "" + original.method());

            ////////////////////////////////////////////////////////////

            final String nonce = new TimestampServiceImpl().getNonce();
            final String timestamp = new TimestampServiceImpl().getTimestampInSeconds();
            Log.d("nonce", nonce);
            Log.d("time", timestamp);

            String dynamicStructureUrl = original.url().scheme() + "://" + original.url().host() + original.url().encodedPath();

            Log.d("ENCODED PATH", "" + dynamicStructureUrl);
            String firstBaseString = original.method() + "&" + urlEncoded(dynamicStructureUrl);
            Log.d("firstBaseString", firstBaseString);
            String generatedBaseString = "";


            if (original.url().encodedQuery() != null) {
                generatedBaseString = original.url().encodedQuery() + "&oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonce + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timestamp + "&oauth_version=1.0";
            } else {
                generatedBaseString = "oauth_consumer_key=" + consumerKey + "&oauth_nonce=" + nonce + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + timestamp + "&oauth_version=1.0";

            }

            ParameterList result = new ParameterList();
            result.addQuerystring(generatedBaseString);
            generatedBaseString = result.sort().asOauthBaseString();
            Log.d("Sorted", "00--" + result.sort().asOauthBaseString());

            String secoundBaseString = "&" + generatedBaseString;

            if (firstBaseString.contains("%3F")) {
                Log.d("iff", "yess iff");
                secoundBaseString = "%26" + urlEncoded(generatedBaseString);
            }

            String baseString = firstBaseString + secoundBaseString;

            String signature = new HMACSha1SignatureService().getSignature(baseString, consumerSecret, "");
            Log.d("Signature", signature);

            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE)
                    .addQueryParameter(OAUTH_CONSUMER_KEY, consumerKey)
                    .addQueryParameter(OAUTH_VERSION, OAUTH_VERSION_VALUE)
                    .addQueryParameter(OAUTH_TIMESTAMP, timestamp)
                    .addQueryParameter(OAUTH_NONCE, nonce)
                    .addQueryParameter(OAUTH_SIGNATURE, signature)
                    .build();

            // Request customization: add request headers
            Request.Builder requestBuilder = original.newBuilder()
                    .url(url);

            Request request = requestBuilder.build();

            return chain.proceed(request);
        } else {
            HttpUrl originalHttpUrl = original.url();

            Log.d("URL", original.url().toString());

            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter(BASIC_CONSUMER_KEY, consumerKey)
                    .addQueryParameter(BASIC_CONSUMER_SECRET, consumerSecret)
                    .build();

            Request.Builder requestBuilder = original.newBuilder()
                    .url(url);

            String credentials = consumerKey + ":" + consumerSecret;
            String authorisation = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            requestBuilder.addHeader("Authorization", authorisation);

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }


    public static final class Builder {

        private String consumerKey;
        private String consumerSecret;
        private int type;

        public Builder consumerKey(String consumerKey) {
            if (consumerKey == null) throw new NullPointerException("consumerKey = null");
            this.consumerKey = consumerKey;
            return this;
        }

        public Builder consumerSecret(String consumerSecret) {
            if (consumerSecret == null) throw new NullPointerException("consumerSecret = null");
            this.consumerSecret = consumerSecret;
            return this;
        }



        public OAuthInterceptor build() {

            if (consumerKey == null) throw new IllegalStateException("consumerKey not set");
            if (consumerSecret == null) throw new IllegalStateException("consumerSecret not set");

            return new OAuthInterceptor(consumerKey, consumerSecret);
        }
    }

    public String urlEncoded(String url) {
        String encodedurl = "";
        try {

            encodedurl = URLEncoder.encode(url, "UTF-8");
            Log.d("TEST", encodedurl);
        } catch (UnsupportedEncodingException e) {
            Log.printStackTrace(e);
        }

        return encodedurl;
    }
}