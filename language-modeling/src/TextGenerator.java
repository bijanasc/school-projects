import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


public class TextGenerator {

	public static void main(String[] args) {
		
		//stores K from command line
		int k = Integer.parseInt(args[0]);
		
		//stores M from command line
	    int m = Integer.parseInt(args[1]);
		
		Scanner input = new Scanner(System.in);
		
		//System.out.println("Please enter a string");
		String source = input.nextLine();
		
		//reads next line until EOF
		while (input.hasNextLine()) {
			
			source += input.nextLine();
			
		}
		
		//closes input
		input.close();
		
		
		Map<String, Markov> map = new TreeMap<String, Markov>();
		
		//create Markov objects from input
		for (int i = 0; i < (source.length() - k); i++) {
			
			//System.out.println(source.substring(i, i+k));
			
			String sample = source.substring(i, i+k);
			
			if(map.containsKey(sample)) {
				
				//System.out.println(sample);
				
				map.get(sample).add(source.charAt(i + k));
				
			}
			else {
				
				map.put(sample, new Markov(sample));
				map.get(sample).add(source.charAt(i + k));
				
			}
			
		}
		
		//System.out.println(map.size() + " distinct keys");
		if(map.size() < 100) {
			
			for(Markov mar: map.values()) {
			
				System.out.println(mar);
			
			}
		
		}
		
		
		String stringK = source.substring(0, k);
		//prints first k character from input string
		System.out.print(stringK);
		
		//prints out random character from order K Markov objects
		for(int i = 0; i < m - 2; i++) {
			String tempString = "";
			char ran = 0;
			while(!map.containsKey(tempString)) {
				ran = map.get(stringK).random();
				tempString = stringK.substring(1) + ran;
			}
			
			stringK = tempString;
			System.out.print(ran);
		}
	}

}
