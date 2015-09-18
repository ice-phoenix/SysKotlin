package samples

import sysk.*

class JKFF(name: String, parent: SysModule? = null): SysModule(name, parent) {

    val j   = wireInput("j")
    val k   = wireInput("k")
    val clk = wireInput("clk")

    private var state = SysWireState.ZERO
    val q = output<SysWireState>("q")

    private val f: SysTriggeredFunction = triggeredFunction({
        println("${SysScheduler.currentTime}: j = ${j.value} k = ${k.value} state = $state")
        if (j.one && state.zero) state = SysWireState.ONE
        else if (k.one && state.one) state = SysWireState.ZERO
        q.value = state
        f.wait()
    }, clk, initialize = false)
}

private class Testbench(name: String): SysModule(name) {

    val j = output<SysWireState>("j")
    val k = output<SysWireState>("k")

    val clk = wireInput("clk")
    val q   = wireInput("q")

    private var counter = 0

    private val f: SysTriggeredFunction = triggeredFunction({
        if (it) {
            j.value = SysWireState.ZERO
            k.value = SysWireState.ZERO
        }
        else {
            println("${SysScheduler.currentTime}: q = ${q.value} counter = $counter")
            when (counter) {
                0 -> {
                    assert(q.zero) { "q should be false at the beginning" }
                    println("ZERO")
                }
                1 -> {
                    assert(q.zero) { "q should be false after q = true and JK = 11" }
                    // All changes at clock N are received at clock N+1 and processed at clock N+2
                    j.value = SysWireState.ONE
                    println("ONE")
                }
                2 -> {
                    assert(q.zero) { "q should be false after q = false and JK = 00" }
                    j.value = SysWireState.ZERO
                    println("TWO")
                }
                3 -> {
                    assert(q.one) { "q should be true after JK = 10" }
                    k.value = SysWireState.ONE
                    println("THREE")
                }
                4 -> {
                    assert(q.one) { "q should be true after q = true and JK = 00" }
                    j.value = SysWireState.ONE
                    println("FOUR")
                }
                5 -> {
                    assert(q.zero) { "q should be false after JK = 01" }
                    println("FIVE")
                }
                6 -> {
                    assert(q.one) { "q should be true after q = false and JK = 11" }
                    j.value = SysWireState.ZERO
                    k.value = SysWireState.ZERO
                    println("SIX")
                }
            }
            counter++
            if (counter > 6) counter = 1
        }
        f.wait()
    }, clk)
}

fun main(args: Array<String>) {
    val j = SysWireSignal("j", SysWireState.ZERO)
    val k = SysWireSignal("k", SysWireState.ZERO)
    val clk = SysClockedSignal("clk", time(20, TimeUnit.NS))
    val q = SysWireSignal("q", SysWireState.ZERO)

    val ff = JKFF("my")
    val tb = Testbench("your")

    bind(ff.j to j, ff.k to k, ff.clk to clk, tb.clk to clk, tb.q to q)
    bind(ff.q to q, tb.j to j, tb.k to k)

    SysScheduler.start(time(1, TimeUnit.US))
}
