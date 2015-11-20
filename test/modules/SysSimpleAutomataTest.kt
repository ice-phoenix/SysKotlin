package modules

import org.junit.Test
import sysk.SysTopModule
import sysk.SysWireState
import sysk.bind

class SysSimpleAutomataTest {

    class SysLatchStagedTester : SysTopModule() {
        val dut = LatchTriggerMoore("not", this)

        val x = wireSignal("x")
        val y = wireSignal("y")

        init {
            bind(dut.x to x)
            bind(dut.y to y)
        }

        init {
            stagedFunction(sensitivities = y.defaultEvent) {
                stage {
                    assert(y.x)
                    x.value = SysWireState.ZERO
                }
                stage {
                    assert(y.x)
                    x.value = SysWireState.ONE
                }
                stage {
                    assert(y.zero)
                    x.value = SysWireState.ZERO
                }
                stage {
                    assert(y.one)
                    x.value = SysWireState.ONE
                }
                stage {
                    assert(y.zero)
                }
                stage {
                    assert(y.one)
                    scheduler.stop()
                }
            }
        }
    }

    class SysLatchTester : SysTopModule() {

        val dut = LatchTriggerMoore("not", this)

        val x = wireSignal("x")
        val y = wireSignal("y")

        init {
            bind(dut.x to x)
            bind(dut.y to y)
        }

        val counter = 0

        init {
            function(sensitivities = y.defaultEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.x)
                        x.value = SysWireState.ZERO
                    }
                    1 -> {
                        assert(y.x)
                        x.value = SysWireState.ONE
                    }
                    2 -> {
                        assert(y.zero)
                        x.value = SysWireState.ZERO
                    }
                    3 -> {
                        assert(y.one)
                        x.value = SysWireState.ONE
                    }
                    4 -> {
                        assert(y.zero)
                    }
                    5 -> {
                        assert(y.one)
                        scheduler.stop()
                    }
                }
            }
        }
    }

    @Test
    fun latchStagedTest() {
        SysLatchStagedTester().start()
    }

    @Test
    fun latchTest() {
        SysLatchTester().start()
    }

    class SysCountTester : SysTopModule() {

        val dut = CountTriggerMoore("not", this)

        val x = wireSignal("x")
        val y = wireSignal("y")

        init {
            bind(dut.x to x)
            bind(dut.y to y)
        }

        val counter = 0

        init {
            function(sensitivities = y.defaultEvent, initialize = false) {
                when (counter) {
                    0 -> {
                        assert(y.zero)
                        x.value = SysWireState.ZERO
                    }
                    1 -> {
                        assert(y.zero)
                        x.value = SysWireState.ONE
                    }
                    2 -> {
                        assert(y.zero)
                    }
                    3 -> {
                        assert(y.one)
                        x.value = SysWireState.ZERO
                    }
                    4 -> {
                        assert(y.zero)
                    }
                    5 -> {
                        assert(y.zero)
                        scheduler.stop()
                    }
                }
            }
        }
    }

    @Test
    fun countTest() {
        SysCountTester().start()
    }
}