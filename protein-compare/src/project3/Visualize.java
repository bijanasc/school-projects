package project3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class Visualize {

	public static void main(String[] args) {

		ArrayList<String> proteinList = readDirectoryContents("fasta");
		ArrayList<String> proteinNames = new ArrayList<String>();

		for (int i = 0; i<proteinList.size(); i++){

			String temp = proteinList.get(i);
			String tempName = getProteinName(temp);
			proteinNames.add(tempName);
			temp = removeFirstLine(temp);
			proteinList.set(i, temp);
			
		}
		
		int counter = 0;
		int totalDistance = 0;

		Graph g = new SingleGraph("");

		for (int i = 0; i < proteinList.size(); i++) {
			for (int j = i; j < proteinList.size(); j++) {

				int distance = getDistance(proteinList.get(i), proteinList.get(j));				
				totalDistance += distance;
				
				if(g.getNode(proteinNames.get(i)) == null){
					g.addNode(proteinNames.get(i)).addAttribute("ui.label", proteinNames.get(i));
				}
				
				if(g.getNode(proteinNames.get(j)) == null){
					g.addNode(proteinNames.get(j)).addAttribute("ui.label", proteinNames.get(j));
				}

				Edge e = g.addEdge(proteinNames.get(i) + "-" + proteinNames.get(j), proteinNames.get(i), proteinNames.get(j));
				e.addAttribute("weight", distance);
				e.addAttribute("ui.label", distance);
				e.setAttribute("ui.style", "size:1px; fill-color: rgb(0,204,0);");
				
				
				if(i != j){
					counter++;
				}
			}
		}
		
		int distanceThreshhold = totalDistance/counter;
		
		System.out.println(distanceThreshhold);

		boolean foundEdgesToRemove = true;

		while(foundEdgesToRemove) {
			foundEdgesToRemove = false;
			for(Edge e: g.getEdgeSet()){

				if(e.getNumber("weight") > distanceThreshhold) {

					g.removeEdge(e);
					foundEdgesToRemove = true;
					break;
				}
			}
		}
		
		g.display(true);
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

	public static ArrayList<String> readDirectoryContents(String directory) {

		File dir = new File(directory);

		ArrayList<String> list = new ArrayList<String>();

		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
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
		for (int i = 1; i <= s2.length(); i++) {

			array[0][i] = array[0][i-1] + 2; 
		}

		//input values in graph
		for (int i = 1; i <= s1.length(); i++) {

			for (int j = 1; j <= s2.length(); j++) {

				int left = array[i][j-1];
				int top = array[i-1][j];
				int diagonal = array[i-1][j-1];

				left += 2;
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

	public static String getProteinName(String s) {
		int start = s.indexOf("[") + 1;
		int end = s.indexOf("]");
		String name = s.substring(start, end);

		return name;
	}
}