package de.troido.bledemo.util

/**
 * Returns a [List]`<`[T]`>` if no elements of `this` ([List]`<`[T?]`>`) are `null`, otherwise
 * returns `null`.
 */
internal inline fun <T : Any> List<T?>.sequence(): List<T>? =
        if (any { it == null }) null else mapNotNull { it }
