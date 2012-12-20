Environment - Application - Adaptation
======================================

Full source code for examples of the Environment-Application-Adaption architecture.

See also [http://eaa.heeere.com](http://eaa.heeere.com)


How to run
==========

The instructions provided are using the "bash" shell under linux. These should be adapted to your environment and operating system. You can create a folder to work.

    mkdir EAA
    cd EAA

Getting necessary files
-----------------------
You need to get the zip with all jars, the zip of omiscidgui and the sources from the git repository.

    wget http://eaa.heeere.com/data/all-jars-and-dependencies.zip
    wget http://eaa.heeere.com/data/omiscidgui-with-userdir.zip
    git clone git://github.com/twitwi/EnvironmentApplicationAdaptation.git
    unzip all-jars-and-dependencies.zip
    unzip omiscidgui-with-userdir.zip

Custom DNSSD Daemon
-------------------

The OMiSCID service oriented middleware uses DNSSD (also known as Bonjour, Zeroconf, mDNS).

An implementation is installed in most Linux distributions (avahi), under windows the bonjour/mdnsresponder software can be installed, which is installed under MacOS.
We also provide a pure Java implementation that can be used in case of need.

To increase robustness and speed, we start a DNSSD Proxy that has the advantage of working offline (contrary to the avahi daemon). Under linux, for it to still inter-operate with avahi, it must be run as root (to get access to the DBUS). Also some libraries need to be installed, e.g. using "apt" if under ubuntu/debian/...

    sudo apt-get install libunixsocket-java
    sudo OMISCID_DNSSD_FACTORY_VERBOSE_MODE=true java -cp lib/'*' fr.prima.omiscid.dnssd.server.OmiscidDnssdServer


Starting the Computer Exporter
------------------------------

    export PATH=${PATH}:tools
    export OMISCID_DNSSD_FACTORY=proxy
    java -cp lib/'*'  com.heeere.eaa.computerexporter.ComputerExportManager

Then, in the opened interface, export a display and mouse by clicking on the "+" button.
Also, if desired export other services.


Starting the Tic Tac Toe game
-----------------------------

    export OMISCID_DNSSD_FACTORY=proxy
    java -cp lib/'*' com.heeere.eaa.tictactoe.Main



Starting OMiSCID Gui
--------------------

    export OMISCID_DNSSD_FACTORY=proxy
    omiscidgui/bin/omiscidgui --userdir userdir



Starting some adapter factories
-------------------------------

    export OMISCID_DNSSD_FACTORY=proxy
    ./EnvironmentApplicationAdaptation/tools/xml-service.sh EnvironmentApplicationAdaptation/adapters/*.xml



Visualization and Orchestration in OMiSCID Gui
----------------------------------------------

In OMiSCID Gui, click on "Window > Binding Panel".
Then, in the service list on the left, click on any service, then right-click on it and select "Update Binding Panel". Note that, in this version, the binding panel does not update automatically, one have to do the "Update Binding Panel" action to refresh the panel when new services are added.
You should then see services and grayed-out adapters in the binding panel.

Now start all the adapters by double clicking on them from left to right.
Each time, wait for the adapter to become opaque, just to ensure no problem happens.
The started adapters allow both players to be played with the same mouse (from the exported display windows) and will display both the game and the mouse cursor on the exported display.
You can check this by moving the mouse in the display window, a cursor should be shown.
Now you can play the TicTacToe by clicking in the display windows (with the RIGHT button).


Other: computer vision button (needs a camera and opencl)
---------------------------------------------------------

    export OMISCID_DNSSD_FACTORY=proxy
    java -cp lib/'*' -Djna.nosys=true fr.prima.gsp.Launcher EnvironmentApplicationAdaptation/pipelines/clicklet-omiscid.xml in.uri=camera:0 in.width=320 in.height=240 l.level=2 diff.useNthFrame=2 clet.radius=50 ondet.onDelay=10 ondet.offDelay=10


Other: android controller
-------------------------

You need to get the apk file from [http://eaa.heeere.com](http://eaa.heeere.com), install it on the device and start it (EAA Exporter).
If everything goes fine and the device is on a wifi network with the computer, the services should appear in the binding panel after a refresh of it.
If you started (using the computer exporter) a presentation controller or a volume controller, the binding panel should propose some adapters.
Then, you can start adapters to use the volume buttons of your Android device to control either a presentation or 


Other: more adapters (not sure)
-------------------------------

More adapters might be available on the repository:

    mkdir adapters
    (cd adapters && ../EnvironmentApplicationAdaptation/tools/update-adapter-library.sh)
    export OMISCID_DNSSD_FACTORY=proxy

Now Ctrl+C the original script that started the adapter factories, and start the new ones (the framework does not remove duplicates).

    ./EnvironmentApplicationAdaptation/tools/xml-service.sh adapters/*.xml

Then update the binding panel in the OMiSCID Gui.
The binding panel can be zoomed in and out with the CTRL + mouse-wheel.

Started adapters can be killed by finding them in the service list and right clicking on them then "Kill Service". It is not very convenient yet but possible.


