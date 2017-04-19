package me.pritam.rltl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import me.pritam.rltl.R;
import me.pritam.rltl.models.InfoGroup;

/**
 * Created by ghost on 19/4/17.
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.Myholder> {
    private List<InfoGroup> infoGroups;
    private Context context;
    private static final int PICKFILE_REQUEST_CODE = 178;


    public GroupsAdapter(List<InfoGroup> infoGroups, Context context) {
        this.infoGroups = infoGroups;
        this.context = context;
    }

    @Override
    public Myholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Myholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_groups_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(Myholder holder, int position) {

        holder.tvGroupName.setText(infoGroups.get(position).getGrpName());
        holder.tvTime.setText(infoGroups.get(position).getGrpTime());
//        try {
//            Picasso.with(context).load(infoGroups.get(position).getGrpImageUrl())
//                    .resize(40, 40).into(holder.profileImage);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


//        holder.profileImage.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.setType("file/*");
//                    context.startActivityForResult(intent, PICKFILE_REQUEST_CODE);
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    v.performClick();
//                }
//                return false;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return infoGroups.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView tvGroupName;
        TextView tvTime;

        Myholder(View itemView) {
            super(itemView);
            profileImage = (ImageView) itemView.findViewById(R.id.profile_image);
            tvGroupName = (TextView) itemView.findViewById(R.id.tvGroupName);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
        }
    }


}
