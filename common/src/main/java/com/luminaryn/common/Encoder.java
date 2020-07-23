package com.luminaryn.common;

import android.util.Base64;
import android.widget.EditText;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * An encoder class, most of which was originally from my Passy app, the generic parts
 * have now been split into this library for ease of use elsewhere.
 */
public class Encoder {
    public static final int HEX    = 0; // Not actually used, but here for completion.
    public static final int BASE64 = 1; // Base64 is probably the most common.
    public static final int BASE91 = 2; // Base91 is more compact, but less HTTP friendly.

    protected MessageDigest digest;

    public Encoder(String algorithm) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public String encodeDigest(int format) {
        byte[] digestBytes = digest.digest();
        if (format == BASE91)
            return Base91.encodeToString(digestBytes);
        else if (format == BASE64)
            return Base64.encodeToString(digestBytes, Base64.NO_WRAP);
        else
            return toHex(digestBytes);
    }


    public void add (byte[] bytes)
    {
        digest.update(bytes);
    }

    public void add (String string)
    {
        add(string.getBytes());
    }

    public void add (EditText text)
    {
        add(getTextBytes(text));
    }

    protected static byte[] getTextBytes(EditText text) {
        return text.getText().toString().getBytes();
    }

    public static String urlize(String text, boolean useTildes) {
        text = text.replace('+', '-');
        text = text.replace('/', '_');
        if (useTildes)
            text = text.replace('=', '~');
        return text;
    }

    /**
     * A special version of encodeDigest that always uses BASE64, and
     * passes it through urlize() to replace characters that might be
     * problematic.
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
     * @param useTildes Replace '=' with '~'
     * @return The URL safe encoded string.
     */
    public String encodeForUrl (boolean useTildes)
    {
        return urlize(encodeDigest(BASE64), useTildes);
    }

}
