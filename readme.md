# msgcodec #
A library to encode and decode messages in various formats where messages are defined as plain old java objects. This library is maintained by [Mikael Brännström ](mailto://mikael.brannstrom@cinnober.com). Feedback is very welcome. 

## Usage ##
Given a POJO:

    public class Hello extends MsgObject {
        /** The greeting. */
        @Required
        public String greeting;
        public Hello() {}
        public Hello(String greeting) {
            this.greeting = greeting;
        }
    }


We can generate a Blink message like this:

    ProtocolDictionary dictionary = new ProtocolDictionaryBuilder().build(Hello.class);
    StreamCodec codec = new BlinkCodec(dictionary);

    Hello msg = new Hello("hello world");
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    codec.encode(msg, bout);

...and decode it back to an object like this:

    Hello decoded = (Hello) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
    System.out.println(decoded.greeting.equals(msg.greeting) + " = true");

Note that the same dictionary can be used with another encoder to produce for example TAP messages. See javadoc and examples for more information.

### Artifacts  ###

To use msgcodec you will need to include the artifacts in the group=`com.cinnober.msgcodec`; `msgcodec` and `msgcodec-{format}` in your application's runtime. Format is one of `xml`, `json`, `blink` or `tap`.

The versions of the artifacts follows [semantic versioning](http://semver.org), which means that a minor version upgrade is always backwards compatible.

In a gradle project one would include the libs in dependencies section of your `build.gradle` file (replace `{version}` with the appropriate current release). Example for a project that uses the TAP encoding format:

    dependencies {
        compile group: 'com.cinnober.msgcodec', name: 'msgcodec', version: '{version}'
        compile group: 'com.cinnober.msgcodec', name: 'msgcodec-tap', version: '{version}'
    }

## Project Structure ##

The code is divided into the following projects:

- msgcodec: contains annotations etc required for defining messages and protocols
- msgcodec-json: JSON codec
- msgcodec-xml: XML codec
- msgcodec-blink: Blink (compact format) codec
- msgcodec-tap: TAP codec
- msgcodec-javadoc: Javadoc doclet for extracting javadoc comments from messages.
- msgcodec-examples: Examples of how to use msgcodec.

## Build ##

First clone a copy of the repo:
>git clone git@gitrepo.cinnober.com:platform/msgcodec.git

Then build:
>cd msgcodec
>gradle build

### Eclipse ###

If you want to edit java files from within eclipse, run
>gradle eclipse

and then from within eclipse File -> Import... -> General -> Existing projects into workspace -> Next.. Browse.. browse to where you cloned repo -> Ok.. -> Select All -> Import.

### Netbeans ###

Install the [Netbeans Gradle Plugin](http://plugins.netbeans.org/plugin/44510/gradle-support) and just open the project (the cloned repo).

### Documentation ###
See the javadoc of `msgcodec`. To build the javadoc run:
>gradle javadoc

The result ends up in msgcodec/build/docs/javadoc/.

### Examples ###

See msgcodec-examples for example usage of msgcodec.

### Release ###

Versions are stored as annotated tags in git. [Semantic versioning](http://semver.org) is used.

To create a new release, e.g. 1.2.3:

    git tag -a 1.2.3 -m "New release"
    git push --tags

Then jenkins will to the following:

    gradle clean build uploadArchives

If changes are made after version 1.2.3 then the version number be '1.3.0-SNAPSHOT' (default a minor change).
