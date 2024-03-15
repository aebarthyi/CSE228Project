package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

object FilterState extends ChiselEnum {
    val idle, streaming = Value
}

class FilterIO(width: Int) extends Bundle {
    val in = Flipped(Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP))))
    val out = Decoupled(Vec(2, FixedPoint(width.W, (width/2).BP)))
    val curr_state = Output(FilterState())
}

class Filter(points: Int, width: Int) extends Module {
    val io = IO(new FilterIO(width))

    val state_r = RegInit(FilterState.idle)
    val counter = new Counter(points)
    val weight_data = Seq.tabulate(points)(i => if(i < points/2) {1.F(width.W, (width/2).BP)} else {0.F(width.W, (width/2).BP)})
    val filter_weights : Vec[FixedPoint] = VecInit(weight_data)

    io.in.ready := false.B
    io.out.valid := false.B
    for (i <- 0 until 2) {
        io.out.bits(i) := 0.F(width.W, (width/2).BP)
    }

    switch(state_r) {
        is(FilterState.idle) {
            io.in.ready := true.B
            when (io.in.fire) {
                state_r := FilterState.streaming

                io.out.valid := true.B
                for (i <- 0 until 2) {
                    io.out.bits(i) := filter_weights(counter.value)*io.in.bits(i)
                }
                printf("OUTPUT: \n")
                printf(cf"${io.out.bits(0).asSInt} + ${io.out.bits(1).asSInt}i\n")
                counter.inc()
            }
        }

        is(FilterState.streaming) {
            io.in.ready := false.B
            io.out.valid := true.B
            for (i <- 0 until 2) {
                io.out.bits(i) := filter_weights(counter.value)*io.in.bits(i)
            }
            printf(cf"${io.out.bits(0).asSInt} + ${io.out.bits(1).asSInt}i\n")
            when(counter.inc()) {
                state_r := FilterState.idle
            }
        }
    }
    io.curr_state := state_r
}