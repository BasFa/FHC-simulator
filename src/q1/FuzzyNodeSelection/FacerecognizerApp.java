package q1.FuzzyNodeSelection;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import q2.utilities.EdgeNodeTopology;
import simulationModels.App;
import simulationModels.Offload;
import simulationModels.Task;
import simulationModels.Topology;

public class FacerecognizerApp {
		//Comparing the proposed Fuzzy approach with Greedy and Nearest for a Facerecognizer application
		public static void main(String[] args) throws Exception {
		System.out.println(" Fuzzy - Greedy - Nearest comparison (Q1 Facerecognizer application):");
			
		int iteration = 1000; //100000  //a confidence interval of 95%
		int discoveredNodes = 20; 
		if(discoveredNodes%2 != 0) //even number of nodes - half 3G and half wifi
			discoveredNodes += 1;
		int allCreatedNodes = iteration * discoveredNodes;
		
		//creating a topology of all nodes that are going to be used in iterations 
		System.out.println(" Creating a different node topologies for all the iterations... ");
		System.out.println(" " + iteration + " iterations are defined and " + discoveredNodes + " nodes are discovered in each iteration. In total " + allCreatedNodes + " nodes are currently generating. ");
		System.out.println(" This may take some time depending mostly on the number of iterations defined... ");
		Topology nodesTopology = new Topology();
		nodesTopology = EdgeNodeTopology.generateNodesTopology(allCreatedNodes, discoveredNodes);
		System.out.println(" Different nodes for all iterations are successfully created. ");
		
		int lowerBound = 0; 
		int higherBound = discoveredNodes; //used for moving between created node topology
		
		int nearestNodeSelected = 0;
		double lowestLatency  = 0;  
		int fuzzyNodeSelected = 0;
		double highestFuzzyValue  = 0; 
		int greedyNodeSelected = 0;
		double highestBW  = 0; 		
		int offloadingTasks = 3;
		int storageSize = 4; //offloadingTasks + 1 for their sum (total app)
		int totalAppIndex = 3; //from 0 to 2 for offloading Tasks and index 3 for their sum (total app)
		double imageSize[] = {1.0, 5.0, 10.0, 20.0};	
		
		double nearestRT[][] = new double[iteration][storageSize]; 
		double fuzzyRT[][] = new double[iteration][storageSize]; 
		double greedyRT[][] = new double[iteration][storageSize]; 
		
		double nearestRTaverage[] = new double[storageSize]; 
		double fuzzyRTaverage[] = new double[storageSize];
		double greedyRTaverage[] = new double[storageSize];
				 
		double findMatchMIPS[]  = new double[iteration];
		double initMIPS[]  = new double[iteration];
		double detectFaceMIPS[]  = new double[iteration];	
		double dataSize = 0;
				
		//Facerecognizer application settings
		ExponentialDistribution findMatchTask;
		ExponentialDistribution initTask;
		ExponentialDistribution detectFaceTask;
		ExponentialDistribution tasksData;
			
		//Exponential distribution generator
		findMatchTask = new ExponentialDistribution(4); 
		initTask = new ExponentialDistribution(4); 
		detectFaceTask = new ExponentialDistribution(8); 
		for(int i = 0; i < iteration; i++)//for nodes used in all iterations
		{
			findMatchMIPS[i] =  findMatchTask.sample();
			initMIPS[i] =  initTask.sample();
			detectFaceMIPS[i] =  detectFaceTask.sample();			
		}		
			
		for(int l = 0; l < imageSize.length; l++) 		
		{			
			System.out.println(" Calculations for " + imageSize[l] + " MB image size: ");
			for(int i = 0;  i < iteration; i++) 
			{		
				for(int offIndex = 0; offIndex < offloadingTasks; offIndex++) 
				{
					nearestRT[i][offIndex] = 0;
					fuzzyRT[i][offIndex] = 0;
					greedyRT[i][offIndex] = 0;
				}
			}
			for(int offIndex = 0;  offIndex < offloadingTasks ; offIndex++) 
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
				//creating application workload for each iteration
				App app = new App(offloadingTasks);	
				Task[] task  = new Task[offloadingTasks];			 	
				tasksData =  new ExponentialDistribution(imageSize[l]); 
				dataSize = tasksData.sample();				
				int taskIndex = 1; //adding tasks in application workload 
				for(int t = 0; t < offloadingTasks; t++) 
				{
					if(taskIndex == 1)
					{
						task[t] = new Task(findMatchMIPS[i], dataSize); 
						//System.out.println(" Created task with: "  +  task[t].operations  + "  MIPS and " + task[t].dataSize +  " MB. ");
						taskIndex++;
					}
					else if(taskIndex == 2)
					{
						task[t] = new Task(initMIPS[i], dataSize);
						taskIndex++;
					}
					else if(taskIndex == 3)
					{
						task[t] = new Task(detectFaceMIPS[i], dataSize);
						taskIndex++;
					}
					else 
					{		
						taskIndex = 1;
					}		
					
					app.tasks[t] = task[t];
				}				
				
				//edge node selection and RT calculation for Fuzzy, Greedy and Nearest approach:
				//Nearest: nearest node = lowest latency
				nearestNodeSelected = 0;
				lowestLatency = nodesTopology.nodes[lowerBound].latency; //defining some default value to be able to make a comparison			 
				for(int n = lowerBound; n < higherBound; n++)
				{ 
					if(lowestLatency > nodesTopology.nodes[n].latency )  //find node with lowest latency 
					{
						lowestLatency = nodesTopology.nodes[n].latency;
						nearestNodeSelected = n;
					}	    		
				}
				//System.out.println("Index of a node with lowest latency is: "  +  nearestNodeSelected);
				nearestRT[i][totalAppIndex] = 0; //total app RT
				for(int t = 0; t < offloadingTasks; t++) 
				{
					nearestRT[i][t] = Offload.measureRT(app.tasks[t], nodesTopology.nodes[nearestNodeSelected]);
					nearestRT[i][totalAppIndex] += nearestRT[i][t]; //total app RT
					//System.out.println(" RT for all offloaded tasks is " + nearestExeTime[i][totalAppIndex]);		
				}
						
				//Fuzzy: highest Fuzzy COG output value 
				fuzzyNodeSelected = 0;
				highestFuzzyValue  = 0; 			
				for(int n = lowerBound; n < higherBound; n++)
				{ 
					if(highestFuzzyValue < nodesTopology.nodes[n].fuzzyValue ) //highest fuzzy value
					{	   
						 highestFuzzyValue = nodesTopology.nodes[n].fuzzyValue;
						 fuzzyNodeSelected = n; 
					}
				}
				
				fuzzyRT[i][totalAppIndex] = 0; 
				for(int t = 0; t < offloadingTasks; t++) 
				{
					fuzzyRT[i][t] = Offload.measureRT(app.tasks[t], nodesTopology.nodes[fuzzyNodeSelected]);
					fuzzyRT[i][totalAppIndex] += fuzzyRT[i][t]; 
				}
								
				//Greedy: highest BW value
				greedyNodeSelected = 0;
				highestBW  = 0; 
				for(int n = lowerBound; n < higherBound; n++) 
				{ 
					if(highestBW < nodesTopology.nodes[n].bandwidth )  //find node with highest bandwidth (greedy).
					{
						 highestBW = nodesTopology.nodes[n].bandwidth;
						 greedyNodeSelected = n;
					}
				}
				
				greedyRT[i][totalAppIndex] = 0;  
				for(int t = 0; t < offloadingTasks; t++) 
				{
					greedyRT[i][t] = Offload.measureRT(app.tasks[t], nodesTopology.nodes[greedyNodeSelected]);
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
			System.out.println(" Nearest approach: " + nearestRTaverage[0]  + " "  + nearestRTaverage[1] + "   "  + nearestRTaverage[2]);
			System.out.println(" Greedy approach: " + greedyRTaverage[0]  + " "  + greedyRTaverage[1] + "   "  + greedyRTaverage[2]);
			System.out.println(" Proposed Fuzzy approach: " + fuzzyRTaverage[0]  + " "  + fuzzyRTaverage[1] + "   "  + fuzzyRTaverage[2]);
			System.out.println(" RT time of all offloaded task: ");		
			System.out.println(" Nearest approach: " + nearestRTaverage[totalAppIndex]);
			System.out.println(" Greedy approach: " + greedyRTaverage[totalAppIndex]);
			System.out.println(" Proposed Fuzzy approach: " + fuzzyRTaverage[totalAppIndex]);
		}
	
		System.out.println(" End of simulations "); 	
	}
}
