package samples

import org.junit.Test
import sysk.*

class TFFTest {

    private class Testbench(name: String, parent: SysModule): SysModule(name, parent) {

        val t = output<SysWireState>("t")

        val clk = wireInput("clk")
        val q   = wireInput("q")

        private var counter = 0
        private var phase = 0

        init {
            triggeredFunction(clk) {
                if (it is SysWait.Initialize) {
                    t.value = SysWireState.ZERO
                } else {
                    when (counter) {
                        0 -> {
                            assert(q.zero) { "q should be false at the beginning" }
                        }
                        1 -> {
                            assert(q.zero) { "q should be false after q = true and T = 1" }
                            // All changes at clock N are received at clock N+1 and processed at clock N+2
                            t.value = SysWireState.ONE
                        }
                        2 -> {
                            assert(q.zero) { "q should be false after q = false and T = 0" }
                            t.value = SysWireState.ZERO
                        }
                        3 -> {
                            assert(q.one) { "q should be true after T = 1" }
                            t.value = SysWireState.ONE
                        }
                        4 -> {
                            assert(q.one) { "q should be true after q = true and T = 0" }
                            t.value = SysWireState.ZERO
                        }
                        5 -> {
                            assert(q.zero) { "q should be false after q = true and T = 1" }
                        }
                    }
                    counter++
                    if (counter > 5) {
                        counter = 1
                        phase++
                        if (phase == 4) {
                            scheduler.stop()
                        }
                    }
                }
            }
        }
    }

    private class Top : SysTopModule("top", SysScheduler()) {
        val t = signal("t", SysWireState.ZERO)

        val clk = clockedSignal("clk", time(20, TimeUnit.NS))
        val q = signal("q", SysWireState.ZERO)

        val ff = TFF("my", this)
        private val tb = Testbench("your", this)

        init {
            bind(ff.t to t, ff.clk to clk, tb.clk to clk, tb.q to q)
            bind(ff.q to q, tb.t to t)
        }
    }

    @Test
    fun test() {
        Top().start()
    }
}
