Persefone
=========

Custom overhaul support helper library

=========

Some of usefull snipets performed in this lib:
  — Revamped Service binder structure and lifecycle to gain control over existing and running services;
  — Tips and frequently needed workaround with some Activity / Fragment / Service / BroadcastReceiver native features to reduce production code time and length;
  — Prepared custom Signal mechanism to observe and control all stages of remote AsyncTask lifecycle and produce custom exception/success events throw the SignalSequencer;
  — Thread-safe mechanism to bind and process sync/async Runnables to modify and reflect over UI thread via RunnableSequencer;
