package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RAMTester extends AnyFlatSpec with ChiselScalatestTester {
    def doWriteTest(dut: RAM, width: Int, real1: Double, real2: Double, imag1: Double, imag2: Double, w_addr1: Int, w_addr2: Int) : Unit = {
        dut.io.enable.poke(true.B)
        dut.io.read.poke(false.B)
        dut.io.realIn1.poke(real1.F(width.W, (width-2).BP))
        dut.io.realIn2.poke(real2.F(width.W, (width-2).BP))
        dut.io.imagIn1.poke(imag1.F(width.W, (width-2).BP))
        dut.io.imagIn2.poke(imag2.F(width.W, (width-2).BP))
        dut.io.addr1.poke(w_addr1.U)
        dut.io.addr2.poke(w_addr2.U)
        dut.clock.step()
        // TODO: could add extra clock step for safety?
    }

    def doReadTest(dut: RAM, width: Int, r_exp_out1: Double, r_exp_out2: Double, i_exp_out1: Double, i_exp_out2: Double, r_addr1: Int, r_addr2: Int) : Unit = {
        dut.io.enable.poke(true.B)
        dut.io.read.poke(true.B)
        dut.io.addr1.poke(r_addr1.U)
        dut.io.addr2.poke(r_addr2.U)
        dut.clock.step()
        dut.io.realOut1.expect(r_exp_out1.F(width.W, (width-2).BP))
        dut.io.realOut2.expect(r_exp_out2.F(width.W, (width-2).BP))
        dut.io.imagOut1.expect(i_exp_out1.F(width.W, (width-2).BP))
        dut.io.imagOut2.expect(i_exp_out2.F(width.W, (width-2).BP))
        dut.clock.step()
    }
    
    behavior of "RAM"
    it should "correctly writes two floats to RAM then reads them back" in {
        test(new RAM(8, 32)) { dut =>
            // write signal
            doWriteTest(dut, 32, 1.0, -1.0, 1.0, -1.0, 0, 1)

            // read signal
            doReadTest(dut, 32, 1.0, -1.0, 1.0, -1.0, 0, 1)
        }
    }

    it should "correctly write to all RAM and read it all back" in {
        test(new RAM(16, 32)) { dut =>
            for(i <- 0 until 16 by 2) {
                doWriteTest(dut, 32, 1.toDouble, -1.toDouble, 1.toDouble, -1.toDouble, i, i+1)
            }

            for(j <- 0 until 16 by 2) {
                doReadTest(dut, 32, 1.toDouble, -1.toDouble, 1.toDouble, -1.toDouble, j, j+1)
            }
        }
    }

    it should "correctly write one and read one element to the entire RAM" in {
        test(new RAM(32, 32)) { dut =>
            for (i <- 0 until 32 by 2) {
                doWriteTest(dut, 32, 1.toDouble, -1.toDouble, 1.toDouble, -1.toDouble, i, i+1)
                doReadTest(dut, 32, 1.toDouble, -1.toDouble, 1.toDouble, -1.toDouble, i, i+1)
            }
        }
    }
}