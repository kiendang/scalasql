package scalasql.query

import scalasql._
import utest._

import java.time.LocalDate

/**
 * Tests for basic insert operations
 */
trait DeleteTests extends ScalaSqlSuite {
  override def utestBeforeEach(path: Seq[String]): Unit = checker.reset()
  def tests = Tests {
    test("single") {
      checker(
        query = Purchase.delete(_.id === 2),
        sql = "DELETE FROM purchase WHERE purchase.id = ?",
        value = 1
      )

      checker(
        query = Purchase.select,
        value = Seq(
          Purchase[Id](id = 1, shippingInfoId = 1, productId = 1, count = 100, total = 888.0),
          // id==2 got deleted
          Purchase[Id](id = 3, shippingInfoId = 1, productId = 3, count = 5, total = 15.7),
          Purchase[Id](id = 4, shippingInfoId = 2, productId = 4, count = 4, total = 493.8),
          Purchase[Id](id = 5, shippingInfoId = 2, productId = 5, count = 10, total = 10000.0),
          Purchase[Id](id = 6, shippingInfoId = 3, productId = 1, count = 5, total = 44.4),
          Purchase[Id](id = 7, shippingInfoId = 3, productId = 6, count = 13, total = 1.3)
        )
      )
    }
    test("multiple") {
      checker(
        query = Purchase.delete(_.id !== 2),
        sql = "DELETE FROM purchase WHERE purchase.id <> ?",
        value = 6
      )

      checker(
        query = Purchase.select,
        value = Seq(
          Purchase[Id](id = 2, shippingInfoId = 1, productId = 2, count = 3, total = 900.0)
        )
      )
    }
    test("all") {
      checker(
        query = Purchase.delete(_ => true),
        sql = "DELETE FROM purchase WHERE ?",
        value = 7
      )

      checker(
        query = Purchase.select,
        value = Seq[Purchase[Id]](
          // all Deleted
        )
      )
    }
  }
}