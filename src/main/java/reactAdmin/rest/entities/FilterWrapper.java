package reactAdmin.rest.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@RequiredArgsConstructor
public class FilterWrapper {
    private final JSONObject filter;
    private final JSONArray range;
    private final JSONArray sort;

}
