package com.lcm.core.utilities;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class CSVDataManipulator
{
    public ArrayList<String>[] getAllRecordsAsArrayList(final String filePath) throws IOException {
        ArrayList<String>[] inputData = null;
        int n = 0;
        try (final BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String row = br.readLine();
            if (row != null) {
                final String[] cellValues = row.split(",");
                n = cellValues.length;
                inputData = (ArrayList<String>[])new ArrayList[n];
                for (int i = 0; i < n; ++i) {
                    inputData[i] = new ArrayList<String>();
                }
            }
            while (row != null) {
                if (row.contains(",")) {
                    final String[] cellValues = row.split(",");
                    if (cellValues.length == n || cellValues.length > n) {
                        for (int i = 0; i < n; ++i) {
                            if (!cellValues[i].equals("")) {
                                inputData[i].add(cellValues[i]);
                            }
                            else {
                                inputData[i].add("");
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < n - 1; ++i) {
                            if (!cellValues[i].equals("")) {
                                inputData[i].add(cellValues[i]);
                            }
                            else {
                                inputData[i].add("");
                            }
                        }
                        inputData[n - 1].add("");
                    }
                }
                else if (row.equals("")) {
                    for (int i = 0; i < n; ++i) {
                        inputData[i].add("");
                    }
                }
                else {
                    for (int i = 0; i < n; ++i) {
                        if (i == 0) {
                            inputData[i].add(row);
                        }
                        else {
                            inputData[i].add("");
                        }
                    }
                }
                row = br.readLine();
            }
            return inputData;
        }
    }
    
    public Map<String, String> getAllRecordsAsMap(final String filePath) {
        final Map<String, String> resultsMap = new HashMap<>();
        try {
            final ArrayList<String>[] arraylist = this.getAllRecordsAsArrayList(filePath);
            for (int i = 0; i < arraylist[0].size(); ++i) {
                resultsMap.put(arraylist[0].get(i), arraylist[1].get(i));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return resultsMap;
    }

}
