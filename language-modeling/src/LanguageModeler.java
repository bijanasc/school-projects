import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


public class LanguageModeler {

	public static void main(String[] args) {
		
		//stores K from command line
		int k = Integer.parseInt(args[0]);
		
		Scanner input = new Scanner(System.in);
		
		//System.out.println("Please enter a string");
		String source = input.nextLine();
		
		//close input
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
		
		//prints out Markov objects with their counts
		//prints corresponding suffixes with their counts
		System.out.println(map.size() + " distinct keys");
		
		for(Markov m: map.values()) {
			
			System.out.println(m );
			
		}
		
	}

}
