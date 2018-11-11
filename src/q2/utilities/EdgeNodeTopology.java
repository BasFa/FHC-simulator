package q2.utilities;

import simulationModels.Node;
import simulationModels.Topology;

import org.apache.commons.math3.distribution.ParetoDistribution;
public class EdgeNodeTopology {
	
	public static Topology generateNodesTopology(int nodesCreated, int newlyDiscoveredNodes) throws Exception 
	{
	    double nodeBandwidth;
	    double nodeCpu; 
		double nodeLatency;
	
		double cpuWifi3G[] =  new double[nodesCreated];   
		double lat3G[] = new double[nodesCreated/2];
		double latWifi[] = new double[nodesCreated];
		double bw3G[] = new double[nodesCreated/2];
		double bwWifi[] = new double[nodesCreated];
		int counter3G = 0; //from 0 to nodesCreated/2  //half 3G and half wifi
		int counterWifi = ((int)nodesCreated/2); //from nodesCreated/2 to nodesCreated		
		for(int n = 0 ; n < nodesCreated; n++ ) 
		{
			cpuWifi3G[n] = 0;
			latWifi[n] = 0;
			bwWifi[n] = 0;
		}
		for(int n = 0 ; n < nodesCreated/2; n++ ) 
		{
			cpuWifi3G[n] = 0;
			latWifi[n] = 0;
			bwWifi[n] = 0;
		}
			
		int halfDiscoverd = 0;		
		double CPUdesiredMean = 1; //cpu wifi or 3G- 15 MIPS
		double G3desiredMeanLatency = 1; //latency 3G - 54ms
		double WIFIdesiredMeanLatency = 1; //latency wifi - 15ms
		double G3desiredMeanBW = 1; //bandwidth 3G - 7.2ms
		double WIFIdesiredMeanBW =  1;  ///bandwidth wifi - 32ms		
		int maxBw = 50; //following input MF for bandwidth 
		int maxLat = 95; //following input MF for latency
		int maxCpu = 40; //following input MF for cpu
		double shapeParameter = 1.25;
		ParetoDistribution bwParetoDist3G;
		ParetoDistribution bwParetoDistWifi; 
		ParetoDistribution cpuParetoDist; 
		ParetoDistribution latParetoDist3G; 
		ParetoDistribution latParetoDistWifi;   
		double bwPareto3G[] = new double[nodesCreated];
		double bwParetoWifi[] = new double[nodesCreated];
		double cpuPareto[] = new double[nodesCreated];
		double latPareto3G[] = new double[nodesCreated];
		double latParetoWifi[] = new double[nodesCreated];
  
		bwParetoDist3G = new ParetoDistribution( 7.2, shapeParameter); 
		bwParetoDistWifi = new ParetoDistribution(32, shapeParameter); 
		cpuParetoDist = new ParetoDistribution(15, shapeParameter);
		latParetoDist3G = new ParetoDistribution(54, shapeParameter);
		latParetoDistWifi = new ParetoDistribution(15, shapeParameter);
		
		for (int i = 0; i < nodesCreated; i++)
		{	
			bwPareto3G[i] = bwParetoDist3G.sample();
			while (bwPareto3G[i] > maxBw)
				bwPareto3G[i] = bwParetoDist3G.sample();			
			bwParetoWifi[i] = bwParetoDistWifi.sample();
			while (bwParetoWifi[i] > maxBw)
				bwParetoWifi[i] = bwParetoDistWifi.sample();
			cpuPareto[i] = cpuParetoDist.sample();
			while (cpuPareto[i] > maxCpu)
				cpuPareto[i] = cpuParetoDist.sample();
			latPareto3G[i] = latParetoDist3G.sample();
			while (latPareto3G[i] > maxLat)
				latPareto3G[i] = latParetoDist3G.sample();
			latParetoWifi[i] = latParetoDistWifi.sample();
			while (latParetoWifi[i] > maxLat) 
				latParetoWifi[i] = latParetoDistWifi.sample();	
		}	
		   	   
		for(int n = 0; n < nodesCreated/2; n++) //cellular (3G) connection 
	    {
			lat3G[n] = G3desiredMeanLatency * latPareto3G[n];
			bw3G[n] = G3desiredMeanBW * bwPareto3G[n];
	    }
		for(int iCreated = (int)nodesCreated/2; iCreated < nodesCreated; iCreated++) // Wi-Fi connection
	    {
			latWifi[iCreated] = WIFIdesiredMeanLatency * latParetoWifi[iCreated];
			bwWifi[iCreated] = WIFIdesiredMeanBW * bwParetoWifi[iCreated];
	    }
				
		for(int i = 0 ; i < nodesCreated; i++ )  // Wi-Fi and cellular (3G) connection 
    	{ 
			cpuWifi3G[i] = CPUdesiredMean * cpuPareto[i];
    	}
			
		if (nodesCreated == 0) {
	    	System.out.println("There is no discovered node for offloading your task, computation will be done localy or in cloud.");
	    	return null;
	    }
	    else if  (nodesCreated == 1) {
	    	System.out.println("There is only one discovered node available for offloading your app. ");
	    	return null;
	    }
	    else {  
	        Topology nodeTopology = new Topology(nodesCreated);
	    	for(int n = 0 ; n <= nodesCreated - 1; n++ )  
	    	{    		
	    		nodeCpu = CPUdesiredMean * cpuPareto[n];
	    		if(halfDiscoverd < newlyDiscoveredNodes/2) //cellular (3G) connection 
				{ 
	        		nodeLatency = lat3G[counter3G];	        		
		        	nodeBandwidth = bw3G[counter3G];
		        	counter3G++;
				}
				else  // Wi-Fi connection
				{
					nodeLatency = latWifi[counterWifi];
					nodeBandwidth = bwWifi[counterWifi];
		            counterWifi++;
				}  						
	        	halfDiscoverd++;
				if(halfDiscoverd >= newlyDiscoveredNodes)
					halfDiscoverd = 0;
				
	    		Node nodeTemporarly = new Node(nodeBandwidth, nodeCpu, nodeLatency, 0 );  // first set fuzzy values to 0 for all nodes
	    		nodeTopology.nodes[n] = nodeTemporarly;
	    	}
	    	
	    		
	    	for(int i = 0 ; i < nodesCreated; i++ ) //calculate and save fuzzy value for all nodes
	    	{    		
	    		nodeTopology.nodes[i].fuzzyValue = FuzzyOutputValue.calculateFuzzyValue(nodeTopology.nodes[i]) ;  	
	    	}
	    	
	    	return nodeTopology; 
	    }
	  }


	
	public static  Topology initializeTopology(int nodesCreated) throws Exception 
	{
		  Topology nodeTopology = new Topology(nodesCreated);
	      for(int i = 0 ; i < nodesCreated; i++ ) //a parameters collection from the discovered nodes
	      {  
	    	  Node nodeTemporarily = new Node(0, 0, 0, 0);  //nodeBandwidth, nodeCpu, nodeLatency, fuzzy value
	    	  nodeTopology.nodes[i] = nodeTemporarily;
	      } 
	      return nodeTopology;
	}


	public static void update(Topology onceFullclusterFilled, int lowerBound, int higherBound,double changeBW[][], double changeCPU[][], double changeLAT[][], int currentChangeIndex) throws Exception 
	{
		//dynamic changes in network and edge node workload
		for (int n = lowerBound; n < higherBound; n++) //affecting currently discovered nodes.
		{							
			double changeBWamount = 0;
			double changeCPUamount = 0;
			double changeLATamount = 0;				
			int minPositiveValue = 5; //to avoide making bw, lat or cpu negativ						 
			changeBWamount  = changeBW[n][currentChangeIndex] * onceFullclusterFilled.nodes[n].bandwidth /100; //Percent of a change
			changeCPUamount = changeCPU[n][currentChangeIndex] * onceFullclusterFilled.nodes[n].cpu / 100; 
			changeLATamount = changeLAT[n][currentChangeIndex] * onceFullclusterFilled.nodes[n].latency / 100;
			if(onceFullclusterFilled.nodes[n].bandwidth > minPositiveValue ) 
				onceFullclusterFilled.nodes[n].bandwidth = onceFullclusterFilled.nodes[n].bandwidth  + changeBWamount;
			if(onceFullclusterFilled.nodes[n].cpu > minPositiveValue ) 	
				onceFullclusterFilled.nodes[n].cpu = onceFullclusterFilled.nodes[n].cpu  + changeCPUamount; 
			if(onceFullclusterFilled.nodes[n].latency > minPositiveValue )
				onceFullclusterFilled.nodes[n].latency = onceFullclusterFilled.nodes[n].latency  + changeLATamount ;
		} 
	}
	
}