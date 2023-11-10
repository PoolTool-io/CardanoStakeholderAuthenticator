package com.pegasus.csas.authenticator

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.*

class DebugPrinter {

//    private val mutex = Mutex()
//    private val file: File

    init {
//        val fileName = "debug.txt"
//        file = File(fileName)
//        file.createNewFile()
    }

    suspend fun print(text: String) {
//        mutex.withLock {
            println("DebugPrinter: $text")
//            file.appendText("$text\n")
//        }
    }

    suspend fun print(timestamp: Long, text: String) {
//        mutex.withLock {
            println("DebugPrinter: ${(Date().time - timestamp) / 1000} seconds - $text")
//            file.appendText("${(Date().time - timestamp) / 1000} seconds - $text\n")
//        }
    }



}