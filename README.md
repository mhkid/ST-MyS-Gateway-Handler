# IoT
Home IoT senors on MySensors framework and SmartThings platform

## Summary
This is a home IoT project using the arduino, esp8266, and raspberry pi (future).  This is based on the MySensors framework found at mysensors.org.  This also uses the SmartThings hub as the central controller between homemade IoT devices and commercially available IoT devices such as light switches, reed contact sensors, cameras, etc.  The SmartThings hub was what started all this for me.  I bought that and started installing light switches, contact sensors, etc.  When I discovered I could build my own home security system that led me to microcontrollers and the arduino hardware.  With my background as a software engineer/architect this was a fun way to blend that with my love for tinkering and building things around the house.

Many of the design and code ideas were borrowed from others on the SmartThings and MySensors forums as well as mixed in with my own.  This is very much in a discovery and prototyping phase and truly is a hobby project for my own home use.

## IoT Devices

### Pig Pen Project
We raise show pigs for 4H and FFA and there are some automation around that.

**Current**

*Temperature/Humidity Sensor:* A battery powered sensor used to measure the temp/humidity and send that back to the ST hub which triggers automations to turn on/off heat lamps which are plugged into smart outlets in the winter and in the summer it turns a fan on/off.

**Future**
*Water pressure measurement:* I built a self watering system that the pigs hit a nipple to get water that's connected directly to a pressured water system that runs on a pump (not mine, the irrigation district runs).  Occassionaly the pump goes down and leaving the pigs without water, which can be a real problem in the hot summer months.  I'll build and install a pressure sensor that will measure the PSI and send a text message or alert on the ST mobile app so we'll know when it goes down and the ID can be called to come out and get the pump going.  (For some reason the IR doesn't have their own sensor for this??)

*Water flow:* Water consumption is critical for pigs in gaining weight.  Measuring the amount of water they are drinking helps to understand and predict weight gain in show pig.  This would also be an indicator of when filters need to be changed (see water quality below).

*Water quality:* Don't know how yet but would like to build an IoT device that would measure water quality real-time.  I'm building a water filtration/purification system and it would help to tell me when to change filters and keep the animals healthy.

### Horses
**Future**

*Water Valve:* Our horses are currently watered by hand filling a trough.  Every few days the trough needs to be filled.  The plan is to install a float or ultra sonic sensor to measure the water level and when it goes below a certain level a valve is opened to fill the trough and probably sent a text message or alert to tell us the trough has been filled.  As well as report the current level to the ST mobile app so the level can be seen at any time.

*Contact Sensors:* I'm going to put these on pasture gates so I know if they've been left open.  Kids can be forgettful and animals get out.  A text message or notification would be sent to us so someone would know to shut the gate.

### Smarthome
Lots of current things around the house including home security.  However there are seveal future projects I have in mind.

**Future**

*Motion Sensors:* I have a couple commercial sensors I'm using but I've got others I've built, 3D printed the enclosures and am going to deploy to sense motion to turn on/off lights in various locations.  Also going to put a couple at my driveway entrances so I know when people are coming or going.

*Security Cameras:*  I have some cameras now but it would be nice to turn on all cameras if motion is detected in a certain area for instance instead of just the camera that detects motion.  Might have to actually build the camera (raspberry pi maybe?) as I haven't found one with a good API that would allow this.  But there seems to be more of these cameras popping up all the time.

Lots, lots more stuff in my head but I already have a day job!
