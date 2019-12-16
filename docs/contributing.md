---
id: contributing
title: Contributing
sidebar_label: Contributing
---

These instructions are for contributing to the code. To see how to add Scalapy as a library, see [Getting Started](getting-started.md).

## Development environment

First, follow the Scala Native [environment setup instructions](http://www.scala-native.org/en/v0.3.9-docs/user/setup.html) to install build and runtime dependencies for Scala Native.

You will also need to have Python 3 installed. The project is tested on [Travis CI](https://travis-ci.com/shadaj/scalapy) with Python 3.7.1.

## Building

This project cross-compiles to multiple platforms and Scala versions. It uses [sbt-crossproject](https://github.com/portable-scala/sbt-crossproject) to target both the JVM and Scala Native, 
and [sbt's cross-building](https://www.scala-sbt.org/1.x/docs/Cross-Build.html#Cross+building+a+project) to cross-compile between 2.12 and 2.13 on the JVM.
You can run the following sbt commands:

- `coreJVM/compile` to compile the JVM version on 2.13
- `+ coreJVM/compile` to compile the JVM versions on all supported Scala versions
- `coreNative/compile` to compile the Scala Native version
- `++ 2.12.8` to switch the current build's Scala version to 2.12.8

## Documentation

The documentation website is a [Docusaurus](https://docusaurus.io/) site, for which the code is in the `website` directory.
To install these dependencies of the site, you'll need to have yarn or npm installed, and run:

```console
$ cd website
$ npm install  # or yarn install
```

You can edit the documentation in `docs/*.md`. To build the documentation website, run:

- `sbt docs/mdoc` to type-check the code in the docs, and build to `built-docs`
- `npm start` (or `yarn start`) from the `website` directory to serve a live-reloaded version at [localhost:3000](http://localhost:3000).
- `npm run build` (or `yarn build`) from the `website` directory to build the static assets of the site

Note that you will have to run `sbt docs/mdoc` in order for the docs to be on the site. 
Also note that the documentation is versioned; to see your changes, you'll need to check [localhost:3000/docs/next/](http://localhost:3000/docs/next) 
(instead of the docs in the latest release).

To set up file watching and live-reloading of the site, run `sbt ~docs/mdoc` in one terminal, and `npm start` (from the `website` directory) in another.

To add a page to the sidebar, you can edit `website/sidebars.json`; see the [relevant Docusaurus documentation](https://docusaurus.io/docs/en/navigation) for more information.
