package stream

import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck._

class StreamSpec extends FunSuite with GeneratorDrivenPropertyChecks with Matchers {
  val iotaStreamGen: Gen[Stream[Int]] =
    for {
      n <- Gen.choose(0, 200)
    } yield Stream.fromIterator(Iterator.range(0, n))


  test("adding gives expected result") {
    val s =
      Stream.fromIterator(Iterator(1,2,3)).map(x => x + 1)

    s.foldLeft(0)(_ + _) should ===(9)
  }

  test("mapping identity does not change stream output") {
    forAll(iotaStreamGen){ stream =>
      stream.toList should ===(stream.map(x => x).toList)
    }
  }
}
