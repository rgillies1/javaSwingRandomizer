package randomizer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Handles randomizing of RandomTable objects.
 * @author Raymond Gillies
 */
public class Randomizer {
	
	/**
	 * Generates a random output from the randomTables ArrayList. If unified is true, the output will be selected from all tables
	 * in the ArrayList. If unified is false, each RandomTable will generate an output, and this method will return them all combined.
	 * This method creates a clone of each table, and does not modify the table passed.
	 * @param randomTables The ArrayList of RandomTables that should be drawn from
	 * @param unified Indicates whether the output should be drawn from all tables (if true) or a unique one should be pulled from each table (if false)
	 * @return A raw String[] array that contains the result of randomizing
	 */
	public String[] randomize(ArrayList<RandomTable> randomTables, boolean unified) {
		// Random seed is system time
		Random rand = new Random(System.currentTimeMillis());
		String[] result;

		ArrayList<RandomTable> toBeRandomized = new ArrayList<>();
		for(RandomTable randTable : randomTables) {
			toBeRandomized.add((RandomTable)randTable.clone());
		}
		int randIndex;
		if(toBeRandomized.isEmpty()) {
			return null;
		}
		if(unified) {
			int numOutputs = toBeRandomized.get(0).getNumOutputs();
			boolean allowRepeats = toBeRandomized.get(0).canRepeat();
			result = new String[numOutputs];
			for(int i = 0; i < numOutputs; i++) {
				randIndex = rand.nextInt(toBeRandomized.size()); // Randomly select a table
				RandomTable randTable = toBeRandomized.get(randIndex);
				randIndex = rand.nextInt(randTable.size());
				result[i] = randTable.get(randIndex);
				if(!allowRepeats) randTable.remove(randIndex);
			}
		} else {
			ArrayList<String> resultsAsList = new ArrayList<>();
			for(RandomTable randTable : toBeRandomized) {
				for(int i = 0; i < randTable.getNumOutputs(); i++) {
					randIndex = rand.nextInt(randTable.size());
					resultsAsList.add(randTable.get(randIndex));
					if(!randTable.canRepeat()) randTable.remove(randIndex);
				}
			}
			result = resultsAsList.toArray(new String[resultsAsList.size()]);
		}
		return result;
	}
}
