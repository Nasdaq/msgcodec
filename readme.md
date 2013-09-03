# msgcodec #
A library to encode and decode messages in various formats where messages are defined as plain old java objects. This library is maintained by [Mikael Brännström ](mailto://mikael.brannstrom@cinnober.com). Feedback is very welcome. 

## Usage ##
Given a POJO:

    class Hello {
        private String greeting;
        public Hello() {}
        public Hello(String greeting) {
            this.greeting = greeting;
        }
        @Required
        public String getGreeting() {
            return greeting;
        }
        public void setGreeting(String greeting) {
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
    System.out.println(decoded.getGreeting().equals(msg.getGreeting()) + " = true");

Note that the same dictionary can be used with another encoder to produce for example TAP messages. See javadoc and tests for more examples.


## Download ##
You will need to include the `msgcodec-{version}.jar` and `msgcodec-{format}-{version}.jar` in your application's runtime. If your application uses several encoding formats add several `msgcodec-{format}-{version}.jars`.

In a gradle project one would include the libs in dependencies section of your `build.gradle` file (replace `{version}` with the appropriate current release). Example for a project that uses the TAP encoding format:

    dependencies {
      compile group: 'com.cinnober', name: 'msgcodec', version:'{version}'
      compile group: 'com.cinnober', name: 'msgcodec-tap', version:'{version}'
    }

## Building msgcodec ##
First clone a copy of the repo:
>git clone git@git.cinnober.com:products/msgcodec.git

To build msg-codec you need cbuilder. Cd into msgcodec/Build and run
>gradle getDependentModules
then cd up one level and run
>gradle build


If you want to edit java files from within eclipse, run
>gradle eclipse

and then from within eclipse File -> Import... -> General -> Existing projects into workspace -> Next.. Browse.. browse to where you cloned repo -> Ok.. -> Select All -> Import.

## Documentation ##
See the javadoc of `msgcodec`. To build the javadoc run:
>gradle javadoc

The result ends up in msgcodec/build/docs/javadoc/.

## Undocumented ##
How are tests run?
Continuous integration?
Stable release?
