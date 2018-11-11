package q2.constants;

public class DesiredRTfacerecognizer {
	
	public double[][] taskDesiredRT = new double[2][3];		
	
	public DesiredRTfacerecognizer ()
	{	
		//desiredRT for 3 offloadable tasks
		//average + 50%
		//desiredRT for image size: 1MB 
 		taskDesiredRT[0][0] = 413;
 		taskDesiredRT[0][1] = 414;
 		taskDesiredRT[0][2] = 686;
			
		//desiredRT for image size: 5MB			
 		taskDesiredRT[1][0] = 686;
 		taskDesiredRT[1][1] = 687;
 		taskDesiredRT[1][2] = 959;
	}
}
