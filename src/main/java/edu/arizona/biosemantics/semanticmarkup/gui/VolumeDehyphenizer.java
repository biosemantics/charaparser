package edu.arizona.biosemantics.semanticmarkup.gui;


import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;


/**
 * normalize hyphens in the document folder. may be plain text or html/xml docs. for the latter, tags are ignored in dehyphenization process.
 * run this before VolumeMarkup.
 * @author hongcui
 *
 */
@SuppressWarnings({ "unused","static-access" })
public class VolumeDehyphenizer {

    protected String database = "";
    //protected Hashtable<String,String> mapping = new Hashtable<String, String>();
    protected ProcessListener listener;
    private static final Logger LOGGER = Logger.getLogger(VolumeDehyphenizer.class);
    private Display display;
    private Text perlLog;
    private String dataPrefix;
    private DeHyphenAFolder dhf;
    private Table descriptorTable;
    private MainForm mainForm;
    private VolumeMarkupDbAccessor vmdb;
    private String glossaryTableName;
    
    public VolumeDehyphenizer(String workdir, 
    		String todofoldername, String databaseName, String databaseUser, String databasePassword, 
    		Display display, Text perlLog, String dataPrefix, /*Table descriptorTable,*/ MainForm mainForm) {
        //this.database = database;
        /** Synchronizing UI and background process **/
        this.display = display;
        this.perlLog = perlLog;
        this.dataPrefix = dataPrefix;
        //this.descriptorTable = descriptorTable;
        this.mainForm = mainForm;
        this.glossaryTableName = mainForm.glossaryPrefixCombo.getText();
        this.vmdb = new VolumeMarkupDbAccessor(databaseName, databaseUser, databasePassword, this.dataPrefix, this.glossaryTableName);
     
        this.dhf = new DeHyphenAFolder(workdir, todofoldername, databaseName, databaseUser, databasePassword, this, dataPrefix, this.glossaryTableName);
    }

    public void run () {
       	boolean done = dhf.dehyphen();//dhf waits for all unmatched brackets are fixed.
    }
}

