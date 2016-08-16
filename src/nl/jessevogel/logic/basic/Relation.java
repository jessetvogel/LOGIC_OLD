package nl.jessevogel.logic.basic;

public class Relation extends Variable {
	public RelationType type;
	public Variable first, second;
	
	public Relation(RelationType type, Variable P, Variable Q) {
		super.type = type.type;
		this.type = type;
		this.first = P;
		this.second = Q;
	}
}
