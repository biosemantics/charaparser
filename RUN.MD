Run Charaparser
================

Setup
---------
0. Install maven and aspectj plugin for eclipse
1. Checkout out the repository
2. Import as general project
3. Configure as Maven Project
4. Configure as AspectJ Project
5. Install MySQL if necessary
6. Create charaparser database
7. Import SetupDB.sql dump located in root project directory
8. Create Run Configuration with Main class <a href="https://github.com/biosemantics/charaparser/blob/master/src/main/java/edu/arizona/biosemantics/semanticmarkup/ETCLearnMain.java
">ETCLearnMain</a>
 * As arguments use 
   * -c {TaxonGroup} 
-w {Path-to-charaparser-project}\wordnet\wn31\dict 
-l {Path-to-charaparser-project}\src\main\perl\edu\arizona\biosemantics\semanticmarkup\markupelement\description\ling\learn\lib\perl 
-n {database-host} -p {database-port} -d {database-name} -u {database-user} -s {database-password} 
-i input -z {runId} -o {oto2-installation-url} -y -q {ontology-owl-file-directory}
 * <a href="https://github.com/biosemantics/common/blob/master/biology/src/main/java/edu/arizona/biosemantics/common/biology/TaxonGroup.java">{TaxonGroup} possiblities</a>
9. Create Run COnfiguration with Main class <a href="https://github.com/biosemantics/charaparser/blob/master/src/main/java/edu/arizona/biosemantics/semanticmarkup/ETCMarkupMain.java
">ETCMarkupMain</a>
 * Use the same arguments as for ETCLearnMain

Running
----------
1. Run ETCLearnMain
2. Visit nextStep.html located in workspace/{runId} to categorize learned terms (optional)
3. Run ETCMarkupMain

After you run either Learn or Markup
workspace/{runId}/out will contain output files
workspace/{runId}/*.log will be created log files
