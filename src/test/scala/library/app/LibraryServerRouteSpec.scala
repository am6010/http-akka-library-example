package library.app

import akka.http.scaladsl.testkit.ScalatestRouteTest
import library.SetupTest
import library.domain._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{AsyncWordSpec, Matchers}
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import spray.json.DefaultJsonProtocol.{jsonFormat2, jsonFormat3, jsonFormat5}
import spray.json.RootJsonFormat

@RunWith(classOf[JUnitRunner])
class LibraryServerRouteSpec extends AsyncWordSpec with Matchers
  with ScalatestRouteTest with SetupTest {

  val mockService = new MockLibraryService()
  private val route = new LibraryRoute(mockService, this.materializer).route
  private implicit val employeeFormat: RootJsonFormat[Employee] = jsonFormat2(Employee)
  private implicit val bookFormat: RootJsonFormat[Book] = jsonFormat3(Book)
  private implicit val loanFormat: RootJsonFormat[Loan] = jsonFormat5(Loan)
  private implicit val invalidReturnFormat: RootJsonFormat[InvalidReturn] = jsonFormat2(InvalidReturn)
  private implicit val validReturnFormat: RootJsonFormat[ValidReturn] = jsonFormat1(ValidReturn)

  "The service" should {
    "return a list of specs for employee" in {
      Get("/loan/employee/1") ~> route ~> check {
        responseAs[Seq[Loan]] shouldBe Seq(loan1)
      }
    }
    "return a list of loans for a book" in {
      Get("/loan/book/1") ~> route ~> check {
        responseAs[Seq[Loan]] shouldBe Seq(loan2)
      }
    }
    "return a success loan opt" in {
      val returnDate = now + 7 * dayInSeconds
      Put("/loan/borrow", Loan(-1, book3, employee1, now, returnDate)) ~> route ~> check {
        responseAs[Loan] shouldBe Loan(4, book3, employee1, now, returnDate)
      }
    }

    "return an empty loan opt for invalid operation" in {
      val returnDate = now + 7 * dayInSeconds
      Put("/loan/borrow", Loan(-1, book2, employee1, now, returnDate)) ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }
    }

    "return valid return if return date is less of the limit" in {
      Post("/loan/return", Loan(-1, book1, employee1, now, returnDate = now + 3 * dayInSeconds)) ~> route ~> check {
        responseAs[ValidReturn] shouldBe ValidReturn()
      }
    }

    "return valid return with delay 2 days" in {
      Post("/loan/return", Loan(-1, book3, employee1, now, returnDate = now + 3 * dayInSeconds)) ~> route ~> check {
        responseAs[InvalidReturn] shouldBe InvalidReturn(2)
      }
    }

    "return an invalide return if the there is a problem with the request" in {
      Post("/loan/return", Loan(-1, book2, employee1, now, returnDate = now + 3 * dayInSeconds)) ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }
    }
  }
}

