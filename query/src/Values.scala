package scalasql.query

import scalasql.core.DialectBase
import scalasql.core.SqlStr.{Renderable, SqlStringSyntax}
import scalasql.core.{Queryable, Sql, SqlStr, TypeMapper, SubqueryRef}
import scalasql.core.Context

/**
 * A SQL `VALUES` clause, used to treat a sequence of primitive [[T]]s as
 * a [[Select]] query.
 */
class Values[Q, R](val ts: Seq[R])(
    implicit val qr: Queryable.Row[Q, R],
    protected val dialect: DialectBase
) extends Select.Proxy[Q, R] {
  assert(ts.nonEmpty, "`Values` clause does not support empty sequence")

  protected def selectSimpleFrom() = this.subquery
  val tableRef = new SubqueryRef(this, qr)
  protected def columnName(n: Int) = s"column${n + 1}"

  override val expr: Q = qr.deconstruct(ts.head)

  override protected def queryWalkLabels() = qr.walkExprs(expr).indices.map(i => List(i.toString))

  override protected def queryWalkExprs() = qr.walkExprs(expr)

  override protected def selectRenderer(prevContext: Context): scalasql.core.SelectBase.Renderer =
    new Values.Renderer(this)(implicitly, prevContext)

  override protected def selectLhsMap(prevContext: Context): Map[Sql.Identity, SqlStr] = {
    qr.walkExprs(expr)
      .zipWithIndex
      .map { case (e, i) => (Sql.exprIdentity(e), SqlStr.raw(columnName(i))) }
      .toMap
  }
}

object Values {
  class Renderer[Q, R](v: Values[Q, R])(implicit qr: Queryable.Row[Q, R], ctx: Context)
      extends scalasql.core.SelectBase.Renderer {
    def wrapRow(t: R): SqlStr = sql"(" + SqlStr.join(
      qr.walkExprs(qr.deconstruct(t)).map(i => sql"$i"),
      SqlStr.commaSep
    ) + sql")"
    def render(liveExprs: Option[Set[Sql.Identity]]): SqlStr = {
      val rows = SqlStr.join(v.ts.map(wrapRow), SqlStr.commaSep)
      sql"VALUES $rows"
    }

    def context = ctx
  }
}