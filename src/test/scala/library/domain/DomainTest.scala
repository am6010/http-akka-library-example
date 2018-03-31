package library.domain

import library.SetupTest
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DomainTest extends FunSuite{

  test("insert a loan at state with loans map test") {
    new SetupTest {
      val newLoan = Loan(3, book3, employee2, now, now + 10 * dayInSeconds)
      val newLoans: Map[Int, Loan] = loans + (3 -> newLoan)
      val newState: State = initState.updateLoans(newLoans)

      assert(newState.books === initState.books, "Books collection should be the same")
      assert(newState.employees === initState.employees, "Employees collection should be the same")
      assert(newState.loans === newLoans, "The loan collection should have been updated with a new one")
    }
  }
}
