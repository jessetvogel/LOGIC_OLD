package nl.jessevogel.logic.basic;

import java.util.ArrayList;

import nl.jessevogel.logic.basic.Scope.Value;

public class Cases {
	public ArrayList<Variable> foundTrue, foundFalse;
	
	public Cases() {
		// Initially set the lists to null to indicate that the first case is coming 
		foundTrue = null;
		foundFalse = null;
	}
	
	public void filterScope(Scope scope) {
		// Stop if this scope encountered a contradiction
		if(scope.contradiction) return;
		
		// In the first case, create new arraylists
		if(foundTrue == null || foundFalse == null) {
			foundTrue = new ArrayList<Variable>();
			foundFalse = new ArrayList<Variable>();
			
			// Add statements that were deduced in this scope to the lists
			for(Variable s : scope.variableList.variables) {
				if(!Type.is(s, Type.STATEMENT)) continue;
				
				if(scope.getValue(s) == Value.TRUE && scope.deducedIn(s) == scope)
					foundTrue.add(s);
				
				if(scope.getValue(s) == Value.FALSE && scope.deducedIn(s) == scope)
					foundFalse.add(s);
			}
		}
		else {
			// In the non-first case, filter the lists
			int foundTrueSize = foundTrue.size();
			int foundFalseSize = foundFalse.size();
			
			// Remove all statements that are not true in this case 
			for(int i = 0;i < foundTrueSize;i ++) {
				Variable s = foundTrue.get(i);
				if(scope.getValue(s) != Value.TRUE || scope.deducedIn(s) != scope) {
					foundTrue.remove(s);
					foundTrueSize --;
					i --; // TODO: iterate through list instead of this sloppy way of doing it
				}
			}
			
			// Remove all statements that are not false in this case
			for(int i = 0;i < foundFalseSize;i ++) {
				Variable s = foundFalse.get(i);
				if(scope.getValue(s) != Value.FALSE || scope.deducedIn(s) != scope) {
					foundFalse.remove(s);
					foundFalseSize --;
					i --;  // TODO: iterate through list instead of this sloppy way of doing it
				}
			}
		}
	}

	public boolean finish(Scope scope) {
		// If foundTrue and foundFalse are still null, all of the possibilities ended up in contradictions. Therefore,
		if(foundTrue == null || foundFalse == null) {
			scope.contradiction = true;
			return true;
		}
		
		// All statements that were true or false in all cases, set them to be true in the given scope
		boolean deduced = false;
		for(Variable s : foundTrue)
			deduced |= scope.setValue(s, Value.TRUE);
		
		for(Variable s : foundFalse)
			deduced |= scope.setValue(s, Value.FALSE);
		
		return deduced;
	}
}
