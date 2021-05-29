package com.example.last_capston.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.last_capston.CloudStorage;
import com.example.last_capston.R;
import com.example.last_capston.databinding.FragmentEnterRoomBinding;
import com.example.last_capston.main.MQTTClient;
import com.example.last_capston.main.MainViewModel;

import java.util.ArrayList;


public class EnterRoomFragment extends Fragment {

    private FragmentEnterRoomBinding binding = null;
    private MQTTClient client = MQTTClient.getInstance();
    private MainViewModel viewModel;
    private CloudStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEnterRoomBinding.inflate(inflater, container, false);
        viewModel = MainViewModel.getInstance();
        storage = new CloudStorage(getActivity(), viewModel);


        binding.enterOK.setOnClickListener(v -> {
            viewModel.setTopic(binding.enterRoomID.getText().toString());
            viewModel.setName(binding.enterUserID.getText().toString());
            viewModel.clickSubmit();
            storage.checkRoomBeforeEnter(binding.enterRoomID.getText().toString(), binding.enterRoomPW.getText().toString(), binding.enterUserID.getText().toString());
        });

        binding.enterExit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_homeFragment);
        });

        binding.enterHomeButton.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_homeFragment);
        });


        viewModel.userListLivedata.observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> strings) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_chatRoomFragment);
            }
        });

        return binding.getRoot();
    }
}