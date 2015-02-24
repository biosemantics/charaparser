Run Charaparser
================

Setup
---------
* Checkout out the repository
* Import as general project
* Configure as Maven Project
* Configure as AspectJ Project
* Install MySQL if necessary
* Create charaparser database
* Import SetupDB.sql dump located in root project directory
* Create Run Configuration with Main class <a href="https://github.com/biosemantics/charaparser/blob/master/src/main/java/edu/arizona/biosemantics/semanticmarkup/ETCLearnMain.java
">ETCLearnMain</a>
** As arguments use 
-c {TaxonGroup} 
-w {Path-to-charaparser-project}\wordnet\wn31\dict 
-l {Path-to-charaparser-project}\src\main\perl\edu\arizona\biosemantics\semanticmarkup\markupelement\description\ling\learn\lib\perl 
-n {database-host} -p {database-port} -d {database-name} -u {database-user} -s {database-password} 
-i input -z {runId} -o {oto2-installation-url} -y -q {ontology-owl-file-directory}
** {TaxonGroup} possibilities see https://github.com/biosemantics/common/blob/master/biology/src/main/java/edu/arizona/biosemantics/common/biology/TaxonGroup.java
* Create Run COnfiguration with Main class <a href="https://github.com/biosemantics/charaparser/blob/master/src/main/java/edu/arizona/biosemantics/semanticmarkup/ETCMarkupMain.java
">ETCMarkupMain</a>
** Use the same arguments as for ETCLearnMain

Running
----------
* Run ETCLearnMain
* Visit nextStep.html located in workspace/{runId} to categorize learned terms (optional)
* Run ETCMarkupMain

After you run either Learn or Markup
workspace/{runId}/out will contain output files
workspace/{runId}/*.log will be created log files
