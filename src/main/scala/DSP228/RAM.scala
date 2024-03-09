package DSP228
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint

class RAMUnitIO(NumEntries: Int) extends Bundle {
    // read or write signal
    val read = Input(Bool()) // if read = high then read, else write
    
    // writing inputs
    val in_data1 = Input(FixedPoint(32.W, 8.BP))
    val in_data2 = Input(FixedPoint(32.W, 8.BP))
    
    // addresses
    val addr1 = Input(UInt(log2Ceil(NumEntries).W))
    val addr2 = Input(UInt(log2Ceil(NumEntries).W))
    
    // output signals
    val out1 = Output(FixedPoint(32.W, 8.BP))
    val out2 = Output(FixedPoint(32.W, 8.BP))
}

class RAM(NumEntries: Int) extends Module {
    val io = IO(new RAMUnitIO(NumEntries))
    // TODO: Implement Behavior
    val mem1 = SyncReadMem(NumEntries, FixedPoint(32.W, 8.BP))
    val mem2 = SyncReadMem(NumEntries, FixedPoint(32.W, 8.BP))
    
    io.out1 := DontCare
    io.out2 := DontCare
    
    val rdwr_port1 = mem1(io.addr1)
    val rdwr_port2 = mem1(io.addr2)
    when(io.read) {
        io.out1 := rdwr_port1
        io.out2 := rdwr_port2
    } .otherwise {
        rdwr_port1 := io.in_data1
        rdwr_port2 := io.in_data2
    }
}