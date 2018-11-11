package q2.utilities;

import java.io.IOException;


public class ControlOutput 
{	
	public static double calculate(double pole, double desiredRT, double measuredRT, double controlerOutput) throws IOException
	{	
		double error = desiredRT - measuredRT; 
		double alfa = measuredRT / controlerOutput; 
		controlerOutput =  controlerOutput + (1 / alfa) * (1 - pole) * error;		
		controlerOutput = (double)Math.round(controlerOutput * 10000d) / 10000d ;
		controlerOutput = Math.max(controlerOutput, 0.0);  //controlerOutput = ctli -> alwyas in [0, 1] range
		controlerOutput = Math.min(controlerOutput, 1.0);
		return controlerOutput;
	}
}
