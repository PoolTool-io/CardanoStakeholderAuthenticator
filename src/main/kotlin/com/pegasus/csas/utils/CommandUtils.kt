package com.pegasus.csas.utils

import java.util.*

fun execute(command: String, verbose: Boolean = false): String {
    return try {
        if (verbose) {
            println("Executing command: $command")
        }
        val proc = Runtime.getRuntime().exec(command)
        var lines = ""
        var error = ""
        Scanner(proc.inputStream).use {
            while (it.hasNextLine()) lines += it.nextLine() + "\n"
        }
        Scanner(proc.errorStream).use {
            while (it.hasNextLine()) error += it.nextLine() + "\n"
        }
        if (verbose) {
            println("Result: $lines")
        }
        if (verbose && error.isNotEmpty()) {
            println("Error: $error")
        }
        lines
    } catch (e: Exception) {
        if(verbose) {
            e.printStackTrace()
        }
        e.localizedMessage
    }
}

fun execute(command: Array<String>, verbose: Boolean = false): String {
    return try {
        if (verbose) {
            println("Executing command: $command")
        }
        val proc = Runtime.getRuntime().exec(command)
        var lines = ""
        var error = ""
        Scanner(proc.inputStream).use {
            while (it.hasNextLine()) lines += it.nextLine() + "\n"
        }
        Scanner(proc.errorStream).use {
            while (it.hasNextLine()) error += it.nextLine() + "\n"
        }
        if (verbose) {
            println("Result: $lines")
        }
        if (verbose && error.isNotEmpty()) {
            println("Error: $error")
        }
        lines
    } catch (e: Exception) {
        if(verbose) {
            e.printStackTrace()
        }
        e.localizedMessage
    }
}