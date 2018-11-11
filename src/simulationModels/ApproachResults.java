package simulationModels;

public class ApproachResults {

	public int[] handoffs;
	public double[] appRT;
	public double[] monetaryCost;
	
	public ApproachResults (){}
	
	public ApproachResults(int[] migrationsNMB, double[] appRTall, double[] monetaryTimeCost){
		handoffs = migrationsNMB;
		appRT  = appRTall;
		monetaryCost = monetaryTimeCost;
	} 
}
