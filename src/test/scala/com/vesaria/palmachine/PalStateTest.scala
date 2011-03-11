package com.vesaria.palmachine

import org.scalatest.FunSuite

class PalStateTest extends FunSuite {
	test("PalState isTerminable") {
		assert(PalState.Empty.isTerminable, "Empty PalState should be terminable")
		assert(PalState(true, "a").isTerminable, "One char string should be terminable")
		assert(!PalState(true, "ab").isTerminable, "Two char string non-symmetric should not be terminable")
		assert(PalState(true, "aa").isTerminable, "Two char string symmetric should be terminable")
		assert(!PalState(true, "aab").isTerminable, "Three char string non-symmetric should not be terminable")
		assert(PalState(true, "aba").isTerminable, "Three char string symmetric should be terminable")
	}
	
	test("PalState +operator") {
		// PalState operators are defined by these rules:
		assert(PalState.Empty + PalState(true, "a") === PalState(true, "a"), "0 +x --> +x")
		
		assert(PalState(true, "abcd") + PalState(false, "ab") === PalState(true, "cd"), "+xy -x --> +y")
		assert(PalState(true, "mno") + PalState(false, "mnopqrs") === PalState(false, "pqrs"), "+x -xy --> -y")
		
		assert(PalState(false, "abcd") + PalState(true, "ab") === PalState(false, "cd"), "-xy +x --> -y")
		assert(PalState(false, "mno") + PalState(true, "mnopqrs") === PalState(true, "pqrs"), "-x +xy --> +y")
		
		// All other operations are undefined (and will produce undefined results).
		
		// Additional cases:
		assert(PalState(true, "a") + PalState.Empty === PalState(true, "a"), "+x + 0 --> +x")
		assert(PalState(true, "xyz") + PalState(false, "xyz") === PalState.Empty, "+x -x --> 0")
		assert(PalState(false, "xyz") + PalState(true, "xyz") === PalState.Empty, "-x +x --> 0")
	}
}