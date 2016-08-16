package nl.jessevogel.logic.basic;

import java.util.HashMap;
import java.util.Map.Entry;

import nl.jessevogel.logic.debug.Log;
import nl.jessevogel.logic.prover.Deduce;
import nl.jessevogel.logic.prover.Prover;

public class Scope {
	public enum Value { TRUE, FALSE, UNKNOWN }
	
	public VariableList variableList;
	public HashMap<Variable, Value> values;
	public Scope parent;
	public int depth;
	public boolean contradiction;
	
	public Scope(Scope parent) {
		if(parent == null) {
			// Main scope
			depth = 0;
			values = new HashMap<Variable, Value>();
			variableList = new VariableList();
			contradiction = false;
		}
		else {
			// Child scope, copy all values and set parent
			depth = parent.depth + 1;
			values = new HashMap<Variable, Value>();
			values.putAll(parent.values);
			variableList = parent.variableList.duplicate();
			contradiction = parent.contradiction;
			this.parent = parent;
		}
	}
	
	public boolean deduce(int assumptionsAllowed) {
		// Keep looping through the statement lists as long as new insights are deduced
		boolean deduced;
		do {
			deduced = false;
			for(Variable variable : variableList.variables) {
				if(Type.is(variable, Type.STATEMENT)) {
					deduced |= Deduce.deduce(this, variable, assumptionsAllowed);
				}
				if(contradiction) break;
			}
		}
		while(deduced && !contradiction);
		
		// If a contradiction appeared in the main scope, give an error
		if(contradiction && this == Prover.mainScope) {
			Log.error("Contradiction in main scope!");
			return false;
		}
		
		return deduced;
	}
	
	public Scope deducedIn(Variable P) {
		// Check if the value is known in this scope, if it is not return null
		Value valueP = getValue(P);
		if(valueP == Value.UNKNOWN) return null;
		
		// If in main scope, return this
		if(parent == null) return this;
		
		// Otherwise, check if the value is known in the parent scope
		Scope x = parent.deducedIn(P);
		if(x == null) return this; else return x;
	}
	
	public Value getValue(Variable P) {
		// Check if P is of type Statement TODO: maybe remove this block, since it only slows down performance
		if(!Type.is(P, Type.STATEMENT)) {
			Log.error("Variable '" + P.identifier + "' is not of type Statement");
			return Value.UNKNOWN;
		}
		
		// Obtain value from the HashMap, in case it is not found, it is UNKNOWN
		if(values.containsKey(P)) return values.get(P); else return Value.UNKNOWN;
	}
	
	public boolean setValue(Variable P, Value value) {
		// Check if P is of type Statement TODO: maybe remove this block, since it only slows down performance
		if(!Type.is(P, Type.STATEMENT)) {
			Log.error("Variable '" + P.identifier + "' is not of type Statement");
			return false;
		}
		
		// If this fact was already known, just return false
		Value valueP = getValue(P);
		if(valueP == value) {
			return false;
		}
		
		// If the value of P was unknown, set the value and return true
		if(valueP == Value.UNKNOWN) {
			values.put(P, value);
//			Log.debug("SCOPE '" + depth + "': '" + P.identifier + "' was set to " + (value == Value.TRUE ? "true" : "false"));
			Deduce.deduce(this, P, 0); // See if any direct deductions can be made for performance
			return true;
		}
		
		// If this point is reached, we must have P.value = !value, and thus a contradiction
//		Log.debug("Contradiction in scope '" + depth + "'! '" + P.identifier + "' was set to " + (value == Value.TRUE ? "TRUE" : "FALSE") + " while it was already " + (value == Value.TRUE ? "FALSE" : "TRUE"));
		
		contradiction = true;
		return true;
	}
	
	public void reset() {
		// Reset the values according to its parent
		values.clear();
		if(parent != null)
			values.putAll(parent.values);
		
		// Reset contradiction
		contradiction = false;
	}
	
	public void show() {
		for(Entry<Variable, Value> entry : values.entrySet()) {
			System.out.println("'" + entry.getKey().identifier + "' = " + (entry.getValue() == Value.TRUE ? "true" : "false"));
//			VariablePrinter.print(entry.getKey());
//			System.out.println();
		}
	}
	
	public static Scope deepest(Scope a, Scope b) {
		if(a == null) return b;
		if(b == null) return a;
		if(a.depth > b.depth) return a; else return b;
	}

	public static Scope highest(Scope a, Scope b) {
		if(a == null) return b;
		if(b == null) return a;
		if(a.depth > b.depth) return b; else return a;
	}
}
