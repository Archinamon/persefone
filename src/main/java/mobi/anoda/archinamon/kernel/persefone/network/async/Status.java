package mobi.anoda.archinamon.kernel.persefone.network.async;

/**
     * Indicates the current status of the task. Each status will be set only once
     * during the lifetime of a task.
     */
public enum Status {
    /**
     * Indicates that the task has not been executed yet.
     */
    PENDING,
    /**
     * Indicates that the task is running.
     */
    RUNNING,
    /**
     * Indicates that {@link CoreAsyncTask#onPostExecute} has finished.
     */
    FINISHED
}