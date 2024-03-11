package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

//stages 0-log2(points)

class AddressGenerationIO(points: Int) extends Bundle{
  val en = Input(Bool())
  val done = Output(Bool())
  val addressA = Output(UInt(log2Ceil(points).W))
  val addressB = Output(UInt(log2Ceil(points).W))
  val twiddleAddress = Output(UInt(log2Ceil(points).W))
}
class AddressGenerationUnit(points: Int) extends Module{
  val io = IO(new AddressGenerationIO(points))
  val resetCounter = Wire(Bool())
  val addressAoff = Wire(UInt(log2Ceil(points).W))
  val aCicularShiftFirst = Wire(UInt(log2Ceil(points).W))
  val aCicularShiftSecond = Wire(UInt(log2Ceil(points).W))
  val addressBoff = Wire(UInt(log2Ceil(points).W))
  val bCicularShiftFirst = Wire(UInt(log2Ceil(points).W))
  val bCicularShiftSecond = Wire(UInt(log2Ceil(points).W))
  val (counter, wrap) = Counter(0 until points/2, io.en, resetCounter )
  val stageCounter = new Counter(log2Ceil(points))
  val twiddleMask = ShiftRegisters(1.B, log2Ceil(points)-1, 0.U(1.W), wrap)
  resetCounter := false.B
  io.done := false.B
  addressAoff := (counter << 1)
  addressBoff := addressAoff + 1.U

  aCicularShiftFirst := (addressAoff << stageCounter.value)
  bCicularShiftFirst := (addressBoff <<  stageCounter.value)

  aCicularShiftSecond := (addressAoff >> (log2Ceil(points).U -  stageCounter.value))
  bCicularShiftSecond := (addressBoff >> (log2Ceil(points).U -  stageCounter.value))

  io.addressA := aCicularShiftFirst | aCicularShiftSecond
  io.addressB := bCicularShiftFirst | bCicularShiftSecond

  io.twiddleAddress := Cat(twiddleMask) & counter

  when(wrap){
    stageCounter.inc()
  }

  when(stageCounter.value === (log2Ceil(points)-1).U && wrap){
    io.done := true.B
  }
}

