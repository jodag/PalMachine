package com.vesaria.palmachine.db

/**
 * A thin access layer to the Palmachine Database.
 */
object DataAccess {
	import java.sql._
	import javax.sql._
	
	private lazy val conn = DataConnection.connect()
	
	def prepare(sql: String) = conn.prepareStatement(sql)
	
	def selectStrings(stmt: PreparedStatement) : Iterable[String] = {
		var res = Set[String]()
		var count = 0
		val rows = stmt.executeQuery()
		while (rows.next()) {
			res += rows.getString(1)
			count += 1
		}
		rows.close()
		res
	}
	
	def selectStrings(stmt: PreparedStatement, param: String) : Iterable[String] = {
		stmt.setString(1, param)
		selectStrings(stmt)
	}
	
	private lazy val saveTransitionStmt = prepare("""
			INSERT INTO transition (
				from_polarity, 
				from_chars,
				to_polarity,
				to_chars,
				to_is_terminable,
				via_left_side,
				via_right_side)
				VALUES (?,?,?,?,?,?,?)
			""")
	
	def save(tran: Transition) {
		saveTransitionStmt.clearParameters()
		saveTransitionStmt.setBoolean(1, tran.from.polarity)
		saveTransitionStmt.setString(2, tran.from.chars)
		saveTransitionStmt.setBoolean(3, tran.to.polarity)
		saveTransitionStmt.setString(4, tran.to.chars)
		saveTransitionStmt.setBoolean(5, tran.to.isTerminable)
		saveTransitionStmt.setString(6, tran.via.leftSide)
		saveTransitionStmt.setString(7, tran.via.rightSide)
		saveTransitionStmt.executeUpdate()
	}
	
}

