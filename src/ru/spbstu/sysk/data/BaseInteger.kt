package ru.spbstu.sysk.data

import java.math.BigInteger

fun Number.longOrNull() = this as? Long

fun Number.bigInteger() = BigInteger("$this")

fun Number.add(arg: Number): Number =
        longOrNull()?.let { arg.longOrNull()?.plus(it) } ?: bigInteger() + arg.bigInteger()

abstract class BaseInteger(val width: Int, open val value: Number) {

    /** Adds arg to this integer, with result width is maximum of argument's widths */
    operator fun plus(arg: BaseInteger): BaseInteger {
        val resWidth = Math.max(width, arg.width);
        return create(resWidth, value.add(arg.value))
    }

    companion object {
        fun create(width: Int, value: Number): BaseInteger =
                if (width <= SysInteger.MAX_WIDTH) SysInteger(width, value.toLong())
                else SysBigInteger(width, value.bigInteger())
    }

}