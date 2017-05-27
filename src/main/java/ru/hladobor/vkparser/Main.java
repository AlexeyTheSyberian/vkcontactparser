package ru.hladobor.vkparser;

import ru.hladobor.vkparser.parser.FriendsParser;
import ru.hladobor.vkparser.parser.GroupParser;

import java.io.UnsupportedEncodingException;

/**
 * Created by sever on 25.04.2017.
 */
public class Main {
    private static final String PROPERTIES_PATH = "src/main/resources/vk.properties";
    private static final String OUTPUT_PATH = "output.csv";

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println("Encoding: " + System.getProperty("file.encoding"));
        FriendsParser vkFriendsParser = new FriendsParser();
        vkFriendsParser.parse();

        GroupParser groupParser = new GroupParser();
        groupParser.parse();
    }

}
