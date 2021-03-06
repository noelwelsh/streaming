package stream
package target

sealed abstract class Result[+A] extends Product with Serializable {
  import Result._

  def map[B](f: A => B): Result[B] =
    this match {
      case Emit(value) => Emit(f(value))
      case Await => Await
      case Completed => Completed
    }

  def flatMap[B](f: A => Result[B]): Result[B] =
    this match {
      case Emit(value) => f(value)
      case Await => Await
      case Completed => Completed
    }
}
object Result {
  final case class Emit[A](value: A) extends Result[A]
  final case object Await extends Result[Nothing]
  final case object Completed extends Result[Nothing]


  def emit[A](value: A): Result[A] = Emit(value)
  def await[A]: Result[A] = Await
  def completed[A]: Result[A] = Completed
}
