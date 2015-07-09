-- Clean up child assets when the parent is deleted

alter table mediaasset
drop constraint mediaasset_parent_fkey,
add constraint mediaasset_parent_fkey
  foreign key (parent)
  references mediaasset (id)
  on delete cascade;
