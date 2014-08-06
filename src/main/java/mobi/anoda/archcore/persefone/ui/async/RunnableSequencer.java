package mobi.anoda.archcore.persefone.ui.async;

import android.os.Handler;
import android.os.Message;
import com.google.common.collect.ImmutableMultiset;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mobi.anoda.archcore.persefone.annotation.Implement;
import mobi.anoda.archcore.persefone.annotation.Sequence;
import mobi.anoda.archcore.persefone.annotation.SequenceTask;
import mobi.anoda.archcore.persefone.annotation.SequenceTask.Type;
import mobi.anoda.archcore.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archcore.persefone.utils.LogHelper;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RunnableSequencer {

    public static final class Builder {

        public static final String TAG = Builder.class.getSimpleName();
        private AbstractActivity      mActivity;
        private Set<FloatingRunnable> mTasks;
        private FloatingRunnable      mPreCompileTask;
        private FloatingRunnable      mPostCompileTask;
        private Mode                  mMode;

        public Builder(AbstractActivity context) {
            mTasks = new LinkedHashSet<>();
            mActivity = checkNotNull(context);
        }

        public Builder addSyncTask(Runnable task) {
            addTask(task, true);
            return this;
        }

        public Builder addAsyncTask(Runnable task) {
            addTask(task, false);
            return this;
        }

        public Builder setOnPreCompileSyncTask(Runnable task) {
            addOnPreCompileTask(task, true);
            return this;
        }

        public Builder setOnPreCompileAsyncTask(Runnable task) {
            addOnPreCompileTask(task, false);
            return this;
        }

        public Builder setOnPostCompileSyncTask(Runnable task) {
            addOnPostCompileTask(task, true);
            return this;
        }

        public Builder setOnPostCompileAsyncTask(Runnable task) {
            addOnPostCompileTask(task, false);
            return this;
        }

        public Builder parseSequence(@Nonnull final Object type) {
            Class objClass = type.getClass();
            Annotation[] annotations = objClass.getAnnotations();

            boolean isSequenceAnnotationPresent = false;
            for (Annotation annotation : annotations) {
                if (annotation instanceof Sequence)
                    isSequenceAnnotationPresent = true;
            }

            if (!isSequenceAnnotationPresent)
                throw new IllegalArgumentException("argument should be annotated as Sequence");

            Sequence seqAnnotation = (Sequence) objClass.getAnnotation(Sequence.class);
            if (objClass.isSynthetic() && !seqAnnotation.synthetic())
                throw new IllegalArgumentException("sequence class should be annotated as synthetic or avoid synth access");

            for (@Nonnull final Method method : objClass.getDeclaredMethods()) {
                SequenceTask sequenceTask = method.getAnnotation(SequenceTask.class);
                if (sequenceTask == null)
                    continue;

                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                final boolean isSync = sequenceTask.value() == Type.SYNC_UI;
                switch (sequenceTask.exec_order()) {
                    case PRE_COMPILE:
                        addOnPreCompileTask(new Runnable() {

                            @Implement
                            public void run() {
                                try {method.invoke(type);} catch (Exception e) {LogHelper.println_error(TAG, e);}
                            }
                        }, isSync);
                        break;
                    case POST_COMPILE:
                        addOnPostCompileTask(new Runnable() {

                            @Implement
                            public void run() {
                                try {method.invoke(type);} catch (Exception e) {LogHelper.println_error(TAG, e);}
                            }
                        }, isSync);
                        break;
                    case RUNTIME:
                        addTask(new Runnable() {

                            @Implement
                            public void run() {
                                try {method.invoke(type);} catch (Exception e) {LogHelper.println_error(TAG, e);}
                            }
                        }, isSync);
                        break;
                }
            }

            return this;
        }

        public RunnableSequencer build(Mode mode) {
            mMode = mode;
            return new RunnableSequencer(this);
        }

        private void addTask(Runnable task, boolean shouldRunInUiThread) {
            FloatingRunnable floatTask = FloatingRunnable.newInstance();
            if (shouldRunInUiThread) floatTask.markUiRunning();
            floatTask.define(checkNotNull(task));
            mTasks.add(floatTask);
        }

        private void addOnPreCompileTask(Runnable task, boolean shouldRunInUiThread) {
            FloatingRunnable floatTask = FloatingRunnable.newInstance();
            if (shouldRunInUiThread) floatTask.markUiRunning();
            floatTask.define(checkNotNull(task));
            mPreCompileTask = floatTask;
        }

        private void addOnPostCompileTask(Runnable task, boolean shouldRunInUiThread) {
            FloatingRunnable floatTask = FloatingRunnable.newInstance();
            if (shouldRunInUiThread) floatTask.markUiRunning();
            floatTask.define(checkNotNull(task));
            mPostCompileTask = floatTask;
        }
    }

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

        void preCompile(@Nonnull final FloatingRunnable task) {
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
                case MODE_INONEWAY:
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
                    break;
            }

            completeCompile();
        }

        void postCompile(@Nonnull final FloatingRunnable task) {
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

    private RunnableSequencer(Builder config) {
        mContext = config.mActivity;
        mTaskSet = ImmutableMultiset.copyOf(config.mTasks);
        mPreCompileTask = config.mPreCompileTask;
        mPostCompileTask = config.mPostCompileTask;
        mExecutingMode = config.mMode;
    }

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