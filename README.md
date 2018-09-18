Muddy is an Android Plugin for simple encryption of string constants in android project.

### Usage

Add a dependency in `build.gradle` in root project as following:

```groovy
dependencies {
  classpath 'com.panda912.muddy:plugin:1.0.0'
  ...
}
```

Apply plugin in application module of `build.gradle`:

```groovy
apply plugin: 'muddy'
muddy {
  key = 2018 // your custom key
}
```

