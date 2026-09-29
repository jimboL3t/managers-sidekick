# Licensing and website distribution

[Ελληνικά](LICENSING.md)

Manager’s Sidekick: Copyright (C) 2026 Dimitrios Diamantis.
License: **GNU GPL version 3 only**, SPDX `GPL-3.0-only`.
The root LICENSE contains the official unchanged text; COPYING.md
identifies the application copyright holder and licensing scope.

## Why GPLv3

GPLv3 fits a project whose recipients may run, study, modify and redistribute
it, while distributed covered derivatives must preserve the GPL freedoms.
Commercial use and selling copies are permitted. It is not a noncommercial
license, an exclusive-distribution right or protection against competing forks.
Private modifications do not automatically have to be published to the world.
Redistributing covered binaries brings corresponding-source obligations.
See the [GNU GPLv3 text](https://www.gnu.org/licenses/gpl-3.0.html).

Version 3 **only** was chosen, rather than automatically granting permission
under future GNU license versions. An MIT/Apache-style permissive license would
be a different choice if proprietary derivative distribution is desired.
A website offering downloads does not itself require AGPL; if the application
later becomes a network service, reconsider licensing for that separate model.
See the [GNU comparison](https://www.gnu.org/licenses/quick-guide-gplv3.html).

## Publishing a release

Provide license/copyright notices with downloads, and access to the exact
corresponding source and build/install scripts for the binary release. For a
website, a clear adjacent source-download link is a straightforward approach;
GitHub is optional. A link to an unrelated latest branch is not a reliable
substitute for the exact released source. Keep the matching version available.

`python3 scripts/package_release.py` includes LICENSE/COPYING.md in the binary
ZIP and creates a separate `-source.zip` containing the application source,
resources, tests, documentation and build/packaging scripts. This archive excludes
personal data, caches, Git and IDE settings. It is an application-source archive,
not a claim that every third-party corresponding-source requirement is fulfilled.
Dependency versions are pinned in pom.xml and the packaging script. Before public
binary distribution, also arrange the required source availability for the actual
included dependencies and any bundled Java runtime, retaining their notices.
A Maven download link or a copyright line alone is not a blanket substitute for
all distribution obligations. See GPLv3 section 6 for permitted arrangements.

The project JAR embeds its own license under META-INF/sidekick/ to avoid replacing
third-party META-INF notices. Portable distributions retain individual runtime
library notices in licenses/. Native builds inherit these application notices
and the runtime's legal files; existing old ZIP/DMG files are not retroactively
rewritten by this commit. Rebuild and review artifacts before publishing them.

## Dependencies and other material

The current packaged runtime libraries (Gson, error_prone_annotations, PDFBox,
pdfbox-io, FontBox and Commons Logging) are listed with their original notices
by the packager. Apache-2.0 is compatible with GPLv3 for this combination; it does
not turn the original library into exclusively GPL-licensed code. See the
[Apache Software Foundation's explanation](https://apache.org/licenses/GPL-compatibility.html).
JUnit is a test dependency and is not shipped in the application JAR. A bundled
OpenJDK has its own licensing, exceptions, notices and source obligations; check
the exact distribution/version used for native packages. System fonts are not
included by the current packager. Clear rights for any future distributor logo,
font or other contributed assets before including them.

The application license does not make employee data or ordinary exported
schedule PDFs GPL-licensed. Only license material you have authority to license;
future contributions and third-party assets need their own rights checked.
This guide explains the project's distribution choices, not an individualized
legal opinion.
