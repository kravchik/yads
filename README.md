YADS
=======

**Y**ads **A**bstract **D**ata **S**yntax

Mark-up language (like JSON, YAML), but much more convenient.

* no commas
* no white-space indentation or mandatory new lines
* quotes are optional everywhere
* can use `""`  and `''` interchangeably
* can use new-lines in `""` or `''` strings
* comments `//` and `/* */` and are accessible in data

### Data Syntax examples

```Java
// Some hierarchical UI definition
HBox(
  pos = (100 200)
  VBox(
    Input(hint = '...input here')
    Button(text = Send)
  )
)
```

```Java
// Some config
serverType = node
port = 8080
//port = 80
data = (info = "Awesome super server" author = "John Doe")
services = (AuthService() AdminService())
```

```Java
// Some properties
greeting = 'Hello traveller!'

signature = '
Have a nice day,
travaller!
'
```

### Java implementation specifics

* reading text to data
* printing plus formatting
* comments are also read/printed (except for Java serialization)
* Java serializing/deserializing (focus on readability) 

```Java
    //serialize some abstract data
    String serialized = Yads.printYadsEntity(yourInstance);
    Object y = Yads.readYadsEntity("YourClass(field1=value1 field2=value2)");
    
    //serialize some abstract data without top-level class
    String serialized = Yads.printYadsEntities(yourInstance);
    YList<Object> y = Yads.readYadsEntities("'someString' field1=value1 field2=value2");
    
    //serialize some Java instance
    String serialized = Yads.printJava(yourInstance);
    YourClass y = (YourClass)Yads.readJava(YourClass.class, "YourClass(field1=value1 field2=value2)");

    //serialize body of some Java class
    String serialized = Yads.printJavaBody(yourInstance);
    YourClass y = Yads.readJavaBody(YourClass.class, "field1=value1 field2=value2");
```

### Syntax overview

* `()` - empty list
* `(a b)` - list with two elements
* `(=)` - empty map
* `(a b k1=v1 k2=v2)` - keys/values and simple values can be together
* `1 1f 1.0 1d 1l null true false` - numbers and other types
* `someString` - unquoted string, no new lines, limited character set
* `'some string' or "some string"` - quoted strings of any characters, new lines permitted
* `'hello \r\n world \b'` - several escape types
* `one-two,three` - list of five elements, operators and commas are parsed as separate elements
* `Vec2(x=1 y=2)` - 'class' with data

## mvn artifact
```xml
<!--no artifact for the latest version currently -->
<!--<repository>-->
<!--   <id>yk.jcommon</id>-->
<!--   <url>https://github.com/kravchik/mvn-repo/raw/master</url>-->
<!--</repository>-->

<dependency>
    <groupId>yk</groupId>
    <artifactId>yads</artifactId>
<!-- NO ARTIFACT ! SHOULD CHECK-OUT AND BUILD LOCALLY   -->
    <version>0.3-SNAPSHOT</version>
</dependency>
```
(current dev version is 0.3-SNAPSHOT)

