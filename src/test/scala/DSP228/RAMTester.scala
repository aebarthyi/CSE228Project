package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RAMTester extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "RAM"
    it should "correctly have output valid false" in {
        test(new RAM(8)) { dut =>
            dut.io.read.valid.poke(false.B)
            dut.io.read.bits.poke(true.B)
            dut.io.in_data1.poke(0.F(32.W, 8.BP))
            dut.io.in_data2.poke(0.F(32.W, 8.BP))
            dut.io.addr1.poke(0.U)
            dut.io.addr2.poke(0.U)

            dut.io.out_valid.expect(false.B)
            dut.io.out1.expect(0.F(32.W, 8.BP))
            dut.io.out2.expect(0.F(32.W, 8.BP))
        }
    }

}