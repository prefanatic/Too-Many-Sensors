package io.github.prefanatic.toomanysensors.extension

public fun Int.pow(to: Int) = Math.pow(this.toDouble(), to.toDouble()).toInt()
