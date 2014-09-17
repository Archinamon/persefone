package mobi.anoda.archinamon.kernel.persefone.ui.async;

import android.os.Handler;
import android.os.Message;
import com.google.common.collect.ImmutableMultiset;
import java.util.Iterator;
import android.support.annotation.NonNull;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;

final class RunnableSequencerImpl implements ISequencer {

    private static final int TASK_PRE_COMPILE  = 0x0016;
    private static final int TASK_DO_COMPILE   = 0x0024;
    private static final int TASK_POST_COMPILE = 0x0032;
    private AbstractActivity                    mContext;
    private ImmutableMultiset<FloatingRunnable> mTaskSet;
    private FloatingRunnable                    mPreCompileTask;
    private FloatingRunnable                    mPostCompileTask;
    private Mode                                mExecutingMode;

    final Handler mProcessor = new Handler() {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case TASK_PRE_COMPILE:
                    assert msg.obj != null;
                    final FloatingRunnable actionPreCompile = ((FloatingRunnable) msg.obj);
                    preCompile(actionPreCompile);

                    break;
                case TASK_DO_COMPILE:
                    compile();
                    break;
                case TASK_POST_COMPILE:
                    assert msg.obj != null;
                    final FloatingRunnable actionPostCompile = ((FloatingRunnable) msg.obj);
                    postCompile(actionPostCompile);

                    break;
            }
        }

        void preCompile(@NonNull final FloatingRunnable task) {
            Runnable workTask = new Runnable() {

                @Implement
                public void run() {
                    task.run();
                    mProcessor.sendEmptyMessage(TASK_DO_COMPILE);
                }
            };

            if (task.isUiRunning()) {
                mContext.runOnUiThread(workTask);
            } else {
                runOnSeparateThread(workTask);
            }
        }

        void compile() {
            switch (mExecutingMode) {
                case MODE_COHERENCE:
                    final Iterator<FloatingRunnable> iterator = mTaskSet.iterator();
                    doCoherenceStep(iterator);
                    break;
                case MODE_ONEWAY:
                    for (final FloatingRunnable task : mTaskSet) {
                        if (task.isUiRunning()) {
                            mContext.runOnUiThread(new Runnable() {

                                @Implement
                                public void run() {
                                    task.run();
                                }
                            });
                        } else {
                            runOnSeparateThread(task);
                        }
                    }

                    completeCompile();
                    break;
            }
        }

        void postCompile(@NonNull final FloatingRunnable task) {
            if (task.isUiRunning()) {
                mContext.runOnUiThread(new Runnable() {

                    @Implement
                    public void run() {
                        task.run();
                    }
                });
            } else {
                runOnSeparateThread(task);
            }
        }

        private void completeCompile() {
            if (mPostCompileTask != null) {
                Message message = mProcessor.obtainMessage();
                message.what = TASK_POST_COMPILE;
                message.obj = mPostCompileTask;

                mProcessor.sendMessage(message);
            }
        }

        private void doCoherenceStep(final Iterator<FloatingRunnable> iterator) {
            if (iterator.hasNext()) {
                final FloatingRunnable task = iterator.next();
                task.mCallback = new ISentenceCallback() {

                    @Implement
                    public void onComplete(long sentenceId) {
                        doCoherenceStep(iterator);
                    }
                };

                if (task.isUiRunning()) {
                    mContext.runOnUiThread(new Runnable() {

                        @Implement
                        public void run() {
                            task.run();
                        }
                    });
                } else {
                    runOnSeparateThread(task);
                }
            } else {
                completeCompile();
            }
        }

        private void runOnSeparateThread(Runnable task) {
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.setUncaughtExceptionHandler(Thread.currentThread().getUncaughtExceptionHandler());
            thread.start();
        }
    };

    /*package_local*/ RunnableSequencerImpl(SequenceBuilder config) {
        mContext = config.mActivity;
        mTaskSet = ImmutableMultiset.copyOf(config.mTasks);
        mPreCompileTask = config.mPreCompileTask;
        mPostCompileTask = config.mPostCompileTask;
        mExecutingMode = config.mMode;
    }

    @Implement
    public final void exec() {
        if (mPreCompileTask != null) {
            Message msg = mProcessor.obtainMessage();
            msg.what = TASK_PRE_COMPILE;
            msg.obj = mPreCompileTask;

            mProcessor.sendMessage(msg);
        } else {
            mProcessor.sendEmptyMessage(TASK_DO_COMPILE);
        }
    }
}