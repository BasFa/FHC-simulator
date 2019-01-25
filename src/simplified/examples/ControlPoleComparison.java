package simplified.examples;



import q2.utilities.ControlOutput;

public class ControlPoleComparison {
	
	public static void main(String[] args) throws Exception {
		System.out.println(" Pole (tunable parameter) - Comparison; Example:");
		
		double measuredRT[] = {300, 350, 400, 450, 500, 550, 600, 650, 700, 750}; //ms
		double desiredRT = 400; //ms
		double pole[] = {0.3, 0.5, 0.7, 0.9}; //values from 0 to 1: [0-1]
		double controlerOutput[][] = new double[pole.length][measuredRT.length+1]; //[0-1]
		double nmbOfDeterioratedTasks[] = {0, 0 , 0, 0}; 
		boolean hasHandoff[] = {false, false, false, false};  
		double controlThreshold[][] = new double[pole.length][measuredRT.length]; //[0-1]
		int handoffStartAt[] = {0, 0 , 0, 0}; //after this task number handoff is triggered
		for(int p = 0; p < pole.length; p++) 
		{ 
			for(int t = 0; t < measuredRT.length; t++) //measuredRT.length = number of offloaded tasks
			{
				controlerOutput[p][t] = 1; //default value 
				controlThreshold[p][t] = 0; //default value
			}
		}
		
		for(int p = 0; p < pole.length; p++) 
		{ 
			for(int t = 0; t < measuredRT.length; t++) //measuredRT.length = number of offloaded tasks
			{ 
				if(t==0)
					controlerOutput[p][t]  = ControlOutput.calculate(pole[p], desiredRT, measuredRT[t], controlerOutput[p][t]); 
				else 
					controlerOutput[p][t]  = ControlOutput.calculate(pole[p], desiredRT, measuredRT[t], controlerOutput[p][t-1]); 			
				
				
				if(t!=0 && ((int)measuredRT[t]  > desiredRT) ) 
				{
					nmbOfDeterioratedTasks[p]++;    			
				}
				
				controlThreshold[p][t] = 1 - ( 1 / (1 + nmbOfDeterioratedTasks[p] ) ); 
				controlThreshold[p][t] = (double)Math.round(controlThreshold[p][t] * 10000d) / 10000d ; 
				
				if(controlerOutput[p][t] < controlThreshold[p][t] && (int)measuredRT[t] > desiredRT ) 
				{
					hasHandoff[p] = true;
					handoffStartAt[p] = t;
					//handoff triggered, stop execution, compare for the next pole value
					t = measuredRT.length; 
					System.out.println(" For a pole " + pole[p] + " handoff is triggered after task nmb " + (handoffStartAt[p]+1) + " with a measured RT: " + measuredRT[handoffStartAt[p]]); 	
				}    	
				
			}
		}
		
		for(int p = 0; p < pole.length; p++)
		{ 
			System.out.println( " Pole: " + pole[p]  + ", nmb of deteriorated tasks:" + (nmbOfDeterioratedTasks[p]-1) + ", handoff trigered: " + hasHandoff[p]); 
			for(int t = 0; t < handoffStartAt[p]+1; t++) //offloaded tasks
			{
				System.out.println(" Task number: " + t + "; Control output value:  "  + controlerOutput[p][t] + ",  Control Threshold value:" + controlThreshold[p][t] ); 
			}
		}
		
		System.out.println(" End of example "); 	
	}
}
