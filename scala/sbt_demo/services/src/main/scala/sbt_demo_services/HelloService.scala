package sbt_demo_services

import sbt_demo_core.Utils
import sbt_demo_repositories.HelloRepository

object HelloService {
  val hello = s"hello from HelloService with ${HelloRepository.hello} with ${Utils.hello}"
}
