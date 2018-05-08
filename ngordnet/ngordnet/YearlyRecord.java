package ngordnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;


public class YearlyRecord{
	
	private Map<String, Integer> stringToInteger = new HashMap<String, Integer>();
		
	private Map<String, Integer> wordToRank = new HashMap<String, Integer>();
	
	private boolean changeRank = true;
	
	private ArrayList<String> words = new ArrayList<String>();
	
	private ArrayList<Number> count = new ArrayList<Number>();
	
	
	
    public YearlyRecord(){
    	
    }
    
    public YearlyRecord(HashMap<String, Integer> otherCountMap){
        stringToInteger = otherCountMap;
        wordToRank = new HashMap<String, Integer>(otherCountMap);
        changeRank = false;
    }
    
    public int count(String word){
    	return stringToInteger.get(word);
    }
    
    public Collection<Number> counts(){
    	if (changeRank){
            return count;
    	}
    	else{
    		count.clear();
    		words.clear();
    		ArrayList<String> allKeys = new ArrayList<String>();
        	for (String keys : stringToInteger.keySet()){
        	    allKeys.add(keys);
        	}
    		Collections.sort(allKeys, wordCompare);
    		int rank = allKeys.size();
    		for (String word: allKeys){
    			wordToRank.put(word, rank);
    			rank -= 1;
    			words.add(word);
    			count.add(stringToInteger.get(word));
    		}
    		changeRank = true;
    	}
    	return count;
    }
   
    
    public void put(String word, int count){
    	changeRank = false;
    	stringToInteger.put(word, count);
    }
    
    public int rank(String word){
    	ArrayList<String> allKeys = new ArrayList<String>();
    	for (String keys : stringToInteger.keySet()){
    	    allKeys.add(keys);
    	}
    	Collections.sort(allKeys, wordCompare);
		int rank = allKeys.size();
		for (String key: allKeys){
			wordToRank.put(key, rank);
			rank -= 1;
		}
    	return wordToRank.get(word);
    }
    
    public int size(){
    	return stringToInteger.size();
    }
    
    private Comparator<String> wordCompare = new Comparator<String>(){
    	public int compare(String word1, String word2){
    		int value1 = stringToInteger.get(word1);
    		int value2 = stringToInteger.get(word2);
    		if (value1 < value2){
    			return -1;
    		} else if (value1 == value2){
    			return 0;
    		}
    		else{
    			return 1;
    		}
    	}
    };
    
    public Collection<String> words(){
    	if (changeRank){
            return words;
    	}
    	else{
    		ArrayList<String> allKeys = new ArrayList<String>();
    		words.clear();
    		count.clear();
        	for (String key : stringToInteger.keySet()){
        		allKeys.add(key);
        	}
    		Collections.sort(allKeys, wordCompare);
    		int rank = words.size();
    		for (String word: allKeys){
    			wordToRank.put(word , rank);
    			words.add(word);
    			count.add(stringToInteger.get(word));
    			rank -= 1;
    		}
    		changeRank = true;
    	}
    	return words;
    }
}
