package nl.jessevogel.logic.basic;

public class Variable {
	public Type type;
	public String identifier;
	
	public Variable() {
		// Set type
		type = Type.VARIABLE;
	}
	
	public static boolean compare(Variable a, Variable b) {
		// If they are the same object, return true
		if(a == b) return true;
		if(a == null && b != null) return false;
		if(a != null && b == null) return false;
		
		// If they have different types, return false
		if(a.type != b.type) return false;
		
		// If they are not relations, return false (for reasons, "echt waar het klopt wel vertrouw me maar, groetjes Jesse")
		if(!(a instanceof Relation) || !(b instanceof Relation)) return false;
		
		// If firsts and seconds are the same, return true
		Relation relationA = (Relation) a;
		Relation relationB = (Relation) b;
		return compare(relationA.first, relationB.first) && compare(relationA.second, relationB.second);
	}
}
