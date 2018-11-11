package simulationModels;

public class Task {
	public double dataSize;
	public double operations;
	
	public Task(double mi , double data){
		dataSize = data;
		operations  = mi; //millions of instructions
	}	
	public Task()
	{
		dataSize = 0;
		operations  = 0;
	}
}
