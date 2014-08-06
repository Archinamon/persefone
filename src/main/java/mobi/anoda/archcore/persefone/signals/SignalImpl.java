package mobi.anoda.archcore.persefone.signals;

import android.os.Parcel;
import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.network.processor.ISignal;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
class SignalImpl implements ISignal {

    public static final String              TAG     = SignalImpl.class.getSimpleName();
    public static final Creator<SignalImpl> CREATOR = new Creator<SignalImpl>() {

        @Implement
        public SignalImpl createFromParcel(Parcel source) {
            SignalImpl sig = new SignalImpl();
            sig.mCommandStr = source.readString();

            return sig;
        }

        @Implement
        public SignalImpl[] newArray(int size) {
            return new SignalImpl[size];
        }
    };
    private String        mCommandStr;

    private SignalImpl() {
    }

    SignalImpl(String action) {
        mCommandStr = action;
    }

    SignalImpl(Broadcastable action) {
        mCommandStr = action.getAction();
    }

    @Implement
    public byte[] initCommand() {
        return mCommandStr.getBytes();
    }

    @Implement
    public int describeContents() {
        return 0;
    }

    @Implement
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCommandStr);
    }
}
