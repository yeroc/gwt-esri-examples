gwt-esri-examples
=================
A simple example showing how to use the [gwt-esri](https://github.com/CSTARS/gwt-esri) API.

## Trying it out
On a Unix-like operating system:
```
git clone https://github.com/yeroc/gwt-esri-examples.git
# for now you'll also need to check out the gwt-esri library as it isn't available via 
# Maven Central
git clone https://github.com/CSTARS/gwt-esri.git
# install gwt-esri into your local repository first
cd gwt-esri
mvn install
# if this didn't fail then built/package up this example
cd ../gwt-esri-examples
mvn package
# the previous command will create a war file named target/gwt-esri-examples.war which 
# you can deploy to your favourite web container that supports Servlet 3

