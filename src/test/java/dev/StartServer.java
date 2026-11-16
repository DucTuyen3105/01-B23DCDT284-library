package dev;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

/** Local development runner only; not included in the WAR. */
public final class StartServer {
    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(new File("target/tomcat-" + System.getProperty("http.port", "8080")).getAbsolutePath());
        tomcat.setHostname("127.0.0.1");
        tomcat.setPort(Integer.parseInt(System.getProperty("http.port", "8080")));
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        Context context = tomcat.addWebapp("/libman", new File("src/main/webapp").getAbsolutePath());
        context.setParentClassLoader(StartServer.class.getClassLoader());
        tomcat.start();
        System.out.println("LibMan: http://localhost:" + tomcat.getConnector().getPort() + "/libman/");
        tomcat.getServer().await();
    }

}
