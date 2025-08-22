YADS
=======

**Y**ads **A**bstract **D**ata **S**yntax

Mark-up language (like JSON/YAML), but with optional quotes, no commas, and multi-line strings.

TODO simple example
                   
TODO updated scheme
   
```
text 
  -> YadsCstParser -> (YadsCst) 
    -> YadsEntityDeseralizer -> (YadsEntity and other types) 
      -> YadsJavaDeserializer -> (pure java types) 

text 
  <- YadsCstPrinter <- (YadsCst, YadsEntity and other types)
    <- YadsJavaSerializer <- (pure java types) 
```


With this library, you can parse YADS data into Abstract Syntax Tree, or you can use Yads class to serialize/deserialize data directly from/to Java classes.

Yads is the next step after JSON and YAML (which were good steps after XML). It simplifies the syntax even further.
* No commas are needed in any lists (like in Lisp)! So you no more bothering when adding or removing list elements.
* No mandatory quotes. And you can use any of `""` or `''` if needed and escape escaping (especially cool when you need to write something like ```String expected = "('some' 'expected' 'strings')";```)
* Newlines in strings (YAML supports it, but JSON is not)
* No mandatory white-spacing  (JSON has no it, YAML has)
* Comments both `//` and `/* */` (JSON has no comments at all, YAML supports only one-line comments)

### Syntax examples

*Some UI definition*
```Java
HBox(
  pos=(100 200)
  VBox(
    Input(hint='...input here')
    Button(text='Send')
  )
)
```
*Some config*
```Java
serverType = node
port = 8080
//port = 80
data = (info = "Awesome super server" author = "John Doe")
services = (AuthService() AdminService())
```
*Some properties*
```Java
greeting = 'Hello traveller!'
signature = 'Have a nice day,
travaller'
```
### Syntax

##### syntax
* `()` - empty list
* `(a b)` - list with two elements
* `(=)` - empty map
* `(k1=v1 k2=v2)` - map with several keys and values
* `1 1f 1.0 1d 1l null true false` - numbers and other types
* `'some string'` - string
* `"some string"` - string
* `some string` - two string elements (`some` and `string`)
* `someString` - string or field name or class name
* `some-string` - three elements: `some`, `minus`, `string`
* `Vec2(1 2)` - instantiation of class `Vec2` via constructor
* `Vec2(x=1 y=2)` - instantiation of class `Vec2` via explicit fields setting

##### feautes
* no white-space indentation or mandatory new lines
* no commas or semicolon
* no mandatory `""` or `'''`
* can use `""`  and `''` interchangeably
* can use new-lines in `""` or `''` strings
* comments `//` and `/* */`  

### API
```Java
    //serialize some instance
    String serialized = Yads.serialize(yourInstance);
    YourClass y = (YourClass)Yads.deserialize("import=your.package.YourClass YourClass(field1=value1 field2=value2)");
    //or with default imports:
    YourClass y = (YourClass)Yads.deserialize(
            al("import=your.package.YourClass"), 
            "YourClass(field1=value1 field2=value2)");

    //serialize body of a map
    String serialized = Yads.serializeBody(someMap);
    Map deserialized = (Map)Yads.deserializeBody("hello=world");

    //serialize body of some class
    String serialized = Yads.serializeBody(yourInstance);
    YourClass y = Yads.deserializeBody(YourClass.class, "field1=value1 field2=value2");
```

### TODO

* security (restrict via API classes possible to load)
* streaming
* all Java types serialization
* custom serialization
* multiline string indentation
* typed array/map

  *Parsing, serialization, deserialization - currently available in Java only. I am open to collaboration for other languages.*

[Serialization](serialization.md)

[Why yet another syntax?](why-another.md)

## mvn artifact
```xml
<repository>
   <id>yk.jcommon</id>
   <url>https://github.com/kravchik/mvn-repo/raw/master</url>
</repository>

<dependency>
    <groupId>yk</groupId>
    <artifactId>yads</artifactId>
    <version>0.03</version>
</dependency>
```
(current dev version is 0.02-SNAPSHOT)

