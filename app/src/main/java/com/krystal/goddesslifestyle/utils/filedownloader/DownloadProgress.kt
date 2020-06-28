package com.krystal.goddesslifestyle.utils.filedownloader

class DownloadProgress<DATA> {
    val progress: Float
    val data: DATA?

    constructor(progress: Float) {
        this.progress = progress
        data = null
    }

    constructor(data: DATA) {
        progress = 1f
        this.data = data
    }

    val isDone: Boolean
        get() = data != null

}