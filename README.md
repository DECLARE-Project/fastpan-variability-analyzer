# fastpan-variability-analyzer

[![GitHub tag](https://img.shields.io/github/tag/DECLARE-Project/fastpan-variability-analyzer.svg?maxAge=30)](https://github.com/DECLARE-Project/fastpan-variability-analyzer/releases)
[![Travis](https://img.shields.io/travis/DECLARE-Project/fastpan-variability-analyzer.svg?maxAge=30)](https://travis-ci.org/DECLARE-Project/fastpan-variability-analyzer)
[![Coverage](https://img.shields.io/codecov/c/github/DECLARE-Project/fastpan-variability-analyzer.svg?maxAge=30)](https://codecov.io/gh/DECLARE-Project/fastpan-variability-analyzer)
[![license](https://img.shields.io/github/license/DECLARE-Project/fastpan-variability-analyzer.svg?maxAge=30)](LICENSE)

Performance analysis for software product lines.

> **Attention:** This project is a work in progress and as such, the API is unstable and may change anytime. For recent changes refer to the change log.


## Installation

- Check out this project from source.
- Hop on a shell and run `mvn clean install`. You may also do this from your favorite IDE.

#### Export as Library:

You may export this project as standalone JAR library including all required dependencies by running `mvn clean package`. The JAR file is then created in `/target/*.jar`.


## Usage

TBD


## Release

To release a new version, run the following commands:

```sh
mvn release:prepare
mvn release:perform -Darguments="-Dmaven.javadoc.skip=true"
```


## Contributing

Open a PR :-)


## [Change Log](CHANGELOG.md)

See all changes made to this project in the [change log](CHANGELOG.md). This project follows [semantic versioning](http://semver.org/).


## [License](LICENSE)

This project is licensed under the terms of the [MIT license](LICENSE).


---

Project created and maintained by [Fabian Keller](http://www.fabian-keller.de) in the scope of his master's thesis.