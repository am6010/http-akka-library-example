package library.service

import library.SetupTest
import library.domain._
import org.junit.runner.RunWith
import org.scalatest.AsyncFlatSpec
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class InMemoryBookLoanServiceImplTest extends AsyncFlatSpec with SetupTest {

  private val currentContext = this.executionContext

  val service = new InMemoryBookLoanServiceImp(initState, currentContext)

  behavior of "in memory service"

  it should "get loans by employee with a valid id test" in {
     val loanSeqFuture: Future[Seq[Loan]] = service.showAllLoansByEmployeeId(1)
     loanSeqFuture.map { seq => assert(seq ===  Seq(loan1))}
  }

  it should "get loans by employee with an invalid id test" in {
    val loanSeqFuture: Future[Seq[Loan]] = service.showAllLoansByEmployeeId(-1)
    loanSeqFuture.map {seq => assert(seq.isEmpty)}
  }

  it should "get loans by book with a valid id test" in {
    val loanSeqFuture: Future[Seq[Loan]] = service.showAllLoansByBookId(2)
    loanSeqFuture.map { seq => assert(seq ===  Seq(loan1))}
  }

  it should "get loans by book with an invalid id test" in {
    val loanSeqFuture: Future[Seq[Loan]] = service.showAllLoansByBookId(-1)
    loanSeqFuture.map { seq => assert(seq.isEmpty)}
  }

  it should "return an empty option if no valid employ" in {
    val returnDate = now + 5 * dayInSeconds
    val loanFuture = service.loanBookToEmployee(book1, Employee(4, "Alex"), now, returnDate = returnDate)
    loanFuture.map(loanOpt => assert(loanOpt.isEmpty))
  }

  it should "return an empty option if no valid book" in {
    val returnDate = now + 5 * dayInSeconds
    val loanFuture = service.loanBookToEmployee(Book(-1, "BookInvalid", 3), employee1, now,  returnDate = returnDate)
    loanFuture.map(loanOpt => assert(loanOpt.isEmpty))
  }

  it should "return an empty option if no copy available" in {
    val returnDate = now + 5 * dayInSeconds
    val loanFuture = service.loanBookToEmployee(book2, employee1, now, returnDate = returnDate)
    loanFuture.map(loanOpt => assert(loanOpt.isEmpty))
  }

  it should "return a loan future and Update the state of the service" in {
    val returnDate = now + 5 * dayInSeconds
    val loanFuture = service.loanBookToEmployee(book3, employee1, now, returnDate = returnDate)
    loanFuture.map {loanOpt =>
      assert(loanOpt.isDefined)
      assert(loanOpt.get === Loan(4, book3, employee1, now, returnDate))
    }
  }

  it should "return an empty option if invalid employee" in {
    val returnFuture = service.returnBookFromEmployee(book2, Employee(4, "Alex"), now)
    returnFuture.map(returnType => assert(returnType.isEmpty))
  }

  it should "return an empty option if invalid book" in {
    val returnTypeFuture = service.returnBookFromEmployee(Book(-1, "InvalidBook", 4), employee1, now)
    returnTypeFuture.map(returnType => assert(returnType.isEmpty))
  }

  it should "return valid return if all okay" in {
    val returnTypeFuture = service.returnBookFromEmployee(book3, employee2, now + 3 * dayInSeconds)
    returnTypeFuture.map(returnType => assert(returnType === Some(ValidReturn())))
  }

  it should "return an InvalidReturn with delay" in {
    val returnTypeFuture = service.returnBookFromEmployee(book2, employee1, now + 8 * dayInSeconds)
    returnTypeFuture.map(returnType => assert(returnType === Some(InvalidReturn(3))))
  }
}
