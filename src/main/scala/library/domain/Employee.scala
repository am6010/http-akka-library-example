package library.domain

/**
  * Employee Class
  * @param id of employee
  * @param name name of employee
  */
final case class Employee(id: Int,
                          name: String)

/**
  * Book case class
  * @param id of the book
  * @param title title of the book
  * @param numberOfCopies number of copies
  */
final case class Book(id: Int,
                      title: String,
                      numberOfCopies: Int)

/**
  * Loan class
  * @param id of the loan
  * @param book Book the was given
  * @param employee Employee who bored the book
  * @param loanDate date of loan timestamp
  * @param returnDate return date timestamp
  */
final case class Loan(id: Int,
                      book: Book,
                      employee: Employee,
                      loanDate: Long,
                      returnDate: Long)

/**
  * Return type when a book is returned
  */
sealed trait ReturnType

/**
  * Valid return on time
  */
case class ValidReturn(isDelayed: Boolean = false) extends ReturnType

/**
  * In case of a delay
  * @param delayDays the number of days
  */
case class InvalidReturn(delayDays: Int, isDelayed :Boolean = true) extends ReturnType


/**
  * State of the System
  * @param employees of the company
  * @param books offered books
  * @param loans current loans
  */
case class State(employees: Map[Int, Employee], books: Map[Int, Book], loans: Map[Int, Loan]) {
  def updateLoans(newLoans: Map[Int, Loan]): State = {
    State(employees, books, newLoans)
  }
}

object State {
  val alice =  Employee(1, "Alice")
  val kate =  Employee(2, "Kate")
  val bob =  Employee(3, "Bob")
  val employees = Map((1, alice), (2, kate), (3, bob))
  val scalaByExample  =  Book(1, "Scala by Example", 2)
  val algorithms =  Book(2, "Algorithms", 1)
  val scalaCookBook = Book(3, "Scala Cookbook", 2)
  val books = Map((1, scalaByExample), (2, algorithms), (3, scalaCookBook))

  val dayInSeconds: Long = 24 * 60 * 60L
  val now :Long = System.currentTimeMillis() / 1000
  val aliceAlgorithms = Loan(1, algorithms, alice, now - 2 * dayInSeconds, now + 5 * dayInSeconds)
  val bobScalaByExample = Loan(2, scalaByExample, bob, now - 1 * dayInSeconds, now + 7 * dayInSeconds)
  val kateScalaCookBook = Loan(3, scalaCookBook, kate, now - 2 * dayInSeconds, now + 5 * dayInSeconds)
  val loans = Map((1, aliceAlgorithms), (2, bobScalaByExample), (3, kateScalaCookBook))

  // Initial State of the System
  val initialState = State(employees, books, loans)

}
