package com.heeere.eaa.androidexporter;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import fr.prima.omiscid.dnssd.interf.DNSSDFactory;
import fr.prima.omiscid.user.connector.ConnectorListener;
import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.connector.Message;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.ServiceFilters;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscid.user.service.ServiceRepositoryListener;
import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import fr.prima.omiscid.user.util.Utility;
import fr.prima.omiscid.user.variable.VariableAccessType;

public class MainActivity extends Activity {

    public static String branding = "nb";
    ServiceFactory f;
    ServiceRepository repo;
    Service s;
    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                s.sendToAllClients("androidInited", Utility.message("Hi from " + branding));
            }
        });
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                KeyEvent event = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
//                s.sendToAllClients("androidWaiting", Utility.message(event.toString()));
//                s.sendToAllClients("androidInited", Utility.message(event.getCharacters()));
//                s.sendToAllClients("androidInited", Utility.message("" + event.getDownTime()));
//                s.sendToAllClients("androidInited", Utility.message("" + event.isLongPress()));
//                /*
//                 if (ke.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
//                 }*/
//
//            }
//        }, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
        /*
         View view = (View) this.findViewById(R.id.button);
         view.setOnKeyListener(new View.OnKeyListener() {
         public boolean onKey(View v, int keyCode, KeyEvent event) {
         if (keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) {
         s.sendToAllClients("androidWaiting", Utility.message(event.toString()));
         s.sendToAllClients("androidInited", Utility.message(event.getCharacters()));
         s.sendToAllClients("androidInited", Utility.message(""+event.getDownTime()));
         s.sendToAllClients("androidInited", Utility.message(""+event.isLongPress()));
         return true;
         }
         return false;
         }
         });*/

        handler.postDelayed(new Runnable() {
            public void run() {
                setUp();
            }
        }, 2000);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        s.sendToAllClients("events", Utility.message("KEY" + keyCode + (action == 0 ? "DOWN" : "UP")));

        s.sendToAllClients("androidWaiting", Utility.message(event.toString()));
        s.sendToAllClients("androidInited", Utility.message(event.getCharacters()));
        s.sendToAllClients("androidInited", Utility.message("" + event.getDownTime()));
        s.sendToAllClients("androidInited", Utility.message("" + event.isLongPress()));
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    //TODO
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    /**
     * Called when the activity is first created.
     */
    private void setUp() {
        DNSSDFactory.DefaultFactory.factoryToTryFirst = "fr.prima.omiscid.dnssd.jmdns.DNSSDFactoryJmdns";
        DNSSDFactory.DefaultFactory.verboseMode = true;
        DNSSDFactory.DefaultFactory.verboseModeMore = true;

        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock("HeeereDnssdLock");
        lock.setReferenceCounted(true);
        lock.acquire();

        f = new ServiceFactoryImpl();
        notifyUser("onStart " + branding);
        s = f.create("ServiceOnAndroid");
        notifyUser("s created");
        try {
            ConnectorListener logger = new ConnectorListener() {
                public void connected(Service service, String localConnectorName, int peerId) {
                    notifyUser("Connector " + localConnectorName + " connected to " + Utility.intTo8HexString(peerId));
                }

                public void messageReceived(Service service, String localConnectorName, Message message) {
                    notifyUser("Connector " + localConnectorName + " received from " + Utility.intTo8HexString(message.getPeerId()) + ": " + message.getBufferAsStringUnchecked());
                }

                public void disconnected(Service service, String localConnectorName, int peerId) {
                    notifyUser("Connector " + localConnectorName + " disconnected from " + Utility.intTo8HexString(peerId));
                }
            };
            s.addVariable("provides", "String", "provided functionalities", VariableAccessType.CONSTANT);
            s.setVariableValue("provides", "GenericEventSource");
            s.addConnector("events", "all exported events (string)", ConnectorType.OUTPUT);
            s.addConnector("androidInited", "da", ConnectorType.INOUTPUT);
            s.addConnector("androidWaiting", "da", ConnectorType.INOUTPUT);
            s.addConnectorListener("androidInited", logger);
            s.addConnectorListener("androidWaiting", logger);
            s.addConnectorListener("androidWaiting", new ConnectorListener() {
                public void messageReceived(Service service, String localConnectorName, Message message) {
                    notifyUser("Sending pong reply through connector " + localConnectorName + " to " + Utility.intTo8HexString(message.getPeerId()));
                    service.sendReplyToMessage(Utility.message("pong"), message);
                }

                public void disconnected(Service service, String localConnectorName, int peerId) {
                }

                public void connected(Service service, String localConnectorName, int peerId) {
                }
            });
            /*
             s.addConnector("androidInited", "da", ConnectorType.INOUTPUT);
             s.addConnectorListener("androidInited", new ConnectorListener() {
             public void messageReceived(Service service, String localConnectorName, Message message) {
             notifyUser("Message: "+message.getBufferAsStringUnchecked());
             }

             public void disconnected(Service service, String localConnectorName, int peerId) {
             notifyUser("deco");
             }

             public void connected(Service service, String localConnectorName, int peerId) {
             notifyUser("connected");
             }
             });
             notifyUser("androidInited added");
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
        s.start();
        notifyUser("s started");
        repo = f.createServiceRepository();
        repo.addListener(new ServiceRepositoryListener() {
            @Override
            public void serviceRemoved(ServiceProxy serviceProxy) {
                notifyUser("removed " + serviceProxy.getPeerIdAsString() + " " + serviceProxy.getName());
            }

            @Override
            public void serviceAdded(ServiceProxy serviceProxy) {
                notifyUser("added " + serviceProxy.getPeerIdAsString() + " " + serviceProxy.getName());
                s.connectTo("androidInited", serviceProxy, "hostWaiting");
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                notifyUser("gc'ed");
            }
        }, ServiceFilters.nameIs("ServiceOnPC"));
    }

    private void notifyUser(final String msg) {
        handler.postDelayed(new Runnable() {
            public void run() {

                TextView t = (TextView) findViewById(R.id.text);
                t.setText(msg + "\n=== " + t.getText());
            }
        }, 1);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //new Thread(){public void run() {setUp();}}.start();
    }

    @Override
    protected void onDestroy() {
        if (repo != null) {
            repo.stop();
        }
        if (s != null) {
            s.closeAllConnections();
            s.stop();
        }
        if (lock != null) {
            lock.release();
        }
        super.onStop();
    }
    /*
     WifiManager.MulticastLock lock;

     @Override
     public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     }

     private void setUp() { // to be called by onCreate
     // Omiscid should try directly jmds, not avahi, not mdns
     DNSSDFactory.DefaultFactory.factoryToTryFirst = "jmdns";
     DNSSDFactory.DefaultFactory.verboseMode = true;
     DNSSDFactory.DefaultFactory.verboseModeMore = true;

     WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
     lock = wifi.createMulticastLock("HeeereDnssdLock");
     lock.setReferenceCounted(true);
     lock.acquire();
     }

     @Override
     protected void onDestroy() {
     if (lock != null) {
     lock.release();
     }
     }*/
}
