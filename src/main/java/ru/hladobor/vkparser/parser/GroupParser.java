package ru.hladobor.vkparser.parser;

import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ru.hladobor.vkparser.VkConnectionAgent;
import ru.hladobor.vkparser.output.OutputService;
import ru.hladobor.vkparser.output.impl.XlsxOuputService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by sever on 26.05.2017.
 */
public class GroupParser extends BaseParser{
    private Logger LOGGER = Logger.getRootLogger();

    public GroupParser() {
       super();
    }

    public void parse(){
        String[] groups = properties.getProperty("groups").split(",");
        String outputPath = Paths.get(properties.getProperty("outputPath", ""), "groups.xls").toString();
        String token = properties.getProperty("token");
        try(XlsxOuputService outpuService = new XlsxOuputService(outputPath)){

            for(String group : groups){
                System.out.println("Starting to parse group: " + group);
                URIBuilder groupInfoBuidler = VkConnectionAgent.buildGroupInfoURI(group, token);
                StringWriter groupInfoContent = VkConnectionAgent.getResponseContent(groupInfoBuidler);
                List<ArrayList<String>> groupInfo = buildGroupInfo(parseStringForObject(groupInfoContent.toString()));
                outpuService.addSheet(group);
                for(ArrayList<String> rowArr : groupInfo){
                    outpuService.fillRow(rowArr.toArray(new String[rowArr.size()]));
                }
                outpuService.fillRow(Arrays.asList("").toArray(new String[1]));
                outpuService.fillRow(Arrays.asList("��������� ������").toArray(new String[1]));
                outpuService.fillRow(buildHeaderData());
                List<ArrayList<String>> users = buildGroupUsersList(group, token, outpuService);
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
                "���-�� ����������", getValueForField("members_count", json)));
        result.add(members);
        result.add(new ArrayList<>(Arrays.asList("�������������: ")));

        JSONArray contacts = (JSONArray)json.get("contacts");
        for(int i = 0; i < contacts.size(); i++){
            JSONObject contact = (JSONObject) contacts.get(i);
            ArrayList<String> contactArr = new ArrayList<>(Arrays.asList(
                    getValueForField("desc", contact), getValueForField("user_id", contact)));
            result.add(contactArr);
        }
        return result;
    }

    List<ArrayList<String>> buildGroupUsersList(String group, String token,
                                                XlsxOuputService outputService) throws ParseException {
        List<ArrayList<String>> result = new ArrayList<>();
        int count = 1000;
        int offset = 0;
        Long membersCount = 0L;
        do{
            URIBuilder uriBuilder = VkConnectionAgent.builgGroupMembersURI(group, token, count, offset);
            JSONObject content = parseStringForObject(VkConnectionAgent.getResponseContent(uriBuilder).toString());
            Map<String, Object> responseMap = (Map)content.get("response");
            if(membersCount == 0) {
                membersCount = (Long) responseMap.get("count");
                System.out.println("Members: " + membersCount);
            }
            offset += count;
            JSONArray users = (JSONArray) responseMap.get("users");
            for (int i = 0; i < users.size(); i++) {
                JSONObject user = (JSONObject) users.get(i);
                String[] output = buildUserOutputData(user);
                outputService.fillRow(output);
            }
            System.out.println("Group " + group + "; parsed " + offset + " members of " + membersCount);
        }while(offset < membersCount );
        return result;
    }
}
