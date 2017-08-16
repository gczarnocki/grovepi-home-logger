# grovepi-logger

## [English]

Home logger project using `Raspberry Pi 3` and `GrovePi` sensors set written in `Bash`, `Python` and `Java` and `Apache Derby` database.

Main purpose of this project is to log GrovePi sensor data into Apache Derby database:
* `Logger` - Python script reading data from sensors and saving to flat .txt file,
* `Watcher` - Bash script watching for semaphore, deleting it and running Java `Loader`,
* `Loader` - Java console app - loading data from flat file to Apache Derby database.

**NOTE: This description is in Polish. For English translation, contact me @ GitHub. ❤**

## Krótki opis projektu

Głównym celem tego projektu było zapisywanie danych zaczytanych z czytników pakietu `GrovePi` za pomocą `Raspberry Pi 3` do bazy `Apache Derby`.

Na projekt składają się następujące części:
- `Logger` - skrypt napisany w Pythonie, czytający dane z czytników i zapisujący je do pliku .txt,
  - po zakończeniu działania tego skryptu, wystawiany jest semafor o nazwie równej nazwie pliku .txt,
- `Watcher` - skrypt Bashowy nasłuchujący na pojawienie się semafora i uruchamiający `Loader`,
- `Loader` - aplikacja konsolowa napisana w Javie, ładuje dane z pliku .txt do bazy Apache Derby.

## Dokładny opis projektu

Dokładny techniczny opis projektu i poszczególnych części znajduje się [pod tym adresem](DESCRIPTION.md).