my $text="(3-5) 7 blades [abaxially. purple] ovate";
$text = hideBrackets($text);
print $text."\n";

#hide [\.\?\;\:\!] if they are in brackets
sub hideBrackets{
	my $text = shift;
	$text =~ s/([\(\)\[\]\{\}])/ \1 /g;

	my $lround=0;
	my $lsquare=0;
	my $lcurly=0;

	my $hidden="";

	my @tokens = split(/[\s]+/, $text);

	foreach (@tokens){
		if($_ eq "("){
			$lround++;
			$hidden .= "(";	
		}elsif($_ eq ")"){
			$lround--;
			$hidden .= ") ";
		}elsif($_ eq "["){
			$lsquare++;
			$hidden .= "[";
		}elsif($_ eq "]"){
			$lsquare--;
			$hidden .= "] ";
		}elsif($_ eq "{"){
			$lcurly++;
			$hidden .= "{";
		}elsif($_ eq "}"){
			$lcurly--;
			$hidden .= "} ";
		}else{
			if($lround+$lsquare+$lcurly>0){
				if(/.*?[\.\?\;\:\!].*?/){
					s/\./\[DOT\]/g;
					s/\?/\[QST\]/g;
					s/\;/\[SQL\]/g;
					s/\:/\[QLN\]/g;
					s/\!/\[EXM\]/g;
				}
			}
			$hidden .= $_;
			$hidden .= " ";
		}
	}	
	$hidden =~ s/([\(\[\{]\s+)/\1/g;
	$hidden =~ s/\s+([\)\]\}])/\1/g;
	return $hidden;
}