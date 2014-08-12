package mobi.anoda.archinamon.kernel.persefone.network.async;

import java.util.EmptyStackException;
import java.util.Stack;
import mobi.anoda.archinamon.kernel.persefone.service.async.AsyncRequest;

public class AsyncTwister {

    final Stack<AsyncRequest> mCommands = new Stack<>();
    volatile long         mLastErrorTime;
    volatile AsyncRequest mLastCall;

    AsyncTwister(AsyncRequest... calls) {
        for (AsyncRequest call : calls) {
            mCommands.push(call);
        }
    }

    final synchronized AsyncRequest next() throws EmptyStackException, InterruptedException {
            if (mLastErrorTime + 300 > System.currentTimeMillis()) {
                throw new InterruptedException();
            }
            return this.mLastCall = mCommands.pop();
        }

        final synchronized AsyncRequest withLast() throws InterruptedException {
            long time = System.currentTimeMillis();
            if (mLastErrorTime + 500 > time) {
                throw new InterruptedException();
            }

            mLastErrorTime = time;
            return mCommands.push(mLastCall);
        }
    }