# Eclipse Kapua™ Micro Client SDK [![Build status](https://api.travis-ci.org/ctron/kapua-micro-client.svg)](https://travis-ci.org/ctron/kapua-micro-client) [![Maven Central](https://img.shields.io/maven-central/v/de.dentrassi.kapua/kapua-micro-client.svg "Maven Central Status")](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22de.dentrassi.kapua%22%20AND%20a%3A%22kapua-micro-client%22)

This project provides a minimal SDK for connecting to [Eclipse Kapua](https://eclipse.org/kapua) as a gateway.
It is targeted to use only minimal dependencies and run on constrained devices and profiles (like the CLDC 8).

**Note:** This is not part of the Eclipse Kapua project.

**Note:** This is a work in progress and should not be considered production ready.

If you plan to run this in Java 8+ SE, then please use [ctron/kapua-gateway-client](https://github.com/ctron/kapua-gateway-client "ctron/kapua-gateway-client repository") instead. The main difference between this project and the `kapua-gateway-client` is, that this project only uses minal dependencies and Java 7 as a base. While the other project makes use of Java 8 to provide a nice API and a simple, modular, extensible architecture and implementation. But sometimes it may be necessary
to ditch all the fun and concentrate on the most simple solution.

Also see:
 * [Project Information](https://ctron.github.io/kapua-micro-client/) 
 * [API docs](https://ctron.github.io/kapua-micro-client/apidocs/) 

## How to use

The following quick steps should provide you with a working example.

### Add to your Maven project

```xml
<dependency>
  <groupId>de.dentrassi.kapua</groupId>
  <artifactId>kapua-micro-client</artifactId>
  <version><!-- replace with current version --></version>
</dependency>
```

## About dependencies and target environment

This client targets small embedded devices and thus doesn't make use of external dependencies
aside from Eclipse Paho for MQTT and Google Protobuf for Kura message encoding format.

It also tries to be conservative when it comes to using Java APIs in order to run on
e.g. the Java CLDC 8 profile.

This is why some things may seem a little bit odd and redundant. But for example the CLDC 8
doesn't offer `java.lang.Void` as a type, so this library has to provide its own type named
`Nothing` instead. Choosing `Void` as an alternative creates some problems as `java.lang.Void`
is located under `lava.lang` and thus doesn't need to be imported. Defining a custom `Void` would
thus create name clashes all the time.

## Things known to work and not to work

Let me know if you tested this elsewhere, I am happy to list your findings.

* Works in Java SE 7+
* Works on MicroEJ 4.1 – Tested on the STM32F746G-DISCO board
