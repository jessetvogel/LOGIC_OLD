package nl.jessevogel.logic.debug;

import nl.jessevogel.logic.basic.Relation;
import nl.jessevogel.logic.basic.Variable;

public class VariablePrinter {
	public static void print(Variable v) {
		print(v, "");
		System.out.println();
	}
	
	private static void print(Variable v, String prefix) {
		// Relations
		if(v instanceof Relation) {
			Relation relation = (Relation) v;
			System.out.println(prefix + relation.type.name + " (");
			if(((Relation) v).first != null) {
				print(relation.first, prefix + "  ");
			}
			if(((Relation) v).first != null && ((Relation) v).second != null) {
				System.out.println(prefix + ",");
			}
			if(((Relation) v).second != null) {
				print(relation.second, prefix + "  ");
			}
			System.out.print("\n" + prefix + ")");
			return;
		}
		
//		if(v instanceof Dummy) {
//			System.out.print(prefix + "dummy (" + v.type.name + ", " + v.identifier + ")");
//			return;
//		}
		
		// By default, print its identifier
		System.out.print(prefix + v.identifier);
	}
}
