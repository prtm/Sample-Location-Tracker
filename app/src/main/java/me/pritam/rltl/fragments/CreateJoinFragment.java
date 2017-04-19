package me.pritam.rltl.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.pritam.rltl.L;
import me.pritam.rltl.OnUpdate;
import me.pritam.rltl.R;

public class CreateJoinFragment extends Fragment implements View.OnClickListener {
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String username;
    private OnUpdate updateComm;
    boolean isPasswordMatchCreate = false;
    boolean isPasswordMatchJoin = false;
    ProgressDialog progressDialog;

    public CreateJoinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateComm= (OnUpdate) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_join_group, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(getActivity());
        auth = FirebaseAuth.getInstance();
        username = auth.getCurrentUser().getEmail().split("@")[0];

        ImageButton createGrp = (ImageButton) view.findViewById(R.id.createGrp);
        ImageButton joinGrp = (ImageButton) view.findViewById(R.id.joinGrp);
        createGrp.setOnClickListener(this);
        joinGrp.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createGrp:
                dialogBuild("Create Group", true);

                break;
            case R.id.joinGrp:
                dialogBuild("Join Group", false);

                break;
        }
    }


    private void dialogBuild(String title, boolean isCreateGrp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView);

        final EditText grpName = (EditText) dialogView.findViewById(R.id.grpName);
        final EditText passwd = (EditText) dialogView.findViewById(R.id.passwd);


        if (isCreateGrp) {
            //Create Grp logic
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    final String groupId = grpName.getText().toString() + "-" + Calendar.getInstance().getTimeInMillis();
                    final String grpNameString = grpName.getText().toString();
                    final String passwdString = passwd.getText().toString();
                    DatabaseReference drf = databaseReference.child("groups");
                    final Query query = drf.orderByKey().startAt(grpNameString);

                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(final DataSnapshot dataSnapshot, String str) {
                            if (dataSnapshot.exists() && dataSnapshot.hasChild("details")) {
                                GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                                };

                                Map<String, String> map = dataSnapshot.child("details").getValue(genericTypeIndicator);
                                if (map.containsKey(grpNameString)) {
                                    if (map.get(grpNameString).equals(passwdString)) {
                                        isPasswordMatchCreate = true;
                                    }

                                }

                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!isPasswordMatchCreate) {
                                databaseReference.child("users").child(username).child("groups").push().setValue(groupId);
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put(grpNameString, passwdString);
                                databaseReference.child("groups").child(groupId).child("details").setValue(hashMap);
                                databaseReference.child("groups").child(groupId).child("members").push().setValue(username);
                                L.shortToast(getActivity().getApplicationContext(), "Group Created Successfully");
                                updateComm.updateView();

                            } else {
                                isPasswordMatchCreate = false;
                                L.shortToast(getActivity().getApplicationContext(), "Group Already Exists");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

        } else {
            //Join Grp logic
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    final String grpNameString = grpName.getText().toString();
                    final String passwdString = passwd.getText().toString();
                    DatabaseReference drf = databaseReference.child("groups");
                    final Query query = drf.orderByKey().startAt(grpNameString);
                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(final DataSnapshot dataSnapshot, String str) {
                            if (dataSnapshot.exists() && dataSnapshot.hasChild("details")) {
                                L.logD("Join Success");
                                GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                                };

                                Map<String, String> map = dataSnapshot.child("details").getValue(genericTypeIndicator);
                                if (map.containsKey(grpNameString)) {
                                    if (map.get(grpNameString).equals(passwdString)) {
                                        isPasswordMatchJoin = true;
                                        //grp name and passwd matched now check if grp already exists
                                        Query query1 = databaseReference.child("users").child(username).child("groups").orderByValue().equalTo(dataSnapshot.getRef().getKey());
                                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dsnap) {
                                                if (dsnap.exists()) {
                                                    L.shortToast(getActivity().getApplicationContext(), "Already Joined Group");
                                                } else {
                                                    L.shortToast(getActivity().getApplicationContext(), "Joined Group Success");
                                                    updateComm.updateView();
                                                    dataSnapshot.getRef().child("members").push().setValue(username);
                                                    databaseReference.child("users").child(username).child("groups").push().setValue(dataSnapshot.getRef().getKey());

                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                }

                                if (!isPasswordMatchJoin) {
                                    L.shortToast(getActivity().getApplicationContext(), "Wrong GroupName or Password");
                                }

                            } else {
                                L.shortToast(getActivity().getApplicationContext(), "Wrong GroupName or Password");
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
        }
        AlertDialog b = builder.create();
        b.setCancelable(false);
        b.show();

    }
}
