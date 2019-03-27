package q2.utilities;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

import simulationModels.Topology;

public class ResultsEvaluation {
	
	public static void evaluate(double[] fhcRT , double[] fhcMonetaryCost, double[] fhcHandoffs, int tS, double[] aHandoffs, double[] approachRT , double[] aMonetaryCost) throws IOException
	{	
		//Writer approachAllResults; //save in file
		//approachAllResults = new BufferedWriter(new FileWriter("C:\\Users\\Documents...txt", true));
		
		//tS = possible taskSizes = nmbOfTasks.length 
		// a = approach
		//aHandoffs = number of handoffs executed in another approach used to compare to FHC
		
		//fhcRT in milliseconds
		//fhcMonetaryCost in minutes
		
		double msToMin = 60000;
		double msToSec= 1000; //divide
		double secToMin = 60;
		double secToMs = 1000; //multiply		 
		//handoff brings additional time cost (e.g. VM initialization)
		double hAddTimeCost[] = {0.42, 15.8, 39.3}; //0.42s, 15.8s, 39.3s 
		double hAddTimeCostFHC_s[][] = new double[hAddTimeCost.length][tS];
		double hAddTimeCostA_s[][] = new double[hAddTimeCost.length][tS];
		double fhcRTfinal_s[][] = new double [hAddTimeCost.length][tS]; 
		double aRTfinal_s[][] = new double [hAddTimeCost.length][tS]; 		 
		double monetrayCostPercent[] =  new double[tS];
		double finalRTpercente[][] = new double [hAddTimeCost.length][tS]; 
		
		for (int l = 0; l < hAddTimeCost.length; l++) //setter
		{
			for (int p = 0; p < tS; p++) //tS = possible taskSizes = nmbOfTasks.length 
			{
				aRTfinal_s[l][p] = 0;
				fhcRTfinal_s[l][p] = 0;
				finalRTpercente[l][p] = 0;
			}	
		}	 				 
		//RT cost
		for (int l = 0; l < hAddTimeCost.length; l++)
		{
			for (int p = 0; p < tS; p++)
			{
				hAddTimeCostA_s[l][p] = aHandoffs[p] * hAddTimeCost[l];
				aRTfinal_s[l][p] = hAddTimeCostA_s[l][p] + (approachRT[p]/msToSec);				 
				hAddTimeCostFHC_s[l][p] = fhcHandoffs[p] * hAddTimeCost[l];
				fhcRTfinal_s[l][p] = hAddTimeCostFHC_s[l][p] + (fhcRT[p]/msToSec);				 
			}
		}
		//monetary cost
		for (int p = 0; p < tS; p++) 
		{
			monetrayCostPercent[p] = 0;
			if(aMonetaryCost[p] == 0 ) //setter for NeverHandoff approach where nmb of handoffs is always 0
				aMonetaryCost[p] =  approachRT[p]/msToMin; //
		}		 
		String approachResults = ""; 
		approachResults =  (" monetaryCostReducedPercente: ");
		for (int p = 0; p < tS; p++)
		{
			monetrayCostPercent[p] = ((aMonetaryCost[p]-fhcMonetaryCost[p])/aMonetaryCost[p]*100);
			approachResults +=  ( String.valueOf(((double)Math.round( ( monetrayCostPercent[p]* 100d)  ) / 100d)) + " ");
		}		 
		for (int l = 0; l < hAddTimeCost.length; l++)
		{
			approachResults += ("  RTreducedPercente " + hAddTimeCost[l] + ": ");
			for (int nt = 0; nt < tS; nt++)
			{
				finalRTpercente[l][nt] = (((aRTfinal_s[l][nt]/secToMin) - (fhcRTfinal_s[l][nt]/secToMin)) / (aRTfinal_s[l][nt]/secToMin) * 100) ;
				approachResults += ( String.valueOf(((double)Math.round( ( finalRTpercente[l][nt]* 100d)  ) / 100d)) + " ");
			}
		}
		approachResults += (System.lineSeparator());
		System.out.println(approachResults);
		
		//approachAllResults.write(approachResults);
		//approachAllResults.close();		
	}		
	
	
	public static void inform(String approach, int nodesCreated, int iteration) throws IOException
	{	
		//Writer generalInformation;  //save in file
		//generalInformation = new BufferedWriter(new FileWriter("C:\\Users\\Documents...txt", true));
		String info = (System.lineSeparator() + approach + " approach compared with Proposed_FuzzyHandoffControl;   nodeTopologySize: " + String.valueOf(nodesCreated) + "nodes; simulations repeatead " + String.valueOf(iteration) + " times; " + " Results saved as percentages.");
		System.out.println(info);
		//generalInformation.write(info);
		//generalInformation.close();		
	}	
	
	
}	
	 
	 
	
