package q2.HandoffController;

import q2.constants.DesiredRTfacerecognizer;
import q2.constants.DesiredRTnavigator;
import q2.utilities.EdgeNodeTopology;
import q2.utilities.ResultsEvaluation;
import simulationModels.App;
import simulationModels.ApproachResults;
import simulationModels.Task;
import simulationModels.Topology;
import org.apache.commons.math3.distribution.ExponentialDistribution;

public class ApproachesComparison {
			//Comparing the proposed Fuzzy approach with Greedy and Nearest for a Navigator application
			public static void main(String[] args) throws Exception {
			System.out.println(" Fuzzy Handoff Controller - Never Handoff - Always Handoff comparison using Facerecognizer and Navigator application models: ");
		
			int iteration = 2;
			int discoveredNodes = 20;	
			if(discoveredNodes%2 != 0) //even number of nodes - half 3G and half wifi
				discoveredNodes += 1;
			int nodesCreated = 7000;  
						
			int nmbOfTasks[] = {50, 100, 200, 300,  400, 500, 600, 700, 800, 900, 1000};  
			double pole[] = {0.7};  //e.g. {0.3, 0.7, 0.9};
			int naviData[] = {5, 10, 15}; //MB
			int facerecData[] = {1, 5}; //1MB, 5MB, ...			
			int runningApp = 0; // 0 = Facerecognizer or 1 = Navigator

			int facerecOffTasks = 3; //number of offloaded tasks
			int naviOffTasks = 4;
			
			int facerecDataInputIndex = 0;
			int naviDataInputIndex = 0;
			
			int maxNmbOfTasks = 0;
	    	int indexMaxNmbTask = 0;	
					
			//Facerecognizer application settings
			ExponentialDistribution findMatchTaskF;
			ExponentialDistribution initTaskF;
			ExponentialDistribution detectFaceTaskF;
			ExponentialDistribution tasksDataFacerec;
			//Navigator application settings
			ExponentialDistribution controlTaskN;
			ExponentialDistribution mapsTaskN;
			ExponentialDistribution trafficTaskN;
			ExponentialDistribution pathTaskN;		
			ExponentialDistribution tasksDataNavi;
				
			String[] approach = {" Proposed_FuzzyHandoffControl ", " NeverHandoff " ,  " AlwaysHandoff "};
			int nmbOfApproaches = approach.length;
			int currentApproach = 0;
	    	
	    	ApproachResults oneApproachResults =  new ApproachResults();
	    	ApproachResults[][] allApproachResults= new ApproachResults[pole.length][nmbOfApproaches];
	    	
	    	int[][][] handoffs = new int[pole.length][nmbOfApproaches][nmbOfTasks.length];
	    	double[][][] appRT = new double[pole.length][nmbOfApproaches][nmbOfTasks.length]; //milliseconds
	    	double[][][] monetaryTimeCost = new double[pole.length][nmbOfApproaches][nmbOfTasks.length]; //minutes 
	    	double[][][] handoffsAverage = new double[pole.length][nmbOfApproaches][nmbOfTasks.length]; 
	    	double[][][] appRTaverage = new double[pole.length][nmbOfApproaches][nmbOfTasks.length]; //milliseconds
	    	double[][][] monetaryTimeCostAverage = new double[pole.length][nmbOfApproaches][nmbOfTasks.length]; //minutes 
	    	for(int p = 0; p < pole.length; p++)
			{
		    	for(int a = 0; a < nmbOfApproaches; a++)
		    	{   for (int t = 0; t < nmbOfTasks.length; t++)
			    	{
		    			handoffs[p][a][t] = 0;
		    			appRT[p][a][t] = 0;
		    			monetaryTimeCost[p][a][t] = 0;
			    	}
			    }
			}
	    	for(int p = 0; p < pole.length; p++)
			{
		    	for(int a = 0; a < nmbOfApproaches; a++)
		    	{ 
		    		allApproachResults[p][a] =  new ApproachResults(handoffs[p][a], appRT[p][a], monetaryTimeCost[p][a] );
		    	}
			}
	    		    
	    	controlTaskN = new ExponentialDistribution(2);  
	    	mapsTaskN = new ExponentialDistribution(3);  
	    	trafficTaskN = new ExponentialDistribution(5);  
	    	pathTaskN = new ExponentialDistribution(5);  
			
	    	findMatchTaskF = new ExponentialDistribution(4);
	    	initTaskF = new ExponentialDistribution(4); 
	    	detectFaceTaskF = new ExponentialDistribution(8); 
		
	    	try{	
				for(int i = 1; i <= iteration ; i++)
				{	

					System.out.println(" Generating an edge node topology... ");		
					Topology nodeTopologySetup = EdgeNodeTopology.generateNodesTopology(nodesCreated, discoveredNodes);
					System.out.println(" Edge node topology successfully generated ... ");	
					
					App[] app = new App[nmbOfTasks.length];
					Task[] task  = new Task [nmbOfTasks[nmbOfTasks.length-1]]; 
					double[] desiredRT = new double[nmbOfTasks[nmbOfTasks.length-1]];		
					 
					for(int nTask = 0;  nTask < nmbOfTasks[nmbOfTasks.length-1]; ) 
					{
						runningApp = (0) + (int)(Math.random() * 2 ); // 0 = Facerecognizer or 1 = Navigator
						if(runningApp == 0) //Facerecognizer app 
						{
							facerecDataInputIndex = (0) + (int)(Math.random() * facerecData.length); //generates 0 or 1  //selects {1 or 5};
							tasksDataFacerec =  new ExponentialDistribution(facerecData[facerecDataInputIndex]); 
							double dataSizeFacerec = tasksDataFacerec.sample();
							DesiredRTfacerecognizer taskDesiredRTfacere = new DesiredRTfacerecognizer(); //desired RT for each task	
							int taskIndexFacerec = 0;
							for(int f = 0;  f < facerecOffTasks; f++) 
							{
								if(taskIndexFacerec == 0)
								{
									if(nTask < nmbOfTasks[nmbOfTasks.length-1])
									{
										desiredRT[nTask] = taskDesiredRTfacere.taskDesiredRT[facerecDataInputIndex][taskIndexFacerec];  
										task[nTask] = new Task(findMatchTaskF.sample(), dataSizeFacerec); 
										taskIndexFacerec++; 
										nTask++;
									}
								}
								else if(taskIndexFacerec == 1)
								{
									if(nTask < nmbOfTasks[nmbOfTasks.length-1])
									{
										desiredRT[nTask] =taskDesiredRTfacere.taskDesiredRT[facerecDataInputIndex][taskIndexFacerec];  
										task[nTask] = new Task(initTaskF.sample(), dataSizeFacerec);
										taskIndexFacerec++; 
										nTask++;
									}
								}
								else if(taskIndexFacerec == 2)
								{
									if(nTask < nmbOfTasks[nmbOfTasks.length-1])
									{
										desiredRT[nTask] = taskDesiredRTfacere.taskDesiredRT[facerecDataInputIndex][taskIndexFacerec];
										task[nTask] = new Task(detectFaceTaskF.sample(), dataSizeFacerec);
										taskIndexFacerec++; 
										nTask++;
									}
								}
								else 
								{
									taskIndexFacerec = 0; 
								}						
							}
						}
						else if(runningApp == 1) //Navigator app  
						{
							naviDataInputIndex = (0) + (int)(Math.random() * naviData.length); // generates 0 or 1 //selects {5, 10, 15};
							tasksDataNavi =  new ExponentialDistribution(naviData[naviDataInputIndex]); 
							double dataSizeNavi = tasksDataNavi.sample();
							DesiredRTnavigator taskDesiredRTnavi = new DesiredRTnavigator(); //desired RT for each task	
							int taskIndexNavi = 0; 					
							for(int n = 0;  n < naviOffTasks; n++)
							{
								if(taskIndexNavi == 0)
								{
									if(nTask < nmbOfTasks[nmbOfTasks.length-1])
									{
										desiredRT[nTask] = taskDesiredRTnavi.taskDesiredRT[naviDataInputIndex][taskIndexNavi];
										task[nTask] = new Task(controlTaskN.sample(), 5);
										taskIndexNavi++; 
										nTask++;
									}
								}
								else if(taskIndexNavi == 1)
								{
									if(nTask < nmbOfTasks[nmbOfTasks.length-1])
									{
										desiredRT[nTask] = taskDesiredRTnavi.taskDesiredRT[naviDataInputIndex][taskIndexNavi];
										task[nTask] = new Task(mapsTaskN.sample(), dataSizeNavi);
										taskIndexNavi++; 
										nTask++;
									}
								}
								else if(taskIndexNavi == 2)
								{
									if(nTask < nmbOfTasks[nmbOfTasks.length-1])
									{
										desiredRT[nTask] = taskDesiredRTnavi.taskDesiredRT[naviDataInputIndex][taskIndexNavi];
										task[nTask] = new Task(trafficTaskN.sample(), dataSizeNavi);
										taskIndexNavi++; 
										nTask++;
									}
								}
								else if(taskIndexNavi == 3)
								{
									if(nTask < nmbOfTasks[nmbOfTasks.length-1])
									{
										desiredRT[nTask] = taskDesiredRTnavi.taskDesiredRT[naviDataInputIndex][taskIndexNavi];
										task[nTask] = new Task(pathTaskN.sample(), dataSizeNavi);
										taskIndexNavi++; 
										nTask++;
									}
								}						
								else 
								{
									taskIndexNavi = 0;
								}							
							}	
						}
					}
					
					for(int a = 0;  a < nmbOfTasks.length; a++) //generating workload
					{
						app[a] = new App(nmbOfTasks[a]);
						for(int iTask = 0; iTask < nmbOfTasks[a]; iTask++)
					    {	  
							app[a].tasks[iTask] = task[iTask];
						}				
					}			
				
					
					maxNmbOfTasks = 0;
			    	indexMaxNmbTask = 0;	  
			    	for(int l = 0; l < nmbOfTasks.length; l++) //max number of offloadable tasks search
				    {
			    		if(maxNmbOfTasks < nmbOfTasks[l]) {
			    			maxNmbOfTasks = nmbOfTasks[l];
			    			indexMaxNmbTask = l;
			    		}
				    }
			    	
			       	double changeBW[][] = new double[nodesCreated][maxNmbOfTasks];
			    	double changeCPU[][] = new double[nodesCreated][maxNmbOfTasks];
			    	double changeLAT[][] = new double[nodesCreated][maxNmbOfTasks]; 
			    	
			    	double bwHigherBound[] = new double[nodesCreated];
			    	double cpuHigherBound [] = new double[nodesCreated];
					
			    	double bwLowerBound[] = new double[nodesCreated];
			    	double cpuLowerBound[] = new double[nodesCreated];
			    	int rangeLowerBound = -1; 
			    	int rangeHigherBound = 1;	    	 	
			    	double rangeSize = 2; //calculate as: (rangeSizeLowerBound*rangeSizeLowerBound) + (rangeSizeHigherBound*rangeSizeHigherBound)
			    	for(int n = 0; n < nodesCreated; n++) 
				    {
			    		rangeLowerBound = -1;
			    		rangeHigherBound = 1;	
			    		rangeSize = 2; 
			    		bwLowerBound[n] =  (rangeLowerBound)+ (double)(Math.random() * rangeSize);
			    		cpuLowerBound[n] = (rangeLowerBound)+ (double)(Math.random() * rangeSize);
			    		bwHigherBound[n] =  (rangeLowerBound)+ (double)(Math.random() * rangeSize);
			    		cpuHigherBound[n] =  (rangeLowerBound)+ (double)(Math.random() * rangeSize);
			    		while(bwHigherBound[n] < bwLowerBound[n]) //lowerBound[i] must be < then higherBound[i]
			    		{
			    			bwHigherBound[n] =  (rangeLowerBound)+ (double)(Math.random() * rangeSize);
			    		}
			    		while(cpuHigherBound[n] < cpuLowerBound[n]) //lowerBound[i] must be < then higherBound[i]
			    		{
			    			cpuHigherBound[n] =  (rangeLowerBound)+ (double)(Math.random() * rangeSize);
			    		}    			    	    		
			    		for(int t = 0; t < maxNmbOfTasks; t++)
			    		{
			    			if(bwHigherBound[n] == bwLowerBound[n])
			    			{
			    				changeBW[n][t] = bwLowerBound[n]; 
			    			}
			    			else
			    			{
			    				//(bwLowerBound[i] >= 0 && bwHigherBound[i] < 0) can never happen since higherBound[i] is always >= compared to lowerBound[i]
				    			if( bwLowerBound[n]<0 && bwHigherBound[n] >= 0) //e.g. from -0.7 to 0.3
				    			{ 
				    				rangeSize = (bwLowerBound[n]*(-1)) + bwHigherBound[n]; 
				    			}
				    			else if(bwLowerBound[n] < 0 && bwHigherBound[n] < 0)//e.g. from -0.7 to -0.3
				    			{ 
				    				rangeSize = (bwLowerBound[n]*(-1)) - (bwHigherBound[n]*(-1));
				    			}
				    			else //e.g. from 0.6 to 0.9 
				    			{
				    				rangeSize = bwHigherBound[n] - bwLowerBound[n];
				    			}
				    			changeBW[n][t] = (bwLowerBound[n]) + (double)(Math.random() * rangeSize);
			    			}	    			
			    			
			    			if(cpuLowerBound[n] == cpuHigherBound[n])
			    			{
			    				changeCPU[n][t] = cpuLowerBound[n]; 
			    			}
			    			else
			    			{
				    			if( cpuLowerBound[n]<0 && cpuHigherBound[n] >= 0)  
				    			{ 
				    				rangeSize = (cpuLowerBound[n]*(-1)) + cpuHigherBound[n]; 
				    			}
				    			else if(cpuLowerBound[n] < 0 && cpuHigherBound[n] < 0)
				    			{ 
				    				rangeSize = (cpuLowerBound[n]*(-1)) - (cpuHigherBound[n]*(-1));
				    			}
				    			else
				    			{
				    				rangeSize = cpuHigherBound[n] - cpuLowerBound[n];
				    			}
				    			changeCPU[n][t] = (cpuLowerBound[n]) + (double)(Math.random() * rangeSize);
				    		}
			    			changeLAT[n][t] = (0) + (double)(Math.random() * 1); //increasing the latency as user going further away
			    		}	    	
				    }
			    	
			    	for(int p = 0;  p < pole.length; p++)
			    	{
			    		currentApproach = 0;
			    		oneApproachResults = FuzzyHandoffController.execution(nodesCreated, app, nodeTopologySetup,  pole[p], desiredRT, changeBW, changeCPU, changeLAT, discoveredNodes, nmbOfTasks, indexMaxNmbTask);
						for (int t= 0 ; t < nmbOfTasks.length ; t++)
						{
							allApproachResults[p][currentApproach].handoffs[t] = allApproachResults[p][currentApproach].handoffs[t] + oneApproachResults.handoffs[t];
							allApproachResults[p][currentApproach].appRT[t] = allApproachResults[p][currentApproach].appRT[t] + oneApproachResults.appRT[t];
							allApproachResults[p][currentApproach].monetaryCost[t] = allApproachResults[p][currentApproach].monetaryCost[t] + oneApproachResults.monetaryCost[t];
						}		
						currentApproach++;
						oneApproachResults = NeverHandoff.execution(nodesCreated, app, nodeTopologySetup,  pole[p], desiredRT, changeBW, changeCPU, changeLAT, discoveredNodes, nmbOfTasks, indexMaxNmbTask);
						for (int t= 0 ; t < nmbOfTasks.length ; t++)
						{
							allApproachResults[p][currentApproach].handoffs[t] = allApproachResults[p][currentApproach].handoffs[t] + oneApproachResults.handoffs[t];
							allApproachResults[p][currentApproach].appRT[t] = allApproachResults[p][currentApproach].appRT[t] + oneApproachResults.appRT[t];
							allApproachResults[p][currentApproach].monetaryCost[t] = allApproachResults[p][currentApproach].monetaryCost[t] + oneApproachResults.monetaryCost[t];
						}	
						currentApproach++;
						oneApproachResults = AlwaysHandoff.execution(nodesCreated, app, nodeTopologySetup, pole[p], desiredRT, changeBW, changeCPU, changeLAT, discoveredNodes, nmbOfTasks, indexMaxNmbTask); 
						for (int t= 0 ; t < nmbOfTasks.length ; t++)
						{
							allApproachResults[p][currentApproach].handoffs[t] = allApproachResults[p][currentApproach].handoffs[t] + oneApproachResults.handoffs[t];
							allApproachResults[p][currentApproach].appRT[t] = allApproachResults[p][currentApproach].appRT[t] + oneApproachResults.appRT[t];
							allApproachResults[p][currentApproach].monetaryCost[t] = allApproachResults[p][currentApproach].monetaryCost[t] + oneApproachResults.monetaryCost[t];
						}
						currentApproach++;
		
			    	}	
					System.out.println(" End of iteration: " + i);			
				}//("end of iteration"); 
				
			
			    
				for(int p = 0; p < pole.length; p++)
				{
					for(int a = 0; a < nmbOfApproaches; a++)
			    	{   for (int t = 0; t < nmbOfTasks.length; t++)
				    	{
			    			handoffsAverage[p][a][t] = (double)((double)allApproachResults[p][a].handoffs[t] / (double)iteration);	 
			    			appRTaverage[p][a][t] = (double)((double)allApproachResults[p][a].appRT[t] / (double)iteration);
			    			monetaryTimeCostAverage[p][a][t] = (double)((double)allApproachResults[p][a].monetaryCost[t] / (double)iteration);
				    	}
				    }
				}		
				for(int p = 0; p < pole.length; p++)
				{
					for(int r = 1; r < nmbOfApproaches; r++) 
					{
						ResultsEvaluation.inform(approach[r], nodesCreated, iteration);
						ResultsEvaluation.evaluate(appRTaverage[p][0],  monetaryTimeCostAverage[p][0], handoffsAverage[p][0], nmbOfTasks.length, handoffsAverage[p][r], appRTaverage[p][r],  monetaryTimeCostAverage[p][r]);
					}
				}
			  }
	     catch(ArrayIndexOutOfBoundsException e){
	        System.out.println(" Please, make sure the number of created nodes in topology is high enough to cover Always Handoff approach. ");
	     }
	     catch(Exception e){
	        System.out.println(" Please try again. ");
	     }
		
		 System.out.println("end of execution"); 
			
  }
		
}

