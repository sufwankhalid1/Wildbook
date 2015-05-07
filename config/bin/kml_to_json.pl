#!/usr/bin/perl

use XML::Simple;
use Data::Dumper;
use JSON;

my $xmlin = join('', <>);
my $xml = XMLin($xmlin);
#print Dumper($xml); exit;

my $type;
$type = 'kml' if ($xml->{xmlns} =~ /kml/);  #kinda hacky, but meh.

my $sub;
$sub = "parse_$type" if ($type =~ /^(kml)$/);

die "unknown geo file type" unless $sub;

my $h = &{$sub}($xml);

die "could not parse $type xml" unless $h;




#print Dumper($h); exit;

print to_json($h);


sub parse_kml {
	my $xml = shift;

	my $placemarks = &find_children($xml, 'Placemark');
	return unless $placemarks;

# NOTE: some opengis kml has this (useful?) property under Placemark
# <ExtendedData>
#  <SchemaData schemaUrl="#monterey">
#   <SimpleData name="OBJECTID">57207</SimpleData>
#   <SimpleData name="TRACK_STAR">2014/05/25</SimpleData>
#   <SimpleData name="TRACK_END">2014/05/25</SimpleData>
#   <SimpleData name="TRACK_UNIQ">
#      10_0J46FUmYM0_9152990b34f0fe35_v3.03.01_52520149941
#   </SimpleData>
#   <SimpleData name="BOAT_TYPE">commercial</SimpleData>
#   <SimpleData name="BOAT_NAME">sea wolf</SimpleData>
# </SchemaData>
#</ExtendedData>

	my $h;

	foreach $pm (@$placemarks) {
		my $pobj;
#print Dumper($pm);
		my $ext = &find_children($pm, 'SimpleData');
		$pobj->{meta} = $ext if $ext;  #handles case (opengis?) where we can glean data from this metadata
		#seems like we need to find LineString *first* then coordinates under that -- not just straight-up coordinates, as some kml has coordinates for start/end points
		my $ls = &find_children($pm, 'LineString');
#print Dumper($ls);
		my $coor = &find_children($ls, 'coordinates') if $ls;
		next unless $coor;
		foreach my $c (@$coor) {  #we "should" (?) only get one big coordinates string
			foreach my $set (split(/\s+/s, $c)) {
				push(@{$pobj->{geo}}, [ split(/[\s,]+/, $set) ]);
			}
		}
		push(@$h, $pobj);
	}

return $h;

=pad

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
	return $j;
=cut

}



sub find_children {
	my ($xml, $tag) = @_;

	my $c;

	#case where we need to walk through an array
	if (ref($xml) eq 'ARRAY') {
		foreach my $a (@{$xml}) {
			my $gc = &find_children($a, $tag);
			push(@$c, @{$gc}) if $gc;
		}
		return $c;
	}

	return unless (ref($xml) eq 'HASH');

	for my $k (keys %$xml) {
		if ($k eq $tag) {
			if (ref($xml->{$k}) eq 'ARRAY') {
				push(@$c, @{$xml->{$k}});
			} elsif (ref($xml->{$k}) eq 'HASH') {
				push(@$c, $xml->{$k});
			} elsif (!ref($xml->{$k})) {
				push(@$c, $xml->{$k});
			}
		} else {
			my $gc = &find_children($xml->{$k}, $tag);
			push(@$c, @{$gc}) if $gc;
		}
	}
	return $c;
}


