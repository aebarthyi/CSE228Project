package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RAMTester extends AnyFlatSpec with ChiselScalatestTester {
    def doWriteTest(dut: RAM, in1: Double, in2: Double, w_addr1: Int, w_addr2: Int) : Unit = {
        dut.io.enable.poke(true.B)
        dut.io.read.poke(false.B)
        dut.io.in_data1.poke(in1.F(32.W, 8.BP))
        dut.io.in_data2.poke(in2.F(32.W, 8.BP))
        dut.io.addr1.poke(w_addr1.U)
        dut.io.addr2.poke(w_addr2.U)
        dut.clock.step()
        // TODO: could add extra clock step for safety?
    }

    def doReadTest(dut: RAM, r_exp_out1: Double, r_exp_out2: Double, r_addr1: Int, r_addr2: Int) : Unit = {
        dut.io.enable.poke(true.B)
        dut.io.read.poke(true.B)
        dut.io.addr1.poke(r_addr1.U)
        dut.io.addr2.poke(r_addr2.U)
        dut.clock.step()
        dut.io.out1.expect(r_exp_out1.F(32.W, 8.BP))
        dut.io.out2.expect(r_exp_out2.F(32.W, 8.BP))
        dut.clock.step()
    }
    
    behavior of "RAM"
    it should "correctly writes two floats to RAM then reads them back" in {
        test(new RAM(8)) { dut =>
            // write signal
            doWriteTest(dut, 5.0, 7.0, 0, 1)

            // read signal
            doReadTest(dut, 5.0, 7.0, 0, 1)
        }
    }

}