package sysk

interface StageContainer {
    val stages: MutableList<Stage>

    var stageNumber: Int

    fun complete() = stageNumber >= stages.size

    fun wait(): SysWait

    fun stage(f: () -> Unit): Stage.Atomic {
        val result = Stage.Atomic {
            f()
            wait()
        }
        stages.add(result)
        return result
    }

    fun complexStage(init: Stage.Complex.() -> Unit): Stage.Complex {
        val result = Stage.Complex(wait(), linkedListOf())
        result.init()
        stages.add(result)
        return result
    }

    fun run(event: SysWait): SysWait {
        if (!complete()) {
            stages[stageNumber].let {
                val result = it.run(event)
                when (it) {
                    is Stage.Atomic -> stageNumber++
                    is Stage.Complex -> if (it.complete()) {
                        stageNumber++
                    }
                }
                return result
            }
        }
        else return SysWait.Never
    }
}

sealed class Stage {

    abstract fun run(event: SysWait): SysWait

    class Atomic internal constructor(private val f: () -> SysWait) : Stage() {
        override fun run(event: SysWait) = f()
    }

    class Complex internal constructor(
            private val sensitivities: SysWait,
            override val stages: MutableList<Stage>
    ) : Stage(), StageContainer {

        override fun wait() = sensitivities

        override var stageNumber = 0

        override fun run(event: SysWait) = super.run(event)
    }
}

class StagedFunction private constructor(
        override val stages: MutableList<Stage>,
        private var initStage: Stage.Atomic? = null,
        private var infiniteStage: Stage.Atomic? = null,
        sensitivities: SysWait = SysWait.Never
): SysFunction(sensitivities, initialize = true), StageContainer {
    internal constructor(sensitivities: SysWait = SysWait.Never):
            this(linkedListOf(), null, null, sensitivities)

    fun initStage(f: () -> Unit): Stage.Atomic {
        initStage = Stage.Atomic {
            f()
            wait()
        }
        return initStage!!
    }

    fun infiniteStage(f: () -> Unit): Stage.Atomic {
        infiniteStage = Stage.Atomic {
            f()
            wait()
        }
        return infiniteStage!!
    }

    private fun init(event: SysWait): SysWait {
        // BUG: return infiniteStage?.run() ?: SysWait.Never, see KT-10142
        if (initStage == null) return wait()
        return initStage!!.run(event)
    }

    private fun infinite(event: SysWait): SysWait {
        // BUG: return infiniteStage?.run() ?: SysWait.Never, see KT-10142
        if (infiniteStage == null) return SysWait.Never
        return infiniteStage!!.run(event)
    }

    override var stageNumber = 0

    override fun run(event: SysWait): SysWait {
        if (event == SysWait.Initialize) {
            return init(event)
        }
        else if (!complete()) {
            return super.run(event)
        }
        else {
            return infinite(event)
        }
    }
}
