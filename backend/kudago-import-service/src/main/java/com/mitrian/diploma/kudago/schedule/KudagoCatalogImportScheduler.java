package com.mitrian.diploma.kudago.schedule;

import com.mitrian.diploma.kudago.service.KudagoCatalogImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
	prefix = "diploma.kudago-import",
	name = "scheduled-import-enabled",
	havingValue = "true",
	matchIfMissing = true
)
public class KudagoCatalogImportScheduler {

	private static final Logger log = LoggerFactory.getLogger(KudagoCatalogImportScheduler.class);

	private final KudagoCatalogImportService importService;

	public KudagoCatalogImportScheduler(KudagoCatalogImportService importService) {
		this.importService = importService;
	}

	@Scheduled(
		cron = "${diploma.kudago-import.scheduled-import-cron}",
		zone = "${diploma.kudago-import.scheduled-import-zone}"
	)
	public void runScheduledCatalogImport() {
		log.info("Scheduled KudaGo catalog import started");
		KudagoCatalogImportService.KudagoCatalogImportStats stats = importService.importRestaurantsWithinRadius();
		log.info(
			"Scheduled KudaGo catalog import finished: apiListTotal={} withinRadius={} savedOrUpdated={} "
				+ "skippedClosed={} skippedNoCoords={} skippedTooFar={} detailFailures={} persistFailures={}",
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
