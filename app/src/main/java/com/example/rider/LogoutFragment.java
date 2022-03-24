package com.example.rider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LogoutFragment extends Fragment implements View.OnClickListener {

    FirebaseAuth fAuth;
    ProgressDialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.activity_logout_fragment, container, false);
        Button yesBtn = (Button) v.findViewById(R.id.yesBtn_id);
        yesBtn.setOnClickListener(this);
        Button noBtn = (Button) v.findViewById(R.id.noBtn_id);
        noBtn.setOnClickListener(this);
        return v;
    }
    public void LogOut(View view) {
        fAuth.getInstance().signOut();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.yesBtn_id:
                Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                fAuth.getInstance().signOut();
                Intent i = new Intent(getActivity(),
                        UserOptions.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

                break;

            case R.id.noBtn_id:
                Toast.makeText(getContext(), "Good Decision", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), StudentSideNavBar.class);
                startActivity(intent);

                break;
        }
    }
}