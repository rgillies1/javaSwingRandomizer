package randomizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Receives messages and writes them to a log file
 * @author Raymond Gillies
 *
 */
public class Log {
	
	private static String LOG_FILE_NAME = "log.txt";
	
	File logFile;
	FileWriter logFileWriter;
	PrintWriter logPrintWriter;
	final boolean enableLogging = false;
	
	Log() throws IOException {
		logFile = new File(LOG_FILE_NAME);
		if(logFile.exists()) logFile.delete();
		if(enableLogging) {
			try {
				logFile.createNewFile();
				logFileWriter = new FileWriter(LOG_FILE_NAME);
				logPrintWriter = new PrintWriter(logFileWriter);
			} catch(IOException e) {
				throw new IOException();
			}
		}

	}
	
	/**
	 * If logging is enabled, write the passed object to the log.
	 * @param toWrite
	 */
	public void writeToLog(Object toWrite) {
		if(enableLogging) logPrintWriter.println(toWrite);
	}
	
	/**
	 * Closes the log's write streams and writes the result to the file.
	 */
	public void closeBuffer() {
		if(enableLogging) { // Nothing is initialized if logging is not enabled
			try {
				logPrintWriter.close();
				logFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns this log's FileWriter.
	 * @return This log's FileWriter object.
	 */
	public PrintWriter getWriter() {
		return logPrintWriter;
	}
	
	/**
	 * Returns true if logging is enabled, false otherwise.
	 * @return true if logging is enabled, false otherwise.
	 */
	public boolean isEnabled() {
		return enableLogging;
	}
}
