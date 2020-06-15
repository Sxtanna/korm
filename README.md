[![Maven Central](https://img.shields.io/maven-central/v/com.sxtanna.korm/Korm.svg?logo=kotlin&style=flat-square)](http://repo1.maven.org/maven2/com/sxtanna/korm/Korm/) [![Travis](https://img.shields.io/travis/com/Sxtanna/KORM.svg?style=flat-square)](https://travis-ci.com/Sxtanna/KORM) [![GitHub](https://img.shields.io/github/license/Sxtanna/KORM.svg?style=flat-square)](https://opensource.org/licenses/MIT)

<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/Sxtanna/korm/">
    <img src="https://i.imgur.com/KNDngsc.png" alt="logo" width="320" height="160">
  </a>

  <h3 align="center">Korm</h3>

  <p align="center">
    Kotlin Object Relational Mapping
    <br />
    <a href="https://github.com/Sxtanna/korm/wiki"><strong>Unimplemented Wiki :) »</strong></a>
    <br />
    <br />
    <a href="https://github.com/Sxtanna/korm/blob/master/src/test/kotlin/com/sxtanna/korm/Examples.kt">View Demo</a>
    ·
    <a href="https://github.com/Sxtanna/korm/issues">Report Bug</a>
    ·
    <a href="https://github.com/Sxtanna/korm/issues">Request Feature</a>
    ·
    <a href="https://github.com/Sxtanna/korm/pulls">Send a Pull Request</a>
  </p>
</p>


#### This is basically a stripped down Json with slightly different syntax. This readme is going to follow the same format as [TOML's](https://github.com/toml-lang/toml)
#### At it's core, Korm is meant to be purely a transactional format, meaning the document should always be a representation of some object.


Example
-------
```kotlin
title: "KORM Example"

owner: {
  name: "Sxtanna"
  date: "03/07/2018"
}

database: {
  server: "192.168.1.1"
  ports: [1234, 5678, 9100]
  maxConnections: 150
  enabled: true
}

servers: {
  alpha: {
    ip: "10.0.0.1"
    dc: "random"
  }
  omega: {
    ip: "10.0.0.2"
    dc: "random"
  }
}

clients: {
  data: [["gamma", "delta"], [1, 2]]
}

hots: [
  "alpha",
  "omega"
]
```

Getting Korm
--------
### Gradle
```groovy
compile "com.sxtanna.korm:Korm:+"
```

### Maven
```xml
<dependency>
    <groupId>com.sxtanna.korm</groupId>
    <artifactId>Korm</artifactId>
    <version>LATEST</version>
</dependency>
```

Basic Usage
--------
```kotlin
data class User(val name: String, val pass: String)

val korm = Korm()
val user = User(name = "Sxtanna", pass = "BadPassword")

// converting from object to korm
val push = korm.push(user)
println(push) // user: "Sxtanna"
              // pass: "BadPassword"


// converting from korm to object
val pull = korm.pull(push).to<User>()
println(pull) // User(name=Sxtanna, pass=BadPassword)

val same: Boolean = user == pull
println(same) // true
```

Symbols
--------
Symbols come in two formats, basic and complex.

#### Basic
```kotlin
basicKey: 21 // A basic key assigned to the number `21`
```

In json:
```json
{
  "basicKey": 21
}
```


#### Complex
```kotlin
`char: 'C' hour: 12`: "Hello" // A complex key assigned to the word `"Hello"`
```

In json:
```json
{
  "{ "char": 'C', "hour": 12 }": "Hello"
}
```

As you can see, a complex key can be literally anything, they are parsed as their own separate Korm document, which can then be parsed into whatever object they represent.
Symbols directly represent either field names, or hash keys.


Symbol:Assignment
--------
The most basic component of Korm is a symbol and it's assignment, in korm, all symbols must be assigned to something. But data may be free of a symbol. (In this case the entire document would be a single object)

```kotlin
symbol: 21
```
```kotlin
[1, 2, 3, 4, 5, 6] // free `List<Int>`
```


List
--------
Lists should always be a single complex type, or basic types. (Basic or "primitive" types are resolved eagerly and can be used heterogeneously)

Homogeneous List
```kotlin
numbers: [1, 2, 3, 4, 5] // List<Int>
```

Heterogeneous List
```kotlin
numbers: [1, 2, 3.0, 40000] // Can be resolved to `List<Number>` since all components are of `Number`
```

```kotlin
data class Age(val age: Int)

ages: [{ age: 12 }, { age: 14 }, { age: 16 }] // Can be resolved to `List<Age>` (or any collection you want).

ages: [21, { age: 23 }] // Will result in either a null result for whichever type isn't supplied, or an error when resolving. Complex lists must always be homogeneous.
```

Hash
--------
Hash types behave the same way as described above for Lists.

Homogeneous Hash
```kotlin
pairs: { key0: 0 key1: 1 key2: 2 } // Map<String, Int>
```

Heterogeneous Hash
```kotlin
pairs: { key0: 0 key1: 1.0 key2: 2 } // Map<String, Number>
```

```kotlin
ages: {  // Map<Age, Int>
  `{ age: 21 }`: 21
  `{ age: 22 }`: 22
  `{ age: 23 }`: 23
}
```


Massive Improvements due to Java Profiling with [![GitHub](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png)](https://www.ej-technologies.com/products/jprofiler/overview.html) 
