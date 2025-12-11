package ccf.ccf.enforcement;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SagaContext {
    private String sagaId;
    private Map<String, Object> data = new HashMap<>();
    private boolean completed;
    private boolean compensating;

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }
}