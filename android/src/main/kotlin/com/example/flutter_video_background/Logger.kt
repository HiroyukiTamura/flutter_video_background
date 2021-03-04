package com.example.flutter_video_background

interface LoggerListener {
    fun d(tag: String, msg: Any?)

    fun e(tag: String, msg: String?)

    fun e(tag: String, thr: Throwable)
}

class Logger {

    private var loggerListener: LoggerListener? = null

    fun d(tag: String, msg: Any?) = loggerListener?.d(tag, msg)
    
    fun e(tag: String, msg: String?) = loggerListener?.e(tag, msg)
    
    fun e(tag: String, thr: Throwable) = loggerListener?.e(tag, thr)

    fun initialize(loggerListener: LoggerListener) {
        this.loggerListener = loggerListener
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var mInstance: Logger? = null

        val instance: Logger
            get() =
                mInstance ?: synchronized(this) {
                    mInstance ?: Logger()
                            .also { mInstance = it }
                }
    }
}