package mobi.anoda.archinamon.kernel.persefone.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.support.annotation.NonNull;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractDialog;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.WarningDialog;

/**
 * author: Archinamon
 * project: FavorMe
 */
public class BitmapCaptureHelper {

    public static final String TAG = BitmapCaptureHelper.class.getSimpleName();

    @Nullable
    public static Uri launchCameraIntent(@NonNull final FragmentActivity context, @NonNull final Fragment fragment, final int CAMERA_REQUEST_CODE, @Nullable final String tag) {
        Uri imageUri = null;
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //NOTE: Do NOT SET: intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPicUri) on Samsung Galaxy S2/S3/.. for the following reasons:
            // 1.) it will break the correct picture orientation
            // 2.) the photo will be stored in two locations (the given path and additionally in the MediaStore)
            String manufacturer = android.os
                    .Build
                    .MANUFACTURER
                    .toLowerCase();
            if (!(manufacturer.contains("samsung")) && !(manufacturer.contains("sony"))) {
                String filename = "photo_" + System.currentTimeMillis() + ".jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, filename);
                imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }
            fragment.startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Bundle params = new Bundle();
            params.putString(WarningDialog.CUSTOM_DATA, context.getString(R.string.error_default));
            WarningDialog popup = (WarningDialog) AbstractDialog.newInstance(WarningDialog.class, params);
            popup.show(context.getSupportFragmentManager(),
                     tag != null
                     ? tag
                     : TAG);
        }

        return imageUri;
    }

    public static InputStream openInputStream(@NonNull Context activity, Uri uri) throws FileNotFoundException {
        if (TextUtils.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            return activity.getContentResolver()
                           .openInputStream(uri);
        } else if (TextUtils.isEmpty(uri.getScheme()) || TextUtils.equals(uri.getScheme(), ContentResolver.SCHEME_FILE)) {
            return new FileInputStream(uri.getPath());
        }
        return null;
    }

    /**
     *
     * @param activity context to build the resource stream
     * @param uri      file content link
     * @param size     max size to scale image to; pass here -1 if no resize needed;
     * @return resized bitmap
     */
    public static Bitmap getResizedBitmap(@NonNull Activity activity, Uri uri, int size) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(openInputStream(activity, uri), null, o);

            int scale = 1;
            if (size != -1) {
                while (o.outWidth / scale >= size && o.outHeight / scale >= size) {
                    scale <<= 1;
                }
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            InputStream is = openInputStream(activity, uri);

            return BitmapFactory.decodeStream(is, null, o2);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    public static Bitmap readBitmap(Context context, Uri selectedImage) {
        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inScaled = false;
        options.inSampleSize = 3;
        FileInputStream fio = null;
        try {
            File file = new File(context.getCacheDir(), selectedImage.toString());
            fio = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            LogHelper.println_error(TAG, e);
            return null;
        } finally {
            bm = BitmapFactory.decodeStream(fio, new Rect(), options);

            try {
                if (fio != null) fio.close();
            } catch (IOException e) {
                LogHelper.println_error(TAG, e);
            }
        }

        return bm;
    }

    public static void saveToPictures(Context context, Bitmap image, String folderName, String fileName, MediaScannerConnection.OnScanCompletedListener listener) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, folderName + "/" + fileName);
        try {
            file.getParentFile().mkdirs();
            image.compress(CompressFormat.JPEG, 80, new FileOutputStream(file));
            MediaScannerConnection.scanFile(context, new String[] {file.toString()}, null, listener);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
