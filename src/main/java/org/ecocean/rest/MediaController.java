package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.CommonConfiguration;
import org.ecocean.servlet.ServletUtilities;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/media")
public class MediaController {

//    private final UserService userService;
//
//    @Inject
//    public UserController(final UserService userService) {
//        this.userService = userService;
//    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public UploadResult uploadMedia(@RequestBody @Valid final UploadData data) {
        return new UploadResult("hello, I am the upload");
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public @ResponseBody UploadResult test(@RequestParam(value="data") final String data) {
      String value = "hello, test with data [" + data + "]"; 
      UploadResult upload = new UploadResult(value);
      return upload;
    }

    @RequestMapping(value = "/test2", method = RequestMethod.POST)
    public int test2() {
        return 42;
    }

    @RequestMapping(value = "/test3", method = RequestMethod.POST)
    public String test3(@RequestParam(value="data") final String data) {
      String value = "hello, test with data [" + data + "]"; 
      UploadResult upload = new UploadResult(value);
      return upload.value;
    }

    @RequestMapping(value = "/test4", method = RequestMethod.POST)
    public UploadResult test4(@RequestParam(value="data") final String data) {
      String value = "hello, I am the test4 with [data=" + data + "]"; 
      UploadResult upload = new UploadResult(value);
      return upload;
    }

    @RequestMapping(value = "/test5", method = RequestMethod.POST)
    public String test5(HttpServletRequest request,
                        @RequestParam(value="data") final String data) {
      String context = ServletUtilities.getContext(request);
      String value = "hello, test with data ["
          + data
          + "] and mail host ["
          + CommonConfiguration.getMailHost(context)
          + "]"; 
      UploadResult upload = new UploadResult(value);
      return upload.value;
    }

//    @RequestMapping(value = "/test6", method = RequestMethod.POST)
//    public @ResponseBody Junk test6(@RequestParam(value="data") final String data) {
//      Junk junk = new Junk();
//      return junk;
//    }

    
    public static class UploadResult {
      private String value;
      
      public UploadResult(final String value)
      {
        this.value = value;
      }
      
      public String getValue()
      {
        return value;
      }
    }
    
    public static class UploadData {
      public int id;
      public String value;
    }
}
