package com.vesaria.palmachine

/**
 * PalSeq is a sequence of characters on the left and right which can be used to assemble palindromes.
 * 
 * @param leftSide		The left side of the sequence
 * @param rightSide		The right side, in reverse
 *
 * For example, PalSeq("some", "somem").
 * Note that a PalSeq is used to assemble palindromes, but does _not_ need to be a palindrome itself.  
 * For example, PalSeq("plan", "amanap") is perfectly valid.
 *
 * @see PalState
 * @see Transition
 */ 
case class PalSeq(leftSide: String, rightSide: String) {
	
	/** Creates a new PalSeq by inserting that PalSeq into the _middle_ of this PalSeq */
	def + (that: PalSeq) = PalSeq(leftSide + that.leftSide, rightSide + that.rightSide)
	
	/** The PalState of this PalSeq */
	def state: PalState = {
		if (leftSide == "")  // This special case is necessary since we define PalState.Empty.polarity to be false
			PalState(false, rightSide)
		else
			PalState(true, leftSide) + PalState(false, rightSide)
	}
	
	def length = leftSide.length + rightSide.length
	
	override def toString = {
		"'%s_%s'".format(leftSide, rightSide.reverse)
	}
}
