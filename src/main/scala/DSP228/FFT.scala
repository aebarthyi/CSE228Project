package DSP228

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._

object FFTState extends ChiselEnum {
  val idle, load, calculateStage, writeStage, out = Value
}
class FFTIO(points: Int, width: Int) extends Bundle{
  val in = Flipped(Decoupled(Vec(2, FixedPoint(width.W, (width-2).BP))))
  val out = Decoupled(Vec(2, FixedPoint(width.W, (width-2).BP)))
}

class FFT(points: Int, width: Int) extends Module {
  val io = IO(new FFTIO(points, width))
  val fftState = RegInit(FFTState.idle)
  val fftMem = Module(new RAM(points, width))
  val butterfly = Module(new ButterflyUnit(width))
  val agu = Module(new AddressGenerationUnit(points))
  val twiddleRom = Module(new TwiddleFactor(points/2, width))
  val startCounter = Wire(Bool())
  val startOutput = Wire(Bool())
  val (bitReversedCounter, wrap) = Counter(0 until points by 2, startCounter)
  val (bitReversedCounter2, wrap2) = Counter(1 until points by 2, startCounter)
  val (outputCounter, outFin) = Counter(0 until points, startOutput)
  startCounter := false.B
  startOutput := false.B
  twiddleRom.io.m := 0.U
  butterfly.io.aReal := 0.F(width.W, (width-2).BP)
  butterfly.io.bReal := 0.F(width.W, (width-2).BP)
  butterfly.io.aImg := 0.F(width.W, (width-2).BP)
  butterfly.io.bImg := 0.F(width.W, (width-2).BP)
  butterfly.io.twiddleReal := 0.F(width.W, (width-2).BP)
  butterfly.io.twiddleImg := 0.F(width.W, (width-2).BP)
  agu.io.advance := false.B
  io.in.ready := true.B
  io.out.bits(0) := 0.F(width.W, (width-2).BP)
  io.out.bits(1) := 0.F(width.W, (width-2).BP)
  io.out.valid := false.B
  fftMem.io.addr1 := 0.U
  fftMem.io.addr2 := 0.U
  fftMem.io.realIn1 := 0.F(width.W, (width-2).BP)
  fftMem.io.realIn2 := 0.F(width.W, (width-2).BP)
  fftMem.io.imagIn1 := 0.F(width.W, (width-2).BP)
  fftMem.io.imagIn2 := 0.F(width.W, (width-2).BP)
  fftMem.io.enable := false.B
  fftMem.io.read := true.B

  switch(fftState){
    is(FFTState.idle){
      io.in.ready := true.B
      fftMem.io.read := true.B

      when(io.in.valid){
        fftMem.io.enable := true.B
        fftMem.io.read := false.B
        startCounter := true.B
        fftMem.io.realIn1 := io.in.bits(0)
        fftMem.io.imagIn1 := 0.F(width.W, (width-2).BP)
        fftMem.io.realIn2 := io.in.bits(1)
        fftMem.io.imagIn2 := 0.F(width.W, (width-2).BP)
        fftMem.io.addr1 := Reverse(bitReversedCounter)
        fftMem.io.addr2 := Reverse(bitReversedCounter2)
        fftState := FFTState.load
      }
    }

    is(FFTState.load){
      fftMem.io.enable := true.B
      fftMem.io.read := false.B
      startCounter := true.B
      fftMem.io.realIn1 := io.in.bits(0)
      fftMem.io.imagIn1 := 0.F(width.W, (width-2).BP)
      fftMem.io.realIn2 := io.in.bits(1)
      fftMem.io.imagIn2 := 0.F(width.W, (width-2).BP)
      fftMem.io.addr1 := Reverse(bitReversedCounter)
      fftMem.io.addr2 := Reverse(bitReversedCounter2)
      when(wrap){
        fftState := FFTState.calculateStage
      }
    }

    is(FFTState.calculateStage){
      fftMem.io.read := true.B
      printf(cf"new stage\n")
      printf(cf"A: ${butterfly.io.aReal.asUInt}|${butterfly.io.aImg.asUInt}\n")
      printf(cf"B: ${butterfly.io.bReal.asUInt}|${butterfly.io.bImg.asUInt}\n")
      agu.io.advance := false.B
      fftMem.io.enable := true.B
      fftMem.io.read := true.B
      startCounter := false.B

      fftMem.io.addr1 := agu.io.addressA
      fftMem.io.addr2 := agu.io.addressB

      twiddleRom.io.m := agu.io.twiddleAddress

      butterfly.io.aReal := fftMem.io.realOut1
      butterfly.io.bReal := fftMem.io.realOut2

      butterfly.io.aImg := fftMem.io.imagOut1
      butterfly.io.bImg := fftMem.io.imagOut2

      butterfly.io.twiddleReal := twiddleRom.io.twiddleFactorReal
      butterfly.io.twiddleImg := twiddleRom.io.twiddleFactorImag

      fftState := FFTState.writeStage
    }

    is(FFTState.writeStage){
      fftMem.io.addr1 := agu.io.addressA
      fftMem.io.addr2 := agu.io.addressB
      twiddleRom.io.m := agu.io.twiddleAddress

      butterfly.io.aReal := fftMem.io.realOut1
      butterfly.io.bReal := fftMem.io.realOut2
      butterfly.io.aImg := fftMem.io.imagOut1
      butterfly.io.bImg := fftMem.io.imagOut2
      butterfly.io.twiddleReal := twiddleRom.io.twiddleFactorReal
      butterfly.io.twiddleImg := twiddleRom.io.twiddleFactorImag

      fftMem.io.realIn1 := butterfly.io.coutReal
      fftMem.io.realIn2 := butterfly.io.doutReal
      fftMem.io.imagIn1 := butterfly.io.coutImg
      fftMem.io.imagIn1 := butterfly.io.doutImg

      fftMem.io.enable := true.B
      fftMem.io.read := false.B
      when(agu.io.done){
        fftState := FFTState.out
      }.otherwise {
        agu.io.advance := true.B
        fftState := FFTState.calculateStage
      }
    }

    is(FFTState.out){
      fftMem.io.enable := true.B
      fftMem.io.read := true.B
      io.out.valid := true.B
      startOutput := true.B
      fftMem.io.addr1 := outputCounter
      io.out.bits(0) := fftMem.io.realOut1
      io.out.bits(1) := fftMem.io.imagOut1
      when(outFin){
        fftState := FFTState.idle
      }
    }
  }


}
