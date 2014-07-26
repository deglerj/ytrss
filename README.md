ytrss
=====
ytrss is a small, server-based application that automatically downloads videos from selected Youtube channels, converts them to MP3 files and offers them as RSS or Atom feeds that you can add to your podcast player.
You can access ytrss through a simple web-interface, that allows you to manage channel subscriptions, get feed URLs and download single files.


Warning
----
ytrss is still in an early development stage (aka "it's working for me"). Please report any bugs you find.


Getting started
----
1. Make sure you have Java 8 or higher installed
2. Make sure "ffmpeg" is available on the command line
  - If you're using Linux simply install ffmpeg through your package manager (e.g. apt-get install ffmpeg)
  - If you're using Windows download the latest version here: http://ffmpeg.zeranoe.com/builds/, extract it anywhere and add the "bin" directory to Windows' PATH variable
3. Download ytrss-&lt;version&gt;.jar from here: https://github.com/deglerj/ytrss/releases
4. Run from command line using "java -jar ytrss-&lt;version&gt;.jar"
5. Wait a few seconds for the console output to say "Running"
6. Open the web-interface at http://localhost:8080
7. Log in using the password "ytrss" and any user name. You can change the password later in the settings.
8. Add a Youtube channel
9. Wait a few minutes for the first videos to finish downloading and transcoding. Files are stored in the current user's home in ".ytrss".
10. Open the channel page and subscribe to the RSS (recommended) or Atom feed



3rd party libraries
----
- Spring (http://spring.io/)
- Jetty (http://eclipse.org/jetty/)
- Guava  (http://code.google.com/p/guava-libraries/)
- HSQLDB (http://hsqldb.org/)
- Flyway (http://flywaydb.org/)
- Hibernate Validator (http://hibernate.org/validator/)
- ROME (http://rometools.github.io/rome/)
- Gradle (http://gradle.org/)
- slf4j (http://slf4j.org/)
- mp3agic (http://github.com/mpatric/mp3agic)
- jQuery (http://jquery.com/)
- Bootstrap (http://getbootstrap.com/)
- JDT (http://eclipse.org/jdt/)
- Argo (http://argo.sourceforge.net)
