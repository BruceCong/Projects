package ngordnet;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.IllegalArgumentException;
public class TestTimeSeries {
	
	@Test
	public void testConstructor(){
        TimeSeries<Double> ts = new TimeSeries<Double>();
        ts.put(1991, 2.5);
        ts.put(1992, 3.4);
        ts.put(1993, 5.0);
        System.out.println(ts.years());
        System.out.println(ts.data());
        TimeSeries<Double> ts2 = new TimeSeries<Double>(ts);
        System.out.println(ts2.years());
        System.out.println(ts2.data());
        TimeSeries<Double> ts3 = new TimeSeries<Double>(ts, 1992, 1993);
        System.out.println(ts3.years());
        System.out.println(ts3.data());
	}
	@Test
	public void testSum(){
		TimeSeries<Double> ts = new TimeSeries<Double>();
		ts.put(1991, 2.5);
		ts.put(1992, 3.4);
        ts.put(1993, 5.0);
        TimeSeries<Double> ts2 = new TimeSeries<Double>();
        ts2.put(1991, 3.0);
        ts2.put(1992, -5.0);
        ts2.put(1993, 6.0);
        TimeSeries<Double> sum = ts.plus(ts2);
        assertEquals(5.5, sum.get(1991), 0.01);
        assertEquals(-1.6, sum.get(1992), 0.01);
        assertEquals(11.0, sum.get(1993), 0.01);
	}
	
	
	@Test
	public void testDivision(){
		TimeSeries<Double> ts = new TimeSeries<Double>();
		ts.put(1992, 5.0);
        ts.put(1993, 12.0);
        TimeSeries<Double> ts2 = new TimeSeries<Double>();
        ts2.put(1991, 3.0);
        ts2.put(1992, -5.0);
        ts2.put(1993, 6.0);
        TimeSeries<Double> quotient = ts.dividedBy(ts2);
        assertEquals(0, quotient.get(1991), 0.01);
        assertEquals(-1.0, quotient.get(1992), 0.01);
        assertEquals(2.0, quotient.get(1993), 0.01);
        TimeSeries<Double> ts3 = new TimeSeries<Double>();
        ts3.put(1991, 5.0);
        ts3.put(1992, 2.0);
        ts3.put(1993, 3.0);
        TimeSeries<Double> ts4 = new TimeSeries<Double>();
        ts4.put(1995, 4.0);
        ts4.put(1996, 2.0);
        boolean foundError = false;
        try{
        	ts3.dividedBy(ts4);
        }
        catch (IllegalArgumentException e){
            foundError = true;
        }
        assertTrue(foundError);
	}
    
    public static void main(String[] args) {
		// TODO Auto-generated method stub
        jh61b.junit.textui.runClasses(TestTimeSeries.class);
    }
}
