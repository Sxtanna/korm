# KORM [![Maven Central](https://img.shields.io/maven-central/v/com.sxtanna.korm/Korm.svg?logo=kotlin&style=flat-square)](http://repo1.maven.org/maven2/com/sxtanna/korm/Korm/) [![Travis](https://img.shields.io/travis/com/Sxtanna/KORM.svg?style=flat-square)](https://travis-ci.com/Sxtanna/KORM) [![GitHub](https://img.shields.io/github/license/Sxtanna/KORM.svg?style=flat-square)](https://opensource.org/licenses/MIT)
Kotlin Object-Relational Mapping


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

RefTypes
--------
A [RefType](https://github.com/Sxtanna/KORM/blob/master/src/main/kotlin/com/sxtanna/korm/data/RefType.kt) is a class that takes advantage of Java's superclass generic preservation. 

It should be used anywhere that a generic would be otherwise erased.


Reader
--------
Korm's reader currently has no extra options or functionality. It's job is to read from your input, lex it's contents, and type the results to `KormType` instances

If the only thing you need is to be able to read Korm documents, you can simply create an instance of [KormReader](https://github.com/Sxtanna/KORM/blob/master/src/main/kotlin/com/sxtanna/korm/reader/KormReader.kt)

The `KormReader` provides a couple of read functions that take in different sources

Function | Description | Usage
------------ | ------------- | -------------
`fun read(reader: Reader): ReaderContext` | Creates a context from the text read from the provided `Reader` | `KormReader#read(Reader)`
`fun read(text: String): ReaderContext` | Creates a context from the provided text | `KormReader#read("basicKey: 21")`
`fun read(file: File): ReaderContext` | Creates a context from the text content read from a file | `KormReader#read(File("file.korm"))`
`fun read(stream: InputStream, charset: Charset = Charset.defaultCharset()): ReaderContext` | Creates a context from the text read from the provided `InputStream` | `KormReader#read(InputStream)`


All read implementations of KormReader return an instance of `KormReader.ReaderContext`, which provides several methods of data manipulation.

Function | Description | Usage
------------ | ------------- | -------------
`<T : Any> to(clazz: KClass<T>): T?` | Attempts to map to an instance of `T` | `ReaderContext#to(Int::class)`
`inline fun <reified T : Any> to(): T?` | Reified implementation of ^ | `ReaderContext#to<Int>()`
`fun <T : Any> toRef(type: RefType<T>): T?` | Attempts to map to an instance of `T` from the given `RefType<T>` | `ReaderContext#toRef(RefType.of<Int>())`
`inline fun <reified T : Any> toRef(): T?` | Reified implementation of ^ | `ReaderContext#toRef<Int>()`
`fun <T : Any> toList(clazz: KClass<T>): List<T>` | Attempts to create a list of type `T` | `ReaderContext#toList(String::class)`
`inline fun <reified T : Any> toList(): List<T>` | Reified implementation of ^ | `ReaderContext#toList<String>()`
`fun <T : Any> toListRef(ref: RefType<T>): List<T>`| Attempts to create a list of type `T` from the given `RefType<T>` | `ReaderContext#toListRef(RefType.of<List<String>>())` // would return `List<List<String>>`
`inline fun <reified T : Any> toListRef(): List<T>`| Reified implementation of ^ | `ReaderContext#toListRef<List<String>>()` // would return `List<List<String>>`
`fun <K : Any, V : Any> toHash(kType: KClass<K>, vType: KClass<V>): Map<K, V>` | Attempts to create a map of types `<K, V>` | `ReaderContext#toHash(Int::class, String::class)`
`inline fun <reified K : Any, reified V : Any> toHash(): Map<K, V>` | Reified implementation of ^ | `ReaderContext#toHash<Int, String>()`
`fun <K : Any, V : Any> toHashRef(kRef: RefType<K>, vRef: RefType<V>): Map<K, V>` | Attempts to create a map of types `<K, V>` from the given `RefType<K>` and `RefType<V>` | `ReaderContext#toHashRef(RefType.of<List<Int>>(), RefType.of<List<String>>())`  // would return `Map<List<Int>, List<String>>`
`inline fun <reified K : Any, reified V : Any> toHashRef(): Map<K, V>` | Reified implementation of ^ | `ReaderContext#toHashRef<List<Int>, List<String>>()` // would return `Map<List<Int>, List<String>>`
