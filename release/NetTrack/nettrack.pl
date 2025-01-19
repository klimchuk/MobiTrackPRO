#!/usr/bin/perl -w
use CGI;
# Saving track
# Version 1.2
# Use at your own risk. 

my $path="/pub/home/unteh42/tracks/";
my $filename="";

print "Content-type: text/plain\n\n";

        $q=new CGI();
	
        $un=$q->param('un');
        $pw=$q->param('pw');
        $name=$q->param('name');
        $cds=$q->param('cds');
	
my $filename=$un."-".$pw."-".$name.".plt";

        open FNAME, ">$path$filename" or die("\Error creating file");
	print FNAME "OziExplorer Track Point File Version 2.0\r\nWGS 84\r\nAltitude is in Feet\r\nReserved 3\r\n0,3,2951611,$un-$name,0\r\n36\r\n";
	print FNAME "$cds";
	close FNAME or die("Error closed");

print "Ok";
