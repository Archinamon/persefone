package mobi.anoda.archcore.persefone.inhouse;

import android.os.Parcel;
import android.os.Parcelable;
import mobi.anoda.archcore.persefone.annotation.Implement;

/**
 * Created by Archinamon on 5/7/14.
 */
public class InHouseApplicationInfo implements Parcelable {

    public static final Creator<InHouseApplicationInfo> CREATOR = new Creator<InHouseApplicationInfo>() {

        @Implement
        public InHouseApplicationInfo createFromParcel(Parcel source) {
            return null;
        }

        @Implement
        public InHouseApplicationInfo[] newArray(int size) {
            return new InHouseApplicationInfo[size];
        }
    };

    @Implement
    public int describeContents() {
        return 0;
    }

    @Implement
    public void writeToParcel(Parcel dest, int flags) {

    }
}
