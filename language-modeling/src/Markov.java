import java.util.Map;
import java.util.TreeMap;


public class Markov {
	
	String substring;
	int count = 0;
	Map<Character, Integer> suffix = new TreeMap<Character, Integer>();
	
	
	//constructs Markov object
	public Markov(String substring) { 
		
		this.substring = substring;
		
	}
	
	//calculates random character using suffixes
	public char random() {
		
		double randomNum = Math.random()*count;
		
		for (Character k: suffix.keySet()) {
			
			randomNum -= suffix.get(k);
			
			if (randomNum <= 0) {
				
				return k;
				
			}
			
		}
				
		return '~';	
	}
	
	//stores suffixes and their counts
	public void add(char c) {  
		
		if (suffix.containsKey(c)) {

			int count = suffix.get(c) + 1;
			suffix.put(c, count);			
		}
		
		else {
			
			suffix.put(c, 1);						
		}	
		
		count++;		
	}
	
	//prints suffixes and their counts along with the preceding substring
	public String toString() {  
		
		String suffixString = "";
		
		for (Character k: suffix.keySet()) {
			
			suffixString += suffix.get(k) + " " + k + " ";
			
		}
		
		return count + " " + substring + ": " + suffixString;
		
	}

}
