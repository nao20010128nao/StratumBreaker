package com.nao20010128nao.StratumBreaker

import com.google.gson.JsonArray
import com.google.gson.JsonElement


fun Any.wait() {
    (this as java.lang.Object).wait()
}

fun Any.notify() {
    (this as java.lang.Object).notifyAll()
}


fun JsonRpc.waitNext(): Pair<Int?, JsonRpc.Response> {
    val lock = Any()
    var data: Pair<Int?, JsonRpc.Response>? = null
    val handler = object : JsonRpc.Handler {
        override fun onData(id: Int?, response: JsonRpc.Response) {
            data = id to response
            synchronized(lock) {
                lock.notify()
            }
        }
    }
    add(handler)
    synchronized(lock) {
        lock.wait()
    }
    remove(handler)
    return data!!
}

fun emptyJsonArray(): JsonArray = JsonArray()

fun JsonArray.toList():List<JsonElement> = asSequence().toList()
