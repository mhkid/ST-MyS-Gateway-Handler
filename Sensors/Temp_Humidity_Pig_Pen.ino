/**
 * The MySensors Arduino library handles the wireless radio link and protocol
 * between your home built sensors/actuators and HA controller of choice.
 * The sensors forms a self healing radio network with optional repeaters. Each
 * repeater and gateway builds a routing tables in EEPROM which keeps track of the
 * network topology allowing messages to be routed to nodes.
 *
 * Created by Henrik Ekblad <henrik.ekblad@mysensors.org>
 * Copyright (C) 2013-2015 Sensnology AB
 * Full contributor list: https://github.com/mysensors/Arduino/graphs/contributors
 *
 * Documentation: http://www.mysensors.org
 * Support Forum: http://forum.mysensors.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 *******************************
 *
 * REVISION HISTORY
 * Version 1.0: Henrik EKblad
 * Version 1.1 - 2016-07-20: Converted to MySensors v2.0 and added various improvements - Torben Woltjen (mozzbozz)
 * 
 * DESCRIPTION
 * This sketch provides an example of how to implement a humidity/temperature
 * sensor using a DHT11/DHT-22.
 *  
 * For more information, please visit:
 * http://www.mysensors.org/build/humidity
 * 
 */

// Enable debug prints
//#define MY_DEBUG

// Enable and select radio type attached 
#define MY_RADIO_NRF24
//#define MY_RADIO_RFM69
//#define MY_RS485

// Identify node id because controller isn't attached yet
#define MY_NODE_ID 1
 
#include <SPI.h>
#include <MySensors.h>  
#include <DHT.h>
#include <math.h>


// Set this to the pin you connected the DHT's data pin to
#define DHT_DATA_PIN 3


// Set this offset if the sensor has a permanent small offset to the real temperatures
#define SENSOR_TEMP_OFFSET 0

// Sleep time between sensor updates (in milliseconds)
// Must be >1000ms for DHT22 and >2000ms for DHT11
static const uint64_t UPDATE_INTERVAL = 60000 * 30;  

// Force sending an update of the temperature after n sensor reads, so a controller showing the
// timestamp of the last update doesn't show something like 3 hours in the unlikely case, that
// the value didn't change since;
// i.e. the sensor would force sending an update every UPDATE_INTERVAL*FORCE_UPDATE_N_READS [ms]
static const uint8_t FORCE_UPDATE_N_READS = 10;

// Made the implementation decision to handle the humidty / temperature sensor as a combined device
// for SmartThings for a better UX/UI.  This is handled as CUSTOM sensor type in MyS.  If this is desired to be 
// separate devices in ST, the ST gateway device handler would need to be modified / created as well as
// creating separate ST device handlers for temp / hum. 
#define CHILD_ID_COMBO 0
//#define CHILD_ID_HUM 1

#define VBAT_PER_BITS 0.003363075  // Calculated volts per bit from the used battery montoring voltage divider.   Internal_ref=1.1V, res=10bit=2^10-1=1023, Eg for 3V (2AA): Vin/Vb=R1/(R1+R2)=470e3/(1e6+470e3),  Vlim=Vb/Vin*1.1=3.44V, Volts per bit = Vlim/1023= 0.003363075
#define VMIN 1.9  // Battery monitor lower level. Vmin_radio=1.9V
#define VMAX 3.3  // Battery monitor high level. Vmin<Vmax<=3.44


float lastTemp;
float lastHum;
uint8_t nNoUpdatesTemp;
uint8_t nNoUpdatesHum;
bool metric = false;
char msg[20];
int sensorValue;
const char* t = "T:";
const char* h = "H:";
// Set pin for battery level measurement
int BATTERY_SENSE_PIN = A0;
int oldBatteryPct = 0;


//MyMessage msgHum(CHILD_ID_HUM, V_HUM);
MyMessage msgCombo(CHILD_ID_COMBO, V_VAR1);
DHT dht;


void presentation()  
{ 
  // Register all sensors to gw (they will be created as child devices)
  //present(CHILD_ID_COMBO, S_TEMP, "Temperature");
  //present(CHILD_ID_HUM, S_HUM, "Humidity");
  present(CHILD_ID_COMBO, S_CUSTOM, "TempHumidity");

  // Send the sketch version information to the gateway
  sendSketchInfo("TemperatureAndHumidity", "1.0");

  #ifdef MY_DEBUG
  Serial.println("presentation");
  #endif
  //metric = getControllerConfig().isMetric;
}


void setup()
{

  analogReference(INTERNAL);
  
  dht.setup(DHT_DATA_PIN); // set data pin of DHT sensor
  if (UPDATE_INTERVAL <= dht.getMinimumSamplingPeriod()) {
    Serial.println("Warning: UPDATE_INTERVAL is smaller than supported by the sensor!");
  }
  // Sleep for the time of the minimum sampling period to give the sensor time to power up
  // (otherwise, timeout errors might occure for the first reading)
  sleep(dht.getMinimumSamplingPeriod());
}


void loop()      
{  
  // Force reading sensor, so it works also after sleep()
  dht.readSensor(true);
  
  // Get temperature from DHT library
  float temperature = dht.getTemperature();
  if (isnan(temperature)) {
    Serial.println("Failed reading temperature from DHT!");
  } else if (temperature != lastTemp || nNoUpdatesTemp == FORCE_UPDATE_N_READS) {
    // Only send temperature if it changed since the last measurement or if we didn't send an update for n times
    lastTemp = temperature;
    if (!metric) {
      temperature = dht.toFahrenheit(temperature);
    }
    // Reset no updates counter
    nNoUpdatesTemp = 0;
    temperature += SENSOR_TEMP_OFFSET;
    // Convert to string and add param identifier
    sensorValue = temperature;
    sprintf(msg,"T:%d",sensorValue);   // T:999 no decimal
    
    send(msgCombo.set(msg));

    #ifdef MY_DEBUG
    Serial.print("Temperature: ");
    Serial.println(msg);
    #endif
  } else {
    // Increase no update counter if the temperature stayed the same
    nNoUpdatesTemp++;
  }

  // Get humidity from DHT library
  int humidity = dht.getHumidity();
  if (isnan(humidity)) {
    Serial.println("Failed reading humidity from DHT");
  } else if (humidity != lastHum || nNoUpdatesHum == FORCE_UPDATE_N_READS) {
    // Only send humidity if it changed since the last measurement or if we didn't send an update for n times
#ifdef MY_DEBUG
    Serial.print("humidity: ");
    Serial.print(humidity);
    Serial.print(" | lastHum: ");
    Serial.println(lastHum);
    Serial.print("nNoUpdatesHum: ");
    Serial.print(nNoUpdatesHum);
    Serial.print(" | FORCE_UPDATE_N_READS: ");
    Serial.println(FORCE_UPDATE_N_READS);
#endif
    
    lastHum = humidity;
    // Reset no updates counter
    nNoUpdatesHum = 0;

    sensorValue = humidity;
    sprintf(msg,"H:%d",sensorValue);   // H:99 no decimal

    send(msgCombo.set(msg));
    
#ifdef MY_DEBUG
    Serial.print("Humdity: ");
    Serial.println(msg);
#endif
  } else {
    // Increase no update counter if the humidity stayed the same
    nNoUpdatesHum++;
  }

   
  // get the battery voltage
  int sensorValue = analogRead(BATTERY_SENSE_PIN);

  float Vbat = sensorValue * VBAT_PER_BITS;

#ifdef MY_DEBUG
  Serial.println(sensorValue);
#endif

  int batteryPct = static_cast<int>(((Vbat-VMIN)/(VMAX-VMIN))*100.);
  if (batteryPct > 100) {
    batteryPct = 100;
  }

#ifdef MY_DEBUG
  Serial.print("Battery Voltage: ");
  Serial.print(Vbat);
  Serial.println(" V");

  Serial.print("Battery percent: ");
  Serial.print(batteryPct);
  Serial.println(" %");
#endif

  if (oldBatteryPct != batteryPct) {
      // Power up radio after sleep
      sprintf(msg,"B:%d",batteryPct);   // B:1-100 Battery %

      send(msgCombo.set(msg));
      //sendBatteryLevel(batteryPct);
      oldBatteryPct = batteryPct;
  }

  // Sleep for a while to save energy
  sleep(UPDATE_INTERVAL); 
}


