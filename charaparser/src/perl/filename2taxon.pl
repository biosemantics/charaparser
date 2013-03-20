use strict;
use DBI;

#this program generates a table called filename2taxon table from the "taxon" table (which is created by CharaParser)

#filename2taxon table (to be created):
#DROP TABLE IF EXISTS `toboston`.``;
#CREATE TABLE  `toboston`.`fnav19_filename2taxon` (
#  `filename` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
#  `hasdescription` tinyint(1) DEFAULT NULL,
#  `family` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `subfamily` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `tribe` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `subtribe` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `genus` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `subgenus` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `section` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `subsection` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `species` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `subspecies` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
#  `variety` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL
#) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#taxon table(existing):
#DROP TABLE IF EXISTS `markedupdatasets`.`fnav20_taxon`;
#CREATE TABLE  `markedupdatasets`.`fnav20_taxon` (
#  `taxonnumber` varchar(10) DEFAULT NULL,
#  `name` varchar(500) DEFAULT NULL,
#  `rank` varchar(20) DEFAULT NULL,
#  `filenumber` int(11) DEFAULT NULL
#) ENGINE=InnoDB DEFAULT CHARSET=utf8;

my $db = "toboston"; #could be any database.
my $user = "root";
my $password = "root";
my $host = "localhost";
my $dbh = DBI->connect("DBI:mysql:host=$host", $user, $password) or die DBI->errstr."\n";
my $taxontb = "markedupdatasets.fnav20_taxon";
my $f2ttb = "fnav20_filename2taxon";

my $test = $dbh->prepare('use '.$db) or die $dbh->errstr."\n";
$test->execute() or die $test->errstr."\n";


$test = $dbh->prepare('DROP TABLE IF EXISTS '.$f2ttb) or die $dbh->errstr."\n";
$test->execute() or die $test->errstr."\n";

$test = $dbh->prepare('CREATE TABLE  '.$f2ttb.' (
  `filename` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `hasdescription` tinyint(1) DEFAULT 0,
  `family` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `subfamily` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `tribe` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `subtribe` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `genus` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `subgenus` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `section` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `subsection` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `species` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `subspecies` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT "",
  `variety` varchar(250) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT ""
) ENGINE=InnoDB DEFAULT CHARSET=utf8;') or die $dbh->errstr."\n";
$test->execute() or die $test->errstr."\n";

my ($taxonname, $file, $rank, $prank, $fields, $values, $insert);

$test = $dbh->prepare('select name, filenumber, rank from '.$taxontb) or die $dbh->errstr."\n";
$test->execute() or die $test->errstr."\n";

$fields = "filename, hasdescription";
$values = ""; #$fields and $values will be used to compose the sql insert statement
while(($taxonname, $file, $rank) = $test->fetchrow_array()){
	$rank =~ s#_name##;
	$file = $file.".xml";
	if($values !~ /\w/){
		$values = "'".$file."', 0";
	}else{
		$values =~ s#.*? 0#'$file', 0#; #update $filename
	}
	if($fields =~ /\b$rank\b/i){#a rank visited before, remove everything from the rank on, then append the rank
		$fields =~ s#, $rank.*##;
		#make sure $values always has a good correspondence with $fields  
		$values = matchValues($fields, $values);
	}
	#a new lower rank: append
	$fields .=", ".$rank;
	$values .=", '".$taxonname."'";
	my $query = 'insert into '.$f2ttb.' ('.$fields.') values('.$values.')';		
	print "fields: $fields\n";
	print "values: $values\n";
	print "query: $query\n\n";
	$insert = $dbh->prepare($query) or die $dbh->errstr."\n";
	$insert->execute() or die $insert->errstr."\n";
}

#generate the pattern based on the number of , in $fields
sub matchValues{
	my ($fields, $values) = @_;
	my @fields = split(/\s*,\s*/, $fields);
	my $keepsize = @fields;
	my @values = split(/\s*,\s*/,$values);
	my $newvalues = "";
	for(my $c = 0; $c < $keepsize; $c++){
		$newvalues .= $values[$c].", "
	}
	$newvalues =~ s#,\s*$##;
	return $newvalues;
}