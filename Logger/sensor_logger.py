# Logowanie temperatury, poziomu swiatla, dzwieku, odleglosci za pomoca RPi

# GrovePi + Sound Sensor + Light Sensor +
# Temperature Sensor + Ultrasonic Ranger Sensor + LED

# http://www.seeedstudio.com/wiki/Grove_-_Sound_Sensor
# http://www.seeedstudio.com/wiki/Grove_-_Light_Sensor
# http://www.seeedstudio.com/wiki/Grove_-_Temperature_and_Humidity_Sensor_Pro
# http://wiki.seeed.cc/Grove-Ultrasonic_Ranger/
# http://www.seeedstudio.com/wiki/Grove_-_LED_Socket_Kit

'''
## License

The MIT License (MIT)

GrovePi for the Raspberry Pi: an open source platform for connecting Grove Sensors to the Raspberry Pi.
Copyright (C) 2017  Dexter Industries

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
'''

import time
import grovepi
import math
from grovepi import *
from grove_rgb_lcd import *

def pi_sleep(len):
    if(len < 1e-3):
        len = 1e-3

    time.sleep(len)

def safe_exit():
    print('Wyjscie...')
    setRGB(0, 0, 0)
    setText('')
    analogWrite(led, 0)
    analogWrite(led2, 0)

    open(log_file + '.sem', 'a').close()
    
    exit()

timestamp = time.strftime('%Y-%m-%d:%H:%M:%S')
log_file = 'logs/' + timestamp + '.log'

log = open(log_file, 'a')

# Polaczenia
light_sensor = 0	# port A0
sound_sensor = 1        # port A1
temperature_sensor = 2  # port D2
led = 3                 # port D3
ranger = 4		# port D4
led2 = 5		# port D5
button = 6		# port D6
potentiometer = 2	# port A2
buzzer = 7		# port D7

grovepi.pinMode(led, "OUTPUT")
grovepi.pinMode(button, "INPUT")
grovepi.pinMode(buzzer, "OUTPUT")
grovepi.analogWrite(led, 255)
grovepi.analogWrite(led2, 255)

rgbRangerCoeff = 255/(350*1.0) # floaty!
# mapowanie przedzialu [0, 350] na [0, 255] (RGB)

thresholdRangerCoeff = 510/(1024*1.0) # floaty!
# mapowanie przedzialu wejscia analogowego [0, 1023]
# na przedzial odleglosciomierza  [0, 510] - wartosc
# ustalona empirycznie

threshold = 0 # wartosc, od ktorej generowany jest alert dzwiekowy

while True:
    timestamp = time.strftime('%Y-%m-%d:%H:%M:%S')

    # Obsluga bledow w przypadku problemow z polaczeniem
    try:
	if digitalRead(button):
	    safe_exit()

        light_intensity = grovepi.analogRead(light_sensor)
	grovepi.analogWrite(led, light_intensity / 4)

        sound_level = grovepi.analogRead(sound_sensor)
	proximity = ultrasonicRead(ranger)
	grovepi.analogWrite(led2, proximity / 4)	

        time.sleep(0.5)

	threshold = grovepi.analogRead(2)
	threshold = threshold * thresholdRangerCoeff;

	rgb = rgbRangerCoeff * proximity
	setRGB(int(round(rgb)), light_intensity / 4, sound_level / 4)

        [t, h] = [0, 0]
        [t, h] = grovepi.dht(temperature_sensor, 0)

	if(proximity > threshold):
		digitalWrite(buzzer, 1)
	else:
		digitalWrite(buzzer, 0)

	# temperatura, wilgotnosc, swiatlo, dzwiek, odleglosc, threshold
        results = "%s %.2f %.2f %.2f %.2f %.2f %d" %(timestamp, t, h, light_intensity/10, sound_level, proximity, threshold)
        print (results)
        log.write(results + '\n')

	setText('Proxmty: %d cm\nThrshld: %d cm' % (proximity, threshold))
    except IOError:
        print("Error")
        safe_exit()
    except KeyboardInterrupt:
        safe_exit()

    time.sleep(1)

log.close()
safe_exit()