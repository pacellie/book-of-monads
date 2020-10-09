package eq

trait Eq[A] {
  def eq(x: A, y: A): Boolean
}

object Eq {
  def apply[A: Eq]: Eq[A] =
    implicitly[Eq[A]]

  def eq[A: Eq](x: A, y: A): Boolean =
    implicitly[Eq[A]].eq(x, y)

  implicit val eqBoolean: Eq[Boolean] = (x: Boolean, y: Boolean) =>
    (x, y) match {
      case (true, true)   => true
      case (false, false) => true
      case _              => false
    }

  implicit def eqList[A: Eq]: Eq[List[A]] =
    new Eq[List[A]] {
      def eq(xs: List[A], ys: List[A]): Boolean =
        (xs, ys) match {
          case (Nil, Nil)                           => true
          case (x :: xs, y :: ys) if Eq[A].eq(x, y) => eq(xs, ys)
          case _                                    => false
        }
    }

  object syntax {
    implicit final class EqSyntax[A](private val x: A) extends AnyVal {
      def === (y: A)(implicit witness: Eq[A]): Boolean =
        witness.eq(x, y)
    }
  }
}
