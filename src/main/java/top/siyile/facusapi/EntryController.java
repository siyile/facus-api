package top.siyile.facusapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class EntryController {
    private final EntryRepository repository;
    @Autowired
    public EntryController(EntryRepository repository) {
        this.repository = repository;
    }

    private String content(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder();
        final String template = "%s: %s ";
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            sb.append(String.format(template, entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }

    @GetMapping("/counter")
    public CounterResponse counter() {
        List<Entry> entries = repository.findAll();
        Map<String, Integer> map = new HashMap<>();
        for (Entry entry : entries) {
            map.put(entry.value, map.getOrDefault(entry.value, 0) + 1);
        }
        return new CounterResponse(map.size(), content(map));
    }
}
