ytrss
=====
ytrss is a small, server-based application that automatically downloads videos from selected Youtube channels, converts them to MP3 files and offers them as RSS or Atom feeds that you can add to your podcast player.
You can access ytrss through a simple web-interface, that allows you to manage channel subscriptions, get feed URLs and download single files.


Early alpha
----
ytrss is still in a very early alpha stage. Many settings are hardcoded and bugs are to be expected.
Future versions will add:
- Better feeds with ID3 tags, pictures, ...
- AJAXified admin interface
- Password-secured admin interface
- ...


3rd party libraries
----
ytrss uses
- Spring
- Jetty
- Google Guava
- HSQLDB
- Jave - Java Audio Video Encoder
- Flyway
- Hibernate Validation
- ROME
- Gradle
