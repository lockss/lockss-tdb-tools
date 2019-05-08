# `lockss-tdbxml-maven-plugin`

This project defines a Maven plugin to invoke the `tdbxml` tool from `lockss-tdb-tools`.

## Using

          <plugin>
            <groupId>org.lockss.laaws</groupId>
            <artifactId>lockss-tdbxml-maven-plugin</artifactId>
            <version>${version.of.lockss-tdbxml-maven-plugin}</version>
            <executions>
              <execution>
                <id>processTdb</id>
                <phase>test-compile</phase>
                <goals>
                  <goal>tdbxml</goal>
                </goals>
                <configuration>
                  <skip>false</skip>
                  <srcDir>/some/source/path</srcDir>
                  <dstDir>/some/destination/path</dstDir>
                  <recurse>true</recurse>
                </configuration>
              </execution>
            </executions>
          </plugin>

*More extensive documentation is under construction.*

## Releases

*   [Tagged releases of `lockss-tdbxml-maven-plugin` on GitHub](https://github.com/lockss/lockss-tdb-tools/releases) (tags with `lockss-tdbxml-maven-plugin` in the label)
*   [`lockss-tdb-tools` artifacts on Maven Central](https://search.maven.org/search?q=g:org.lockss.laaws%20AND%20a:lockss-tdbxml-maven-plugin&core=gav)

## See Also

*   [`lockss-tdb-tools`](https://github.com/lockss/lockss-tdb-tools/lockss-tdb-tools): suite of Java tools to process LOCKSS TDB files

## Resources

*   [LOCKSS Program website](https://www.lockss.org/)
*   [LOCKSS Program on GitHub](https://github.com/lockss)
*   [LOCKSS Software Pages](https://lockss.github.io/software)
*   [LOCKSS Developer Pages](https://lockss.github.io/developers)
