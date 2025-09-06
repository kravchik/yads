# TODO update

#### Internal API scheme for deserialization to anything

* _text_ -> **YadsCstParser** -> _YadsCst_
* _YadsCst_ -> **YadsEntityDeseralizer** -> _primitives, YadsEntity_
* _primitives, YadsEntity_ -> **YadsJavaDeserializer** -> _Java types_

#### Internal API scheme for serialization of anything

* _text_ <- **YadsCstPrinter** <- _primitives, YadsCst, YadsEntity_
* _primitives, YadsCst, YadsEntity_ <- **YadsJavaSerializer** <- _any type_


##### top-level structure
If you are using ```YadsParser```, then you are getting just an AST which will just represent data as it is. But if you are using ```Yads``` class, then you can serialize/deserialize directly from/to Java classes and you need to choose between two styles of YADS data.

Yads.serialize/Yads.deserialize will work with one top-level element, so a file can look like this:
```Java
HBox(
  pos=(100 200)
  VBox(
    Input(hint='...input here')
    Button(text='Send')
  )
)
```
Yads.serializeBody/Yads.deserializeBody will work with a bunh of key-values, or list elements, so a file can look like this:
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



