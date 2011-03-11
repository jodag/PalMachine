package com.vesaria.palmachine.db

object DataConnection {
	import java.sql._
	import javax.sql._
	
	val dsUrl = "jdbc:postgresql:%s?user=palmachine&password=palmachine".format("palmachine")
	
	private var isConnected = false
	
	def connect() : Connection = connect(dsUrl)
	
	def connect(dsUrl: String) = {
		if (isConnected) close()
		
		Class.forName("org.postgresql.Driver")
		print("Connecting to database " + dsUrl + "... ")
		val c = DriverManager.getConnection(dsUrl)
		isConnected = true
		println("connected.")
		c
	}	
	
	def close() { conn.close() }
	
	lazy val conn = connect()
		
	override def finalize { close() }
}

