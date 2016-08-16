package nl.jessevogel.logic.basic;

import java.util.ArrayList;
import java.util.HashMap;

public class Alias {
	public static final ArrayList<Alias> aliases = new ArrayList<Alias>();	
	public static final HashMap<Dummy, Variable> reference = new HashMap<Dummy, Variable>();
	
	public static final String DEFAULT_IDENTIFIER = "FROM ALIAS";
	
	public Variable source;
	public Variable destination;
	
	public Alias(Variable source, Variable destination) {
		// Copy source and destination
		this.source = source;
		this.destination = destination;
	}
	
	public static void add(Alias alias) {
		// Add alias to the beginning list
		aliases.add(0, alias);
	}
	
	public static Variable applyAll(Variable variable) {
		// Apply all aliases
		for(Alias a : aliases)
			variable = a.apply(variable);
		
		return variable;
	}
	
	public Variable apply(Variable variable) {
		// Clear references
		reference.clear();
		
		// If not a match, return false
		if(!match(source, variable)) return variable;
		
		// If it was, return the destination with references applied
		return output(destination);
	}
	
	public static Variable output(Variable variable) {
		// In case of a Dummy variable, return the reference
		if(variable instanceof Dummy) {
			return reference.get(variable);
		}
		
		// In case of a Relation, create a new Relation
		if(variable instanceof Relation) {
			Relation relation = (Relation) variable;
			variable = RelationType.create(relation.type, output(relation.first), output(relation.second));
			variable.identifier = DEFAULT_IDENTIFIER;
			return variable;
		}
		
		// Otherwise, just return the variable (before, set identifier to know where it came from)
		return variable;
	}
	
	public static boolean match(Variable source, Variable variable) {
		// If it is the Object, return true
		if(source == variable) return true;
		
		// In case source is a Dummy, check if variable is of the same type
		if(source instanceof Dummy) {
			if(Type.is(variable, source.type)) {
				// If the dummy was not yet referenced to some variable
				if(!reference.containsKey((Dummy) source)) {
					reference.put((Dummy) source, variable);
					return true;
				}
				else
				// If it was, it is only a match if it is referenced to this variable
					return reference.get((Dummy) source) == variable;
			}
		}
		
		// If they are both Relations, compare them
		if(source instanceof Relation && variable instanceof Relation) {
			Relation a = (Relation) source;
			Relation b = (Relation) variable;
			
			return a.type == b.type && match(a.first, b.first) && match(a.second, b.second);
		}
		
		// Otherwise, return false
		return false;
	}	
}
