package simulationModels;

public class Offload {

	public static double measureRT(Task appRunning, Node nodeSelected)
	{
		//executionTime = remote RT = communicationDelay + transferTime + computeTime;
		double responseTime = 0; 
		double transferTime = 0;
		double executionTime = 0;
		double communicationDelay = nodeSelected.latency;		
		transferTime = (1/nodeSelected.bandwidth) * appRunning.dataSize * 1000; //1000: s in ms
		executionTime = (1/ (nodeSelected.cpu )) * appRunning.operations * 1000; //1000: s in ms
		responseTime = (2*transferTime) +  executionTime + (2*communicationDelay);  
		
		return responseTime;		
	}	
}
