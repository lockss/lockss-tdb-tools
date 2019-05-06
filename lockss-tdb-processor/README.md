# `lockss-tdb-tools`

This project is a suite of Java tools to process LOCKSS TDB files.

The suite consists of:

*   `TdbParse`: parses TDB files into an abstract representation
*   `TdbOut`: filters and outputs list-oriented or column-oriented information from TDB data
*   `TdbXml`: outputs TDB data in an XML format consumed by the LOCKSS software

## Building

You must have Git, Java 8 and Maven installed.

Follow these instructions to build the project:

1.  `git clone https://github.com/lockss/lockss-tdb-tools`
1.  `cd lockss-tdb-tools/lockss-tdb-tools`
    *   This directory (whose path can be printed by typing `pwd`) is the root of the project.
1.  `mvn package`

## Using

Each of `TdbParse`, `TdbOut` and `TdbXml` comes with a runner script, respectively `bin/tdbparse`, `bin/tdbout` and `bin/tdbxml` (relative to the root of the project).

Each accepts a `--help` argument, that will display a usage summary.

*More extensive documentation is under construction.*

## Releases

*   [Tagged releases of `lockss-tdb-tools` on GitHub](https://github.com/lockss/lockss-tdb-tools/releases) (tags with `lockss-tdb-tools` in the label)
*   [`lockss-tdb-tools` artifacts on Maven Central](https://search.maven.org/search?q=g:org.lockss%20AND%20a:lockss-tdb-tools&core=gav)

## See Also

*   [`lockss-tdbxml-maven-plugin`](https://github.com/lockss/lockss-tdb-tools/lockss-tdbxml-maven-plugin): Maven plugin to invoke the `tdbxml` tool from `lockss-tdb-tools`

## Resources

*   [LOCKSS Program website](https://www.lockss.org/)
*   [LOCKSS Program on GitHub](https://github.com/lockss)
*   [LOCKSS Software Pages](https://lockss.github.io/software)
*   [LOCKSS Developer Pages](https://lockss.github.io/developers)
