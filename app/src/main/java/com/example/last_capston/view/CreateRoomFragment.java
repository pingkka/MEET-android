package com.example.last_capston.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.last_capston.CloudStorage;
import com.example.last_capston.R;
import com.example.last_capston.databinding.FragmentCreateRoomBinding;
import com.example.last_capston.main.MQTTClient;
import com.example.last_capston.main.MainViewModel;

import java.util.ArrayList;

public class CreateRoomFragment extends Fragment {

    private FragmentCreateRoomBinding binding = null;
    private MainViewModel viewModel;
    private CloudStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateRoomBinding.inflate(inflater, container, false);
        viewModel = MainViewModel.getInstance();
        storage = new CloudStorage(getActivity(), viewModel);


        binding.createBTN.setOnClickListener(v -> {
            viewModel.setTopic(binding.inputRoomID.getText().toString());
            viewModel.setName(binding.inputUserID.getText().toString());
            viewModel.clickSubmit();
            storage.checkRoomBeforeCreate(binding.inputRoomID.getText().toString(), binding.inputRoomPW.getText().toString(), binding.inputUserID.getText().toString());
        });

        binding.ereateExit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_homeFragment);
        });

        binding.createHomeButton.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_homeFragment);
        });


        viewModel.userListLivedata.observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> strings) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_createRoomFragment_to_chatRoomFragment);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Toast.makeText(getActivity(), "onDestroyView", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getActivity(), "onDestroy", Toast.LENGTH_SHORT).show();


    }
}










