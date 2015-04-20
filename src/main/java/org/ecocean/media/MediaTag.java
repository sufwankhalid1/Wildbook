
package org.ecocean.media;

import java.util.List;
import java.util.ArrayList;
import org.ecocean.SinglePhotoVideo;

public class MediaTag {
  private String name;
  private List<SinglePhotoVideo> media; 

	public String getName() {
		return name;
	}
	public void setName(String n) {
		name = n;
	}

	public List<SinglePhotoVideo> getMedia() {
		return media;
	}
	public void setMedia(List<SinglePhotoVideo> m) {
		media = m;
	}
	public void addMedia(List<SinglePhotoVideo> m) {
		if ((m == null) || (m.size() < 1)) return;
		List<SinglePhotoVideo> all = getMedia();
		if (all == null) all = new ArrayList<SinglePhotoVideo>();
		all.addAll(m);
		setMedia(all);
	}
}
