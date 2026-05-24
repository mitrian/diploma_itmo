import requests
from collections import Counter

BASE_URL = "https://kudago.com/public-api/v1.4"

LOCATIONS = ["spb", "msk"]  # при желании добавить другие города
CATEGORIES = "restaurants,bar,brewery,rynok"

def fetch_places(location: str):
    page = 1
    while True:
        resp = requests.get(
            f"{BASE_URL}/places/",
            params={
                "location": location,
                "categories": CATEGORIES,
                "fields": "id,tags",
                "page": page,
                "page_size": 100,
                "text_format": "text",
            },
            timeout=10,
        )
        resp.raise_for_status()
        data = resp.json()
        for item in data.get("results", []):
            yield item
        if not data.get("next"):
            break
        page += 1

def collect_tags():
    counter = Counter()
    for loc in LOCATIONS:
        for place in fetch_places(loc):
            for tag in place.get("tags", []):
                counter[tag] += 1
    return counter

if __name__ == "__main__":
    tags_counter = collect_tags()

    # Выведем все теги по убыванию частоты
    for tag, count in tags_counter.most_common():
        print(f"{tag}: {count}")