# Read Me for TomTom SpeedTools Library

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3ce697b78d8742b89613dc02ef9021d5)](https://www.codacy.com/app/rijnb/speedtools?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=tomtom-international/speedtools&amp;utm_campaign=Badge_Grade)
[![Build Status](https://img.shields.io/travis/tomtom-international/speedtools.svg?maxAge=3600&branch=master)](https://travis-ci.org/tomtom-international/speedtools)
[![License](http://img.shields.io/badge/license-APACHE2-blue.svg)]()
[![Release](https://img.shields.io/github/release/tomtom-international/speedtools.svg?maxAge=3600)](https://github.com/tomtom-international/speedtools/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.tomtom.speedtools/speedtools.svg?maxAge=3600)](https://maven-badges.herokuapp.com/maven-central/com.tomtom.speedtools/speedtools)

**Copyright (C) 2012-2017, TomTom NV. All rights reserved.**

SpeedTools is a collection of generic utilities, originally developed by Rijn Buve,
Ruud Diterwich, Daan van Dijk and Andreas Wuest for a large commercial project.

The library was created to develop highly scalable web services, using Typesafe Akka,
Google Guice and MongoDB, but many of the tools are usable in other contexts
as well.

For documentation on what this library offers, have a look at the `speedtools/src/site/markdown`
directory, which is the entry point for the site documentation. It contains a list of all
modules and APIs.

You can view the **general documentation** here:

**http://tomtom-international.github.io/speedtools/**

Or have a look at the **JavaDoc API documentation** at:

**http://tomtom-international.github.io/speedtools/apidocs/index.html**

The versioning scheme of the library starts at 3.0.0, because earlier releases exist
were disclosed within TomTom only. From version 3.0.0 TomTom decided to contribute
this library under the Apache License 2.0 to the open source community.

The **test coverage** of the project is unevenly distributed. Some of the modules have excellent
test coverage in units tests, others lag a bit behind, or cannot be run without running, for example,
a local MongoDB instance, or having access to secret API keys. Such unit tests have been marked
as `@Ignore` in the source tree, which brings the test coverage down. We welcome initiatives to
increase the test coverage further. Most of the original code was suspect to strict code
reviews.

There is an additional project `speedtools-examples` with real-life examples
of how to use the SpeedTools library. Check it out at
`https://github.com/tomtom-international/speedtools-examples`.

Happy coding!

**Rijn Buve**

*TomTom NV*

PS. For questions, issues and other remarks, you can contact me via email, or
send a tweet to **@rijnb**.

# License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# Using Git and `.gitignore`

It's good practice to set up a personal global `.gitignore` file on your machine which filters a number of files
on your file systems that you do not wish to submit to the Git repository. You can set up your own global
`~/.gitignore` file by executing:
`git config --global core.excludesfile ~/.gitignore`

In general, add the following file types to `~/.gitignore` (each entry should be on a separate line):
`*.com *.class *.dll *.exe *.o *.so *.log *.sql *.sqlite *.tlog *.epoch *.swp *.hprof *.hprof.index *.releaseBackup *~`

If you're using a Mac, filter:
`.DS_Store* Thumbs.db`

If you're using IntelliJ IDEA, filter:
`*.iml *.iws .idea/`

If you're using Eclips, filter:
`.classpath .project .settings .cache`

If you're using NetBeans, filter:
`nb-configuration.xml *.orig`

The local `.gitignore` file in the Git repository itself to reflect those file only that are produced by executing
regular compile, build or release commands, such as:
`target/ out/`

# Bug Reports and New Feature Requests

If you encounter any problems with this library, don't hesitate to use the `Issues` session to file your issues.
Normally, one of our developers should be able to comment on them and fix.
