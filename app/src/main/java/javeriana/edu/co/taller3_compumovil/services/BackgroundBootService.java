package javeriana.edu.co.taller3_compumovil.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javeriana.edu.co.taller3_compumovil.MainActivity;
import javeriana.edu.co.taller3_compumovil.MapsActivity;
import javeriana.edu.co.taller3_compumovil.R;
import javeriana.edu.co.taller3_compumovil.pojos.User;

public class BackgroundBootService extends Service {

    private Handler handler;
    private Runnable runnable;

    // Realtime DB
    private DatabaseReference database;
    // Global listener is required so it can be removed later
    ChildEventListener userListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            // This method is called once for each child that is added
            User user = dataSnapshot.getValue(User.class);
            Log.d("RealtimeDB", "Listener -> User added: " + user.toString());
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            // This method is called whenever a child is updated
            User user = dataSnapshot.getValue(User.class);
            Log.d("RealtimeDB", "Listener -> User changed: "  + user.toString());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            // This method is called when a child is removed
            User user = dataSnapshot.getValue(User.class);
            Log.d("RealtimeDB", "Listener -> User removed: "  + user.toString());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            // This method is called when a child location is changed
            User user = dataSnapshot.getValue(User.class);
            Log.d("RealtimeDB", "Listener -> User moved: "  + user.toString());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting User failed, log a message
            Log.d("RealtimeDB", "Listener -> loadUser:onCancelled", databaseError.toException());
        }
    };

    public void initRealtimeDB() {
        database = FirebaseDatabase.getInstance().getReference();
        Log.d("RealtimeDB", "RealtimeDB init done.");
        subscribeToChanges();
    }

    public void subscribeToChanges() {
        // Add already defined listener
        database.child("users").addChildEventListener(userListener);
        Log.d("RealtimeDB", "Subbed to changes.");
    }

    public void unSubscribeToChanges() {
        // Add already defined listener
        database.child("users").removeEventListener(userListener);
        Log.d("RealtimeDB", "UNSubbed to changes.");
    }


    // Service stuff

    @Override
    public void onCreate() {
        super.onCreate();

        initRealtimeDB();

        Log.i("ListenerService", "BOOT Service has been started");

        handler = new Handler();
        runnable = () -> {
            Log.d("ListenerService", "Listener Service IS RUNNING -> Log message every 5 secs (It stops the process from dying)");
            handler.postDelayed(runnable, 5000);
        };
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    public void onDestroy() {

        Log.i("ListenerService", "BOOT Service has been stopped");
        Toast.makeText(this, "BOOT service stopped", Toast.LENGTH_LONG).show();

        unSubscribeToChanges();

    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("ListenerService", "BOOT Service has been started - onStartCommand");

        Toast.makeText(this, "ListenerService Started", Toast.LENGTH_LONG).show();

        handler.post(runnable);

        createNotificationChannel();
        Intent intent2 = new Intent(this, MainActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(com.google.firebase.database.R.drawable.common_google_signin_btn_text_dark)
                .setContentTitle("Taller 3!")
                .setContentText("La aplicacion esta corriendo de fondo, pendiente del estado de otros usuarios!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true); // Make the notification sticky


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

        startForeground(1, builder.build());


        return START_STICKY;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ListenerNotiChannel";
            String description = "ListenerNotiChannel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}