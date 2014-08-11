package mobi.anoda.archinamon.kernel.persefone.upload;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.UnsupportedEncodingException;

/**
 * Represents a request parameter.
 *
 * @author alexbbb (Alex Gotev)
 */
public class NameValue implements Parcelable {

    // This is used to regenerate the object.
    // All Parcelables must have a CREATOR that implements these two methods
    public static final  Parcelable.Creator<NameValue> CREATOR  = new Parcelable.Creator<NameValue>() {

        @Override
        public NameValue createFromParcel(final Parcel in) {
            return new NameValue(in);
        }

        @Override
        public NameValue[] newArray(final int size) {
            return new NameValue[size];
        }
    };
    private static final String                        NEW_LINE = "\r\n";
    private final String mName;
    private final String mValue;

    public NameValue(final String name, final String value) {
        this.mName = name;
        this.mValue = value;
    }

    public final String getName() {
        return mName;
    }

    public final String getValue() {
        return mValue;
    }

    public byte[] getBytes() throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();

        builder.append("Content-Disposition: form-data; name=\"")
               .append(mName)
               .append("\"")
               .append(NEW_LINE)
               .append(NEW_LINE)
               .append(mValue);

        return builder.toString()
                      .getBytes("UTF-8");
    }

    @Override
    public boolean equals(Object object) {
        final boolean areEqual;

        if (object instanceof NameValue) {
            final NameValue other = (NameValue) object;
            areEqual = this.mName.equals(other.mName) && this.mValue.equals(other.mValue);
        } else {
            areEqual = false;
        }

        return areEqual;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
        parcel.writeString(mName);
        parcel.writeString(mValue);
    }

    private NameValue(Parcel in) {
        mName = in.readString();
        mValue = in.readString();
    }
}