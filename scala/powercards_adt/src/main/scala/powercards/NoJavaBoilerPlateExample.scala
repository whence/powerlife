package powercards

object NoJavaBoilerPlateExample {

  class Dto(val field1: String, val field2: String, val field3: String)

  val fields: Seq[Dto => String] = Seq(_.field1, _.field2, _.field3)

  val fieldValidators: Seq[String => Boolean] = Seq(_ != null, _.nonEmpty, _.length <= 50, Set("option1", "option2"))

  def validate(dtos: Seq[Dto]):Boolean = {
    val results = for {
      dto <- dtos
      field <- fields
      validator <- fieldValidators
    } yield validator(field(dto))

    results.forall(identity)
  }
}
