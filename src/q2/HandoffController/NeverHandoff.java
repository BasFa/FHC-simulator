package q2.HandoffController;

import q2.utilities.EdgeNodeTopology;
import simulationModels.App;
import simulationModels.ApproachResults;
import simulationModels.Offload;
import simulationModels.Topology;

public class NeverHandoff {
	public static ApproachResults execution(int nodesCreated, App[] app, Topology nodeTopologySetup, double pole, double[] setPoint, double changeBW[][], double changeCPU[][], double changeLAT[][], int newlyDiscoveredNodes, int nmbOfTasks[], int indexMaxNmbTask) throws Exception 
	{	
		System.out.println("Never Handoff - calculations..." );
		
		Topology nodeTopology =  EdgeNodeTopology.initializeTopology( nodesCreated); 
		for(int n = 0; n < nodeTopologySetup.nodes.length ; n++)
		{
			nodeTopology.nodes[n].bandwidth = nodeTopologySetup.nodes[n].bandwidth;
			nodeTopology.nodes[n].cpu = nodeTopologySetup.nodes[n].cpu;
			nodeTopology.nodes[n].latency = nodeTopologySetup.nodes[n].latency;
			nodeTopology.nodes[n].fuzzyValue = nodeTopologySetup.nodes[n].fuzzyValue;
		}
		int lowerBound = 0; 
		int higherBound = newlyDiscoveredNodes;
		double measuredRT = 0;
		double highestFuzzyValue  = 0 ; 
		int fuzzyNodeSelected = 0;
		double taskRT[] = new double[nmbOfTasks.length];
		double totalRT[]= new double[nmbOfTasks.length];
		int handoffTimes[]= new int[nmbOfTasks.length];
		double minUsageTimeTotal[] = new double[nmbOfTasks.length]; //minutes
		
		for(int a = 0; a < nmbOfTasks.length; a++)
		{
			taskRT[a] = 0.0;  
			totalRT[a] = 0.0; 
			handoffTimes[a] = 0;
			minUsageTimeTotal[a] = 0.0;
		}			
		int currentChangeIndex = 0;		
		for(int n = lowerBound; n < higherBound; n++ ) //Fuzzy logic node selection
		{ 
			if(highestFuzzyValue < nodeTopology.nodes[n].fuzzyValue ) //node with higher FV is found
			{   
				highestFuzzyValue = nodeTopology.nodes[n].fuzzyValue;
				fuzzyNodeSelected = n;
		 	}	    		
		}		
		for(int i = 0; i < nmbOfTasks[indexMaxNmbTask]; i++) 
		{
			for(int a = 0; a < nmbOfTasks.length; a++)
			{
				if(i < nmbOfTasks[a])
				{
					taskRT[a] = Offload.measureRT(app[a].tasks[i], nodeTopology.nodes[fuzzyNodeSelected]);
					totalRT[a] += taskRT[a];
					handoffTimes[a] = 0;  //never handoff approach therefore handoff never happen    				 
				}
			}
			measuredRT = taskRT[indexMaxNmbTask]; 
			
			EdgeNodeTopology.update(nodeTopology, lowerBound, higherBound, changeBW, changeCPU, changeLAT, currentChangeIndex);
			currentChangeIndex++;   	
		}
		
		ApproachResults FuzzyConstantlyResults = new ApproachResults(handoffTimes, totalRT, minUsageTimeTotal);
				
		return FuzzyConstantlyResults;
	}
}
