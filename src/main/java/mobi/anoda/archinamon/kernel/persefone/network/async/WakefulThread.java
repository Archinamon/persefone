/***
 Copyright (c) 2010 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package mobi.anoda.archinamon.kernel.persefone.network.async;

import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;

/**
 * HandlerThread that unlocks itself when its work is complete. Used in conjunction with a WakeLock to accomplish this end.
 */
public class WakefulThread extends HandlerThread {

    private PowerManager.WakeLock mWakeLock = null;
    private final Runnable mOpTask;

    /**
     * Constructor
     *
     * @param lock Already-acquired WakeLock to be released when work done
     * @param name Name to supply to HandlerThread
     */
    WakefulThread(PowerManager.WakeLock lock, Runnable task, String name) {
        super(name);

        this.mWakeLock = lock;
        this.mOpTask = task;
    }

    /**
     * Override this method if you want to do something before looping begins
     */
    protected void onPreExecute() {
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    /**
     * Override this method if you want to do something before the WakeLock is released when the thread's work is done or if an unhandled exception is raised while the thread runs
     */
    protected void onPostExecute() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        if (!mWakeLock.isHeld()) {
            onUnlocked();
        }
    }

    /**
     * Override this method if you want to do something when the WakeLock is fully unlocked (e.g., shut down a service)
     */
    protected void onUnlocked() {
        // no-op by default
    }

    @Override
    protected void onLooperPrepared() {
        try {
            onPreExecute();
        } catch (RuntimeException e) {
            Log.e("WakefulThread", "Exception onLooperPrepared()", e);
            onPostExecute();
            throw (e);
        }
    }

    @Override
    public void run() {
        try {
            super.run();
            mOpTask.run();
        } finally {
            onPostExecute();
        }
    }
}