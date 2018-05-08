package ngordnet;
import java.util.HashMap;
import java.util.Map;
import edu.princeton.cs.introcs.In;

public class NGramMap {
    public NGramMap(String wordsFileName, String countsFileName){
        In wFile = new In(wordsFileName);
        In cFile = new In(countsFileName);
        while (wFile.hasNextLine()){
        	String line = wFile.readLine();
        	String[] wordFileInfo = line.split(",");
        	String word = wordFileInfo[0];
        	String year = wordFileInfo[1];
        	int count = Integer.parseInt(wordFileInfo[2]);
        }
    }
    
    //*Provides a defensive copy of the history of the WORD *//
    public TimeSeries<Integer> countHistory(String word){
    	
    }
    
    //*Provides the history of WORD between STARTYEAR and ENDYEAR *//
    public TimeSeries<Integer> countHistory(String word, int startYear, int endYear){
    	
    }
    
    //*Returns the absolute count of WORD in the given YEAR *//
    public int countInYear(String word, int year){
    	
    }
    
    //*Returns a defensive copy of the YearlyRecord in the given YEAR *//
    public YearlyRecord getRecord(int year){
    	
    }
    
    //*Returns the summed relative frequency of all WORDS *//
    public TimeSeries<Double> summedWeightHistory
    
}
