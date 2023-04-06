## Code analysis

Project configured with next code analysis tools:
 - [ktlint](https://pinterest.github.io/ktlint/)
 - [detekt](https://detekt.dev/)
 - [lint](https://developer.android.com/studio/write/lint)

You can execute custom gradle task `catalog:verify` to run code analysis for `catalog` module.

## Documentation

Project configured with [dokka](https://github.com/Kotlin/dokka) plugin to generate the 
documentation.
- Use [KDoc](https://kotlinlang.org/docs/kotlin-doc.html) syntax.
- Run `dokkaHtml` to generate documentation in html format.

## Maven

Project configured with [maven](http://simpligility.github.io/android-maven-plugin/) plugin
for library deployment.
