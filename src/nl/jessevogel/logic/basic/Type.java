package nl.jessevogel.logic.basic;

import java.util.HashMap;

import nl.jessevogel.logic.debug.Log;

public class Type {
	// Store these types for performance
	public static Type VARIABLE;
	public static Type STATEMENT;
	public static Type FORALL;
	public static Type EXISTS;
	
	public static final HashMap<String, Type> map = new HashMap<String, Type>();
	
	
	public String name;
	public Type parent;
	
	public Type(String name, Type parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public static boolean add(String name, Type parent) {
		// If this Type was already defined, give an error
		if(map.containsKey(name)) {
			Log.error("Type '" + name + "' was already defined");
			return false;
		}
		
		// Create a new Type and put it in the map
		map.put(name, new Type(name, parent));
		return true;
	}

	public static Variable create(Type type) {
		// Create a new variable with the given type
		Variable variable = new Variable();
		variable.type = type;
		return variable;
	}

	public static Type get(String name) {
		// Return the Type that is in the map
		return map.get(name);
	}

	public static boolean is(Variable variable, Type type) {
		// Obvious
		if(variable == null) return type == null;
		return variable.type.childOf(type);
	}
	
	private boolean childOf(Type parent) {
		Type type = this;
		
		if(type == parent)
			return true;
		
		while(type.parent != null) {
			type = type.parent;
			if(type == parent)
				return true;
		}
		
		return false;
	}
}
