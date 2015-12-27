package sysk.data

import org.junit.Test
import sysk.core.SysScheduler
import sysk.core.TimeUnit
import sysk.core.time

class SysClockedSignalTest {

    @Test
    fun test() {
        val scheduler = SysScheduler()
        val clock = SysClockedSignal("clock", time(20, TimeUnit.NS), scheduler)
        assert(clock.zero)
        scheduler.start(time(55, TimeUnit.NS))
        assert(clock.one)
        scheduler.start(time(115, TimeUnit.NS))
        assert(clock.one)
        scheduler.start(time(145, TimeUnit.NS))
        assert(clock.zero)
    }
}