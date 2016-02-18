package ru.spbstu.sysk.data

import java.math.BigInteger

fun Number.longOrNull() = this as? Long

fun Number.bigInteger() = BigInteger("$this")

private fun Number.op(
        arg: Number,
        longOp: (Long, Long) -> Long,
        bigOp: (BigInteger, BigInteger) -> BigInteger
): Number =
        longOrNull()?.let { x -> arg.longOrNull()?.let { y -> longOp(x, y) } }
        ?: bigOp(bigInteger(), arg.bigInteger())

private fun Number.op2(
        arg: Number,
        longOp: Long.(Long) -> Long,
        bigOp: BigInteger.(BigInteger) -> BigInteger
): Number =
        longOrNull()?.let { x -> arg.longOrNull()?.let { y -> x.longOp(y) } }
                ?: bigInteger().bigOp(arg.bigInteger())


abstract class BaseInteger(val width: Int, open val value: Number) {

    /** Adds arg to this integer, with result width is maximum of argument's widths */
    operator fun plus(arg: BaseInteger): BaseInteger {
        val resWidth = Math.max(width, arg.width);
        return create(resWidth, value.op(arg.value, { x, y -> x + y}, { x, y -> x + y }))
    }

    companion object {
        fun create(width: Int, value: Number): BaseInteger =
                if (width <= SysInteger.MAX_WIDTH) SysInteger(width, value.toLong())
                else SysBigInteger(width, value.bigInteger())
    }

}