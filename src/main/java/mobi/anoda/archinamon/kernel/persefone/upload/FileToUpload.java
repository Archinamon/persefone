package mobi.anoda.archinamon.kernel.persefone.upload;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Represents a file to upload.
 *
 * @author alexbbb (Alex Gotev)
 * @author eliasnaur
 */
public class FileToUpload implements Parcelable {

    // This is used to regenerate the object.
    // All Parcelables must have a CREATOR that implements these two methods
    public static final  Parcelable.Creator<FileToUpload> CREATOR  = new Parcelable.Creator<FileToUpload>() {

        @Override
        public FileToUpload createFromParcel(final Parcel in) {
            return new FileToUpload(in);
        }

        @Override
        public FileToUpload[] newArray(final int size) {
            return new FileToUpload[size];
        }
    };
    private static final String                           NEW_LINE = "\r\n";
    private final File   mFile;
    private final String mFileName;
    private final String mParamName;
    private final String mContentType;

    /**
     * Create a new {@link FileToUpload} object.
     *
     * @param path          absolute path to the file
     * @param parameterName parameter name to use in the multipart form
     * @param contentType   content type of the file to send
     */
    public FileToUpload(final String path, final String parameterName, final String fileName, final String contentType) {
        this.mFile = new File(path);
        this.mParamName = parameterName;
        this.mContentType = contentType;

        if (fileName == null || "".equals(fileName)) {
            this.mFileName = this.mFile.getName();
        } else {
            this.mFileName = fileName;
        }
    }

    public final InputStream getStream() throws FileNotFoundException {
        return new FileInputStream(mFile);
    }

    public final File getFile() {
        return mFile;
    }

    public final String getFileName() {
        return mFileName;
    }

    public final String getMultipartParam() {
        return mParamName;
    }

    public byte[] getMultipartHeader() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();

        builder.append("Content-Disposition: form-data; name=\"")
               .append(mParamName)
               .append("\"; filename=\"")
               .append(mFileName)
               .append("\"")
               .append(NEW_LINE);

        if (mContentType != null) {
            builder.append("Content-Type: ")
                   .append(mContentType)
                   .append(NEW_LINE);
        }

        builder.append(NEW_LINE);

        return builder.toString()
                      .getBytes("UTF-8");
    }

    public long length() {
        return mFile.length();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
        parcel.writeString(mFile.getAbsolutePath());
        parcel.writeString(mParamName);
        parcel.writeString(mContentType);
        parcel.writeString(mFileName);
    }

    private FileToUpload(Parcel in) {
        mFile = new File(in.readString());
        mParamName = in.readString();
        mContentType = in.readString();
        mFileName = in.readString();
    }
}