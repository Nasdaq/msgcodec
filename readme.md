# MsgCodec Github project currently on hold !

The MsgCodec project is currently on hold here on github, due to a lack of resources for managing the open source project at Cinnober. 
Bug reports and feature requests will not be processed anytime soon, nor will there be any new releases.

<br>
<br>
<br>
<br>


# msgcodec #
[![Build Status](https://travis-ci.org/cinnober/msgcodec.svg?branch=master)](https://travis-ci.org/cinnober/msgcodec)

A library to encode and decode messages in various formats where messages are defined as plain old java objects.
Java 7 is required for version 2.x and Java 8 is required for version 3.x (latest).

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


We can generate a [Blink](http://blinkprotocol.org) message like this:

    Schema schema = new SchemaBuilder().build(Hello.class);
    MsgCodec codec = new BlinkCodecFactory(schema).createCodec();

    Hello msg = new Hello("hello world");
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    codec.encode(msg, bout);

...and decode it back to an object like this:

    Hello decoded = (Hello) codec.decode(new ByteArrayInputStream(bout.toByteArray()));
    System.out.println(decoded.greeting.equals(msg.greeting) + " = true");

Note that the same schema can be used with another encoder to produce for example JSON messages. See javadoc and examples for more information.

### Artifacts  ###

To use msgcodec you will need to include the artifacts in the group=`com.cinnober.msgcodec`; `msgcodec` and `msgcodec-{format}` in your application's runtime. Format is one of `xml`, `json` or `blink`.

The versions of the artifacts follows [semantic versioning](http://semver.org), which means that a minor version upgrade is always backwards compatible.

In a gradle project one would include the libs in dependencies section of your `build.gradle` file (replace `{version}` with the appropriate current release). Example for a project that uses the Blink encoding format:

    dependencies {
        compile group: 'com.cinnober.msgcodec', name: 'msgcodec', version: '{version}'
        compile group: 'com.cinnober.msgcodec', name: 'msgcodec-blink', version: '{version}'
    }

## Project Structure ##

The code is divided into the following projects:

- `msgcodec`: contains annotations etc required for defining messages and protocols
- `msgcodec-json`: JSON codec
- `msgcodec-json-jaxrs`: JSON codec JAX-RS support
- `msgcodec-json-swagger`: JSON codec Swagger support
- `msgcodec-xml`: XML codec
- `msgcodec-blink`: Blink (compact format) codec
- `msgcodec-javadoc`: Javadoc doclet for extracting javadoc comments from messages.
- `msgcodec-examples`: Examples of how to use msgcodec.


## Build ##

	# First clone a copy of the repo:
	git clone git@github.com:cinnober/msgcodec.git
	cd msgcodec
	
	# Compile, build and test:
	gradle build

### Eclipse IDE ###

If you want to edit java files from within eclipse, run `gradle eclipse`

and then from within eclipse File -> Import... -> General -> Existing projects into workspace -> Next.. Browse.. browse to where you cloned repo -> Ok.. -> Select All -> Import.

### Netbeans IDE ###

Install the [Netbeans Gradle Plugin](http://plugins.netbeans.org/plugin/44510/gradle-support) and just open the project (the cloned repo).

### Documentation ###
See the javadoc of `msgcodec`. To build the javadoc run: `gradle javadoc`

The result ends up in msgcodec/build/docs/javadoc/.

### Examples ###

See msgcodec-examples for example usage of msgcodec.

### Release ###

Versions are stored as annotated tags in git. [Semantic versioning](http://semver.org) is used.

To create a new release, e.g. 1.2.3:

    git tag -a 1.2.3 -m "New release"
    git push --tags

If changes are made after version 1.2.3 then the version number be '1.3.0-SNAPSHOT' (default a minor change).

To upload the archives to the Maven Central (through the OSSRH), run:

    gradle clean build uploadArchives

Note that credentials are required for uploads. They should be placed in e.g. your
~/.gradle/gradle.properties for `uploadArchives`.
See [gradle.properties](gradle.properties) for more information.
