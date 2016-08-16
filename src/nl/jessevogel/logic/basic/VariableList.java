package nl.jessevogel.logic.basic;

import java.util.ArrayList;
import java.util.HashMap;

import nl.jessevogel.logic.debug.Log;

public class VariableList {
	public ArrayList<Variable> variables;
	public HashMap<String, Variable> map;
	
	public VariableList() {
		// Create a list and a map
		variables = new ArrayList<Variable>();
		map = new HashMap<String, Variable>();
	}
	
	public Variable get(String name) {
		// Return from map
		return map.get(name);
	}

	public void add(Variable variable) {
		// Add this variable to the list (if it wasn't already)
		if(variables.contains(variable)) return;
		variables.add(variable);

		// Also add 'children' if needed
		if(variable instanceof Relation) {
			Relation relation = (Relation) variable;
			if(relation.first != null) add(relation.first);
			if(relation.second != null) add(relation.second);
		}
	}
	
	public boolean let(String name, Variable variable) {
		// If this name was already used, give an error
		if(map.containsKey(name)) {
			Log.error("Variable '" + name + "' was already defined");
			return false;
		}
		
		// Store this variable in the map and add it to the list of variables
		map.put(name, variable);
		variables.add(variable);
		return true;
	}

	public void removeDummies() {
		// Go through the whole list, and remove all Dummy variables
		int size = variables.size();
		for(int i = 0;i < size;i ++) {
			Variable variable = variables.get(i);
			if(variable instanceof Dummy) {
				variables.remove(i);
				map.remove(variable.identifier);
				size --;
				i --;
			}
		}
	}
	
	public void clear() {
		// Clear the list and the map
		map.clear();
		variables.clear();
	}

	public VariableList duplicate() {
		// Create a new VariableList and append all values of this to that
		VariableList variableList = new VariableList();
		variableList.variables.addAll(variables);
		variableList.map.putAll(map);
		return variableList;
	}
}
