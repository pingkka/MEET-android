package com.example.last_capston;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.last_capston.main.MainViewModel;
import com.google.firebase.components.Lazy;

import java.util.List;

import lombok.Setter;

@Setter
public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
//    public interface OnBackPressedListener {
//        void onBackPressed();
//    }
//    private OnBackPressedListener onBackPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setActionBarTitle();
//        setFragment();
    }



    public void setActionBarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

//    public void setFragment() {
//        MainFragment mainFragment = new MainFragment();
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.fragment, mainFragment);
//        transaction.commit();
//    }


//    @Override
//    public void onBackPressed() {
//        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
//        if (fragmentList != null) {
//            for(Fragment fragment : fragmentList) {
//                if(fragment instanceof OnBackPressedListener) {
//                    ((OnBackPressedListener)fragment).onBackPressed();
//                }
//            }
//        }
//
//        if (onBackPressedListener != null) {
//            onBackPressedListener.onBackPressed();
//        } else {
//            AlertDialog alertDialog = new AlertDialog.Builder(this)
//                    .setTitle("종료")
//                    .setMessage("앱을 종료하시겠습니까?")
//                    .setPositiveButton(R.string.ok, (dialog, which) -> {
//                        MainActivity.super.onBackPressed();
//                    })
//                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
//
//                    })
//                    .create();
//
//            alertDialog.show();
//        }
//    }
}