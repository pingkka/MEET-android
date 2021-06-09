package com.example.LastCapston.home;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.LastCapston.R;
import com.example.LastCapston.databinding.FragmentHomeBinding;

import com.example.LastCapston.main.Permission;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding = null;
    private Permission permission;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        permissionCheck();



        binding.createRoomBTN.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_homeFragment_to_createRoomFragment);
        });

        binding.enterRoomBTN.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_homeFragment_to_enterRoomFragment);
        });

        return binding.getRoot();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void permissionCheck() {
        // SDK 23버전 이하 버전에서는 Permission이 필요하지 않음
        if(Build.VERSION.SDK_INT >= 23) {
            permission = new Permission(getActivity(), requireContext());

            if(!permission.checkPermission()) {
                permission.requestPermission();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 권한 허용을 거부한 경우, 재요청
        if(!permission.permissionResult(requestCode, permissions, grantResults)) {
            permission.requestPermission();
        }
    }



}