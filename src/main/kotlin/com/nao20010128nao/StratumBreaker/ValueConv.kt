package com.nao20010128nao.StratumBreaker

import joptsimple.ValueConverter

object ParseInt : ValueConverter<Int> {
    override fun convert(value: String): Int = value.toInt()

    override fun valueType(): Class<out Int> = Int::class.java

    override fun valuePattern(): String? = null
}

class Require(
        private val pattern: String? = null,
        private val predicate: (String) -> Boolean
) : ValueConverter<String> {
    override fun convert(value: String): String = value.also {
        require(predicate(it))
    }

    override fun valueType(): Class<out String> = String::class.java

    override fun valuePattern(): String? = pattern
}