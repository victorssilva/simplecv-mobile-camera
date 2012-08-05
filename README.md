SimpleCV Mobile App
======================

Make use of SimpleCV on your mobile applications. This model Android app communicates with a Tornado web server, showing you how easy it is to take pictures on phones and get all the detection/manipulation work done on the cloud.

[This](<http://i.imgur.com/bWSFS.png>) is how things work.

[SimpleCV](<http://simplecv.org>) is a framework for Open Source Machine Vision, using OpenCV and the Python programming language.
It provides a concise, readable interface for cameras, image manipulation, feature extraction, and format conversion.  Our mission is to give casual users a comprehensive interface for basic machine vision functions and an elegant programming interface for advanced users.

---------------------------
## Setting things up

### SimpleCV

If you still haven't installed SimpleCV, go to [SimpleCV's Download Page](<http://simplecv.org/download/index.html>).

### Android SDK

You'll obviously need the [Android SDK](<http://developer.android.com/sdk/installing/index.html>). Follow all the steps and try to set up an emulator and get a Hello World working before you move on.

### Tornado

As tornado is listed in [PyPI](<http://pypi.python.org/pypi/tornado>), you can install it with *pip* or *easy_install*. 

---------------------------
## Running the app

### Android

After cloning the repository to your computer, in Eclipse, do:

1. File -> New -> Project
2. Android Project -> Click next
3. "Create project from existing source" and browse for the root folder of the source.

When it finishes up loading, you can finally run it! It'll open up the emulator you configured, and the app will be automatically installed and started. 

Newer versions of Android (probably > 2.3) are able to use your computer's webcam as a camera for the phone, so I'd suggest emulating those. 

Before you're able to upload and modify your pictures, there's only one more step: Running the server.

### Tornado

Just go to simplecv-mobile-camera/web-server and do

    python tornado_server.py


There you go!

---------------------------
## Getting help

Head over to SimpleCV's IRC channel if you have any questions.

\#SimpleCV on Freenode
