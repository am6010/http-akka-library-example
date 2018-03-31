package library

import library.domain._
import library.service.BookLoanService

import scala.concurrent.{ExecutionContext, Future}

trait SetupTest {
  val employee1 =  Employee(1, "Alice")
  val employee2 =  Employee(2, "Kate")
  val employee3 =  Employee(3, "Bob")
  val employees = Map((1, employee1), (2, employee2), (3, employee3))

  val book1  =  Book(1, "Book1", 2)
  val book2 =  Book(2, "Book2", 1)
  val book3 = Book(3, "Book3", 2)
  val books = Map((1, book1), (2, book2), (3, book3))

  val dayInSeconds: Long = 24 * 60 * 60L
  val now :Long = System.currentTimeMillis() / 1000
  val loan1 = Loan(1, book2, employee1, now - 2 * dayInSeconds, now + 5 * dayInSeconds)
  val loan2 = Loan(2, book1, employee3, now - 2 * dayInSeconds, now + 5 * dayInSeconds)
  val loan3 = Loan(3, book3, employee2, now - 2 * dayInSeconds, now + 5 * dayInSeconds)
  val loans = Map((1, loan1), (2, loan2), (3, loan3))

  val initState = State(employees, books, loans)

  class MockLibraryService(implicit val executionContext: ExecutionContext) extends BookLoanService {

    def showAllLoansByBookId(id: Int): Future[Seq[Loan]] = Future{ Seq(loan2) }

    def returnBookFromEmployee(book: Book, employee: Employee, returnDate: Long): Future[Option[ReturnType]] =
     Future {
       book match {
         case `book1` => Some(ValidReturn())
         case `book3` => Some(InvalidReturn(2))
         case _ => None
       }
     }

    def showAllLoansByEmployeeId(id: Int): Future[Seq[Loan]] = Future{ Seq(loan1) }

    def loanBookToEmployee(book: Book, employee: Employee, loanDate: Long, returnDate: Long): Future[Option[Loan]] =
      Future {
        if (book == book3)
          Some(Loan(4, book3, employee1, now, now + 7 * dayInSeconds))
        else
          None
      }
  }
}
