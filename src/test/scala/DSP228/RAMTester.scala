package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RAMTester extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "RAM"
    it should "correctly writes two floats to RAM then reads them back" in {
        test(new RAM(8)) { dut =>
            // enable
            dut.io.enable.poke(true.B)
            
            // write signal
            dut.io.read.poke(false.B)
            dut.io.in_data1.poke(5.F(32.W, 8.BP))
            dut.io.in_data2.poke(7.F(32.W, 8.BP))
            dut.io.addr1.poke(0.U)
            dut.io.addr2.poke(1.U)
            dut.clock.step(2)

            // read signal
            dut.io.read.poke(true.B)
            dut.io.addr1.poke(0.U)
            dut.io.addr2.poke(1.U)
            dut.clock.step()
            dut.io.out1.expect(5.F(32.W, 8.BP))
            dut.io.out2.expect(7.F(32.W, 8.BP))
            dut.clock.step()
        }
    }

}