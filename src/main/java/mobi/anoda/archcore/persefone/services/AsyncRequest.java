package mobi.anoda.archcore.persefone.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.model.NetworkModel;
import mobi.anoda.archcore.persefone.network.processor.ISignal;
import mobi.anoda.archcore.persefone.signals.CallQueue;
import mobi.anoda.archcore.persefone.signals.Channel;
import mobi.anoda.archcore.persefone.ui.dialog.AbstractPopup;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public final class AsyncRequest<Model extends NetworkModel> implements Parcelable {

    public static final String                TAG     = AsyncRequest.class.getSimpleName();
    public static final Uri                   DATA    = Uri.parse("async://mobi.anoda/archcore/persefone/virtual.request/");
    public static final Creator<AsyncRequest> CREATOR = new Creator<AsyncRequest>() {

        @Implement
        public AsyncRequest createFromParcel(Parcel source) {
            Channel gate = Channel.values()[source.readInt()];
            ISignal signal = source.readParcelable(ISignal.class.getClassLoader());
            NetworkModel model = source.readParcelable(NetworkModel.class.getClassLoader());
            //parametrize section
            Class<? extends AbstractPopup> popup = (Class<? extends AbstractPopup>) source.readSerializable();
            boolean isDaemon = source.readByte() > 0;

            AsyncRequest obj = new AsyncRequest<>(gate, signal, model);
            obj.addPopup(popup);
            obj.setDaemon(isDaemon);

            return obj;
        }

        @Implement
        public AsyncRequest[] newArray(int size) {
            return new AsyncRequest[size];
        }
    };
    private Channel                        mGate;
    private ISignal                        mCommand;
    private Model                          mModel;
    private Class<? extends AbstractPopup> mProgressClass;
    private boolean                        mIsDaemon;

    public AsyncRequest(Channel action, ISignal call, Model params) {
        mGate = action;
        mCommand = call;
        mModel = params;
    }

    public AsyncRequest(Channel action, ISignal call) {
        mGate = action;
        mCommand = call;
        mModel = null;
    }

    public void setDaemon(boolean val) {
        this.mIsDaemon = val;
    }

    public void addPopup(Class<? extends AbstractPopup> popup) {
        this.mProgressClass = popup;
    }

    public static <Server extends AbstractAsyncServer> void send(@Nonnull final Context context, final AsyncRequest what, @Nonnull final Class<Server> where) {
        Intent intent = new Intent(context, where);
        intent.setData(DATA)
              .putExtra(CallQueue.DATA, what);

        context.startService(intent);
    }

    Channel getGate() {
        return mGate;
    }

    String getCommand() throws UnsupportedEncodingException {
        return new String(mCommand.initCommand(), Charset.forName("UTF-8"));
    }

    Model getPropagator() {
        return mModel;
    }

    Class<? extends AbstractPopup> getPopupClass() {
        return mProgressClass;
    }

    boolean isDaemon() {
        return mIsDaemon;
    }

    @Implement
    public int describeContents() {
        return 0;
    }

    @Implement
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mGate.ordinal());
        dest.writeParcelable(mCommand, flags);
        dest.writeParcelable(mModel, flags);
        dest.writeSerializable(mProgressClass);
        dest.writeByte((byte) (mIsDaemon ? 1 : 0));
    }
}