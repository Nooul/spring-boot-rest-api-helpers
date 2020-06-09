package com.nooul.apihelpers.springbootrest.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class QueryParamWrapper {
    private final JSONObject filter;
    private final JSONArray filterOr;
    private final JSONArray range;
    private final JSONArray sort;

}
