## Baza danych

Dostęp do IJ (SQL), komenda: `java org.apache.derby.tools.ij`  
Stworzenie bazy danych: `connect ‘jdbc:derby:/usr/db/<db_name>;create=true’;`  
Podłączenie do bazy danych: `connect ‘jdbc:derby:/usr/db/<db_name>’`;  
Uruchomienie skryptu - stworzenie tabel: `run ‘init.sql’` (po podłączeniu do bazy).

Struktura tabeli SENSOR_INFO:

Nazwa kolumny | Typ
--- | ---
ID | ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)
LOG_TIME | TIMESTAMP
TEMPERATURE | REAL
HUMIDITY | REAL
LIGHT | REAL
SOUND | REAL
PROXIMITY | REAL
THRESHOLD | INTEGER

## Logger

Prosty logger, którego można użyć do logowania informacji z mieszkania pod naszą nieobecność. Korzystamy z następujących komponentów:
- wyświetlacz LCD RGB, port I2C-1,
- czujnik światła, port A0,
- czujnik dźwięku, port A1,
- potencjometr,	port A2,
- czujnik temperatury i wilgotności, port D2,
- dioda LED, port D3,
- odległościomierz,	port D4,
- dioda LED, port D5,
- przycisk, port D6,
- brzęczyk, port D7.

### Funkcjonalności:

- zapis danych z czujników do pliku tekstowego (folder /logs),
  - nazwa pliku: `%y%m%d_%H%M%S.log`
- format linii: `%s %.2f %.2f %.2f %.2f %.2f %d`
  - `timestamp, temperatura, wilgotność, światło, dźwięk, odległość, threshold`.
- zapis odbywa się co jedną sekundę,
- przyciśnięcie przycisku przez dłużej niż sekundę powoduje zakończenie działania,
- informacja dot. obecnie obliczonej odległości przedstawiona jest na wyświetlaczu,
- ekran wyświetlacza podświetlony jest kolorem, którego poszczególne składowe to:
  - `R` - odległość,
  - `G` - poziom światła,
  - `B` - poziom dźwięku,
- druga linia na ekranie zajęta jest przez wartość dot. alarmu dźwiękowego,
- jeśli odległość jest większa niż ustawiona wartość alarmu, alarm podniesie się.

Ponadto, skrypt po zakończeniu działania (po przyciśnięciu przycisku) wystawia plik semafora służący drugiemu skryptowi (tzw. `watcher’owi`) uruchomienie skryptu (`loader'a`) służącego do pobrania danych z pliku tekstowego i wstawienie ich do bazy danych. Sugerowanym sposobem na zakończenie logowania jest przyciśnięcie przycisku (port `D6`). Wtedy tworzy się semafor.

## Watcher

`Watcher` to prosty skrypt napisany w Bashu, który w pętli nasłuchuje na pojawienie się pliku o rozszerzeniu .sem w danym folderze. Po wykryciu takiego pliku, usuwa on semafor i przekazuje nazwę pliku do skryptu loadera, by wstawić rekordy do bazy danych. Parametry:
- nasłuchiwanie na pliki o nazwie: `<nazwa_pliku>.txt.sem`,
- nasłuchiwanie działa w pętli nieskończonej,
- ścieżka do folderu jest pierwszym argumentem skryptu,
- uruchomienie: 
  - `./watcher.sh <folder_z_logami>`

## Loader

`Loader` to program napisany w Javie, który służy do pobrania danych z pliku tekstowego. Nazwa tego pliku jest pierwszym argumentem pliku tekstowego. Służy ono do wczytania danych do bazy Apache Derby. Aby program poprawnie się wykonał, nie może istnieć połączenie do bazy z poziomu `ij`. Łączymy się do `Derby` w trybie `embedded`, nie klient - serwer, więc musimy mieć połączenie na wyłączność. 

Jeżeli wciąż instancja bazy jest uruchomiona, należy ją zamknąć komendą: `connect 'jdbc:derby:;shutdown=true';`

Jeżeli wciąż występuje błąd z połączeniem, można spróbować usunąć wszystkie procesy, które korzystają z Derby i jednocześnie usunąć wszystkie pliki `.lck` z folderu bazy danych.

Uruchomienie programu: `java -jar Loader-1.0-SNAPSHOT.jar <nazwa_pliku>.log` (należy go wcześniej zbudować, plik `pom.xml` znajduje się w repozytorium).

## W jaki sposób uruchomić skrypty?

1. W tle uruchamiamy `Watcher`: `./watcher.sh <folder_z_logami>`
2. Uruchamiamy w drugim terminalu Logger: `sudo python Logger/logger.py`
3. Kończymy `Logger` - przyciskiem,
4. Wykrycie semafora, uruchomienie `Loader'a` (automatycznie),
5. `Loader` importuje dane do bazy danych,
6. Wracamy do punktu 2.

## Pozostałe informacje

### Uruchomienie urządzenia

`Raspberry Pi` powinno mieć podłączoną do siebie nakładkę `GrovePi`. Podłączamy kabel microUSB do płytki (zielonej), kabel Ethernet do płytki i wolnego portu w routerze. Podłączamy kabel do gniazdka - powinny zaświecić się diody: `PWR` i `ACT` na Raspberry Pi i `PWR` na GrovePi. Jeśli tak, możemy przejść dalej.

### Podłączenie do płytki

Możemy teraz podłączyć się do płytki przez dwa narzędzia:
- `PuTTY` - tryb konsolowy,
- `VNC Viewer` - tryb graficzny,
- `WinSCP` - menadżer plików.

IP można znaleźć za pomocą programu `Advanced Ip Scanner`. Program ten służy do przeskanowania lokalnej sieci w poszukiwaniu urządzeń i nadanych im numerów IP.

###  Opisy najważniejszych metod, opis portów

- `pinMode()` ustawia tryb danego portu na `INPUT` - wejście, `OUTPUT` - wyjście.
- `analogRead(2)` i `digitalRead(2)` czytają z dwóch różnych portów na GrovePi.
  - `analogRead(2)` z portu oznaczonego `A2`,
  - `digitalRead(2)` z portu oznaczonego `D2`.
- Istnieją aliasy na porty analogowe (by użyć metod jak do portów cyfrowych):
```
A0 = D14
A1 = D15
A2 = D16
```
- Stąd analogRead(0) i analogRead(14) zwrócą tę samą wartość.

| Nazwa portu | Typ portu | Zakres wartości |
| --- | --- | --- | 
| A0, A1, A2 (D14, D15, D16) | analogowe | 0-1023
| D2-D8 | cyfrowe, 1-bitowe | 0-1
| D3, D5, D6 | analogowe, + PWM | 0-255 (tylko zapis)

```
grovepi.analogRead(0) - port A0, odczyt 0-1023
grovepi.analogRead(1) - port A1, odczyt 0-1023
grovepi.analogRead(2) - port A2, odczyt 0-1023
grovepi.analogRead(14) - port A0, odczyt 0-1023
grovepi.analogRead(15) - port A1, odczyt 0-1023
grovepi.analogRead(16) - port A2, odczyt 0-1023
grovepi.analogWrite(3, val) - port D3, zapis PWM 0-255
grovepi.analogWrite(5, val) - port D5, zapis PWM 0-255
grovepi.analogWrite(6, val) - port D6, zapis PWM 0-255
grovepi.digitalRead(2) - port D2, odczyt 0-1
grovepi.digitalRead(3) - port D3, odczyt 0-1
grovepi.digitalRead(4) - port D4, odczyt 0-1
grovepi.digitalRead(5) - port D5, odczyt 0-1
grovepi.digitalRead(6) - port D6, odczyt 0-1
grovepi.digitalRead(7) - port D7, odczyt 0-1
grovepi.digitalRead(8) - port D8, odczyt 0-1
grovepi.digitalRead(14) - port A0, odczyt 0-1
grovepi.digitalRead(15) - port A1, odczyt 0-1
grovepi.digitalRead(16) - port A2, odczyt 0-1
grovepi.digitalWrite(2, val) - port D2, zapis 0-1
grovepi.digitalWrite(3, val) - port D3, zapis 0-1
grovepi.digitalWrite(4, val) - port D4, zapis 0-1
grovepi.digitalWrite(5, val) - port D5, zapis 0-1
grovepi.digitalWrite(6, val) - port D6, zapis 0-1
grovepi.digitalWrite(7, val) - port D7, zapis 0-1
grovepi.digitalWrite(8, val) - port D8, zapis 0-1
grovepi.digitalWrite(14, val) - port A0, zapis 0-1
grovepi.digitalWrite(15, val) - port A1, zapis 0-1
grovepi.digitalWrite(16, val) - port A2, zapis 0-1
```

### Apache Derby

Apache Derby do ściągnięcia tutaj: <http://db.apache.org/derby/derby_downloads.html>

Zweryfikować instalację Apache Derby można następującą komendą: `java org.apache.derby.tools.sysinfo`

Żeby zapewnić, że zmienne środowiskowe dostępne są z poziomu konsoli, należy dodać je do plików `~/.bashrc` i `~/.bash_profile`.
