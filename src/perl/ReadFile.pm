package ReadFile;
#use encoding "iso-8859-1"; #latin1
#use encoding "cp1252";

sub readfile{
	my $file = shift;
	my $content = "";
	open $f, '<:encoding(UTF-8)', $file;  # auto decoding on read
	#open(F, "$file") || die "$!:$file\n";
	while($line =<$f>){
		$line =~ s#\r|\n# #g;
		$content .= $line;
	}		 
	$content =~ s#\s+# #g;
	return $content;
}

1;