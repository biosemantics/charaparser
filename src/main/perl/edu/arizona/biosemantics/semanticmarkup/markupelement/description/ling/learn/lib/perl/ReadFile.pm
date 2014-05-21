package ReadFile;
#use encoding "iso-8859-1"; #latin1
#use encoding "cp1252";
use Encoding::FixLatin qw(fix_latin);

sub readfile{
	my $file = shift;
	my $content = "";
	open $f, '<:encoding(UTF-8)', $file;   # auto decoding on read 
	#open($f, "$file") || die "$!:$file\n";
	
	while($line =<$f>){
		$line = fix_latin($line);		
		$line =~ s#\r|\n# #g;
		$content .= $line;
	}		 
	$content =~ s#\s+# #g;
	return $content;
}

1;