package ru.hladobor.vkparser.output.impl;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.hladobor.vkparser.output.OutputService;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sever on 25.05.2017.
 */
public class XlsxOuputService extends OutputService {

    private Workbook workbook;
    private Map<String, Sheet> sheetMap;
    private String outputPath;

    private String currentSheetName;

    public XlsxOuputService(String outputPath){
        this.outputPath = outputPath;
        workbook = new HSSFWorkbook();
        sheetMap = new HashMap<>();
    }

    public void addSheet(String sheetName){
        if(!sheetMap.containsKey(sheetName)){
            Sheet newSheet = workbook.createSheet(sheetName);
            sheetMap.put(sheetName, newSheet);
        }
        currentSheetName = sheetName;
    }

    public void setCurrentSheet(String sheetName){
        currentSheetName = sheetName;
    }

    @Override
    public void fillRow(String[] outputValues) {
        Sheet sheet = sheetMap.get(currentSheetName);
        int lastRowNum = sheet.getLastRowNum();
        int currRowNum = lastRowNum+1;
        if(lastRowNum == 0){
            int physLastRowNum = sheet.getPhysicalNumberOfRows();
            if(physLastRowNum == 0){
                currRowNum = 0;
            }
        }
        fillRow(sheet, currRowNum, outputValues);
    }

    private void fillRow(Sheet sheet, int rowNum, String[] values){
        Row row = sheet.createRow(rowNum);
        for(int i = 0; i < values.length; i++){
            Cell cell = row.createCell(i);
            cell.setCellValue(encodeToWin1251(values[i]));
        }
    }

    @Override
    public void close() throws Exception {
        try(FileOutputStream fos = new FileOutputStream(outputPath)){
            workbook.write(fos);
        }finally {
            workbook.close();
        }
    }
}
