package com.krystal.goddesslifestyle.utils.filedownloader

interface ProgressListener {
    fun update(
        bytesRead: Long,
        contentLength: Long,
        done: Boolean
    )
}