package com.example.rider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import static com.example.rider.RegisterActivity.TAG;

public class ProfileFragment extends Fragment {

    TextView profileFirstName,profileLastName, profileEmail, profilePhone, profileAddress;
    Button updateButton, cancelButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.activity_profile_fragment, container, false);
        profileFirstName =(TextView) v.findViewById(R.id.profile_firstname_id);
        profileLastName =(TextView) v.findViewById(R.id.profile_lastname_id);
        profileEmail = (TextView) v.findViewById(R.id.profile_email_id);
        profilePhone = (TextView) v.findViewById(R.id.profile_phone_id);
        profileAddress = (TextView) v.findViewById(R.id.profile_address_id);
        updateButton = (Button) v.findViewById(R.id.updateBtn_id);
        fAuth = FirebaseAuth.getInstance();
        fStore  = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
           Log.d(TAG, "onCreate: \"+user.getDisplayName()");
        }
        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                profileAddress.setText(documentSnapshot.getString("address"));
                profileEmail.setText(documentSnapshot.getString("email"));
                profilePhone.setText(documentSnapshot.getString("phoneNumber"));
                profileFirstName.setText(documentSnapshot.getString("firstName"));
                profileLastName.setText(documentSnapshot.getString("lastName"));
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
        return v;
    }
    @Override
    public void onStart() {
        super.onStart();
        userID = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()){
                            String firstNameResult  = task.getResult().getString("firstName");
                            String lastNameResult  = task.getResult().getString("lastName");
                            String emailResult  = task.getResult().getString("email");
                            String addressResult  = task.getResult().getString("address");
                            String phoneResult  = task.getResult().getString("phoneNumber");

                            profileFirstName.setText(firstNameResult);
                            profileLastName.setText(lastNameResult);
                            profileEmail.setText(emailResult);
                            profilePhone.setText(phoneResult);
                            profileAddress.setText(addressResult);

                        }
                        else {
                            Toast.makeText(getContext(), "No profile", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }
    private void updateProfile() {
        final String firstName = profileFirstName.getText().toString();
        final String lastName = profileLastName.getText().toString();
        final String address = profileAddress.getText().toString();
        final String email = profileEmail.getText().toString();
        final String phoneNumber = profilePhone.getText().toString();

        final  DocumentReference sDoc  = fStore.collection("users").document(userID);

        fStore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                transaction.update(sDoc,"firstName",firstName);
                transaction.update(sDoc,"lastName",lastName);
                transaction.update(sDoc,"email",email);
                transaction.update(sDoc,"phoneNumber",phoneNumber);
                transaction.update(sDoc,"address",address);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "successs", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}