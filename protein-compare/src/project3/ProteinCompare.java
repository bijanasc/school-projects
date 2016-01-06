package project3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ProteinCompare {


	public static void main(String[] args) {

		ArrayList<String> proteinList = readDirectoryContents("fasta");
		
		for (int i = 0; i<proteinList.size(); i++){
			
			String temp = proteinList.get(i);
			temp = removeFirstLine(temp);
			proteinList.set(i, temp);
			
		}

		for(int i = 0; i < proteinList.size(); i++){
			for(int j = i; j < proteinList.size(); j++){
				
				int distance = getDistance(proteinList.get(i), proteinList.get(j));				
				
				System.out.println("Protein" + i + "\t" + "Protein" + j + "\t" + "cost: " + distance);
				
			}			
		}		
	}


	public static String readFile(String filename) {

		Path path = Paths.get(filename);

		String stringFromFile = "";

		try {
			stringFromFile = new String(
					java.nio.file.Files.readAllBytes(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("file does not exist: " + filename);
		}

		return stringFromFile;
	} 
	
	public static ArrayList<String> readDirectoryContents(String directory) {
		
		File dir = new File(directory);
		
		ArrayList<String> list = new ArrayList<String>();

		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				// Do something with child
				list.add(readFile(child.getPath()));
			}
		} else {
			
			System.out.println("fasta directory missing");
		}

		return list;
		
	}
	
	private static int getDistance(String s1, String s2) {
		
		//create two dimensional array to represent graph
		int[][] array = new int [s1.length()+1][s2.length()+1];
		
		//input values into array using delta
		for(int i = 1; i <= s1.length(); i++) {
			
			array[i][0] = array[i-1][0] + 2;
			
		}
		
		//input values into array using delta
		for(int i = 1; i <= s2.length(); i++) {
			
			array[0][i] = array[0][i-1] + 2; 
			
		}
		
		//input values in graph
		for(int i = 1; i <= s1.length(); i++) {
			
			for (int j = 1; j <= s2.length(); j++) {
				
				int left = array[i][j-1];
				int top = array[i-1][j];
				int diagonal = array[i-1][j-1];
				
                left+= 2;
                top += 2;
				
               if (s2.charAt(j-1) != s1.charAt(i-1)) {
            	   
            	   diagonal += 1;            	   
               }
               
               array[i][j] = Math.min(top, Math.min(left, diagonal));              
			}			
		}
		//return bottom rightmost index value in the array		
		return array[s1.length()][s2.length()];
	}
	
public static String removeFirstLine(String s){
		
		int endIndex = s.indexOf("\n");
		String substring = s.substring(endIndex + 1);
		
		return substring;
	}

}
