## Поток работы с KudaGo API для выбора заведений общепита

**Базовый URL**: `https://kudago.com/public-api/v1.4`

Во всех запросах ниже предполагается:

- `lang=ru`
- `text_format=text` (тексты без HTML)
- город по умолчанию `spb` (можно заменить на `msk` и др.).

---

## 1. Получить список городов

**Задача**: узнать доступные города и выбрать `slug` города для дальнейших запросов.

```bash
curl "https://kudago.com/public-api/v1.4/locations/?lang=ru&fields=slug,name,coords,timezone"
```

**Пример ответа (фрагмент)**:

```json
[
  {
    "slug": "spb",
    "name": "Санкт-Петербург",
    "coords": { "lat": 59.939095, "lon": 30.315868 },
    "timezone": "GMT+03:00"
  },
  {
    "slug": "msk",
    "name": "Москва",
    "coords": { "lat": 55.753676, "lon": 37.619899 },
    "timezone": "GMT+03:00"
  }
]
```

**Важные поля**:

- `slug` – короткий код города (`spb`, `msk`, `ekb` и т.п.).  
- `name` – название города.

**Как использовать**:

- сохранить список городов и дать пользователю выбрать один `slug`;  
- во всех следующих запросах подставлять его в параметр `location`.

---

## 2. Получить категории и выбрать категории общепита

**Задача**: узнать все категории мест и выделить только те, что относятся к общепиту.

```bash
curl "https://kudago.com/public-api/v1.4/place-categories/?lang=ru&fields=id,slug,name&order_by=slug"
```

**Пример ответа (фрагмент)**:

```json
[
  { "id": 15, "slug": "restaurants", "name": "Рестораны и кафе" },
  { "id": 19, "slug": "bar",         "name": "Бары и пабы" },
  { "id": 134,"slug": "brewery",     "name": "Пивоварня" },
  { "id": 136,"slug": "rynok",       "name": "Рынок" },
  { "id": 17, "slug": "anticafe",    "name": "Антикафе" }
]
```

**Категории общепита, которые стоит использовать**:

- `restaurants` – «Рестораны и кафе» (основная категория).  
- `bar` – «Бары и пабы».  
- `brewery` – «Пивоварня».  
- `rynok` – «Рынок» (фуд‑корты и рынки с едой, не факт, что стоит юзать).

Опционально, если нужно расширить выбор:

- `anticafe` – «Антикафе».  
- `clubs` – «Клубы».  
- `strip-club` – «Стрип-клуб».

**Как использовать**:

- сформировать массив «разрешённых» категорий  
- подставлять их в параметр `categories` при запросе `/places/`.

---

## 3. Получить места общепита в выбранном городе

**Задача**: получить список заведений (рестораны, бары и т.п.) с базовой информацией.

Простейший запрос для города `spb`:

```bash
curl "https://kudago.com/public-api/v1.4/places/?location=spb&categories=restaurants,bar,brewery,rynok&fields=id,title,address,coords,categories,tags,site_url,is_closed&text_format=text&page=1&page_size=50"
```

**Пример ответа (фрагмент)**:

```json
{
  "count": 103,
  "next": null,
  "previous": null,
  "results": [
    {
      "id": 33697,
      "title": "digital-ресторан «Игристые»",
      "address": "пер. Гривцова, д. 13/11",
      "site_url": "https://kudago.com/spb/place/restoran-igristyie/",
      "coords": { "lat": 59.927163, "lon": 30.316651 },
      "categories": ["restaurants", "bar", "sights"],
      "tags": ["european", "restaurants", "panorama", "..."],
      "is_closed": false
    },
    {
      "id": 35320,
      "title": "Chang & Kuta",
      "address": "ул. Некрасова, д. 1/38",
      "site_url": "https://kudago.com/spb/place/restoran-chang-and-kuta/",
      "coords": { "lat": 59.938792, "lon": 30.350497 },
      "categories": ["restaurants"],
      "tags": ["pan asian cuisine", "pan asian", "restaurants", "..."],
      "is_closed": false
    }
  ]
}
```

**Важные моменты**:

- `location` – город (slug из шага 1).  
- `categories` – список категорий общепита из шага 2.  
- `page` и `page_size` – пагинация (для обхода всей выборки).  
- `is_closed` – заведения с `true` лучше отфильтровать на своей стороне.

**Как использовать**:

- постранично обойти все страницы и сохранить заведения в свою БД 
- хранить `id` KudaGo как внешний идентификатор;  
- показывать пользователю только те заведения, где `is_closed=false`.

При необходимости можно дополнительно ограничить выборку по расстоянию от точки:

```bash
curl "https://kudago.com/public-api/v1.4/places/?location=spb&categories=restaurants,bar,brewery,rynok&lon=30.3159&lat=59.9391&radius=5000&fields=id,title,address,coords,categories,tags,site_url,is_closed&text_format=text"
```

где `lat`, `lon` – координаты точки, `radius` – радиус в метрах.

---

## 4. Как искать заведения по виду кухни

У KudaGo **нет отдельного справочника кухонь**. Тип кухни хранится в тегах (`tags`) объекта `place`.

### 4.1. Собрать список доступных кухонь из тегов

**Задача**: один раз пройтись по ресторанам и вытащить все «кухонные» теги.

Пример запроса (берём рестораны и смотрим их `tags`):

```bash
curl "https://kudago.com/public-api/v1.4/places/?location=spb&categories=restaurants&fields=id,title,tags&text_format=text&page=1&page_size=100"
```

**Пример ответа (фрагмент)**:

```json
{
  "count": 103,
  "next": null,
  "previous": null,
  "results": [
    {
      "id": 35320,
      "title": "Chang & Kuta",
      "tags": [
        "про еду",
        "pan asian cuisine",
        "asia",
        "pan asian",
        "restaurants",
        "..."
      ]
    }
  ]
}
```

Дальше в коде:

- собрать уникальные значения из массива `tags`;  
- руками/логикой отделить именно **виды кухни** (например, `european`, `pan asian`, `chinese`, `thai`, `vegetarian` и т.д.);  
- сохранить этот список как справочник «кухонь» в своём приложении.

Чтобы охватить больше ресторанов, нужно обойти несколько страниц (`page=1,2,3,...`).

### 4.2. Фильтровать места по нужной кухне

Когда есть выбранный тег кухни, можно фильтровать запросом по параметру `tags`.

Важно: **в параметре `tags` нужно указывать не текст из ответа, а его slug** — обычно это та же строка, но с дефисами вместо пробелов.  
Например, для тега, который в ответе выглядит как `pan asian cuisine`, правильный slug будет `pan-asian-cuisine`.

Пример: паназиатская кухня в СПб (slug `pan-asian-cuisine`):

```bash
curl "https://kudago.com/public-api/v1.4/places/?location=spb&categories=restaurants,bar,brewery,rynok&tags=pan-asian-cuisine&fields=id,title,address,coords,tags,categories,site_url&text_format=text"
```

**Пример ответа (фрагмент)**:

```json
{
  "count": 4,
  "results": [
    {
      "id": 35320,
      "title": "Chang & Kuta",
      "address": "ул. Некрасова, д. 1/38",
      "categories": ["restaurants"],
      "tags": ["pan asian cuisine", "pan asian", "restaurants", "..."]
    }
  ]
}
```

**Как использовать во флоу приложения**:

1. Пользователь выбирает город (`locations`).  
2. Приложение загружает и кеширует категории, использует только категории общепита.  
3. На этапе подготовки данных приложение собирает теги из ресторанов и строит список доступных кухонь.  
4. В UI пользователь выбирает вид кухни → на сервере формируется запрос к `/places/` с нужным тегом(ами) в параметре `tags`.

---

## 5. Что можно показать в карточке заведения (детализация места)

**Задача**: получить максимум информации о конкретном месте для отображения «карточки» (название, адрес, кухни, рейтинг, сайт, фото и т.д.).

```bash
curl "https://kudago.com/public-api/v1.4/places/35320/?lang=ru&expand=images&fields=id,title,short_title,slug,address,location,timetable,phone,description,body_text,coords,subway,site_url,foreign_url,age_restriction,images,favorites_count,comments_count,is_closed,categories,tags&text_format=html"
```

**Пример ответа для ресторана (фрагмент)**:

```json
{
    "id": 35320,
    "title": "Chang & Kuta",
    "slug": "restoran-chang-and-kuta",
    "address": "ул. Некрасова, д. 1/38",
    "timetable": "пн–пт 11:00–23:00, сб, вс 10:00–23:00",
    "phone": "+7 911 923-53-30",
    "body_text": "<p>Chang — это место не только с тем самым том ямом, в том числе с королевским и Ultimate версией. Здесь также можно оценить рамэн, рис, роллы, лапшу, ананас с морепродуктами и классические закуски вроде гёдза и спринг-роллов. В качестве десерта — боул с драгонфрутом или чизкейк Сан-Себастьян с мисо-карамелью. </p><p>В заведении на Некрасова с Chang соседствует Kuta, в которой создают булочки с самыми сочными начинками — лайм, манго, юдзу, маракуйя, личи, бананы и кокосы. Именно здесь ввели моду на круглые круассаны: рекомендуем попробовать с золотом, манго и шоколадом. Это отличное место на каждый день, куда можно прийти на завтрак (попробуйте любимый завтрак босса или фруктовые сырники), обед или ужин. </p><p>Футуристичный интерьер подтолкнёт вас к тому, чтобы сделать парочку памятных снимков, количество рыбы в блюдах заставит не раз восхититься позициями, а музыка вызовет желание подпевать любимым трекам. </p>",
    "description": "<p>Кафе Chang, которое называет свой том ям лучшим в городе, и пекарня Kuta, которая предлагает булочки с тропическими начинками, познакомят вас с паназиатской кухней. </p>",
    "site_url": "https://kudago.com/spb/place/restoran-chang-and-kuta/",
    "foreign_url": "https://vk.com/changcafe",
    "coords": {
        "lat": 59.93879150000013,
        "lon": 30.35049680000024
    },
    "subway": "Чернышевская",
    "favorites_count": 1,
    "images": [
        {
            "image": "https://media.kudago.com/images/place/74/0f/740f3bf43227107c7483ada5f3fdcbad.png",
            "thumbnails": {
                "640x384": "https://media.kudago.com/thumbs/640x384/images/place/74/0f/740f3bf43227107c7483ada5f3fdcbad.png",
                "144x96": "https://media.kudago.com/thumbs/144x96/images/place/74/0f/740f3bf43227107c7483ada5f3fdcbad.png"
            },
            "source": {
                "name": "",
                "link": "https://changcafe.ru/"
            }
        },
        {
            "image": "https://media.kudago.com/images/place/f2/89/f289ebb69ea9ad9b54ab658a6f723828.png",
            "thumbnails": {
                "640x384": "https://media.kudago.com/thumbs/640x384/images/place/f2/89/f289ebb69ea9ad9b54ab658a6f723828.png",
                "144x96": "https://media.kudago.com/thumbs/144x96/images/place/f2/89/f289ebb69ea9ad9b54ab658a6f723828.png"
            },
            "source": {
                "name": "",
                "link": "https://changcafe.ru/"
            }
        }
    ],
    "comments_count": 0,
    "is_closed": false,
    "categories": [
        "restaurants"
    ],
    "short_title": "Chang & Kuta",
    "tags": [
        "с друзьями",
        "выпечка",
        "про еду",
        "еда",
        "рестораны и кафе",
        "завтраки",
        "паназиатская",
        "доставка",
        "азия",
        "паназиатская кухня"
    ],
    "location": "spb",
    "age_restriction": null
}
```

**Что из этого можно использовать в карточке ресторана**:

- **Название**: `title` (для краткого — `short_title`, если есть).  
- **Адрес**: `address` +, при необходимости, `subway`.  
- **Город**: `location` (через справочник городов → человекочитаемое имя).  
- **Часы работы**: `timetable`.  
- **Телефон**: `phone`.  
- **Кухни / особенности**: теги `tags` (выделенные как виды кухни + дополнительные фичи типа `street food`, `live music` и т.п.).  
- **Категории**: `categories` (ресторан, бар, рынок и т.д.).  
- **Координаты**: `coords` (для карты и вычисления расстояния/района).  
- **Возрастное ограничение**: `age_restriction` (если релевантно).  
- **Фото**: первый элемент из `images` как обложка, остальные — галерея.  
- **Ссылки**: `site_url` (страница на KudaGo) и `foreign_url` (официальный сайт заведения).  
- **Признак закрытости**: `is_closed` (если `true` — пометка «закрыто» либо скрывать карточку).  
- **Популярность**: `favorites_count`, `comments_count` — можно использовать как прокси‑рейтинг или просто инфо.

Такую детализацию можно вызывать точечно при открытии карточки пользователем или предварительно кешировать для часто используемых мест.


---

## 6. Как автоматически собрать список «кухонных» тегов

### 6.1. Python‑скрипт для сбора всех тегов ресторанов

```python
import requests
from collections import Counter

BASE_URL = "https://kudago.com/public-api/v1.4"

LOCATIONS = ["spb"]  # при желании добавить другие города
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

    for tag, count in tags_counter.most_common():
        print(f"{tag}: {count}")
```

После этого можно вручную/полуавтоматически выбрать теги, которые относятся именно к кухне.

### 6.2. Примерный список тегов‑кухонь (по данным KudaGo)

На основе анализа тегов ресторанов можно ориентироваться на такой список:

- `european` – европейская;  
- `russian` – русская;  
- `american` – американская;  
- `italian` – итальянская;  
- `japanese` – японская;  
- `pan asian`, `pan asian cuisine` → slug `pan-asian-cuisine` – паназиатская;  
- `chinese` – китайская;  
- `thai` – тайская;  
- `korean` – корейская;  
- `georgian` – грузинская;  
- `armenian` – армянская;  
- `caucasus` – кавказская;  
- `indian` – индийская;  
- `mexican` – мексиканская;  
- `middle eastern` / `orient` / `eastern` – ближневосточная/восточная;  
- `german` – немецкая;  
- `spanish` – испанская;  
- `street food` – стритфуд;  
- `fast-food` – фастфуд;  
- `vegetarian`, `vegeterian dishes` – вегетарианская;  
- `ethnic cuisine` – этническая кухня (общий тег);  
- `grande cuisine`, `gastronomic cuisine` – «гастрономическая» (скорее стиль, чем кухня).

В проекте можно:

- хранить свой **словарь кухонь**, сопоставляя человекочитаемое название и один/несколько тегов KudaGo;  
- использовать этот словарь при построении фильтров и при отображении списка кухонь в карточке ресторана.


