package gov.epa.exp_data_gathering.parse.ECOTOX;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EcotoxDbCreator implements AutoCloseable {
	
	private String ecotoxAsciiFolderPath;
	private Connection conn;
	
	private static final int INSERT_BATCH_SIZE = 1000; // Modify as needed for performance
	private static final FilenameFilter ASCII_TABLE_FILENAME_FILTER = new FilenameFilter() {
	    @Override
	    public boolean accept(File folder, String name) {
	        return name.endsWith("txt") && !name.startsWith("release_notes");
	    }
	};
	
	public EcotoxDbCreator(String ecotoxAsciiFolderPath) {
		this.ecotoxAsciiFolderPath = ecotoxAsciiFolderPath;
		
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			File file = new File(ecotoxAsciiFolderPath + ".db");
			file.createNewFile();
			
			String url = "jdbc:sqlite:" + ecotoxAsciiFolderPath + ".db";
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void create() {
		List<File> asciiTableFiles = getAsciiTableFiles();
		for (File file:asciiTableFiles) {
			createAndPopulateTableFromAsciiTableFile(file);
		}
	}

	private List<File> getAsciiTableFiles() {
		File ecotoxAsciiFolder = new File(ecotoxAsciiFolderPath);
		if (!ecotoxAsciiFolder.exists()) {
			System.out.println("Could not find ECOTOX ASCII folder at " + ecotoxAsciiFolderPath);
			return null;
		}
		
		File ecotoxAsciiValidationFolder = new File(ecotoxAsciiFolderPath + "/validation");
		if (!ecotoxAsciiValidationFolder.exists()) {
			System.out.println("Could not find ECOTOX ASCII validation folder at " + ecotoxAsciiFolderPath + "/validation");
			return null;
		}
		
		List<File> asciiTableFiles = new ArrayList<File>();
		asciiTableFiles.addAll(Arrays.asList(ecotoxAsciiFolder.listFiles(ASCII_TABLE_FILENAME_FILTER)));
		asciiTableFiles.addAll(Arrays.asList(ecotoxAsciiValidationFolder.listFiles(ASCII_TABLE_FILENAME_FILTER)));
		
		return asciiTableFiles;
	}
	
	private void createAndPopulateTableFromAsciiTableFile(File asciiTableFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(asciiTableFile))) {
			String header = br.readLine();
			
			String tableName = asciiTableFile.getName().replaceAll("\\.txt", "");
			if (tableName.equals("references")) {
				tableName = "references_"; // "references" is reserved in SQL
			}
			
			String[] headers = header.replaceAll(" ","").split("\\|");
			int len = headers.length;
			
			System.out.println("Creating table " + tableName + "...");
			String[] dataTypes = createTable(tableName, headers);
			
			System.out.println("\tPopulating table " + tableName + "...");
			try {
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			try (Statement stat = conn.createStatement()) {
				String line = null;
				int count = 0;
				while ((line = br.readLine())!=null) {
					String[] values = line.split("\\|");
					
					StringBuilder sb = new StringBuilder("INSERT INTO " + tableName + " (");
					sb.append(String.join(", ", headers) + ") VALUES (");
					
					for (int i = 0; i < len; i++) {
						String dataType = dataTypes[i];
						String value = values[i];
						
						if (value.equals("None")) {
							sb.append("NULL");
						} else if (dataType.startsWith("INTEGER") || dataType.startsWith("REAL")) {
							// SQLite doesn't like comma thousands separators
							value = value.replaceAll(",", "");
							if (!value.matches("[0-9\\.,-]+")) {
								// Ignore "NA", "NR", "NC", etc.
								sb.append("NULL");
							} else if (value.matches("\\.[0-9]+")) {
								// Fix bare decimals (e.g. .09)
								sb.append("0" + value);
							} else if (value.matches("0\\.0\\.[0-9]+")) {
								// There is one single stupid typo in row ~600,000 of 'doses' that this fixes
								// Ask me how long it took to figure that out
								sb.append(value.replaceFirst("0\\.", ""));
							} else {
								sb.append(value);
							}
						} else {
							sb.append("'" + value.replaceAll("'", "''") + "'");
						}
						
						if (i < len - 1) {
							sb.append(", ");
						} else {
							sb.append(");");
						}
					}
					
					stat.addBatch(sb.toString());
					if (++count % INSERT_BATCH_SIZE==0) {
						stat.executeBatch();
						stat.clearBatch();
					}
				}
				
				// Execute and clear out the remaining statements
				stat.executeBatch(); 
				stat.clearBatch(); 
				
				conn.setAutoCommit(true);
				System.out.println("\tFinished table " + tableName + "!");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String[] createTable(String tableName, String[] headers) {
		doSql("DROP TABLE IF EXISTS " + tableName + ";");
		int len = headers.length;
		String[] dataTypes = new String[len];
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
		for (int i = 0; i < len; i++) {
			String header = headers[i];
			String headerEndsWith = header.contains("_") ? header.substring(header.lastIndexOf("_") + 1) : header;
			String dataType = null;
			if (i==0 && tableName.equals("species_synonyms")) {
				// Species number in 'species_synonyms' is not unique
				dataType = "INTEGER NOT NULL";
			} else if (i==0 && (headerEndsWith.equals("id") || headerEndsWith.equals("number"))) {
				dataType = "INTEGER PRIMARY KEY NOT NULL";
			} else if (i==0 && headerEndsWith.equals("code")) {
				dataType = "TEXT PRIMARY KEY NOT NULL";
			} else if (headerEndsWith.equals("id") || headerEndsWith.equals("number") || headerEndsWith.equals("cas")) {
				dataType = "INTEGER";
			} else if (headerEndsWith.equals("value")
					|| headerEndsWith.equals("min")
					|| headerEndsWith.equals("max")
					|| headerEndsWith.equals("mean")) {
				dataType = "REAL";
			} else {
				dataType = "TEXT";
			}
			
			sb.append(header + " " + dataType);
			dataTypes[i] = dataType;
			
			if (i < len - 1) {
				sb.append(", ");
			} else {
				sb.append(");");
			}
		}
		
		doSql(sb.toString());
		return dataTypes;
	}
	
	private void doSql(String sql) {
		try (Statement stat = conn.createStatement()) {
			stat.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			if (!conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String ECOTOX_ASCII_FOLDER_PATH = "data/experimental/ECOTOX/ecotox_ascii_12_15_2021";
		try (EcotoxDbCreator edbc = new EcotoxDbCreator(ECOTOX_ASCII_FOLDER_PATH)) {
			edbc.create();
		}
	}

}
