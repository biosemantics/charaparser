use strict;
use DBI;
use utf8;

my $prefix="fnav20";
my $user="root";
my $password="root";
my $host="localhost";
my $port="3306";
my $dbh = DBI->connect("DBI:mysql:host=$host;port=$port", $user, $password,  {mysql_enable_utf8 => 1 })
or die DBI::errstr."\n";
$dbh->{RaiseError} = 1;
$dbh->do('SET NAMES utf8');

my $test = $dbh->prepare('use markedupdatasets_thomas_cp');
$test->execute() or die $test->errstr."\n";

my $stmt = "select count(*) from ".$prefix."_allwords where word ='my'";
	my $sth = $dbh->prepare($stmt);
	$sth->execute() or die "error: ". $dbh->errstr."\n"; 

my $line = "Cypselae mostly columnar, ellipsoid, obpyramidal, or prismatic, seldom clavate ( if clavate, not distally stipitate_glandular ).";
#my $line = "blades (1 or obscurely 3-nerved) obovate to oblanceolate, blah blah blah";
#my $line = "plagio- , dicho- , or tricho-triaenes";
print stdout $line."\n";
$line = normalizeBrokenWords($line);
print stdout $line."\n";

#normalize $line: "... plagio-, dicho-, and/or/plus trichotriaenes ..." => "... plagiotriaenes, dichotriaenes, and trichotriaenes ..."
#normalize $line: "... plagio- and/or/plus trichotriaenes ..." => "... plagiotriaenes and trichotriaenes ..."
#normalize $line: "palm- or fern-like" => "palm-like or fern-like"
#"blades (1- or obscurely 3-nerved) obovate to oblanceolate, blah blah blah";
sub normalizeBrokenWords{
	my $line = shift;
	$line =~ s#([(\[{])#$1 #g;
	$line =~ s#([)\]}])# $1#g;
	my $result = "";
	while($line=~/(.*?\b)((\w+\s*-\s*,.*?\b)((?:and|or|plus|to)\s+.*))/ || $line=~/(.*?\b)((\w+\s*-\s+)((?:and|or|plus|to)\s+.*))/){
		my @completed = completeWords($2, $3, $4);
		$result .= $1.$completed[0]." ";
		$line = $completed[1];	
	}
	$result .= $line;
	$result =~ s#\s+# #g;
	$result =~ s#([(\[{])\s+#$1#g;
	$result =~ s#\s+([)\]}])#$1#g;
	return $result;
}

#normalize $text: "plagio- , dicho- , and/or/plus/to trichotriaenes" => "plagiotriaenes, dichotriaenes, and trichotriaenes"
#normalize $text: "plagio- and/or/plus/to trichotriaenes" => "plagiotriaenes and trichotriaenes"
#"blades (1- or obscurely 3-nerved) obovate to oblanceolate, blah blah blah";
#return [0]: completed token, [1]: rest of the text to be processed
sub completeWords{
	my $text = shift; #text starting with the segments.
	my $seg = shift; #segments
	my $later = shift; #text starting with and|or|plus|to 
	my @result = ();
	$result[0] = $seg;
	$result[1] = $later;
	my @incompletewords = split(/\s*-\s*,?/, $seg);
	#search through the tokens one by one
	my @tokens = split(/\s+/, $later);
	for(my $i = 0; $i<@tokens; $i++){
		if($tokens[$i]!~/\w/){
			last;
		}elsif($tokens[$i] =~/and|or|plus|to/){
			next;
		}elsif($tokens[$i]=~/-/){ #use token to complete the segment
			my $missing = $tokens[$i];
			$missing =~ s#.*?-##;
			$seg =~ s#-#-$missing#g; #attach the missing part to all segs
			$result[0] = join(' ', $seg, splice(@tokens, 0, $i));
			$result[1] = join(' ', @tokens);
			return @result;			
		}else{
			for(my $j = 1; $j < length($tokens[$i]); $j++){#shrink token letter by letter from the front
				my $missing = substr($tokens[$i], $j);
				if(inCorpus($missing) || oneFixedWordExists($missing, @incompletewords)){#found missing part
					$seg =~ s#-#$missing#g; #attach the missing part to all segs
					$result[0] = join(' ', $seg,splice(@tokens, 0, $i)) ;
					$result[1] = join(' ', @tokens);
					return @result;	
				}
			}
		}	
	}
	#failed
	return @result;
}

sub oneFixedWordExists{
	my $missing =shift;
	my @incompletewords = @_;
	for my $incword (@incompletewords){
		if(inCorpus($incword.$missing)){
			return 1;
		}
	}
	return 0;
}

sub inCorpus{
	my $word = shift;
	my ($stmt, $sth);
	$stmt = "select count(*) from ".$prefix."_allwords where word ='".$word."'";
	$sth = $dbh->prepare($stmt);
	$sth->execute(); 
	if ($dbh->err){
		print STDOUT "db error in querying allwords table: ".$dbh->errstr."\n";
	}else{
		my ($count) = $sth->fetchrow_array();
		if($count >=1){
			return 1;
		}
	}
	return 0;
}

#my $word =  "leaves";

#my $pos = getnumber($word);

#print $pos;

#my $sent = "hong's mom";
#$sent =~ s#'#\\'#g;
#print $sent; #escape

#$dbh->prepare("select * from anytable where avalue in ('a', 'b', 'c')");

#my @words = ([1, 2, 3], [4, 5, 6]);
#
#for(my $i = 0; $i <= $#words; $i++){
#	for(my $j = 0; $j <=$#{$words[$i]}; $j++){	
#		print "here $i, $j , $words[$i][$j]\n";
#	}
#}

