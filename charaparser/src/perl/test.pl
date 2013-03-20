use strict;

#my $word =  "leaves";

#my $pos = getnumber($word);

#print $pos;

#my $sent = "hong's mom";
#$sent =~ s#'#\\'#g;
#print $sent; #escape

#$dbh->prepare("select * from anytable where avalue in ('a', 'b', 'c')");

my @words = ([1, 2, 3], [4, 5, 6]);

for(my $i = 0; $i <= $#words; $i++){
	for(my $j = 0; $j <=$#{$words[$i]}; $j++){	
		print "here $i, $j , $words[$i][$j]\n";
	}
}





