package com.example.LastCapston.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.example.LastCapston.R;
import com.example.LastCapston.databinding.FragmentEnterRoomBinding;
import com.example.LastCapston.main.CloudStorage;
import com.example.LastCapston.main.MQTTClient;

import com.example.LastCapston.main.MainViewModel;


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

        /* 버튼클릭시 viewModel로 id정보를 넘겨주고 조건이 맞으면 방 입장 */
        binding.enterOK.setOnClickListener(v -> {
            viewModel.setTopic(binding.enterRoomID.getText().toString());
            viewModel.setName(binding.enterUserID.getText().toString());
            viewModel.clickSubmit();
            storage.checkRoomBeforeEnter(binding.enterRoomID.getText().toString(), binding.enterRoomPW.getText().toString(), binding.enterUserID.getText().toString());
        });

        /* 버튼 클릭시 홈 화면으로 이동 */
        binding.enterExit.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_homeFragment);
        });
        binding.enterHomeButton.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_homeFragment);
        });

        /* 방이 생성시 enterFlag값에 true를 넣어 화면 전환 */
        viewModel.enterFlag.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_enterRoomFragment_to_chatRoomFragment);
            }
        });

        return binding.getRoot();
    }


}