package samples

import sysk.*

public class DFF (name: String, parent: SysModule): SysModule(name, parent) {

    val d = wireInput("d")
    val clk = wireInput("clk")

    private var state = SysWireState.X
    val q = output<SysWireState>("q")

    private val f: SysTriggeredFunction = triggeredFunction({

        if (d.one) state = SysWireState.ONE
        else if (d.zero) state = SysWireState.ZERO
        q.value = state
        f.wait()
    }, clk, initialize = false)
}

