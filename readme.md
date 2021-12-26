# SymAsset

Simulation of an asset with anlog/relay wire resource and power meter for the Sympower control and metering API.

It implements the endpoints for the control and analog API server. By default it starts with two analog and two relay wires, each with one power meter. Additional assets can be added via UI and API.

## Features
- Simulate a balancing resource (analog or relay wire)
- Simulate a power meter, responding to the target level of a resource
- Set error state of resource or meter
- Web UI and REST API for viewing and controlling assets

## Running the application

To run the application locally, execute: `./gradlew run`

## Publishing to Heroku manually

Create a Heroku app using the Heroku CLI:

```$ heroku create```

Build your application, and run the deployHeroku task:

```$ ./gradlew stage deployHeroku```
