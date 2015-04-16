#!/usr/bin/perl

use XML::Simple;
use Data::Dumper;
use JSON;

my $xmlin = join('', <>);
my $xml = XMLin($xmlin);


my $j;
for my $k (keys %{$xml->{Document}->{Placemark}}) {
	if ($k =~ /^track/i) {
		my $c = $xml->{Document}->{Placemark}->{$k}->{MultiGeometry}->{LineString}->{coordinates};
		die "no coordinates" unless $c;
		foreach my $set (split(/\s+/s, $c)) {
#print "($set)\n";
			push(@$j, [ split(/[\s,]+/, $set) ]);
		}
		last;
	}
}

print to_json($j);

