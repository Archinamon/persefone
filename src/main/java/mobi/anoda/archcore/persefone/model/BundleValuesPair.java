package mobi.anoda.archcore.persefone.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
import mobi.anoda.archcore.persefone.annotation.Implement;

public final class BundleValuesPair<I extends Serializable> implements Parcelable {

    public static final Creator<BundleValuesPair> CREATOR = new Creator<BundleValuesPair>() {

        @Implement
        public BundleValuesPair createFromParcel(Parcel source) {
            BundleValuesPair model = new BundleValuesPair();

            model.mKey = source.readString();
            model.mValue = source.readSerializable();

            return model;
        }

        @Implement
        public BundleValuesPair[] newArray(int size) {
            return new BundleValuesPair[size];
        }
    };
    private String mKey;
    private I      mValue;

    public BundleValuesPair() {
    }

    public BundleValuesPair(String key, I value) {
        this.setKey(key);
        this.setValue(value);
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String mKey) {
        this.mKey = mKey;
    }

    public I getValue() {
        return mValue;
    }

    public void setValue(I mValue) {
        this.mValue = mValue;
    }

    @Implement
    public int describeContents() {
        return 0;
    }

    @Implement
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mKey);
        dest.writeSerializable(this.mValue);
    }
}