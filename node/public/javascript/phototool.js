var phototool = (function () {

//  //============================
//  // Carousel hack to make it show three at a time.
//  // UPDATE: Can't get it to work. Modified from this http://www.bootply.com/94452#
//  // NOTE: Also requires css hack, something like
//  .carousel-inner .active.left { left: -33.33%; }
//  .carousel-inner .next        { left:  33.33%; }
//  .carousel-inner .prev        { left: -33.33%; }
//  .carousel-control            { width:  4%; }
//  .carousel-control.left,.carousel-control.right {margin-left:15px;background-image:none;}
//  .carousel-inner .col-md-4 {width: 33.33%;}
//
//  $('.carousel .item').each(function(){
//      var next = $(this).next();
//      if (!next.length) {
//        next = $(this).siblings(':first');
//      }
//      next.children(':first-child').clone().appendTo($(this));
//
//      for (var i=0;i<1;i++) {
//        next=next.next();
//        if (!next.length) {
//            next = $(this).siblings(':first');
//        }
//
//        next.children(':first-child').clone().appendTo($(this));
//      }
//  });
//  //
//  // Carousel hack
//  //============================

    function startGallery(pswpElement, startIndex, photos) {
        var options = {
            index: startIndex
        };

        // Initializes and opens PhotoSwipe
        var gallery = new PhotoSwipe( pswpElement, PhotoSwipeUI_Default, photos, options);

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

    function setPhotos(photos) {
        //
        // We should supply the height and width for each
        // image and we aren't storing that in SinglePhotoVideo. Are we going to do that
        // MediaAsset? The photoswipe library depends on it but thanks to hack in the startGallery function
        // it works without.
        //
        var pswpElement = document.querySelectorAll('.pswp')[0];

        if (pswpElement) {
            $("#main-photo").click(function() {
                startGallery(pswpElement, 0, photos);
            })

            $("#sub-photo1").click(function() {
                startGallery(pswpElement, 1, photos);
            })

            $("#sub-photo2").click(function() {
                startGallery(pswpElement, 2, photos);
            })

            $("#sub-photo3").click(function() {
                startGallery(pswpElement, 3, photos);
            })
        }
    }

    return {
        setPhotos: setPhotos
    };
})();
