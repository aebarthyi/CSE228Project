package DSP228

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FilterTester extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "Filter"
    it should "correctly transitions through states" in {
        test(new Filter(8,32)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
            dut.io.in.valid.poke(false.B)
            for(i <- 0 until 2){
                dut.io.in.bits(i).poke(0.F(32.W, 16.BP))
            }
            dut.io.in.ready.expect(true.B)
            dut.io.out.valid.expect(false.B)
            dut.io.curr_state.expect(FilterState.idle)
            dut.clock.step()

            for(i <- 0 until 8) {
                dut.io.in.valid.poke(true.B)
                for(i <- 0 until 2){
                    dut.io.in.bits(i).poke(1.F(32.W, 16.BP))
                }
                dut.io.out.valid.expect(true.B)
                for(i <- 0 until 2){
                    dut.io.out.bits(i).expect(1.F(32.W, 16.BP))
                }
                if (i < 1) {
                    dut.io.curr_state.expect(FilterState.idle)
                } else {
                    dut.io.curr_state.expect(FilterState.streaming)
                }
                dut.clock.step()
            }

            dut.io.in.valid.poke(false.B)
            for(i <- 0 until 2){
                dut.io.in.bits(i).poke(0.F(32.W, 16.BP))
            }
            dut.io.in.ready.expect(true.B)
            dut.io.out.valid.expect(false.B)
            dut.io.curr_state.expect(FilterState.idle)
        }
    }
}