package library.service

import library.domain._

import scala.concurrent.{ExecutionContext, Future}

/**
  * A service to describe the main operations they should offer for
  * supporting loans of books
  */
trait BookLoanService {

  /**
    * A method to insert new loans to the system
    * @param book the book to borrow
    * @param employee the employee who wants to borrow a book
    * @param loanDate the date the book was given
    * @param returnDate the return date
    * @return A loan option too make sure it is processed as expected
    */
  def loanBookToEmployee(book: Book,
                         employee: Employee,
                         loanDate: Long,
                         returnDate: Long): Future[Option[Loan]]

  /**
    * A method to offer the return operation
    * @param book the book the is returned
    * @param employee the employee who returns the book
    * @param returnDate the date the book is returned
    * @return If the loan was removed succesfully
    */
  def returnBookFromEmployee(book: Book,
                             employee: Employee,
                             returnDate: Long) : Future[Option[ReturnType]]

  /**
    * A method to show all the loans form employee
    * @param id of employee
    * @return all the current loans of employee
    */
  def showAllLoansByEmployeeId(id: Int):Future[Seq[Loan]]

  /**
    *  A method to show all the loans for a book
    * @param id book id
    * @return all the current loans of the book
    */
  def showAllLoansByBookId(id: Int):Future[Seq[Loan]]
}

/**
  * An in memory Book loan Service
  * @param state of the system
  */
class InMemoryBookLoanServiceImp(private var state: State,
                                 implicit val executionContext: ExecutionContext) extends BookLoanService {

  /**
    * A method to insert new loans to the system
    *
    * @param book       the book to borrow
    * @param employee   the employee who wants to borrow a book
    * @param loanDate   the date the book was given
    * @param returnDate the return date
    * @return A loan option too make sure it is processed as expected
    */
  def loanBookToEmployee(book: Book, employee: Employee, loanDate: Long, returnDate: Long): Future[Option[Loan]] = {
    Future {

      val employeeOpt = state.employees.get(employee.id)
      val bookOpt = state.books.get(book.id)
      val maxLoanId = state.loans.keys.max

      val loanOpt:Option[Loan] = for {
        employee <- employeeOpt
        book <- bookOpt
        count = state.loans.values.count(loan => loan.book.id == book.id)
        if count < book.numberOfCopies
      } yield Loan(maxLoanId + 1, book, employee, loanDate, returnDate)

      loanOpt.foreach { loan =>
        val newLoans = state.loans + (loan.id -> loan)
        state = state.updateLoans(newLoans)
      }
      loanOpt
    }
  }

  /**
    * A method to offer the return operation
    *
    * @param book       the book the is returned
    * @param employee   the employee who returns the book
    * @param returnDate the date the book is returned
    * @return If the loan was removed succesfully
    */
  def returnBookFromEmployee(book: Book, employee: Employee, returnDate: Long): Future[Option[ReturnType]] = {
    Future {
      val State(_, _, loans) = state
      val loanOpt = loans.values
        .find(loan => loan.book.id == book.id && loan.employee.id == employee.id)

      loanOpt.foreach { loan =>
        val newLoans = loans - loan.id
        state = state.updateLoans(newLoans)
      }

      loanOpt.map { loan =>
        if (loan.returnDate > returnDate) ValidReturn()
        else {
          val daysDelay = (returnDate - loan.returnDate) / (24 * 60 * 60L)
          InvalidReturn(daysDelay.toInt)
        }
      }
    }
  }

  /**
    * A method to show all the loans
    *
    * @return all the current loans
    */
  def showAllLoansByEmployeeId(id: Int): Future[Seq[Loan]] = {
    Future {
      state.loans.values.filter(_.employee.id == id).toSeq
    }
  }

  /**
    * A method to show all the loans for a book
    *
    * @param id book id
    * @return all the current loans of the book
    */
  def showAllLoansByBookId(id: Int): Future[Seq[Loan]] = {
    Future {
      state.loans.values.filter(_.book.id == id).toSeq
    }
  }
}
