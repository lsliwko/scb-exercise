package com.test.server.service.model;

import com.test.server.service.constraint.StrDateFormat;
import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.StrRegEx;
import org.supercsv.cellprocessor.ift.CellProcessor;

@Component
public class TradeCsvModel implements CsvModel {

    public static final CellProcessor[] TRADE_CSV_PROCESSORS = new CellProcessor[] {
            new StrDateFormat("yyyyMMdd"), // date in 20221231 format
            new ParseLong(), // product_id in number format
            new StrRegEx("[A-Z]{3}"), // currency in three capital letters format
            new ParseDouble(), // price
    };

    @Override
    public CellProcessor[] getProcessors() {
        return TRADE_CSV_PROCESSORS;
    }
}
