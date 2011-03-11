package com.vesaria.palmachine

import org.scalatest.FunSuite

class TransitionTest extends FunSuite {
	test("Transition +operator") {
		assert(
			PalState.Empty.transitionVia(PalSeq("some","somem")) +
			PalState(false, "m").transitionVia(PalSeq("men", "")) ===
			PalState.Empty.transitionVia(PalSeq("somemen", "somem"))
		)
	}
	
	test("PalState.transitions") {
		assert(PalState(true, "some").transitions.exists(_.via == PalSeq("", "somem")), "+some --'    memos'--> -m")
	}
	
	test("Transition.nextTransitions") {
		assert(PalState.Empty.transitionVia(PalSeq("","somem")).nextTransitions.
						exists(_ == Transition(PalState.Empty, PalState(false, "m"), PalSeq("some", "somem")))
					)
	}
	
}