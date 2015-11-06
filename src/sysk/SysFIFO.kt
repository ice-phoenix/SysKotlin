package sysk

import java.util.*

open class SysFifo<T> internal constructor(
        val capacity: Int, name: String, startValue: T, scheduler: SysScheduler, parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    private var counterSysFifoInputs: Int = 0

    protected val changeEvent = SysWait.Event("changeEvent", scheduler, this)

    protected var fifo: Queue<T> = LinkedList()

    var input: T = startValue
        set(value) {
            field = value;
        }

    var output: T = startValue
        private set

    val size: Int
        get() = fifo.size

    val empty: Boolean
        get() = fifo.isEmpty()

    val full: Boolean
        get() = fifo.size == capacity

    open var push: SysWireState = SysWireState.X
        set(value) = push()

    open var pop: SysWireState = SysWireState.X
        set(value) = pop()

    internal open fun pop() {
        if (!fifo.isEmpty()) fifo.remove()
        if (!fifo.isEmpty() && output != fifo.element()) {
            output = fifo.element()
            changeEvent.happens()
        }
    }

    internal open fun push() {
        if (!full) fifo.add(input)
        this.output = fifo.element()
    }

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun register(port: SysPort<*>) {
        if (port is SysFifoOutput<*>) ++counterSysFifoInputs
        if (counterSysFifoInputs > 1)
            throw IllegalStateException("SysFifo ${name} may have only one output port.")
    }

    override fun toString() = fifo.toString()
}

open class SysWireFifo internal constructor(
        capacity: Int, name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysFifo<SysWireState>(capacity, name, SysWireState.X, scheduler, parent) {

    val posEdgeEvent = SysWait.Event("posEdgeEvent", scheduler, this)

    val negEdgeEvent = SysWait.Event("negEdgeEvent", scheduler, this)

    val zero: Boolean
        get() = output.zero

    val one: Boolean
        get() = output.one

    val x: Boolean
        get() = output.x

    override fun pop() {
        val prevValue = this.output
        super.pop()
        if (prevValue.one && output.zero) {
            negEdgeEvent.happens()
        } else if (prevValue.zero && output.one) {
            posEdgeEvent.happens()
        }
    }
}

open class SysAsynchronousFifo<T> internal constructor(
        capacity: Int, name: String, startValue: T, scheduler: SysScheduler, parent: SysObject? = null
) : SysFifo<T>(capacity, name, startValue, scheduler, parent) {

    override var push: SysWireState = SysWireState.X
        get() = throw UnsupportedOperationException(
                "SysAsynchronousFifo $name: Read is not supported for push port.")
        set(value) {
            if (field.zero && value.one) push()
            field = value;
        }

    override var pop: SysWireState = SysWireState.X
        get() = throw UnsupportedOperationException(
                "SysAsynchronousFifo $name: Read is not supported for pop port.")
        set(value) {
            if (field.zero && value.one) pop()
            field = value;
        }
}

