package top.siyile.facusapi;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data public class Entry {
    @Id
    public String id;

    public String key;
    public String value;

    public Entry() {}

    public Entry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", key, value);
    }
}
