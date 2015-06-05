package org.ecocean.rest;

import org.ecocean.Shepherd;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.media.MediaTag;

import javax.validation.Valid;
import javax.jdo.*;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping(value = "/obj/mediatag")
public class MediaTagController {

//    private final UserService userService;
//
//    @Inject
//    public UserController(final UserService userService) {
//        this.userService = userService;
//    }

		public PersistenceManager getPM(final HttpServletRequest request) {
        String context = "context0";
        context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
				return myShepherd.getPM();
		}

/*
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ResponseEntity<List<Survey>> save(final HttpServletRequest request) {
				PersistenceManager pm = getPM(request);
				Extent ext = pm.getExtent(Survey.class);
				Query q = pm.newQuery(ext);
        ArrayList all = new ArrayList((Collection) q.execute());
        return new ResponseEntity<List<Survey>>(all, HttpStatus.OK);
    }
*/


    //note: will create the tag if it does not exist
    @RequestMapping(value = "/appendMedia/{tagName}", method = RequestMethod.POST)
    public ResponseEntity<MediaTag> appendMedia(final HttpServletRequest request,
                                              @RequestBody @Valid List<SinglePhotoVideo> media,
                                              @PathVariable("tagName") final String tagName) {

				PersistenceManager pm = getPM(request);
        MediaTag tag = null;
        try {
          tag = (MediaTag) pm.getObjectById(MediaTag.class, tagName);
        } catch (Exception ex) {
				}

				if (tag == null) {
        	tag = new MediaTag();
					tag.setName(tagName);
				}

				//for some reason, media does not get populated such that when tag is persisted, it creates all new SPVs.. grrr wtf.. explicitely loading them fixes this
				List<SinglePhotoVideo> med = new ArrayList<SinglePhotoVideo>();
				for (SinglePhotoVideo s : media) {
      		SinglePhotoVideo obj = ((SinglePhotoVideo) (pm.getObjectById(pm.newObjectIdInstance(SinglePhotoVideo.class, s.getDataCollectionEventID()), true)));
					if (obj != null) med.add(obj);
				}

				tag.addMedia(med);
				pm.makePersistent(tag);
        return new ResponseEntity<MediaTag>(tag, HttpStatus.OK);
    }

    @RequestMapping(value = "/removeMedia/{tagName}", method = RequestMethod.POST)
    public ResponseEntity<MediaTag> removeMedia(final HttpServletRequest request,
                                              @RequestBody @Valid List<SinglePhotoVideo> media,
                                              @PathVariable("tagName") final String tagName) {

				PersistenceManager pm = getPM(request);
        MediaTag tag = null;
        try {
          tag = (MediaTag) pm.getObjectById(MediaTag.class, tagName);
        } catch (Exception ex) {
				}

        //no tag means do nothing
				if (tag == null) return new ResponseEntity<MediaTag>(tag, HttpStatus.OK);

				//for some reason, media does not get populated such that when tag is persisted, it creates all new SPVs.. grrr wtf.. explicitely loading them fixes this
				List<SinglePhotoVideo> med = new ArrayList<SinglePhotoVideo>();
				for (SinglePhotoVideo s : media) {
      		SinglePhotoVideo obj = ((SinglePhotoVideo) (pm.getObjectById(pm.newObjectIdInstance(SinglePhotoVideo.class, s.getDataCollectionEventID()), true)));
					if (obj != null) med.add(obj);
				}

				tag.removeMedia(med);
				//tag.removeMedia(media);
				pm.makePersistent(tag);
        return new ResponseEntity<MediaTag>(tag, HttpStatus.OK);
    }


}

