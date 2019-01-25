package q2.utilities;

import net.sourceforge.jFuzzyLogic.FIS;
import simulationModels.Node;

public class FuzzyOutputValue {
	public static double calculateFuzzyValue(Node discoveredNode)
	{
		FIS fis = FIS.load("fcl/fuzzySettings.fcl", true); //fis = fuzzy inference system
		fis.setVariable("bandwidth", discoveredNode.bandwidth);// Set input values
		fis.setVariable("cpu", discoveredNode.cpu);
		fis.setVariable("latency", discoveredNode.latency);  	    	    
		fis.evaluate(); 
		double calculatedFuzzyValue = fis.getVariable("node").getValue(); //node is the name of output variable defined in FIS 
		return calculatedFuzzyValue;
	}
		
		
		
}

