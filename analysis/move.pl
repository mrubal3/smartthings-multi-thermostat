#!/usr/bin/env perl

use 5.012;
use strict;
use warnings;
use Date::Manip;
use JSON;

my @measurements;

while (<>) {
    chomp;
    my ( $date_str, $temp ) = split /,/, $_;
    my ( $millis ) = $_ =~ /\.(\d{3})\s/;

    my $unix_time = UnixDate( ParseDate($date_str), "%s" );
    $unix_time *= 1000 + $millis;

    # say "Time: $unix_time, Temp: $temp";
    push @measurements, { compId => "Office Sensor", streamId => "temperature", data => $temp, time => $unix_time };
}

print encode_json \@measurements;
