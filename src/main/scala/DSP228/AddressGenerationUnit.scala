package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

//stages 0-log2(points)

class AddressGenerationIO(points: Int, width: Int) extends Bundle{
  val en = Input(Bool())
  val stage = Input(UInt(log2Ceil(points).W))
  val addressA = Output(UInt(width.W))
  val addressB = Output(UInt(width.W))
  val twiddleAddress = Output(UInt(width.W))
}
class AddressGenerationUnit(points: Int, width: Int) extends Module{
  val io = IO(new AddressGenerationIO(points, width))
  val resetCounter = Wire(Bool())
  val addressAoff = Wire(UInt(width.W))
  val aCicularShiftFirst = Wire(UInt(width.W))
  val aCicularShiftSecond = Wire(UInt(width.W))
  val addressBoff = Wire(UInt(width.W))
  val bCicularShiftFirst = Wire(UInt(width.W))
  val bCicularShiftSecond = Wire(UInt(width.W))
  val (counter, wrap) = Counter(0 until points/2, io.en, resetCounter )
  val twiddleMask = ShiftRegisters(1.B, width-1, 0.U(1.W), wrap)
  resetCounter := false.B
  addressAoff := (counter << 1)
  addressBoff := addressAoff + 1.U

  aCicularShiftFirst := (addressAoff << io.stage)
  bCicularShiftFirst := (addressBoff << io.stage)

  aCicularShiftSecond := (addressAoff >> (log2Ceil(points).U - io.stage))
  bCicularShiftSecond := (addressBoff >> (log2Ceil(points).U - io.stage))

  io.addressA := aCicularShiftFirst | aCicularShiftSecond
  io.addressB := bCicularShiftFirst | bCicularShiftSecond

  io.twiddleAddress := Cat(twiddleMask) & counter
}

