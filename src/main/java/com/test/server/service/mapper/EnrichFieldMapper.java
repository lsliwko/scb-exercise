package com.test.server.service.mapper;

import java.util.Optional;

public interface EnrichFieldMapper {

    //name of the field to be mapped from when enriching csv
    String getInputFieldName();

    //name of the field to be mapped into when enriching csv
    String getOutputFieldName();

    Optional<String> findOutputValueById(String id);

    String defaultValue();

}
