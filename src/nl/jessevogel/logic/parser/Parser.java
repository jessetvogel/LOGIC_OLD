package nl.jessevogel.logic.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import nl.jessevogel.logic.basic.Alias;
import nl.jessevogel.logic.basic.Dummy;
import nl.jessevogel.logic.basic.Quantifier;
import nl.jessevogel.logic.basic.RelationType;
import nl.jessevogel.logic.basic.Type;
import nl.jessevogel.logic.basic.Variable;
import nl.jessevogel.logic.basic.VariableList;
import nl.jessevogel.logic.debug.Log;

public class Parser {
	
	public static final ArrayList<String> filenames = new ArrayList<String>();
	
	public static void loadFile(String filename) {
		// If the file was already loaded, stop
		if(filenames.contains(filename)) return;
		
		// If filename is a directory, parse all .math files in that directory
		File file = new File(filename);
		if(file.isDirectory()) {
			for(final File entry : file.listFiles()) {
				String path = entry.getAbsolutePath();
				int i = path.lastIndexOf('.');
				if(i < 0) continue;
				if(path.substring(i).equals(".math"))
					loadFile(path);
			}
			return;
		}
		
		// Otherwise, open the file and parse it
		Log.message("Reading '" + filename + "'");
		try {
			// Read contents of the file, and put it into one string
			BufferedReader br = new BufferedReader(new FileReader(filename));
			StringBuilder contents = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
					// Add line to the contents if it is not a comment and nonempty
					if(!line.equals("") && line.charAt(0) != '#') {
						contents.append(line);
						contents.append('\n');
					}
			}
			br.close();
			
			// Parse the file
			(new Parser(contents.toString()))
				.setDirectory(filename.substring(0, filename.lastIndexOf("/") + 1))
				.parseCommands();
			
		} catch (IOException e) {
			// Could not open the file
			Log.error("Unable to parse '" + filename + "'");
		}
		
		// Add this file to the list of filenames that have been loaded
		filenames.add(filename);
	}

	private String contents;
	private int length;
	private int pointer;

	public String directory;
	public boolean fail = false;
	private boolean allowDummies = false;
	private boolean appendToList = false;
	
	public Parser(String contents) {
		// Set file contents, its length and pointer to the start
		this.contents = contents;
		this.length = contents.length();
		this.pointer = 0;
	}
	
	public Parser setDirectory(String directory) {
		// Set directory and return this Object
		this.directory = directory;
		return this;
	}
	
	public Parser allowDummies(boolean allow) {
		// Set allowDummies and return this Object
		allowDummies = allow;
		return this;
	}
	
	public Parser appendToList(boolean allow) {
		// Set appendToList and return this Object
		appendToList = allow;
		return this;
	}
	
	private void skipWhiteSpace() {
		// Obvious
		while(pointer < length && Character.isWhitespace(contents.charAt(pointer)))
			pointer ++;
	}
	
	private String readWord() {
		// Skip all leading whitespace
		skipWhiteSpace();
		
		if(pointer == length) return null;
		
		// Special case: when we start with a '~', return that with the next word
		if(contents.charAt(pointer) == '~') {
			pointer ++;
			return "~" + readWord();
		}
		
		// Special cases: quantifiers "ForAll[...]" and "Exists[...]"
		if(length >= 8 && contents.indexOf("ForAll") == pointer) {
			pointer += 6;
			return "ForAll" + readWord();
		}
		
		if(length >= 8 && contents.indexOf("Exists") == pointer) {
			pointer += 6;
			return "Exists" + readWord();
		}
		
		// Begin reading at current value of pointer, and read until any nonword character appears
		int beginPointer = pointer;
		int depthRound = 0, depthCurly = 0, depthPointy = 0, depthSquare = 0;
		char c;
		countCommas = 0; // TODO: come up with a better solution than this
		while(pointer < length) {
			c = contents.charAt(pointer);
			
			if(depthRound == 0 && depthCurly == 0 && depthPointy == 0 && depthSquare == 0 && !(Character.isLetter(c) || c == '_') && pointer != beginPointer) {
				// Nonword character ([^A-Za-z0-9_] == \w) indicates end of word in case it is not the first character
				break;
			}
			
			// Update bracket count TODO: we need a better brakcet count, i.e. ')' when last bracket was a '[' should result in an error --> need stack
			if(c == '(') depthRound ++; if(c == ')') depthRound --; if(c == '{') depthCurly ++; if(c == '}') depthCurly --; if(c == '<') depthPointy ++; if(c == '>') depthPointy --; if(c == '[') depthSquare ++; if(c == ']') depthSquare --;
			
			if(depthRound == 0 && depthCurly == 0 && depthPointy == 0 && depthSquare == 0 && (c == ')' || c == '}' || c == '>' || c == ']')) {
				// In this case the closing bracket indicates the end of the word
				pointer ++;
				break;
			}
			
			if(depthRound == 0 && depthCurly == 0 && depthPointy == 0 && depthSquare == 1 && c == ',')
				countCommas ++;
			
			pointer ++;
		}
		
		if(!(depthRound == 0 && depthCurly == 0 && depthPointy == 0 && depthSquare == 0)) {
			// If one of the brackets was not closed, it must be due to reaching the end of the file
			Log.error("Unexpected end of file");
			return null;
		}
		
		// Return the part that was read
		return contents.substring(beginPointer, pointer);
	}
	
	private int countCommas;
	public String[] readArguments() {
		// Skip all leading whitespace
		skipWhiteSpace();
		
		// Check to make sure there is an '['
		if(contents.charAt(pointer) != '[') {
			Log.error("Expected '[', but was not found");
			return null;
		}
		
		// Read the part between '[' and ']'
		String argumentsString = readWord();
		if(argumentsString == null) return null;
		
		// Remove '[' and ']' and do some more stuff TODO: make this better
		argumentsString = argumentsString.substring(1, argumentsString.length() - 1);
		int length = argumentsString.length();
		String[] arguments = new String[countCommas + 1];
		int index = 0;
		int beginPointer = 0;
		int depthRound = 0, depthCurly = 0, depthPointy = 0, depthSquare = 0;
		char c;
		for(int i = 0;i < length;i ++) {
			c = argumentsString.charAt(i);
			if(c == '(') depthRound ++; if(c == ')') depthRound --; if(c == '{') depthCurly ++; if(c == '}') depthCurly --; if(c == '<') depthPointy ++; if(c == '>') depthPointy --; if(c == '[') depthSquare ++; if(c == ']') depthSquare --;
			if(depthRound == 0 && depthCurly == 0 && depthPointy == 0 && depthSquare == 0 && c == ',') {
				arguments[index] = argumentsString.substring(beginPointer, i).trim();
				index ++;
				beginPointer = i + 1;
			}
		}
		arguments[index] = argumentsString.substring(beginPointer).trim();
		return arguments;
	}
	
	public void parseCommands() {
		// As long as we did not reach the end of the file, continue searching for commands
		while(!fail && pointer < length) {
			// Read word
			String word = readWord();
			if(word == null) break;
			
			// Check if it is a command. If not, give an error
			if(!Command.check(word, this)) {
				fail = true;
				return;
			}
		}
	}
	
	private void variablePreprocessing() {
		boolean updated = true;
		int newLength;
		while(updated) {
			updated = false;
			
			// Try to trim
			contents = contents.trim();
			newLength = contents.length(); 
			if(length != newLength) {
				length = newLength;
				updated = true;
			}
			
			// If contents is a single word encapsulated by '(' ... ')', remove the brackets
			if(contents.charAt(0) == '(' && contents.charAt(length - 1) == ')' && isSingleWord()) {
				contents = contents.substring(1, length - 1);
				length -= 2;
				updated = true;
			}
		}
	}
	
	private boolean isSingleWord() {
		// Set pointer to 0. Read one word, if pointer ends up at the end, it is 1 word
		pointer = 0;
		readWord();
		if(pointer == length) {
			pointer = 0;
			return true;
		}
		else {
			pointer = 0;
			return false;
		}
	}
	
	public Variable parseVariable(VariableList variableList) {
		// Do some variable preprocessing
		variablePreprocessing();
		
		// Keep track of the variable we are parsing
		Variable variable = null;
		
		do { // Use a 'do' so we can break out of it
			
		// Check if it is one word
		if(isSingleWord()) {
			// If it starts with a '~', create a not gate
			if(contents.charAt(0) == '~') {
				pointer = 1;
				String nextWord = readWord();
				Variable nextVariable = (new Parser(nextWord))
						.allowDummies(allowDummies)
						.appendToList(appendToList)
						.parseVariable(variableList);
				
				// Make sure a Statement follows
				if(!Type.is(nextVariable, Type.STATEMENT)) {
					Log.error("Expected a Statement after '~', but was not found");
					return null;
				}
				
				variable = RelationType.create(RelationType.NOT, null, nextVariable);
				break;
			}
			
			// If allowed, check if it is a dummy variable
			if(contents.charAt(0) == '{' && contents.charAt(length - 1) == '}') { // TODO: I just thought of this: if charAt(0) == '{', then we MUST have charAt(length - 1) == '}' right? So no need to check for it?
				if(!allowDummies) {
					Log.error("No Dummy variables are allowed here");
					return null;
				}
				
				// Obtain the type and the name of the dummy variable 
				String[] parts = contents.substring(1, length - 1).trim().split("\\s+");
				if(parts.length != 2) {
					Log.error("Dummy variables have to be of format '{Type variable}'");
					return null;
				}
				
				// Create a new dummy variable
				variable = new Dummy();
				variable.type = Type.get(parts[0]);
				if(variable.type == null) {
					Log.error("Unknown Type " + parts[0]);
					return null;
				}
				String name = parts[1];
				variable.identifier = name;
				if(appendToList) {
					if(!variableList.let(name, variable))
						return null;
				}
				break;
			}
			
			// Check for quantifiers
			if(contents.indexOf("ForAll") == 0) {
				pointer = 6;
				String[] arguments = readArguments();
				if(arguments.length < 2) {
					Log.error("ForAll expects at least 2 arguments");
					return null;
				}
				// Create array of dummy variables
				Dummy dummies[] = new Dummy[arguments.length - 1];
				VariableList vl = variableList.duplicate();
				for(int i = 0;i < arguments.length - 1;i ++) {
					Variable dummy = (new Parser(arguments[i]))
							.allowDummies(true)
							.appendToList(true)
							.parseVariable(vl);
					if(!(dummy instanceof Dummy)) {
						Log.error("ForAll expects Dummy variable");
						return null;
					}
					dummies[i] = (Dummy) dummy;
				}
				Variable statement = (new Parser(arguments[arguments.length - 1]))
						.allowDummies(false)
						.appendToList(false)
						.parseVariable(vl);
				if(!Type.is(statement, Type.STATEMENT)) {
					Log.error("ForAll expects last argument to be a Statement");
					return null;
				}
				
				// Finally, create the Quantifier
				variable = new Quantifier(dummies, statement);
				variable.type = Type.FORALL;
				break;
			}
			
			if(contents.indexOf("Exists") == 0) {
				pointer = 6;
				String[] arguments = readArguments();
				if(arguments.length < 2) {
					Log.error("Exists expects at least 2 arguments");
					return null;
				}
				// Create array of dummy variables
				Dummy[] dummies = new Dummy[arguments.length - 1];
				VariableList vl = variableList.duplicate();
				for(int i = 0;i < arguments.length - 1;i ++) {
					Variable dummy = (new Parser(arguments[i]))
							.allowDummies(true)
							.appendToList(true)
							.parseVariable(vl);
					if(!(dummy instanceof Dummy)) {
						Log.error("Exists expects Dummy variable");
						return null;
					}
					dummies[i] = (Dummy) dummy;
				}
				Variable statement = (new Parser(arguments[arguments.length - 1]))
						.allowDummies(false)
						.appendToList(false)
						.parseVariable(vl);
				if(!Type.is(statement, Type.STATEMENT)) {
					Log.error("Exists expects last argument to be a Statement");
					return null;
				}
				
				// Finally, create the Quantifier
				variable = new Quantifier(dummies, statement);
				variable.type = Type.EXISTS;
				break;
			}
			
			// By default, try to find the variable in the variablelist. If it does not exists, give an error
			variable = variableList.get(contents);
			if(variable == null) {
				Log.error("Variable '" + contents + "' does not exist");
				return null;
			}
			break;
		}
		
		// Otherwise it is a compound, so parse it piece by piece 
		variable = parseSection(variableList);
		break;
		
		} while(false);
		
		// If no variable was parsed, or something went wrong, return null
		if(variable == null) return null;
		
		// Apply all aliases
		variable = Alias.applyAll(variable);
		
		// Set an identifier (in case it was not already set)
		if(variable.identifier == null || variable.identifier.equals(Alias.DEFAULT_IDENTIFIER))
			variable.identifier = contents;
		
		// Store the variable in a list in case that was asked
		if(appendToList) {
			if(variableList != null) {
				boolean alreadyDefined = false;
				for(Variable v : variableList.variables) {
					if(Variable.compare(variable, v)) {
						variable = v;
						alreadyDefined = true;
						break;
					}
				}
				if(!alreadyDefined)
					variableList.add(variable);
			}
		}
		
		// In the end, return the variable
		return variable;
	}

	private Variable parseSection(VariableList variableList) {
		// Keep track of the current variable
		Variable variable = null;
		
		while(!fail && pointer < length) {
			// Read word
			String word = readWord();
			
			// Try if it is a relation
			RelationType relationType = RelationType.get(word);
			if(relationType != null) {
				String nextWord = readWord();
				Variable first = variable;
				Variable second = (new Parser(nextWord))
						.allowDummies(allowDummies)
						.appendToList(appendToList)
						.parseVariable(variableList);
				variable = RelationType.create(relationType, first, second);
				if(variable == null) {
					Log.error("Unknown Relation '" + word + "' between " + first.type.name + " and " + second.type.name);
					return null;
				}
				continue;
			}
			
			// Otherwise, it may be the first word
			if(variable == null) {
				variable = (new Parser(word))
						.allowDummies(allowDummies)
						.appendToList(appendToList)
						.parseVariable(variableList);
				if(variable == null) return null;
				continue;
			}
			
			// Otherwise, we call it an unknwon relation
			Log.error("Unknown Relation '" + word + "'");
			return null;
		}
		
		return variable;
	}
	
	public boolean expectNumberOfArguments(String[] arguments, int amount) {
		// In case arguments is null, just return false
		if(arguments == null) return false;
		
		// Show an error when the number of arguments does not match the expected number of arguments
		if(arguments.length != amount) {
			// The only exception is for amount = 0 and arguments.length = 1 and arguments[0] = ""
			if(amount == 0 && arguments.length == 1 && arguments[0].equals("")) return true;
			
			// Otherwise, give an error
			Log.error("Expected " + amount + " argument" + (amount != 1 ? "s" : "") + ", but " + arguments.length + " were given");
			return false;
		}
		return true;
	}
}
