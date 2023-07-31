package com.sherdle.universal.providers.audio.api.soundcloud;

import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpRequestInterceptor;
import cz.msebera.android.httpclient.auth.AuthScheme;
import cz.msebera.android.httpclient.auth.AuthState;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import cz.msebera.android.httpclient.client.protocol.ClientContext;
import cz.msebera.android.httpclient.protocol.HttpContext;

import java.io.IOException;

class OAuth2HttpRequestInterceptor implements HttpRequestInterceptor {
    @Override public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request == null) throw new IllegalArgumentException("HTTP request may not be null");
        if (context == null) throw new IllegalArgumentException("HTTP context may not be null");

        if (!request.getRequestLine().getMethod().equalsIgnoreCase("CONNECT")) {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
            if (authState != null) {
                AuthScheme authScheme = authState.getAuthScheme();
                if (authScheme != null && !authScheme.isConnectionBased()) {
                    try {
                        request.setHeader(authScheme.authenticate(null, request));
                    } catch (AuthenticationException ignored) {
                        // ignored
                    }
                }
            }
        }
    }
}
