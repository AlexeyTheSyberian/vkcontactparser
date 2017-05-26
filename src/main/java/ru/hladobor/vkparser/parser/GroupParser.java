package ru.hladobor.vkparser.parser;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import ru.hladobor.vkparser.VkConnectionAgent;
import ru.hladobor.vkparser.output.OutputService;
import ru.hladobor.vkparser.output.impl.XlsxOuputService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by sever on 26.05.2017.
 */
public class GroupParser {
    private Logger LOGGER = Logger.getRootLogger();
    Properties properties;

    public GroupParser() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("vk.properties"));
        } catch (IOException ex) {
            System.out.println("Error loading properties from vk.properties: ");
            ex.printStackTrace();
        }
    }

    public void parse(){
        String[] groups = properties.getProperty("groups").split(";");
        String outputPath = Paths.get(properties.getProperty("outputPath", ""), "groups.xlsx").toString();
        try(XlsxOuputService outpuService = new XlsxOuputService(outputPath)){

            for(String group : groups){
                URIBuilder uriBuilder = VkConnectionAgent.builgGroupMembersURI(group,
                        properties.getProperty("token"), 1000, 0);
                StringWriter content = VkConnectionAgent.getResponseContent(uriBuilder);
                System.out.println("Group request: " + uriBuilder.toString());
                System.out.println("group response: " + content.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
