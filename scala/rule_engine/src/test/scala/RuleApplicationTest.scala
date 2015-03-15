import org.scalatest._
import RuleApplication._

class RuleApplicationTest extends FlatSpec with Matchers {

  val simpleJson =
    """
        {
           "left":{
                "field":"amount",
                "operator":"equals",
                "rhs":"1000"
            },
            "operator":"or",
            "right":{
                "field":"description",
                "operator":"contains",
                "rhs":"Caltex"
            }
        }
    """

  val simpleExpr: Expr = LogicalExpr(Predicate("amount", "equals", "1000"), "or", Predicate("description", "contains", "Caltex"))

  val complexJson =
    """
      {
          "left":{
              "left":{
                  "field":"amount",
                  "operator":"equals",
                  "rhs":"1000"
              },
              "operator":"or",
              "right":{
                  "field":"description",
                  "operator":"contains",
                  "rhs":"caltex"
              }
          },
          "operator":"and",
          "right":{
              "left":{
                  "left":{
                      "field":"price",
                      "operator":"equals",
                      "rhs":"210"
                  },
                  "operator":"and",
                  "right":{
                      "field":"name",
                      "operator":"contains",
                      "rhs":"xero"
                  }
              },
              "operator":"or",
              "right":{
                  "field":"comments",
                  "operator":"contains",
                  "rhs":"myob"
              }
          }
      }
    """

  val complexExpr: Expr = LogicalExpr(
    LogicalExpr(Predicate("amount", "equals", "1000"), "or", Predicate("description", "contains", "caltex")),
    "and",
    LogicalExpr(
      LogicalExpr(Predicate("price", "equals", "210"), "and", Predicate("name", "contains", "xero")),
      "or",
      Predicate("comments", "contains", "myob")
    )
  )

  it should "parse simple json" in {
    parse(simpleJson) should be (Right(simpleExpr))
  }

  it should "exec simple json" in {
    exec(simpleExpr) should be (true)
  }

  it should "parse complex json" in {
    parse(complexJson) should be (Right(complexExpr))
  }

  it should "exec complex json" in {
    exec(complexExpr) should be (true)
  }
}
