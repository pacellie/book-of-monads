package monad
package examples

sealed trait Tree[A]

object Tree {
  final case class Leaf[A](value: A)                      extends Tree[A]
  final case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A]

  import State._
  import Syntax._

  def relabel[A](tree: Tree[A]): State[Int, Tree[(A, Int)]] =
    tree match {
      case Leaf(value) => i => Leaf(value -> i) -> i
      case Node(l, r) =>
        for {
          l <- relabel(l)
          r <- relabel(r)
        } yield Node(l, r)
    }
}

object State {
  type State[S, A] = S => (A, S)

  implicit def stateMonad[S]: Monad[State[S, ?]] =
    new Monad[State[S, ?]] {
      def point[A](a: A): State[S, A] =
        s => {
          (a, s)
        }

      def flatMap[A, B](sa: State[S, A])(f: A => State[S, B]): State[S, B] =
        s => {
          val (a, s1) = sa(s)
          f(a)(s1)
        }
    }
}

sealed trait List[+A] {
  import List.{Cons, Nil}

  lazy val length: Int =
    this match {
      case Nil         => 0
      case Cons(_, xs) => 1 + xs.length
    }

  def ++ [B >: A](ys: List[B]): List[B] =
    this match {
      case Nil         => ys
      case Cons(x, xs) => Cons(x, xs ++ ys)
    }
}

object List {
  final case object Nil                        extends List[Nothing]
  final case class Cons[A](hd: A, tl: List[A]) extends List[A]

  implicit val listMonad: Monad[List] =
    new Monad[List] {
      def point[A](a: A): List[A] =
        Cons(a, Nil)

      def flatMap[A, B](xs: List[A])(f: A => List[B]): List[B] =
        xs match {
          case Nil         => Nil
          case Cons(x, xs) => f(x) ++ flatMap(xs)(f)
        }
    }
}

sealed trait Option[+A]

object Option {
  final case object None             extends Option[Nothing]
  final case class Some[A](value: A) extends Option[A]

  implicit val optionMonad: Monad[Option] =
    new Monad[Option] {
      def point[A](a: A): Option[A] =
        Some(a)

      def flatMap[A, B](xs: Option[A])(f: A => Option[B]): Option[B] =
        xs match {
          case None    => None
          case Some(a) => f(a)
        }
    }
}

object Person {
  type Name = String
  type Age  = Int

  final case class Person(name: Name, age: Age)

  import Syntax._

  def validateName(s: String): Option[Name] = ???

  def validateAge(i: Int): Option[Age] = ???

  def validatePerson(s: String, i: Int): Option[Person] =
    for {
      name <- validateName(s)
      age  <- validateAge(i)
    } yield Person(name, age)
}
