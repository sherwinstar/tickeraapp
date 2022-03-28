package com.tickera.tickeraapp.stripe_terminal

import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.models.ConnectionTokenException

class TokenProvider(var secretKey: String) : ConnectionTokenProvider {
    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        try {
            // Your backend should call /v1/terminal/connection_tokens and return the
            // JSON response from Stripe. When the request to your backend succeeds,
            // return the `secret` from the response to the SDK.
            callback.onSuccess(secretKey)
        } catch (e: Exception) {
            callback.onFailure(
                ConnectionTokenException("Failed to fetch connection token", e)
            )
        }
    }
}