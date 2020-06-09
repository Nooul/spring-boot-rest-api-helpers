package com.nooul.apihelpers.springbootrest.utils;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CsvUtils {

    private CsvUtils() {

    }

    public static <T> List<T> read(Class<T> clazz, InputStream stream, boolean withHeaders, char separator) throws IOException {
        CsvMapper mapper = new CsvMapper();

        mapper.enable(CsvParser.Feature.TRIM_SPACES);
        mapper.enable(CsvParser.Feature.ALLOW_TRAILING_COMMA);
        mapper.enable(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS);
        mapper.enable(CsvParser.Feature.SKIP_EMPTY_LINES);
        mapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        CsvSchema schema = mapper.schemaFor(clazz).withColumnReordering(true);
        ObjectReader reader;
        if (separator == '\t') {
            schema = schema.withColumnSeparator('\t');
        }
        else {
            schema = schema.withColumnSeparator(',');
        }
        if (withHeaders) {
            schema = schema.withHeader();
        }
        else {
            schema = schema.withoutHeader();
        }
        reader = mapper.readerFor(clazz).with(schema);
        return reader.<T>readValues(stream).readAll();
    }
}