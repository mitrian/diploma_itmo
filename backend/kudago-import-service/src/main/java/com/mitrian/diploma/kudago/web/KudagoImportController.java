package com.mitrian.diploma.kudago.web;

import com.mitrian.diploma.kudago.config.KudagoImportProperties;
import com.mitrian.diploma.kudago.service.KudagoCatalogImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/kudago")
public class KudagoImportController {

	private final KudagoImportProperties properties;

	private final KudagoCatalogImportService importService;

	public KudagoImportController(KudagoImportProperties properties, KudagoCatalogImportService importService) {
		this.properties = properties;
		this.importService = importService;
	}

	@PostMapping("/import")
	public ResponseEntity<KudagoCatalogImportService.KudagoCatalogImportStats> importCatalog(
		@RequestHeader(value = "X-Import-Token", required = false) String token
	) {
		String expected = properties.getHttpImportToken();
		if (expected == null || expected.isBlank()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		if (token == null || !expected.equals(token)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		return ResponseEntity.ok(importService.importRestaurantsWithinRadius());
	}
}
