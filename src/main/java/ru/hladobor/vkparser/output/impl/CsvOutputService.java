package ru.hladobor.vkparser.output.impl;

import ru.hladobor.vkparser.output.OutputService;

import java.io.*;

/**
 * Created by sever on 25.05.2017.
 */
public class CsvOutputService extends OutputService {

    private FileOutputStream fos;
    private OutputStreamWriter osWriter;
    private PrintWriter writer;

    public CsvOutputService(String outputPath) throws FileNotFoundException, UnsupportedEncodingException {
        fos = new FileOutputStream(outputPath);
        osWriter = new OutputStreamWriter(fos, "cp1251");
        writer = new PrintWriter(osWriter);
    }

    @Override
    public void fillHeader(String[] headers) {
        joinAndOutput(headers);
    }

    @Override
    public void fillRow(String[] outputValues) {
        joinAndOutput(outputValues);
    }

    private void joinAndOutput(String[] output){
        writer.println(encodeToWin1251(String.join(";", output)));
    }

    @Override
    public void close() throws Exception {
        if(writer != null){
            writer.close();
        }
        if(osWriter != null){
            osWriter.close();
        }
        if(fos != null){
            fos.close();
        }
    }
}
