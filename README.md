<img src="http://biosemantics.github.io/charaparser/CP_Logo.jpg">

Charaparser
===========
CharaParser is a NLP tool which processes morphological descriptions of the biodiversity domain.
The output of CharaParser is a structured description of structures, their characters and relations between structures
in XML format.

Relevant Publications 
---------------------

1. Cui, H., Boufford, D., & Selden, P. (2010). Semantic annotation of biosystematics literature without training examples. Journal of American Society of Information Science and Technology. 61 (3): 522-542.http://onlinelibrary.wiley.com/doi/10.1002/asi.21246/full

2. Cui, H. (2012). CharaParser for fine-grained semantic annotation of organism morphological descriptions. Journal of American Society of Information Science and Technology. 63(4) DOI: 10.1002/asi.22618 http://onlinelibrary.wiley.com/doi/10.1002/asi.22618/pdf

If you use CharaParser in your research/work, please cite the above publications.

License
-------

   Copyright 2013 CharaParser Authors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


Project Page
----------
More information is available on the <a href="http://etc-project.org/">project page</a>.

Contribution
----------
If you want to contribute, the source is built using Maven and AspectJ.
In Eclipse you can therefore use:
* m2e - Maven Integration for Eclipse (e.g. for Juno version: http://download.eclipse.org/releases/juno)
* AspectJ Development Tools (http://download.eclipse.org/tools/ajdt/42/update)
* Maven Integration for AJDT (http://dist.springsource.org/release/AJDT/configurator/)

and configure your Eclipse project to be a Maven and AspectJ Project.

Sources are built for Java compiler compliance level 1.7.

Please [configure your git](http://git-scm.com/book/en/Customizing-Git-Git-Configuration) for this repository as:
* `core.autocrlf` true if you are on Windows 
* or `core.autocrlf input` if you are on a Unix-type OS

To get started, an architectural overview can be found [here](https://github.com/biosemantics/charaparser/blob/master/ARCHITECTURE.md).

Software Dependencies
----------
In its current version, CharaParser dependes on the following additional Software.
* MySQL
* Perl (including modules DBI, DBD::MySQL, Encoding::FixLatin)
* WordNet

