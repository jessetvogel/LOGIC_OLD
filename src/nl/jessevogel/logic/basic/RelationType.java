package nl.jessevogel.logic.basic;

import java.util.HashMap;

import nl.jessevogel.logic.debug.Log;

public class RelationType {
	// Store these types for performance
	public static RelationType NOT;
	
	public static final HashMap<String, RelationType> map = new HashMap<String, RelationType>();

	public String name;
	public Type type;
	public Type firstType;
	public Type secondType;
	
	
	public RelationType(String name, Type type, Type firstType, Type secondType) {
		// Copy name and types
		this.name = name;
		this.type = type;
		this.firstType = firstType;
		this.secondType = secondType;
	}

	public static boolean add(String name, Type type, Type firstType, Type secondType) {
		// If this Type was already defined, give an error
		if(map.containsKey(name)) {
			Log.error("RelationType '" + name + "' was already defined");
			return false;
		}
		
		// Create a new RelationType and put it in the map
		map.put(name, new RelationType(name, type, firstType, secondType));
		return true;
	}
	
	public static Relation create(RelationType relationType, Variable first, Variable second) {
		if(relationType == null) return null;
		
		// Check if first and second have the right types
		if(!Type.is(first, relationType.firstType)) {
			Log.error("Relation '" + relationType.name + "' has " + relationType.firstType.name + " as first type, but " + first.type.name + " was given");
			return null;
		}
		if(!Type.is(second, relationType.secondType)) {
			Log.error("Relation '" + relationType.name + "' has " + relationType.secondType.name + " as second type, but " + second.type.name + " was given");
			return null;
		}
		// If so, return the Relation
		return new Relation(relationType, first, second);
	}

	public static RelationType get(String word) {
		// Return the RelationType in the map
		return map.get(word);
	}
	
}
