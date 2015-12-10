package samples.k580

import samples.Register
import sysk.*

class ByteRegister(name: String, parent: SysModule):
        Register<SysInteger>(name, @Width(8) SysInteger.uninitialized(8), parent)

// Register file: A, B + C, D + E, H + L
class RegisterFile(name: String, parent: SysModule): SysModule(name, parent) {

    val clk = wireInput("clk")

    val we = wireInput("we")

    val wrReg = input<@Width(3) SysInteger>("wrReg")

    val rdReg = output<@Width(3) SysInteger>("rdReg")

    val wrValue = input<@Width(16) SysInteger>("wrValue")

    val byteValue = output<@Width(8) SysInteger>("byteValue")

    val shortValue = output<@Width(16) SysInteger>("shortValue")

    private val ad = byteSignal("ad")
    private val aq = byteSignal("aq")
    private val bd = byteSignal("bd")
    private val bq = byteSignal("bq")
    private val cd = byteSignal("cd")
    private val cq = byteSignal("cq")
    private val dd = byteSignal("dd")
    private val dq = byteSignal("dq")
    private val ed = byteSignal("ed")
    private val eq = byteSignal("eq")
    private val hd = byteSignal("hd")
    private val hq = byteSignal("hq")
    private val ld = byteSignal("ld")
    private val lq = byteSignal("lq")

    private val ae = wireSignal("ae")
    private val be = wireSignal("be")
    private val ce = wireSignal("ce")
    private val de = wireSignal("de")
    private val ee = wireSignal("ee")
    private val he = wireSignal("he")
    private val le = wireSignal("le")

    private fun byteRegister(name: String) = ByteRegister(name, this)
    private fun byteSignal(name: String) = signal(name, SysInteger.uninitialized(8))

    private val a = byteRegister("a")
    private val b = byteRegister("b")
    private val c = byteRegister("c")
    private val d = byteRegister("d")
    private val e = byteRegister("e")
    private val h = byteRegister("h")
    private val l = byteRegister("l")

    init {
        bind(a.d to ad, b.d to bd, c.d to cd, d.d to dd, e.d to ed, h.d to hd, l.d to ld)
        bind(a.q to aq, b.q to bq, c.q to cq, d.q to dq, e.q to eq, h.q to hq, l.q to lq)
        bindPorts(a.clk to clk, b.clk to clk, c.clk to clk, d.clk to clk, e.clk to clk, h.clk to clk, l.clk to clk)
        bind(a.en to ae, b.en to be, c.en to ce, d.en to de, e.en to ee, h.en to he, l.en to le)

        function(SysWait.OneOf(we.defaultEvent, wrReg.defaultEvent), initialize = true) {
            ae.reset()
            be.reset()
            ce.reset()
            de.reset()
            ee.reset()
            he.reset()
            le.reset()
            if (it !is SysWait.Initialize) {
                if (we.one) {
                    when (wrReg.value.value.toInt()) {
                        0 -> be.set()
                        1 -> ce.set()
                        2 -> de.set()
                        3 -> ee.set()
                        4 -> he.set()
                        5 -> le.set()
                        7 -> ae.set()
                    }
                }
                byteValue.value = when (rdReg.value.value.toInt()) {
                    0 -> bq.value
                    1 -> cq.value
                    2 -> dq.value
                    3 -> eq.value
                    4 -> hq.value
                    5 -> lq.value
                    7 -> aq.value
                    else -> SysInteger.uninitialized(8)
                }
                shortValue.value = when (rdReg.value.value.toInt()) {
                    0 -> bq.value cat cq.value
                    1 -> dq.value cat eq.value
                    2 -> hq.value cat lq.value
                    else -> SysInteger.uninitialized(16)
                }
            }
            val byte = wrValue.value.truncate(8)
            ad.value = byte
            bd.value = byte
            cd.value = byte
            dd.value = byte
            ed.value = byte
            hd.value = byte
            ld.value = byte
        }
    }
}