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

#my $line = "Cypselae mostly columnar, ellipsoid, obpyramidal, or prismatic, seldom clavate ( if clavate, not distally stipitate_glandular ).";
#my $line = "blades (1- or obscurely rought 3(-5)-nerved) obovate to oblanceolate, blah blah blah";
#my $line = "plagio- , dicho- , or tricho-triaenes";
#my $line = "Stems usually 1, thinly to densely gray- or white-tomentose, sometimes ï¿½ glabrate;";
#my $line = " abaxial faces usually Â± densely gray- or white-tomentose with felted arachnoid trichomes,";
#my $line = "Corollas white to faintly pink- or lilac-tinged.";
#my $line ="some not readily assignable to usual ray- and disc-floret categories,";
#my $line =" (rarely with 2 teeth opposite the 3- or 4-toothed laminae ).";
#my $line = "Corollas white, sometimes abaxially rose- or purple-veined;";
#my $line ="abaxial faces densely gray- or white-tomentose, adaxial faces green, glandular-scabrous.";
#my $line ="procumbent (ï¿½ woolly-tomentose, sometimes stipitate- or sessile-glandular ).";
#my $line ="minutely stipitate- or sessile-glandular beneath other in-duments.";
#my $line ="adaxial minutely stipitate- or sessile-glandular, otherwise glabrous or glabrate";
#my $line = "usually stipitate- or sessile-glandular as well ";
#my $line ="both faces usually stipitate- or sessile-glandular.";
#my $line ="Cypselae obpyramidal (4-, sometimes 5-angled, each face usually)";
#my $line ="blades (1-), 3-, or (5-)nerved, elliptic to lanceolate,";
#my $line ="arrays (wand-, club-, or secund cone-shaped )or in axillary clusters.";
#my $line="sometimes with weakly developed, non- or weakly spinulose projection, gland-dotted,"; #didn't handle
#my $line ="blades (1- or obscurely 3-nerved )obovate to oblanceolate, 20ΓÇô35 ├ù 3ΓÇô15 mm, distally reduced and narrowed, bases cuneate, margins irregularly incised to coarsely serrate or 2-serrate, faces glabrous, gland-dotted, resinous.";
#my $line = "blades (1-), 3-, or (5-)nerved, elliptic to lanceolate, 7â€“30 Ã— 0.5â€“2.5 (â€“4)cm, bases cuneate to attenuate, margins entire (usually ciliate ).";
#my $line = "(corollas lemon- or golden yellow)laminae narrowly oblong, 6â€“7 mm.";
my $line = "Cypselae (black or brown)± compressed or flattened, often 3- or 4-angled or biconvex, ± cuneiform in silhouette";
#print stdout $line."\n";
my $connectors = "and|or|plus|to|sometimes";
$line = normalizeBrokenWords($line);
#print stdout $line."\n";

#normalize $line: "... plagio-, dicho-, and/or/plus trichotriaenes ..." => "... plagiotriaenes, dichotriaenes, and trichotriaenes ..."
#normalize $line: "... plagio- and/or/plus trichotriaenes ..." => "... plagiotriaenes and trichotriaenes ..."
#normalize $line: "palm- or fern-like" => "palm-like or fern-like"
#"blades (1- or obscurely 3-nerved) obovate to oblanceolate, blah blah blah";

sub normalizeBrokenWords{
	
	my $line = shift;
	my $cline = $line;
	$line =~ s#([(\[{])(?=[a-zA-Z])#$1 #g; #add space to () that enclose text strings (not numbers such as 3-(5))
	$line =~ s#(?<=[a-zA-Z])([)\]}])# $1#g;
	my $result = "";
	my $needsfix = 0;
	while($line=~/(.*?\b)((\w+\s*-\s*\)?,.*?\b)((?:$connectors)\s+.*))/ || $line=~/(.*?\b)((\w+\s*-\s+)((?:$connectors)\s+.*))/){
		my @completed = completeWords($2, $3, $4);
		$result .= $1.$completed[0]." ";
		$line = $completed[1];
		$needsfix = 1;	
	}
	$result .= $line;
	$result =~ s#\s+# #g;
	$result =~ s#([(\[{])\s+#$1#g;
	$result =~ s#\s+([)\]},;\.])#$1#g;
	$result =~ s#(^\s+|\s+$)##g; #trim
	$cline =~ s#(^\s+|\s+$)##g; #trim
	if($needsfix and $cline ne $result){
		 print STDOUT "broken words normalization: [$cline] to \n";
		 print STDOUT "borken words normalization: [$result] \n";
	};
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
	my $last = 0;
	for(my $i = 0; $i<@tokens; $i++){
		if($last){ last;}
		if($tokens[$i]=~/[,\.;:?!]/){#encounter a punct mark, then this is the last token tested
			$last=1;
		}
		if($tokens[$i] =~/^($connectors)$/){
			next;
		}elsif($tokens[$i]=~/-/){ #use token to complete the segment
			my $missing = $tokens[$i];
			$missing =~ s#.*-##; #greedy to find the last "-" in the token
			$missing =~ s#[[:punct:]]+$##; #remove trailing punct marks
			$missing =~ s#^[[:punct:]]+##; #remove leading punct marks
			$seg =~ s#-#-$missing#g; #attach the missing part to all segs
			$result[0] = join(' ', $seg, splice(@tokens, 0, $i));
			$result[1] = join(' ', @tokens);
			return @result;			
		}else{
			for(my $j = 1; $j < length($tokens[$i])-4; $j++){#shrink token letter by letter from the front
				my $missing = substr($tokens[$i], $j);
				$missing =~ s#[[:punct:]]+$##; #remove trailing punct marks
				$missing =~ s#^[[:punct:]]+##; #remove leading punct marks: (5-)nerved )nerved
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

