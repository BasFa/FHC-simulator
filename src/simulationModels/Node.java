package simulationModels;

public class Node {
	
	public double bandwidth;
	public double cpu;
	public double latency;
	public double fuzzyValue;

	public Node(double nBW, double nCPU, double nLAT, double fValue){
		bandwidth = nBW;
		cpu  = nCPU;
		latency = nLAT;
		fuzzyValue = fValue;
	}		
}
