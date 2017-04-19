package me.pritam.rltl.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import me.pritam.rltl.L;
import me.pritam.rltl.R;
import me.pritam.rltl.RecyclerViewItemClick;
import me.pritam.rltl.activities.MapsActivity;
import me.pritam.rltl.adapters.GroupsAdapter;
import me.pritam.rltl.models.InfoGroup;

public class GroupsFragment extends Fragment {
    private String username;
    private RecyclerView recyclerView;
    private ImageView emptyImage;
    private GroupsAdapter adapter;
    private ProgressBar progressBar;
    private List<InfoGroup> infoGroups;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        infoGroups = new ArrayList<>();
        username = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        emptyImage = (ImageView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        getData();
        return view;
    }

    private void setUpView(final List<InfoGroup> infos) {
        this.infoGroups = infos;
        progressBar.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemDecorator());
        adapter = new GroupsAdapter(infoGroups, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClick(getActivity(), recyclerView, new RecyclerViewItemClick.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("infoName", infoGroups.get(position).getGrpFullName());
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete Group " + infoGroups.get(position).getGrpName());
                builder.setMessage("Group " + infoGroups.get(position).getGrpName() + " will permanently deleted.");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteGroup(infoGroups.get(position).getGrpFullName(), position);


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }));
    }


    private void deleteGroup(final String grpName, final int position) {
        DatabaseReference drf = FirebaseDatabase.getInstance().getReference("groups").child(grpName).child("members");
        final List<String> membersList = new ArrayList<>();
        drf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                    };

                    Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);
                    membersList.addAll(map.values());
                    FirebaseDatabase.getInstance().getReference("groups").child(grpName).removeValue();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                    for (final String user : membersList) {
                        final DatabaseReference drfs = databaseReference.child(user).child("groups");
                        drfs.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                                    };

                                    Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);
                                    for (Map.Entry<String, String> e : map.entrySet()) {
                                        String key = e.getKey();
                                        String value = e.getValue();
                                        L.logD("Checking " + value);
                                        if (value.equals(grpName)) {
                                            L.logD("Deleting Value from users list");
                                            dataSnapshot.child(key).getRef().removeValue();
                                            removeItem(position);
                                        }
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeItem(int position) {
        infoGroups.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, infoGroups.size() - position);
    }

    private void getData() {
        final List<InfoGroup> infoGroups = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(username).child("groups");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyImage.setVisibility(View.GONE);
                    GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                    };

                    Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);
                    for (String st : map.values()) {
                        InfoGroup infoGroup = new InfoGroup();
                        String str[] = st.split("-");
                        infoGroup.setGrpFullName(st);
                        infoGroup.setGrpName(str[0]);
                        //date
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(str[1]));

                        int mYear = calendar.get(Calendar.YEAR);
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int hour = calendar.get(Calendar.HOUR);
                        infoGroup.setGrpTime(mDay + "-" + mMonth + "-" + mYear);

                        infoGroups.add(infoGroup);
                    }
                    setUpView(infoGroups);

                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onUpdate() {
        getData();
    }

    public class ItemDecorator extends RecyclerView.ItemDecoration {


        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = 2;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildPosition(view) == 0)
                outRect.top = 0;
        }
    }

}
