package nl.jessevogel.logic.basic;

public class Quantifier extends Variable {
	
	public Dummy[] dummies;
	public Variable statement;
	
	public Quantifier(Dummy[] dummies, Variable statement) {
		// Store variablelist and the statement
		this.dummies = dummies;
		this.statement = statement;
	}
}
