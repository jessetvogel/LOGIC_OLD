package nl.jessevogel.logic.prover;

import java.util.ArrayList;

import nl.jessevogel.logic.basic.Scope;
import nl.jessevogel.logic.basic.Scope.Value;
import nl.jessevogel.logic.basic.Variable;
import nl.jessevogel.logic.debug.Log;
import nl.jessevogel.logic.debug.Timer;

public class Prover {
	public static final Scope mainScope = new Scope(null);
	
	private static ArrayList<Variable> goals = new ArrayList<Variable>();
	
	public static void addGoal(Variable goal) {
		// Add goal to the list
		goals.add(goal);
	}

	public static boolean done() {
		// Return false if any of the goals is not proven in scope
		for(Variable goal : goals) {
			if(mainScope.getValue(goal) != Value.TRUE)
				return false;
		}
			
		// If they are, return true
		return true;
	}

	public static void run() {
		// Create Timer and try to prove the goals
		Timer timer = new Timer();
		Log.message("Start proving...");
		timer.start();
		mainScope.deduce(1);
		long duration = timer.stop();
		
		if(Prover.done())
			Log.message("Q.E.D. in " + ((float) duration / 1000) + " seconds");
		else
			Log.message("Was not able to prove this!");
		
		// Show deductions made in scope TODO: remove this
		System.out.println();
		Prover.mainScope.show();
	}

	public static void clearGoals() {
		// Clear the list of goals
		goals.clear();
	}
}
