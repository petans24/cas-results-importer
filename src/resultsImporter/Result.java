package resultsImporter;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Result {
	// Disciplína;Fáze;Závodník;Ročník;Oddíl/stát;Pořadí;Výkon;Vítr;Reg.
	// výkon;Reg. vítr;Body;Reakce;PB;SB;Extraliga;

	/*
	 * disciplinaID, fazeID, zavodnikID, poradi, vykon, vitr, regVykon, regVitr,
	 * body, reakce, pb, sb
	 */
	//private String disciplina;// 0
	private int disciplinaID;
	/** disciplinaId int **/
	//private String faze;// 1
	private int fazeID;
	/** fazeId int **/
	private String zavodnikJmeno;// 2
	private String zavodnikPrijmeni;// 2
	private int zavodnikID;
	private String rocnik;// 3
	private Double poradi = null;// 5
	private Double vykon;// 6
	private Double vitr;// 7
	private Double regVykon;// 8
	private Double regVitr;// 9
	private Double body;// 10
	private Double reakce;// 11
	private boolean pb;// 12
	private boolean sb;// 13
	private int zavodID;
	private String poznamka = null;

	private static String dbVysledku = "vysledky";
	private static String tVysledku = "vysledky";
	private static String tFaze = "faze";
	/* private static String tZavod = "zavod"; */
	private static String tZavodnik = "zavodnik";
	private static String tDisciplina = "disciplina";

	public Result(int zavodID, String disciplina, String faze,
			String zavodnikJmeno, String zavodnikPrijmeni, String rocnik,
			String poradi, String vykon, String vitr, String regVykon,
			String regVitr, Double body, Double reakce, String pb, String sb) {
		super();
		//
		//this.disciplina = disciplina;
		try {
			this.disciplinaID = getDisciplinaID(disciplina);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//this.faze = faze;
		try {
			this.fazeID = getFazeID(faze);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.zavodnikJmeno = zavodnikJmeno;
		this.zavodnikPrijmeni = zavodnikPrijmeni;
		try {
			this.zavodnikID = getZavodnikID(zavodnikJmeno, zavodnikPrijmeni,
					rocnik);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.rocnik = rocnik;
		if (!poradi.isEmpty()) {
			if (isNumeric(poradi)) {
				this.poradi = Double.valueOf(poradi);
			} else {
				if (isNumeric(poradi.replace("=", ""))) {
				this.poradi = Double.valueOf(poradi.replace("=", ""));
				} 
				this.poznamka = poradi;
			}
		}
		this.vykon = normalizeVykon(vykon);
		this.regVykon = normalizeVykon(regVykon);
		this.vitr = normalizeVitr(vitr);
		this.regVitr = normalizeVitr(regVitr);
		this.body = body;
		this.reakce = reakce;
		if (pb.length() > 0) this.pb = true; else this.pb = false;
		if (sb.length() > 0) this.sb = true; else this.sb = false;
		this.zavodID = zavodID;
	}

	private int getZavodnikID(String j, String p, String r) throws SQLException {
		String selectQuery = "select id from " + dbVysledku + "." + tZavodnik
				+ " where jmeno='" + j + "' and prijmeni='" + p
				+ "' and rocnik='" + r + "'";
		String insertQuery = "insert into " + dbVysledku + "." + tZavodnik
				+ " VALUES (null,'" + j + "','" + p + "','" + r + "');";
		return getXID(selectQuery, insertQuery);
	}

	private int getDisciplinaID(String nazev) throws SQLException {
		String regexWordCZFC = "([A-ZĚŠČŘŽÝÁÍÉÚŮÓĎŤŇ][a-zěščřžýáíéúůďťň]*\\s*)+$";
		// Create a Pattern object
		Pattern r = Pattern.compile(regexWordCZFC);

		// Now create matcher object.
		Matcher m = r.matcher(nazev);
		String c = null;
		if (m.find()) {
			c = m.group(1);
		}
		String p[] = nazev.split(regexWordCZFC);

		String selectQuery = "select id from " + dbVysledku + "." + tDisciplina
				+ " where nazev='" + p[0].trim() + "' and kategorie = '" + c.trim() + "'";
		String insertQuery = "insert into " + dbVysledku + "." + tDisciplina
				+ " VALUES (null,'" + p[0].trim() + "','" + c.trim() + "');";
		return getXID(selectQuery, insertQuery);
	}

	private int getFazeID(String nazev) throws SQLException {
		String selectQuery = "select id from " + dbVysledku + "." + tFaze
				+ " where nazev='" + nazev + "'";
		String insertQuery = "insert into " + dbVysledku + "." + tFaze
				+ " VALUES (null,'" + nazev + "');";
		return getXID(selectQuery, insertQuery);
	}

	private int getXID(String selectQuery, String insertQuery)
			throws SQLException {
		MySQLConnector conn = new MySQLConnector();
		//System.out.println(selectQuery);
		conn.selectQuery(selectQuery);
		ResultSet rs = conn.getResultSet();
		rs.next();
		int id;
		if (rs.getRow() == 0) {
			conn.executeQuery(insertQuery);
			conn.closeConn();
			conn = new MySQLConnector();
			conn.selectQuery(selectQuery);
			rs = conn.getResultSet();
			rs.next();
		}
		id = rs.getInt("id");
		conn.closeConn();
		return id;
	}

	public void insertToDB() {
		MySQLConnector conn = new MySQLConnector();
		String pq = "insert into " + dbVysledku + "." + tVysledku
				+ " VALUES (null,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try {
		PreparedStatement preparedStmt = conn.preparedQuery(pq);
		preparedStmt.setInt(1, this.zavodID);
		preparedStmt.setInt(2, this.disciplinaID);
		preparedStmt.setInt(3, this.fazeID);
		preparedStmt.setInt(4, this.zavodnikID);
		
		
		if(this.poradi == null) preparedStmt.setString(5, null); else preparedStmt.setBigDecimal(5, new BigDecimal(this.poradi));
		if(this.vykon == null) preparedStmt.setString(6, null); else preparedStmt.setBigDecimal(6, new BigDecimal(this.vykon));
		if(this.vitr == null) preparedStmt.setString(7, null); else preparedStmt.setBigDecimal(7, new BigDecimal(this.vitr));
		if(this.regVykon == null) preparedStmt.setString(8, null); else preparedStmt.setBigDecimal(8, new BigDecimal(this.regVykon));
		if(this.regVitr == null) preparedStmt.setString(9, null); else preparedStmt.setBigDecimal(9, new BigDecimal(this.regVitr));
		if(this.body == null) preparedStmt.setInt(10, 0); else preparedStmt.setBigDecimal(10, new BigDecimal(this.body));
		if(this.reakce == null) preparedStmt.setString(11, null); else preparedStmt.setBigDecimal(11, new BigDecimal(this.reakce));
		preparedStmt.setBoolean(12, this.pb);
		preparedStmt.setBoolean(13, this.sb);
		preparedStmt.setString(14, this.poznamka);
		/*
		 * disciplinaID, fazeID, zavodnikID, poradi, vykon, vitr, regVykon, regVitr,
		 * body, reakce, pb, sb, poznamka
		 */
		preparedStmt.execute();   
	    conn.closeConn();
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Double timeInSeconds(String r) {
		if (r.length() > 0) {
			String[] parts = r.replace(",", ".").split(":");
			int size = parts.length;
			Double re = 0.0;
			for (int i = 0; i + 1 < size; i++) {
				re = Double.parseDouble(parts[i]) * 60;
			};
			return re + Double.parseDouble(parts[size - 1]);
		} else {
			return null;
		}
	}

	public String getZavodnikJmeno() {
		return zavodnikJmeno;
	}

	public String getZavodnikPrijmeni() {
		return zavodnikPrijmeni;
	}

	public String getRocnik() {
		return rocnik;
	}
	
	public static boolean isNumeric(String str)
	{
	  Pattern r = Pattern.compile("^-?(\\d)+(:(\\d)+)?(\\.\\d+)?$");//match a number with optional '-' and decimal.
	  Matcher m = r.matcher(str);
	  return m.find();  
	}
	
	private Double normalizeVykon(String v) {
		v = v.replace(",",".");
		if (isNumeric(v)) {
			return timeInSeconds(v);
		} else {
			if (!v.isEmpty()) { 
				this.poznamka = v;
			
			}
			return null;
		}
	}
	
	private Double normalizeVitr(String s) {
		if (!s.isEmpty() && isNumeric(s)) {
		return  Double.parseDouble(s.replace("m", "").replace(",", "."));
		} return null;
	}
}
