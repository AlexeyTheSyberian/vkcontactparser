package ru.hladobor.vkparser.parser;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ru.hladobor.vkparser.VkConnectionAgent;
import ru.hladobor.vkparser.output.OutputService;
import ru.hladobor.vkparser.output.impl.CsvOutputService;

import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by sever on 06.05.2017.
 */
public class FriendsParser extends BaseParser {
    private Logger LOGGER = Logger.getRootLogger();

    public FriendsParser() {
        super();
    }

    public void parse() {
        URIBuilder uriBuilder = VkConnectionAgent.buildFriendsURI(properties.getProperty("userId"),
                properties.getProperty("token"));
        LOGGER.info("URI: " + uriBuilder.toString());
        StringWriter content = VkConnectionAgent.getResponseContent(uriBuilder);
        String outputPath = Paths.get(properties.getProperty("outputPath", ""), "output.csv").toString();

        String[] fieldNames = properties.getProperty("fieldsToOuput").split(",");

        try (OutputService output = new CsvOutputService(outputPath)) {

            output.fillRow(buildHeaderData());
            LOGGER.info("Content: " + content.toString());
            JSONObject jsonResp = parseStringForObject(content.toString());
            LOGGER.info("Response: " + jsonResp.toJSONString());
            JSONArray postsList = (JSONArray) ((Map) jsonResp.get("response")).get("items");
            System.out.println("records number: " + postsList.size());
            for (int i = 1; i < postsList.size(); i++) {
                JSONObject friend = (JSONObject) postsList.get(i);
                output.fillRow(buildUserOutputData(friend));
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
            System.exit(-1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Result file created at " + outputPath);
    }
}
