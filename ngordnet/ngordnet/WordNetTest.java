package ngordnet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;


public class WordNetTest {
	@Test
	public void testConstructor(){
		WordNet wn = new WordNet("./wordnet/synsets11.txt", "./wordnet/hyponyms11.txt");
		assertTrue(wn.isNoun("jump"));
		assertFalse(wn.isNoun("punched"));
		assertTrue(wn.nouns().contains("jump"));
		assertEquals(12, wn.nouns().size());
		Set<String> actionHyponyms = wn.hyponyms("action");
		assertTrue(actionHyponyms.contains("action"));
		assertTrue(actionHyponyms.contains("change"));
		assertTrue(actionHyponyms.contains("demotion"));
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		jh61b.junit.textui.runClasses(WordNetTest.class);
	}

}
