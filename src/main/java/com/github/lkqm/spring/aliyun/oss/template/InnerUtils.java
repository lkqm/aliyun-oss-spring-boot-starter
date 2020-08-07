package com.github.lkqm.spring.aliyun.oss.template;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class InnerUtils {

    public static String getUrlPath(String url) {
        try {
            String path = new URL(url).getPath();
            return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
        } catch (MalformedURLException | UnsupportedEncodingException e) { // Ignore
        }
        return "";
    }

    public static String generateHost(String endpoint, String bucket) {
        String http = "http://";
        String https = "https://";
        if (endpoint.startsWith(http)) {
            return http + bucket + "." + endpoint.substring(http.length());
        }
        if (endpoint.startsWith(https)) {
            return https + bucket + "." + endpoint.substring(https.length());
        }
        return http + bucket + "." + endpoint;
    }


    public static String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Never Happen!", e);
        }
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Never Happen!", e);
        }
    }

    public static void checkArgument(boolean expect, String messageFormat, Object... args) {
        if (!expect) {
            String message = String.format(messageFormat, args);
            throw new IllegalArgumentException(message);
        }
    }

}