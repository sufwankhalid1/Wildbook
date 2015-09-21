package org.ecocean.email.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.CommonConfiguration;
import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;
import org.ecocean.media.MediaSubmission;

public class NotificationMailerHelper {
    private NotificationMailerHelper() {
        // prevent instantiation
    }

    /**
       * Creates a basic tag map for the specified encounter.
       * This map can subsequently be enhanced with extra tags.
       * Individual tags included:
       * <ul>
       * <li>&#64;INDIVIDUAL_LINK&#64;</li>
       * <li>&#64;INDIVIDUAL_ID&#64;</li>
       * <li>&#64;INDIVIDUAL_ALT_ID&#64;</li>
       * <li>&#64;INDIVIDUAL_SEX&#64;</li>
       * <li>&#64;INDIVIDUAL_NAME&#64;</li>
       * <li>&#64;INDIVIDUAL_NICKNAME&#64;</li>
       * <li>&#64;INDIVIDUAL_NICKNAMER&#64;</li>
       * <li>&#64;INDIVIDUAL_COMMENTS&#64;</li>
       * </ul>
       *
       * @param req servlet request for data reference
       * @param ind MarkedIndividual for which to add tag data
       * @return map instance for tag replacement in email template
       */
      public static Map<String, String> createBasicTagMap(final HttpServletRequest req, final MarkedIndividual ind) {
        Map<String, String> map = new HashMap<>();
        NotificationMailerHelper.addTags(map, req, ind);
        return map;
      }

    /**
       * Creates a basic tag map for the specified encounter.
       * This map can subsequently be enhanced with extra tags.
       * Encounter tags included:
       * <ul>
       * <li>&#64;ENCOUNTER_LINK&#64;</li>
       * <li>&#64;ENCOUNTER_ID&#64;</li>
       * <li>&#64;ENCOUNTER_ALT_ID&#64;</li>
       * <li>&#64;ENCOUNTER_INDIVIDUALID&#64;</li>
       * <li>&#64;ENCOUNTER_DATE&#64;</li>
       * <li>&#64;ENCOUNTER_LOCATION&#64;</li>
       * <li>&#64;ENCOUNTER_LOCATIONID&#64;</li>
       * <li>&#64;ENCOUNTER_SEX&#64;</li>
       * <li>&#64;ENCOUNTER_LIFE_STAGE&#64;</li>
       * <li>&#64;ENCOUNTER_COUNTRY&#64;</li>
       * <li>&#64;ENCOUNTER_SUBMITTER_NAME&#64;</li>
       * <li>&#64;ENCOUNTER_SUBMITTER_ID&#64;</li>
       * <li>&#64;ENCOUNTER_SUBMITTER_EMAIL&#64;</li>
       * <li>&#64;ENCOUNTER_SUBMITTER_ORGANIZATION&#64;</li>
       * <li>&#64;ENCOUNTER_SUBMITTER_PROJECT&#64;</li>
       * <li>&#64;ENCOUNTER_PHOTOGRAPHER_NAME&#64;</li>
       * <li>&#64;ENCOUNTER_PHOTOGRAPHER_EMAIL&#64;</li>
       * <li>&#64;ENCOUNTER_COMMENTS&#64;</li>
       * <li>&#64;ENCOUNTER_USER&#64;</li>
       * </ul>
       *
       * @param req servlet request for data reference
       * @param enc Encounter for which to add tag data
       * @return map instance for tag replacement in email template
       */
      public static Map<String, String> createBasicTagMap(final HttpServletRequest req, final Encounter enc) {
        Map<String, String> map = new HashMap<>();
        NotificationMailerHelper.addTags(map, req, enc);
        return map;
      }

    /**
       * Creates a basic tag map for the specified MediaSubmission.
       * This map can subsequently be enhanced with extra tags.
       * MediaSubmission tags included:
       * <ul>
       * <li>&#64;MEDIASUBMISSION_LINK&#64;</li>
       * <li>&#64;MEDIASUBMISION_NAME&#64;</li>
       * <li>&#64;MEDIASUBMISION_EMAIL&#64;</li>
       * <li>&#64;MEDIASUBMISION_VERBATIMLOCATION&#64;</li>
       * <li>&#64;MEDIASUBMISION_LATITUDE&#64;</li>
       * <li>&#64;MEDIASUBMISION_LONGITUDE&#64;</li>
       * <li>&#64;MEDIASUBMISION_DESCRIPTION&#64;</li>
       * <li>&#64;MEDIASUBMISION_STATUS&#64;</li>
       * <li>&#64;MEDIASUBMISION_SUBMISSIONID&#64;</li>
       * </ul>
       *
       * @param req servlet request for data reference
       * @param ms MediaSubmission for which to add tag data
       * @return map instance for tag replacement in email template
       */
      public static Map<String, String> createBasicTagMap(final HttpServletRequest req, final MediaSubmission ms) {
        Map<String, String> map = new HashMap<>();
        addTags(map, req, ms);
        return map;
      }

    /**
       * Creates a basic tag map for the specified encounter.
       * This map can subsequently be enhanced with extra tags.
       * Tags included are the union of those added by
       * {@link NotificationMailerHelper#addTags(Map, HttpServletRequest, MarkedIndividual)}
       * and {@link NotificationMailerHelper#addTags(Map, HttpServletRequest, Encounter)}.
       *
       * @param req servlet request for data reference
       * @param ind MarkedIndividual for which to add tag data
       * @param enc Encounter for which to add tag data
       * @return map instance for tag replacement in email template
       */
      public static Map<String, String> createBasicTagMap(final HttpServletRequest req, final MarkedIndividual ind, final Encounter enc) {
        Map<String, String> map = new HashMap<>();
        NotificationMailerHelper.addTags(map, req, ind);
        NotificationMailerHelper.addTags(map, req, enc);
        return map;
      }

    /**
       * Adds info tags for the specified encounter.
       *
       * @param req servlet request for data reference
       * @param ind MarkedIndividual for which to add tag data
       * @param map map to which to add tag data
       */
      static void addTags(final Map<String, String> map, final HttpServletRequest req, final MarkedIndividual ind) {
        Objects.requireNonNull(map);
        if (!map.containsKey("@URL_LOCATION@"))
          map.put("@URL_LOCATION@", String.format("http://%s", CommonConfiguration.getURLLocation(req)));
        if (ind != null) {
          map.put("@INDIVIDUAL_LINK@", String.format("%s/individuals.jsp?number=%s", map.get("@URL_LOCATION@"), ind.getIndividualID()));
          map.put("@INDIVIDUAL_ID@", ind.getIndividualID());
          map.put("@INDIVIDUAL_ALT_ID@", ind.getAlternateID());
          map.put("@INDIVIDUAL_SEX@", ind.getSex());
          map.put("@INDIVIDUAL_NAME@", ind.getName());
          map.put("@INDIVIDUAL_NICKNAME@", ind.getNickName());
          map.put("@INDIVIDUAL_NICKNAMER@", ind.getNickNamer());
          map.put("@INDIVIDUAL_COMMENTS@", ind.getComments());
        }
      }

    /**
       * Creates a basic tag map for the specified encounter.
       * This map can subsequently be enhanced with extra tags.
       *
       * @param req servlet request for data reference
       * @param enc Encounter for which to add tag data
       * @return map instance for tag replacement in email template
       */
      static void addTags(final Map<String, String> map, final HttpServletRequest req, final Encounter enc) {
        Objects.requireNonNull(map);
        if (!map.containsKey("@URL_LOCATION@"))
          map.put("@URL_LOCATION@", String.format("http://%s", CommonConfiguration.getURLLocation(req)));
        if (enc != null) {
          // Add useful encounter fields.
          map.put("@ENCOUNTER_LINK@", String.format("%s/encounters/encounter.jsp?number=%s", map.get("@URL_LOCATION@"), enc.getCatalogNumber()));
          map.put("@ENCOUNTER_ID@", enc.getCatalogNumber());
          map.put("@ENCOUNTER_ALT_ID@", enc.getAlternateID());
          map.put("@ENCOUNTER_INDIVIDUALID@", enc.getIndividualID());
          map.put("@ENCOUNTER_DATE@", enc.getDate());
          map.put("@ENCOUNTER_LOCATION@", enc.getLocation());
          map.put("@ENCOUNTER_LOCATIONID@", enc.getLocationID());
          map.put("@ENCOUNTER_SEX@", enc.getSex());
          map.put("@ENCOUNTER_LIFE_STAGE@", enc.getLifeStage());
          map.put("@ENCOUNTER_COUNTRY@", enc.getCountry());
          map.put("@ENCOUNTER_SUBMITTER_NAME@", enc.getSubmitterName());
          map.put("@ENCOUNTER_SUBMITTER_ID@", enc.getSubmitterID());
          map.put("@ENCOUNTER_SUBMITTER_EMAIL@", enc.getSubmitterEmail());
          map.put("@ENCOUNTER_SUBMITTER_ORGANIZATION@", enc.getSubmitterOrganization());
          map.put("@ENCOUNTER_SUBMITTER_PROJECT@", enc.getSubmitterProject());
          map.put("@ENCOUNTER_PHOTOGRAPHER_NAME@", enc.getPhotographerName());
          map.put("@ENCOUNTER_PHOTOGRAPHER_EMAIL@", enc.getPhotographerEmail());
          map.put("@ENCOUNTER_COMMENTS@", enc.getComments());
          map.put("@ENCOUNTER_USER@", enc.getAssignedUsername());
        }
      }

    /**
       * Creates a basic tag map for the specified MediaSubmission.
       * This map can subsequently be enhanced with extra tags.
       *
       * @param req servlet request for data reference
       * @param ms MediaSubmission for which to add tag data
       * @return map instance for tag replacement in email template
       */
      static void addTags(final Map<String, String> map, final HttpServletRequest req, final MediaSubmission ms) {
        Objects.requireNonNull(map);
        if (!map.containsKey("@URL_LOCATION@"))
          map.put("@URL_LOCATION@", String.format("http://%s", CommonConfiguration.getURLLocation(req)));
        if (ms != null) {
          map.put("@MEDIASUBMISSION_LINK@", String.format("%s/mediaSubmissionAdmin.jsp?mediaSubmissionID=%s", map.get("@URL_LOCATION@"), ms.getSubmissionid()));
          map.put("@MEDIASUBMISION_NAME@", ms.getName());
          map.put("@MEDIASUBMISION_EMAIL@", ms.getEmail());
          map.put("@MEDIASUBMISION_VERBATIMLOCATION@", ms.getVerbatimLocation());
          map.put("@MEDIASUBMISION_LATITUDE@", Objects.toString(ms.getLatitude(), ""));
          map.put("@MEDIASUBMISION_LONGITUDE@", Objects.toString(ms.getLongitude(), ""));
          map.put("@MEDIASUBMISION_DESCRIPTION@", ms.getDescription());
          map.put("@MEDIASUBMISION_STATUS@", ms.getStatus());
          map.put("@MEDIASUBMISION_SUBMISSIONID@", ms.getSubmissionid());
          map.put("@MEDIASUBMISION_DATE@", Objects.toString(ms.getStartTime(), ""));
        }
      }

    /**
       * Splits a comma-separated string of email addresses.
       * @param cs comma-separated string of email addresses
       * @return list of strings
       */
      public static List<String> splitEmails(final String cs) {
        if (cs == null) {
          return Collections.emptyList();
        }

        // Conservative checking to avoid potential blank email entries.
        String[] sep = cs.split("\\s*,\\s*");
        List<String> list = new ArrayList<>();
        for (String s : sep) {
          String t = s.trim();
          if (!"".equals(t))
            list.add(t);
        }
        return list;
      }


}
