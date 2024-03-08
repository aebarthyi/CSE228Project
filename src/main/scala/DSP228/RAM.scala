package DSP228
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint

class RAMUnitIO(NumEntries: Int) extends Bundle {
    // read or write signal
    val read = Flipped(Valid(Bool())) // if read.bits = high then read, else write
    
    // writing inputs
    val in_data1 = Input(FixedPoint(32.W, 8.BP))
    val in_data2 = Input(FixedPoint(32.W, 8.BP))
    
    val addr1 = Input(UInt(log2Ceil(NumEntries).W))
    val addr2 = Input(UInt(log2Ceil(NumEntries).W))
    
    val out_valid = Output(Bool())
    val out1 = Output(FixedPoint(32.W, 8.BP))
    val out2 = Output(FixedPoint(32.W, 8.BP))
}

class RAM(NumEntries: Int) extends Module {
    val io = IO(new RAMUnitIO(NumEntries))
    // TODO: Implement Behavior
    val mem1 = SyncReadMem(NumEntries, FixedPoint(32.W, 8.BP))
    val mem2 = SyncReadMem(NumEntries, FixedPoint(32.W, 8.BP))

    io.out_valid := 0.B 
    io.out1 := 0.F(32.W, 8.BP)
    io.out2 := 0.F(32.W, 8.BP)
}