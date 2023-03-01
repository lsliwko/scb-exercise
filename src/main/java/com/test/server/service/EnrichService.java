package com.test.server.service;

import com.test.server.exception.CsvException;
import com.test.server.service.model.CsvModel;
import com.test.server.service.mapper.EnrichFieldMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

@Service
public class EnrichService {

    final static private Logger logger = LoggerFactory.getLogger(EnrichService.class);

    //this method can be used to enrich wide variety of cvs types by providing different processors
    public void enrichCsv(
            Reader csvReader,
            Writer enrichedCsvWriter,
            CsvModel csvModel,
            EnrichFieldMapper enrichFieldMapper
    ) throws IOException, CsvException {

        try (
                ICsvMapReader inputMapReader = new CsvMapReader(csvReader, CsvPreference.STANDARD_PREFERENCE);
                ICsvMapWriter outputMapWriter = new CsvMapWriter(enrichedCsvWriter, CsvPreference.STANDARD_PREFERENCE);
        ) {
            final String inputFieldName = enrichFieldMapper.getInputFieldName();
            final String outputFieldName = enrichFieldMapper.getOutputFieldName();

            // the header columns are used as the keys to the Map
            final String[] inputHeaders = inputMapReader.getHeader(true);
            if (!ArrayUtils.contains(inputHeaders,inputFieldName)) throw new CsvException(String.format("Input csv doesn't contain '%s'", inputFieldName));
            if (ArrayUtils.contains(inputHeaders,outputFieldName)) throw new CsvException(String.format("Input csv already contains '%s'", outputFieldName));

            // prepare output headers
            final String[] outputHeaders = Arrays.copyOf(inputHeaders, inputHeaders.length);
            outputHeaders[ArrayUtils.indexOf(outputHeaders, inputFieldName)] = outputFieldName;

            // write headers
            outputMapWriter.writeHeader(outputHeaders);

            Map<String, Object> inputValuesMap;
            while (true) {  //while loop is v-fast (vs. streams)

                //catch any parse exceptions
                try {
                    inputValuesMap = inputMapReader.read(inputHeaders, csvModel.getProcessors());
                } catch (SuperCsvCellProcessorException e) {
                    logger.error(String.format(
                            "Error reading csv line no %d [%s]: %s",  inputMapReader.getLineNumber(), inputMapReader.getUntokenizedRow(), e.getMessage()),
                            e);
                    //skip line
                    continue;
                }

                //nothing more to read?
                if (inputValuesMap==null) break;

                //enrich csv row
                final Object inputFileValue = inputValuesMap.get(inputFieldName);
                final Optional<String> outputFieldValueOption = enrichFieldMapper.findOutputValueById(Objects.toString(inputFileValue));

                //default value if missing
                final Object outputFieldValue = outputFieldValueOption.orElseGet(() -> enrichFieldMapper.defaultValue());

                //work on writable copy
                Map<String,Object> outputValuesMap    = new HashMap<>(inputValuesMap);

                //exchange field
                outputValuesMap.remove(inputFieldName);
                outputValuesMap.put(outputFieldName, outputFieldValue);

                outputMapWriter.write(outputValuesMap, outputHeaders);
            }
        }
    }
}
