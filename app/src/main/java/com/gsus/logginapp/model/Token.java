package com.gsus.logginapp.model;

import com.google.gson.annotations.SerializedName;

import java.security.PrivateKey;

public class Token {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
