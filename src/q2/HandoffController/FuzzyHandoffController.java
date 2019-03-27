package q2.HandoffController;


import q2.utilities.ControlOutput;
import q2.utilities.EdgeNodeTopology;
import q2.utilities.FuzzyOutputValue;
import simulationModels.App;
import simulationModels.ApproachResults;
import simulationModels.Offload;
import simulationModels.Topology;

public class FuzzyHandoffController {
	
	public static ApproachResults execution(int nodesCreated, App[] app, Topology nodeTopologySetup, double pole, double[] desiredRT, double changeBWrange[][], double changeCPU[][], double changeLATrange[][],  int newlyDiscoveredNodes, int nmbOfTasks[], int indexMaxNmbTask) throws Exception 
	{
		System.out.println("Fuzzy Handoff Controller - calculations..." );		
		double highestFuzzyValue  = 0; 
		int fuzzyNodeSelected = 0;			
    	
		Topology nodeTopology =  EdgeNodeTopology.initializeTopology(nodesCreated); 
		for(int i = 0; i < nodeTopologySetup.nodes.length ; i++)
		{
			nodeTopology.nodes[i].bandwidth = nodeTopologySetup.nodes[i].bandwidth;
			nodeTopology.nodes[i].cpu = nodeTopologySetup.nodes[i].cpu;
			nodeTopology.nodes[i].latency = nodeTopologySetup.nodes[i].latency;
			nodeTopology.nodes[i].fuzzyValue = nodeTopologySetup.nodes[i].fuzzyValue;
		}
		
		int lowerBound = 0; 
		int higherBound = newlyDiscoveredNodes;
		
		double measuredRT = 0;		
		double controllerOutput = 1;
		double controlThreshold = 0;
		double controlOutputDefault = 1;		
		double taskRT[] = new double[nmbOfTasks.length];
		double totalRT[]= new double[nmbOfTasks.length];
		int handoffTimes[]= new int[nmbOfTasks.length];
		int nmbOfDeterioratedTasks = 1;
		double msUsageTimeNode[] = new double[nmbOfTasks.length]; //milliseconds
		double sUsageTimeNode[] = new double[nmbOfTasks.length]; //seconds
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
			if(highestFuzzyValue < nodeTopology.nodes[n].fuzzyValue )
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
			if((int)measuredRT > desiredRT[i] ) 
			{
				nmbOfDeterioratedTasks++;
			}
			
			if(msUsageTimeNode[nmbOfTasks.length-1] < 60000) //60000ms = 1minute
				pole = 0.9;
			else 
				pole = 0.7; 	
			
			controlThreshold = 1.0 - ( 1.0 / (nmbOfDeterioratedTasks ) );    		
			controlThreshold = (double)Math.round(controlThreshold * 10000d) / 10000d ; 
			controllerOutput = ControlOutput.calculate(pole, desiredRT[i], measuredRT, controllerOutput); 
			    	
			EdgeNodeTopology.update(nodeTopology, lowerBound, higherBound, changeBWrange, changeCPU, changeLATrange, currentChangeIndex );
			currentChangeIndex++;
			     		
			if(controllerOutput < controlThreshold && (int)measuredRT > desiredRT[i]) //perform handoff
			{
				//handoff is needed therefore discover new nodes in surrounding 
				lowerBound = higherBound ; //iterates through newly discovered nodes
				higherBound = higherBound + newlyDiscoveredNodes; //iterates through newly discovered nodes
				nmbOfDeterioratedTasks = 1;
				for(int s = lowerBound; s < higherBound; s++) //workload changes affect fuzzy values (FVs), therefore update FVs 
				{
					nodeTopology.nodes[s].fuzzyValue = FuzzyOutputValue.calculateFuzzyValue(nodeTopology.nodes[s]);  
				}    
				double temporarilyHighestFuzzyValue = 0;
				for(int s = lowerBound; s < higherBound; s++) //find node with best fuzzy value among new discovered range
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
					controllerOutput = controlOutputDefault; //after performed handoff set default values
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
					if( i == nmbOfTasks[l]-1 && i != nmbOfTasks[indexMaxNmbTask]-1 ) //no handoff performed in after observed number of tasks 
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
			
			if(i == nmbOfTasks[indexMaxNmbTask]-1 ) //number of task = last task observed
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
		
		ApproachResults fuzzyHandoResults = new ApproachResults(handoffTimes, totalRT, minUsageTimeTotal);
		
		return fuzzyHandoResults;
	}
}


