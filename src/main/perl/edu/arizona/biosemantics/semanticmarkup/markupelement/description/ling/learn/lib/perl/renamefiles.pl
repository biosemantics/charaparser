use DBI;

my $host = "localhost";
my $user = "termsuser";
my $password = "termspassword";
my $dbh = DBI->connect("DBI:mysql:host=$host", $user, $password, { mysql_enable_utf8 => 1 })
or die DBI->errstr."\n";
$dbh->do('SET NAMES utf8');

my $test = $dbh->prepare('use treatiseh_benchmark')
or die $dbh->errstr."\n";
$test->execute();

<<<<<<< .mine
#$dir = "C:\\Treatise\\TreatiseH-dehyphened-copy\\";
$dir = "X:\\data\\foc\\vol5-numbered\\";
=======
#$dir = "C:\\Treatise\\TreatiseH-dehyphened-copy\\";
$dir = "X:\\data\\foc\\vol5-numbered\\";

>>>>>>> .r210
opendir(IN, "$dir") || die "$!: $dir\n";
$count = 1;

while(defined ($file=readdir(IN))){	
	next if $file !~ /\w/;
	`rename $dir$file $count.txt`;
	#$sth1 = $dbh->prepare("select source, sentid from sentence where source like '$file%'");
	#$sth1->execute() or die $sth1->errstr."\n";
	#while(($source, $id) = $sth1->fetchrow_array()){
	#	print "$source : :";
	#	$source =~ s#^\w+(\..*)#$count\1#;
	#	print "$source\n";
	#	$sth2 = $dbh->prepare("update sentence set source='$source' where sentid = $id");
	#	$sth2->execute() or die $sth2->errstr."\n";

	#}
	$count++;
}