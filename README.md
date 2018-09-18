Muddy is an Android Plugin for simple encryption of string constants in android project.

### Usage

##### Quick Start

1. Add a dependency in `build.gradle` in root project as following:

```groovy
dependencies {
  classpath 'com.panda912.muddy:plugin:1.0.0'
  ...
}
```

2. Apply plugin in application module of `build.gradle`:

```groovy
apply plugin: 'muddy'
muddy {
  key = 2018 // your custom key
}
```

3. Clean and rebuild your project, expand `app_module/build/intermediates/transforms/muddy/` directory and check the generated classes:

```java
public class MainActivity extends AppCompatActivity {
  private static final String TAG = Crypto.decode("ϝϱϹϾϑϳϤϹϦϹϤϩ");
  ...
  ...
}
```

##### Advanced Usage

If you want muddy to work on the whole project except some specified packages or classes, you can use `excludes` like this:

```groovy
apply plugin: 'muddy'
muddy {
  key = 2018 // your custom key
  excludes = ["com.panda912.muddy.data", "com.panda912.muddy.MainActivity"] // packages or classes, must be start with package name
}
```

Otherwise, if you want muddy only work on some specified packages or classes, you can use `includes`.

Muddy default does not work for the third party library, but if you want it work on some specified libraries, you can use `includeLibs`.