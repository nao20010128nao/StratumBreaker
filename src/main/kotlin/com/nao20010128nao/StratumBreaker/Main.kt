package com.nao20010128nao.StratumBreaker

import joptsimple.OptionParser

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val spec = OptionParser()
        spec.accepts("url").withRequiredArg()
                .withValuesConvertedBy(Require { it.startsWith("stratum+tcp://") })
        spec.accepts("user").withRequiredArg()
        spec.accepts("pass").withRequiredArg()
        spec.accepts("threads").withRequiredArg()
                .withValuesConvertedBy(ParseInt).defaultsTo(4)
    }
}