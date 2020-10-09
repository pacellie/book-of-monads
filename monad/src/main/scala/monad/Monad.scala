package monad

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]

  def lift[A, B](f: A => B): F[A] => F[B] =
    fa => map(fa)(f)
}

object Functor {
  def apply[F[_]: Functor]: Functor[F] =
    implicitly[Functor[F]]

  def void[F[_]: Functor, A](fa: F[A]): F[Unit] =
    Functor[F[?]].map(fa)(_ => ())
}

trait Applicative[F[_]] extends Functor[F] {
  def point[A](a: A): F[A]

  def ap[A, B](fa: F[A])(f: F[A => B]): F[B]

  final def lift2[A, B, C](f: A => B => C): F[A] => F[B] => F[C] =
    fa => fb => ap(fb)(map(fa)(f))

  final def lift3[A, B, C, D](f: A => B => C => D): F[A] => F[B] => F[C] => F[D] =
    fa => fb => fc => ap(fc)(ap(fb)(map(fa)(f)))

  final def product[A, B](fa: F[A])(fb: F[B]): F[(A, B)] =
    ap(fa)(map(fb)(b => a => a -> b))

  final def productL[A, B](fa: F[A])(fb: F[B]): F[A] =
    ap(fa)(map(fb)(_ => a => a))

  final def productR[A, B](fa: F[A])(fb: F[B]): F[B] =
    ap(fa)(map(fb)(b => _ => b))

  final override def map[A, B](fa: F[A])(f: A => B): F[B] =
    ap(fa)(point(f))
}

object Applicative {
  def apply[F[_]: Applicative]: Applicative[F] =
    implicitly[Applicative[F]]

}

trait Monad[M[_]] extends Applicative[M] {
  def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]

  final override def ap[A, B](fa: M[A])(f: M[A => B]): M[B] =
    flatMap(fa) { a =>
      map(f)(_(a))
    }

  final def flatten[A](mma: M[M[A]]): M[A] =
    flatMap(mma)(identity)
}

object Monad {
  def apply[M[_]: Monad]: Monad[M] =
    implicitly[Monad[M]]

  def sequence[M[_]: Monad, A](xs: List[M[A]]): M[List[A]] =
    xs match {
      case Nil => Monad[M[?]].point(Nil)
      case x :: xs =>
        Monad[M[?]].lift2[A, List[A], List[A]](y => ys => y :: ys)(x)(sequence(xs))
    }
}

object Syntax {
  implicit class FunctorSyntax[F[_], A](private val fa: F[A]) extends AnyVal {
    def map[B](f: A => B)(implicit F: Functor[F]): F[B] =
      F.map(fa)(f)
  }

  implicit class ApplicativeSyntax[F[_], A](private val fa: F[A]) extends AnyVal {
    def <*> [B](f: F[A => B])(implicit F: Applicative[F]): F[B] =
      F.ap(fa)(f)

    def ** [B](fb: F[B])(implicit F: Applicative[F]): F[(A, B)] =
      F.product(fa)(fb)

    def <* [B](fb: F[B])(implicit F: Applicative[F]): F[A] =
      F.productL(fa)(fb)

    def *> [B](fb: F[B])(implicit F: Applicative[F]): F[B] =
      F.productR(fa)(fb)
  }

  implicit class MonadSyntax[M[_], A](private val ma: M[A]) extends AnyVal {
    def flatMap[B](f: A => M[B])(implicit M: Monad[M]): M[B] =
      M.flatMap(ma)(f)

    def >>= [B](f: A => M[B])(implicit M: Monad[M]): M[B] =
      M.flatMap(ma)(f)
  }
}
