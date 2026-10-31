package kr.routineslim.controller;

import java.util.List;
import kr.routineslim.dto.CatalogItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {
  private final JdbcTemplate db;

  public CatalogController(JdbcTemplate db) {
    this.db = db;
  }

  @GetMapping
  public List<CatalogItem> list() {
    return db.query(
        "SELECT name,category,icon,background FROM service_catalog"
            + " WHERE active=TRUE ORDER BY sort_order,name",
        (row, index) ->
            new CatalogItem(
                row.getString("name"),
                row.getString("category"),
                row.getString("icon"),
                row.getString("background")));
  }
}
