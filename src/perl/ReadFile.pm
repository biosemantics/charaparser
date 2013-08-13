package ReadFile;
#use encoding "iso-8859-1"; #latin1
#use encoding "cp1252";
use Encoding::FixLatin qw(fix_latin);

sub readfile{
	my $file = shift;
	my $content = "";
	#open $f, '<:encoding(UTF-8)', $file;   # auto decoding on read 
	#-> there are files where different encodings are mixed in the same file (our own test files).
	# Those cause different behavior if this is run on Windows or Unix. On Windows a warning message is shown, where
	# on Unix nothing is shown at all. The warning on windows apears to causes the java portion that calls perl to stall.
	# If we do not want to set UTF8 as strict input requirement 
	# an alternative instead is to read without specifying an encoding and instead use fix_latin to convert the input by buest guess to an utf8 only string.
	open($f, "$file") || die "$!:$file\n";
	while($line =<$f>){
		$line = fix_latin($line);		
		$line =~ s#\r|\n# #g;
		$content .= $line;
	}		 
	$content =~ s#\s+# #g;
	return $content;
}

1;