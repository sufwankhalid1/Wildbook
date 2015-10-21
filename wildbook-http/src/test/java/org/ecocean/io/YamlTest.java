package org.ecocean.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.html.HtmlConfig;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlTest {
    @Test
    final public void test1() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:io/webapp_config.yml");

        Yaml yaml = new Yaml(new Constructor(HtmlConfig.class));
        HtmlConfig test = (HtmlConfig) yaml.load(new FileReader(file));

//        Yaml yaml = new Yaml();
//        Object test = yaml.load(new FileReader(file));

        ToStringBuilder.setDefaultStyle(new SuccinctToStringStyle());
        Assert.assertEquals(test.toString(), "[navbar=[menus=[[name=home,url=/,target=<null>,role=<null>,login=false,type=<null>,submenus=<null>], [name=learn,url=<null>,target=<null>,role=<null>,login=false,type=<null>,submenus=[[name=aboutYourProject,url=/overview.jsp,target=<null>,role=<null>,login=false,type=<null>,submenus=<null>], [name=learnAboutShepherd,url=http://www.wildme.org/wildbook,target=_blank,role=<null>,login=false,type=<null>,submenus=<null>]]], [name=encounters,url=<null>,target=<null>,role=<null>,login=false,type=<null>,submenus=[[name=states,url=<null>,target=<null>,role=<null>,login=false,type=header,submenus=<null>], [name=<null>,url=<null>,target=<null>,role=<null>,login=false,type=divider,submenus=<null>], [name=viewMySubmissions,url=/encounters/searchResults.jsp?username=current,target=<null>,role=<null>,login=true,type=<null>,submenus=<null>]]], [name=contactUs,url=/contactus.jsp,target=<null>,role=<null>,login=false,type=<null>,submenus=<null>], [name=administer,url=<null>,target=<null>,role=<null>,login=true,type=<null>,submenus=[[name=myAccount,url=/myAccount.jsp,target=<null>,role=<null>,login=false,type=<null>,submenus=<null>], [name=general,url=/appadmin/admin.jsp,target=<null>,role=admin,login=false,type=<null>,submenus=<null>]]]]]]");
    }
}
