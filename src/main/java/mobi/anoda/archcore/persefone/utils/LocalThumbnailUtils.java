package mobi.anoda.archcore.persefone.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.provider.MediaStore.Images;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import mobi.anoda.archcore.persefone.utils.MediaFile.MediaFileType;

/**
 * author: Archinamon
 * project: FavorMe
 */
public class LocalThumbnailUtils {

    private static class SizedThumbnailBitmap {

        public byte[] mThumbnailData;
        public Bitmap mBitmap;
        public int    mThumbnailWidth;
        public int    mThumbnailHeight;
    }

    public static final  String TAG               = LocalThumbnailUtils.class.getSimpleName();
    public static final  int    THUMB_SIZE_NORMAL = 320;
    public static final  int    THUMB_SIZE_SMALL  = 120;
    private static final int    MAX_PIXELS_SIZE   = 512 * 384;

    public static Bitmap createImageThumbnail(String filePath, int kind) {
        boolean wantMini = (kind == Images.Thumbnails.MINI_KIND);
        int targetSize = wantMini
                         ? THUMB_SIZE_NORMAL
                         : THUMB_SIZE_SMALL;
        int maxPixels = MAX_PIXELS_SIZE;
        SizedThumbnailBitmap sizedThumbnailBitmap = new SizedThumbnailBitmap();
        Bitmap bitmap = null;
        MediaFileType fileType = MediaFile.getFileType(filePath);
        if (fileType != null && fileType.fileType == MediaFile.FILE_TYPE_JPEG) {
            createThumbnailFromEXIF(filePath, targetSize, maxPixels, sizedThumbnailBitmap);
            bitmap = sizedThumbnailBitmap.mBitmap;
        }

        if (bitmap == null) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(filePath);
                FileDescriptor fd = stream.getFD();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd, null, options);
                if (options.mCancel || options.outWidth == -1
                    || options.outHeight == -1) {
                    return null;
                }
                options.inSampleSize = computeSampleSize(options, targetSize, maxPixels);
                options.inJustDecodeBounds = false;

                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
            } catch (IOException ex) {
                Log.e(TAG, "", ex);
            } catch (OutOfMemoryError oom) {
                Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "", ex);
                }
            }

        }

        return bitmap;
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                                                   maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8 ) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                         (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                         (int) Math.min(Math.floor(w / minSideLength),
                                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
            (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    private static void createThumbnailFromEXIF(String filePath, int targetSize,
                                                int maxPixels, SizedThumbnailBitmap sizedThumbBitmap) {
        if (filePath == null) return;

        ExifInterface exif = null;
        byte [] thumbData = null;
        try {
            exif = new ExifInterface(filePath);
            thumbData = exif.getThumbnail();
        } catch (IOException ex) {
            Log.w(TAG, ex);
        }

        BitmapFactory.Options fullOptions = new BitmapFactory.Options();
        BitmapFactory.Options exifOptions = new BitmapFactory.Options();
        int exifThumbWidth = 0;
        int fullThumbWidth = 0;

        // Compute exifThumbWidth.
        if (thumbData != null) {
            exifOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
            exifOptions.inSampleSize = computeSampleSize(exifOptions, targetSize, maxPixels);
            exifThumbWidth = exifOptions.outWidth / exifOptions.inSampleSize;
        }

        // Compute fullThumbWidth.
        fullOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, fullOptions);
        fullOptions.inSampleSize = computeSampleSize(fullOptions, targetSize, maxPixels);
        fullThumbWidth = fullOptions.outWidth / fullOptions.inSampleSize;

        // Choose the larger thumbnail as the returning sizedThumbBitmap.
        if (thumbData != null && exifThumbWidth >= fullThumbWidth) {
            int width = exifOptions.outWidth;
            int height = exifOptions.outHeight;
            exifOptions.inJustDecodeBounds = false;
            sizedThumbBitmap.mBitmap = BitmapFactory.decodeByteArray(thumbData, 0,
                                                                     thumbData.length, exifOptions);
            if (sizedThumbBitmap.mBitmap != null) {
                sizedThumbBitmap.mThumbnailData = thumbData;
                sizedThumbBitmap.mThumbnailWidth = width;
                sizedThumbBitmap.mThumbnailHeight = height;
            }
        } else {
            fullOptions.inJustDecodeBounds = false;
            sizedThumbBitmap.mBitmap = BitmapFactory.decodeFile(filePath, fullOptions);
        }
    }
}
