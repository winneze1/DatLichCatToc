package com.example.datlichcattoc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datlichcattoc.Common.Common;
import com.example.datlichcattoc.Fragments.HomeFragment;
import com.example.datlichcattoc.Fragments.ShoppingFragment;
import com.example.datlichcattoc.Helper.LocaleHelper;
import com.example.datlichcattoc.Model.User;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {


    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;

    CollectionReference userRef;


    AlertDialog dialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase,"en"));
    }

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            super.onBackPressed();
            return;
        }
        else { Toast.makeText(getBaseContext(), "Touch again to exit", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Kiểm tra rating dialog
        checkRatingDialog();
    }

    private void checkRatingDialog() {
        Paper.init(this);
        String dataSerialized = Paper.book().read(Common.RATING_INFORMATION_KEY, "");
        if (!TextUtils.isEmpty(dataSerialized)) //nếu không null
        {
            Map<String,String> dataReceived = new Gson()
                    .fromJson(dataSerialized, new TypeToken<Map<String,String>>(){}.getType());
            if (dataReceived != null)
            {
                Common.showRatingDialog(HomeActivity.this,
                        dataReceived.get(Common.RATING_DISTRICT_KEY),
                        dataReceived.get(Common.RATING_SALON_ID),
                        dataReceived.get(Common.RATING_SALON_NAME),
                        dataReceived.get(Common.RATING_BARBER_ID));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);

        //Init
        userRef = FirebaseFirestore.getInstance().collection("User");
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        //Kiem tra intent, neu da dang nhap thi set = true, full access
        //neu dang nhap false thi chi cho user xem hang thoi

        if (getIntent()!= null)
        {
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN,false);
            if (isLogin){
                dialog.show();

                //Kiem tra user da ton tai
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Paper.init(HomeActivity.this);
                Paper.book().write(Common.LOGGED_KEY,user.getPhoneNumber());
                //Log.d("Phone", ""+user.getPhoneNumber());

                            DocumentReference currentUser = userRef.document(user.getPhoneNumber());
                            currentUser.get()
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful())
                                        {
                                            DocumentSnapshot userSnapShot = task.getResult();
                                            if(!userSnapShot.exists()){
                                                showUpdateDialog(user.getPhoneNumber());
                                            }
                                            else {
                                                //Neu user da ton tai
                                                Common.currentUser = userSnapShot.toObject(User.class);
                                                bottomNavigationView.setSelectedItemId(R.id.action_home);
                                                Log.d("hello","3");
                                            }
                                            if (dialog.isShowing())
                                                dialog.dismiss();


                                        }
                                    });
                        }

            }


        //View
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId()==R.id.action_home)
                    fragment = new HomeFragment();
                else if( menuItem.getItemId()== R.id.action_shopping)
                    fragment = new ShoppingFragment();
                return loadFragment(fragment);
            }
        });



    }




    private boolean loadFragment(Fragment fragment) {
        if(fragment != null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(final String phoneNumber)
    {


        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("One more step!!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information, null);

        Button btn_update = (Button)sheetView.findViewById(R.id.btn_update);
        final TextInputEditText edt_name = (TextInputEditText)sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edt_address = (TextInputEditText)sheetView.findViewById(R.id.edt_address);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edt_name.getText().length() > 0 && edt_address.getText().length() > 0
                        && !edt_name.getText().toString().trim().equals("") && !edt_address.getText().toString().trim().equals(""))
                {
                    btn_update.setEnabled(true);
                }
                else if (edt_address.getText().toString().trim().equals(""))
                {
                    btn_update.setEnabled(false);
                }
                else if (edt_name.getText().toString().trim().equals(""))
                {
                    btn_update.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        edt_name.addTextChangedListener(textWatcher);
        edt_address.addTextChangedListener(textWatcher);

        btn_update.setOnClickListener(v -> {

            if (!dialog.isShowing())
                dialog.show();

            final User user = new User(edt_name.getText().toString(),
                    edt_address.getText().toString(),
                    phoneNumber);
            userRef.document(phoneNumber)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        bottomSheetDialog.dismiss();
                        if (dialog.isShowing())
                            dialog.dismiss();

                        Common.currentUser = user;
                        bottomNavigationView.setSelectedItemId(R.id.action_home);

                        Toast.makeText(HomeActivity.this, "Thank you", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    bottomSheetDialog.dismiss();
                    if (dialog.isShowing())
                        dialog.dismiss();
                    Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}
