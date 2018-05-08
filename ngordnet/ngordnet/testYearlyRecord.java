package ngordnet;


import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class testYearlyRecord {
	
	@Test
	public void testConstructor(){
		YearlyRecord yr = new YearlyRecord();
		ArrayList<Number> emptyNumbers = new ArrayList<Number>();
		ArrayList<String> emptyWords = new ArrayList<String>();
		assertEquals(emptyNumbers, yr.counts());
		assertEquals(emptyWords, yr.words());
		HashMap<String, Integer> strInt = new HashMap<String, Integer>();
		strInt.put("one", 1);
		strInt.put("two", 2);
		YearlyRecord yr2 = new YearlyRecord(strInt);
		emptyNumbers.add(1);
		emptyNumbers.add(2);
		emptyWords.add("one");
		emptyWords.add("two");
		assertEquals(emptyNumbers, yr2.counts());
		assertEquals(emptyWords, yr2.words());
	}
		
	@Test
	public void testPut(){
		YearlyRecord yr = new YearlyRecord();
		yr.put("Hi", 10000);
		yr.put("Sick!", 10);
		ArrayList<Number> fullNumbers = new ArrayList<Number>();
		ArrayList<String> fullWords = new ArrayList<String>();
		fullWords.add("Sick!");
		fullWords.add("Hi");
		fullNumbers.add(1);
		fullNumbers.add(2);
		assertEquals(fullWords ,yr.words());
	}
	
	@Test
	public void testRank(){
		YearlyRecord yr = new YearlyRecord();
		yr.put("Hi", 10000);
		yr.put("Sick!", 10);
		assertEquals(1, yr.rank("Hi"));
		assertEquals(2, yr.rank("Sick!"));
		yr.put("WHAT", 100);
		assertEquals(2, yr.rank("WHAT"));
		assertEquals(3, yr.rank("Sick!"));
		assertEquals(1, yr.rank("Hi"));
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		jh61b.junit.textui.runClasses(testYearlyRecord.class);
	}

}
