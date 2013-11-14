/**
 * 
 */
package semanticMarkup.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * @author hongcui
 * Move the dyhypen() function from VolumeDehypenizer, to make DeHyphenAFolder a utility class that can be called by other projects.
 */
@SuppressWarnings("unchecked")
public class DeHyphenAFolder {
	private String databaseName;
	@SuppressWarnings("unused")
	private VolumeDehyphenizer vd;
	@SuppressWarnings("unused")
	private String dataPrefix;
	private String tablename;
	private File folder;
	private File outfolder;
	private static final Logger LOGGER = Logger.getLogger(DeHyphenAFolder.class);
	private Connection conn;
    //static public String num = "\\d[^a-z]+";
    private Hashtable<String,String> mapping = new Hashtable<String, String>();
    private String glossarytable;
	private String databaseUser;
	private String databasePassword;
	/**
	 * 
	 */
	public DeHyphenAFolder(String workdir, 
    		String todofoldername, String databaseName, String databaseUser, String databasePassword, 
    		VolumeDehyphenizer vd, String dataPrefix, String glossarytable) {
		this.glossarytable = glossarytable;
        this.databaseName = databaseName;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.vd = vd;
        this.dataPrefix = dataPrefix;
        this.tablename = dataPrefix+"_allwords";
        
        workdir = workdir.endsWith("/")? workdir : workdir+"/";
        this.folder = new File(workdir+todofoldername);
        this.outfolder = new File(workdir+ "dehyphened");
        if(!outfolder.exists()){
            outfolder.mkdir();
        }
        
        try{
            if(conn == null){
            	Class.forName("com.mysql.jdbc.Driver");
        		String URL = "jdbc:mysql://localhost/" + this.databaseName + "?user=" + this.databaseUser + "&password=" + 
    					this.databasePassword + "&connecttimeout=0&sockettimeout=0&autoreconnect=true";
                conn = DriverManager.getConnection(URL);
                //createNumTextMixTable();
                createAllWordsTable();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
	}
	
	   public boolean dehyphen(){
	       fillInWords();
	     
	       DeHyphenizer dh = new DeHyphenizerCorrected(this.databaseName, this.databaseUser, this.databasePassword, 
	    		   this.tablename, "word", "count", "-", this.glossarytable);

	       try{
	            Statement stmt = conn.createStatement();
	            ResultSet rs = stmt.executeQuery("select word from "+tablename+" where word like '%-%'");
	            while(rs.next()){
	                String word = rs.getString("word");
	                String dhword = dh.normalFormat(word).replaceAll("-", "_"); //so dhwords in _allwords table are comparable to words in _wordpos and other tables.
	                Statement stmt1 = conn.createStatement();
	                stmt1.execute("update "+tablename+" set dhword ='"+dhword+"' where word='"+word+"'");
	                mapping.put(word, dhword);
	            }
	            stmt.execute("update "+tablename+" set dhword=word where dhword is null");
	       }catch(Exception e){
	        	e.printStackTrace();
	       }
	       normalizeDocument();
	       return true;
	    }
	    
	   
	   /**
	    * 
	    * @return pass the check or not
	    */
	    private boolean hasProblems() {
        	boolean has = false;
        	int problemcount = 0;
	        try {
	            File[] flist = folder.listFiles();
	            for(int i= 0; i < flist.length; i++){
	                BufferedReader reader = new BufferedReader(new FileReader(flist[i]));
	                String line = null; 
	                StringBuffer sb = new StringBuffer();
	                while ((line = reader.readLine()) != null) {
	                    line = line.replaceAll(System.getProperty("line.separator"), " ");
	                    sb.append(line);
	                }
	                reader.close();
	                String text = sb.toString();
	                //check for unmatched brackets
	                if(hasUnmatchedBrackets(text)){
	                	has = true;
						//automatically open the file in a text editor for corrections
	                	Runtime.getRuntime().exec("notepad" 
								+ " \"" + flist[i].getAbsolutePath() + "\"");
	                }
	                //check for missing spaces between text and numbers: 
	                if(text.matches(".*[a-zA-Z]\\d.*") || text.matches(".*\\d[a-zA-Z].*")){
	                	//has =true; //ant descriptions contain "Mf4"
	                 	//vd.showPerlMessage((++problemcount)+": "+flist[i].getAbsolutePath()+" misses a space between a word and a number in \""+text+"\"\n");      	       
	                }
	                //check for (?)
	                if(text.matches(".*?\\(\\s*\\?\\s*\\).*")){
	                	has =true;
	                	Runtime.getRuntime().exec("notepad" 
								+ " \"" + flist[i].getAbsolutePath() + "\"");
	                }
	            }
	        }catch(Exception e){
	            	e.printStackTrace();
	        }
	        return has;
	    }

		private void createAllWordsTable(){
	        try{
	            Statement stmt = conn.createStatement();
	            stmt.execute("drop table if exists "+tablename);
	            String query = "create table if not exists "+tablename+" (word varchar(150) unique not null primary key, count int, dhword varchar(150), inbrackets int default 0)"
	            		+ " CHARACTER SET utf8 engine=innodb";
	            stmt.execute(query);	           
	        }catch(Exception e){
	        	e.printStackTrace();	
	        }
	    }

	    /**
	     * check for unmatched brackets too.
	     */
	    private void fillInWords(){
	        try {
	            Statement stmt = conn.createStatement();
	            ResultSet rs = null;
	            File[] flist = folder.listFiles();
	            int total = flist.length;
	            for(int i= 0; i < flist.length; i++){
	                BufferedReader reader = new BufferedReader(new FileReader(flist[i]));
	                String line = null; 
	                StringBuffer sb = new StringBuffer();
	                while ((line = reader.readLine()) != null) {
	                    line = line.replaceAll(System.getProperty("line.separator"), " ");
	                    sb.append(line);
	                }
	                reader.close();
	                String text = sb.toString();
	                text = text.toLowerCase();
	                text = text.replaceAll("<[^<]+?>", " ");
	                text = text.replaceAll("\\d", " ");
	                text = text.replaceAll("\\(", " ( ");
	                text = text.replaceAll("\\)", " ) ");
	                text = text.replaceAll("\\[", " [ ");
	                text = text.replaceAll("\\]", " ] ");
	                text = text.replaceAll("\\{", " { ");
	                text = text.replaceAll("\\}", " } ");
	                text = text.replaceAll("\\s+", " ").trim();
                    String[] words = text.split("\\s+");
                    int lround = 0;
                    int lsquare = 0;
                    int lcurly = 0;
                    int inbracket = 0;
                    for(int j = 0; j < words.length; j++){
                        String w = words[j].trim();
                        if(w.compareTo("(")==0) lround++;
                        else if(w.compareTo(")")==0) lround--;
                        else if(w.compareTo("[")==0) lsquare++;
                        else if(w.compareTo("]")==0) lsquare--;
                        else if(w.compareTo("{")==0) lcurly++;
                        else if(w.compareTo("}")==0) lcurly--;
                        else{
                        	w = w.replaceAll("[^-a-z]", " ").trim();
                            if(w.matches(".*?\\w.*")){
                            	if(lround+lsquare+lcurly > 0){
                            		inbracket = 1;
                            	}else{
                            		inbracket = 0;
                            	}
	                            int count = 1;
	                            rs = stmt.executeQuery("select word, count, inbrackets from "+tablename+"  where word='"+w+"'");
	                            if(rs.next()){ //normal word exist
	                                count += rs.getInt("count");
	                                inbracket *= rs.getInt("inbrackets");
	                            }
	                            stmt.execute("delete from "+tablename+" where word ='"+w+"'");
	                            stmt.execute("insert into "+tablename+" (word, count, inbrackets) values('"+w+"', "+count+","+inbracket+")");
	                        }
                        }
                    }
	            }
	            if(rs!=null)
	            	rs.close();
                stmt.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }
	    private boolean hasUnmatchedBrackets(String text) {
	    	String[] lbrackets = new String[]{"\\[", "(", "{"};
	    	String[] rbrackets = new String[]{"\\]", ")", "}"};
	    	for(int i = 0; i<lbrackets.length; i++){
	    		int left1 = text.replaceAll("[^"+lbrackets[i]+"]", "").length();
	    		int right1 = text.replaceAll("[^"+rbrackets[i]+"]", "").length();
	    		if(left1!=right1) return true;
	    	}
			return false;
		}
	    
	    @SuppressWarnings("unused")
		private String fixBrokenHyphens(String broken){ //cup-[,]  disc-[,]  or dish-shaped
	        StringBuffer fixed = new StringBuffer();
	        Pattern p = Pattern.compile("(.*?\\b)([a-z]+)-\\W[^\\.]*?[a-z]+-([a-z]+)(.*)");
	        Matcher m = p.matcher(broken);
	        while(m.matches()){
	            String begin = m.group(1);
	            String part = broken.substring(m.start(2), m.start(3));
	            broken = m.group(4);
	            String fix = m.group(3);
	            part = part.replaceAll("-(?!\\w)", "-"+fix);
	            fixed.append(begin+part);
	            m = p.matcher(broken);
	        }
	        fixed.append(broken);
	        return fixed.toString();
	    }
	    
	    
	    private void normalizeDocument(){
	        try {
	            File[] flist = folder.listFiles();
	            for(int i= 0; i < flist.length; i++){
	                BufferedReader reader = new BufferedReader(new FileReader(flist[i]));
	                String line = null; //DO NOT normalize case
	                StringBuffer sb = new StringBuffer();
	                while ((line = reader.readLine()) != null) {
	                    line = line.replaceAll(System.getProperty("line.separator"), " ");
	                    sb.append(line);
	                }
	                reader.close();
	                String text = sb.toString();
	                text = performMapping(text);
	                //turn "." that are in brackets as [.DOT.] for unsupervised learning pl.
	                text = text.replaceAll("\\(", " ( ");
	                text = text.replaceAll("\\)", " ) ");
	                text = text.replaceAll("\\[", " [ ");
	                text = text.replaceAll("\\]", " ] ");
	                text = text.replaceAll("\\{", " { ");
	                text = text.replaceAll("\\}", " } ");
	                text = text.replaceAll("\\s+", " ").trim();
	                int lround = 0;
	                int lsquare = 0;
	                int lcurly = 0;
	                sb = new StringBuffer();
                    String[] words = text.split("\\s+");
	                for(int j = 0; j < words.length; j++){
                        String w = words[j].trim();
                        if(w.compareTo("(")==0){
                        	lround++;
                        	sb.append("(");
                        }else if(w.compareTo(")")==0){
                        	lround--;
                        	sb.append(")");
                        }else if(w.compareTo("[")==0){
                        	lsquare++;
                        	sb.append("[");
                        }else if(w.compareTo("]")==0){
                        	lsquare--;
                        	sb.append("]");
                        }else if(w.compareTo("{")==0){
                        	lcurly++;
                        	sb.append("{");
                        }else if(w.compareTo("}")==0){
                        	lcurly--;
                        	sb.append("}");
                        }else{
                        	if(w.matches(".*?[.?;:!].*?") && (lround+lsquare+lcurly)>0){
                        		w = w.replaceAll("\\.", "[DOT]");
                        		w = w.replaceAll("\\?", "[QST]");
                        		w = w.replaceAll(";", "[SQL]");
                        		w = w.replaceAll(":", "[QLN]");
                        		w = w.replaceAll("!", "[EXM]");
                        	}
                        	sb.append(w+" ");                        	
                        }                        
	                }
	                text = sb.toString().replaceAll("\\s*\\(\\s*", "(").replaceAll("\\s*\\)\\s*", ")")
	                		.replaceAll("(?<=[^0-9+–-])\\(", " (").replaceAll("\\)(?=[a-z])", ") ").trim();
	                //write back
	                File outf = new File(outfolder, flist[i].getName());
	                //BufferedWriter out = new BufferedWriter(new FileWriter(flist[i]));
	                BufferedWriter out = new BufferedWriter(new FileWriter(outf));
	                out.write(text);
	                out.close();
	                //System.out.println(flist[i].getName()+" dehyphenized");
	            }
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }

	    private String performMapping(String original){
	        Enumeration en = mapping.keys();
	        while(en.hasMoreElements()){
	            String hword = (String)en.nextElement();
	            String dhword = (String)mapping.get(hword);
	            //System.out.println("hword: "+hword +" dhword: "+dhword);
	            if(!hword.equals(dhword) && !hword.startsWith("-") && !hword.endsWith("-")){
	                //replace those in lower cases
	                original = original.replaceAll(hword, dhword);
	                //hyphen those phrases that are hyphened once 
	                String dhw = dhword.replaceAll("-", " "); //cup-shaped => cup shaped
	                original = original.replaceAll(dhw, dhword); //cup shaped =>cup-shaped
	                //upper cases
	                hword = hword.toUpperCase().substring(0,1)+hword.substring(1);
	                dhword = dhword.toUpperCase().substring(0,1)+dhword.substring(1);
	                original = original.replaceAll(hword, dhword);
	                dhw = dhword.replaceAll("-", " "); //Cup-shaped => Cup shaped
	                original = original.replaceAll(dhw, dhword); //Cup shaped =>Cup-shaped
	            }
	        }
	        return original;
	    }
}
