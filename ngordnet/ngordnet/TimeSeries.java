package ngordnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.lang.IllegalArgumentException;


public class TimeSeries<T extends Number> extends TreeMap<Integer, T> {
    public TimeSeries(){
    }
	
    public TimeSeries(TimeSeries<T> ts){
        super(ts);
    }
	
    public TimeSeries(TimeSeries<T> ts, int startYear, int endYear){
        super(ts);
        while (firstKey() < startYear){
        	remove(firstKey());
        }
        while (lastKey() > endYear){
        	remove(lastKey());
        }
    }
    
    public Collection<Number> years(){
    	Collection<Number> collection = new ArrayList<Number>();
        for (int i :keySet()){
        	collection.add(i);
        }
        return collection;
    }

    public Collection<Number> data(){
        Collection<Number> collection = new ArrayList<Number>();
        for (T t : values()){
            collection.add(t);
        }
        return collection;
    }

	
    public TimeSeries<Double> dividedBy(TimeSeries<? extends Number> ts){
        TimeSeries<Double> quotient = new TimeSeries<Double>();
        for (Number years : years()){
        	if (!ts.years().contains(years)){
        		throw new IllegalArgumentException();
        	}
        	else{
                quotient.put(years.intValue(), get(years).doubleValue()/ts.get(years).doubleValue());	
        	}
        }
        for (Number years : ts.years()){
        	if (!years().contains(years)){
        		quotient.put(years.intValue(), 0.0);
        	}
        }
        return quotient;
    }
    
    
    public TimeSeries<Double> plus(TimeSeries<? extends Number> ts){
    	TimeSeries<Double> sum = new TimeSeries<Double>();
    	for (Number years : years()){
        	if (ts.years().contains(years)){
        	    sum.put(years.intValue(), get(years).doubleValue() + ts.get(years).doubleValue());
        	}
        	else if (!ts.years().contains(years)){
        	    sum.put(years.intValue(), get(years).doubleValue());
        	}
        }
    	for (Number years : ts.years()){
    		if (!years().contains(years)){
    			sum.put(years.intValue(), ts.get(years).doubleValue());
    		}
    	}
    	return sum;
    }

}
