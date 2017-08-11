package com.mathbeta.models.test;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.junit.Test;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xiuyou.xu on 2017/8/11.
 */
public class VelocityTest {
    @Test
    public void testParse() {
        Velocity.init("src/test/resources/velocity.properties");
        Template template = Velocity.getTemplate("src/test/resources/hello.vm");
        VelocityContext context = new VelocityContext();
        StringWriter writer = new StringWriter();
        context.put("username", "麦子");
        context.put("companyName", "Easylife ltd.");
        context.put("now", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        template.merge(context, writer);
        System.out.println(writer.toString());
    }
}
