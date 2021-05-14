package randomizer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Maintains an ArrayList of Strings, along with other information needed in order to be
 * randomized.
 * @author Raymond Gillies
 */
public class RandomTable extends ArrayList<String> 
							implements Serializable {
	
	private static final long serialVersionUID = 9116264912910319759L;
	private String tableName;
	private int numOutputs;
	private boolean canRepeat;

	@SuppressWarnings("unchecked")
	public RandomTable(ArrayList<String> from, String tableName, int numOutputs, boolean canRepeat) {
		this.addAll((ArrayList<String>)from.clone());
		this.tableName = tableName;
		this.numOutputs = numOutputs;
		this.canRepeat = canRepeat;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getNumOutputs() {
		return numOutputs;
	}

	public void setNumOutputs(int numOutputs) {
		this.numOutputs = numOutputs;
	}

	public boolean canRepeat() {
		return canRepeat;
	}

	public void setCanRepeat(boolean canRepeat) {
		this.canRepeat = canRepeat;
	}
	
}
