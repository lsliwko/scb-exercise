package com.test.server.service.model;

import org.supercsv.cellprocessor.ift.CellProcessor;

public interface CsvModel {
    CellProcessor[] getProcessors();
}
