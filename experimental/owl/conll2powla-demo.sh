#!/bin/bash
# illustrates how to call conll2powla.sh

HOME=`echo $0 | sed s/'[^\/]*$'//`./; 
$HOME/conll2powla.sh $HOME/../../data/conll/2011-02-21.news.libya-civil-war-gaddafi.txt.stp WORD LEMMA POS ID HEAD EDGE
