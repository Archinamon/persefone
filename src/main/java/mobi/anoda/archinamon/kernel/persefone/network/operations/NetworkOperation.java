package mobi.anoda.archinamon.kernel.persefone.network.operations;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
import java.util.List;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.json.IJson;

/**
 *
 * @author archinamon
 */
public interface NetworkOperation {

    class ErrorReport implements Parcelable {

        public static final List<Integer>      FATALS      = Arrays.asList(-9, -11, -7, 0);
        public static final int                NO_INTERNET = -1;
        public static final Creator CREATOR     = new Creator() {

            @Implement
            public ErrorReport createFromParcel(Parcel source) {
                return new ErrorReport(source);
            }

            @Implement
            public ErrorReport[] newArray(int size) {
                return new ErrorReport[size];
            }
        };

        int mResponseCode = -1;
        String mMessage;

        public static ErrorReport newReport(String msg, int code) {
            ErrorReport report = new ErrorReport(null);
            report.mMessage = msg;
            report.mResponseCode = code;

            return report;
        }

        ErrorReport(Parcel in) {
            if (in != null) {
                mResponseCode = in.readInt();
                mMessage = in.readString();
            }
        }

        public final int getStatus() {
            return mResponseCode;
        }

        public String getMessage() {
            return mMessage;
        }

        @Implement
        public int describeContents() {
            return 0;
        }

        @Implement
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mResponseCode);
            dest.writeString(mMessage);
        }
    }

    String getStringBody(AnodaApplicationDelegate appContext);

    IJson getJsonProjection(AnodaApplicationDelegate appContext);

    ErrorReport obtainErrorReport();
}
