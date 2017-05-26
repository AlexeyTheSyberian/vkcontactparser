package ru.hladobor.vkparser.output;

import java.io.UnsupportedEncodingException;

/**
 * Created by sever on 25.05.2017.
 */
public abstract class OutputService implements AutoCloseable {
    public abstract void fillHeader(String[] headers);

    public abstract void fillRow(String[] outputValues);

    protected String encodeToWin1251(String strUtf8){
        String output = strUtf8;
        try {
            output = new String(strUtf8.getBytes("Cp1251"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            output += ";Error while encoding to WINDOWS-1251";
        }
        return output;
    }
}
