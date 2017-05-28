package ru.hladobor.vkparser.parser;


import org.apache.commons.lang.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by sever on 27.05.2017.
 */
public class BaseParser {
    protected static String NULL_VALUE = "Не указано";
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

    protected JSONParser parser = new JSONParser();
    protected Properties properties;

    public BaseParser(){
        properties = new Properties();
        try {
            properties.load(new FileInputStream("vk.properties"));
        } catch (IOException ex) {
            System.out.println("Error loading properties from vk.properties: ");
            ex.printStackTrace();
        }
    }

    protected JSONObject parseStringForObject(String content) throws ParseException {
        return (JSONObject) parser.parse(content);
    }

    protected String getValueForField(String field, JSONObject json) {
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

    protected String[] buildUserOutputData(JSONObject json){
        String[] result = null;
        String[] fieldsToOutput = properties.getProperty("fieldsToOuput").split(",");
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

    protected String[] buildHeaderData(){
        String[] fieldsNames = properties.getProperty("fieldsToOuput").split(",");
        String[] universityFieldNames = properties.getProperty("universityFields").split(",");
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
}
