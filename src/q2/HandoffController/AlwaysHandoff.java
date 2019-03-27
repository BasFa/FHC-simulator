package q2.HandoffController;

import q2.utilities.EdgeNodeTopology;
import q2.utilities.FuzzyOutputValue;
import simulationModels.App;
import simulationModels.ApproachResults;
import simulationModels.Offload;
import simulationModels.Topology;

public class AlwaysHandoff {
	
	public static ApproachResults execution(int nodesCreated, App[] app, Topology nodeTopologySetup, double pole, double[] desiredRT, double changeBW[][], double changeCPU[][], double changeLAT[][],  int newlyDiscoveredNodes, int nmbOfTasks[], int indexMaxNmbTask) throws Exception 
	{
		System.out.println("Always Handoff - calculations..." );
		
		double highestFuzzyValue  = 0; 
		int fuzzyNodeSelected = 0;
		
		Topology nodeTopology =  EdgeNodeTopology.initializeTopology(nodesCreated); 		
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
		double taskRT[] = new double[nmbOfTasks.length];
		double totalRT[]= new double[nmbOfTasks.length];
		int handoffTimes[]= new int[nmbOfTasks.length];
				
		double msUsageTimeNode[] = new double[nmbOfTasks.length]; //milliseconds
		double sUsageTimeNode[] = new double[nmbOfTasks.length];  //seconds
		double minUsageTimeNode[] = new double[nmbOfTasks.length]; //minutes
		double minUsageTimeTotal[] = new double[nmbOfTasks.length]; //minutes
		double sUsageTimeTotal[] = new double[nmbOfTasks.length]; //seconds		
		
		for(int a = 0; a < nmbOfTasks.length; a++)
		{
			taskRT[a] = 0.0; 
			totalRT[a] = 0.0; 
			handoffTimes[a] = 0;			
			msUsageTimeNode[a] = 0.0;
			sUsageTimeNode[a] = 0.0;
			minUsageTimeNode[a] = 0.0;
			minUsageTimeTotal[a] = 0.0;
			sUsageTimeTotal[a] = 0.0;			
		}				 			 
		int handoff = 0;
		int newSelectedNode = 0;
		int handoffCount = 0;  					
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
			handoffCount = 1 + handoff;     			    	
			
			for(int a = 0; a < nmbOfTasks.length; a++)
			{
				if(i < nmbOfTasks[a])
				{
					taskRT[a] = Offload.measureRT(app[a].tasks[i], nodeTopology.nodes[fuzzyNodeSelected]);
					totalRT[a] += taskRT[a];
					handoffTimes[a] = handoffCount;
					
				}
			}
			measuredRT = taskRT[indexMaxNmbTask]; 
			for(int a = 0; a < nmbOfTasks.length; a++)
			{
				if(i < nmbOfTasks[a])
				{
					msUsageTimeNode[a] += measuredRT;
				}
			}   		
			
			EdgeNodeTopology.update(nodeTopology, lowerBound, higherBound, changeBW, changeCPU, changeLAT, currentChangeIndex);
			currentChangeIndex++;    		
			 
			if( (int)measuredRT > desiredRT[i]) //always perform handoff when RT is deteriorated
			{    			
				//handoff is needed therefore discover new nodes in surrounding 
				lowerBound = higherBound ; //iterates through newly discovered nodes
				higherBound = higherBound + newlyDiscoveredNodes;  //iterates through newly discovered nodes
    		    		
				for(int s = lowerBound; s < higherBound; s++) //workload changes affect fuzzy values (FVs), therefore update FVs 
				{
					nodeTopology.nodes[s].fuzzyValue = FuzzyOutputValue.calculateFuzzyValue(nodeTopology.nodes[s]);  
				}    			 
				double temporarilyHighestFuzzyValue = 0;
				for(int s = lowerBound; s < higherBound; s++) //fuzzy logic selection within discovered nodes
				{
					if(temporarilyHighestFuzzyValue < nodeTopology.nodes[s].fuzzyValue)
					{
						temporarilyHighestFuzzyValue = nodeTopology.nodes[s].fuzzyValue;
						newSelectedNode = s;
					}
				}			    	
				if(fuzzyNodeSelected != newSelectedNode) 
				{
					currentChangeIndex = 0;
					
					handoff ++; 
					
					fuzzyNodeSelected = newSelectedNode;
					
	       		
					for(int a = 0; a < nmbOfTasks.length; a++)
					{
						if(i < nmbOfTasks[a])
						{
							if(msUsageTimeNode[a] < 60000) //used less then 60 seconds (60000ms = 60s)
							{
								msUsageTimeNode[a] = 60000; //paying for a whole minute (60s = 1min)
							}
							sUsageTimeTotal[a] +=  (msUsageTimeNode[a]/1000); //ms in s 
							msUsageTimeNode[a] = 0;	//execution moved to another node
						}
					}  				    				   
				}
			}
			else {
				for(int l = 0; l < nmbOfTasks.length; l++)
				{		
					if( i == nmbOfTasks[l]-1 && i != nmbOfTasks[indexMaxNmbTask]-1 ) //no handoff performed within observed number of tasks 
					{
						for(int a = 0; a < nmbOfTasks.length; a++)
						{
							if(i < nmbOfTasks[a])
							{
								if(msUsageTimeNode[a] < 60000) //used less then 60 seconds (60000ms = 60s)
								{
									msUsageTimeNode[a] = 60000; //paying for a whole minute (60s = 1min)
								}
								sUsageTimeTotal[a] +=  (msUsageTimeNode[a]/1000); //ms in s
								msUsageTimeNode[a] = 0;	//execution moved to another node
							}
						}
					}
				}
			}    		
			if(i == nmbOfTasks[indexMaxNmbTask]-1) //number of task = last task observed
			{
				for(int a = 0; a < nmbOfTasks.length; a++)
				{
					if(i < nmbOfTasks[a])
					{
						if(msUsageTimeNode[a] < 60000) //used less then 60 seconds (60000ms = 60s)
						{
							msUsageTimeNode[a] = 60000; //paying for a whole minute (60s = 1min)
						}
						sUsageTimeTotal[a] +=  (msUsageTimeNode[a]/1000); //ms in s
						msUsageTimeNode[a] = 0;	//execution moved to another node
					}
				}
			}    	    		
		}				    		    			
		for(int a = 0; a < nmbOfTasks.length; a++)
		{
			minUsageTimeTotal[a] = sUsageTimeTotal[a]/60; //s in min
		}
		
		ApproachResults fuzzyResults = new ApproachResults(handoffTimes, totalRT, minUsageTimeTotal); //save results perceived in this approach 
		
		return fuzzyResults;
	}
}
