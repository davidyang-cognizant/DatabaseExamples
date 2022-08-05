package com.example.roomsdatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.roomsdatabase.adapter.ItemAdapter;
import com.example.roomsdatabase.adapter.PersonAdapter;
import com.example.roomsdatabase.broadcast.CustomReceiver;
import com.example.roomsdatabase.helper.FeedReaderContract;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Pineapple";
    FirebaseFirestore fbdb = FirebaseFirestore.getInstance();
    PersonAdapter personAdapter;

    private CustomReceiver mReceiver = new CustomReceiver();

    private ListView personListView;
    private RecyclerView personRecyclerView;
    private EditText nameEt;
    private EditText cityEt;
    private EditText filterEt;
    private PersonDatabase db;
    private ArrayAdapter arrayAdapter;
    private Spinner spinner;

    String[] options = {"Name", "City"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all variables
//        personListView = findViewById(R.id.personListView);
        personRecyclerView = findViewById(R.id.personRecyclerView);
        cityEt = findViewById(R.id.city_et);
        nameEt = findViewById(R.id.name_et);
        filterEt = findViewById(R.id.filter_et);
        spinner = findViewById(R.id.spinner);

        //Set up spinner
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(0);

        // Initialize local database
        db = PersonDatabase.getInstance(getApplicationContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, intentFilter);

        // Populate list view
        populateListFromFBStore();
    }

    // Function to tell the app to start getting
    // data from database on starting of the activity
    @Override protected void onStart()
    {
        super.onStart();
        personAdapter.startListening();
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override protected void onStop()
    {
        super.onStop();
        personAdapter.stopListening();
    }


    public void handleSubmit(View view) {
        // Insert
        switch (view.getId()) {
            case R.id.button:
//                insert();
                insertCP();
                break;
            case R.id.button2:
                fireBaseInsert();
        }
    }

    private void insertCP(){
        Uri uriEntries = Uri.parse("content://com.example.user.provider/users");
        ContentValues mapValues = new ContentValues();
        mapValues.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME, "Tiger Woods");
        mapValues.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CITY, "Orange County");
        getContentResolver().insert(uriEntries, mapValues);

    }

    /**
     * Insert a new object into the local database
     */
    private void insert() {
        if (!nameEt.getText().toString().equals("") && !cityEt.getText().toString().equals("")) {
            Person person = new Person(nameEt.getText().toString(), cityEt.getText().toString());
            db.personDao().insertPerson(person);
            populateList();
        }
    }

    /**
     * Inserting into Firestore Database
     */
    private void fireBaseInsert() {
        Map<String, Object> user = new HashMap<>();
        if (!nameEt.getText().toString().equals("") && !cityEt.getText().toString().equals("")) {
            user.put("name", nameEt.getText().toString());
            user.put("city", cityEt.getText().toString());

            fbdb.collection("users").add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                            populateListFromFBStore();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error adding document", e);
                }
            });
        }
    }

    /**
     * Populate the listview/recyclerview from firestore
     */
    private void populateListFromFBStore() {
        /**
         * FirebaseRecyclerOptions
         */
        Query query = fbdb.collection("users").orderBy("name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Person> options = new FirestoreRecyclerOptions.Builder<Person>()
                .setQuery(query, Person.class)
                .build();
        personAdapter = new PersonAdapter(options);
        personRecyclerView.setAdapter(personAdapter);
        /**
         * Firebase ListView
         */
//        fbdb.collection("users")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            List<String> arrayList = new ArrayList<>();
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                                Map<String, Object> data = document.getData();
//                                arrayList.add(data.get("name").toString() + ": " + data.get("city").toString());
//                                arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
//                                personListView.setAdapter(arrayAdapter);
//                            }
//                        } else {
//                            Log.w(TAG, "Error getting documents.", task.getException());
//                        }
//                    }
//                });
    }



    private void delete(Person person) {
        db.personDao().deletePerson(person);
    }

    private void update(Person person) {
        db.personDao().updatePerson(person);
    }

    private void populateList() {
        Toast.makeText(this, "Populating", Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Person> persons = db.personDao().getPersonList();
                List<String> arrayList = new ArrayList<>();
                for (Person person :
                        persons) {
                    arrayList.add(person.getName() + ": " + person.getCity());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * Populate with list view
                         */
//                        arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
//                        personListView.setAdapter(arrayAdapter);
                        /**
                         * Populate with recycler view
                         */
                        personRecyclerView.setAdapter(new ItemAdapter(getApplicationContext(), arrayList));
                        personRecyclerView.setHasFixedSize(true);

                    }
                });

            }
        });
    }

    /**
     * When the user tries to filter a name or city from the database, this will handle that.
     * @param view
     */
    public void handleFilter(View view) {
        String filter = filterEt.getText().toString();
        if (!filter.equals("")) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    final List<Person> persons;
                    if (spinner.getSelectedItem().toString().equals("Name")) {
                        persons = db.personDao().loadPersonByName(filter);
                    } else {
                        persons = db.personDao().loadPersonByCity(filter);
                    }
                    List<String> arrayList = new ArrayList<>();
                    for (Person person :
                            persons) {
                        arrayList.add(person.getName() + ": " + person.getCity());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
                            personListView.setAdapter(arrayAdapter);
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "No filter specified", Toast.LENGTH_SHORT).show();
        }
    }
}