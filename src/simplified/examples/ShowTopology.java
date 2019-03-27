package simplified.examples;

import q2.utilities.EdgeNodeTopology;
import simulationModels.Topology;

public class ShowTopology {
	
		public static void main(String[] args) throws Exception 
		{
			System.out.println(" Generate Topology, example ");						
			int discoveredNodes = 20;
			int nodesCreated = discoveredNodes; 	 	
			Topology topologySetup = EdgeNodeTopology.generateNodesTopology(nodesCreated, discoveredNodes);
			for(int n = 0;  n < topologySetup.nodes.length; n++) 
			{
				System.out.println(n + " Latency (ms): " + topologySetup.nodes[n].latency + " Bandwidth (Mbps): " + topologySetup.nodes[n].bandwidth + " CPU (MIPS): " + topologySetup.nodes[n].cpu);
			}
		}
}
