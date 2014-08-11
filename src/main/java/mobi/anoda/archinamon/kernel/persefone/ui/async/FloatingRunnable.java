package mobi.anoda.archinamon.kernel.persefone.ui.async;

import java.util.Random;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

final class FloatingRunnable implements Runnable {

    final long _ID = new Random().nextInt(((Object) this).hashCode());
    boolean isUiRunning = false;
    ISentenceCallback mCallback;
    Runnable mLinkedTask;

    static FloatingRunnable newInstance() {
        return new FloatingRunnable();
    }

    void define(Runnable task) {
        mLinkedTask = task;
    }

    boolean isUiRunning() {
        return isUiRunning;
    }

    void markUiRunning() {
        isUiRunning = true;
    }

    @Implement
    public void run() {
        if (mLinkedTask == null) throw new LinkageError("You should use define() method to link executable task");
        mLinkedTask.run();
        if (mCallback != null) mCallback.onComplete(_ID);
    }
}