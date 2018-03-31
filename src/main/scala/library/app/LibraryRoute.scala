package library.app

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import library.domain._
import library.service.BookLoanService
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

class LibraryRoute(private final val service: BookLoanService,
                   implicit val materializer: ActorMaterializer) {

  private implicit val employeeFormat: RootJsonFormat[Employee] = jsonFormat2(Employee)
  private implicit val bookFormat: RootJsonFormat[Book] = jsonFormat3(Book)
  private implicit val loanFormat: RootJsonFormat[Loan] = jsonFormat5(Loan)
  private implicit val invalidReturnFormat: RootJsonFormat[InvalidReturn] = jsonFormat2(InvalidReturn)
  private implicit val validReturnFormat: RootJsonFormat[ValidReturn] = jsonFormat1(ValidReturn)

  val route: Route =
    get {
      pathPrefix("loan") {
        path("employee" / IntNumber) { id =>
          val loansByEmployee = service.showAllLoansByEmployeeId(id)
          onComplete(loansByEmployee)(loans => complete(loans))
        }~
        path("book"/ IntNumber) { id =>
          val loansByBook = service.showAllLoansByBookId(id)
          onComplete(loansByBook)(loans => complete(loans))
        }
      }
    } ~
  put {
    pathPrefix("loan") {
     path("borrow") {
       entity(as[Loan]) { case Loan(_, book, employee, loanDate, returnDate) =>
         val borrowOpt = service.loanBookToEmployee(book, employee, loanDate, returnDate)
         onSuccess(borrowOpt) {
           case Some(loan) => complete(loan)
           case None => complete(StatusCodes.BadRequest, "The loan info is invalid.  Cannot process request")
         }
       }
     }
    }
  } ~
  post {
    pathPrefix("loan") {
     path("return") {
       entity(as[Loan]) { case Loan(_, book, employee, _, returnDate) =>
         val returnOpt = service.returnBookFromEmployee(book, employee, returnDate )
           onSuccess(returnOpt) {
             case Some(ValidReturn(isDelayed)) => complete(ValidReturn(isDelayed))
             case Some(InvalidReturn(days, isDelayed)) => complete(InvalidReturn(days, isDelayed))
             case None => complete(StatusCodes.BadRequest, "The loan info is invalid. Cannot process request")
           }
       }
     }
    }
  }
}

