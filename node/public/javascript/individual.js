'use strict';

var individualPage = (function () {
    function init(data) {

//        //============================
//        // Carousel hack to make it show three at a time.
//        // UPDATE: Can't get it to work. Modified from this http://www.bootply.com/94452#
//        // NOTE: Also requires css hack, something like
//        .carousel-inner .active.left { left: -33.33%; }
//        .carousel-inner .next        { left:  33.33%; }
//        .carousel-inner .prev        { left: -33.33%; }
//        .carousel-control            { width:  4%; }
//        .carousel-control.left,.carousel-control.right {margin-left:15px;background-image:none;}
//        .carousel-inner .col-md-4 {width: 33.33%;}
//
//        $('.carousel .item').each(function(){
//            var next = $(this).next();
//            if (!next.length) {
//              next = $(this).siblings(':first');
//            }
//            next.children(':first-child').clone().appendTo($(this));
//
//            for (var i=0;i<1;i++) {
//              next=next.next();
//              if (!next.length) {
//                  next = $(this).siblings(':first');
//              }
//
//              next.children(':first-child').clone().appendTo($(this));
//            }
//        });
//        //
//        // Carousel hack
//        //============================

        //============================
        // PHOTOSWIPE CODE
        //
        // We should supply the height and width for each
        // image and we aren't storing that in SinglePhotoVideo. Are we going to do that
        // MediaAsset? This library depends on it but thanks to hack below it works without.
        //
        var pswpElement = document.querySelectorAll('.pswp')[0];

        if (pswpElement) {
            // build items array
            var items = [];
            data.photos.forEach(function(photo){
                items.push({src: app.config.wildbook.staticUrl + photo.url, w: 0, h:0});
            });

            function startGallery(startIndex) {
                var options = {
                    index: startIndex
                };

                // Initializes and opens PhotoSwipe
                var gallery = new PhotoSwipe( pswpElement, PhotoSwipeUI_Default, items, options);

                //
                // HACK:
                // Found this hack for allowing images not specifying the size. From @gincius
                // https://github.com/dimsemenov/PhotoSwipe/issues/796
                //
                gallery.listen('gettingData', function(index, item) {
                    if (item.w < 1 || item.h < 1) { // unknown size
                        var img = new Image();
                        img.onload = function() { // will get size after load
                            item.w = this.width; // set image width
                            item.h = this.height; // set image height
                            gallery.invalidateCurrItems(); // reinit Items
                            gallery.updateSize(true); // reinit Items
                        }
                        img.src = item.src; // let's download image
                    }
                });
                gallery.init();
            }

            $("#main-photo").click(function() {
                startGallery(0);
            })

            $("#sub-photo1").click(function() {
                startGallery(1);
            })

            $("#sub-photo2").click(function() {
                startGallery(2);
            })

            $("#sub-photo3").click(function() {
                startGallery(3);
            })
        }
        //
        // END PHOTOSWIPE CODE
        //============================


        var map = maptool.createMap('map-sightings');
        //
        // Add encounters to map and set view to be centered around these encounters.
        //
        var latlngs = [];
        data.encounters.forEach(function(encounter) {
            if (encounter.latitude && encounter.longitude) {
                var popup = $("<div>");
                popup.append($("<span>").addClass("sight-date").text(moment(encounter.dateInMilliseconds).format('LL')));
                popup.append($("<br>"));
                popup.append($("<span>").addClass("sight-date-text").text(encounter.verbatimLocation));
                popup.append($("<br>"));
                popup.append(app.userDiv(encounter.submitter));
                latlngs.push({latlng: [encounter.latitude, encounter.longitude],
                              popup: popup[0]});
            }
        });

        map.addIndividuals(latlngs);
    }

    return {init: init};
})();