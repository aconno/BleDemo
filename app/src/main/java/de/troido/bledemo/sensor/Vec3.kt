package de.troido.bledemo.sensor

data class Vec3<out T>(val x: T, val y: T, val z: T) {
    fun <U> map(f: (T) -> U): Vec3<U> = Vec3(f(x), f(y), f(z))
}
