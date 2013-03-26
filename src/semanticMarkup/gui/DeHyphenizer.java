package semanticMarkup.gui;


//
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Hashtable;

import org.apache.log4j.Logger;


/**
 * run through a column in a database, remove hyphens that separate one word
 * @author hongcui
 *
 */
@SuppressWarnings({ "unchecked", "unused" })
public class DeHyphenizer {
	private String tablename;
	private String columnname;
	private String countcolumn;
	private String hyphen; 
	static private Connection conn = null;
	private static final Logger LOGGER = Logger.getLogger(DeHyphenizer.class);
	private String glossTable = null;
	private String databaseName;
	private String databaseUser;
	private String databasePassword;
	
	public DeHyphenizer(String databaseName, String databaseUser, String databasePassword, 
			String table, String column, String countcolumn, String hyphen, String glosstable) {
		this.glossTable = glosstable;
		this.databaseName = databaseName;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		try{
			if(conn == null){
				Class.forName("com.mysql.jdbc.Driver");
				String URL = "jdbc:mysql://localhost/" + this.databaseName + "?user=" + this.databaseUser + "&password=" + 
						this.databasePassword + "&connectTimeout=0&socketTimeout=0&autoReconnect=true";
				conn = DriverManager.getConnection(URL);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		this.tablename = table;
		this.columnname = column;
		this.countcolumn = countcolumn;
		this.hyphen = hyphen;
	}

	public void deHyphen(){
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select "+columnname+" from "+tablename+" where "+columnname+" like '%\\"+hyphen+"%'");
			while(rs.next()){
				String hyphenedterm = rs.getString(1);
				String[] parts = hyphenedterm.split("\\s+");
				String term = "";
				for(int i = 0; i<parts.length; i++){
					term += normalFormat(parts[i])+" ";
				}
				term = term.trim();
				if(term.compareTo(hyphenedterm) != 0){
					//System.out.println("\nold term: "+hyphenedterm);
					//System.out.println("new term: "+term);
					updateTable(hyphenedterm, term);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String normalFormat(String hyphened){
		/*if(hyphened.startsWith("NUM-")){
			return hyphened;
		}*/
		hyphened = hyphened.replaceFirst("^_", "");
		String[] segs = hyphened.split(this.hyphen);
		String[] terms = new String[segs.length];
		int[][] matrix = new int[segs.length][segs.length];
		//fill matrix
		fillMatrix(segs, matrix);
		collectTerms(segs, terms, matrix);
			
		//out put term
		String term = "";
		for(int i =0; i<terms.length; i++){
			if(terms[i] != null){
				term += terms[i]+"-";
			}
		}
		return term.replaceFirst("-$", "");
	}

	protected void collectTerms(String[] segs, String[] terms, int[][] matrix) {
		//rank rows
		int max = 0;
		Hashtable rank = new Hashtable();
		for(int i = 0; i < segs.length; i++){
			int ones = countOnes(matrix[i]);
			if(ones > max){
				max = ones;
			}
			String list = (String)rank.get(ones+"");
			if(list == null){
				rank.put(ones+"", i+"");
			}else{
				rank.put(ones+"", list+" "+i+"");
			}
		}
		//collect terms
		String checked="-";
		for(int i = 0; i <= max; i++){
			String list = (String)rank.get(i+"");
			if(list!= null && i == 0){//term not see in learned or glossary, and not connectable to other terms
				String[] indexes = list.split(" ");
				for(int j = 0; j < indexes.length; j++){
					int ind = Integer.parseInt(indexes[j]);
					terms[ind] = segs[ind];
				}

			}else if(list!=null){
				String[] indexes = list.split(" ");
				for(int j = 0; j < indexes.length; j++){
					int ind = Integer.parseInt(indexes[j]);
					if(checked.indexOf("-"+ind+"-")<0){
						int lastk = otherEndIndex(matrix[ind], ind); //last index of 1 in the row
					    terms[ind] = ind > lastk? formTerm(segs, lastk, ind) : formTerm(segs, ind, lastk);
					    checked += ind > lastk? formString(lastk, ind, "-") : formString(ind, lastk, "-");
					}
				}
			}
		}
	}

	private void fillMatrix(String[] segs, int[][] matrix) {
		for(int i = 0; i < segs.length; i++){
			for(int j = i; j<segs.length; j++){
				matrix[i][j] = isTerm(segs, i, j);
				matrix[j][i] = matrix[i][j];
			}
		}
	}
	protected String formString(int start, int end, String connector){
		String str = "";
		for(int i = start; i<=end; i++){
			str +=i+connector;
		}
		return str;
	}
	
	protected String formTerm(String[] segs, int start, int end){
		String str="";
		for(int i = start; i<=end; i++){
			str +=segs[i];
		}
		return str;
	}
	
	private int otherEndIndex(int [] array, int theotherindex){
		boolean self = false;
		int index = -1;
		for(int i = 0; i< array.length; i++){
			if(array[i]==1 && i == theotherindex){
				self = true;
			}else if(array[i]==1){//take the greatest index, may cause problem here.
				index = i;
			}
		}
		
		return index==-1? theotherindex : index;
	}
	
	private int countOnes(int[] array){
		int count = 0;
		for(int i = 0; i< array.length; i++){
			if(array[i]==1){
				count++;
			}
		}
		return count;
	}
	
	private int isTerm(String[] segs, int start, int end){
		String str=formTerm(segs, start, end);
		if(stringMatchInGloss(str)){ 
			return 1;
		}
		if(stringMatch(str)){
			return 1;
		}
		return 0;
	}
		
	
	private void updateTable(String oldt, String newt){
		try{
			Statement stmt = conn.createStatement();
			if(countcolumn == null ||countcolumn.trim().compareTo("") == 0){//without count
				ResultSet rs = stmt.executeQuery("select "+columnname+" from "+tablename+" where "+columnname+" = '"+newt+"'");
				if(!rs.next()){//newt not exist
					stmt.execute("update "+tablename+" set "+columnname+" = '"+newt+"' where "+columnname+" = '"+oldt+"'");
					//System.out.println("no count, update old with new ");
				}else{
					stmt.execute("delete from "+tablename+" where "+columnname+" = '"+oldt+"'");
					//System.out.println("no count, new exists, remove old");
				}
			}else{//with count
				int total  = 0;
				boolean newexist = false;
				ResultSet rs = stmt.executeQuery("select "+countcolumn+" from "+tablename+" where "+columnname+" = '"+oldt+"'");
				if(rs.next()){
					total += rs.getInt(1);
				}
			
				rs = stmt.executeQuery("select "+countcolumn+" from "+tablename+" where "+columnname+" = '"+newt+"'");
				if(rs.next()){
					total += rs.getInt(1);
					newexist = true;
				}
				if(newexist){
					stmt.execute("delete from "+tablename+" where "+columnname+" = '"+oldt+"'");
					stmt.execute("update "+tablename+" set "+countcolumn+" = "+ total+" where "+columnname+" = '"+newt+"'");
					//System.out.println("count, new exist, remove old, update count for new: "+total);
				}else{
					String query = "update "+tablename+" set "+countcolumn+" = "+ total+", "+columnname+" = '"+newt+"' where "+columnname+" = '"+oldt+"'";
					stmt.execute(query);
					//System.out.println("count, update old with new  with count: "+total);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean stringMatchInGloss(String term){
		boolean find = false;
		try{
			Statement stmt = conn.createStatement();
			String query = "select term from "+this.glossTable+" where term like '% "+term+" %' or term like '"+term+" %' or term like '% "+term+"' or term like '"+term+"'";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				find = true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return find;
	}
	
	public boolean stringMatch(String term){
		boolean find = false;
		try{
			Statement stmt = conn.createStatement();
			String query = "select "+this.columnname+" from "+tablename+" where "+this.columnname+" like '% "+term+" %' or "+this.columnname+" like '"+term+" %' or "+this.columnname+" like '% "+term+"' or "+this.columnname+" like '"+term+"'";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				find = true;
			}
		}catch(Exception e){
			e.printStackTrace();
			
		}
		return find;
	}

}
