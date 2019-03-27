package simplified.examples;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Rule;

public class GenerateFuzzyGraph {
	
	public static void main(String[] args) throws Exception 
	{
			System.out.println(" Generate Fuzzy Logic graph, example");
			FIS fis = FIS.load("fcl/fuzzySettings.fcl", true); 			
			
			fis.setVariable("bandwidth", 45);// Set input values
			fis.setVariable("cpu", 37);
			fis.setVariable("latency", 30);   	    

			fis.evaluate(); // Evaluate      
			fis.getVariable("node").chartDefuzzifier(true); 

			double fuzzyValue = fis.getVariable("node").getValue(); 
			System.out.println(fuzzyValue);

			// Show each rule (and degree of support)
			for( Rule r : fis.getFunctionBlock("nodeSelection").getFuzzyRuleBlock("No1").getRules() )
				System.out.println(r);

			System.out.println(fis);
	}
}

