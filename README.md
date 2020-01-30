YADS
=======

**Y**ads **A**bstract **D**ata **S**yntax

**Y**et **A**nother **D**ata **S**yntax (the perfect one actually).

Mark-up language like JSON and YAML, but better, and with builtin serialization.

### Several self-explanatory examples

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
### Syntax
Here provided some base standings which unlikely to change. Development is in progress so more specifics will be added later.

##### feautes
* no white-space indentation or mandatory new lines
* no commas or semicolon
* no mandatory `""` or `'''`
* can use `""`  and `''` interchangeably
* can use new-lines in `""` or `''` strings
* comments `//` and `/* */`  

##### syntax
* `()` - empty list
* `(a b)` - list with two elements
* `(=)` - empty map
* `(k1=v1 k2=v2)` - map with several keys and values
* `1 1f 1.0 1d 1l null true false` - numbers and other types
* `'some string'` - string
* `"some string"` - string
* `some string` - two string elements (`some` and `string`)
* `someString` - string or field name
* `some-string` - three elements: `some`, `minus`, `string`
* `Vec2(1 2)` - instantiation of class `Vec2` via constructor
* `Vec2(x=1 y=2)` - instantiation of class `Vec2` via explicit fields setting

##### top-level structure
Depends on the serialization/deserialization method called. Can be either with a mandatory top-level element (in this example - `HBox`):
```Java
HBox(
  pos=(100 200)
  VBox(
    Input(hint='...input here')
    Button(text='Send')
  )
)
```
Or can contain a body. In which case serialization method should be aware of a class (in this example - some config class):
```Java
serverType = node
port = 8080
//port = 80
data = (info = "Awesome super server" author = "John Doe")
services = (AuthService() AdminService())
```

##### importing
Serializer should know a type of class in order to use `VBox(...)` instead of `some.kind.of.package.VBox(...)`. While both ways are available, it is more convenient to place the imports section at the beginning:
```
    import some.kind.of.package.VBox
    
    VBox(...)
    VBox(...)
```
Or you can specify all needed imports in the serializer method so you don't need any mention of it in the text.

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
* get rid of commons.lang3


  *Parsing, serialization, deserialization - currently available in Java only. I am open to collaboration for other languages.*

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
    <version>0.01-SNAPSHOT</version>
</dependency>
```
(current dev version is 0.01-SNAPSHOT)

