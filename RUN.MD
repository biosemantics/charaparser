Run Charaparser
================

Setup
---------
0. Install maven and aspectj plugin for eclipse
 * m2e - Maven Integration for Eclipse (e.g. for Juno version: http://download.eclipse.org/releases/juno)
 * AspectJ Development Tools (http://download.eclipse.org/tools/ajdt/42/update)
 * Maven Integration for AJDT (http://dist.springsource.org/release/AJDT/configurator/)
1. Checkout out the repository
2. Import as general project
3. Configure as Maven Project
4. Configure as AspectJ Project
5. Install <a href="http://www.mysql.com/">MySQL</a> if necessary
6. Install <a href="https://www.perl.org/">Perl</a> if necessary, e.g. for Windows you can use <a href="http://www.activestate.com/activeperl">ActivePerl</a>
 * including modules 
    1. <a href="http://search.cpan.org/dist/DBI-1.622/">DBI</a>
    2. <a href="http://search.cpan.org/dist/DBD-mysql-4.022/">DBD::mysql</a>
    3. <a href="http://search.cpan.org/dist/Encoding-FixLatin-1.02/">Encoding::FixLatin</a>
 * if you use ActivePerl on Windows, you can install them using the package manager. For Encoding::FixLatin you may have to download the module manually, add the FixLatin.pm file of the archive 
   to {ActivePerlDir}/site/lib/Encoding/ to find it installable in the package manager.
7. Install <a href="http://wordnet.princeton.edu/">WordNet</a> if necessary
8. Create charaparser database
9. Import <a href="https://github.com/biosemantics/charaparser/blob/master/setupDB.sql">setupDB.sql</a> dump
10. Create Run Configuration with Main class <a href="https://github.com/biosemantics/charaparser/blob/master/src/main/java/edu/arizona/biosemantics/semanticmarkup/ETCLearnMain.java">ETCLearnMain</a>
 * As arguments use 
   * -c {TaxonGroup}  -i {input-directory} -z {runId-datasetprefix} -f debug

 * <a href="https://github.com/biosemantics/common/blob/master/biology/src/main/java/edu/arizona/biosemantics/common/biology/TaxonGroup.java">{TaxonGroup} possiblities</a>
 
11. Create Run Configuration with Main class <a href="https://github.com/biosemantics/charaparser/blob/master/src/main/java/edu/arizona/biosemantics/semanticmarkup/ETCMarkupMain.java
">ETCMarkupMain</a>
 * Use the same arguments as for ETCLearnMain
12. Create config.properties file from <a href="https://github.com/biosemantics/charaparser/blob/master/src/main/resources/edu/arizona/biosemantics/semanticmarkup/config.properties.template">template</a>
   
Running
----------
1. Run ETCLearnMain
2. Visit nextStep.html located in workspace/{runId} to categorize learned terms (optional)
3. Run ETCMarkupMain

After you run either Learn or Markup
* workspace/{runId}/out will contain output files
* workspace/{runId}/*.log will be created log files
