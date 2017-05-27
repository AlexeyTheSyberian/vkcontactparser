package ru.hladobor.vkparser.parser;

import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.hladobor.vkparser.VkConnectionAgent;
import ru.hladobor.vkparser.output.OutputService;
import ru.hladobor.vkparser.output.impl.XlsxOuputService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by sever on 26.05.2017.
 */
public class GroupParser extends BaseParser{
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
        String[] groups = properties.getProperty("groups").split(",");
        String outputPath = Paths.get(properties.getProperty("outputPath", ""), "groups.xls").toString();
        String token = properties.getProperty("token");
        try(XlsxOuputService outpuService = new XlsxOuputService(outputPath)){

            for(String group : groups){
                URIBuilder groupInfoBuidler = VkConnectionAgent.buildGroupInfoURI(group, token);
                StringWriter groupInfoContent = VkConnectionAgent.getResponseContent(groupInfoBuidler);
                List<ArrayList<String>> groupInfo = buildGroupInfo(parseStringForObject(groupInfoContent.toString()));
                outpuService.addSheet(group);
                for(ArrayList<String> rowArr : groupInfo){
                    outpuService.fillRow(rowArr.toArray(new String[rowArr.size()]));
                }

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

    List<ArrayList<String>> buildGroupInfo(JSONObject outerJson){
        JSONObject json = (JSONObject)((JSONArray)outerJson.get("response")).get(0);
        List<ArrayList<String>> result = new ArrayList<>();
        ArrayList<String> caption = new ArrayList<>(Arrays.asList(getValueForField("name", json)));
        result.add(caption);

        ArrayList<String> members = new ArrayList<>(Arrays.asList(
                "Кол-во участников", getValueForField("members_count", json)));
        result.add(members);
        result.add(new ArrayList<>(Arrays.asList("Администрация: ")));

        JSONArray contacts = (JSONArray)json.get("contacts");
        for(int i = 0; i < contacts.size(); i++){
            JSONObject contact = (JSONObject) contacts.get(i);
            ArrayList<String> contactArr = new ArrayList<>(Arrays.asList(
                    getValueForField("desc", contact), getValueForField("user_id", contact)));
            result.add(contactArr);
        }
        return result;
    }
}
