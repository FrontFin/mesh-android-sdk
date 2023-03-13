## Front.B2B.Android

### Code analysis

Project configured with [gnag](https://github.com/btkelly/gnag) plugin
to perform code analysis tools: [ktlint](https://pinterest.github.io/ktlint/),
[detekt](https://detekt.dev/), [lint](https://developer.android.com/studio/write/lint).

- Run `ktlintFormat` to format the code.
- Run `gnagCheck` to preform code analysis according to config in `gnag.gradle`.
- Run `detekt` to analyse with detekt only.

### Maven

Project configured with [maven](http://simpligility.github.io/android-maven-plugin/) plugin
for library deployment. Run `catalog:deploy` to publish library to Sonatype Repository and
Maven Central then.

### Documentation

Project configured with [dokka](https://github.com/Kotlin/dokka) plugin to generate the 
documentation in html format.

- Document the code using [KDoc](https://kotlinlang.org/docs/kotlin-doc.html)
- Run `dokkaHtml` to get documentation in html format. Find it in `build` folder.

