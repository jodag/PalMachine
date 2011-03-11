package com.vesaria.palmachine

/**
 * PalState is the state of a PalSeq.
 * It uniquely determines what can be added to the PalSeq.
 *
 * For example, PalSeq("some", "somem") and PalSeq("ate", "atem")
 * both have PalState -m.  Thus, any PalSeq which can be added to one 
 * can be added to the other.
 *
 * @param chars		The unmatched characters of this state
 * @param polarity	Whether these chars are on the left side (true),
 * 								or backwards on the right (false).
 * 								PalState.Empty.polarity is defined as false.
 *
 * @see PalSeq
 */ 
case class PalState(polarity: Boolean, chars: String) {
	import PalState._
	val length = chars.length
	
	// PalState.Empty in truth has no polarity.
	// To make implementation easier, we arbitrarily define it as negative.
	// [[REFACTOR]] This does cause a lot of special case checks in the code, though - consider refactoring.
	require(!polarity || length > 0, "The empty string PalState (PalState.Empty) is defined as negative")
	
	/**
	 *  Is this PalState terminable?
	 *  That is, if we stop at this state, do we have a complete palindrome?
	 */
	lazy val isTerminable = {
		val midpoint = length / 2 // Rounds down
		val leftS = chars.substring(0, midpoint)
		val rightS = chars.substring(if (length % 2 == 0) midpoint else midpoint + 1).reverse.toString // reverse gives a RichString, which isn't == a String
		leftS == rightS
	}
	
	/** 
	 * Adds that PalState to this PalState to yield a new PalState.
	 * 
	 * The addition follows this identity:
	 * (PalSeqA + PalSeqB).state == PalSeqA.state + PalSeqB.state
	 *
	 * This allows us to determine the state of a sum of PalSeq's by only 
	 * looking at their states; this observation is what greatly simplifies the FSM.
	 * 
	 * PalState addition is defined by these rules:
	 *		0 +x --> +x
	 *		+xy -x --> +y
	 *		+x -xy --> -y
	 *		-xy +x --> -y
	 *		-x +xy --> +y
	 *	
	 * (x and y are sequences of 0 or more chars; 0 is the empty string.)
	 *
	 *	All other additions are undefined (and will produce undefined results).
	 *
	 * All these operations can be viewed as cases of one commutative operation:
	 * 		SUM(+-xy, -+x) = +-y
	 * or, rewriting more clearly:
	 * 		SUM(longer, shorther) = PalState(longer.polarity, longer.removeFromBeginning(shorter))
	 * SUM is associative, commutative, and symmetric with respect to polarity.  (This definition treats 0 as being of either polarity.)
	 *
	 */
	def + (that: PalState) : PalState = {
		assert(isSumDefined(that), "Sum of %s and %s is undefined".format(this, that))
		
		if (this.length > that.length)
			PalState(this.polarity, this.chars.substring(that.length, this.length))
		else if (this.length < that.length)
			that + this
		else
			PalState.Empty
	}
	
	def isSumDefined(that: PalState) = {
		this.isEmpty || that.isEmpty || (
			this.polarity != that.polarity &&
				(this.chars.startsWith(that.chars) || 
				that.chars.startsWith(this.chars))
			)
	}
	
	/** A Transition moving from this PalState to a new PalState via palSeq */
	def transitionVia(palSeq: PalSeq) = Transition(this, this + palSeq.state, palSeq)
	
	/** The set of all Transitions in the database starting from this PalState */
	def transitions = {
		// [[REFACTOR]] It would be nice to clean this up by merging the select from pos and from neg, 
		// and by having them return PalSeq's and not just String's
		val ws = db.DataAccess.selectStrings(if (polarity) SelectNextFromPos else SelectNextFromNeg, chars)
		ws.map({w: String =>
			val p = if (polarity) PalSeq("", w) else PalSeq(w, "")
			transitionVia(p)
		})
	}
	
	def isEmpty = length == 0
	
	override def toString = {
		if (length==0)
			"0"
		else
			(if (polarity) '+' else '-') + chars
	}
}

object PalState {
	/** The Empty PalState */
	val Empty = PalState(false, "")
	
	private lazy val SelectNextFromPos = db.DataAccess.prepare("SELECT next_from_pos(?)")
	private lazy val SelectNextFromNeg = db.DataAccess.prepare("SELECT next_from_neg(?)")
}