package nl.jessevogel.logic.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nl.jessevogel.logic.parser.Parser;
import nl.jessevogel.logic.prover.Initializer;

public class Main {
	
	public static void main(String[] args) {
		// Initialize
		Initializer.initialize();
		
		// Read assumptions
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while((line = br.readLine()) != null) {
				// Only parse non-empty strings
				if(line.matches("^\\s*$")) continue;
				
				(new Parser(line)).setDirectory("./").parseCommands();
			}
			br.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
