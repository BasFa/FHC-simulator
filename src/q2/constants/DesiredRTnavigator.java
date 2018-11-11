package q2.constants;

public class DesiredRTnavigator {
	
	public double[][] taskDesiredRT = new double[3][4];
	
	
	
	public DesiredRTnavigator ()
	{
	
		//desiredRT for 4 offloadable tasks
		//average + 50%
		//desiredRT for map size: 5MB
		taskDesiredRT[0][0] = 552;
		taskDesiredRT[0][1] = 619;
		taskDesiredRT[0][2] = 754;
		taskDesiredRT[0][3] = 754;		
		
		//desiredRT for map size: 10MB
		taskDesiredRT[1][0] = 552;
		taskDesiredRT[1][1] = 955;
		taskDesiredRT[1][2] = 1089;
		taskDesiredRT[1][3] = 1090;
		
		
		//desiredRT for map size: 15MB
		taskDesiredRT[2][0] = 552;
		taskDesiredRT[2][1] = 1304;
		taskDesiredRT[2][2] = 1439;
		taskDesiredRT[2][3] = 1439;

	}
}
