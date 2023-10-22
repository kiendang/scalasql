package usql

import usql.query.{Aggregatable, Expr, Insert, Joinable, Select, Update}

class TableOps[V[_[_]]](t: Table[V]) extends Joinable[V[Expr]]{
  def select: Select[V[Expr]] = {
    val ref = t.tableRef
    Select.fromTable(t.metadata.vExpr(ref).asInstanceOf[V[Expr]], ref)(t.containerQr)
  }

  def update: Update[V[Column.ColumnExpr]] = {
    val ref = t.tableRef
    Update.fromTable(t.metadata.vExpr(ref), ref)(t.containerQr)
  }

  def insert: Insert[V[Column.ColumnExpr]] = {
    val ref = t.tableRef
    Insert.fromTable(t.metadata.vExpr(ref), ref)(t.containerQr)
  }

  def isTrivialJoin = true
}

trait Dialect {
  implicit def ExprBooleanOpsConv(v: Expr[Boolean]): operations.ExprBooleanOps =
    new operations.ExprBooleanOps(v)
  implicit def ExprIntOpsConv[T: Numeric](v: Expr[T]): operations.ExprNumericOps[T] =
    new operations.ExprNumericOps(v)
  implicit def ExprOpsConv(v: Expr[_]): operations.ExprOps = new operations.ExprOps(v)
  implicit def ExprStringOpsConv(v: Expr[String]): operations.ExprStringOps
  implicit def AggNumericOpsConv[V: Numeric](v: Aggregatable[Expr[V]])(implicit
      qr: Queryable[Expr[V], V]
  ): operations.AggNumericOps[V] =
    new operations.AggNumericOps(v)

  implicit def AggOpsConv[T](v: Aggregatable[T])(implicit
      qr: Queryable[T, _]
  ): operations.AggOps[T] =
    new operations.AggOps(v)

  implicit def SelectOpsConv[T](v: Select[T]): operations.SelectOps[T] =
    new operations.SelectOps(v)

  implicit def TableOpsConv[V[_[_]]](t: Table[V]): TableOps[V] = new TableOps(t)

}