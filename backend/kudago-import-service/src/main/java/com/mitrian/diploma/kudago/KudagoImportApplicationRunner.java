package com.mitrian.diploma.kudago;

import com.mitrian.diploma.kudago.service.KudagoCatalogImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("kudago-import")
@Order(0)
public class KudagoImportApplicationRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KudagoImportApplicationRunner.class);

	private final KudagoCatalogImportService importService;

	public KudagoImportApplicationRunner(KudagoCatalogImportService importService) {
		this.importService = importService;
	}

	@Override
	public void run(ApplicationArguments args) {
		KudagoCatalogImportService.KudagoCatalogImportStats stats = importService.importRestaurantsWithinRadius();
		log.info(
			"KudaGo import: apiListTotal={} withinRadius={} savedOrUpdated={} skippedClosed={} "
				+ "skippedNoCoords={} skippedTooFar={} detailFetchFailures={} persistFailures={}",
			stats.apiListTotal(),
			stats.withinRadius(),
			stats.savedOrUpdated(),
			stats.skippedClosed(),
			stats.skippedNoCoords(),
			stats.skippedTooFar(),
			stats.detailFetchFailures(),
			stats.persistFailures()
		);
	}
}
