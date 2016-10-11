# Wisper Library for Java

## Overview

wisper-java contains source code for wisper-java.jar library which is a JAR that could be imported in plain Java or Android projects. The library has no dependencies on the Android runtime. This library is an implementation of the wisper protocol, a JSON RPC based protocol invented by authors at Widespace.

Wisper could be used to deliver messages to and from wisper-compliant classes that are registered under a certain route.

## How to use

#### Building
`$ ./gradlew clean build`

The built `jar` is available in `build/libs/`

#### Running unit tests
`$ ./gradlew clean build test -i`

## Contributors
- Ehssan Hoorvash (ehssan.hoorvash@widespace.com)
- Patrik Nyblad (patrik.nyblad@widespace.com)
- Oskar Segersvärd (oskar.segersvard@widespace.com)
- Rana Hamid (rana.hamid@widespace.com)
