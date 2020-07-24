package com.luminaryn.common;

import android.util.Base64;

public class Safe64 {

    /**
     * Convert a Base64 string into a Safe64 string.
     * Safe64 is a version of Base64 with all URL-safe characters.
     *
     * As per my other similar libraries in PHP and Javascript:
     *
     *  '+' becomes '-'
     *  '/' becomes '_'
     *
     * If useTildes is true, then:
     *
     *  '=' becomes '~'
     *
     * If useTildes is false then '=' characters will simply be stripped.
     *
     * @param text
     * @param useTildes
     * @return
     */
    public static String urlize(String text, boolean useTildes) {
        text = text
                .replace('+', '-')
                .replace('/', '_');
        if (useTildes)
            text = text.replace('=', '~');
        else
            text = text.replace("=","");
        return text;
    }

    public static String deurlize(String text) {
        text = text
                .replace('-','+')
                .replace('_','/')
                .replace('~','=');
        String append = "===".substring((text.length()+3)%4);
        if (!append.isEmpty()) {
            text = text + append;
        }
        return text;
    }

    public static String encode (byte[] data, boolean useTildes, int b64flags) {
        return urlize(Base64.encodeToString(data, b64flags), useTildes);
    }

    public static String encode (byte[] data, boolean useTildes) {
        return encode(data, useTildes, Base64.NO_WRAP);
    }

    public static byte[] decode (String data, int b64flags) {
        return Base64.decode(data, b64flags);
    }

}
