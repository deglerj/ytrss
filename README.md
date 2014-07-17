ytrss
=====
ytrss is a small, server-based application that automatically downloads videos from selected Youtube channels, converts them to MP3 files and offers them as RSS or Atom feeds that you can add to your podcast player.
You can access ytrss through a simple web-interface, that allows you to manage channel subscriptions, get feed URLs and download single files.


Early alpha
----
ytrss is still in an early development stage. Expect bugs and some missing features.


Getting started
----
1. Make sure you have Java 8 or higher installed
2. Download ytrss-<version>.jar
3. Run from command line using "java -jar ytrss-<version>.jar"
4. Wait a few seconds for the console output to say "Running"
5. Open the web-interface at http://localhost:8080
6. Log in using the password "ytrss" and any user name. (You can change the password under "Settings")
7. Add a Youtube channel
8. Wait a few minutes for the first videos to finish downloading and transcoding
9. Open the channel page and subscribe to the RSS or Atom feed



3rd party libraries
----
- Spring (http://spring.io/)
- Jetty (http://eclipse.org/jetty/)
- Guava  (http://code.google.com/p/guava-libraries/)
- HSQLDB (http://hsqldb.org/)
- Tarsos Transcoder (http://0110.be/tags/TarsosTranscoder)
- Flyway (http://flywaydb.org/)
- Hibernate Validator (http://hibernate.org/validator/)
- ROME (http://rometools.github.io/rome/)
- Gradle (http://gradle.org/)
- slf4j (http://slf4j.org/)
- mp3agic (http://github.com/mpatric/mp3agic)
- jQuery (http://jquery.com/)
- Bootstrap (http://getbootstrap.com/)
- JDT (http://eclipse.org/jdt/)
