package randomizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Maintains a map that maps ArrayLists to the name given to them by the user. Also handles the creation
 * of said map entries, and the serialization to and from a file to save and load.
 * @author Raymond Gillies
 */
public class RandomPool {

	private final String SERIAL_FILE_NAME = "randomTables.ser";
	private HashMap<String, ArrayList<String>> tables = new HashMap<>();
	private StringBuilder builder;
	private Log log;
	
	RandomPool(Log log) {
		this.log = log;
	}
	
	/**
	 * Attempts to create a new RandomTable entry and add it to the list of tables in Randomizer.
	 * Designed to take input from a JTextArea; decides when to add a new line by the presence
	 * of a newline character. Ignores blank lines
	 * @param tableText the text to be turned into a RandomTable
	 * @param tableName a String representing the name of the table
	 */
	public void newTableFromString(String tableText, String tableName) {
		String temp = ""; 
		ArrayList<String> newTable = new ArrayList<>();
		
		for(int i = 0; i < tableText.length(); i++) {
			if(tableText.charAt(i) != '\n') {
				temp += tableText.charAt(i);
			} else {
				newTable.add(temp);
				temp = "";
			}
			// Add last line to table, even if newline is not present
			if(i == tableText.length() - 1 && tableText.charAt(i) != '\n') {
				newTable.add(temp);
				temp = "";
			}
		}
		
		tables.put(tableName , newTable);
	}
	
	/**
	 * Attempts to serialize the current tables HashMap
	 * @author Raymond Gillies
	 * @return true if successfully serialized, false otherwise
	 */
	public boolean save() {
		/*
		 * Attempt to write to .ser file. If it isn't there, try to make a new file.
		 * If that doesn't work, print the stack trace and return false.
		 * (False == failure to save in this case)
		 * I'm not sure when IOException would be thrown in this case (outer try), so
		 * let's just print and return false
		 */
		try(ObjectOutputStream objOut = new ObjectOutputStream(
				new FileOutputStream(SERIAL_FILE_NAME));) {
			objOut.writeObject(tables);
		} catch (FileNotFoundException e) {
			try {
				File newFile = new File(SERIAL_FILE_NAME);
				newFile.createNewFile();
			} catch (IOException e1) {
				if(log.isEnabled()) e1.printStackTrace(log.getWriter());
				return false;
			}
		} catch (IOException e) {
			if(log.isEnabled()) e.printStackTrace(log.getWriter());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Attempts to deserialize the tables HashMap from the file SERIAL_FILE_NAME
	 * @return true if successfully deserialized, false otherwise
	 * @author Raymond Gillies
	 */
	@SuppressWarnings("unchecked")
	public boolean load() {
		try(ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(SERIAL_FILE_NAME));) {
			tables = (HashMap<String, ArrayList<String>>) objIn.readObject();
		} catch (FileNotFoundException e) {
			File newFile = new File(SERIAL_FILE_NAME);
			try {
				newFile.createNewFile();
			} catch (IOException e1) {
				if(log.isEnabled()) e1.printStackTrace(log.getWriter());
			}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			// There's a serious problem if this is ever thrown...
			if(log.isEnabled()) e.printStackTrace(log.getWriter());
			return false;
		}
		return false;
	}
	
	/***
	 * Returns the text of the passed table as a string with each element
	 * delimited as a newline (\n)
	 * @author Raymond Gillies
	 * @param table the Collection whose elements should be converted to a string
	 * @return a newline-delimited string representation of the passed RandomTable's elements
	 */
	public String textFromTable(Collection<String> table) {
		builder = new StringBuilder();
		for(String str : table) {
			builder.append(str + '\n');
		}
	
		return builder.toString();
	}
	
	/**
	 * Checks to see if the table name entered already exists.
	 * @param name table name to search for
	 * @return true if name already exists, false otherwise
	 */
	public boolean has(String name) {
		if(name == null) return false;
		return tables.containsKey(name);
	}
	
	/**
	 * Removes the table at the selected index
	 * @param index index of table to be deleted
	 */
	public void deleteTable(String name) {
		tables.remove(name);
	}
	
	/**
	 * Completely clears the list of tables.
	 * Mostly used for debugging purposes
	 * @author Raymond Gillies
	 */
	public void clear() {
		tables.clear();
	}
	
	/**
	 * Returns an array of Strings containing the names of each stored table
	 * @return an array of stored table names
	 */
	public String[] getTableNames() {
		int index = 0;
		String[] names = new String[tables.size()];
		for(String str : tables.keySet()) {
			names[index++] = str;
		}
		return names;
	}
	
	/**
	 * Checks if there are no tables available for the user to edit / choose from
	 * @return true if there are no tables, false otherwise
	 */
	public boolean isEmpty() {
		if(tables.isEmpty()) return true;
		else return false;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> getTable(String name) {
		return (ArrayList<String>)tables.get(name).clone();
	}
}
