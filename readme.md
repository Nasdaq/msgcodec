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

Note that the same dictionary can be used with another encoder to produce for example TAP messages. See javadoc and tests for more examples.


## Download ##
You will need to include the `msgcodec-{version}.jar` and `msgcodec-{format}-{version}.jar` in your application's runtime. If your application uses several encoding formats add several `msgcodec-{format}-{version}.jars`.

In a gradle project one would include the libs in dependencies section of your `build.gradle` file (replace `{version}` with the appropriate current release). Example for a project that uses the TAP encoding format:

    dependencies {
      compile group: 'com.cinnober', name: 'msgcodec', version:'{version}'
      compile group: 'com.cinnober', name: 'msgcodec-tap', version:'{version}'
    }

## Building msgcodec ##
PENDING: maybe we can remove the "Development" prefix below?

First clone a copy of the repo:
>git clone git@gitrepo.cinnober.com:platform/msgcodec.git Developmentmsgcodec

Bootstrap dependencies (cBuilder) and build:
>cd Developmentmsgcodec
>cd Build && gradle getDependentModules && cd -
>gradle build

If you want to edit java files from within eclipse, run
>gradle eclipse

and then from within eclipse File -> Import... -> General -> Existing projects into workspace -> Next.. Browse.. browse to where you cloned repo -> Ok.. -> Select All -> Import.

## Documentation ##
See the javadoc of `msgcodec`. To build the javadoc run:
>gradle javadoc

The result ends up in msgcodec/build/docs/javadoc/.

## Examples ##

See msgcodec-examples for example usage of msgcodec.

## Structure ##

The code is divided into the following projects:
- msgcodec: contains annotations etc required for defining messages and protocols
- msgcodec-json: JSON codec
- msgcodec-xml: XML codec
- msgcodec-blink: Blink (compact format) codec
- msgcodec-tap: TAP codec
- msgcodec-javadoc: Javadoc doclet for extracting javadoc comments from messages.
- msgcodec-examples: Examples of how to use msgcodec.

## Versions ##

Versions are stored as annotated tags in git. Semantic versioning (http://semver.org) are used, using the major.minor.patch syntax.

To create a new version, e.g. 1.2.3:

    git tag -a 1.2.3 -m "New release"
    git push --tags
    gradle clean build uploadArchives

If changes are made after version 1.2.3 then the version number will look like '1.2.3+8-g6542423', until a new annotated tag is created.

## Undocumented ##
How are tests run?
Continuous integration?
Stable release?
