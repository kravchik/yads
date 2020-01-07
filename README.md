YADS - Yads Abstract Data Syntax
=======

**Parsing, serialization, deserialization - currently available in Java only. I am open to collaboration for other languages.**

### little description


### several self explanatory examples

*Example of using YADS for UI definition*
```
HBox (
  pos=(100 200)
  VBox(
    Input(hint='...input here')
    Button(text='Send')
  )
)
```
*Example of using YADS as simple config*
```
serverType = node
port = 8080
//port = 80
data = (info = "Awesome super server" author = "John Doe")
services = (AuthService() AdminService())
```
*Example of using YADS as properties*
```
greeting = 'Hello traveller!'
signature = 'Have a nice day,
travaller'
```


### Why yet another data syntax?

* no white-space indentation and mandatory new lines like in YAML
* no mandatory "" like in json and yml
* can use ""  and '' interchangeably
* can use new-lines in "" or '' strings
* no commas
* not verbose like xml with the same capabilities
* built-in serialization/deserialization
* serialization to beutifuly formatted text
* comments (both one-liners and multi-liners)  

#### no white-space indentation and mandatory new lines like in YAML
Same as in JSON. One can write the whole file in one line. It is very useful when you want to write your data in a string inside your code or in the command line. Also, very convenient if you want to include parts of the config in some input field, or in an Excel table.

In all these cases, white-space indentation would be a pain.

#### no mandatory "" like in json
  though you'd need "" or '' if a string should include spaces (in constrast with YAML)
  example with simple keys and values
  example with escapes in Java strings for jsons and ymls
#### can use ""  and '' interchangeably
#### can use new-lines in "" or '' strings
  like in YML
  example with Java strings
#### no commas (remember those additions of an element in json?)
#### not verbose like xml with the same capabilities
#### built-in serialization/deserialization
  currently - Java only, but syntax provides ways to other languages be included
#### serialization to beutifuly formatted text
  1. convenient to read and edit
  1. can be used for reporting of data (tests)
#### comments (both one-liners and multi-liners)  



todo code examples


config (s12/des12), no need to map, no need to parse, no need of annotations
  because of built-in ser/deser - config not only readable, but also generatable! (example)
simple properties file
can write in one line (profit when script goes in one-liner, excel, other?)
example: can use for UI layouts instead of XML
imports can be defined as default in ser/deser, instead of a text itself


**yk.lang.yads**

Yet Another Data Syntax (the perfect one actually).
Yads Abstract Data Syntax

How would UI markup look like:

```
import=ui
HBox (
  pos=100, 200
  VBox (
    Input (text='input here')
    Button (text='hello world')
  )
)
```
How would some game config look lie:
```
Npc (
  name='Grumble Fingur'
  type=Goblin
  model=(type=AngryBastard colorScheme=red)
  items= items.Hat(name='Hat of sun'), random(type=ring)
  )
)
```
Or just simple config file:
```
serverType = node
port = 8080
//port = 80
description = "Awesome
super server"
```

### API
    String serialized = Yads.serializeBody(someMap);
    Map deserialized = Yads.deserializeBody("hello=world");
    ...
    String serialized = Yads.serialize(yourInstance);
    YourClass y = (YourClass)Yads.deserialize("import=(your.package) YourClass(field1=value1 field2=value2)");
    ...
    etc


### syntax
* no commas or semicolons needed, so the noise level is very low
* strings and keys without quotes (that also reduces noise)
* but can use "" or '' (for strings with spaces, for example)
* ' ' for strings - so you can include YADS in java strings without escaping
* spaces and tabs don't have special meaning (opposite to yaml or python), so you can arrange data-text as you wish, even in one line (important for various input types: xls cells, input fields, etc)
* multiline strings (with both "" and '' quotes)
* numbers, booleans
* utf8, no restriction on keys or strings
* comments (one line // and multiline /**/)
* carefully controlled comma, to avoid one level parentheses like in (pos=10, 10 size=100, 200)

### serialization
* serialize any data to the human-readable string, and then back to the same data without any additional effort
* maps, lists, arrays, objects with class preservation
* if a type is unknown - array, map, or special class is constructed

[more on wiki](https://github.com/kravchik/jcommon/wiki/YADS-instead-of-.properties-syntax-example)

## mvn artifact
```xml
<repository>
   <id>yk.jcommon</id>
   <url>https://github.com/kravchik/mvn-repo/raw/master</url>
</repository>

<dependency>
    <groupId>yk</groupId>
    <artifactId>yincubator</artifactId>
    <version>0.02</version>
</dependency>
```
(current dev version is 0.120-SNAPSHOT)

