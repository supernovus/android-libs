package com.luminaryn.common;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class Images {

    private static void saveImage(@NonNull final Context context, @NonNull final Bitmap bitmap,
                           @NonNull final Bitmap.CompressFormat format, @NonNull final String mimeType,
                           @NonNull final int quality, @NonNull final String filename) throws IOException {
        OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File image = new File(imagesDir, filename);
            fos = new FileOutputStream(image);
        }
        bitmap.compress(format, quality, fos);
        Objects.requireNonNull(fos).close();
    }

    public static void saveImage(Context context, Bitmap bitmap, @NonNull int quality, @NonNull String filename) throws IOException {
        final Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        final String mimeType = "image/jpeg";
        saveImage(context, bitmap, format, mimeType, quality, filename);
    }

    public static void saveImage(Context context, Bitmap bitmap, @NonNull String filename) throws IOException {
        saveImage(context, bitmap, 100, filename);
    }

    public static String base64Encode (Bitmap bitmap, @NonNull Bitmap.CompressFormat format, @NonNull int quality) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(format, quality, bos);
        String base64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        bos.close();
        return base64;
    }

    public static String base64Encode (Bitmap bitmap, @NonNull int quality) throws IOException {
        return base64Encode(bitmap, Bitmap.CompressFormat.JPEG, quality);
    }

    public static String base64Encode (Bitmap bitmap) throws IOException {
        return base64Encode(bitmap, 100);
    }

    public static byte[] toJPEG (Bitmap bitmap, @NonNull int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    public static byte[] toJPEG (Bitmap bitmap) {
        return toJPEG(bitmap, 100);
    }

}
