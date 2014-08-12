package mobi.anoda.archinamon.kernel.persefone.network.processor;

import android.os.AsyncTask.Status;
import javax.annotation.Nonnull;
import mobi.anoda.archinamon.kernel.persefone.model.NetworkModel;
import mobi.anoda.archinamon.kernel.persefone.network.async.AbstractAsyncTask;
import mobi.anoda.archinamon.kernel.persefone.ui.dialog.AbstractPopup;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public final class RESTSignal {

    public static final class Builder {

        private transient AbstractAsyncTask  mCoherenceTask;

        public Builder() {}

        public final Builder bindCoherentTask(@Nonnull final AbstractAsyncTask task) {
            this.mCoherenceTask = task;
            return this;
        }

        public final Builder asDaemon() {
            this.mCoherenceTask.dispatchSpinner();
            return this;
        }

        public final Builder attachPopup(AbstractPopup popup) {
            this.mCoherenceTask.applySpinner(popup);
            return this;
        }

        public final Builder attachPopup(Class<? extends AbstractPopup> popup) {
            this.mCoherenceTask.applySpinner(popup);
            return this;
        }

        public final <Projection extends NetworkModel> Builder bindProjection(Projection model) {
            if (model != null) {
                model.packModel();
                this.mCoherenceTask.coherence(model.getPackage());
            }
            return this;
        }

        public final RESTSignal build() {
            return new RESTSignal(this);
        }
    }

    public static final String TAG = RESTSignal.class.getSimpleName();
    private transient AbstractAsyncTask  mTask;

    private RESTSignal(Builder config) {
        mTask = config.mCoherenceTask;
    }

    protected final void eval() {
        if (mTask.getStatus() != Status.RUNNING) mTask.execute();
    }
}
