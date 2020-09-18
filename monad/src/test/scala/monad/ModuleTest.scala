import monad._
import org.specs2._
import org.specs2.ScalaCheck

class ModuleTest extends Specification with ScalaCheck {

  def is = s2"""
    Hello Spec $hello
  """

  def hello = Module.hello === "hello world"

}