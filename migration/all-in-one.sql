
-- setup version on OCCURRENCE

ALTER TABLE "OCCURRENCE" ADD COLUMN "VERSION" BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000);
UPDATE "OCCURRENCE" SET "VERSION" = "MILLIS" WHERE "MILLIS" IS NOT NULL;


-- Occurrence.occurrenceID => .id

BEGIN;

ALTER TABLE "OCCURRENCE" RENAME COLUMN "OCCURRENCEID" TO "ID";

ALTER TABLE "OCCURRENCE_ASSETS" DROP CONSTRAINT "OCCURRENCE_ASSETS_FK1";
ALTER TABLE "OCCURRENCE_ASSETS" RENAME COLUMN "OCCURRENCEID_OID" TO "ID_OID";
ALTER TABLE "OCCURRENCE_ASSETS" ADD CONSTRAINT "OCCURRENCE_ASSETS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OCCURRENCE_BEHAVIORS" DROP CONSTRAINT "OCCURRENCE_BEHAVIORS_FK1";
ALTER TABLE "OCCURRENCE_BEHAVIORS" RENAME COLUMN "OCCURRENCEID_OID" TO "ID_OID";
ALTER TABLE "OCCURRENCE_BEHAVIORS" ADD CONSTRAINT "OCCURRENCE_BEHAVIORS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OCCURRENCE_ENCOUNTERS" DROP CONSTRAINT "OCCURRENCE_ENCOUNTERS_FK1";
ALTER TABLE "OCCURRENCE_ENCOUNTERS" RENAME COLUMN "OCCURRENCEID_OID" TO "ID_OID";
ALTER TABLE "OCCURRENCE_ENCOUNTERS" ADD CONSTRAINT "OCCURRENCE_ENCOUNTERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OCCURRENCE_INFORMOTHERS" DROP CONSTRAINT "OCCURRENCE_INFORMOTHERS_FK1";
ALTER TABLE "OCCURRENCE_INFORMOTHERS" RENAME COLUMN "OCCURRENCEID_OID" TO "ID_OID";
ALTER TABLE "OCCURRENCE_INFORMOTHERS" ADD CONSTRAINT "OCCURRENCE_INFORMOTHERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OCCURRENCE_OBSERVATIONS" DROP CONSTRAINT "OCCURRENCE_OBSERVATIONS_FK1";
ALTER TABLE "OCCURRENCE_OBSERVATIONS" RENAME COLUMN "OCCURRENCEID_OID" TO "ID_OID";
ALTER TABLE "OCCURRENCE_OBSERVATIONS" ADD CONSTRAINT "OCCURRENCE_OBSERVATIONS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OCCURRENCE_SUBMITTERS" DROP CONSTRAINT "OCCURRENCE_SUBMITTERS_FK1";
ALTER TABLE "OCCURRENCE_SUBMITTERS" RENAME COLUMN "OCCURRENCEID_OID" TO "ID_OID";
ALTER TABLE "OCCURRENCE_SUBMITTERS" ADD CONSTRAINT "OCCURRENCE_SUBMITTERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OCCURRENCE_TAXONOMIES" DROP CONSTRAINT "OCCURRENCE_TAXONOMIES_FK1";
ALTER TABLE "OCCURRENCE_TAXONOMIES" RENAME COLUMN "OCCURRENCEID_OID" TO "ID_OID";
ALTER TABLE "OCCURRENCE_TAXONOMIES" ADD CONSTRAINT "OCCURRENCE_TAXONOMIES_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") DEFERRABLE INITIALLY DEFERRED;

END;


-- Encounter.catalogNumber => .id

BEGIN;

ALTER TABLE "ENCOUNTER" RENAME COLUMN "CATALOGNUMBER" TO "ID";


ALTER TABLE "ENCOUNTER_ANNOTATIONS" DROP CONSTRAINT "ENCOUNTER_ANNOTATIONS_FK1";
ALTER TABLE "ENCOUNTER_ANNOTATIONS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_ANNOTATIONS" ADD CONSTRAINT "ENCOUNTER_ANNOTATIONS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID") ON DELETE CASCADE ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_IMAGES" DROP CONSTRAINT "ENCOUNTER_IMAGES_FK1";
ALTER TABLE "ENCOUNTER_IMAGES" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_IMAGES" ADD CONSTRAINT "ENCOUNTER_IMAGES_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_INFORMOTHERS" DROP CONSTRAINT "ENCOUNTER_INFORMOTHERS_FK1";
ALTER TABLE "ENCOUNTER_INFORMOTHERS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_INFORMOTHERS" ADD CONSTRAINT "ENCOUNTER_INFORMOTHERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_LEFTREFERENCESPOTS" DROP CONSTRAINT "ENCOUNTER_LEFTREFERENCESPOTS_FK1";
ALTER TABLE "ENCOUNTER_LEFTREFERENCESPOTS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_LEFTREFERENCESPOTS" ADD CONSTRAINT "ENCOUNTER_LEFTREFERENCESPOTS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_MEASUREMENTS" DROP CONSTRAINT "ENCOUNTER_MEASUREMENTS_FK1";
ALTER TABLE "ENCOUNTER_MEASUREMENTS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_MEASUREMENTS" ADD CONSTRAINT "ENCOUNTER_MEASUREMENTS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_METALTAGS" DROP CONSTRAINT "ENCOUNTER_METALTAGS_FK1";
ALTER TABLE "ENCOUNTER_METALTAGS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_METALTAGS" ADD CONSTRAINT "ENCOUNTER_METALTAGS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_OBSERVATIONS" DROP CONSTRAINT "ENCOUNTER_OBSERVATIONS_FK1";
ALTER TABLE "ENCOUNTER_OBSERVATIONS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_OBSERVATIONS" ADD CONSTRAINT "ENCOUNTER_OBSERVATIONS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_PHOTOGRAPHERS" DROP CONSTRAINT "ENCOUNTER_PHOTOGRAPHERS_FK1";
ALTER TABLE "ENCOUNTER_PHOTOGRAPHERS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_PHOTOGRAPHERS" ADD CONSTRAINT "ENCOUNTER_PHOTOGRAPHERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_RIGHTREFERENCESPOTS" DROP CONSTRAINT "ENCOUNTER_RIGHTREFERENCESPOTS_FK1";
ALTER TABLE "ENCOUNTER_RIGHTREFERENCESPOTS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_RIGHTREFERENCESPOTS" ADD CONSTRAINT "ENCOUNTER_RIGHTREFERENCESPOTS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_RIGHTSPOTS" DROP CONSTRAINT "ENCOUNTER_RIGHTSPOTS_FK1";
ALTER TABLE "ENCOUNTER_RIGHTSPOTS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_RIGHTSPOTS" ADD CONSTRAINT "ENCOUNTER_RIGHTSPOTS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_SPOTS" DROP CONSTRAINT "ENCOUNTER_SPOTS_FK1" ;
ALTER TABLE "ENCOUNTER_SPOTS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_SPOTS" ADD CONSTRAINT "ENCOUNTER_SPOTS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_SUBMITTERS" DROP CONSTRAINT "ENCOUNTER_SUBMITTERS_FK1";
ALTER TABLE "ENCOUNTER_SUBMITTERS" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_SUBMITTERS" ADD CONSTRAINT "ENCOUNTER_SUBMITTERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ENCOUNTER_TISSUESAMPLES" DROP CONSTRAINT "ENCOUNTER_TISSUESAMPLES_FK1";
ALTER TABLE "ENCOUNTER_TISSUESAMPLES" RENAME COLUMN "CATALOGNUMBER_OID" TO "ID_OID";
ALTER TABLE "ENCOUNTER_TISSUESAMPLES" ADD CONSTRAINT "ENCOUNTER_TISSUESAMPLES_FK1" FOREIGN KEY ("ID_OID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "IMPORTTASK_ENCOUNTERS" DROP CONSTRAINT "IMPORTTASK_ENCOUNTERS_FK2";
ALTER TABLE "IMPORTTASK_ENCOUNTERS" RENAME COLUMN "CATALOGNUMBER_EID" TO "ID_EID";
ALTER TABLE "IMPORTTASK_ENCOUNTERS" ADD CONSTRAINT "IMPORTTASK_ENCOUNTERS_FK2" FOREIGN KEY ("ID_EID") REFERENCES "ENCOUNTER"("ID")  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "MARKEDINDIVIDUAL_ENCOUNTERS" DROP CONSTRAINT "MARKEDINDIVIDUAL_ENCOUNTERS_FK2";
ALTER TABLE "MARKEDINDIVIDUAL_ENCOUNTERS" RENAME COLUMN "CATALOGNUMBER_EID" TO "ID_EID";
ALTER TABLE "MARKEDINDIVIDUAL_ENCOUNTERS" ADD CONSTRAINT "MARKEDINDIVIDUAL_ENCOUNTERS_FK2" FOREIGN KEY ("ID_EID") REFERENCES "ENCOUNTER"("ID") ON DELETE CASCADE  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OCCURRENCE_ENCOUNTERS" DROP CONSTRAINT "OCCURRENCE_ENCOUNTERS_FK2";
ALTER TABLE "OCCURRENCE_ENCOUNTERS" RENAME COLUMN "CATALOGNUMBER_EID" TO "ID_EID";
ALTER TABLE "OCCURRENCE_ENCOUNTERS" ADD CONSTRAINT "OCCURRENCE_ENCOUNTERS_FK2" FOREIGN KEY ("ID_EID") REFERENCES "ENCOUNTER"("ID") ON DELETE CASCADE  ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

END;

-- now we have to do away with non-uuid ids on Encounters, sigh

ALTER TABLE "ENCOUNTER" ADD COLUMN tmp_non_uuid TEXT;

BEGIN;

UPDATE "ENCOUNTER" SET tmp_non_uuid = "ID" WHERE LENGTH("ID") != 36 OR "ID" NOT LIKE '%-%-%-%';

-- this one *must* happen first
UPDATE "ENCOUNTER" SET "OTHERCATALOGNUMBERS" = CONCAT("OTHERCATALOGNUMBERS", ', ', tmp_non_uuid) WHERE tmp_non_uuid IS NOT NULL AND "OTHERCATALOGNUMBERS" IS NOT NULL;
UPDATE "ENCOUNTER" SET "OTHERCATALOGNUMBERS" = tmp_non_uuid WHERE tmp_non_uuid IS NOT NULL AND "OTHERCATALOGNUMBERS" IS NULL;

-- this should affect all fk refs as well, thanks to ON UPDATE CASCADE previously added
UPDATE "ENCOUNTER" SET "ID" = uuid_generate_v4() WHERE tmp_non_uuid IS NOT NULL;

END;


ALTER TABLE "ENCOUNTER" DROP COLUMN tmp_non_uuid;





-- MarkedIndividual.individualID => .id

BEGIN;

ALTER TABLE "MARKEDINDIVIDUAL" RENAME COLUMN "INDIVIDUALID" TO "ID";


ALTER TABLE "MARKEDINDIVIDUAL_ENCOUNTERS" DROP CONSTRAINT "MARKEDINDIVIDUAL_ENCOUNTERS_FK1";
ALTER TABLE "MARKEDINDIVIDUAL_ENCOUNTERS" RENAME COLUMN "INDIVIDUALID_OID" TO "ID_OID";
ALTER TABLE "MARKEDINDIVIDUAL_ENCOUNTERS" ADD CONSTRAINT "MARKEDINDIVIDUAL_ENCOUNTERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "MARKEDINDIVIDUAL"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;


ALTER TABLE "RELATIONSHIP" DROP CONSTRAINT "RELATIONSHIP_FK1";
-- no RENAME needed, i think
ALTER TABLE "RELATIONSHIP" ADD CONSTRAINT "RELATIONSHIP_FK1" FOREIGN KEY ("MARKEDINDIVIDUALNAME1") REFERENCES "MARKEDINDIVIDUAL"("ID") DEFERRABLE INITIALLY DEFERRED;


ALTER TABLE "RELATIONSHIP" DROP CONSTRAINT "RELATIONSHIP_FK2";
-- no RENAME needed, i think
ALTER TABLE "RELATIONSHIP" ADD CONSTRAINT "RELATIONSHIP_FK2" FOREIGN KEY ("MARKEDINDIVIDUALNAME2") REFERENCES "MARKEDINDIVIDUAL"("ID") DEFERRABLE INITIALLY DEFERRED;


ALTER TABLE "RELATIONSHIP" DROP CONSTRAINT "RELATIONSHIP_FK3";
ALTER TABLE "RELATIONSHIP" RENAME COLUMN "INDIVIDUAL1_INDIVIDUALID_OID" TO "INDIVIDUAL1_ID_OID";
ALTER TABLE "RELATIONSHIP" ADD CONSTRAINT "RELATIONSHIP_FK3" FOREIGN KEY ("INDIVIDUAL1_ID_OID") REFERENCES "MARKEDINDIVIDUAL"("ID") DEFERRABLE INITIALLY DEFERRED;


ALTER TABLE "RELATIONSHIP" DROP CONSTRAINT "RELATIONSHIP_FK4";
ALTER TABLE "RELATIONSHIP" RENAME COLUMN "INDIVIDUAL2_INDIVIDUALID_OID" TO "INDIVIDUAL2_ID_OID";
ALTER TABLE "RELATIONSHIP" ADD CONSTRAINT "RELATIONSHIP_FK4" FOREIGN KEY ("INDIVIDUAL2_ID_OID") REFERENCES "MARKEDINDIVIDUAL"("ID") DEFERRABLE INITIALLY DEFERRED;


END;


BEGIN;

ALTER TABLE "WILDBOOKSCHEDULEDTASK" DROP CONSTRAINT "WILDBOOKSCHEDULEDTASK_FK1";
ALTER TABLE "WILDBOOKSCHEDULEDTASK" RENAME COLUMN "PRIMARYINDIVIDUAL_INDIVIDUALID_OID" TO "PRIMARYINDIVIDUAL_ID_OID";
ALTER TABLE "WILDBOOKSCHEDULEDTASK" ADD CONSTRAINT "WILDBOOKSCHEDULEDTASK_FK1" FOREIGN KEY ("PRIMARYINDIVIDUAL_ID_OID") REFERENCES "MARKEDINDIVIDUAL"("ID") DEFERRABLE INITIALLY DEFERRED;


ALTER TABLE "WILDBOOKSCHEDULEDTASK" DROP CONSTRAINT "WILDBOOKSCHEDULEDTASK_FK2";
ALTER TABLE "WILDBOOKSCHEDULEDTASK" RENAME COLUMN "SECONDARYINDIVIDUAL_INDIVIDUALID_OID" TO "SECONDARYINDIVIDUAL_ID_OID";
ALTER TABLE "WILDBOOKSCHEDULEDTASK" ADD CONSTRAINT "WILDBOOKSCHEDULEDTASK_FK2" FOREIGN KEY ("SECONDARYINDIVIDUAL_ID_OID") REFERENCES "MARKEDINDIVIDUAL"("ID") DEFERRABLE INITIALLY DEFERRED;


END;


-- this section populates Occurrence.alternateId with (old) ID when ID is not a UUID

BEGIN;

ALTER TABLE "OCCURRENCE" ALTER COLUMN "ID" SET DEFAULT uuid_generate_v4();

-- now we "store" legacy values in ALTERNATEID
ALTER TABLE "OCCURRENCE" ADD COLUMN "ALTERNATEID" TEXT;
CREATE INDEX "OCCURRENCE_ALTERNATEID_idx" ON "OCCURRENCE" ("ALTERNATEID");
UPDATE "OCCURRENCE" SET "ALTERNATEID" = "ID";

-- this allows us to alter id primary key on occurrence (thanks to added CASCADE)
ALTER TABLE "OCCURRENCE_ENCOUNTERS" DROP CONSTRAINT "OCCURRENCE_ENCOUNTERS_FK1";
ALTER TABLE "OCCURRENCE_ENCOUNTERS" ADD CONSTRAINT "OCCURRENCE_ENCOUNTERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OCCURRENCE_ASSETS" DROP CONSTRAINT "OCCURRENCE_ASSETS_FK1";
ALTER TABLE "OCCURRENCE_ASSETS" ADD CONSTRAINT "OCCURRENCE_ASSETS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OCCURRENCE_BEHAVIORS" DROP CONSTRAINT "OCCURRENCE_BEHAVIORS_FK1";
ALTER TABLE "OCCURRENCE_BEHAVIORS" ADD CONSTRAINT "OCCURRENCE_BEHAVIORS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OCCURRENCE_INFORMOTHERS" DROP CONSTRAINT "OCCURRENCE_INFORMOTHERS_FK1";
ALTER TABLE "OCCURRENCE_INFORMOTHERS" ADD CONSTRAINT "OCCURRENCE_INFORMOTHERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OCCURRENCE_OBSERVATIONS" DROP CONSTRAINT "OCCURRENCE_OBSERVATIONS_FK1";
ALTER TABLE "OCCURRENCE_OBSERVATIONS" ADD CONSTRAINT "OCCURRENCE_OBSERVATIONS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OCCURRENCE_SUBMITTERS" DROP CONSTRAINT "OCCURRENCE_SUBMITTERS_FK1";
ALTER TABLE "OCCURRENCE_SUBMITTERS" ADD CONSTRAINT "OCCURRENCE_SUBMITTERS_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OCCURRENCE_TAXONOMIES" DROP CONSTRAINT "OCCURRENCE_TAXONOMIES_FK1";
ALTER TABLE "OCCURRENCE_TAXONOMIES" ADD CONSTRAINT "OCCURRENCE_TAXONOMIES_FK1" FOREIGN KEY ("ID_OID") REFERENCES "OCCURRENCE"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "TAXONOMY" DROP CONSTRAINT "TAXONOMY_FK1";
ALTER TABLE "TAXONOMY" ADD CONSTRAINT "TAXONOMY_FK1" FOREIGN KEY ("TAXONOMIES_OCCURRENCEID_OWN") REFERENCES "OCCURRENCE"("ID") ON UPDATE CASCADE DEFERRABLE INITIALLY DEFERRED;


-- whoa, lets assign random uuids to where needed
UPDATE "OCCURRENCE" SET "ID" = uuid_generate_v4() WHERE LENGTH("ID") != 36 OR "ID" NOT LIKE '%-%-%-%';

-- blank out alternate id where it is uuid
UPDATE "OCCURRENCE" SET "ALTERNATEID" = null WHERE LENGTH("ALTERNATEID") = 36 AND "ALTERNATEID" LIKE '%-%-%-%';

-- update encounter.occurrenceID to reflect new occurrence.id
UPDATE "ENCOUNTER" SET "OCCURRENCEID" = (SELECT "ID" FROM "OCCURRENCE" where "ALTERNATEID" = "ENCOUNTER"."OCCURRENCEID") WHERE LENGTH("OCCURRENCEID") != 36 OR "OCCURRENCEID" NOT LIKE '%-%-%-%';

END;


-- these relationship bits add a little sanity to messy data and set some not-null constraints where (i think) they should be

BEGIN;
UPDATE "RELATIONSHIP" SET "INDIVIDUAL1_ID_OID" = "MARKEDINDIVIDUALNAME1" WHERE ("INDIVIDUAL1_ID_OID" != "MARKEDINDIVIDUALNAME1" OR "INDIVIDUAL1_ID_OID" IS NULL);
UPDATE "RELATIONSHIP" SET "INDIVIDUAL2_ID_OID" = "MARKEDINDIVIDUALNAME2" WHERE ("INDIVIDUAL2_ID_OID" != "MARKEDINDIVIDUALNAME2" OR "INDIVIDUAL2_ID_OID" IS NULL);
-- we might need to check for case where MARKEDINDIVIDUALNAMEx is null as well?
END;

BEGIN;
ALTER TABLE "RELATIONSHIP" ALTER COLUMN "MARKEDINDIVIDUALNAME1" SET NOT NULL;
ALTER TABLE "RELATIONSHIP" ALTER COLUMN "MARKEDINDIVIDUALNAME2" SET NOT NULL;
ALTER TABLE "RELATIONSHIP" ALTER COLUMN "MARKEDINDIVIDUALROLE1" SET NOT NULL;
ALTER TABLE "RELATIONSHIP" ALTER COLUMN "MARKEDINDIVIDUALROLE2" SET NOT NULL;
ALTER TABLE "RELATIONSHIP" ALTER COLUMN "TYPE" SET NOT NULL;
ALTER TABLE "RELATIONSHIP" ALTER COLUMN "INDIVIDUAL1_ID_OID" SET NOT NULL;
ALTER TABLE "RELATIONSHIP" ALTER COLUMN "INDIVIDUAL2_ID_OID" SET NOT NULL;
--this is what incorrectly migrated old-world db might look like:
--ALTER TABLE "RELATIONSHIP" ALTER COLUMN "INDIVIDUAL1_INDIVIDUALID_OID" SET NOT NULL;
--ALTER TABLE "RELATIONSHIP" ALTER COLUMN "INDIVIDUAL2_INDIVIDUALID_OID" SET NOT NULL;
END;

