/**
 * MainFormDbAccessor.java
 *
 * Description : This performs all the database access needed by the MainForm
 * Version     : 1.0
 * @author     : Partha Pratim Sanyal
 * Created on  : Aug 29, 2009
 *
 * Modification History :
 * Date   | Version  |  Author  | Comments
 *
 * Confidentiality Notice :
 * This software is the confidential and,
 * proprietary information of The University of Arizona.
 */
package semanticMarkup.gui;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LogLevel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@SuppressWarnings({ "unused" })
public class MainFormDbAccessor {

	private static final Logger LOGGER = Logger
			.getLogger(MainFormDbAccessor.class);
	private static Connection connection = null;
	private String databaseName;
	private String databasePrefix;
	private String databasePassword;
	private String databaseUser;

	@Inject
	public MainFormDbAccessor(@Named("databaseName")String databaseName, @Named("databaseUser")String databaseUser, 
			@Named("databasePassword")String databasePassword, @Named("databasePrefix")String databasePrefix) {
		this.databaseName = databaseName;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		this.databasePrefix = databasePrefix;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String URL = "jdbc:mysql://localhost/" + this.databaseName + "?user=" + this.databaseUser + "&password=" + 
					this.databasePassword + "&connectTimeout=0&socketTimeout=0&autoReconnect=true";
			connection = DriverManager.getConnection(URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createNonEQTable() {
		// noneqterms table is refreshed for each data collection
		try {
			Statement stmt = connection.createStatement();
			stmt.execute("drop table if exists " + this.databasePrefix
					+ "_noneqterms");
			stmt.execute("create table if not exists "
					+ this.databasePrefix
					+ "_noneqterms (term varchar(100) not null, source varchar(200), savedid varchar(40))");
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createTyposTable() {
		try {
			Statement stmt = connection.createStatement();
			;
			String typotable = this.databasePrefix + "_typos";
			stmt.execute("drop table if exists " + typotable);
			String query = "create table if not exists "
					+ typotable
					+ " (typo varchar(150), correction varchar(150), primary key (typo, correction))";
			stmt.execute(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * change pos for these removedtags to 'b' in wordpos table
	 * 
	 * @param removedTags
	 * @throws ParsingException
	 * @throws SQLException
	 */
	public void changePOStoB(List<String> removedTags) throws Exception {
		// Connection conn = null;
		PreparedStatement stmt = null;
		try {
			// conn = DriverManager.getConnection(url);
			String tablePrefix = MainForm.dataPrefixCombo.getText();
			String sql = "update " + this.databasePrefix + "_wordpos "
					+ "set pos = 'b' where word = ?";
			stmt = connection.prepareStatement(sql);
			for (String tag : removedTags) {
				stmt.setString(1, tag);
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to remove the Bad structure names from Tab4, after
	 * they are marked RED,two steps are taken: First Step: Remove from the
	 * database (update the tag). Step Two: Keep the UI as it is with selected
	 * rows in Red color
	 * 
	 * @param removedTags
	 *            : List of structures that should be removed
	 * @throws ParsingException
	 * @throws SQLException
	 */
	public void setUnknownTags(List<String> removedTags) throws Exception {

		// Connection conn = null;
		PreparedStatement stmt = null;
		try {

			// Class.forName(driverPath);
			// conn = DriverManager.getConnection(url);
			String tablePrefix = MainForm.dataPrefixCombo.getText();
			String sql = "update " + tablePrefix
					+ "_sentence set tag = 'unknown' where tag = ?";
			stmt = connection.prepareStatement(sql);

			for (String tag : removedTags) {
				stmt.setString(1, tag);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void loadTagsData(Combo tagListCombo, Combo modifierListCombo)
			throws Exception {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt_select = null;
		// Connection conn = null;
		try {
			// Class.forName(driverPath);
			// conn = DriverManager.getConnection(url);
			String tablePrefix = MainForm.dataPrefixCombo.getText();
			String sql = "select distinct tag from "
					+ tablePrefix
					+ "_sentence where tag != 'unknown' and tag is not null order by tag asc";
			stmt = connection.prepareStatement(sql);

			rs = stmt.executeQuery();
			while (rs.next()) {
				String tag = rs.getString("tag");
				tagListCombo.add(tag);
			}
			// changed 02/28 added modifier != ''
			sql = "select distinct modifier from "
					+ tablePrefix
					+ "_sentence where modifier is not null and modifier != '' order by modifier asc";
			stmt_select = connection.prepareStatement(sql);
			rs = stmt_select.executeQuery();

			while (rs.next()) {
				String mod = rs.getString("modifier");
				modifierListCombo.add(mod);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (stmt != null) {
				stmt.close();
			}

			if (stmt_select != null) {
				stmt_select.close();
			}
		}
	}

	/**
	 * 
	 * @param tagTable
	 * @return # of records loaded
	 * @throws ParsingException
	 * @throws SQLException
	 */
	public int loadTagsTableData(Table tagTable) throws Exception {

		PreparedStatement stmt = null;
		// Connection conn = null;
		ResultSet rs = null;

		try {
			// Class.forName(driverPath);
			// conn = DriverManager.getConnection(url);
			String tablePrefix = MainForm.dataPrefixCombo.getText();
			String sql = "select * from "
					+ tablePrefix
					+ "_sentence where tag = 'unknown' or isnull(tag) order by sentence";
			stmt = connection.prepareStatement(sql);

			int i = 0;
			rs = stmt.executeQuery();
			while (rs.next()) {
				String sentid = rs.getString("sentid");
				String tag = rs.getString("tag");
				// String sentence = rs.getString("sentence");
				String sentence = rs.getString("originalsent");
				TableItem item = new TableItem(tagTable, SWT.NONE);
				item.setText(new String[] { ++i + "", sentid, "", tag, sentence });
			}
			return i;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (stmt != null) {
				stmt.close();
			}
			return -1;
		}
	}

	public void updateContextData(int sentid, StyledText contextStyledText)
			throws Exception {

		// Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String min = "" + (sentid - 2);
		String max = "" + (sentid + 2);

		try {
			// Class.forName(driverPath);
			// conn = DriverManager.getConnection(url);
			String tablePrefix = MainForm.dataPrefixCombo.getText();
			String sql = "select * from " + tablePrefix
					+ "_sentence where sentid > ? and sentid < ?";
			stmt = connection.prepareStatement(sql);

			stmt.setString(1, min);
			stmt.setString(2, max);

			rs = stmt.executeQuery();
			while (rs.next()) {
				String sid = rs.getString("sentid");
				String tag = rs.getString("tag");
				// String sentence = rs.getString("sentence");
				String sentence = rs.getString("originalsent");
				int start = contextStyledText.getText().length();

				contextStyledText.append(sentence + "\r\n");
				if (Integer.parseInt(sid) == sentid) {
					StyleRange styleRange = new StyleRange();
					styleRange.start = start;
					styleRange.length = sentence.length();
					styleRange.fontStyle = SWT.BOLD;
					// styleRange.foreground =
					// display.getSystemColor(SWT.COLOR_BLUE);
					contextStyledText.setStyleRange(styleRange);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * This is used when Save is clicked on Step5.
	 * 
	 * @param tagTable
	 * @throws ParsingException
	 * @throws SQLException
	 */
	public void saveTagData(Table tagTable) throws Exception {

		// Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt_update = null;
		ResultSet rs = null;

		try {
			// Class.forName(driverPath);
			// conn = DriverManager.getConnection(url);

			for (TableItem item : tagTable.getItems()) {
				String sentid = item.getText(1);
				String modifier = item.getText(2);
				String tag = item.getText(3);

				if (tag.equals("unknown"))
					continue;

				if (tag.equals("PART OF LAST SENTENCE")) {// find tag of the
															// last sentence
					String tablePrefix = MainForm.dataPrefixCombo.getText();
					String sql = "select tag from " + tablePrefix
							+ "_sentence where sentid ="
							+ (Integer.parseInt(sentid) - 1);
					stmt = connection.prepareStatement(sql);
					rs = stmt.executeQuery();
					rs.next();
					tag = rs.getString("tag");
				}
				String tablePrefix = MainForm.dataPrefixCombo.getText();
				String sql = "update "
						+ tablePrefix
						+ "_sentence set modifier = ?, tag = ? where sentid = ?";
				stmt_update = connection.prepareStatement(sql);
				stmt_update.setString(1, modifier);
				stmt_update.setString(2, tag);
				stmt_update.setString(3, sentid);

				stmt_update.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (stmt != null) {
				stmt.close();
			}

			if (stmt_update != null) {
				stmt_update.close();
			}
		}
	}

	// added March 1st
	public void glossaryPrefixRetriever(List<String> datasetPrefixes)
			throws Exception {
		// Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			// conn = DriverManager.getConnection(url);
			stmt = connection.createStatement();
			;
			rset = stmt
					.executeQuery("SELECT table_name FROM information_schema.tables where table_schema ='"
							+ this.databaseName
							+ "' and table_name like '%glossaryfixed'");
			while (rset.next()) {
				datasetPrefixes.add(rset.getString("table_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rset != null) {
				rset.close();
			}

			if (stmt != null) {
				stmt.close();
			}
		}
	}

	// added March 1st ends
	public void datasetPrefixRetriever(List<String> datasetPrefixes)
			throws Exception {

		// Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;

		String createprefixTable = "CREATE TABLE  if not exists datasetprefix ("
				+ "prefix varchar(20) NOT NULL DEFAULT '', "
				+ "time_last_accessed timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
				+ "tab_general varchar(1) DEFAULT NULL, "
				+ "tab_segm varchar(1) DEFAULT NULL, "
				+ "tab_verf varchar(1) DEFAULT NULL, "
				+ "tab_trans varchar(1) DEFAULT NULL, "
				+ "tab_struct varchar(1) DEFAULT NULL, "
				+ "tab_unknown varchar(1) DEFAULT NULL, "
				+ "tab_finalm varchar(1) DEFAULT NULL, "
				+ "tab_gloss varchar(1) DEFAULT NULL, "
				+ "glossary varchar(40) DEFAULT NULL, "
				+ "option_chosen varchar(1) DEFAULT '', "
				+ "PRIMARY KEY (prefix, time_last_accessed) ) ";

		try {
			// conn = DriverManager.getConnection(url);
			stmt = connection.createStatement();
			;
			stmt.execute(createprefixTable);
			rset = stmt
					.executeQuery("select * from datasetprefix order by time_last_accessed desc");
			while (rset.next()) {
				datasetPrefixes.add(rset.getString("prefix"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rset != null) {
				rset.close();
			}

			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public String getLastAccessedDataSet(int option_chosen) throws Exception {

		// Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		String recent = null;

		try {
			// conn = DriverManager.getConnection(url);
			stmt = connection.createStatement();
			;
			rset = stmt
					.executeQuery("select * from datasetprefix where option_chosen= '"
							+ option_chosen
							+ "' order by time_last_accessed desc");
			if (rset.next()) {
				recent = rset.getString("prefix");
				recent = recent.concat("|").concat(rset.getString("glossary"));
				// added by prasad to extract the glossary name along with
				// dataset prefix
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rset != null) {
				rset.close();
			}

			if (stmt != null) {
				stmt.close();
			}

			if (connection != null) {
				connection.close();
			}
		}

		return recent;
	}

	public void saveOtherTerms(HashMap<String, String> otherTerms)
			throws SQLException {

		// Connection conn = null;
		PreparedStatement stmt = null;
		String tablePrefix = MainForm.dataPrefixCombo.getText();
		try {
			// conn = DriverManager.getConnection(url);
			String postable = this.databasePrefix + "_wordpos ";

			stmt = connection.prepareStatement("insert into " + postable
					+ "(word,pos) values (?,?)");
			Set<String> keys = otherTerms.keySet();
			for (String key : keys) {
				try {
					stmt.setString(1, key);
					stmt.setString(2, otherTerms.get(key));
					stmt.execute();
					System.out.println(key + " " + otherTerms.get(key)
							+ " inserted");
				} catch (Exception exe) {
					if (!exe.getMessage().contains("Duplicate entry")) {
						throw exe;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void savePrefixData(String prefix, String glossaryName,
			int optionChosen) throws Exception {
		// Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rset = null;

		try {
			if (!prefix.equals("")) {
				stmt = connection
						.prepareStatement("select prefix from datasetprefix where prefix='"
								+ prefix + "'");
				rset = stmt.executeQuery();
				if (rset.next()) {
					// stmt =
					// connection.prepareStatement("update datasetprefix set time_last_accessed = current_timestamp, tab_general = 1,tab_segm=1,"
					// +
					// "tab_verf =1,tab_trans =1,tab_struct =1,tab_unknown =1,tab_finalm =1,tab_gloss =1,glossary= '"
					// +glossaryName+"',option_chosen='"+optionChosen+"' where prefix='"+prefix+"'");
					stmt = connection
							.prepareStatement("update datasetprefix set time_last_accessed = current_timestamp where prefix='"
									+ prefix + "'"); // keep the status of
														// markup from a
														// previous run
					stmt.executeUpdate();
				} else {
					// stmt =
					// connection.prepareStatement("insert into datasetprefix values ('"+
					// prefix +
					// "', current_timestamp, 1, 1, 1 ,1, 1, 1, 1, 1,'"+glossaryName+"','"+optionChosen+"')");
					stmt = connection
							.prepareStatement("insert into datasetprefix values ('"
									+ prefix
									+ "', current_timestamp, 1, 0, 0 ,0, 0, 0, 0, 0,'"
									+ glossaryName
									+ "','"
									+ optionChosen
									+ "')");
					// changed insert from 0 to 1 by Prasad, since the remaining
					// code is taking 1 as unprocessed
					stmt.executeUpdate();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rset != null) {
				rset.close();
			}

			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void loadStatusOfMarkUp(boolean[] markUpStatus, String dataPrefix)
			throws Exception {

		// Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			if (dataPrefix != null && !dataPrefix.equals("")) {
				// conn = DriverManager.getConnection(url);
				stmt = connection
						.prepareStatement("select * from datasetprefix where prefix ='"
								+ dataPrefix + "'");
				rset = stmt.executeQuery();

				if (rset != null && rset.next()) {

					/* Segmentation tab */
					if (rset.getInt("tab_segm") == 0) {
						markUpStatus[1] = false;
					} else {
						markUpStatus[1] = true;
					}

					/* Verification tab */
					if (rset.getInt("tab_verf") == 0) {
						markUpStatus[2] = false;
					} else {
						markUpStatus[2] = true;
					}

					/* Transformation tab */
					if (rset.getInt("tab_trans") == 0) {
						markUpStatus[3] = false;
					} else {
						markUpStatus[3] = true;
					}

					/* Structure Name Correction tab */
					if (rset.getInt("tab_struct") == 0) {
						markUpStatus[4] = false;
					} else {
						markUpStatus[4] = true;
					}

					/* Unknown removal tab */
					if (rset.getInt("tab_unknown") == 0) {
						markUpStatus[5] = false;
					} else {
						markUpStatus[5] = true;
					}

					/* Finalizer tab */
					if (rset.getInt("tab_finalm") == 0) {
						markUpStatus[6] = false;
					} else {
						markUpStatus[6] = true;
					}

					/* Glossary tab */
					if (rset.getInt("tab_gloss") == 0) {
						markUpStatus[7] = false;
					} else {
						markUpStatus[7] = true;
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (rset != null) {
				rset.close();
			}

			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void saveStatus(String tab, String prefix, boolean status)
			throws SQLException {

		// Connection conn = null;
		PreparedStatement stmt = null;
		int tabStatus = 0;
		// Lookup
		{
			if (tab.equals("General")) {
				tab = "tab_general";
			}
			if (tab.equals("Step 1")) {
				tab = "tab_segm";
			}
			if (tab.equals("Step 2")) {
				tab = "tab_verf";
			}
			if (tab.equals("Step 3")) {
				tab = "tab_trans";
			}
			if (tab.equals("Step 4")) {
				tab = "tab_struct";
			}
			if (tab.equals("Step 5")) {
				tab = "tab_unknown";
			}
			if (tab.equals("Step 7")) {
				tab = "tab_finalm";
			}
			if (tab.equals("Character States")) {
				tab = "tab_gloss";
			}
		}

		if (status == true) {
			tabStatus = 1;
			// tabStatus = 0;//changed to 0 by Prasad. if status is 0 that means
			// processed and can be loaded
			// status of 1 means yet to be loaded
		}

		try {
			// conn = DriverManager.getConnection(url);
			System.out.println(tab);
			System.out.println(tabStatus);
			String query = "update datasetprefix set " + tab + "= "
					+ tabStatus + " where prefix='" + prefix + "'";
			stmt = connection
					.prepareStatement("update datasetprefix set " + tab + "= "
							+ tabStatus + " where prefix='" + prefix + "'");
			stmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void removeDescriptorData(List<String> words) throws SQLException {
		// Connection conn = null;
		PreparedStatement pstmt = null;
		String tablePrefix = MainForm.dataPrefixCombo.getText();
		try {
			// conn = DriverManager.getConnection(url);
			pstmt = connection.prepareStatement("delete from " + tablePrefix
					+ "_wordpos where pos=? and word=?");
			for (String word : words) {
				pstmt.setString(1, "b");
				pstmt.setString(2, word);
				pstmt.addBatch();
			}
			pstmt.executeBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	public ArrayList<String> getUnknownWords() throws SQLException {

		// Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		ArrayList<String> unknownWords = new ArrayList<String>();
		String tablePrefix = MainForm.dataPrefixCombo.getText();
		try {
			// conn = DriverManager.getConnection(url);
			stmt = connection.prepareStatement("select word from "
					+ tablePrefix + "_unknownwords " + "where flag = ?");
			stmt.setString(1, "unknown");
			rset = stmt.executeQuery();
			if (rset != null) {
				while (rset.next()) {
					unknownWords.add(rset.getString("word"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rset != null) {
				rset.close();
			}

			if (stmt != null) {
				stmt.close();
			}
		}
		return unknownWords;
	}

	/**
	 * This function will save terms from the Markup - (Structure tab) to
	 * database
	 * 
	 * @param terms
	 */
	public void saveTermRole(ArrayList<String> terms, String role, UUID last,
			UUID current) throws SQLException {
		// Connection conn = null;
		PreparedStatement pstmt = null;
		String tablePrefix = MainForm.dataPrefixCombo.getText();
		try {
			// conn = DriverManager.getConnection(url);
			String wordrolesable = this.databasePrefix + "_wordroles";
			Statement stmt = connection.createStatement();
			;
			stmt.execute("delete from " + wordrolesable + " where savedid='"
					+ last.toString() + "'");
			stmt.close();
			pstmt = connection.prepareStatement("Insert into " + wordrolesable
					+ " (word,semanticrole, savedid) values (?,?, ?)");
			// stmt =
			// connection.prepareStatement("Update "+postable+" set saved_flag ='green' where word = ?");
			for (String term : terms) {
				pstmt.setString(1, term);
				pstmt.setString(2, role);
				pstmt.setString(3, current.toString());
				try {
					pstmt.execute();
				} catch (Exception exe) {
					if (!exe.getMessage().contains("Duplicate entry")) {
						throw exe;
					}
				}
			}
			// stmt.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	public void recordNonEQTerms(ArrayList<String> words, UUID last,
			UUID current) throws SQLException {
		// Connection conn = null;
		PreparedStatement pstmt = null;
		String tablePrefix = MainForm.dataPrefixCombo.getText();
		try {
			Statement stmt = connection.createStatement();
			;
			if (last != null) {
				// clean up last saved info
				stmt.execute("update " + this.databasePrefix + "_wordpos "
						+ " set saved_flag = '' where savedid='"
						+ last.toString() + "'");
			}
			if (current == null) {
				// set flag in pos table
				// pstmt =
				// connection.prepareStatement("update "+tablePrefix+"_"+ApplicationUtilities.getProperty("POSTABLE")+
				// " set saved_flag ='red' where pos=? and word=?");
				pstmt = connection.prepareStatement("update "
						+ this.databasePrefix + "_wordpos "
						+ " set saved_flag ='red' where word=?");
			} else {
				// pstmt =
				// connection.prepareStatement("update "+tablePrefix+"_"+ApplicationUtilities.getProperty("POSTABLE")+
				// " set saved_flag ='red', savedid='"+current.toString()+"' where pos=? and word=?");
				pstmt = connection.prepareStatement("update "
						+ this.databasePrefix + "_wordpos "
						+ " set saved_flag ='red', savedid='"
						+ current.toString() + "' where word=?");
			}
			for (String word : words) {
				// pstmt.setString(1, "b");
				// pstmt.setString(2, word);
				pstmt.setString(1, word);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			// insert words in noneqterms table
			// clean up last saved info
			if (last != null) {
				stmt.execute("delete from " + this.databasePrefix
						+ "_noneqterms" + " where savedid='" + last.toString()
						+ "'");
			}
			if (last != null) {
				pstmt = connection.prepareStatement("insert into "
						+ this.databasePrefix + "_noneqterms"
						+ "(term, source, savedid) values(?, ?, ?)");
			} else {
				pstmt = connection.prepareStatement("insert into "
						+ this.databasePrefix + "_noneqterms"
						+ "(term, source) values(?, ?)");
			}
			for (String word : words) {
				pstmt.setString(1, word);
				pstmt.setString(2, tablePrefix);
				if (current != null)
					pstmt.setString(3, current.toString());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	/*
	 * public void createHeuristicTermsTable(){ //Connection conn = null;
	 * Statement stmt = null ; String tablePrefix =
	 * MainForm.dataPrefixCombo.getText(); try { //conn =
	 * DriverManager.getConnection(url); stmt = connection.createStatement();;
	 * stmt
	 * .execute("drop table if exists "+tablePrefix+"_"+ApplicationUtilities.
	 * getProperty("HEURISTICSTERMS"));
	 * stmt.execute("create table if not exists "
	 * +tablePrefix+"_"+ApplicationUtilities.getProperty("HEURISTICSTERMS")+
	 * " (word varchar(50), type varchar(20), primary key(word))"); } catch
	 * (SQLException e){ e.printStackTrace(); } finally { try{ if (stmt != null)
	 * { stmt.close(); } }catch(Exception e){ e.printStackTrace(); } } }
	 */

	public void createWordRoleTable() {
		// Connection conn = null;
		Statement stmt = null;
		String tablePrefix = MainForm.dataPrefixCombo.getText();
		try {
			// conn = DriverManager.getConnection(url);
			stmt = connection.createStatement();
			;
			stmt.execute("drop table if exists " + this.databasePrefix
					+ "_wordroles");
			stmt.execute("create table if not exists "
					+ this.databasePrefix
					+ "_wordroles"
					+ " (word varchar(50), semanticrole varchar(2), savedid varchar(40), primary key(word, semanticrole))");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// added newly to load the styled context for step 4 (all 4 sub-tabs)
	public void getContextData(String word, StyledText context)
			throws Exception {

		// Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Class.forName(driverPath);
			// conn = DriverManager.getConnection(url);
			String tablePrefix = MainForm.dataPrefixCombo.getText();
			word = word.replaceAll("_", "-");
			String sql = "select source,originalsent from " + tablePrefix
					+ "_sentence where originalsent rlike '[[:<:]]" + word
					+ "[[:>:]]'";
			// String sql =
			// "select source,originalsent from "+tablePrefix+"_sentence where originalsent rlike '[[:<:]]"+word+"[[:>:]]' or tag = '"+word+"'";
			stmt = connection.prepareStatement(sql);
			rs = stmt.executeQuery();
			context.cut();
			String text = "";
			int count = 0;
			while (rs.next()) { // collect sentences
				count++;
				String src = rs.getString("source");
				String sentence = rs.getString("originalsent");
				text += count + ": " + sentence + " [" + src + "] \r\n";
				// System.out.println(src+"::"+sentence+" \r\n");
				// context.append(src+"::"+sentence+" \r\n");
			}
			text = text.toLowerCase();
			if (text.length() == 0) {
				text = "No context avaialable for " + word
						+ ". Please categorize it as 'neither'.";
			}

			// format sentences
			ArrayList<StyleRange> srs = new ArrayList<StyleRange>();
			String[] tokens = text.split("\\s");
			int currentindex = 0;
			// String newtext = "";
			for (String token : tokens) {
				if (token.matches(".*?\\b" + word + "\\b.*")) {
					StyleRange sr = new StyleRange();
					sr.start = currentindex;
					sr.length = token.length();
					sr.fontStyle = SWT.BOLD;
					srs.add(sr);
				} else if (token.matches("^\\[.*?\\]$")) {
					StyleRange sr = new StyleRange();
					sr.start = currentindex;
					sr.length = token.length();
					sr.foreground = MainForm.grey;
					srs.add(sr);
				}
				// newtext+=token+" ";
				currentindex += token.length() + 1;
			}
			context.append(text);
			context.setStyleRanges(srs.toArray(new StyleRange[] {}));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * merge grouped_terms and group_decision table and add data into
	 * term_category table, which may already have data also add newly learned
	 * structure term to the table for category "structure" This makes
	 * term_category contain all new terms learned from a volume of text
	 */
	public int finalizeTermCategoryTable() {
		String prefix = MainForm.dataPrefixCombo.getText();
		int count = 0;
		try {
			Statement stmt = connection.createStatement();
			;
			String q = "select distinct groupId, category from " + prefix
					+ "_group_decisions where category !='done'"; // "done" was
																	// a fake
																	// decision
																	// for
																	// unpaired
																	// terms
			ResultSet rs = stmt.executeQuery(q);
			while (rs.next()) {
				int gid = rs.getInt(1);
				String cat = rs.getString(2);
				Statement stmt2 = connection.createStatement();
				;
				ResultSet rs2 = stmt2
						.executeQuery("select term, cooccurTerm from " + prefix
								+ "_grouped_terms where groupId =" + gid);
				while (rs2.next()) {
					String t1 = rs2.getString(1);
					String t2 = rs2.getString(2);
					insert2TermCategoryTable(t1, cat);
					count++;
					if (t2 != null && t2.trim().length() > 0) {
						insert2TermCategoryTable(t2, cat);
						count++;
					}
				}
			}

			// insert structure terms
			q = "select distinct word from "
					+ this.databasePrefix
					+ "_wordroles"
					+ " where semanticrole in ('op', 'os') and "
					+ " word not in (select distinct term from "
					+ MainForm.glossaryPrefixCombo.getText()
					+ " where category in ('STRUCTURE', 'FEATURE', 'SUBSTANCE', 'PLANT', 'nominative', 'structure'))";
			rs = stmt.executeQuery(q);
			while (rs.next()) {
				String t = rs.getString(1);
				insert2TermCategoryTable(t, "structure");
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	private void insert2TermCategoryTable(String term, String cat)
			throws SQLException {
		String sql = "insert into " + MainForm.dataPrefixCombo.getText().trim()
				+ "_term_category(term, category) values (?,?)";
		PreparedStatement pstmt = connection.prepareStatement(sql);
		pstmt.setString(1, term);
		pstmt.setString(2, cat);
		pstmt.execute();
	}

	/**
	 * correct the typos in database
	 * 
	 * @param typos
	 * @return hashtable of typos with associated source files
	 * 
	 */
	public Hashtable<String, TreeSet<String>> correctTyposInDB(
			Hashtable<String, String> typos) {
		Hashtable<String, TreeSet<String>> typosources = new Hashtable<String, TreeSet<String>>();
		Enumeration<String> en = typos.keys();
		while (en.hasMoreElements()) {
			String typo = en.nextElement();
			String correction = typos.get(typo);
			try {
				// find sources
				TreeSet<String> sources = typosources.get(typo);
				PreparedStatement stmt = connection
						.prepareStatement("select source from "
								+ this.databasePrefix + "_sentence"
								+ " where sentence REGEXP '[[:<:]]" + typo
								+ "[[:>:]]'"); // originalsent already corrected
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String src = rs.getString(1);
					src = src.substring(0, src.lastIndexOf("-"));
					if (sources == null) {
						sources = new TreeSet<String>();
					}
					sources.add(src);
				}
				typosources.put(typo, sources);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// make corrections in db tables
			correctTypoInTableExactMatch("wordpos", "word", typo, correction);
			correctTypoInTableWordMatch("sentence", "sentence", typo,
					correction, "sentid");
			correctTypoInTableExactMatch("allwords", "word", typo, correction);
			correctTypoInTableExactMatch("unknownwords", "word", typo,
					correction);
			correctTypoInTableExactMatch("taxonnames", "name", typo, correction);
			correctTypoInTableExactMatch("singularplural", "singular", typo,
					correction);
			correctTypoInTableExactMatch("singularplural", "plural", typo,
					correction);
			correctTypoInTableExactMatch("noneqterms", "term", typo, correction);
		}
		return typosources;

	}

	/**
	 * 
	 * @param table
	 * @param typo
	 * @param correction
	 * @param exactmatch
	 */
	public void correctTypoInTableWordMatch(String table, String column,
			String typo, String correction, String PK) {
		try {
			// mysql can't do word-based match, so had to update sentence one by
			// one
			PreparedStatement stmt = connection.prepareStatement("select " + PK
					+ ", " + column + " from "
					+ MainForm.dataPrefixCombo.getText().trim() + "_" + table
					+ " where " + column + " REGEXP '[[:<:]]" + typo
					+ "[[:>:]]'");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String key = rs.getString(1);
				String text = rs.getString(2);
				String correctioncp = correction;
				Pattern p = Pattern.compile("(.*?)\\b(" + typo + ")\\b(.*)",
						Pattern.CASE_INSENSITIVE);
				// need be case insenstive, but keep the original case
				Matcher m = p.matcher(text);
				while (m.matches()) {
					text = m.group(1);
					String w = m.group(2);
					if (w.matches("^[A-Z].*")) {
						correction = correction.substring(0, 1).toUpperCase()
								+ correction.substring(1);
					} else {
						correction = correctioncp;
					}
					text += correction;
					text += m.group(3);
					m = p.matcher(text);
				}
				// put corrected back
				PreparedStatement stmt1 = connection.prepareStatement("update "
						+ MainForm.dataPrefixCombo.getText().trim() + "_"
						+ table + " set " + column + "='" + text + "' where "
						+ PK + "='" + key + "'");
				stmt1.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param table
	 * @param typo
	 * @param correction
	 * @param exactmatch
	 */
	public void correctTypoInTableExactMatch(String table, String column,
			String typo, String correction) {
		try {
			String where = column + "='" + typo + "'";
			String set = column + "='" + correction + "'";
			PreparedStatement stmt = connection.prepareStatement("update "
					+ MainForm.dataPrefixCombo.getText().trim() + "_" + table
					+ " set " + set + " where " + where);
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * insert a record into the known typo database table. if (correction, typo)
	 * exists in the table, just remove the record, not need to insert (typo,
	 * correction).
	 * 
	 * @param typo
	 * @param correction
	 */
	public void insertTypo(String typo, String correction) {
		try {
			PreparedStatement stmt = connection
					.prepareStatement("select * from  " + this.databasePrefix
							+ "_typos" + " where typo = ? and correction = ?");
			stmt.setString(1, correction);
			stmt.setString(2, typo);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				// delete the record
				stmt = connection.prepareStatement("delete from  "
						+ this.databasePrefix + "_typos"
						+ " where typo = ? and correction = ?");
				stmt.setString(1, correction);
				stmt.setString(2, typo);
				stmt.execute();
				return;
			}

			PreparedStatement stmt1 = connection
					.prepareStatement("insert into  " + this.databasePrefix
							+ "_typos" + " (typo, correction) values (?, ?)");
			stmt1.setString(1, typo);
			stmt1.setString(2, correction);
			stmt1.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read typos from the known database table into the hashtable.
	 * 
	 * @param typos
	 */
	public void readInTypos(Hashtable<String, String> typos) {
		try {
			PreparedStatement stmt = connection
					.prepareStatement("select typo, correction from "
							+ this.databasePrefix + "_typos");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				typos.put(rs.getString(1), rs.getString(2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
