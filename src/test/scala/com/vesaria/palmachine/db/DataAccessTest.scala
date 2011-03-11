package com.vesaria.palmachine.db

import org.scalatest.FunSuite

class DataAccessTest extends FunSuite {
	test("DataAccess prepare()") {
		DataAccess.prepare("SELECT 0")
	}
	
	test("DataAccess selectStrings()") {
		assert(DataAccess.selectStrings(DataAccess.prepare("SELECT 'hello'")).toList === List("hello"), "simple SELECT")
		assert(DataAccess.selectStrings(DataAccess.prepare("SELECT ?"), "goodbye").toList === List("goodbye"), "parameterized SELECT")
		assert(DataAccess.selectStrings(DataAccess.prepare("SELECT w FROM d WHERE w LIKE ?"), "meta%").toList.exists(_ == "metal"), "multirow parameterized SELECT")
	}
}