package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FilterTester extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "Filter"
    it should "correctly low pass filters 8-point FFT" in {
        test(new Filter(8,32)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            val filter = Seq(1,2,3,4,0,0,0,0)
            dut.io.in.valid.poke(false.B)
            for(i <- 0 until 2){
                dut.io.in.bits(i).poke(0.F(32.W, 16.BP))
            }
            dut.io.in.ready.expect(true.B)
            dut.io.out.valid.expect(false.B)
            dut.clock.step()

            for(j <- 0 until 8) {
                dut.io.in.valid.poke(true.B)
                dut.io.in.bits(0).poke((j+1).F(32.W, 16.BP))
                dut.io.in.bits(1).poke(0.F(32.W, 16.BP))
                dut.io.out.valid.expect(true.B)
                dut.clock.step()
            }

            dut.io.in.valid.poke(false.B)
            for(i <- 0 until 2){
                dut.io.in.bits(i).poke(0.F(32.W, 16.BP))
            }
            dut.io.in.ready.expect(true.B)
            dut.io.out.valid.expect(false.B)
        }
    }

    it should "correctly low pass filters 16-point FFT" in {
        test(new Filter(16,32)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            val filter = Seq(1,2,3,4,5,6,7,8,0,0,0,0,0,0,0,0)
            dut.io.in.valid.poke(false.B)
            for(i <- 0 until 2){
                dut.io.in.bits(i).poke(0.F(32.W, 16.BP))
            }
            dut.io.in.ready.expect(true.B)
            dut.io.out.valid.expect(false.B)
            dut.clock.step()

            for(j <- 0 until 16) {
                dut.io.in.valid.poke(true.B)
                dut.io.in.bits(0).poke((j+1).F(32.W, 16.BP))
                dut.io.in.bits(1).poke(0.F(32.W, 16.BP))
                dut.io.out.valid.expect(true.B)
                dut.clock.step()
            }

            dut.io.in.valid.poke(false.B)
            for(i <- 0 until 2){
                dut.io.in.bits(i).poke(0.F(32.W, 16.BP))
            }
            dut.io.in.ready.expect(true.B)
            dut.io.out.valid.expect(false.B)
        }
    }
}