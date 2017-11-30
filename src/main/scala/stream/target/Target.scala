package stream
package target

sealed trait Target[A] {
  def next(): Result[A]
}
object Target {
  import Result._

  def fromStream[A](stream: Stream[A]): Target[A] = {
    stream match {
      case Stream.Map(source, f) => Map(fromStream(source), f)
      case Stream.Zip(left, right) => Zip(fromStream(left), fromStream(right))
      case Stream.Filter(source, predicate) => Filter(fromStream(source), predicate)
      case Stream.FromIterator(source) => FromIterator(source)
      case Stream.FromSeq(source) => FromSeq(source)
    }
  }

  final case class Map[A,B](source: Target[A], f: A => B) extends Target[B] {
    def next(): Result[B] =
      source.next().map(f)
  }

  final case class Filter[A](source: Target[A], predicate: A => Boolean) extends Target[A] {
    def next(): Result[A] =
      source.next().flatMap(v =>
        if(predicate(v)) Result.emit(v) else Result.await
      )
  }

  final case class Zip[A,B](left: Target[A], right: Target[B]) extends Target[(A,B)] {

    private var uninitialized: Boolean = true
    private var completed: Boolean = false
    private var emitted: Boolean = false

    private var lastLeft: Result[A] = _
    private var lastRight: Result[B] = _

    private def initialize(): Unit = {
      lastLeft = left.next
      lastRight = right.next
      uninitialized = false
    }

    private def update(): Unit = {
      if(emitted) {
        lastLeft = left.next
        lastRight = right.next
      } else {
        lastLeft match {
          case Await => lastLeft = left.next
          case Emit(_) | Completed => ()
        }
        lastRight match {
          case Await => lastRight = right.next
          case Emit(_) | Completed => ()
        }
      }
    }

    private def step(): Result[(A,B)] = {
      lastLeft match {
        case Emit(v1) =>
          lastRight match {
            case Emit(v2) =>
              emitted = true
              Result.emit((v1, v2))
            case Await =>
              emitted = false
              Result.await
            case Completed =>
              emitted = false
              completed = true
              Result.completed
          }

        case Await =>
          lastRight match {
            case Emit(v2) =>
              emitted = false
              Result.await
            case Await =>
              emitted = false
              Result.await
            case Completed =>
              emitted = false
              completed = true
              Result.completed
          }

        case Completed =>
          emitted = false
          completed = true
          Result.completed
      }
    }

    def next(): Result[(A,B)] = {
      if(completed) {
        Result.completed
      } else if(uninitialized) {
        initialize()
        step()
      } else {
        update()
        step()
      }
    }
  }

  final case class FromIterator[A](source: Iterator[A]) extends Target[A] {
    def next(): Result[A] =
      if(source.hasNext) Result.emit(source.next) else Result.completed
  }

  final case class FromSeq[A](source: Seq[A]) extends Target[A] {
    private var remaining: Seq[A] = source
    def next(): Result[A] = {
      if(remaining.isEmpty) Result.completed
      else {
        val elt = remaining.head
        remaining = remaining.tail
        Result.emit(elt)
      }
    }
  }
}
