package q1.FuzzyNodeSelection;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import q2.utilities.EdgeNodeTopology;
import simulationModels.App;
import simulationModels.Offload;
import simulationModels.Task;
import simulationModels.Topology;

public class NavigatorApp {
		//Comparing the proposed Fuzzy approach with Greedy and Nearest for a Navigator application
		public static void main(String[] args) throws Exception {
		System.out.println(" Fuzzy - Greedy - Nearest comparison (Q1 Navigator application):");
				
		int iteration = 1000; //100000 //more iterations = higher statistical significance 
		int discoveredNodes = 20; 
		if(discoveredNodes%2 != 0) //even number of nodes - half 3G and half wifi
			discoveredNodes += 1;
		int allCreatedNodes = iteration * discoveredNodes;
				
		//creating a topology of all nodes that are going to be used in iterations 
		System.out.println(" Creating a different node topologies for all the iterations... ");
		System.out.println(" " + iteration + " iterations are defined and " + discoveredNodes + " nodes are discovered in each iteration. In total " + allCreatedNodes + " nodes are currently generating. ");
		System.out.println(" It may take some time depending mostly on the defined number of terations... ");
		Topology nodeTopology = new Topology();
		nodeTopology = EdgeNodeTopology.generateNodesTopology(allCreatedNodes, discoveredNodes);
		System.out.println(" Different nodes for all iterations are successfully created. ");
		
		//variables used		
		int lowerBound = 0; 
		int higherBound = discoveredNodes; //used for moving between created node topology
		
		int nearestNodeSelected = 0;
		double lowestLatency  = 0;  
		int fuzzyNodeSelected = 0;
		double highestFuzzyValue  = 0; 
		int greedyNodeSelected = 0;
		double highestBW  = 0; 	
		int offloadingTasks = 4;
		int storageSize = 5; //offloadingTasks + 1 for their sum (total app)
		int totalAppIndex = 4; //from 0 to 3 for offloading Tasks and index 4 for their sum (total app)
		double mapSize[] = {5.0, 10.0, 15.0, 25.0};
		
		double nearestRT[][] = new double[iteration][storageSize]; 
		double fuzzyRT[][] = new double[iteration][storageSize]; 
		double greedyRT[][] = new double[iteration][storageSize]; 
		
		double nearestRTaverage[] = new double[storageSize]; 
		double fuzzyRTaverage[] = new double[storageSize];
		double greedyRTaverage[] = new double[storageSize];
		 
		double controlMIPS[]  = new double[iteration];
		double mapsMIPS[]  = new double[iteration];
		double trafficMIPS[]  = new double[iteration];
		double pathMIPS[]  = new double[iteration];
		double dataSize = 0;
		
		//Navigator application settings
		ExponentialDistribution controlTask;
		ExponentialDistribution mapsTask;
		ExponentialDistribution trafficTask;
		ExponentialDistribution pathTask;		
		ExponentialDistribution tasksData;
			
		//Exponential distribution generator	
		controlTask = new ExponentialDistribution(2);  
		mapsTask = new ExponentialDistribution(3);  
		trafficTask = new ExponentialDistribution(5);  
		pathTask = new ExponentialDistribution(5);  

		for(int i = 0; i < iteration; i++)  //for nodes used in all iterations
		{
			controlMIPS[i] =  controlTask.sample();
			mapsMIPS[i] =  mapsTask.sample();
			trafficMIPS[i] =  trafficTask.sample();
			pathMIPS[i] =  pathTask.sample();
		}
		
		for(int l = 0; l < mapSize.length; l++) 		
		{			
			System.out.println(" Calculations for " + mapSize[l] + " MB map size. ");
			for(int i = 0;  i < iteration; i++) 
			{		
				for(int offIndex = 0;  offIndex < offloadingTasks + 1; offIndex++) 
				{
					nearestRT[i][offIndex] = 0;
					fuzzyRT[i][offIndex] = 0;
					greedyRT[i][offIndex] = 0;
				}
			}
			for(int offIndex = 0;  offIndex < offloadingTasks + 1; offIndex++) 
			{
				nearestRTaverage[offIndex] = 0;
				fuzzyRTaverage[offIndex] = 0;
				greedyRTaverage[offIndex] = 0;
			}
			
			lowerBound = 0;
			higherBound = discoveredNodes;
			
			nearestNodeSelected = 0;
			lowestLatency  = 0;  
			fuzzyNodeSelected = 0;
			highestFuzzyValue  = 0; 
			greedyNodeSelected = 0;
			highestBW  = 0; 		
		
			for(int i = 0; i < iteration; i++) 
			{
				//creating Application workload for each iteration
				App app = new App(offloadingTasks);		
				Task[] task  = new Task[offloadingTasks];			 	
				tasksData =  new ExponentialDistribution(mapSize[l]); 
				dataSize = tasksData.sample();				
				int taskIndex = 1; //adding tasks in Application workload 
				for(int t = 0;  t < offloadingTasks; t++) 
				{
					if(taskIndex == 1)
					{
						task[t] = new Task(controlMIPS[i], 5);
						//System.out.println(" Created task with: "  +  task[t].operations  + "  MIPS and " + task[t].dataSize +  " MB. ");
						taskIndex++;
					}
					else if(taskIndex == 2)
					{
						task[t] = new Task(mapsMIPS[i], dataSize);
						taskIndex++;
					}
					else if(taskIndex == 3)
					{
						task[t] = new Task(trafficMIPS[i], dataSize);
						taskIndex++;
					}
					else if(taskIndex == 4)
					{
						task[t] = new Task(pathMIPS[i], dataSize);
						taskIndex++;
					}
					else 
					{
						taskIndex = 1;
					}		
					
					app.tasks[t] = task[t];
				}
				
				//choosing an edge node and RT calculation for Fuzzy, Greedy and Nearest approach:
				//Nearest: nearest node = lowest latency
				nearestNodeSelected = 0;
				lowestLatency  = nodeTopology.nodes[lowerBound].latency;  //defining some default value to be able to make a comparison				 
				for(int n = lowerBound; n < higherBound; n++) 
			    { 
					 if(lowestLatency > nodeTopology.nodes[n].latency )  //find node with lowest Latency
			    	{   
						lowestLatency = nodeTopology.nodes[n].latency;
						nearestNodeSelected = n;
			    	}	    		
			    }
				//System.out.println("Index of a node with lowest latency is: "  +  nearestNodeSelected);
				nearestRT[i][totalAppIndex] = 0; //total app RT
				for(int t = 0; t < offloadingTasks; t++) 
				{
					nearestRT[i][t] = Offload.measureRT(app.tasks[t], nodeTopology.nodes[nearestNodeSelected]);
					nearestRT[i][totalAppIndex] += nearestRT[i][t]; //total app RT
					//System.out.println(" RT for all offloaded tasks is " + nearestExeTime[i][totalAppIndex]);
				}
				
				//Fuzzy: highest Fuzzy COG output value 
				fuzzyNodeSelected = 0;
				highestFuzzyValue  = 0; 			
				for(int n = lowerBound; n < higherBound; n++) 
				{ 
					 if(highestFuzzyValue < nodeTopology.nodes[n].fuzzyValue ) //highest fuzzy value
			    	{   
						 highestFuzzyValue = nodeTopology.nodes[n].fuzzyValue;
						 fuzzyNodeSelected = n;
			    	}	    		
			    }
				fuzzyRT[i][totalAppIndex] = 0; 
				for(int t = 0; t < offloadingTasks; t++) 
				{
					fuzzyRT[i][t] = Offload.measureRT(app.tasks[t], nodeTopology.nodes[fuzzyNodeSelected]);
					fuzzyRT[i][totalAppIndex] += fuzzyRT[i][t]; 
				}
				
				//Greedy: highest BW value
				greedyNodeSelected = 0;
				highestBW  = 0; 
				for(int n = lowerBound; n < higherBound; n++) //find node with highest bandwith 
			    { 
					 if(highestBW < nodeTopology.nodes[n].bandwidth )  //ako ovdje stavim bw opet dobivam dobre rezultate tj. fuzzy mi bolji ispada.
			    	{   
						 highestBW = nodeTopology.nodes[n].bandwidth;
						 greedyNodeSelected = n;
			    	}	    		
			    }
				
				greedyRT[i][totalAppIndex] = 0;  
				for(int t = 0; t < offloadingTasks; t++)
				{
					greedyRT[i][t] = Offload.measureRT(app.tasks[t], nodeTopology.nodes[greedyNodeSelected]);
					greedyRT[i][totalAppIndex] += greedyRT[i][t]; 
				}
								
				lowerBound = higherBound; //iterate trough the topology of all created nodes
				higherBound = higherBound + discoveredNodes;
			} //end of all iterations
		
			for(int i = 0; i < iteration; i++)
			{
				for(int t = 0; t < offloadingTasks + 1; t++) //preparing average values obtained after all iterations
				{
					nearestRTaverage[t] += nearestRT[i][t];
					greedyRTaverage[t] += greedyRT[i][t];
					fuzzyRTaverage[t] += fuzzyRT[i][t];	
				}			
			}
			for(int t = 0; t < offloadingTasks + 1; t++) //calculating average values obtained after all iterations
			{
				nearestRTaverage[t] = nearestRTaverage[t]/iteration;
				fuzzyRTaverage[t] = fuzzyRTaverage[t]/iteration;
				greedyRTaverage[t] = greedyRTaverage[t]/iteration;
			}				
			for(int t = 0; t < offloadingTasks + 1; t++) //presenting average values obtained after all iterations
			{
				nearestRTaverage[t] = (double)Math.round(nearestRTaverage[t]  * 100d) / 100d ;
				fuzzyRTaverage[t] = (double)Math.round(fuzzyRTaverage[t]  * 100d) / 100d ;
				greedyRTaverage[t] = (double)Math.round(greedyRTaverage[t]  * 100d) / 100d ;
			}
			//obtained results:
			System.out.println(" RT of a single offloaded task: ");
			System.out.println(" Nearest approach: " + nearestRTaverage[0]  + "   "  + nearestRTaverage[1] + "   "  + nearestRTaverage[2] + "   "  + nearestRTaverage[3]);
			System.out.println(" Greedy approach: " + greedyRTaverage[0]  + "   "  + greedyRTaverage[1] + "   "  + greedyRTaverage[2] + "   "  + greedyRTaverage[3]);
			System.out.println(" Proposed Fuzzy approach: " + fuzzyRTaverage[0]  + "   "  + fuzzyRTaverage[1] + "   "  + fuzzyRTaverage[2] + "   "  + fuzzyRTaverage[3]);
			System.out.println(" RT of all offloaded task: ");		
			System.out.println(" Nearest approach: " + nearestRTaverage[totalAppIndex]);
			System.out.println(" Greedy approach: " + greedyRTaverage[totalAppIndex]);
			System.out.println(" Proposed Fuzzy approach: " + fuzzyRTaverage[totalAppIndex]);
		}

		System.out.println("end of execution"); 	
  }
}
