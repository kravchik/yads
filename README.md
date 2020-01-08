YADS
=======

**YADS**

**Y**ads **A**bstract **D**ata **S**yntax

**Y**et **A**nother **D**ata **S**yntax (the perfect one actually).


Mark-up language like JSON and YAML, but better, and with builtin serialization.

### Several self explanatory examples

*Example of using YADS for UI definition*
```Java
HBox(
  pos=(100 200)
  VBox(
    Input(hint='...input here')
    Button(text='Send')
  )
)
```
*Example of using YADS as simple config*
```Java
serverType = node
port = 8080
//port = 80
data = (info = "Awesome super server" author = "John Doe")
services = (AuthService() AdminService())
```
*Example of using YADS as properties*
```Java
greeting = 'Hello traveller!'
signature = 'Have a nice day,
travaller'
```
### Why yet another data syntax?

* no white-space indentation and mandatory new lines like in YAML
* no mandatory `""` like in json and
* can use ""  and `''` interchangeably
* can use new-lines in `""` or `''` strings
* no commas
* not verbose like xml with the same capabilities
* built-in serialization/deserialization
* serialization to beutifuly formatted text
* comments (both one-liners and multi-liners)  

#### no white-space indentation and mandatory new lines like in YAML
Same as in JSON. One can write the whole file in one line. It is very useful when you want to write your data in a string inside your code or in the command line. Also, very convenient if you want to include parts of the config in some input field, or in an Excel table.

In all these cases, white-space indentation would be a pain.

You can wright in both ways:
```Java
    (a b c)
    
    (
        a
        b
        c
    )
```

#### no commas or semicolons
  You don't need to bother about them when adding or removing elements.
  And noise level is very low.

#### no mandatory `""` like in json
  Which is also reduces noise level.
  Though you'd need `""` or `''` if a string should include spaces.
  Also much simpler to define part of the config in the Java String as don't need to constantly escape those quotes.

#### can use `""`  and `''` interchangeably
  First of all, it is slightly simpler to use `'` instead of `"`. Second - in Java you don't need to escape `'` in strings. Third - you can choose `'` when `"` prevails in your text and vice versa (You'd have to escape `"` symbol in a string like `"quote: \" "`).
#### can use new-lines in `""` or `''` strings
  Like in YML
#### not verbose like xml with the same capabilities
  So you can write like:
```Java
HBox(
  pos=(100 200)
  VBox(
    Input(hint='...input here')
    Button(text='Send')
  )
)
```
  Instead of something like:
```XML
<HBox pos="100 200">
  <VBox>
    <Input hint="...input here"/>
    <Button text="Send"/>
  </VBox>
</HBox>
```

#### built-in serialization/deserialization
  Currently - Java only, but syntax provides ways to other languages be included.
  Serialize any data to the human-readable string, and then back to the same data without any additional effort.
  No annotations needed.
  
#### serialization to beutifuly formatted text
  1. convenient to read and edit
  1. can be used for reporting of data (tests)
  1. can be used to generate configs, not only read them

#### comments (both one-liners and multi-liners)  

#### Can be conviniently used for
  1. simple properties file
  1. config (with both read and wright)
  1. serialization in human readable form

#### UTF, symbol escaping


### API
```Java
    String serialized = Yads.serializeBody(someMap);
    Map deserialized = Yads.deserializeBody("hello=world");
    ...
    String serialized = Yads.serialize(yourInstance);
    YourClass y = (YourClass)Yads.deserialize("import=(your.package) YourClass(field1=value1 field2=value2)");
    ...
    etc
```

  *Parsing, serialization, deserialization - currently available in Java only. I am open to collaboration for other languages.*

## mvn artifact
```xml
<repository>
   <id>yk.jcommon</id>
   <url>https://github.com/kravchik/mvn-repo/raw/master</url>
</repository>

<dependency>
    <groupId>yk</groupId>
    <artifactId>yads</artifactId>
    <version>0.01-SNAPSHOT</version>
</dependency>
```
(current dev version is 0.01-SNAPSHOT)

