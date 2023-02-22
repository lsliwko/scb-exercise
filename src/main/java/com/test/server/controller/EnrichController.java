package com.test.server.controller;

import com.test.server.exception.CsvException;
import com.test.server.service.EnrichService;
import com.test.server.service.mapper.ProductNameEnrichFieldMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1")
public class EnrichController {
    final static public String TEXT_CSV_VALUE  = "text/csv";

    final static private Logger logger = LoggerFactory.getLogger(EnrichController.class);

    @Autowired
    private ProductNameEnrichFieldMapper productNameEnrichFieldMapper;

    @Autowired
    private EnrichService enrichService;

    @RequestMapping(
            value = "/enrich",
            consumes = TEXT_CSV_VALUE,
            produces = TEXT_CSV_VALUE,
            method = RequestMethod.POST
    )
    @ResponseBody
    public void enrichTradeCsv(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType(TEXT_CSV_VALUE);
        response.setCharacterEncoding(request.getCharacterEncoding());  //carry over encoding

        try (
                Reader reader = new InputStreamReader(request.getInputStream(), request.getCharacterEncoding());
                Writer writer = new OutputStreamWriter(response.getOutputStream(), request.getCharacterEncoding())
        ) {
            enrichService.enrichCsv(reader, writer, enrichService.TRADE_CSV_PROCESSORS, productNameEnrichFieldMapper);
        } catch (IOException e) {
            //critical error
            logger.error("Error enriching trade csv (internal server error)", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (CsvException e) {
            //reportable error
            logger.error("Error enriching trade csv (bad request)", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

}
