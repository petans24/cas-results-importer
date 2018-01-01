package resultsImporter;

import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSReader {

	private static List<Result> results = new ArrayList<Result>();
	private static int nColumns = 0;
	private static String mFile = "vysledky.xlsx"; 
	private static int zavodID = 4;
	
	public static void main(String[] args) {
		getZavody();
		try {
			if (args.length != 0) {mFile = args[0];
				zavodID = Integer.valueOf(args[1]);
			}
			FileInputStream file = new FileInputStream(new File(mFile));
			

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();

			ArrayList<String> rowOfResults;
			boolean header = true;
			
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();

				rowOfResults = new ArrayList<String>();

				//getting number of columns by header size
				if (header) {
					while (cellIterator.hasNext()) {
						cellIterator.next();
						nColumns++;
					}
					header = false;
					System.out.println(nColumns);
				}
				else {
				//getting results by size of 
					for (int cn = 0; cn < nColumns; cn++) {
				          Cell c = row.getCell(cn, Row.RETURN_BLANK_AS_NULL);
				          if (c == null) {
				        	  rowOfResults.add("");
				          } else {
				          switch (c.getCellType())
		                    {
		                        case Cell.CELL_TYPE_NUMERIC:
		                           // System.out.print(c.getNumericCellValue() + "t");
		                            rowOfResults.add(String.valueOf(c.getNumericCellValue()));
		                            break;
		                        case Cell.CELL_TYPE_STRING:
		                           // System.out.print(c.getStringCellValue() + "t");
		                            rowOfResults.add(c.getStringCellValue());
		                            break;
		                    }
				          }
					}
				}
				if (rowOfResults.contains("AC TJ Jičín")) {
					String regexWordCZ = "[a-zA-ZěščřžýáíéúůďťňĚŠČŘŽÝÁÍÉÚŮÓĎŤŇ]";
					/*
					 * String pattern = (""+ regexWordCZ + "+ "+ regexWordCZ
					 * +"+");
					 */
					// System.out.println(pattern);

					String line = rowOfResults.get(2);
					String pattern = "^" + regexWordCZ + "+ " + regexWordCZ
							+ "+$";

					// Create a Pattern object
					Pattern r = Pattern.compile(pattern);

					// Now create matcher object.
					Matcher m = r.matcher(line);
					if (m.find()) {
						/*System.out.println(rowOfResults.get(2));*/
						createResult(rowOfResults);
					} /*else {
						System.out.println(rowOfResults.get(2) + "nope");
					}*/
				}
			}
			workbook.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		insertResultsToDB();
	}

	private static void createResult(ArrayList<String> r) {
		String[] name = r.get(2).split("\\s");
		results.add(new Result(zavodID,r.get(0), r.get(1), name[1], name[0], r.get(3),
				r.get(5), r.get(6), r.get(7),
				r.get(8), r.get(9), getDouble(r.get(10)),
				getDouble(r.get(11)), r.get(12), r.get(13)));
	}

	/*
	 * private static int getInt(String s) { return
	 * Integer.valueOf(s.replace(".", ",")); }
	 */

	@SuppressWarnings("finally")
	private static Double getDouble(String s) {
		Double d = null;
		s = s.replace("?", "");
		if (s.length() != 0) {
			try {
				d = Double.parseDouble(s);
			} catch (Exception e) {
				System.out.println(s);
				e.printStackTrace();
				d = null;
			} finally {
				return d;
			}
		} else
			return null;
	}
	
	private static void insertResultsToDB() {
		for (Result result : results) {
			result.insertToDB();
		}
		
		
	}
	
	private static void getZavody() {
		MySQLConnector conn = new MySQLConnector();
		try {
		conn.selectQuery("select id, misto, datum from zavod order by datum desc;");
		ResultSet rs = conn.getResultSet();
			while (rs.next()) {
				System.out.println(rs.getString("misto")+" "+rs.getString("datum"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}