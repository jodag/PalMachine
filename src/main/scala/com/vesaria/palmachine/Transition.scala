package com.vesaria.palmachine

/**
 * A Transition is an edge of the FSM, showing how by emitting a PalSeq, 
 * we can move from one PalState to a new PalState.
 *
 * @param from 	The PalState we start at
 * @param to 		The PalState we move to
 * @param via 		A PalSeq we emit to do so
 */ 
case class Transition(from: PalState, to: PalState, via: PalSeq) {

	def + (that: Transition) = {
		assert(this.to == that.from)
		Transition(this.from, that.to, this.via + that.via)
	}
	
	def nextTransitions = {
		to.transitions.map(this + _)
	}
	
	/** Saves this Transition to the database */
	def save() {
		db.DataAccess.save(this)
	}
	
	override def toString = "[%s --%s--> %s%s]".format(from, via, to, (if (to.isTerminable) "!" else ""))
}
