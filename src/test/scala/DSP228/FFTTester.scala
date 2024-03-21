package DSP228

import chisel3._
import chisel3.util._
import chiseltest._
import chisel3.experimental.FixedPoint
import chiseltest.ChiselScalatestTester
import org.antlr.v4.runtime.misc.Pair
import org.scalatest.flatspec.AnyFlatSpec

import scala.math._
import scala.collection.mutable.ArrayBuffer

class FFTTester extends AnyFlatSpec with ChiselScalatestTester{
  //fftModel from Rosetta code: https://rosettacode.org/wiki/Fast_Fourier_transform#Scala
  import scala.math.{ Pi, cos, sin, cosh, sinh, abs }
  case class Complex(re: Double, im: Double) {
    def +(x: Complex): Complex = Complex(re + x.re, im + x.im)
    def -(x: Complex): Complex = Complex(re - x.re, im - x.im)
    def *(x: Double):  Complex = Complex(re * x, im * x)
    def *(x: Complex): Complex = Complex(re * x.re - im * x.im, re * x.im + im * x.re)
    def /(x: Double):  Complex = Complex(re / x, im / x)
    override def toString(): String = {
      val a = "%1.3f" format re
      val b = "%1.3f" format abs(im)
      (a,b) match {
        case (_, "0.000") => a + " + 0.000i\n"
        case ("0.000", _) => b + "i\n"
        case (_, _) if im > 0 => a + " + " + b + "i\n"
        case (_, _) => a + " - " + b + "i\n"
      }
    }
  }
  def exp(c: Complex) : Complex = {
    val r = (cosh(c.re) + sinh(c.re))
    Complex(cos(c.im), sin(c.im)) * r
  }
  def _fft(cSeq: Seq[Complex], direction: Complex, scalar: Int): Seq[Complex] = {
    if (cSeq.length == 1) {
      return cSeq
    }
    val n = cSeq.length
    assume(n % 2 == 0, "The Cooley-Tukey FFT algorithm only works when the length of the input is even.")

    val evenOddPairs = cSeq.grouped(2).toSeq
    val evens = _fft(evenOddPairs map (_(0)), direction, scalar)
    val odds  = _fft(evenOddPairs map (_(1)), direction, scalar)

    def leftRightPair(k: Int): Tuple2[Complex, Complex] = {
      val base = evens(k) / scalar
      val offset = exp(direction * (Pi * k / n)) * odds(k) / scalar
      (base + offset, base - offset)
    }

    val pairs = (0 until n/2) map leftRightPair
    val left  = pairs map (_._1)
    val right = pairs map (_._2)
    left ++ right
  }
  def  fft(cSeq: Seq[Complex]): Seq[Complex] = _fft(cSeq, Complex(0,  2), 1)
  def testFFT(points: Int, width: Int,inputReal: Seq[Double], inputImag: Seq[Double])= {
    test(new FFT(points, width)).withAnnotations(Seq(WriteVcdAnnotation)){ dut =>
      dut.clock.setTimeout(0)
      dut.io.in.valid.poke(false.B)
      for(i <- 0 until points) {
        dut.io.in.valid.poke(true.B)
        dut.io.in.bits(0).poke(inputReal(i).F(width.W, (width/2).BP))
        dut.io.in.bits(1).poke(inputImag(i).F(width.W, (width/2).BP))
        dut.clock.step()
      }
      dut.io.in.valid.poke(false.B)
      dut.clock.step(points*(points * 3))
    }
  }

  behavior of "FFT"
  it should "correctly calculate FFT for 4 points, 2 stages" in {
    val fourPointsR= Seq.tabulate(4)(i => i.toDouble)
    val fourPointsI= Seq.fill(4)(0.0)
    testFFT(4, 24, fourPointsR, fourPointsI)
    val data = Seq.tabulate(4)(i => Complex(fourPointsR(i), fourPointsI(i)))
    println("Model OUTPUT: ")
    for(i <- 0 until 4){
      print(fft(data)(i))
    }
  }

  it should "correctly calculate FFT for 8 points, 3 stages" in {
    val eightPointsR= Seq.tabulate(8)(i => i.toDouble)
    val eightPointsI= Seq.fill(8)(0.0)
    testFFT(8, 24, eightPointsR, eightPointsI)
    val data = Seq.tabulate(8)(i => Complex(eightPointsR(i), eightPointsI(i)))
    println("Model OUTPUT: ")
    for(i <- 0 until 8){
      print(fft(data)(i))
    }
  }

  it should "correctly calculate FFT for 16 points, 4 stages" in {
    val sixteenPointsR= Seq.tabulate(16)(i => i.toDouble)
    val sixteenPointsI= Seq.fill(16)(0.0)
    testFFT(16, 24, sixteenPointsR, sixteenPointsI)
    val data = Seq.tabulate(16)(i => Complex(sixteenPointsR(i), sixteenPointsI(i)))
    println("Model OUTPUT: ")
    for(i <- 0 until 16){
      print(fft(data)(i))
    }
  }
}
