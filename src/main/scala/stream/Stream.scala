package stream

sealed trait Stream[A] {
  import Stream._

  def map[B](f: A => B): Stream[B] =
    Map(this, f)

  def zip[B](that: Stream[B]): Stream[(A,B)] =
    Zip(this, that)

  /** The interpreter, executes a stream. */
  def foldLeft[B](z: B)(combine: (B, A) => B): B = {
    import Result._

    def next[C](stream: Stream[C]): Result[C] =
      stream match {
        case Map(s,f) => next(s).map(f)
        case Zip(l,r) => (next(l) zip next(r))
        case FromIterator(s) =>
          if (s.hasNext) Result.emit(s.next()) else Result.completed
      }

    next(this) match {
      case Emit(v) => foldLeft(combine(z, v))(combine)
      case Await => foldLeft(z)(combine)
      case Completed => z
    }
  }

  def toList: List[A] = {
    this.foldLeft(List.empty[A])((list, elt) => elt +: list)
  }
}
object Stream {
  final case class Map[A,B](source: Stream[A], f: A => B) extends Stream[B]
  final case class Zip[A,B](left: Stream[A], right: Stream[B]) extends Stream[(A,B)]
  final case class FromIterator[A](source: Iterator[A]) extends Stream[A]

  def fromIterator[A](source: Iterator[A]): Stream[A] =
    FromIterator(source)
}
