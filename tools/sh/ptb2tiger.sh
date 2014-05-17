#!/bin/bash
# read PTB syntax file as argument and write tiger to stdout

# 1. convert trees to xml structures
# 2. use xpath to generate tiger

HOME=`echo $0 | sed s/'[^\/\\]*$'//g`;

# 1. convert to XML tree, remove empty nodes and pseudo tags
(echo '<xml>';
sed -e s/'<'/'\&lt;'/g \
	-e s/'>'/'\&gt;'/g \
	-e s/'('/'<n>'/g \
	-e s/')'/'<\/n>'/g \
	-e s/'&'/'\&amp;'/g \
	$1;
echo '</xml>' ) | \
xmllint --recover - > test1.xml;
cat test1.xml | \
perl -e 'while(<>) { s/\n//gs; s/<n>/\n<n>/g; s/[ \t]*<\/n>/<\/n>\n/gs; print}' | \
egrep '<' | \
grep -v '*-' | # no empty elements
sed -e s/'\t'/' '/g \
	-e s/'   *'/' '/g \
	-e s/'<n>\([^< ]*\) \([^<][^<]*\)'/'<n pos="\1" word="\2">'/g \
	-e s/'\(<n[^>\/]*\)><\/n>'/'\1\/>'/g \
	-e s/'<n>\([^< ][^ <]*\)'/'<n cat="\1">'/g \
	-e s/'cat="\([^"\-][^"\-]*\)\-\([^"][^"]*\)"'/'cat="\1" edge_label="\2"'/g \
	-e s/'\(cat|pos|edge_label\)\(="[^"]*[^"0-9]\)\-[0-9]*"'/'\1\2"'/g \
	-e s/'\(edge_label\)="[0-9]*"'//g \
	-e s/'word="-LRB-"'/'word="("'/g \
	-e s/'word="-RRB-"'/'word=")"'/g \
	-e s/'<n[^>]*pos="-NONE[^>]*\/>'//g \
	-e s/'pos="[^A-Za-z]*"'//g \
	-e s/'\(="[^"]*\)"\([^=>"]*"\)'/'\1\&quot;\2'/g \
	-e s/'   *'/' '/g | \
perl -e 'while(<>) {
	s/[\t\n]/ /gs;
	s/(<n[^>]*word="[^>]*>)/\n$1/g;
	print; } ' | \
sed -e s/'\(<n[^>\/]*\)> *<\/n>'/'\1\/>'/g \
	-e s/'<n[^>]*cat="[^>]*\/>'//g \
	-e s/'\(<n[^>\/]*\)> *<\/n>'/'\1\/>'/g \
	-e s/'<n[^>]*cat="[^>]*\/>'//g \
	-e s/'\(<n[^>\/]*\)> *<\/n>'/'\1\/>'/g \
	-e s/'<n[^>]*cat="[^>]*\/>'//g \
> test2.xml;
cat test2.xml | \
#
# 2. generate TIGER XML
saxon -s:- -xsl:$HOME./xml2tiger.xsl id=$1