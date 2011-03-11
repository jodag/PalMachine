package com.vesaria.palmachine

/**
 * Creates the Finite State Machine (FSM) 
 * corresponding to the set of all palindromes.
 */ 
object Fsm {
	
	/**
	 * When we encounter a nonterminable Transition, 
	 * we can either save it to the FSM immediately, 
	 * or explore it further and save all its children.  
	 * The end result is the same, but performance changes 
	 * drastically.  MaxSaveImmediatelyLength determines 
	 * the maximum length that we'll save immediately; 
	 * it should be at least 3.
	 */
	val MaxSaveImmediatelyLength = 3
	
	/** 
	 * Some applications are only interested in Transitions less than a MaxTransitionLength.
	 * Setting MaxTransitionLength to a nonzero value will filter them automatically;
	 * otherwise, set it to 0.
	 */
	val MaxTransitionLength = 65
	
	def main(args: Array[String]) { generateFsm() }
	
	/**
	 * Generates the complete FSM, starting from the empty string (i.e. state 0), 
	 * and saves it to the database.  See README.markdown and FSM.markdown for background.
	 */
	def generateFsm() { generateFsm(PalState.Empty) }
	
	/**
	 * Generates the complete FSM, starting from initialState, 
	 * and saves it to the database.
	 */
	def generateFsm(initialState: PalState) {
		saveCount = 0
		statesToExplore.clear()
		statesToExplore.push(initialState)
		
		while (!statesToExplore.isEmpty) {
			// [[TODO]] It would be great to do this in parallel for multicores
			val s = statesToExplore.pop()
			if (!visited(s)) {
				exploreState(s)
			} else println("Skipping state %s, already explored.".format(s))
		}
		
		println("%s distinct states visited, %s transitions saved".format(visitees.size, saveCount))
		
	}


	/** Explores PalState s, adding its Transition's to the FSM. */
	private def exploreState(s: PalState) {
		print("Exploring state %s ...".format(s))
		rememberVisit(s)
		val transitionsToExplore = new scala.collection.mutable.Stack[Transition]()
		transitionsToExplore ++= s.transitions.filter(isUsefulTransition)
		
		if (transitionsToExplore.isEmpty)
			println("no Transitions to explore.")
		else
			println("")
		
		while (!transitionsToExplore.isEmpty) {
			val t = transitionsToExplore.pop()
			if (shouldSaveTransition(t)) {
				println("\tSAVING %s".format(t))
				t.save()
				saveCount += 1
				statesToExplore.push(t.to)
			}
			else {
				println("\tExpanding %s...".format(t))
				transitionsToExplore ++= t.nextTransitions.filter(isUsefulTransition)
				}
			}
	}
	
	/** How many Transition's have we saved? For diagnostic purposes */
	private var saveCount = 0
	/** A stack for DFS of PalState's */
	private var statesToExplore = new scala.collection.mutable.Stack[PalState]
	/** Remember the visited PalState's so we don't visit them again */
	private val visitees = new scala.collection.mutable.HashSet[PalState]()
	private def visited(s: PalState) = visitees.contains(s)
	private def rememberVisit(s: PalState) { visitees += s }
	/** Some applications are only interested in Transitions less than a MaxTransitionLength */
	private def isUsefulTransition(t: Transition) = (MaxTransitionLength == 0 || t.via.length <= MaxTransitionLength)
	/** 
	 * For performance reasons, we sometimes save a Transition to the FSM immediately, 
	 * and sometimes expand it and save only its children.
	 * @see MaxSaveImmediatelyLength 
	 */
	private def shouldSaveTransition(t: Transition) = (t.to.isTerminable || t.to.length <= MaxSaveImmediatelyLength)
}