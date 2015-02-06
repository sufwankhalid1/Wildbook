package org.ecocean.rest;

public class MediaController {
  
}
//import javax.validation.Valid;
//
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping(value = "/media")
//public class MediaController {
//
////    private final UserService userService;
////
////    @Inject
////    public UserController(final UserService userService) {
////        this.userService = userService;
////    }
//
//    @RequestMapping(value = "/upload", method = RequestMethod.POST)
//    public UploadResult uploadMedia(@RequestBody @Valid final UploadData data) {
//        return new UploadResult("hello, I am the upload");
//    }
//
//    @RequestMapping(value = "/test", method = RequestMethod.POST)
//    public @ResponseBody UploadResult test(@RequestParam(value="data") final String data) {
//      String value = "hello, I am the test with [data=" + data + "]"; 
//        return new UploadResult(value);
////        return value;
//    }
//
//    @RequestMapping(value = "/test2", method = RequestMethod.POST)
//    public int test2() {
////        return new UploadResult("hello, I am the test2");
//        return 42;
//    }
//
//    private static class UploadResult {
//      private String value;
//      public UploadResult(final String value)
//      {
//        this.value = value;
//      }
//    }
//    
//    private static class UploadData {
//      public int id;
//      public String value;
//    }
//}
