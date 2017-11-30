package stream

sealed trait Stream[A] {
  import Stream._

  def map[B](f: A => B): Stream[B] =
    Map(this, f)

  def zip[B](that: Stream[B]): Stream[(A,B)] =
    Zip(this, that)

  def filter(predicate: A => Boolean): Stream[A] =
    Filter(this, predicate)

  /** The interpreter, executes a stream. */
  def foldLeft[B](z: B)(combine: (B, A) => B): B = {
    import target._
    import Result._

    val t = Target.fromStream(this)

    def loop(result: B): B =
      t.next() match {
        case Emit(v) => loop(combine(result, v))
        case Await => loop(result)
        case Completed => result
      }

    loop(z)
  }

  def toList: List[A] = {
    this.foldLeft(List.empty[A])((list, elt) => elt +: list)
  }
}
object Stream {
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class Filter[A](source: Stream[A], predicate: A => Boolean) extends Stream[A]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]

  def fromIterator[A](source: Iterator[A]): Stream[A] =
    FromIterator(source)
}
