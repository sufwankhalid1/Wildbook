#!/usr/bin/perl

#mkdir -p $TARGET_DIR/7617f993-b005-4699-800d-5bb02ac165e5/_asset_group
#mkdir $TARGET_DIR/7617f993-b005-4699-800d-5bb02ac165e5/_assets
#cp -a $TMP_ASSET_DIR'/7/0/707e578e-47a0-4a93-83bc-ab4945b43df3/7e1241f5-04d8-6056-9ed0-3ad7a2d9cef6.jpg' $TARGET_DIR/7617f993-b005-4699-800d-5bb02ac165e5/_asset_group/869737f8575babf865afda76e6d9f8cf8e9905ab8cfcd3e9b3d441bbdcc8a079
#ln -s ../_asset_group/869737f8575babf865afda76e6d9f8cf8e9905ab8cfcd3e9b3d441bbdcc8a079 $TARGET_DIR/7617f993-b005-4699-800d-5bb02ac165e5/_assets/e6f012cd-8877-4d3b-8cfb-ee415ba9ce06.jpg

print "\$TARGET_DIR=xxxxxx\n\$SOURCE_ROOT=xxxxxx\n\n";

while (<>) {
    next if (/^#/);
    chop;
    ($source_file, $target_dir, $upload_filename, $asset_filename) = split(/\t/, $_);
    printf("mkdir -p \$TARGET_DIR/%s/_uploads/\n", $target_dir);
    printf("mkdir    \$TARGET_DIR/%s/_assets/\n", $target_dir);
    printf("cp \$SOURCE_ROOT/%s \$TARGET_DIR/%s/_uploads/%s\n", $source_file, $target_dir, $upload_filename);
    printf("ln -s ../uploads/%s \$TARGET_DIR/%s/_assets/%s\n\n", $upload_filename, $target_dir, $asset_filename);
}
# source_file	target_dir	upload_filename	asset_filename
#206/Phs025Phs025L.jpg	2a06069d-6ac1-4f43-974f-12da7a8983e9	2ec29f0fa16eac6a65f21503482e32d9c2ecdcb9626699eab67c29eced96fafe	408e2e61-afdc-3986-8fed-58bf11a4384d.jpg
#e/b/ebdfcb03-17b5-4b54-b268-59a402e12572/IMG_5693_L.jpg	3c526624-fde3-46de-b31f-620f268e0748	1651b40c36f23a4746472d823591c9a34cd67efd07e1558f63a52ada926893e8	8e389550-0f49-4729-8e32-a54e15baef25.jpg
