package simulationModels;

public class Topology {
	
	public Node[] nodes;
	public Topology ()	{}	
	public Topology (int discoveredNodesNmb)
	{
		nodes = new Node[discoveredNodesNmb]; 
	}
}
