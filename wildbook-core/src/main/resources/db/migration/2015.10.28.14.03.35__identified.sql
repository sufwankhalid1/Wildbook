alter table individuals add column identified boolean;
update individuals set identified = true where alternateid is not null;
