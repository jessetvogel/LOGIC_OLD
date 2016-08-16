package nl.jessevogel.logic.prover;

import nl.jessevogel.logic.basic.RelationType;
import nl.jessevogel.logic.basic.Type;
import nl.jessevogel.logic.debug.Log;
import nl.jessevogel.logic.parser.Parser;

public class Initializer {
	
	public static final String MAIN_FILE = "/Users/Jesse/Desktop/Logic/main.math";
	
	public static void initialize() {
		// Welcome!
		Log.message("Initializing");
		
		// Add basic types and relations
		Type.add("Variable", null);
		Type.VARIABLE = Type.get("Variable");
		Type.add("Statement", Type.VARIABLE);
		Type.STATEMENT = Type.get("Statement");
		Type.add("ForAll", Type.STATEMENT);
		Type.FORALL = Type.get("ForAll");
		Type.add("Exists", Type.STATEMENT);
		Type.EXISTS = Type.get("Exists");
		
		RelationType.add("or",  Type.STATEMENT, Type.STATEMENT, Type.STATEMENT);
		RelationType.add("and", Type.STATEMENT, Type.STATEMENT, Type.STATEMENT);
		RelationType.add("xor", Type.STATEMENT, Type.STATEMENT, Type.STATEMENT);
		RelationType.add("~", Type.STATEMENT, null, Type.STATEMENT);
		RelationType.NOT = RelationType.get("~");
		
		// Parse main.math
		Parser.loadFile(MAIN_FILE);
	}
}
