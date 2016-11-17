<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.ecocean.servlet.ServletUtilities,
org.ecocean.media.*,
org.ecocean.*,
org.ecocean.identity.IBEISIA,
org.datanucleus.api.rest.orgjson.JSONObject,
org.datanucleus.api.rest.orgjson.JSONArray,
org.ecocean.servlet.ServletUtilities,org.ecocean.Util,org.ecocean.Measurement, org.ecocean.Util.*, org.ecocean.genetics.*, org.ecocean.tag.*, java.awt.Dimension, javax.jdo.Extent, javax.jdo.Query, java.io.File, java.io.FileInputStream,java.text.DecimalFormat,
java.util.*" %>

<%--
  ~ The Shepherd Project - A Mark-Recapture Framework
  ~ Copyright (C) 2011 Jason Holmberg
  ~
  ~ This program is free software; you can redistribute it and/or
  ~ modify it under the terms of the GNU General Public License
  ~ as published by the Free Software Foundation; either version 2
  ~ of the License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  --%>

<%
String context="context0";
context=ServletUtilities.getContext(request);
Shepherd imageShepherd = new Shepherd(context);
imageShepherd.beginDBTransaction();
String encNum = request.getParameter("encounterNumber");


// collect every MediaAsset as JSON into the 'all' array
JSONArray all = new JSONArray();
Encounter enc = null;
List<String[]> captionLinks = new ArrayList<String[]>();
try {

  String langCode=ServletUtilities.getLanguageCode(request);
  Properties encprops = new Properties();
  encprops = ShepherdProperties.getProperties("encounter.properties", langCode,context);
  enc = imageShepherd.getEncounter(encNum);
  ArrayList<Annotation> anns = enc.getAnnotations();
  %>
  <script>
  function isGenusSpeciesSet() {
    var check = <%=((enc.getGenus()!=null)&&(enc.getSpecificEpithet()!=null))%>;
    console.log("isGenusSpeciesSet() = "+check);
    return check;
  }

  function startIdentify(ma) {
	if (!ma) return;
	var aid = ma.annotationId;
    //var aid = el.getAttribute('data-id');
    //el.parentElement.innerHTML = '<i>starting identification</i>';
//console.warn('aid=%o, el=%o', aid, el); return;
    jQuery.ajax({
      url: '../ia',
      type: 'POST',
      dataType: 'json',
      contentType: 'application/javascript',
      success: function(d) {
        console.info('identify returned %o', d);
        if (d.taskID) {
		$('#image-enhancer-wrapper-' + ma.id + ' .image-enhancer-overlay-message').html('<p>sending to result page...</p>');
          window.location.href = 'matchResults.jsp?taskId=' + d.taskID;
        } else {
		$('#image-enhancer-wrapper-' + ma.id + ' .image-enhancer-overlay-message').html('<p>error starting identification</p>');
        }
      },
      error: function(x,y,z) {
		$('#image-enhancer-wrapper-' + ma.id + ' .image-enhancer-overlay-message').html('<p>error starting identification</p>');
        console.warn('%o %o %o', x, y, z);
      },
      data: JSON.stringify({
        identify: { annotationIds: [ aid ] }
      })
    });
  }

  // because we have links within the photoswipe-opening clickable area
  function forceLink(el) {
    var address = el.href;
    if (address) {
      window.location.href = address;
    };
    el.stopPropagation();
  }
  /*
  $(".forceLink").click(function(e) {
    alert('callin!');
    e.stopPropagation();
  });
  */
  //
  </script>
<%




JSONObject iaTasks = new JSONObject();

  if ((anns == null) || (anns.size() < 1)) {
    %> <script>console.log('no annnotations found for encounter <%=encNum %>'); </script> <%
  }
  else {
  	for (Annotation ann: anns) {
      String[] tasks = IBEISIA.findTaskIDsFromObjectID(ann.getId(), imageShepherd);

      // SKIPPING NON-TRIVIAL ANNOTATIONS FOR NOW! TODO
  		//if (!ann.isTrivial()) continue;  ///or not?

  		MediaAsset ma = ann.getMediaAsset();
  		if (ma != null) {
  			JSONObject j = ma.sanitizeJson(request, new JSONObject());
  			if (j != null) {
				j.put("annotationId", ann.getId());
				JSONObject jann = new JSONObject();
				jann.put("id", ann.getId());
				JSONArray feats = new JSONArray();
				for (Feature f : ann.getFeatures()) {
					JSONObject jf = new JSONObject();
					jf.put("id", f.getId());
					jf.put("type", f.getType());
					jf.put("parameters", Util.toggleJSONObject(f.getParameters()));
					feats.put(jf);
				}
				jann.put("features", feats);
				j.put("annotation", jann);
				all.put(j);
			}
  		}
  	}
  	// out.println("var assets = " + all.toString() + ";");
    //System.out.println("All media assets as an array: "+all.toString());

}
	out.println("<script> var iaTasks = " + iaTasks.toString() + ";</script>");

}
catch(Exception e){e.printStackTrace();}
finally{
	imageShepherd.rollbackDBTransaction();
	imageShepherd.closeDBTransaction();
}


// here we just transform captionLinks into the actual captions we want to pass
JSONArray captions = new JSONArray();
for (int i=0; i<captionLinks.size(); i++) {
  //String cappy = "<div class=\"match-tools\">";
  String cappy = "<div>";
  for (String subCaption : captionLinks.get(i)) {
    cappy = cappy+subCaption+"</br>";
  }
  cappy = cappy+ "</div>";
  captions.put(cappy);
}


%>


<style>
.image-enhancer-wrapper {
	cursor: -webkit-zoom-in;
	cursor: -moz-zoom-in;
}

.image-enhancer-wrapper div {
	cursor: auto;
}

	.match-tools {
		padding: 5px 15px;
		background-color: #DDD;
		margin: 4px;
		border-radius: 4px;
    display: inline-block;
    float: right;
    width: 50%;
	}
	.match-tools a {
		cursor: pointer;
		display: block;
	}
  input[type="file"] {
    display:inline;
  }

</style>

<h2>Gallery</h2>

<div class="my-gallery" id="enc-gallery" itemscope itemtype="http://schema.org/ImageGallery"> </div>
<script src='http://<%=CommonConfiguration.getURLLocation(request) %>/javascript/imageDisplayTools.js'></script>



<script>

  // Load each photo into photoswipe: '.my-gallery' above is grabbed by imageDisplayTools.initPhotoSwipeFromDOM,
  // so here we load .my-gallery with all of the MediaAssets --- done with maJsonToFigureElem.
  var assets = <%=all.toString()%>;
  var captions = <%=captions.toString()%>
  var encounterAnnotationIds = <%
	JSONArray ea = new JSONArray();
	for (Annotation ann : enc.getAnnotations()) {
		ea.put(ann.getId());
	}
	out.println(ea.toString());
%>
  captions.forEach( function(elem) {
    console.log("caption here: "+elem);
  })

  //
  var removeAsset = function(maId) {
    if (confirm("Are you sure you want to remove this image from the encounter? The image will not be deleted from the database, and this action is reversible.")) {
      $.ajax({
        url: '../MediaAssetAttach',
        type: 'POST',
        dataType: 'json',
        contentType: "application/json",
        data: JSON.stringify({"detach":"true","EncounterID":"<%=encNum%>","MediaAssetID":maId}),
        success: function(d) {
          console.info("I detached MediaAsset "+maId+" from encounter <%=encNum%>");
          $('#image-enhancer-wrapper-' + maId).closest('figure').remove();
/*
          $('#remove'+maId).prev('figure').remove();
          $('#remove'+maId).after('<p style=\"text-align:center;\"><i>Image removed from encounter.</i></p>');
          $('#remove'+maId).remove();
*/
        },
        error: function(x,y,z) {
          console.warn("failed to MediaAssetDetach");
          console.warn('%o %o %o', x, y, z);
        }
      });
    }
  }


  assets.forEach( function(elem, index) {
    var assetId = elem['id'];
    console.log("EMG asset "+index+" id: "+assetId);
    maLib.maJsonToFigureElemCaption(elem, $('#enc-gallery'), captions[index]);

/*   now added to image hamburger menu
    var removeAssetLink = "<p id=\"remove"+assetId+"\" style=\"text-align:right\"> <a title=\"Remove above image from encounter\" href=\"\" onclick=\"removeAsset("+assetId+")\">Remove image from encounter</a></p>";

    $('#enc-gallery').append(removeAssetLink);
*/
  });



// h/t https://stackoverflow.com/a/12692647
$(window).resize(function() {
	if (this.resizeTO) clearTimeout(this.resizeTO);
  this.resizeTO = setTimeout(function() {
      $(this).trigger('resizeEnd');
  }, 500);
});

$(window).on('resizeEnd', function(ev) {
	checkImageEnhancerResize();
});

//initializes image enhancement (layers)
jQuery(document).ready(function() {
	doImageEnhancer('figure img');
});

function doImageEnhancer(sel) {
	imagesLoading = false;
	$(sel).each(function(i, el) {
console.log('%d >>>>> %o ?%o', i, el, el.complete);
		if (!el.complete) {
			imagesLoading = true;
			console.log('= = = = waiting on %d -> %o', i, el.complete);
		}
	});
	if (imagesLoading) {  //wait (async); bail and repeat
console.info('waiting to try again...........................');
		setTimeout(function() { doImageEnhancer(sel); }, 700);
		return;
	}

    var loggedIn = wildbookGlobals.username && (wildbookGlobals.username != "");
    var opt = {
    };

    if (loggedIn) {
        opt.debug = false;
        opt.menu = [
/*
            ['remove this image', function(enh) {
		removeAsset(enh.imgEl.prop('id').substring(11));
            }],
*/
            ['select focus cat', function(enh) {
		selectAnnotation(enh.imgEl.prop('id').substring(11));
            }],
	    ['rotate image', function(enh) {
		rotationUI(enh.imgEl.prop('id').substring(11));
	    }]
	];

	if (wildbook.iaEnabled()) {
		opt.menu.push(['start new matching scan', function(enh) {
      if (isGenusSpeciesSet()) {
        imageEnhancer.popup("You need full taxonomic classification to start identification!");
        return;
      }
			//var mid = enh.imgEl.context.id.substring(11);
			var mid = enh.imgEl.data('enh-mediaassetid');
      console.log('%o ?????', mid);
			imageEnhancer.message(jQuery('#image-enhancer-wrapper-' + mid), '<p>starting matching; please wait...</p>');
			startIdentify(assetById(mid), enh.imgEl);
		}]);
	}

	var ct = 1;
	for (var annId in iaTasks) {
		//we really only care about first tid now (most recent)
		var tid = iaTasks[annId][0];
		opt.menu.push([
			//'- previous scan results ' + ct,
			'- previous scan results',
			function(enh, tid) {
				console.log('enh(%o) tid(%o)', enh, tid);
				wildbook.openInTab('matchResults.jsp?taskId=' + tid);
			},
			tid
		]);
	}

/*
	opt.menu.push(
            [
		function(enh) { return imagePopupInfoMenuItem(enh); },
		function(enh) { imagePopupInfo(enh); }
            ]
	);

        if (true) {
            opt.menu.push(['set image as encounter thumbnail', function(enh) {
            }]);
        }
*/

        opt.init = [
/*  no keywords for catnip!
            function(el, enh) {
console.info(' ===========>   %o %o', el, enh);
		imageLayerKeywords(el, enh);
            },
*/
            function(el, enh) { drawFeature(el.prop('id').substring(23)); }
        ];

    }

	opt.callback = function() {
		$('.image-enhancer-keyword-wrapper').on('click', function(ev) { ev.stopPropagation(); });
	};
    imageEnhancer.applyTo(sel, opt);
}


function checkImageEnhancerResize() {
	var needUpdate = false;
	$('.image-enhancer-wrapper').each(function(i,el) {
		var imgW = $('#figure-img-' + el.id.substring(23)).width();
		var wrapW = $(el).width();
//console.warn('%o -> %o vs %o', el.id, imgW, wrapW);
		if (imgW && wrapW && (imgW != wrapW)) needUpdate = true;
	});
	if (needUpdate) doImageEnhancer('figure img');
}


var popupStartTime = 0;
function addNewKeyword(el) {
	console.warn(el);
	var jel = $(el);
	var wrapper = jel.closest('.image-enhancer-wrapper');
	if (!wrapper.length) {
		console.error("could not find MediaAsset id from closest wrapper");
		return;
	}
	var mid = wrapper.prop('id').substring(23);
	if (!assetById(mid)) {
		console.error("could not find MediaAsset byId(%o)", mid);
		return;
	}

	var val = jel.val();

	var data = { onMediaAssets: { assetIds: [ mid ] } };

	if (el.id == 'keyword-new') {
		if (val == '') return;
		//imageEnhancer.popup('Adding new keyword <b>' + val + '</b> to this image.');
		data.onMediaAssets.newAdd = [ val ];
	} else if (jel.hasClass('iek-remove')) {
		var kid = jel.parent().prop('id').substring(8);
		//imageEnhancer.popup('Removing keyword <b>' + wildbookGlobals.keywords[kid] + '</b> from this image.');
		data.onMediaAssets.remove = [ kid ];
	} else {
		var name = wildbookGlobals.keywords[val] || '';
		//imageEnhancer.popup('Adding keyword <b>' + name + '</b> to this image.');
		data.onMediaAssets.add = [ val ];
	}
console.info(data);

	popupStartTime = new Date().getTime();
	$.ajax({
		url: wildbookGlobals.baseUrl + '/RestKeyword',
		data: JSON.stringify(data),
		contentType: 'application/javascript',
		success: function(d) {
console.info(d);
			if (d.success) {
/*
				var elapsed = new Date().getTime() - popupStartTime;
				if (elapsed > 6000) {
					$('.image-enhancer-popup').remove();
				} else {
					window.setTimeout(function() { $('.image-enhancer-popup').remove(); }, 6000 - elapsed);
				}
*/
				if (d.newKeywords) {
					for (var id in d.newKeywords) {
						wildbookGlobals.keywords[id] = d.newKeywords[id];
					}
				}
				//the reality is we prob only have one, mid so we save that to update the menu of
				var mainMid = false;
				if (d.results) {
					for (var mid in d.results) {
						if (!mainMid) mainMid = mid;
						assetById(mid).keywords = [];
						for (var id in d.results[mid]) {
							assetById(mid).keywords.push({
								indexname: id,
								readableName: d.results[mid][id]
							});
						}
					}
				}
				if (mainMid) {
					$('#image-enhancer-wrapper-' + mainMid + ' .image-enhancer-keyword-wrapper').remove();
					imageLayerKeywords($('#image-enhancer-wrapper-' + mainMid), { _mid: mainMid });
				}
			} else {
				var msg = d.error || 'ERROR could not make change';
				$('.popup-content').append('<p class="error">' + msg + '</p>');
			}
		},
		error: function(x,a,b) {
			console.error('%o %o %o', x, a, b);
			$('.popup-content').append('<p class="error">ERROR making change: ' + b + '</p>');
		},
		type: 'POST',
		dataType: 'json'
	});
	return false;
}

/*
{
    "success": true,
    "newKeywords": {
        "ff808181557f843f01557f843f280000": "foo"
    },
    "results": {
        "82091": {
            "518aca4e5113bfc8015113bfe77b000e": "2A",
            "518aca4e5113bfc8015113bfe4fd000b": "2C",
            "518aca4e46618bcd0146915d7a120016": "Left-Dorsal",
            "518aca4e430bec5501430bf2fc190001": "fluke",
            "ff808181557f843f01557f843f280000": "foo"
        }
    }
}
*/

function imageLayerKeywords(el, opt) {
	var mid;
	if (opt && opt._mid) {  //hack!
		mid = opt._mid;
	} else {
 		mid = el.context.id.substring(11);
	}
	var ma = assetById(mid);
console.info("############## mid=%s -> %o", mid, ma);
	if (!ma) return;

	if (!ma.keywords) ma.keywords = [];
	var thisHas = [];
	var h = '<div class="image-enhancer-keyword-wrapper">';
	for (var i = 0 ; i < ma.keywords.length ; i++) {
		thisHas.push(ma.keywords[i].indexname);
//console.info('keyword = %o', ma.keywords[i]);
		h += '<div class="image-enhancer-keyword" id="keyword-' + ma.keywords[i].indexname + '">' + ma.keywords[i].readableName + ' <span class="iek-remove" title="remove keyword">X</span></div>';
	}

	h += '<div class="iek-new-wrapper' + (ma.keywords.length ? ' iek-autohide' : '') + '">add new keyword<div class="iek-new-form">';
	if (wildbookGlobals.keywords) {
		var hasSome = false;
		var mh = '<select onChange="return addNewKeyword(this);" style="width: 100%" class="keyword-selector"><option value="">select keyword</option>';
		for (var j in wildbookGlobals.keywords) {
			if (thisHas.indexOf(j) >= 0) continue; //dont list ones we have
			mh += '<option value="' + j + '">' + wildbookGlobals.keywords[j] + '</option>';
			hasSome = true;
		}
		mh += '</select>';
		if (hasSome) h += mh;
	}
	h += '<br /><input placeholder="or enter new" id="keyword-new" type="text" style="" onChange="return addNewKeyword(this);" />';
	h += '</div></div>';

	h += '</div>';
	el.append(h);
	el.find('.image-enhancer-keyword-wrapper').on('click', function(ev) {
		ev.stopPropagation();
	});
	el.find('.iek-remove').on('click', function(ev) {
		//ev.stopPropagation();
		addNewKeyword(ev.target);
	});
}

function imagePopupInfo(obj) {
	if (!obj || !obj.imgEl || !obj.imgEl.context) return;
	var mid = obj.imgEl.context.id.substring(11);
	var ma = assetById(mid);
	if (!ma) return;
	var h = '<div>media asset id: <b>' + mid + '</b><br />';
	if (ma.metadata) {
		for (var n in ma.metadata) {
			h += n + ': <b>' + ma.metadata[n] + '</b><br />';
		}
	}
	h += '</div>';
	imageEnhancer.popup(h);
}

function imagePopupInfoMenuItem(obj) {
//console.log('MENU!!!! ----> %o', obj);
	if (!obj || !obj.imgEl || !obj.imgEl.context) return false;
	var mid = obj.imgEl.context.id.substring(11);
	var ma = assetById(mid);
	if (!ma) return false;
	return 'image info';
}


function assetById(mid) {
	if (!assets || (assets.length < 1)) return false;
	for (var i = 0 ; i < assets.length ; i++) {
		if (assets[i].id == mid) return assets[i];
	}
	return false;
}

function getFocusFeature(mid) {
	if (!assetById(mid) || !assetById(mid).annotation || !assetById(mid).annotation.features || (assetById(mid).annotation.features.length < 1)) return false;
	//do we know for sure there will be only one????
	console.info(assetById(mid).annotation.features[0]);
	return assetById(mid).annotation.features[0];
}

function drawFeature(mid) {
	var asset = assetById(mid);
	if (!asset) return;
	var ft = getFocusFeature(mid);
	if (!ft || !ft.type || (ft.type != 'org.ecocean.boundingBox') || !ft.parameters) return;
	console.warn('%o => %o', mid, ft);
	var cw = $('#image-enhancer-wrapper-' + mid).width();
	var ch = $('#image-enhancer-wrapper-' + mid).height();
	console.warn('w=%d, h=%d', cw, ch);
	var canvas = $('<canvas class="canvas-feature imageenh-canvas" width="' + cw + '" height="' + ch + '"></canvas>');
	$('#image-enhancer-wrapper-' + mid).append(canvas);

	var scale = 1;
	if (asset.metadata && asset.metadata.width) scale = cw / asset.metadata.width;
console.warn('scale = %f', scale);
	var ctx = canvas[0].getContext('2d');
	ctx.beginPath();
	ctx.lineWidth = '3';
	ctx.strokeStyle = 'rgba(200,255,0,0.7)';
	ctx.rect(ft.parameters.x * scale, ft.parameters.y * scale, ft.parameters.width * scale, ft.parameters.height * scale);
	ctx.stroke();

	canvas.on('mousemove click', function(ev) {
		if ((ev.offsetX < ft.parameters.x * scale) || (ev.offsetX > scale * (ft.parameters.x + ft.parameters.width)) ||
		    (ev.offsetY < ft.parameters.y * scale) || (ev.offsetY > scale * (ft.parameters.y + ft.parameters.height))) {
			ev.target.style.cursor = 'inherit';
			ev.target.title = "";
			return;
		}
		ev.target.style.cursor = 'pointer';
		ev.target.title = "this is the cat!";
		ev.stopPropagation();
if (ev.type == 'click') console.warn(ft);
	});
}

var selectAnnotationStart = false;
function selectAnnotation(mid) {
	var ft = getFocusFeature(mid);
	if (!ft) ft = {
		type: 'org.ecocean.boundingBox',
		parameters: { x: 20, y: 20, w: 100, h: 100 }
	};
	console.warn('%o => %o', mid, ft);
	var canvas = $('<canvas class="canvas-annot-select imageenh-canvas" width="' + $('#image-enhancer-wrapper-' + mid).width() +
		'" height="' + $('#image-enhancer-wrapper-' + mid).height() + '"></canvas>');
	$('#image-enhancer-wrapper-' + mid).append(canvas);
	canvas.on('mouseup mousedown mousemove click', function(ev) { selectAnnotationMouse(ev, mid); });
/*
	canvas.on('mousedown', function(ev) { selectAnnotationMouse(ev); });
	canvas.on('mousemove', function(ev) { selectAnnotationMouse(ev); });
	canvas.on('click', function(ev) { selectAnnotationMouse(ev); });
*/
}

function selectAnnotationMouse(ev, mid) {
	ev.stopPropagation();
	ev.preventDefault();
	if (ev.type == 'click') return;
	if (ev.type == 'mousedown') {
		$('#image-enhancer-wrapper-' + mid + ' .quick-tools').remove();
		selectAnnotationStart = [ev.offsetX, ev.offsetY];
		return;
	}
	if (ev.type == 'mouseup') {
		//var rect = [selectAnnotationStart[0], selectAnnotationStart[1], ev.offsetX, ev.offsetY];
		$('#image-enhancer-wrapper-' + mid).append('<div class="quick-tools" style="left: ' + (ev.offsetX + 10) + 'px; top: ' + (ev.offsetY + 10) + 'px">' +
			'<div class="quick-tools-button" onClick="return selectAnnotationSave(event, ' +
				[mid, selectAnnotationStart[0], selectAnnotationStart[1], ev.offsetX, ev.offsetY].join(', ') + ');">save</div>' +
			'<div class="quick-tools-button" onClick="return selectAnnotationCancel(event, ' + mid + ');">cancel</div>' +
			'</div>'
		);
		selectAnnotationStart = false;
		//console.info(rect);
	}
	if (selectAnnotationStart && (ev.type == 'mousemove')) {
		//console.warn(ev);
		var ctx = ev.target.getContext('2d');
		ctx.clearRect(0, 0, ev.target.width, ev.target.height);
		ctx.beginPath();
		ctx.lineWidth = '3';
		ctx.strokeStyle = 'rgba(255,255,0,0.7)';
		ctx.rect(
			Math.min(selectAnnotationStart[0], ev.offsetX),
			Math.min(selectAnnotationStart[1], ev.offsetY),
			Math.abs(selectAnnotationStart[0] - ev.offsetX),
			Math.abs(selectAnnotationStart[1] - ev.offsetY)
		);
		ctx.stroke();
	}
}

function selectAnnotationSave(ev, mid, x1, y1, x2, y2) {
	ev.stopPropagation();
	if (!assetById(mid) || !assetById(mid).metadata || !assetById(mid).metadata.width || !assetById(mid).annotation) {
		alert('could not determine image width or annotation');
		console.warn('could not determine image metadata width or annotation for mid=%o', mid);
		return false;
	}
	var scale = assetById(mid).metadata.width / $('#image-enhancer-wrapper-' + mid).width();
	console.log('mid=%d scale=%.1f [%d,%d,%d,%d]', mid, scale, x1,y1,x2,y2);
	$.ajax({
		url: '../MediaAssetModify',
		type: 'POST',
		data: 'id=' + mid + '&annotationId=' + assetById(mid).annotation.id +
			'&fx=' + Math.round(Math.min(x1, x2) * scale) +
			'&fy=' + Math.round(Math.min(y1,y2) * scale) +
			'&fwidth=' + Math.abs(Math.round((x2 - x1) * scale)) +
			'&fheight=' + Math.abs(Math.round((y2 - y1) * scale)),
		complete: function(x, s) {
console.info('x=%o s=%o', x, s);
			$('#image-enhancer-wrapper-' + mid + ' canvas').remove();
			if (x.status == 200) {
				$('#image-enhancer-wrapper-' + mid + ' .quick-tools').remove();
				if (!x.responseJSON || !x.responseJSON.success || !x.responseJSON.feature) {
					console.warn("invalid response");
					return;
				}
				assetById(mid).features = [ x.responseJSON.feature ];
				assetById(mid).annotation.features = [ x.responseJSON.feature ];
				drawFeature(mid);
			} else {
				$('.quick-tools-button').remove();
				$('#image-enhancer-wrapper-' + mid + ' .quick-tools').append('<div title="close error" class="quick-tools-button" onClick="event.stopPropagation(); $(this).parent().remove();">FAILED: '
					+ x.status + ' ' + x.statusText + '</div>');
			}
		},
		dataType: 'json'
	});
	return true;
}
function selectAnnotationCancel(ev, mid) {
	$('#image-enhancer-wrapper-' + mid + ' canvas.canvas-annot-select').remove();
	$('#image-enhancer-wrapper-' + mid + ' .quick-tools').remove();
	ev.stopPropagation();
}



var rotationDeg = 0;
function rotationUI(mid) {
	$('.my-gallery figure').css('overflow', 'hidden');
	$('#image-enhancer-wrapper-' + mid + ' canvas').hide();
	var rtools = $('<div class="quick-tools" style="right: 4px; bottom: 4px;"></div>');
	rtools.append('<div id="rotate-button-ok" style="background-color: #AFC; display: none;" class="quick-tools-button" onClick="rotationClick2(' + mid + ', -2, event)">save</div>');
	rtools.append('<div class="quick-tools-button" onClick="rotationClick2(' + mid + ', +1, event)"><img class="quick-tools-icon" src="../images/rotate_right.svg" /></div>');
	rtools.append('<div class="quick-tools-button" onClick="rotationClick2(' + mid + ', -1, event)"><img class="quick-tools-icon" src="../images/rotate_left.svg" /></div>');
	rtools.append('<div class="quick-tools-button" onClick="rotationClick2(' + mid + ', -3, event)">cancel</div>');

/*
	rtools.append('<div id="rotate-button-ok" style="background-color: #AFC; display: none;" class="quick-tools-button" onClick="rotationClick(' + mid + ', -2, event)">save</div>');
	rtools.append('<div class="quick-tools-button" onClick="rotationClick(' + mid + ', 90, event)">CW</div>');
	rtools.append('<div class="quick-tools-button" onClick="rotationClick(' + mid + ', 270, event)">CCW</div>');
	rtools.append('<div class="quick-tools-button" onClick="rotationClick(' + mid + ', 180, event)">180</div>');
	rtools.append('<div class="quick-tools-button" onClick="rotationClick(' + mid + ', -3, event)">cancel</div>');
*/
	$('#image-enhancer-wrapper-' + mid).append(rtools);
}

function rotationClick2(mid, n, ev) {
	if (n == -2) return rotationClick(mid, -2, ev);  //save
	if (n == -3) return rotationClick(mid, -3, ev);  //cancel
	rotationDeg = rotationDeg + (n * 90);
	if (rotationDeg < 0) rotationDeg += 360;
	rotationDeg = rotationDeg % 360;
console.log('rotationDeg=%o', rotationDeg);
	return rotationClick(mid, rotationDeg, ev);
}

function rotationClick(mid, deg, ev) {
//console.info('%d, %d, %o', mid, deg, ev);
	ev.stopPropagation();
	if (deg >= 0) {
		if (deg == 0) {
			$('#rotate-button-ok').hide();
		} else {
			$('#rotate-button-ok').show();
		}
		$('#figure-img-' + mid).css('transform', 'rotate(' + deg + 'deg)').data('rotation', deg);
		return;
	}
	$('.my-gallery figure').css('overflow', '');
	if (deg == -2) {
		var rot = $('#figure-img-' + mid).data('rotation') - 0;
console.log('rot -> %o', rot);
		$('.quick-tools-button').remove();
		$('#image-enhancer-wrapper-' + mid + ' .quick-tools').append('<div class="quick-tools-button">saving...</div>');
		$.ajax({
			url: '../MediaAssetModify',
			type: 'POST',
//application/x-www-form-urlencoded
			data: 'id=' + mid + '&rotation=' + rot,
			complete: function(x, s) {
console.log('x=%o, s=%o', x, s);
				$('.quick-tools-button').remove();
				if (x.status == 200) {
					if (!x.responseJSON || !x.responseJSON.success || (x.responseJSON.rotationFinal == undefined)) {
						var errmsg = (x.responseJSON ? (x.responseJSON.error || 'unknown error') : 'error rotating');
						$('#image-enhancer-wrapper-' + mid + ' .quick-tools').append('<div title="close error" class="quick-tools-button" onClick="event.stopPropagation(); $(this).parent().remove();">' + errmsg + '</div>');
					$('#figure-img-' + mid).css('transform', 'rotate(0deg)').data('rotation', 0);
					} else {
						$('#image-enhancer-wrapper-' + mid + ' .quick-tools').append('<div class="quick-tools-button">reloading...</div>');
						window.location.reload();
					}
				} else {
					$('#image-enhancer-wrapper-' + mid + ' .quick-tools').append('<div title="close error" class="quick-tools-button" onClick="event.stopPropagation(); $(this).parent().remove();">FAILED: '
						+ x.status + ' ' + x.statusText + '</div>');
					$('#figure-img-' + mid).css('transform', 'rotate(0deg)').data('rotation', 0);
				}
				rotationDeg = 0;
			},
			dataType: 'json'
		});
		rotationDeg = 0;
		return;
	}
	rotationDeg = 0;
	$('#figure-img-' + mid).css('transform', 'rotate(0deg)').data('rotation', 0);
	$('#image-enhancer-wrapper-' + mid + ' canvas').show();
	$('.quick-tools').remove();
}

</script>
<style>
	.my-gallery figure {
		/*overflow: hidden;   to hide rotated image */
	}
	.quick-tools {
		position: absolute;
		z-index: 20;
	}
	.quick-tools-button {
		margin: 3px 0;
		padding: 0 5px;
		background-color: white;
		border: solid 2px #444;
		cursor: pointer !important;
		border-radius: 4px;
		color: #AAA;
	}
	.quick-tools-button:hover {
		color: #333;
		background-color: #FF8;
	}
	.quick-tools-icon {
		height: 25px;
		margin-left: 15%;
	}

	.imageenh-canvas {
		position: absolute;
	}
	.canvas-annot-select {
		cursor: crosshair;
		cursor: cell;
		cursor: move;
	}
	.canvas-feature {
		/* cursor: pointer; */
	}

	#match-tools {
		padding: 5px 15px;
		display: inline-block;
		background-color: #DDD;
		margin: 4px;
		border-radius: 4px;
	}
	#match-tools a {
		cursor: pointer;
		display: block;
	}

	.error-bad-content-type {
		padding: 10px;
		background-color: #FDD;
		border-radius: 10px;
		margin: 10px;
	}

</style>

<jsp:include page="../photoswipe/photoswipeTemplate.jsp" flush="true"/>
