package ru.hladobor.vkparser.parser;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.hladobor.vkparser.VkConnectionAgent;
import ru.hladobor.vkparser.output.OutputService;
import ru.hladobor.vkparser.output.impl.CsvOutputService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by sever on 06.05.2017.
 */
public class FriendsParser {

    private static String NULL_VALUE = "Не указано";

    private static Map<String, String> FIELD_TITLES;
    private static Map<String, String> UNIVERSITY_FIELD_TITLES;

    static{
        FIELD_TITLES = new HashMap<>();
        FIELD_TITLES.put("last_name","Фамилия");
        FIELD_TITLES.put("first_name","Имя");
        FIELD_TITLES.put("bdate","День рождения");
        FIELD_TITLES.put("country.title","Страна");
        FIELD_TITLES.put("city.title","Город");
        FIELD_TITLES.put("mobile_phone","Мобильный телефон");
        FIELD_TITLES.put("home_phone","Домашний телефон");
        FIELD_TITLES.put("domain","Skype");


        UNIVERSITY_FIELD_TITLES = new HashMap<>();
        UNIVERSITY_FIELD_TITLES.put("name","ВУЗ");
        UNIVERSITY_FIELD_TITLES.put("faculty_name","Факультет");
        UNIVERSITY_FIELD_TITLES.put("chair_name","Кафедра");
        UNIVERSITY_FIELD_TITLES.put("education_form","Форма обучения");
        UNIVERSITY_FIELD_TITLES.put("education_status","Статус");
        UNIVERSITY_FIELD_TITLES.put("graduation","Год выпуска");

    }
    private Logger LOGGER = Logger.getRootLogger();
    Properties properties;

    public FriendsParser() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("vk.properties"));
        } catch (IOException ex) {
            System.out.println("Error loading properties from vk.properties: ");
            ex.printStackTrace();
        }
    }

    public void parse() {
        URIBuilder uriBuilder = VkConnectionAgent.buildFriendsURI(properties.getProperty("userId"),
                properties.getProperty("token"));
        LOGGER.info("URI: " + uriBuilder.toString());
        StringWriter content = VkConnectionAgent.getResponseContent(uriBuilder);
        String outputPath = Paths.get(properties.getProperty("outputPath", ""), "output.csv").toString();
        JSONParser parser = new JSONParser();

        String[] fieldNames = properties.getProperty("fieldsToOuput").split(",");
        String[] universityFieldNames = properties.getProperty("universityFields").split(",");

        try (OutputService output = new CsvOutputService(outputPath)) {
             output.fillHeader(buildHeaderData(fieldNames, universityFieldNames));
            LOGGER.info("Content: " + content.toString());
            JSONObject jsonResp = (JSONObject) parser.parse(content.toString());
            LOGGER.info("Response: " + jsonResp.toJSONString());
            JSONArray postsList = (JSONArray) ((Map) jsonResp.get("response")).get("items");
            System.out.println("records number: " + postsList.size());
            for (int i = 1; i < postsList.size(); i++) {
                JSONObject friend = (JSONObject) postsList.get(i);
                output.fillRow(buildOutputData(friend, fieldNames));
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
            System.exit(-1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Result file created at " + outputPath);
    }

    private String[] buildHeaderData(String[] fieldsNames, String[] universityFieldNames){
        String[] result = new String[fieldsNames.length + universityFieldNames.length];
        int counter = 0;
        for (String field : fieldsNames) {
            result[counter++] = FIELD_TITLES.get(field);
        }
        for (String field : universityFieldNames) {
            result[counter++] = UNIVERSITY_FIELD_TITLES.get(field);
        }
        return result;
    }

    private String[] buildOutputData(JSONObject json, String[] fieldsToOutput){
        String[] result = null;

        String[] noUnivercityInfo = new String[fieldsToOutput.length];
        for (int i =0; i < fieldsToOutput.length; i++){
            String field = fieldsToOutput[i];
            String value = getValueForField(field, json);
            noUnivercityInfo[i] = value;
        }

        JSONArray universities = (JSONArray) json.get("universities");
        if(universities != null && universities.size() > 0) {
            for (int i = 0; i < universities.size(); i++) {
                String[] universityData = buildIUniversityData((JSONObject) universities.get(i));
                result = (String[]) ArrayUtils.addAll(noUnivercityInfo, universityData);
            }
        }else{
            result = (String[])ArrayUtils.addAll(noUnivercityInfo, buildEmptyUniversityData());
        }
        return result;
    }

    private String[] buildIUniversityData(JSONObject university){
        String[] fields = properties.getProperty("universityFields").split(",");
        String[] result = new String[fields.length];
        for(int i =0; i < fields.length; i++){
            String field = fields[i];
            String value = getValueForField(field, university);
            result[i] = value;
        }
        return result;
    }

    private String[] buildEmptyUniversityData(){
        String[] fields = properties.getProperty("universityFields").split(",");
        String[] result = new String[fields.length];
        for(int i = 0; i < fields.length; i++){
            result[i] = NULL_VALUE;
        }
        return result;
    }

    private String getValueForField(String field, JSONObject json) {
        String value = NULL_VALUE;
        if (!field.contains(".")) {
            if (json.get(field) != null) {
                value = json.get(field).toString();
            }
        } else {
            String[] fieldObjects = field.split("\\.");
            int counter = 0;
            Object obj = json.get(fieldObjects[counter++]);
            while (counter < fieldObjects.length) {
                if (obj == null) {
                    break;
                }
                obj = ((JSONObject) obj).get(fieldObjects[counter++]);
            }
            value = obj == null ? NULL_VALUE : obj.toString();
        }
        return value.trim();
    }

}
