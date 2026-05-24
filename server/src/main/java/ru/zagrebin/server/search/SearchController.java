package ru.zagrebin.server.search;

import org.springframework.web.bind.annotation.*;
import ru.zagrebin.server.data.DbService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final DbService db;
    public SearchController(DbService db) { this.db = db; }

    @GetMapping
    public Map<String, List<?>> search(@RequestParam String query, @RequestParam(required = false) String type, @RequestParam(required = false) String tag) {
        return db.search(query, type, tag);
    }
}
