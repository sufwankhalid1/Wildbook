-- Clean up any referenced assets when the asset store is deleted

alter table mediaasset
drop constraint mediaasset_store_fkey,
add constraint mediaasset_store_fkey
  foreign key (store)
  references assetstore (id)
  on delete cascade;
