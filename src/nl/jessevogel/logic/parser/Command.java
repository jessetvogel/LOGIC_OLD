package nl.jessevogel.logic.parser;

import nl.jessevogel.logic.basic.Alias;
import nl.jessevogel.logic.basic.RelationType;
import nl.jessevogel.logic.basic.Variable;
import nl.jessevogel.logic.basic.Scope.Value;
import nl.jessevogel.logic.basic.Type;
import nl.jessevogel.logic.basic.VariableList;
import nl.jessevogel.logic.debug.Log;
import nl.jessevogel.logic.prover.Prover;

public class Command {
	
	public static boolean check(String word, Parser parser) {
		// Check if word equals any of the known commands
		if("Include".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 1))
				return Command.include(parser.directory + arguments[0]);
			return false;
		}
		
		if("Assume".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 1)) {
				Variable argument = (new Parser(arguments[0]))
						.appendToList(true)
						.parseVariable(Prover.mainScope.variableList);
				if(!Type.is(argument, Type.STATEMENT)) {
					Log.error("Expected argument of type Statement, but was given of type " + argument.type.name + " (" + argument.identifier + ")");
					return false;
				}
				return Command.assume(argument);
			}
			return false;
		}
		
		if("Goal".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 1)) {
				Variable argument = (new Parser(arguments[0]))
						.appendToList(true)
						.parseVariable(Prover.mainScope.variableList);
				if(!Type.is(argument, Type.STATEMENT)) {
					Log.error("Expected argument of type Statement, but was given of type " + argument.type.name + " (" + argument.identifier + ")");
					return false;
				}
				return Command.goal(argument);
			}
			return false;
		}
		
		if("Let".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 2)) {
				Type type = Type.get(arguments[1]);
				if(type == null) {
					Log.error("Unknown Type '" + arguments[1] + "'");
					return false;
				}
				if(!arguments[0].matches("^[A-Za-z_]$")) {
					Log.error("'" + arguments[0] + "' is not a valid name");
					return false;
				}
				return Command.let(arguments[0], Type.create(type));
			}
			return false;
		}
		
		if("Clear".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 0))
				return Command.clear();
			return false;
		}
		
		if("Prove".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 0))
				return Command.prove();
			return false;
		}
		
		if("Exit".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 0))
				return Command.exit();
			return false;
		}
		
		if("DefineType".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 2))
				return Command.defineType(arguments[0], Type.get(arguments[1]));
			return false;
		}
		
		if("DefineRelation".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 4)) {
				String name = arguments[0];
				Type type = Type.get(arguments[1]);
				Type firstType = Type.get(arguments[2]);
				Type secondType = Type.get(arguments[3]);
				if(type == null) {
					Log.error("Unknown type '" + arguments[1] + "'");
					return false;
				}
				if(firstType == null) {
					Log.error("Unknown type '" + arguments[2] + "'");
					return false;
				}
				if(secondType == null) {
					Log.error("Unknown type '" + arguments[3] + "'");
					return false;
				}
				return Command.defineRelation(name, type, firstType, secondType);
			}
			return false;
		}
		
		if("Alias".equals(word)) {
			String[] arguments = parser.readArguments();
			if(parser.expectNumberOfArguments(arguments, 2)) {
				VariableList vl = Prover.mainScope.variableList.duplicate();
				Variable source = (new Parser(arguments[0]))
						.allowDummies(true)
						.appendToList(true)
						.parseVariable(vl);
				Variable destination = (new Parser(arguments[1]))
						.parseVariable(vl);
				return Command.alias(source, destination);
			}
			return false;
		}
		
		// If none of the above triggered, it is an unknown command
		Log.error("Unknown command '" + word + "'");
		return false;
	}
	
	public static boolean include(String path) {
		// Parse the given file
		Parser.loadFile(path);
		return true;
	}
	
	public static boolean assume(Variable assumption) {
		// Assume the statement to be true in the main scope 
		Prover.mainScope.setValue(assumption, Value.TRUE);
		return true;
	}

	public static boolean goal(Variable goal) {
		// Add this goal to the list of goals of Prover
		Prover.addGoal(goal);
		return true;
	}

	public static boolean let(String name, Variable variable) {
		// Let name be of type type
		variable.identifier = name;
		return Prover.mainScope.variableList.let(name, variable);
	}

	public static boolean clear() {
		// Delete all variables, statements, assumptions and goals (but keep Types, Relations etc.)
		Prover.mainScope.variableList.clear();
		Prover.clearGoals();
		Prover.mainScope.reset();
		return true;
	}

	public static boolean prove() {
		// Run Prover
		Prover.run();
		return true;
	}

	public static boolean exit() {
		// Exit program
		Log.message("Exit program");
		System.exit(0);
		return true;
	}

	public static boolean defineType(String name, Type type) {
		// Add a Type with parent type
		return Type.add(name, type);
	}

	public static boolean defineRelation(String name, Type type, Type firstType, Type secondType) {
		// Add a RelationType
		return RelationType.add(name, type, firstType, secondType);
	}
	
	public static boolean alias(Variable source, Variable destination) {
		// Create a new alias and add it to the list
		Alias alias = new Alias(source, destination);
		Alias.add(alias);
		return true;
	}
}
