package com.nao20010128nao.StratumBreaker

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.Writer
import java.net.Socket
import kotlin.concurrent.thread

class JsonRpc(val ip: String, val port: Int) {
    private var socket: Socket? = null
    private var writer: Writer? = null
    private var reader: BufferedReader? = null
    private val gson = newGson()
    private var thread: Thread? = null
    private val handlers: MutableList<Handler> = mutableListOf()

    val isConnected: Boolean
        get() = socket?.isConnected == true

    fun startLoop() {
        require(thread == null || !thread!!.isAlive)
        thread = thread {
            val localSocket = Socket(ip, port)
            socket = localSocket
            val localReader = localSocket.getInputStream().reader().buffered(2)
            reader = localReader
            writer = localSocket.getOutputStream().writer()
            while (isConnected) {
                try {
                    val line = localReader.readLine()!!
                    println(">>> $line")
                    val json = try {
                        gson.fromJson(line, JsonObject::class.java)
                    } catch (e: Throwable) {
                        null
                    } ?: continue
                    val result = if (json.has("error") && !json["error"].isJsonNull) {
                        gson.fromJson(json["error"], Response.Error::class.java)
                    } else if (json.has("result") && !json["result"].isJsonNull) {
                        Response.Success(json["result"])
                    } else if (json.has("params") && !json["params"].isJsonNull) {
                        Response.Notify(json["params"].asJsonArray, json["method"].asString)
                    } else {
                        Response.NoResult
                    }
                    val id = json["id"].run {
                        if (isJsonNull) null else asInt
                    }
                    handlers.forEach {
                        try {
                            it.onData(id, result)
                        } catch (e: Throwable) {
                        }
                    }
                } catch (e: Throwable) {
                }
            }
        }
    }

    fun send(id: Int?, method: String, data: JsonArray) {
        require(isConnected)
        val payload = gson.toJson(jsonObjectOf(
                "jsonrpc" to "2.0".toJsonElement(),
                "id" to id.toJsonElementOrNull(),
                "method" to method.toJsonElement(),
                "params" to data
        ))
        println("<<< $payload")
        synchronized(writer!!) {
            writer!!.also {
                it.write(payload + "\n")
                it.flush()
            }
        }
    }

    fun shutSocket() {
        socket?.close()
        socket = null
    }

    fun add(handler: Handler) {
        handlers.add(handler)
    }

    fun remove(handler: Handler) {
        handlers.remove(handler)
    }

    interface Handler {
        fun onData(id: Int?, response: Response)
    }

    sealed class Response {
        data class Success(val content: JsonElement) : Response()

        data class Error(val code: Int, val message: String) : Response()

        data class Notify(val content: JsonArray, val method: String) : Response()

        object NoResult : Response()
    }
}
