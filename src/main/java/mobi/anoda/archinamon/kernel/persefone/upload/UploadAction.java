package mobi.anoda.archinamon.kernel.persefone.upload;

import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;

public enum UploadAction implements Broadcastable {

    BIND(".EXTERN.BIND"),
    POST_STATUS(".EXTERN<POST_STATUS>");

    private final String mAction;

    private UploadAction(String action) {
        mAction = BASE + action;
    }

    @Implement
    public String getAction() {
        return mAction;
    }

    @Implement
    public boolean isEqual(String with) {
        return mAction.equalsIgnoreCase(with);
    }
}