package com.nao20010128nao.StratumBreaker

import com.google.gson.*


fun List<JsonElement>.toJsonArray(): JsonArray = JsonArray().also { ja ->
    forEach {
        ja.add(it)
    }
}

fun String.toJsonElement(): JsonElement = JsonPrimitive(this)
fun Number.toJsonElement(): JsonElement = JsonPrimitive(this)
fun Boolean.toJsonElement(): JsonElement = JsonPrimitive(this)
fun Char.toJsonElement(): JsonElement = JsonPrimitive(this)

fun String?.toJsonElementOrNull(): JsonElement = this?.toJsonElement() ?: JsonNull.INSTANCE
fun Number?.toJsonElementOrNull(): JsonElement = this?.toJsonElement() ?: JsonNull.INSTANCE
fun Boolean?.toJsonElementOrNull(): JsonElement = this?.toJsonElement() ?: JsonNull.INSTANCE
fun Char?.toJsonElementOrNull(): JsonElement = this?.toJsonElement() ?: JsonNull.INSTANCE


fun jsonObjectOf(vararg pairs: Pair<String, JsonElement>): JsonObject =
        JsonObject().apply { pairs.forEach { add(it.first, it.second) } }

fun Map<String, JsonElement>.toJsonObject(): JsonObject = JsonObject().also { jo ->
    entries.forEach { jo.add(it.key, it.value) }
}

fun jsonArrayOf(vararg elements: JsonElement): JsonArray =
        JsonArray().apply { elements.forEach { add(it) } }

fun newGson(): Gson = Gson()
