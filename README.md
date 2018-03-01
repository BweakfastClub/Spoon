## allrecipes-scraper

### Usage: 
```shell
(replace gradlew with gradlew.bat if you're on Windows)
gradlew run -PappArgs="--scrape --ingest"
```

### Options:
You will have to pass all the following options as a String inside `-PappArgs`:
```
-h          Show the help screen
---scrape   Scrape the recipe data from AllRecipes
--ingest    Ingest the scraped data and save it into Cassandra
```