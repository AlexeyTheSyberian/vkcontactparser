package ru.hladobor.vkparser.parser;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.StringWriter;

/**
 * Created by sever on 27.05.2017.
 */
public class BaseParser {
    protected static String NULL_VALUE = "Не указано";

    private JSONParser parser = new JSONParser();

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
}
