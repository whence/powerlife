import play.api.data.validation.ValidationError
import play.api.libs.json._

object RuleApplication {

  sealed abstract class Expr
  case class Predicate(field: String, operator: String, rhs: String) extends Expr
  case class LogicalExpr(left: Expr, operator: String, right: Expr) extends Expr

  def parse(rawJson: String): Either[ValidationError, Expr] = {
    val json = Json.parse(rawJson)

    def loop(json: JsValue): Either[ValidationError, Expr] = {
      (json \ "field").asOpt[String] match {
        case Some(field) =>
          val operator = (json \ "operator").as[String].toLowerCase
          operator match {
            case "equals" | "contains" => Right(Predicate(
              field,
              operator,
              (json \ "rhs").as[String]
            ))
            case _ => Left(ValidationError("Predicate operators can be either Equals or Contains"))
          }
        case None =>
          val operator = (json \ "operator").as[String].toLowerCase
          operator match {
            case "and" | "or" => (loop(json \ "left"), loop(json \ "right")) match {
              case (Right(left), Right(right)) => Right(LogicalExpr(left, operator, right))
              case (Left(error), Right(_)) => Left(error)
              case (Right(_), Left(error)) => Left(error)
              case (Left(ValidationError(msg1)), Left(ValidationError(msg2))) => Left(ValidationError(s"$msg1 $msg2"))
            }
            case _ => Left(ValidationError("Logical operators can be either AND or OR"))
          }
      }
    }

    loop(json)
  }

  def equality[T >: Equals](rhs: T) (lhs: T): Boolean = lhs.equals(rhs)
  def contains[T >: String](rhs: T) (lhs: T): Boolean = lhs.toString.contains(rhs.toString)

  def predicateToFunction(pred: Predicate): (AnyRef => Boolean) =  {
    pred match {
      case Predicate("amount"|"price", "equals", rhs) => equality(BigDecimal(rhs))
      case Predicate(_, "contains", rhs) => contains(rhs)
      case _ => (_: Any) => false
    }
  }

  def fieldToType(field:String): AnyRef = field match {
      case "amount" => BigDecimal("1000")
      case "description" => "Fuel from caltex"
      case "price" => BigDecimal("200")
      case "comments" => "switched from myob"
      case "name" => "xero accounting"
  }

  def exec(expr: Expr): Boolean = expr match {
    case pred @ Predicate(field, operator, rhs) =>
      predicateToFunction(pred)(fieldToType(field))
    case LogicalExpr(left, "and", right) =>
      exec(left) && exec(right)
    case LogicalExpr(left, "or", right) =>
      exec(left) || exec(right)
    case LogicalExpr(left, _, right) =>
      false
  }

  def main(args: Array[String]) {
    parse(io.Source.stdin.getLines().mkString("\n")) match {
      case Right(expr) => println(s"$expr = ${exec(expr)}")
      case Left(ValidationError(msg)) => println(s"error: $msg")
    }
  }
}
