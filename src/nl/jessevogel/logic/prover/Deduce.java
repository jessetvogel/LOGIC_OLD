package nl.jessevogel.logic.prover;

import nl.jessevogel.logic.basic.Alias;
import nl.jessevogel.logic.basic.Cases;
import nl.jessevogel.logic.basic.Quantifier;
import nl.jessevogel.logic.basic.Relation;
import nl.jessevogel.logic.basic.Scope;
import nl.jessevogel.logic.basic.Type;
import nl.jessevogel.logic.basic.Variable;
import nl.jessevogel.logic.basic.Scope.Value;

public class Deduce {
	
	public static boolean deduce(Scope scope, Variable variable, int assumptionsAllowed) {
		// In case of a Relation
		if(variable instanceof Relation) {
			// Treat or, and, xor as special cases
			Relation relation = (Relation) variable;
			if(relation.type.name.equals("or"))
				return deduceOr(scope, relation, assumptionsAllowed);
			
			if(relation.type.name.equals("and"))
				return deduceAnd(scope, relation, assumptionsAllowed);
			
			if(relation.type.name.equals("xor"))
				return deduceXor(scope, relation, assumptionsAllowed);
			
			if(relation.type.name.equals("~"))
				return deduceNot(scope, relation, assumptionsAllowed);
		}
		
		// In case of a Quantifier
		if(variable instanceof Quantifier) {
			if(Type.is(variable, Type.FORALL))
				return deduceForAll(scope, (Quantifier) variable, assumptionsAllowed);
			
			if(Type.is(variable, Type.EXISTS))
				return deduceExists(scope, (Quantifier) variable, assumptionsAllowed);
		}
		
		// In case of a Statement
		if(Type.is(variable, Type.STATEMENT)) {
			return deduceStatement(scope, variable, assumptionsAllowed);
		}
		
		return false;
	}

	private static boolean deduceStatement(Scope scope, Variable statement, int assumptionsAllowed) {
		if(scope.getValue(statement) != Value.UNKNOWN) return false;
		if(assumptionsAllowed <= 0) return false;
		
		// Assume the statement to be true and false as seperate cases, and see if any deductions can be made from those cases
		Cases cases = new Cases();
		
		Scope newScope = new Scope(scope);
		newScope.setValue(statement, Value.TRUE);
		newScope.deduce(assumptionsAllowed - 1);
		cases.filterScope(newScope);
		
		newScope = new Scope(scope);
		newScope.setValue(statement, Value.FALSE);
		newScope.deduce(assumptionsAllowed - 1);
		cases.filterScope(newScope);
		
		return cases.finish(scope);
	}

	private static boolean deduceOr(Scope scope, Relation or, int assumptionsAllowed) {
		// Keep track of if anything was deduced
		boolean deduced = false;
		
		// Direct deduction
		Value valueThis = scope.getValue(or);
		Value valueP    = scope.getValue(or.first);
		Value valueQ    = scope.getValue(or.second);
		Scope scopeThis = scope.deducedIn(or);
		Scope scopeP    = scope.deducedIn(or.first);
		Scope scopeQ    = scope.deducedIn(or.second);
		
		if(valueThis == Value.TRUE && valueP == Value.FALSE) deduced |= Scope.deepest(scopeThis, scopeP).setValue(or.second, Value.TRUE);
		if(valueThis == Value.TRUE && valueQ == Value.FALSE) deduced |= Scope.deepest(scopeThis, scopeQ).setValue(or.first, Value.TRUE);
		
		if(valueThis == Value.FALSE) {
			deduced |= scopeThis.setValue(or.first, Value.FALSE);
			deduced |= scopeThis.setValue(or.second, Value.FALSE);
		}
		
		if(valueP == Value.TRUE) deduced |= scopeP.setValue(or, Value.TRUE);
		if(valueQ == Value.TRUE) deduced |= scopeQ.setValue(or, Value.TRUE);
		
		if(valueP == Value.FALSE && valueQ == Value.FALSE) deduced |= Scope.deepest(scopeP, scopeQ).setValue(or, Value.FALSE);
		
		// In case value = TRUE, there are three possibilities: (0, 1), (1, 0), (1, 1)
		if(valueThis == Value.TRUE && valueP == Value.UNKNOWN && valueQ == Value.UNKNOWN && assumptionsAllowed > 0) {
			Cases cases = new Cases();
			
			// Case (0, 1)
			Scope case1 = new Scope(scope);
			case1.setValue(or.first, Value.FALSE);
			case1.setValue(or.second, Value.TRUE);
			case1.deduce(assumptionsAllowed - 1);
			if(!case1.contradiction) cases.filterScope(case1);
			
			// Case (1, 0)
			Scope case2 = new Scope(scope);
			case2.setValue(or.first, Value.TRUE);
			case2.setValue(or.second, Value.FALSE);
			case2.deduce(assumptionsAllowed - 1);
			if(!case2.contradiction) cases.filterScope(case2);
			
			// Case (1, 1)
			Scope case3 = new Scope(scope);
			case3.setValue(or.first, Value.TRUE);
			case3.setValue(or.second, Value.TRUE);
			case3.deduce(assumptionsAllowed - 1);
			if(!case3.contradiction) cases.filterScope(case3);
			
			cases.finish(scope);
		}
		
		return deduced;	
	}
	
	private static boolean deduceAnd(Scope scope, Relation and, int assumptionsAllowed) {
		// Keep track of if anything was deduced
		boolean deduced = false;
		
		// Direct deduction
		Value valueThis = scope.getValue(and);
		Value valueP    = scope.getValue(and.first);
		Value valueQ    = scope.getValue(and.second);
		Scope scopeThis = scope.deducedIn(and);
		Scope scopeP    = scope.deducedIn(and.first);
		Scope scopeQ    = scope.deducedIn(and.second);
		
		if(valueThis == Value.FALSE && valueP == Value.TRUE) deduced |= Scope.deepest(scopeThis, scopeP).setValue(and.second, Value.FALSE);
		if(valueThis == Value.FALSE && valueQ == Value.TRUE) deduced |= Scope.deepest(scopeThis, scopeQ).setValue(and.first, Value.FALSE);
		
		if(valueThis == Value.TRUE) {
			deduced |= scopeThis.setValue(and.first, Value.TRUE);
			deduced |= scopeThis.setValue(and.second, Value.TRUE);
		}
				
		if(valueP == Value.FALSE) deduced |= scopeP.setValue(and, Value.FALSE);
		if(valueQ == Value.FALSE) deduced |= scopeQ.setValue(and, Value.FALSE);
		
		if(valueP == Value.TRUE  && valueQ == Value.TRUE)  deduced |= Scope.deepest(scopeP, scopeQ).setValue(and, Value.TRUE);
		
		// In case value = FALSE, there are three possibilities: (0, 0), (0, 1), (1, 0) 
		if(valueThis == Value.FALSE && valueP == Value.UNKNOWN && valueQ == Value.UNKNOWN && assumptionsAllowed > 0) {
			Cases cases = new Cases();
			
			// Case (0, 0)
			Scope case1 = new Scope(scope);
			case1.setValue(and.first, Value.FALSE);
			case1.setValue(and.second, Value.FALSE);
			case1.deduce(assumptionsAllowed - 1);
			if(!case1.contradiction) cases.filterScope(case1);
			
			// Case (0, 1)
			Scope case2 = new Scope(scope);
			case2.setValue(and.first, Value.FALSE);
			case2.setValue(and.second, Value.TRUE);
			case2.deduce(assumptionsAllowed - 1);
			if(!case2.contradiction) cases.filterScope(case2);
			
			// Case (1, 0)
			Scope case3 = new Scope(scope);
			case3.setValue(and.first, Value.TRUE);
			case3.setValue(and.second, Value.FALSE);
			case3.deduce(assumptionsAllowed - 1);
			if(!case3.contradiction) cases.filterScope(case3);
			
			cases.finish(scope);
		}
		
		return deduced;	
	}
	
	private static boolean deduceXor(Scope scope, Relation xor, int assumptionsAllowed) {
		// Keep track of if anything was deduced
		boolean deduced = false;
		
		// Direct deduction
		Value valueThis = scope.getValue(xor);
		Value valueP    = scope.getValue(xor.first);
		Value valueQ    = scope.getValue(xor.second);
		Scope scopeThis = scope.deducedIn(xor);
		Scope scopeP    = scope.deducedIn(xor.first);
		Scope scopeQ    = scope.deducedIn(xor.second);
		
		if(valueThis == Value.TRUE && valueP == Value.TRUE) deduced |= Scope.deepest(scopeThis, scopeP).setValue(xor.second, Value.FALSE);
		if(valueThis == Value.TRUE && valueP == Value.FALSE) deduced |= Scope.deepest(scopeThis, scopeP).setValue(xor.second, Value.TRUE);
		
		if(valueThis == Value.TRUE && valueQ == Value.TRUE) deduced |= Scope.deepest(scopeThis, scopeQ).setValue(xor.first, Value.FALSE);
		if(valueThis == Value.TRUE && valueQ == Value.FALSE) deduced |= Scope.deepest(scopeThis, scopeQ).setValue(xor.first, Value.TRUE);
		
		if(valueThis == Value.FALSE && valueP == Value.TRUE) deduced |= Scope.deepest(scopeThis, scopeP).setValue(xor.second, Value.TRUE);
		if(valueThis == Value.FALSE && valueP == Value.FALSE) deduced |= Scope.deepest(scopeThis, scopeP).setValue(xor.second, Value.FALSE);
		
		if(valueThis == Value.FALSE && valueQ == Value.TRUE) deduced |= Scope.deepest(scopeThis, scopeQ).setValue(xor.first, Value.TRUE);
		if(valueThis == Value.FALSE && valueQ == Value.FALSE) deduced |= Scope.deepest(scopeThis, scopeQ).setValue(xor.first, Value.FALSE);
		
		if(valueP != Value.UNKNOWN && valueQ != Value.UNKNOWN)
			deduced |= Scope.deepest(scopeP, scopeQ).setValue(xor, (valueP == Value.TRUE ^ valueQ == Value.TRUE) ? Value.TRUE : Value.FALSE);
		
		if(valueThis == Value.TRUE && valueP == Value.UNKNOWN && valueQ == Value.UNKNOWN && assumptionsAllowed > 0) {
			Cases cases = new Cases();
			
			// Case (0, 1)
			Scope case1 = new Scope(scope);
			case1.setValue(xor.first, Value.FALSE);
			case1.setValue(xor.second, Value.TRUE);
			case1.deduce(assumptionsAllowed - 1);
			if(!case1.contradiction) cases.filterScope(case1);
			
			// Case (1, 0)
			Scope case2 = new Scope(scope);
			case2.setValue(xor.first, Value.TRUE);
			case2.setValue(xor.second, Value.FALSE);
			case2.deduce(assumptionsAllowed - 1);
			if(!case2.contradiction) cases.filterScope(case2);
			
			deduced |= cases.finish(scope);
		}
		
		if(valueThis == Value.FALSE && valueP == Value.UNKNOWN && valueQ == Value.UNKNOWN && assumptionsAllowed > 0) {
			Cases cases = new Cases();
			
			// Case (0, 0)
			Scope case1 = new Scope(scope);
			case1.setValue(xor.first, Value.FALSE);
			case1.setValue(xor.second, Value.FALSE);
			case1.deduce(assumptionsAllowed - 1);
			if(!case1.contradiction) cases.filterScope(case1);
			
			// Case (1, 1)
			Scope case2 = new Scope(scope);
			case2.setValue(xor.first, Value.TRUE);
			case2.setValue(xor.second, Value.TRUE);
			case2.deduce(assumptionsAllowed - 1);
			if(!case2.contradiction) cases.filterScope(case2);
			
			cases.finish(scope);
		}
		
		return deduced;
	}
	
	private static boolean deduceNot(Scope scope, Relation relation, int assumptionsAllowed) {
		// Keep track of if anything was deduced
		boolean deduced = false;
		
		// Direct deduction
		Value valueThis = scope.getValue(relation);
		Value valueP  = scope.getValue(relation.second);
		
		if(valueThis != Value.UNKNOWN)
			deduced |= scope.deducedIn(relation).setValue(relation.second, valueThis == Value.TRUE ? Value.FALSE : Value.TRUE);
		
		if(valueP != Value.UNKNOWN)
			deduced |= scope.deducedIn(relation.second).setValue(relation, valueP == Value.TRUE ? Value.FALSE : Value.TRUE);
		
		return deduced;
	}
	
	private static boolean deduceForAll(Scope scope, Quantifier quantifier, int assumptionsAllowed) {
		Value value = scope.getValue(quantifier);
		// In case of UNKNOWN
		if(value == Value.UNKNOWN) {
			Scope newScope = new Scope(scope);
			Alias.reference.clear();
			for(int i = 0;i < quantifier.dummies.length;i ++)
				// Create arbitrary variables x for the dummy variables
				Alias.reference.put(quantifier.dummies[i], Type.create(quantifier.dummies[i].type));
			// Create a statement P(x) by replacing the dummies by the arbitary objects
			Variable statement = Alias.output(quantifier.statement);
			newScope.variableList.add(statement);
			// Try to prove P(x)
			newScope.deduce(assumptionsAllowed - 1);
			// Check if P(x) was proven, then it must be true for any x (if P(x) turns out to be false, the statement is obviously false)
			Value valueStatement = newScope.getValue(statement);
			if(valueStatement != Value.UNKNOWN) {
				scope.setValue(quantifier, valueStatement);
			}
		}
		
		
		
		return false;
	}
	
	private static boolean deduceExists(Scope scope, Quantifier quantifier, int assumptionsAllowed) {
		// TODO Auto-generated method stub
		return false;
	}	
}
