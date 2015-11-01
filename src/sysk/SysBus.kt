package sysk

import java.util.*

abstract class SysBus<T> constructor(
        name: String, private val scheduler: SysScheduler, parent: SysObject? = null
) : SysInterface, SysObject(name, parent) {

    init {
        scheduler.register(this)
    }

    protected val signals: MutableList<SysSignal<T>> = ArrayList()

    protected final val changeEvent = SysWait.Event("changeEvent", scheduler, this)

    override val defaultEvent: SysWait.Event
        get() = changeEvent

    override fun register(port: SysPort<*>) {
    }

    open fun addWire(startValue: T) {
        signals.add(SysSignal<T>((signals.size).toString(), startValue, scheduler, this))
    }

    abstract fun set(value: T, index: Int, port: SysPort<*>)

    internal operator fun get(index: Int): T {
        if (index >= signals.size || index < 0) {
            throw IllegalArgumentException(
                    "SysBus $name: Wire with index $index is not found.")
        }
        return signals[index].value
    }

    internal abstract fun update()
}

open class SysWireBus constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysWireState>(name, scheduler, parent) {

    private val ports: MutableMap<SysPort<*>, MutableList<SysWireState>> = HashMap()

    override fun register(port: SysPort<*>) {
        val list = ArrayList<SysWireState>()
        for (i in signals.indices) list.add(SysWireState.Z)
        ports.put(port, list)
        for (i in signals.indices) update(i)
    }

    // ToDo: private addWire(startValue: T)
    fun addWire() {
        super.addWire(SysWireState.X)
        ports.forEach { it.value.add(SysWireState.Z) }
        if (!ports.isEmpty()) update(signals.size - 1)
    }

    override fun set(value: SysWireState, index: Int, port: SysPort<*>) {
        ports[port]!!.set(index, value)
        update(index)
    }

    private fun update(index: Int) {
        var value = SysWireState.Z
        ports.forEach { value = value.wiredAnd(it.value[index]) }
        signals[index].value = value;
    }

    override fun update() {
        signals.forEach { it.update() }
    }
}

open class SysPriorityBus<T> constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<SysPriorityValue<T>>(name, scheduler, parent) {

    private val priority: MutableList<Int> = ArrayList()

    override fun addWire(startValue: SysPriorityValue<T>) {
        super.addWire(startValue)
        priority.add(startValue.priority)
    }

    override fun set(value: SysPriorityValue<T>, index: Int, port: SysPort<*>) {
        if (this.priority[index] < value.priority) {
            this.priority[index] = value.priority
            signals[index].value = value
        }
    }

    override fun update() {
        for (i in priority.indices) {
            priority[i] = 0
            signals[i].update()
        }
    }
}

class SysPriorityValue<T> constructor(final val priority: Int, final val value: T) {}

open class SysFifoBus<T> constructor(
        name: String, scheduler: SysScheduler, parent: SysObject? = null
) : SysBus<T>(name, scheduler, parent) {

    private val fifo: MutableList<Queue<T>> = ArrayList()

    override fun addWire(startValue: T) {
        super.addWire(startValue)
        fifo.add(LinkedList())
    }

    override fun set(value: T, index: Int, port: SysPort<*>) {
        fifo[index].add(value);
    }

    override fun update() {
        for (i in signals.indices)
            if (!fifo[i].isEmpty()) {
                signals[i].value = fifo[i].element()
                fifo[i].remove()
            }
        signals.forEach { it.update() }
    }
}