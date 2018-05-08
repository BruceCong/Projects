package ngordnet;


import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.ArrayList;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.introcs.In;

public class WordNet {
    private Digraph d;
    private int count = 0;
    private Map<String, ArrayList<Integer>> synsetStringToInteger = new HashMap<String, ArrayList<Integer>>(1000000);
    private Map<Integer, ArrayList<String>> synsetIntegerToString = new HashMap<Integer, ArrayList<String>>(1000000);
    
    public static void main(String[] args) {
		// TODO Auto-generated method stub
	    WordNet wn = new WordNet("./wordnet/synsets11.txt", "./wordnet/hyponyms11.txt");
        
	        /* These should all print true. */
	        System.out.println(wn.isNoun("jump"));
	        System.out.println(wn.isNoun("leap"));
	        System.out.println(wn.isNoun("nasal_decongestant"));
//
//	        /* The code below should print the following (maybe not in this order): 
//	            All nouns:
//	            augmentation
//	            nasal_decongestant
//	            change
//	            action
//	            actifed
//	            antihistamine
//	            increase
//	            descent
//	            parachuting
//	            leap
//	            demotion
//	            jump
//	        */
//    
	        System.out.println("All nouns:");
	        for (String noun : wn.nouns()) {
	            System.out.println(noun);
	        }

	        /* The code below should print the following (maybe not in this order): 
	            Hypnoyms of increase:
	            augmentation
	            increase
	            leap
	            jump
	        */

	        System.out.println("Hypnoyms of increase:");
	        for (String noun : wn.hyponyms("increase")) {
	            System.out.println(noun);
	        }

	        /* The code below should print the following (maybe not in this order): 
	            Hypnoyms of jump:
	            parachuting
	            leap
	            jump
	        */

	        System.out.println("Hypnoyms of jump:");
	        for (String noun : wn.hyponyms("jump")) {
	            System.out.println(noun);
	        }  

	        /* The code below should print the following (maybe not in this order):
	            Hypnoyms of change:
	            alteration
	            saltation
	            modification
	            change
	            variation
	            increase
	            transition
	            demotion
	            leap
	            jump        
	        */

	        /** From: http://goo.gl/EGLoys */
	        System.out.println("Hypnoyms of change:");

	        WordNet wn2 = new WordNet("./wordnet/synsets14.txt", "./wordnet/hyponyms14.txt");
	        for (String noun : wn2.hyponyms("change")) {
	            System.out.println(noun);
	        }              
	    }    
    
    public WordNet(String synsetFile, String hyponymFile){
        In sFile = new In(synsetFile);
        In hFile = new In(hyponymFile);
        while (sFile.hasNextLine()){
            String line = sFile.readLine();
            String[] synsetInfo = line.split(",");
            int iD = Integer.parseInt(synsetInfo[0]);
            ArrayList<Integer> intValues = new ArrayList<Integer>();
            ArrayList<String> stringValues = new ArrayList<String>();
            intValues.add(iD);
            String key = synsetInfo[1];
            String[] multipleWords = key.split(" ");
            for (String k: multipleWords){
                if (synsetStringToInteger.containsKey(k)){
                    ArrayList<Integer> newList = new ArrayList<Integer>();
                    for (int i: synsetStringToInteger.get(k)){
                        newList.add(i);
                    }
                    newList.add(iD);
                    synsetStringToInteger.put(k, newList);
                }
                else{
                    synsetStringToInteger.put(k, intValues);
                }
            }
            for (String k: multipleWords){
                stringValues.add(k);
            }
            synsetIntegerToString.put(iD, stringValues);
            count += 1;
        }
        d = new Digraph(count);
        while (hFile.hasNextLine()){
            String line = hFile.readLine();
            String[] hyponymInfo = line.split(",", 2);
            int hypernym = Integer.parseInt(hyponymInfo[0]);
            String[] hyponymsArray = hyponymInfo[1].split(",");
            for (String hyponym: hyponymsArray){
                int hyponymId = Integer.parseInt(hyponym);
                d.addEdge(hypernym, hyponymId);
            }
        }
    }
    
    public boolean isNoun(String noun){
        if (noun == null){
            return false;
        }
        else{
            if (nouns() == null){
                return false;
            }
            else{
                return nouns().contains(noun);
            }
        }
    }
	
    public Set<String> nouns(){
        if (synsetStringToInteger == null){
            return null;
        }
        else{
            return synsetStringToInteger.keySet();
        }		
    }
    
    public Set<String> hyponyms(String word){
        Set<String> hyp = new HashSet<String>();
        ArrayList<Integer> synsetId = synsetStringToInteger.get(word);
        Set<Integer> synsetIds = new TreeSet<Integer>();
        for (int i : synsetId){
            synsetIds.add(i);
        }
        Set<Integer> gh = GraphHelper.descendants(d, synsetIds);
        for (int i : gh){
            for (String g : synsetIntegerToString.get(i)){
                hyp.add(g);
            }
        }
        return hyp;
    }
    
}
