package project3;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ShowAlignment {
	
	final static int MATCH = 0;
	final static int NONMATCH = 2;
	final static int DELTA = 2;

	public static void main(String[] args) {

		//create two strings from command line arguments
		String s1 = readFile("fasta/" + args[0]);
		String s2 = readFile("fasta/" + args[1]);
		
		s1 = removeFirstLine(s1);
		s2 = removeFirstLine(s2);
		
		s1 = s1.replaceAll("\n", "");
		s2 = s2.replaceAll("\n", "");
		
		getDistanceResult result = getDistance(s1, s2);
		
//		for(int i = 0; i < result.costs.length; i++)
//		{
//			for(int j = 0; j < result.costs.length; j++)
//				System.out.print((-1*result.costs[j][i]) + "\t");
//			System.out.println();
//		}
//
//		for(int i = 0; i < result.directions.length; i++)
//		{
//			for(int j = 0; j < result.directions.length; j++)
//				System.out.print((result.directions[j][i]) + "\t");
//			System.out.println();
//		}

		System.out.println("Cost of " + result.distance);
		
		int x = result.costs.length-1;
		int y = result.costs[0].length-1;
		
		String buffer = "";
		int i=401;
		while(x != 0 && y != 0){
			
			
			if(result.directions[x][y] == direction.top_) {
				
				buffer = i + " " + " " + " " + s2.charAt(y-1) + "  " + (DELTA+1) + "\n" + buffer;
				y--;
				
			}
			else if(result.directions[x][y] == direction.left) {
				
				buffer = i + " " + s1.charAt(x-1) + " " + " " + "  " + DELTA + "\n" + buffer;
				x--;
				
			}
			else{
				if(s1.charAt(x-1) == s2.charAt(y-1)){
				buffer = i + " " +  s1.charAt(x-1) + " " + s2.charAt(y-1) + "  " + MATCH + "\n" + buffer;
			
				}
				else { 
					buffer = i + " " + s1.charAt(x-1) + " " + s2.charAt(y-1) + "  " + NONMATCH + "\n" + buffer;
				}
				y--;
				x--;
			}
			i--;
		}
		
		System.out.println(buffer);
		
	}
	
	public static String readFile(String filename) {

		Path path = Paths.get(filename);

		String stringFromFile = "";

		try {
			stringFromFile = new String(
					java.nio.file.Files.readAllBytes(path));
		} catch (IOException e) {
			System.out.println("file does not exist: " + filename);
		}

		return stringFromFile;
	}
	
	
	
	static class getDistanceResult{
		
		public int distance;
		
		public int[][] costs;
		
		public direction[][] directions;
		
	}
	
	static enum direction { top_, left, diag}
	
	
	private static getDistanceResult getDistance(String s1, String s2) {
		
		//create two dimensional array to represent graph
		int[][] costs = new int [s1.length()+1][s2.length()+1];
		direction[][] directions = new direction [s1.length()+1][s2.length()+1];
		
		//input values into array using delta
		for(int i = 1; i <= s1.length(); i++) {
			
			costs[i][0] = costs[i-1][0] + DELTA;
			directions[i][0] = direction.left;
			
		}
		
		//input values into array using delta
		for(int i = 1; i <= s2.length(); i++) {
			
			costs[0][i] = costs[0][i-1] + DELTA; 
			directions[0][i] = direction.top_;

		}
		
		//input values in graph
		for(int j = 1; j <= s2.length(); j++) {
			
			for (int i = 1; i <= s1.length(); i++) {
				
				int left = costs[i][j-1];
				int top = costs[i-1][j];
				int diagonal = costs[i-1][j-1];
				
                left+= DELTA;
                top += DELTA;
				
               if (s2.charAt(j-1) != s1.charAt(i-1)) {
            	   
            	   diagonal += NONMATCH;            	   
               }
               else {
            	   
            	   diagonal += MATCH;
            	   
               }
               
               costs[i][j] = Math.min(top, Math.min(left, diagonal));
               
               if(costs[i][j] == top){
               directions[i][j] = direction.left;
               }
               else if(costs[i][j] == left){
            	   directions[i][j] = direction.top_;
            	   
               }
               else {
            	   
            	   directions[i][j] = direction.diag;
            	   
               }
			}			
		}
		//return bottom rightmost index value in the array
		getDistanceResult result = new getDistanceResult();
		result.distance = costs[s1.length()][s2.length()];
		result.costs = costs;
		result.directions = directions;
		return result;
	}
	
	public static String removeFirstLine(String s){
		
		int endIndex = s.indexOf("\n");
		String substring = s.substring(endIndex + 1);
		
		return substring;
	}

}
