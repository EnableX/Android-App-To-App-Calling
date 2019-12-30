package com.tech.shashankg.callkit.web_communication;

public interface WebResponse {
    void onWebResponse(String response, int callCode);

    void onWebResponseError(String error, int callCode);
}
