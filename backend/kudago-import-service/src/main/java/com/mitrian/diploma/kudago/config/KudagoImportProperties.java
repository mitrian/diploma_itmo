package com.mitrian.diploma.kudago.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "diploma.kudago-import")
public class KudagoImportProperties {

	private String baseUrl = "https://kudago.com/public-api/v1.4";

	private String location = "spb";

	private String categories = "restaurants,bar,brewery,rynok";

	private double centerLat = 59.956346;

	private double centerLon = 30.309968;

	private int radiusMeters = 4000;

	private int listPageSize = 100;

	private String httpImportToken = "";

	private boolean scheduledImportEnabled = true;

	private String scheduledImportCron = "0 0 4 1 * ?";

	private String scheduledImportZone = "Europe/Moscow";

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCategories() {
		return categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}

	public double getCenterLat() {
		return centerLat;
	}

	public void setCenterLat(double centerLat) {
		this.centerLat = centerLat;
	}

	public double getCenterLon() {
		return centerLon;
	}

	public void setCenterLon(double centerLon) {
		this.centerLon = centerLon;
	}

	public int getRadiusMeters() {
		return radiusMeters;
	}

	public void setRadiusMeters(int radiusMeters) {
		this.radiusMeters = radiusMeters;
	}

	public int getListPageSize() {
		return listPageSize;
	}

	public void setListPageSize(int listPageSize) {
		this.listPageSize = listPageSize;
	}

	public String getHttpImportToken() {
		return httpImportToken;
	}

	public void setHttpImportToken(String httpImportToken) {
		this.httpImportToken = httpImportToken;
	}

	public boolean isScheduledImportEnabled() {
		return scheduledImportEnabled;
	}

	public void setScheduledImportEnabled(boolean scheduledImportEnabled) {
		this.scheduledImportEnabled = scheduledImportEnabled;
	}

	public String getScheduledImportCron() {
		return scheduledImportCron;
	}

	public void setScheduledImportCron(String scheduledImportCron) {
		this.scheduledImportCron = scheduledImportCron;
	}

	public String getScheduledImportZone() {
		return scheduledImportZone;
	}

	public void setScheduledImportZone(String scheduledImportZone) {
		this.scheduledImportZone = scheduledImportZone;
	}
}
