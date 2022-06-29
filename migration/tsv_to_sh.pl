#!/usr/bin/perl

print "TARGET_DIR=xxxxxx\nSOURCE_ROOT=xxxxxx\n\n";

my @all_lines = <>;
my $ct = $#all_lines;
foreach (@all_lines) {
    next if (/^#/);
    chop;
    ($source_file, $target_dir, $upload_filename, $asset_filename) = split(/\t/, $_);
    die "upload_filename: $upload_filename" if ($upload_filename =~ /"/);
    print "echo $ct\n" if ($ct % 100 == 0);
    printf("mkdir -p \$TARGET_DIR/%s/_uploads/\n", $target_dir);
    printf("mkdir -p \$TARGET_DIR/%s/_assets/\n", $target_dir);
    printf("cp \"\$SOURCE_ROOT/%s\" \$TARGET_DIR/%s/_uploads/%s\n", $source_file, $target_dir, $upload_filename);
    printf("ln -s ../uploads/%s \$TARGET_DIR/%s/_assets/%s\n\n", $upload_filename, $target_dir, $asset_filename);
    $ct--;
}

