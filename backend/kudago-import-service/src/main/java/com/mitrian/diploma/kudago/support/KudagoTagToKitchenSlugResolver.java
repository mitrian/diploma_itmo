package com.mitrian.diploma.kudago.support;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class KudagoTagToKitchenSlugResolver {

	private static final Map<String, String> ALIASES = Map.ofEntries(
		Map.entry("european", "european"),
		Map.entry("европейская", "european"),
		Map.entry("russian", "russian"),
		Map.entry("русская", "russian"),
		Map.entry("american", "american"),
		Map.entry("американская", "american"),
		Map.entry("italian", "italian"),
		Map.entry("итальянская", "italian"),
		Map.entry("japanese", "japanese"),
		Map.entry("японская", "japanese"),
		Map.entry("pan asian", "pan-asian-cuisine"),
		Map.entry("pan asian cuisine", "pan-asian-cuisine"),
		Map.entry("pan-asian", "pan-asian-cuisine"),
		Map.entry("паназиатская", "pan-asian-cuisine"),
		Map.entry("паназиатская кухня", "pan-asian-cuisine"),
		Map.entry("chinese", "chinese"),
		Map.entry("китайская", "chinese"),
		Map.entry("thai", "thai"),
		Map.entry("тайская", "thai"),
		Map.entry("тайская кухня", "thai"),
		Map.entry("korean", "korean"),
		Map.entry("корейская", "korean"),
		Map.entry("georgian", "georgian"),
		Map.entry("грузинская", "georgian"),
		Map.entry("armenian", "armenian"),
		Map.entry("армянская", "armenian"),
		Map.entry("caucasus", "caucasus"),
		Map.entry("кавказская", "caucasus"),
		Map.entry("кавказ", "caucasus"),
		Map.entry("indian", "indian"),
		Map.entry("индийская", "indian"),
		Map.entry("mexican", "mexican"),
		Map.entry("мексиканская", "mexican"),
		Map.entry("middle eastern", "middle-eastern"),
		Map.entry("middle-eastern", "middle-eastern"),
		Map.entry("ближневосточная", "middle-eastern"),
		Map.entry("orient", "middle-eastern"),
		Map.entry("german", "german"),
		Map.entry("немецкая", "german"),
		Map.entry("spanish", "spanish"),
		Map.entry("испанская", "spanish"),
		Map.entry("street food", "street-food"),
		Map.entry("street-food", "street-food"),
		Map.entry("стритфуд", "street-food"),
		Map.entry("fast food", "fast-food"),
		Map.entry("fast-food", "fast-food"),
		Map.entry("фастфуд", "fast-food"),
		Map.entry("vegetarian", "vegetarian"),
		Map.entry("vegeterian dishes", "vegetarian"),
		Map.entry("вегетарианская", "vegetarian"),
		Map.entry("ethnic cuisine", "ethnic-cuisine"),
		Map.entry("этническая", "ethnic-cuisine"),
		Map.entry("gastronomic cuisine", "gastronomic"),
		Map.entry("grande cuisine", "gastronomic"),
		Map.entry("gastronomic", "gastronomic"),
		Map.entry("гастрономическая", "gastronomic"),
		Map.entry("авторская", "gastronomic"),
		Map.entry("asia", "pan-asian-cuisine"),
		Map.entry("азия", "pan-asian-cuisine")
	);

	private KudagoTagToKitchenSlugResolver() {
	}

	public static Optional<String> resolve(String rawTag, Set<String> allowedSlugs) {
		if (rawTag == null) {
			return Optional.empty();
		}
		String trimmed = rawTag.trim();
		if (trimmed.isEmpty()) {
			return Optional.empty();
		}
		String lower = trimmed.toLowerCase(Locale.ROOT);
		String fromAlias = ALIASES.get(lower);
		if (fromAlias != null && allowedSlugs.contains(fromAlias)) {
			return Optional.of(fromAlias);
		}
		String slugified = slugify(lower);
		if (allowedSlugs.contains(slugified)) {
			return Optional.of(slugified);
		}
		return Optional.empty();
	}

	private static String slugify(String lower) {
		String spaced = lower.replace('ё', 'е');
		String withHyphens = spaced.replaceAll("\\s+", "-");
		String ascii = withHyphens.replaceAll("[^a-z0-9\\-]+", "");
		return ascii.replaceAll("-+", "-").replaceAll("^-+", "").replaceAll("-+$", "");
	}
}
