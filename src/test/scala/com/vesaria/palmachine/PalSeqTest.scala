package com.vesaria.palmachine

import org.scalatest.FunSuite

class PalSeqTest extends FunSuite {
	test("PalSeq +operator") {
		assert(PalSeq("amana", "") + PalSeq("plan", "amanap") === PalSeq("amanaplan", "amanap"))
	}
	
	test("PalSeq.state") {
		assert(PalSeq("","").state === PalState.Empty)
		assert(PalSeq("aman","").state === PalState(true, "aman"))
		assert(PalSeq("","amanap").state === PalState(false, "amanap"))
		assert(PalSeq("aman","amanap").state === PalState(false, "ap"))
	}
}