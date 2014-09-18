package mobi.anoda.archinamon.kernel.persefone.signal.impl;

import android.os.Parcel;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.processor.ISignal;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
class ServiceSignal implements ISignal {

    public static final String                 TAG     = ServiceSignal.class.getSimpleName();
    public static final Creator<ServiceSignal> CREATOR = new Creator<ServiceSignal>() {

        @Implement
        public ServiceSignal createFromParcel(Parcel source) {
            ServiceSignal sig = new ServiceSignal();
            sig.mCommandStr = source.readString();

            return sig;
        }

        @Implement
        public ServiceSignal[] newArray(int size) {
            return new ServiceSignal[size];
        }
    };
    private String mCommandStr;

    private ServiceSignal() {
    }

    ServiceSignal(String action) {
        mCommandStr = action;
    }

    ServiceSignal(Broadcastable action) {
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
