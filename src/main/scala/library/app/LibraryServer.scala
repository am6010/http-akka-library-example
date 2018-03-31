package library.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import library.domain.State
import library.service.InMemoryBookLoanServiceImp

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object LibraryServer extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  // Set up the systems components
  val state = State.initialState
  val service = new InMemoryBookLoanServiceImp(state, executionContext)
  val libraryRoute = new LibraryRoute(service, materializer)
  val bindingFuture = Http().bindAndHandle(libraryRoute.route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")


  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
